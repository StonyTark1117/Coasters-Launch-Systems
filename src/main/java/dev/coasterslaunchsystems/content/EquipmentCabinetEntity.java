package dev.coasterslaunchsystems.content;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.math.SpanInterval;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.Vec3;

/** A persisted module identity and binding. Native handles never enter block-entity saves. */
public final class EquipmentCabinetEntity extends KineticBlockEntity {
  private BlockPos controllerPosition;
  private UUID controllerIdentity;
  private UUID identity = UUID.randomUUID();
  private BlockPos from, to;
  private long geometry;
  private double start, end;
  private boolean armed;
  private Direction.Axis boundAxis;
  private String fault = "";
  private DirectedTrack track;
  private int revision = Integer.MIN_VALUE;
  private dev.coasterslaunchsystems.math.BrakeHeat heat = new dev.coasterslaunchsystems.math.BrakeHeat(4096, 8, 0);

  public EquipmentCabinetEntity(BlockPos pos, BlockState state) { super(EquipmentContent.ENTITY.get(), pos, state); }
  @Override public void tick() {
    super.tick();
    if (level instanceof ServerLevel server) {
      dev.coasterslaunchsystems.runtime.EquipmentRuntime.register(server, this);
      if (mechanism() == Mechanism.EDDY_BRAKE && server.getGameTime() % 20 == 0) sendData();
    }
  }
  public BlockPos controllerPosition() { return controllerPosition; }
  public UUID controllerIdentity() { return controllerIdentity; }
  public void linkController(BlockPos position, UUID id) {
    if (controllerIdentity != null && (!controllerIdentity.equals(id) || !controllerPosition.equals(position)))
      throw new IllegalArgumentException("Module already belongs to another controller");
    controllerPosition = position.immutable(); controllerIdentity = id; changed();
  }
  public void unlinkController(ServerLevel level) {
    if (armed || !dev.coasterslaunchsystems.runtime.RideControllers.editingAllowed(level, this))
      throw new IllegalArgumentException("Disarm an empty controller and module before unlinking");
    controllerPosition = null; controllerIdentity = null; changed();
  }
  public UUID identity() { return identity; }
  public boolean armed() { return armed; }
  public Mechanism mechanism() { return ((EquipmentCabinetBlock) getBlockState().getBlock()).mechanism; }
  public SpanInterval interval() { return new SpanInterval(start, end); }
  public String fault() { return fault; }
  public double heat() { return heat.heat(); }
  public double thermalFraction() { return heat.forceFraction(); }
  public void absorbHeat(double work) { if (work > 0) { heat.absorb(work); setChanged(); } }
  public void cool(double dt) { if (heat.heat() > 0) { heat.cool(dt); setChanged(); } }
  @Override public float calculateStressApplied() { return lastStressApplied = mechanism() == Mechanism.TIRE ? 16 : 0; }

  public void setArmed(boolean value) {
    if (value && from == null) throw new IllegalArgumentException("Use the span tool to bind this module first");
    if (value && !fault.isEmpty()) throw new IllegalArgumentException("Inspect and rebind the faulted module before arming");
    if (!value && level instanceof ServerLevel server && !dev.coasterslaunchsystems.runtime.RideControllers.editingAllowed(server, this))
      throw new IllegalArgumentException("Occupied controller equipment cannot be manually disarmed");
    armed = value; changed();
  }

  public void bind(ServerLevel level, BlockPos first, BlockPos second) {
    if (armed) throw new IllegalArgumentException("Disarm the module before rebinding");
    if (!dev.coasterslaunchsystems.runtime.RideControllers.editingAllowed(level, this))
      throw new IllegalArgumentException("Occupied or unavailable controller owns this binding");
    for (var pos : new BlockPos[] {worldPosition, first, second})
      if (Math.abs((long) pos.getX()) > 1024 || Math.abs((long) pos.getZ()) > 1024)
        throw new IllegalArgumentException("Native launch equipment currently requires track within 1024 blocks of origin");
    var resolved = DirectedTrack.resolve(level, first, second)
        .orElseThrow(() -> new IllegalArgumentException("Select the two anchors of one loaded fixed track edge"));
    var hit = CoasterPathTrackFrame.closestHitOnEdge(level, resolved.edge(), worldPosition.getCenter());
    if (hit.point().distanceToSqr(worldPosition.getCenter()) > 9)
      throw new IllegalArgumentException("Place the module within three blocks of its rail contact");
    double distance = resolved.distance(hit);
    // One physical module owns two blocks around its contact, not an entire long edge.
    double beginning = Math.max(0, distance - 1), ending = Math.min(resolved.length(), distance + 1);
    if (ending - beginning < .5) throw new IllegalArgumentException("Leave half a block of supported contact");
    // Tire drives remain level-only. Fin clamps permit unbanked slopes up to a 1-in-4 grade.
    var shaftAxis = getBlockState().getValue(RotatedPillarBlock.AXIS);
    var shaft = switch (shaftAxis) {
      case X -> new Vec3(1, 0, 0);
      case Y -> new Vec3(0, 1, 0);
      case Z -> new Vec3(0, 0, 1);
    };
    for (int sample = 0; sample <= 16; sample++) {
      var sampleHit = resolved.hitAtDistance(level, beginning + (ending - beginning) * sample / 16);
      var frame = CoasterPathTrackFrame.trackBasisAtHit(level, sampleHit);
      var tangent = resolved.forward(level, sampleHit);
      var unbankedUp = new Vec3(0, 1, 0).subtract(tangent.scale(tangent.y)).normalize();
      boolean slopeRated = mechanism() == Mechanism.FIN_BRAKE || mechanism() == Mechanism.EDDY_BRAKE;
      double slopeLimit = slopeRated ? .243 : .01;
      if (frame.up().dot(unbankedUp) < .999 || Math.abs(tangent.y) > slopeLimit
          || Math.abs(shaft.dot(tangent)) > .01 || shaftAxis == Direction.Axis.Y)
        throw new IllegalArgumentException(slopeRated
            ? "Brake module requires unbanked rail, at most 1-in-4 grade, and a transverse shaft"
            : "This module requires level unbanked rail and a transverse shaft");
    }
    from = first.immutable(); to = second.immutable(); geometry = resolved.fingerprint();
    start = beginning; end = ending; track = resolved;
    boundAxis = shaftAxis;
    revision = CoasterPathGraphManager.get(level).revision(); fault = ""; changed();
  }

  public DirectedTrack resolve(ServerLevel level) {
    if (from != null && (boundAxis != getBlockState().getValue(RotatedPillarBlock.AXIS)
        || dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.findSubLevelAt(level, worldPosition) != null)) {
      fail("Module orientation or fixed support changed; inspect and rebind"); return null;
    }
    if (from == null || to == null || !level.hasChunkAt(from) || !level.hasChunkAt(to)) {
      fail("Track support unavailable"); return null;
    }
    int current = CoasterPathGraphManager.get(level).revision();
    if (track == null || revision != current) {
      var resolved = DirectedTrack.resolve(level, from, to).orElse(null);
      if (resolved == null || resolved.fingerprint() != geometry || end > resolved.length()) {
        fail("Track geometry changed; inspect and rebind"); return null;
      }
      track = resolved; revision = current;
    }
    return track;
  }

  public void fail(String reason) {
    String message = reason == null || reason.isBlank() ? "Equipment validation failed" : reason;
    if (!fault.equals(message) || armed) { fault = message; armed = false; changed(); }
  }
  public String status() {
    return mechanism() + ": " + (armed ? "armed" : "disarmed") + ", " + getSpeed()
        + " RPM. " + padStatus()
        + (from == null ? "Select two track anchors, then use the span tool here." : "Contact " + start + ".." + end + ".")
        + (fault.isEmpty() ? "" : " " + fault);
  }
  private String padStatus() {
    if (mechanism() == Mechanism.EDDY_BRAKE) return "Passive magnets; no static hold. Heat " + Math.round(heat.heat())
        + "/4096. " + (heat.overheated() ? "Thermal force derated. " : "");
    if (mechanism() != Mechanism.FIN_BRAKE) return "";
    if (level instanceof ServerLevel server && controllerIdentity != null)
      return dev.coasterslaunchsystems.runtime.RideControllers.finReleased(server, this) ? "Controller granted pad release. " : "Controller spring pads applied when armed. ";
    return Math.abs(getSpeed()) >= 32 ? "Pads power-released. " : "Spring pads applied when armed. ";
  }
  private void changed() { setChanged(); sendData(); }
  @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean client) {
    super.write(tag, registries, client);
    tag.putInt("ls_schema", 3);
    if (controllerIdentity != null) { tag.putUUID("ls_controller", controllerIdentity); tag.putLong("ls_controller_pos", controllerPosition.asLong()); } tag.putDouble("ls_heat", heat.heat()); tag.putUUID("ls_identity", identity);
    tag.putBoolean("ls_armed", armed); tag.putString("ls_fault", fault);
    if (from != null) {
      tag.putLong("ls_from", from.asLong()); tag.putLong("ls_to", to.asLong());
      tag.putLong("ls_geometry", geometry); tag.putDouble("ls_start", start); tag.putDouble("ls_end", end);
      tag.putString("ls_axis", boundAxis.name());
    }
  }
  @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean client) {
    super.read(tag, registries, client);
    if (tag.contains("ls_schema") && tag.getInt("ls_schema") != 1 && tag.getInt("ls_schema") != 2 && tag.getInt("ls_schema") != 3) throw new IllegalArgumentException("Unsupported equipment schema");
    heat = new dev.coasterslaunchsystems.math.BrakeHeat(4096, 8, tag.getInt("ls_schema") >= 2 ? tag.getDouble("ls_heat") : 0);
    controllerIdentity = tag.hasUUID("ls_controller") ? tag.getUUID("ls_controller") : null;
    controllerPosition = controllerIdentity == null ? null : BlockPos.of(tag.getLong("ls_controller_pos"));
    if (tag.hasUUID("ls_identity")) identity = tag.getUUID("ls_identity");
    from = tag.contains("ls_from") ? BlockPos.of(tag.getLong("ls_from")) : null;
    to = tag.contains("ls_to") ? BlockPos.of(tag.getLong("ls_to")) : null;
    geometry = tag.getLong("ls_geometry"); start = tag.getDouble("ls_start"); end = tag.getDouble("ls_end");
    fault = tag.getString("ls_fault"); armed = tag.getBoolean("ls_armed");
    boundAxis = from == null ? null : Direction.Axis.valueOf(tag.getString("ls_axis"));
    if ((from == null) != (to == null) || (from == null && armed)
        || (from != null && (from.equals(to) || end - start > 2.001)))
      throw new IllegalArgumentException("Invalid equipment binding");
    if (from != null) interval();
    track = null; revision = Integer.MIN_VALUE;
  }
}
