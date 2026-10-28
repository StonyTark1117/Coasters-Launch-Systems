package dev.coasterslaunchsystems.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.runtime.EquipmentRuntime;
import dev.coasterslaunchsystems.runtime.NativeForces;
import dev.coasterslaunchsystems.runtime.VehicleInterfaces;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePostPhysicsTickEvent;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartSpawner;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3d;

/** Real Create seat passengers, not ballast substituted for occupied ordinary carts. */
@GameTestHolder("coasterslaunchsystems")
@PrefixGameTestTemplate(false)
public final class OrdinaryPassengerGameTests {
  private static final List<Scene> SCENES = new ArrayList<>();
  private static final java.util.Set<java.util.UUID> FOREIGN_OWNED = new java.util.HashSet<>();
  private static boolean ownerRegistered;
  private static boolean registered;
  private static final List<BlockPos> SEATS = List.of(new BlockPos(-1, 1, -1), new BlockPos(1, 1, -1),
      new BlockPos(-1, 1, 1), new BlockPos(1, 1, 1));

  static final class Scene {
    final GameTestHelper helper;
    final net.minecraft.server.level.ServerLevel level;
    final BlockPos from, to;
    final CoasterPathEdge edge;
    final List<ServerSubLevel> parents = new ArrayList<>();
    final List<Pig> riders = new ArrayList<>();
    Mechanism mechanism;
    DirectedTrack track;
    BlockPos module, motor;
    boolean reverse, entry;
    double initialSpeed, impulse, work, shaftWork, maxCouplerError, peakForce;
    int drivenSteps;
    String failure = "";
    boolean closed;
    Scene(GameTestHelper helper, int slot) { this(helper, slot, 208, -640, false); }
    Scene(GameTestHelper helper, int slot, int height, int zPosition) { this(helper, slot, height, zPosition, false); }
    Scene(GameTestHelper helper, int slot, int height, int zPosition, boolean floorMountedAnchors) {
      this.helper = helper; level = helper.getLevel();
      from = new BlockPos(32 + slot * 48, height, zPosition); to = from.offset(0, 0, 64);
      for (int x = from.getX() / 16 - 1; x <= from.getX() / 16 + 1; x++)
        for (int z = Math.floorDiv(from.getZ(), 16) - 1; z <= Math.floorDiv(to.getZ(), 16) + 1; z++) level.setChunkForced(x, z, true);
      var anchorState = SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState();
      if (floorMountedAnchors) anchorState = anchorState.setValue(dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlock.FACING, Direction.UP);
      for (var pos : List.of(from, to)) level.setBlockAndUpdate(pos, anchorState);
      var curve = new BezierConnection(Couple.create(from, to),
          Couple.create(CoasterAnchorpointBlockEntity.worldCenter(level, from), CoasterAnchorpointBlockEntity.worldCenter(level, to)),
          Couple.create(new Vec3(0, 0, 1), new Vec3(0, 0, -1)), Couple.create(new Vec3(0, 1, 0), new Vec3(0, 1, 0)), true, false, CoasterTrackMaterials.COASTER);
      ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).putAnchorPeerCurve(level, to, curve);
      edge = CoasterPathEdge.curve(from, to, curve);
    }
    ServerSubLevel cart(double distance) {
      var hit = new CoasterPathTrackFrame.GraphHit(from.getCenter().add(0, 0, distance), edge, distance / 64);
      // Use the upstream sampled world point, including its rail-height convention.
      hit = CoasterPathTrackFrame.closestHitOnEdge(level, edge, hit.point());
      return cart(hit);
    }
    ServerSubLevel cart(CoasterPathTrackFrame.GraphHit hit) {
      var body = CoasterCartSpawner.spawnMinimalContraption(level, hit.point(),
          CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(level, hit)), hit);
      parents.add(body);
      var plot = body.getPlot().getEmbeddedLevelAccessor();
      plot.setBlock(BlockPos.ZERO.below(), EquipmentContent.TIRE_SURFACE.get().defaultBlockState(), 3);
      plot.setBlock(new BlockPos(1, -1, 0), EquipmentContent.BRAKE_FIN.get().defaultBlockState(), 3);
      plot.setBlock(new BlockPos(-1, -1, 0), EquipmentContent.CONDUCTIVE_FIN.get().defaultBlockState(), 3);
      for (var bridge : List.of(new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1)))
        helper.assertTrue(plot.setBlock(bridge, Blocks.OAK_PLANKS.defaultBlockState(), 3), "Continuous ordinary deck did not install");
      for (var seat : SEATS) {
        helper.assertTrue(plot.setBlock(seat.below(), Blocks.IRON_BLOCK.defaultBlockState(), 3), "Ordinary seat deck did not install");
        helper.assertTrue(plot.setBlock(seat, AllBlocks.SEATS.get(DyeColor.BLUE).getDefaultState(), 3), "Ordinary Create seat did not install");
      }
      return body;
    }
    void installSkidShoe(ServerSubLevel body) {
      body.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(0, -2, -1), EquipmentContent.SKID_SHOE.get().defaultBlockState(), 3);
    }
    Pig board(ServerSubLevel body, int seat) {
      var pig = EntityType.PIG.create(level); pig.setNoAi(true); pig.setPos(from.getX(), from.getY() + 5, from.getZ() + 8);
      level.addFreshEntity(pig); SeatBlock.sitDown(level, body.getPlot().getCenterBlock().offset(SEATS.get(seat)), pig);
      helper.assertTrue(pig.isPassenger(), "Real ordinary passenger did not board"); riders.add(pig); return pig;
    }
    double mass(ServerSubLevel body) { return NativeForces.mass(level, MotionProviders.resolve(level, body.getUniqueId()).orElseThrow()); }
    double speed(ServerSubLevel body) { return SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(body, new Vector3d()).z; }
    EquipmentCabinetEntity cabinet() { return (EquipmentCabinetEntity) level.getBlockEntity(module); }
    Vector3d bearing(ServerSubLevel body) {
      SubLevelContainer.getContainer(level).physicsSystem().updatePose(body);
      return FourthDimensionForceGameTests.nativePoint(body, new Vector3d(body.getPlot().getCenterBlock().getX() + .5,
          body.getPlot().getCenterBlock().getY() + .5, body.getPlot().getCenterBlock().getZ() + .5));
    }
    void at(int tick, Runnable action) {
      helper.runAtTickTime(tick, () -> { try { action.run(); } catch (RuntimeException e) {
        System.err.println("LS_ORDINARY_FAILURE tick=" + tick); e.printStackTrace(); cleanup(); throw e;
      } });
    }
    void cleanup() {
      if (closed) return; closed = true; SCENES.remove(this);
      riders.forEach(Pig::discard);
      var container = SubLevelContainer.getContainer(level);
      for (var body : parents) {
        FOREIGN_OWNED.remove(body.getUniqueId());
        if (!body.isRemoved() && container.getSubLevel(body.getUniqueId()) == body)
          container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      }
      if (level.getBlockEntity(from) instanceof CoasterAnchorpointBlockEntity anchor) anchor.removeAnchorPeerCurve(level, to);
      level.removeBlock(from, false); level.removeBlock(to, false);
      if (module != null) { level.removeBlock(module, false); level.removeBlock(motor, false); }
    }
  }

  private static void beforePhysics(ForgeSablePrePhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      if (s.level != event.getPhysicsSystem().getLevel()) continue;
      try {
        double positiveWork = 0, totalForce = 0;
        for (var body : s.parents) {
          var motion = MotionProviders.resolve(s.level, body.getUniqueId()).orElseThrow();
          var contact = EngagedContacts.current(s.level, body).orElse(null);
          s.helper.assertTrue(contact != null && s.track.matches(contact.hit()), "Occupied ordinary car lost native contact");
          var axis = s.track.forward(s.level, contact.hit()); var direction = new Vector3d(axis.x, axis.y, axis.z);
          var group = body.getQueuedForceGroups() == null ? null : body.getQueuedForceGroups().get(ForceGroups.PROPULSION.get());
          var impulse = group == null ? new Vector3d() : new Vector3d(group.getForceTotal().getLocalForce());
          double force = impulse.rotate(body.logicalPose().orientation()).dot(direction) / event.getTimeStep();
          totalForce += Math.abs(force); s.impulse += Math.abs(force) * event.getTimeStep();
          if (impulse.lengthSquared() > 1e-18) {
            var point = switch (s.mechanism) {
              case TIRE -> VehicleInterfaces.tirePoint(body);
              case SKID_BRAKE -> VehicleInterfaces.skidPoint(body);
              default -> VehicleInterfaces.conductivePoint(body);
            };
            var dynamics = NativeForces.pointDynamics(body, event.getPhysicsSystem().getPipeline(), point, direction);
            double work = DriveMath.incrementalWork(dynamics.effectiveMass(), dynamics.speed(), force, event.getTimeStep());
            if (s.mechanism == Mechanism.TIRE) { positiveWork += Math.max(0, work); s.work += Math.max(0, work); }
            else {
              s.helper.assertTrue(force * dynamics.speed() <= 1e-8 && work <= 1e-8, "Occupied ordinary magnet supplied energy");
              s.work += Math.max(0, -work);
            }
            s.drivenSteps++;
          }
          if (s.mechanism == Mechanism.FIN_BRAKE && EquipmentRuntime.activeFinClamps(s.level, body.getUniqueId()) > 0) s.drivenSteps++;
          if (s.entry) NativeForces.pointForce(body, body.getMassTracker().getCenterOfMass(), direction.mul(s.mass(body) * (s.mechanism == Mechanism.TIRE ? 1 : 2)), event.getTimeStep());
        }
        s.peakForce = Math.max(s.peakForce, totalForce);
        if (s.mechanism == Mechanism.TIRE) {
          double budget = 8 * Math.abs(s.cabinet().getSpeed()) * 2 * Math.PI / 60 * event.getTimeStep(); s.shaftWork += budget;
          s.helper.assertTrue(positiveWork <= budget + 1e-8, "Occupied consist duplicated its tire shaft work budget");
        } else if (s.mechanism == Mechanism.EDDY_BRAKE || s.mechanism == Mechanism.SKID_BRAKE)
          s.helper.assertTrue(totalForce <= (s.mechanism == Mechanism.SKID_BRAKE ? 192 : 128) + 1e-8, "Occupied consist duplicated its passive brake force budget");
        s.helper.assertTrue(s.riders.stream().allMatch(Pig::isPassenger), "Equipment lost an ordinary passenger");
      } catch (RuntimeException e) { s.failure = e.toString(); }
    }
  }

  private static void afterPhysics(ForgeSablePostPhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      if (s.level != event.getPhysicsSystem().getLevel() || s.parents.size() != 2) continue;
      s.maxCouplerError = Math.max(s.maxCouplerError, Math.abs(s.bearing(s.parents.getFirst()).distance(s.bearing(s.parents.getLast())) - 6));
    }
  }

  @GameTestGenerator
  public static List<TestFunction> occupiedEquipment() {
    var tests = new ArrayList<TestFunction>(); int slot = 0;
    for (var mechanism : List.of(Mechanism.TIRE, Mechanism.FIN_BRAKE, Mechanism.SKID_BRAKE, Mechanism.EDDY_BRAKE))
      for (boolean linked : new boolean[] {false, true}) for (boolean reverse : new boolean[] {false, true}) {
        int selected = slot++;
        String name = mechanism.name().toLowerCase(java.util.Locale.ROOT) + (linked ? "_linked" : "_single") + (reverse ? "_reverse" : "_forward");
        tests.add(new TestFunction("launchsystems_ordinary_equipment", "coasterslaunchsystems.occupied_" + name,
            "coasterslaunchsystems:empty", 300, 0, true, helper -> equipment(new Scene(helper, selected), mechanism, linked, reverse)));
      }
    return tests;
  }

  private static void equipment(Scene s, Mechanism mechanism, boolean linked, boolean reverse) {
    if (!registered) {
      NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, OrdinaryPassengerGameTests::beforePhysics);
      NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, OrdinaryPassengerGameTests::afterPhysics); registered = true;
    }
    s.mechanism = mechanism; s.reverse = reverse;
    s.cart(8); if (linked) s.cart(14);
    if (mechanism == Mechanism.SKID_BRAKE) s.parents.forEach(s::installSkidShoe);
    var block = mechanism == Mechanism.TIRE ? EquipmentContent.TIRE : mechanism == Mechanism.FIN_BRAKE ? EquipmentContent.FIN
        : mechanism == Mechanism.SKID_BRAKE ? EquipmentContent.SKID : EquipmentContent.EDDY;
    s.module = s.from.offset(mechanism == Mechanism.EDDY_BRAKE ? -1 : 1, -2, reverse ? 7 : 9);
    s.motor = mechanism == Mechanism.TIRE ? s.module.west() : s.module.east();
    s.level.setBlockAndUpdate(s.module, block.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
    s.level.setBlockAndUpdate(s.motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, mechanism == Mechanism.TIRE ? Direction.EAST : Direction.WEST));
    ((CreativeMotorBlockEntity) s.level.getBlockEntity(s.motor)).generatedSpeed.setValue(0);
    double[] mass = new double[1];
    s.at(40, () -> {
      mass[0] = s.parents.stream().mapToDouble(s::mass).sum();
      for (var body : s.parents) for (int seat = 0; seat < SEATS.size(); seat++) s.board(body, seat);
    });
    s.at(50, () -> {
      s.helper.assertTrue(Math.abs(s.parents.stream().mapToDouble(s::mass).sum() - mass[0] - .8 * s.riders.size()) < 1e-6, "Occupied ordinary consist rider mass did not reconcile");
      if (linked) s.helper.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(s.level, s.parents.getFirst(), s.parents.getLast(), 6), "Native ordinary cars could not couple at six blocks");
      s.track = DirectedTrack.resolve(s.level, reverse ? s.to : s.from, reverse ? s.from : s.to).orElseThrow();
      s.cabinet().bind(s.level, reverse ? s.to : s.from, reverse ? s.from : s.to);
      SCENES.add(s);
    });
    s.at(60, () -> { s.entry = true; });
    s.at(80, () -> {
      s.entry = false; s.initialSpeed = s.speed(s.parents.getFirst());
      s.helper.assertTrue(s.initialSpeed * (reverse ? -1 : 1) > (mechanism == Mechanism.TIRE ? .7 : 1.4), "Occupied ordinary car did not enter through physical force");
      if (mechanism == Mechanism.TIRE) ((CreativeMotorBlockEntity) s.level.getBlockEntity(s.motor)).generatedSpeed.setValue(64);
      s.cabinet().setArmed(true);
    });
    s.at(160, () -> {
      s.helper.assertTrue(s.failure.isEmpty(), "Occupied native equipment sensor failed: " + s.failure);
      double speed = s.speed(s.parents.getFirst());
      s.helper.assertTrue(s.cabinet().fault().isEmpty() && s.drivenSteps > 20, "Occupied equipment never acted or faulted: " + s.cabinet().fault());
      if (mechanism == Mechanism.TIRE) s.helper.assertTrue(speed * (reverse ? -1 : 1) > Math.abs(s.initialSpeed) + .1 && s.work > .1 && s.work <= s.shaftWork + 1e-8, "Loaded rolling-entry tire transport failed");
      else if (mechanism == Mechanism.FIN_BRAKE) s.helper.assertTrue(Math.abs(speed) < .03, "Occupied ordinary fin did not stop/hold");
      else s.helper.assertTrue(Math.abs(speed) < Math.abs(s.initialSpeed) - .3 && s.work > 1, "Occupied ordinary passive trim failed");
      for (var body : s.parents) s.helper.assertTrue(!CoasterCartTrackSnap.isBearingDisengaged(body) && Math.abs(s.speed(body) - speed) < .1, "Occupied native consist lost support/common translation");
      s.helper.assertTrue(s.maxCouplerError < .05 && s.riders.stream().allMatch(Pig::isPassenger), "Occupied equipment lost coupler or riders");
      System.out.println("LS_ORDINARY_EQUIPMENT mechanism=" + mechanism + " reverse=" + reverse + " cars=" + s.parents.size()
          + " riders=" + s.riders.size() + " mass=" + s.parents.stream().mapToDouble(s::mass).sum() + " initial=" + s.initialSpeed + " speed=" + speed
          + " steps=" + s.drivenSteps + " impulse=" + s.impulse + " work=" + s.work + " shaft_work=" + s.shaftWork + " peak_force=" + s.peakForce + " max_coupler_error=" + s.maxCouplerError);
      if (mechanism == Mechanism.FIN_BRAKE) s.at(220, () -> {
        try { s.helper.assertTrue(Math.abs(s.speed(s.parents.getFirst())) < .03 && s.riders.stream().allMatch(Pig::isPassenger), "Occupied ordinary fin lost its final hold"); }
        finally { s.cleanup(); }
        s.helper.succeed();
      });
      else { s.cleanup(); s.helper.succeed(); }
    });
  }

  @GameTest(template = "empty", timeoutTicks = 320, batch = "launchsystems_ordinary_passengers")
  public static void nativePassengerMassLifecycle(GameTestHelper helper) {
    if (!ownerRegistered) {
      MotionProviders.register(new dev.coasterslaunchsystems.api.VehicleMotionProvider() {
        @Override public int apiVersion() { return API_VERSION; }
        @Override public String id() { return "coasterslaunchsystems:test_mass_owner"; }
        @Override public java.util.Optional<dev.coasterslaunchsystems.api.MotionVehicle> resolve(net.minecraft.server.level.ServerLevel level, java.util.UUID chassis) {
          return FOREIGN_OWNED.contains(chassis) ? java.util.Optional.of(new dev.coasterslaunchsystems.api.MotionVehicle(id(), chassis,
              List.of(chassis), List.of(chassis), java.util.Set.of(), false, false, "test:retained", 2, 256)) : java.util.Optional.empty();
        }
      });
      ownerRegistered = true;
    }
    var s = new Scene(helper, 13); var body = s.cart(8); var baseline = new double[1]; var point = new Vector3d(); var removedSeatMass = new double[1];
    s.at(40, () -> { baseline[0] = s.mass(body); point.set(s.bearing(body)); s.board(body, 0); });
    s.at(60, () -> {
      System.out.println("LS_ORDINARY_PASSENGER phase=board baseline=" + baseline[0] + " mass=" + s.mass(body));
      helper.assertTrue(Math.abs(s.mass(body) - baseline[0] - .8) < 1e-6, "Ordinary passenger omitted or duplicated native mass");
      helper.assertTrue(s.bearing(body).distance(point) < .01, "Boarding moved the native guide point instead of only changing COM bookkeeping");
      s.board(body, 1);
    });
    s.at(80, () -> {
      helper.assertTrue(Math.abs(s.mass(body) - baseline[0] - 1.6) < 1e-6, "Second passenger mass did not reconcile");
      point.set(s.bearing(body));
      System.out.println("LS_ORDINARY_REBUILD phase=before mass=" + body.getSelfMassTracker().getMass());
      helper.assertTrue(SubLevelContainer.getContainer(s.level).physicsSystem().recoverSubLevel(body), "Native body recovery failed");
      System.out.println("LS_ORDINARY_REBUILD phase=after mass=" + body.getSelfMassTracker().getMass() + " invalid=" + body.getSelfMassTracker().isInvalid());
    });
    s.at(100, () -> {
      System.out.println("LS_ORDINARY_REBUILD phase=later removed=" + body.isRemoved() + " mass=" + body.getMassTracker().getMass()
          + " bearing=" + body.getPlot().getEmbeddedLevelAccessor().getBlockState(BlockPos.ZERO));
      helper.assertTrue(Math.abs(s.mass(body) - baseline[0] - 1.6) < 1e-6, "Rebuilt native tracker omitted or replayed rider mass");
      System.out.println("LS_ORDINARY_RECOVERY displacement=" + s.bearing(body).distance(point) + " before=" + point + " after=" + s.bearing(body));
      helper.assertTrue(s.bearing(body).distance(point) < .01, "Mass reconstruction moved the supported chassis");
      s.riders.getFirst().stopRiding();
    });
    s.at(120, () -> {
      helper.assertTrue(Math.abs(s.mass(body) - baseline[0] - .8) < 1e-6, "Dismount left stale rider mass");
      SeatBlock.sitDown(s.level, body.getPlot().getCenterBlock().offset(SEATS.getFirst()), s.riders.getFirst());
    });
    s.at(140, () -> {
      helper.assertTrue(Math.abs(s.mass(body) - baseline[0] - 1.6) < 1e-6, "Reboarding duplicated rider mass");
      s.riders.forEach(Pig::stopRiding);
    });
    s.at(160, () -> {
      helper.assertTrue(Math.abs(s.mass(body) - baseline[0]) < 1e-6, "Empty seats retained physical rider mass");
      SeatBlock.sitDown(s.level, body.getPlot().getCenterBlock().offset(SEATS.getFirst()), s.riders.getFirst());
    });
    s.at(180, () -> {
      helper.assertTrue(Math.abs(s.mass(body) - baseline[0] - .8) < 1e-6, "Rider did not return before provider handoff");
      FOREIGN_OWNED.add(body.getUniqueId());
    });
    s.at(200, () -> {
      var snapshot = MotionProviders.resolve(s.level, body.getUniqueId()).orElseThrow();
      helper.assertTrue(snapshot.provider().equals("coasterslaunchsystems:test_mass_owner") && !snapshot.propulsionAllowed()
          && !snapshot.brakingAllowed() && Math.abs(s.mass(body) - baseline[0]) < 1e-6, "Ordinary rider mass survived foreign provider ownership");
      System.out.println("LS_ORDINARY_OWNERSHIP phase=foreign mass=" + s.mass(body) + " rider_retained=" + s.riders.getFirst().isPassenger());
      FOREIGN_OWNED.remove(body.getUniqueId());
    });
    s.at(240, () -> {
      helper.assertTrue(Math.abs(s.mass(body) - baseline[0] - .8) < 1e-6, "Returning ordinary ownership omitted or duplicated passenger mass");
      var seat = body.getPlot().getCenterBlock().offset(SEATS.getFirst());
      removedSeatMass[0] = dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper.getMass(s.level, seat, s.level.getBlockState(seat));
      body.getPlot().getEmbeddedLevelAccessor().setBlock(SEATS.getFirst(), Blocks.AIR.defaultBlockState(), 3);
    });
    s.at(280, () -> {
      try {
        helper.assertTrue(!s.riders.getFirst().isPassenger() && Math.abs(s.mass(body) - baseline[0] + removedSeatMass[0]) < 1e-6, "Removed seat retained its rider or stale mass");
        System.out.println("LS_ORDINARY_PASSENGER phase=complete baseline=" + baseline[0] + " removed_seat_mass=" + removedSeatMass[0] + " mass=" + s.mass(body));
      } finally { s.cleanup(); }
      helper.succeed();
    });
  }
}
