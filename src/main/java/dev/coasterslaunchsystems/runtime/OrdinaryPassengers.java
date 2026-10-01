package dev.coasterslaunchsystems.runtime;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.LevelEvent;

/** Equal declared seat loads on ordinary native bodies; articulated providers own their own loads. */
public final class OrdinaryPassengers {
  public static final double MASS_PER_RIDER = .8;
  private record Applied(MassTracker tracker, Map<BlockPos, Double> masses) {}
  private static final Map<ServerLevel, Map<ServerSubLevel, Applied>> APPLIED = new HashMap<>();
  private OrdinaryPassengers() {}

  /** Called after Sable builds block mass, before pipeline.add initializes a recovered body.
   * Pure tracker work: the native body may not exist yet, so never upload or call JNI here. */
  public static void rebuilt(ServerSubLevel body) {
    var world = APPLIED.get(body.getLevel());
    if (world == null) return;
    var previous = world.get(body); var tracker = body.getSelfMassTracker();
    if (previous == null || previous.tracker() == tracker || tracker.isInvalid()) return;
    for (var entry : previous.masses().entrySet())
      tracker.addBlockMass(body.getLevel(), Blocks.IRON_BLOCK.defaultBlockState(), entry.getKey(), entry.getValue(), new Vec3(.12, .06, .12));
    world.put(body, new Applied(tracker, new HashMap<>(previous.masses())));
  }

  public static void physics(ForgeSablePrePhysicsTickEvent event) {
    var level = event.getPhysicsSystem().getLevel();
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return;
    var owned = new HashSet<UUID>();
    for (var candidate : container.getAllSubLevels()) {
      if (candidate.isRemoved() || !CoasterCartResolve.isCartSubLevel(candidate)) continue;
      try {
        var motion = MotionProviders.resolve(level, candidate.getUniqueId()).orElse(null);
        if (motion != null && motion.provider().equals(OrdinaryMotionProvider.ID)) owned.addAll(motion.bodies());
      } catch (IllegalArgumentException | IllegalStateException unavailable) {
        // Failed or competing providers grant neither mass ownership nor ordinary fallback.
      }
    }
    var applied = APPLIED.computeIfAbsent(level, ignored -> new HashMap<>());
    applied.keySet().removeIf(body -> body.isRemoved() || container.getSubLevel(body.getUniqueId()) != body);
    for (var raw : container.getAllSubLevels()) {
      if (!(raw instanceof ServerSubLevel body) || body.isRemoved()) continue;
      if (!owned.contains(body.getUniqueId())) {
        if (applied.containsKey(body)) { update(body, applied, Map.of()); applied.remove(body); }
        continue;
      }
      var bounds = body.getPlot().getBoundingBox();
      var query = new AABB(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX() + 1,
          bounds.maxY() + 1, bounds.maxZ() + 1).inflate(.25);
      var desired = new HashMap<BlockPos, Double>();
      var seen = new HashSet<UUID>();
      for (var seat : level.getEntitiesOfClass(SeatEntity.class, query)) {
        if (seat.isRemoved() || !body.getPlot().contains(seat.position())
            || !(level.getBlockState(seat.blockPosition()).getBlock() instanceof SeatBlock)) continue;
        for (var passenger : seat.getPassengers()) {
          if (passenger.isRemoved() || !seen.add(passenger.getUUID())) continue;
          // A fixed game-scale torso load 1.5 blocks above the seat origin; no player attributes.
          desired.merge(seat.blockPosition().above(), MASS_PER_RIDER, Double::sum);
        }
      }
      update(body, applied, desired);
    }
  }

  private static void update(ServerSubLevel body, Map<ServerSubLevel, Applied> applied,
      Map<BlockPos, Double> desired) {
    var tracker = body.getSelfMassTracker();
    if (tracker.isInvalid()) { applied.remove(body); return; }
    var previous = applied.get(body);
    if (previous == null || previous.tracker() != tracker) {
      // Sable rebuilds a fresh block tracker on body restoration/recovery. Do not subtract
      // contributions that belonged to the old tracker or replay them on the new one.
      previous = new Applied(tracker, new HashMap<>()); applied.put(body, previous);
    }
    Set<BlockPos> positions = new HashSet<>(previous.masses().keySet()); positions.addAll(desired.keySet());
    boolean changed = false;
    for (var pos : positions) {
      double delta = desired.getOrDefault(pos, 0.0) - previous.masses().getOrDefault(pos, 0.0);
      if (Math.abs(delta) < 1e-9) continue;
      tracker.addBlockMass(body.getLevel(), Blocks.IRON_BLOCK.defaultBlockState(), pos, delta, new Vec3(.12, .06, .12));
      changed = true;
    }
    previous.masses().clear(); previous.masses().putAll(desired);
    // MergedMassTracker uploads inertia/mass and performs its native COM frame correction.
    if (changed) body.updateMergedMassData(1);
  }

  public static void unload(LevelEvent.Unload event) {
    if (event.getLevel() instanceof ServerLevel level) APPLIED.remove(level);
  }
}
