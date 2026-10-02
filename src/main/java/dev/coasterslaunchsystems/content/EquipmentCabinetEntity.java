package dev.coasterslaunchsystems.content;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.math.SpanInterval;
import dev.coasterslaunchsystems.runtime.NativeCableRig;
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
  private long handleRevision = Long.MIN_VALUE;
  private dev.coasterslaunchsystems.math.BrakeHeat heat = new dev.coasterslaunchsystems.math.BrakeHeat(4096, 8, 0);
  private dev.coasterslaunchsystems.math.EnergyBuffer buffer = new dev.coasterslaunchsystems.math.EnergyBuffer(dev.coasterslaunchsystems.math.LinearMotor.CAPACITY, 0);
  private dev.coasterslaunchsystems.math.FlywheelStore flywheel = new dev.coasterslaunchsystems.math.FlywheelStore(4, 50, 0);
  private dev.coasterslaunchsystems.math.HydraulicAccumulator hydraulic = new dev.coasterslaunchsystems.math.HydraulicAccumulator(4, 2, 250, 10, 1.4, 0);
  private dev.coasterslaunchsystems.math.AirReceiver pneumatic = new dev.coasterslaunchsystems.math.AirReceiver(4, 10, 200, 0);
  private boolean cableInUse;
  private boolean cableInstalled;
  private UUID cableCarriage;
  private UUID cableChassis;
  private double cableHome;
  private boolean cableMoving;
  private boolean cableReturning;
  private String cableReturnProvider = "";
  private NativeCableRig.Phase cablePhase = NativeCableRig.Phase.HOME;
  private double target = 3, accelerationLimit = 2, jerkLimit = 4;

  public EquipmentCabinetEntity(BlockPos pos, BlockState state) { super(EquipmentContent.ENTITY.get(), pos, state); }
  @Override public void tick() {
    super.tick();
    if (level instanceof ServerLevel server) {
      dev.coasterslaunchsystems.runtime.EquipmentRuntime.register(server, this);
      if ((mechanism() == Mechanism.EDDY_BRAKE || mechanism().linear() || mechanism().cable()) && server.getGameTime() % 20 == 0) sendData();
    }
  }
  public BlockPos controllerPosition() { return controllerPosition; }
  public UUID controllerIdentity() { return controllerIdentity; }
  public void linkController(BlockPos position, UUID id) {
    if (cableInUse || cableInstalled) throw new IllegalArgumentException("Inspect the retained cable carriage before changing its controller");
    if (controllerIdentity != null && (!controllerIdentity.equals(id) || !controllerPosition.equals(position)))
      throw new IllegalArgumentException("Module already belongs to another controller");
    controllerPosition = position.immutable(); controllerIdentity = id; changed();
  }
  void restoreController(RideControllerEntity.Peer previous, RideControllerEntity target) {
    if (!previous.identity().equals(controllerIdentity) || !previous.position().equals(controllerPosition)
        || target.armed() || !target.reservation().isEmpty()) throw new IllegalArgumentException("Inspected module ownership changed");
    controllerPosition = target.getBlockPos().immutable(); controllerIdentity = target.identity(); changed();
  }
  public void unlinkController(ServerLevel level) {
    if (armed || cableInUse || cableInstalled || !dev.coasterslaunchsystems.runtime.RideControllers.editingAllowed(level, this))
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
  @Override public float calculateStressApplied() { return lastStressApplied = mechanism().drive() || mechanism().cable() ? 16 : 0; }
  public double energy() { return buffer.energy(); }
  public dev.coasterslaunchsystems.math.FlywheelStore flywheel() {
    if (mechanism() != Mechanism.FLYWHEEL) throw new IllegalStateException("This cabinet is not a flywheel");
    return flywheel;
  }
  public dev.coasterslaunchsystems.math.HydraulicAccumulator hydraulic() {
    if (mechanism() != Mechanism.HYDRAULIC) throw new IllegalStateException("This cabinet is not hydraulic");
    return hydraulic;
  }
  public dev.coasterslaunchsystems.math.AirReceiver pneumatic() {
    if (mechanism() != Mechanism.PNEUMATIC) throw new IllegalStateException("This cabinet is not pneumatic");
    return pneumatic;
  }
  /** The controller uses the actual store, while the native rig retains the physical carriage. */
  private dev.coasterslaunchsystems.math.EnergyStore cableStore() {
    return switch (mechanism()) {
      case FLYWHEEL -> flywheel;
      case HYDRAULIC -> hydraulic;
      case PNEUMATIC -> pneumatic;
      default -> throw new IllegalStateException("This cabinet has no cable energy store");
    };
  }
  public double cableEnergy() { return cableStore().energy(); }
  /** Reserve the final fifth of a finite store for a bounded catch rather than engaging at a marginal charge. */
  public boolean cableReady() {
    var store = cableStore();
    double reserve = switch (mechanism()) {
      case FLYWHEEL -> 750;
      case HYDRAULIC -> store.capacity() * .8;
      case PNEUMATIC -> store.capacity() * .25;
      default -> throw new IllegalStateException("Not a cable store");
    };
    return store.energy() >= reserve;
  }
  public boolean cableInUse() { return cableInUse; }
  public boolean cableInstalled() { return cableInstalled; }
  public UUID cableCarriage() { return cableCarriage; }
  public UUID cableChassis() { return cableChassis; }
  public double cableHome() { return cableHome; }
  /** Saved from the last physical cable step; permits a bounded recovery-only moving catch. */
  public boolean cableMoving() { return cableMoving; }
  /** A released carriage remains owned until its shaft-powered physical return reaches home. */
  public boolean cableReturning() { return cableReturning; }
  public String cableReturnProvider() { return cableReturnProvider; }
  /** Live physical phase for installed-cabinet status; restart recovery remains governed by the persisted journal. */
  public NativeCableRig.Phase cablePhase() { return cablePhase; }
  /** Journal native carriage allocation before the controller can dispatch a catch. */
  public void installCable(NativeCableRig rig) {
    if (!mechanism().cable() || cableInstalled || cableInUse || !fault.isEmpty())
      throw new IllegalStateException("Cable carriage is already installed or awaiting inspection");
    if (rig.mechanism() != mechanism()) throw new IllegalStateException("Cable carriage mechanism does not match its cabinet");
    if (rig.phase() != NativeCableRig.Phase.HOME || rig.carriage().isRemoved())
      throw new IllegalStateException("Only an available home carriage can be installed");
    cableCarriage = rig.carriage().getUniqueId(); cableChassis = null; cableHome = rig.homeDistance(); cableInstalled = true;
    cablePhase = rig.phase(); changed();
  }
  public void retireCable(NativeCableRig rig) {
    if (!mechanism().cable() || !cableInstalled || cableInUse || rig.phase() != NativeCableRig.Phase.HOME
        || !rig.carriage().getUniqueId().equals(cableCarriage) || Math.abs(rig.homeDistance() - cableHome) > 1e-6
        || Math.abs(rig.travel()) >= .03 || Math.abs(rig.speed()) >= .03)
      throw new IllegalStateException("Only a physically home, unlatched cable carriage can be retired");
    rig.close(); cableInstalled = false; cableCarriage = null; cableChassis = null; cableHome = 0;
    cablePhase = NativeCableRig.Phase.HOME; changed();
  }
  public void finishInspectedCableRetirement(UUID carriage) {
    if (!mechanism().cable() || !cableInstalled || cableInUse || armed
        || cableCarriage == null || !cableCarriage.equals(carriage) || fault.isEmpty())
      throw new IllegalStateException("Saved cable installation changed during inspected retirement");
    cableInstalled = false; cableCarriage = null; cableChassis = null; cableHome = 0; cableMoving = false; cableReturning = false; cableReturnProvider = "";
    fault = "Idle carriage reclaimed by inspection; verify and rebind the guide before arming"; changed();
  }
  /** The operator has proved that a stopped captured train is held by an independent fin bank. */
  public void finishInspectedActiveRelease(UUID carriage, UUID chassis) {
    if (!mechanism().cable() || !cableInstalled || !cableInUse || armed
        || cableCarriage == null || !cableCarriage.equals(carriage) || cableChassis == null || !cableChassis.equals(chassis)
        || fault.isEmpty()) throw new IllegalStateException("Saved active catch changed during inspected release");
    cableInUse = false; cableChassis = null; cableMoving = false; cableReturning = false; cableReturnProvider = "";
    fault = "Active catch released by inspection; verify and rebind the guide before arming"; changed();
  }
  public void chargeFlywheel(double dt) {
    if (mechanism() == Mechanism.FLYWHEEL && !cableInUse && fault.isEmpty()
        && flywheel.charge(dev.coasterslaunchsystems.math.LinearMotor.chargingWork(getSpeed(), dt)) > 0) setChanged();
  }
  /** Hydraulic pumps move a closed oil loop; compression losses make its shaft-work rate distinct. */
  public void chargeHydraulic(double dt) {
    if (mechanism() == Mechanism.HYDRAULIC && !cableInUse && fault.isEmpty()
        && hydraulic.charge(.72 * dev.coasterslaunchsystems.math.LinearMotor.chargingWork(getSpeed(), dt)) > 0) setChanged();
  }
  /** Pneumatic compressors store less useful work per shaft tick and discharge through gas expansion. */
  public void chargePneumatic(double dt) {
    if (mechanism() == Mechanism.PNEUMATIC && !cableInUse && fault.isEmpty()
        && pneumatic.charge(.55 * dev.coasterslaunchsystems.math.LinearMotor.chargingWork(getSpeed(), dt)) > 0) setChanged();
  }
  /** The physical catch owns this cabinet's store until it returns home or enters inspection. */
  public void engageCable(NativeCableRig rig, UUID chassis) {
    if (!mechanism().cable() || cableInUse || !fault.isEmpty())
      throw new IllegalStateException("Cable store is unavailable or awaiting cable inspection");
    if (rig.mechanism() != mechanism()) throw new IllegalStateException("Cable carriage mechanism does not match its cabinet");
    rig.engage(chassis);
    cableChassis = chassis; cableInUse = true; cableMoving = false; cableReturning = false; cableReturnProvider = ""; cablePhase = rig.phase(); changed();
  }
  public void consumeFlywheel(double work) {
    if (mechanism() != Mechanism.FLYWHEEL || work > flywheel.energy() + 1e-8)
      throw new IllegalArgumentException("Flywheel cannot fund this work");
    if (flywheel.draw(work) > 0) setChanged();
  }
  public record CableStep(NativeCableRig.Step motion, double charged, double shaftBudget) {}
  public CableStep stepCable(NativeCableRig rig, double dt) {
    if (!mechanism().cable() || !cableInUse)
      throw new IllegalStateException("No cable catch is engaged");
    if (!fault.isEmpty()) {
      rig.emergencyHold(fault);
      observeCableMotion(rig);
      return new CableStep(new NativeCableRig.Step(0, 0, 0, 0, false), 0, 0);
    }
    double shaft = dev.coasterslaunchsystems.math.LinearMotor.chargingWork(getSpeed(), dt);
    var store = cableStore();
    double before = store.energy();
    var phase = rig.phase();
    var motion = rig.step(store, target, shaft, dt);
    observeCablePhase(rig);
    observeCableMotion(rig);
    double charged = 0;
    if (mechanism() == Mechanism.FLYWHEEL && phase != NativeCableRig.Phase.ENGAGED && phase != NativeCableRig.Phase.PULLING
        && rig.phase() != NativeCableRig.Phase.FAULT)
      charged = flywheel.charge(Math.max(0, shaft - Math.max(0, motion.work())));
    if (rig.phase() == NativeCableRig.Phase.HOME) { cableInUse = false; cableChassis = null; cableMoving = false; cableReturning = false; cableReturnProvider = ""; }
    if (before != store.energy() || !cableInUse) setChanged();
    return new CableStep(motion, charged, shaft);
  }
  /** Used by the direct native fixture as well as the cabinet's normal step loop. */
  public void observeCableMotion(NativeCableRig rig) {
    boolean next = cableInUse && rig.phase() == NativeCableRig.Phase.PULLING && Math.abs(rig.speed()) > .5;
    if (cableMoving != next) { cableMoving = next; changed(); }
  }
  /** Keeps the persisted journal aligned with release and return ownership for normal and fixture-driven rigs. */
  public void observeCablePhase(NativeCableRig rig) {
    var phase = rig.phase();
    boolean returning = phase == NativeCableRig.Phase.RELEASING || phase == NativeCableRig.Phase.RETURNING;
    boolean changed = cablePhase != phase || cableReturning != returning;
    cablePhase = phase;
    if (returning) {
      if (cableChassis != null) { cableChassis = null; changed = true; }
      if (cableMoving) { cableMoving = false; changed = true; }
      if (!rig.provider().equals(cableReturnProvider)) { cableReturnProvider = rig.provider(); changed = true; }
      cableReturning = true;
    }
    if (changed) changed();
  }
  /** Drops the widened restart-only inspection allowance once a reconstructed catch is physically settled. */
  public void finishRecoveredCableMotion(UUID carriage, UUID chassis) {
    if (!mechanism().cable() || !cableInstalled || !cableInUse || cableCarriage == null
        || !cableCarriage.equals(carriage) || cableChassis == null || !cableChassis.equals(chassis))
      throw new IllegalStateException("Saved cable catch changed before recovery could settle");
    if (cableMoving) { cableMoving = false; changed(); }
  }
  /** Completes only an inspected, released carriage return; it never restores a train catch or arms a controller. */
  public void finishRecoveredCableReturn(UUID carriage) {
    if (!mechanism().cable() || !cableInstalled || !cableInUse || !cableReturning || armed
        || cableCarriage == null || !cableCarriage.equals(carriage) || cableChassis != null || fault.isEmpty())
      throw new IllegalStateException("Saved return carriage changed before reaching its physical home");
    cableInUse = false; cableReturning = false; cableMoving = false; cableReturnProvider = ""; cablePhase = NativeCableRig.Phase.HOME;
    fault = "Cable return completed under inspection; verify and rebind the guide before arming"; changed();
  }
  public double targetSpeed() { return target; }
  public double accelerationLimit() { return accelerationLimit; }
  public double jerkLimit() { return jerkLimit; }
  public boolean linearPowered() { return mechanism().linear() && Math.abs(getSpeed()) >= 1 && Math.abs(getSpeed()) <= 128 && energy() > 1e-9; }
  public void chargeLinear(double dt) {
    if (mechanism().linear() && buffer.charge(dev.coasterslaunchsystems.math.LinearMotor.chargingWork(getSpeed(), dt)) > 0) setChanged();
  }
  public void spendLinear(double work) {
    dev.coasterslaunchsystems.math.Numbers.nonnegative(work, "Motor work");
    if (!mechanism().linear() || work > buffer.energy() + 1e-8) throw new IllegalArgumentException("Linear buffer cannot fund this step");
    if (buffer.draw(work) > 0) setChanged();
  }
  public void tuneLinear(double speed, double acceleration, double jerk) {
    if (!mechanism().linear() || armed || level instanceof ServerLevel server && !dev.coasterslaunchsystems.runtime.RideControllers.editingAllowed(server, this))
      throw new IllegalArgumentException("Disarm an empty linear drive and controller before tuning");
    dev.coasterslaunchsystems.math.Numbers.finite(speed, "Target speed");
    if (Math.abs(speed) > 12 || acceleration <= 0 || acceleration > 2 || jerk <= 0 || jerk > 8
        || !Double.isFinite(acceleration) || !Double.isFinite(jerk)) throw new IllegalArgumentException("Target must be -12..12, acceleration 0..2, jerk 0..8");
    target = speed; accelerationLimit = acceleration; jerkLimit = jerk; changed();
  }

  public void setArmed(boolean value) {
    if (value && mechanism().cable() && controllerIdentity == null)
      throw new IllegalArgumentException("Cable launch controls require a connected catch and controller");
    if (value && from == null) throw new IllegalArgumentException("Use the span tool to bind this module first");
    if (value && !fault.isEmpty()) throw new IllegalArgumentException("Inspect and rebind the faulted module before arming");
    if (!value && cableInUse) throw new IllegalArgumentException("Retained cable catch cannot be manually disarmed");
    if (!value && level instanceof ServerLevel server && !dev.coasterslaunchsystems.runtime.RideControllers.editingAllowed(server, this))
      throw new IllegalArgumentException("Occupied controller equipment cannot be manually disarmed");
    armed = value; changed();
  }

  public void bind(ServerLevel level, BlockPos first, BlockPos second) {
    if (armed || cableInUse || cableInstalled) throw new IllegalArgumentException("Disarm and inspect the cable catch before rebinding");
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
      boolean slopeRated = mechanism() == Mechanism.FIN_BRAKE || mechanism() == Mechanism.SKID_BRAKE || mechanism() == Mechanism.EDDY_BRAKE;
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
    revision = CoasterPathGraphManager.get(level).revision();
    handleRevision = dev.silvergold.simulatedcoasters.track.CoasterPeerCurveHandleLengths.revision(); fault = ""; changed();
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
    long handles = dev.silvergold.simulatedcoasters.track.CoasterPeerCurveHandleLengths.revision();
    if (track == null || revision != current || handleRevision != handles) {
      var resolved = DirectedTrack.resolve(level, from, to).orElse(null);
      if (resolved == null || resolved.fingerprint() != geometry || end > resolved.length()) {
        fail("Track geometry changed; inspect and rebind"); return null;
      }
      track = resolved; revision = current; handleRevision = handles;
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
    if (mechanism().cable())
      return cableLabel() + " " + Math.round(cableStore().energy()) + "/" + Math.round(cableStore().capacity()) + " work; "
          + (cableInUse ? "Catch retained for physical return or inspection. "
              : cableInstalled ? "Carriage installed at its physical home. "
              : controllerIdentity != null ? "Controller linked; awaiting a supported catch and guide. "
              : "Cable launch control is awaiting its catch/controller connection. ");
    if (mechanism().linear()) return "Target " + target + " blocks/s; acceleration " + accelerationLimit + ", jerk " + jerkLimit
        + ". Buffer " + Math.round(energy()) + "/2048; heat " + Math.round(heat.heat()) + "/4096. "
        + "Disarm; span tool + offhand clock changes target, iron nugget changes acceleration, gold nugget changes jerk. ";
    if (mechanism() == Mechanism.SKID_BRAKE) return "Passive skid friction; no static hold. Heat " + Math.round(heat.heat())
        + "/4096. " + (heat.overheated() ? "Thermal force derated. " : "");
    if (mechanism() == Mechanism.EDDY_BRAKE) return "Passive magnets; no static hold. Heat " + Math.round(heat.heat())
        + "/4096. " + (heat.overheated() ? "Thermal force derated. " : "");
    if (mechanism() != Mechanism.FIN_BRAKE) return "";
    if (level instanceof ServerLevel server && controllerIdentity != null)
      return dev.coasterslaunchsystems.runtime.RideControllers.finReleased(server, this) ? "Controller granted pad release. " : "Controller spring pads applied when armed. ";
    return Math.abs(getSpeed()) >= 32 ? "Pads power-released. " : "Spring pads applied when armed. ";
  }
  private String cableLabel() {
    return switch (mechanism()) {
      case FLYWHEEL -> "Flywheel";
      case HYDRAULIC -> "Hydraulic accumulator";
      case PNEUMATIC -> "Pneumatic receiver";
      default -> throw new IllegalStateException("Not a cable store");
    };
  }
  private void changed() { setChanged(); sendData(); }
  @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean client) {
    super.write(tag, registries, client);
    tag.putInt("ls_schema", 12);
    if (mechanism().cable()) {
      tag.putDouble("ls_flywheel_energy", flywheel.energy()); tag.putBoolean("ls_cable_in_use", cableInUse);
      tag.putBoolean("ls_cable_installed", cableInstalled);
      if (cableInstalled && cableCarriage != null) {
        tag.putUUID("ls_cable_carriage", cableCarriage); tag.putDouble("ls_cable_home", cableHome);
      }
      if (cableInUse && !cableReturning && cableChassis != null) tag.putUUID("ls_cable_chassis", cableChassis);
      tag.putBoolean("ls_cable_moving", cableMoving);
      tag.putBoolean("ls_cable_returning", cableReturning);
      if (cableReturning && !cableReturnProvider.isEmpty()) tag.putString("ls_cable_return_provider", cableReturnProvider);
    }
    if (mechanism().linear()) {
      tag.putDouble("ls_energy", energy()); tag.putDouble("ls_target", target);
      tag.putDouble("ls_acceleration", accelerationLimit); tag.putDouble("ls_jerk", jerkLimit);
    }
    if (mechanism() == Mechanism.HYDRAULIC) tag.putDouble("ls_hydraulic_work", hydraulic.energy());
    if (mechanism() == Mechanism.PNEUMATIC) tag.putDouble("ls_pneumatic_work", pneumatic.energy());
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
    if (tag.contains("ls_schema") && (tag.getInt("ls_schema") < 1 || tag.getInt("ls_schema") > 12)) throw new IllegalArgumentException("Unsupported equipment schema");
    heat = new dev.coasterslaunchsystems.math.BrakeHeat(4096, 8, tag.getInt("ls_schema") >= 2 ? tag.getDouble("ls_heat") : 0);
    controllerIdentity = tag.hasUUID("ls_controller") ? tag.getUUID("ls_controller") : null;
    controllerPosition = controllerIdentity == null ? null : BlockPos.of(tag.getLong("ls_controller_pos"));
    if (tag.hasUUID("ls_identity")) identity = tag.getUUID("ls_identity");
    from = tag.contains("ls_from") ? BlockPos.of(tag.getLong("ls_from")) : null;
    to = tag.contains("ls_to") ? BlockPos.of(tag.getLong("ls_to")) : null;
    geometry = tag.getLong("ls_geometry"); start = tag.getDouble("ls_start"); end = tag.getDouble("ls_end");
    fault = tag.getString("ls_fault"); armed = tag.getBoolean("ls_armed");
    if (mechanism() == Mechanism.SKID_BRAKE && !client) {
      armed = false;
      if (fault.isEmpty()) fault = "Skid brake paused after reload; inspect the shoe and rearm";
    }
    if (mechanism() == Mechanism.FLYWHEEL) {
      double stored = tag.getInt("ls_schema") >= 5 ? tag.getDouble("ls_flywheel_energy") : 0;
      if (!Double.isFinite(stored) || stored < 0 || stored > 5000)
        throw new IllegalArgumentException("Invalid saved flywheel energy");
      flywheel = new dev.coasterslaunchsystems.math.FlywheelStore(4, 50, Math.sqrt(stored / 2));
    }
    if (mechanism().cable()) {
      cableInUse = tag.getBoolean("ls_cable_in_use");
      cableInstalled = tag.getBoolean("ls_cable_installed");
      cableCarriage = cableInstalled && tag.hasUUID("ls_cable_carriage") ? tag.getUUID("ls_cable_carriage") : null;
      cableReturning = cableInUse && tag.getInt("ls_schema") >= 11 && tag.getBoolean("ls_cable_returning");
      cableReturnProvider = cableReturning && tag.getInt("ls_schema") >= 12 ? tag.getString("ls_cable_return_provider") : "";
      cableChassis = cableInUse && !cableReturning && tag.hasUUID("ls_cable_chassis") ? tag.getUUID("ls_cable_chassis") : null;
      cableHome = cableInstalled && tag.contains("ls_cable_home") ? tag.getDouble("ls_cable_home") : 0;
      cableMoving = cableInUse && tag.getInt("ls_schema") >= 8 && tag.getBoolean("ls_cable_moving");
      if (!Double.isFinite(cableHome) || cableHome < 0 || cableHome > 1024)
        throw new IllegalArgumentException("Invalid saved cable home distance");
      if (!client) {
        armed = false;
        if ((cableInUse || cableInstalled) && fault.isEmpty()) fault = cableInstalled && cableCarriage == null
            ? "Legacy cable installation needs inspected recovery after reload"
            : cableReturning && cableInstalled
                ? "Cable return needs inspected recovery after reload"
            : cableInUse && cableChassis == null && cableInstalled
                ? "Legacy active cable catch needs inspected recovery after reload"
                : "Cable catch needs inspected recovery after reload";
      }
    }
    if (mechanism().linear()) {
      buffer = new dev.coasterslaunchsystems.math.EnergyBuffer(dev.coasterslaunchsystems.math.LinearMotor.CAPACITY, tag.getInt("ls_schema") >= 4 ? tag.getDouble("ls_energy") : 0);
      double speed = tag.getInt("ls_schema") >= 4 ? tag.getDouble("ls_target") : 3;
      double acceleration = tag.getInt("ls_schema") >= 4 ? tag.getDouble("ls_acceleration") : 2;
      double jerk = tag.getInt("ls_schema") >= 4 ? tag.getDouble("ls_jerk") : 4;
      if (!Double.isFinite(speed) || Math.abs(speed) > 12 || !Double.isFinite(acceleration) || acceleration <= 0 || acceleration > 2
          || !Double.isFinite(jerk) || jerk <= 0 || jerk > 8) throw new IllegalArgumentException("Invalid saved linear settings");
      target = speed; accelerationLimit = acceleration; jerkLimit = jerk;
      // Saved energy and configuration never replay an enabled field after a server load.
      if (!client) armed = false;
    }
    if (mechanism() == Mechanism.HYDRAULIC) {
      double stored = tag.getInt("ls_schema") >= 9 ? tag.getDouble("ls_hydraulic_work") : 0;
      hydraulic = new dev.coasterslaunchsystems.math.HydraulicAccumulator(4, 2, 250, 10, 1.4, stored);
      if (!client) armed = false;
    }
    if (mechanism() == Mechanism.PNEUMATIC) {
      double stored = tag.getInt("ls_schema") >= 9 ? tag.getDouble("ls_pneumatic_work") : 0;
      pneumatic = new dev.coasterslaunchsystems.math.AirReceiver(4, 10, 200, stored);
      if (!client) armed = false;
    }
    boundAxis = from == null ? null : Direction.Axis.valueOf(tag.getString("ls_axis"));
    if ((from == null) != (to == null) || (from == null && armed)
        || (from != null && (from.equals(to) || end - start > 2.001)))
      throw new IllegalArgumentException("Invalid equipment binding");
    if (from != null) interval();
    track = null; revision = Integer.MIN_VALUE; handleRevision = Long.MIN_VALUE;
  }
}
