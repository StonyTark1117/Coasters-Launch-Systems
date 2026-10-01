package dev.coasterslaunchsystems.gametest;

import com.simibubi.create.content.trains.track.BezierConnection;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.runtime.NativeForces;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.joml.Vector3d;

/** Foundation test, intentionally independent of cart velocity commands or another addon. */
@GameTestHolder("coasterslaunchsystems")
@PrefixGameTestTemplate(false)
public final class NativeForceGameTests {
  static final List<Probe> PROBES = new ArrayList<>();
  private static boolean registered;

  static final class Probe {
    final ServerLevel level;
    final ServerSubLevel body;
    double seconds, impulse;
    boolean braking, reverse;
    DirectedTrack track;
    int contactSteps, missingSteps;
    int vetoSteps;
    Probe(ServerLevel level, ServerSubLevel body) { this.level = level; this.body = body; }
  }

  private static void physics(ForgeSablePrePhysicsTickEvent event) {
    for (var probe : List.copyOf(PROBES)) {
      if (probe.level != event.getPhysicsSystem().getLevel() || probe.body.isRemoved()) continue;
      var pipeline = event.getPhysicsSystem().getPipeline();
      var contact = EngagedContacts.current(probe.level, probe.body).orElse(null);
      if (contact == null || !probe.track.matches(contact.hit())) {
        probe.missingSteps++;
        continue;
      }
      probe.contactSteps++;
      var motion = MotionProviders.resolve(probe.level, probe.body.getUniqueId()).orElse(null);
      if (motion == null) { probe.missingSteps++; continue; }
      if (!motion.propulsionAllowed() || !motion.exclusiveOperator().isEmpty()) {
        probe.vetoSteps++;
        continue;
      }
      var tangent = probe.track.forward(probe.level, contact.hit());
      var direction = new Vector3d(tangent.x, tangent.y, tangent.z);
      double mass = NativeForces.mass(probe.level, motion);
      if (mass <= 0) { probe.missingSteps++; continue; }
      double speed = pipeline.getLinearVelocity(probe.body, new Vector3d()).dot(direction);
      double dt = event.getTimeStep();
      double forceLimit = Math.min(mass * motion.maximumAcceleration(), motion.maximumForce());
      double force = probe.braking ? DriveMath.brakeForce(speed, mass, forceLimit, dt)
          : Math.min(mass * 2, forceLimit) * (probe.reverse ? -1 : 1);
      NativeForces.pointForce(probe.body, probe.body.getMassTracker().getCenterOfMass(),
          direction.mul(force), dt);
      probe.seconds += dt;
      probe.impulse += force * dt;
    }
  }

  @GameTest(template = "empty", timeoutTicks = 260)
  public static void realRailPointForceLaunchAndFrictionStop(GameTestHelper helper) {
    registerPhysics();
    var level = helper.getLevel();
    var from = new BlockPos(32, 100, -128);
    var to = from.offset(0, 0, 64);
    for (int x = 1; x <= 3; x++)
      for (int z = -9; z <= -3; z++) level.setChunkForced(x, z, true);
    level.setBlockAndUpdate(from, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    level.setBlockAndUpdate(to, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var curve = new BezierConnection(Couple.create(from, to),
        Couple.create(CoasterAnchorpointBlockEntity.worldCenter(level, from),
            CoasterAnchorpointBlockEntity.worldCenter(level, to)),
        Couple.create(new Vec3(0, 0, 1), new Vec3(0, 0, -1)),
        Couple.create(new Vec3(0, 1, 0), new Vec3(0, 1, 0)), true, false,
        CoasterTrackMaterials.COASTER);
    ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).putAnchorPeerCurve(level, to, curve);
    var edge = CoasterPathEdge.curve(from, to, curve);
    var hit = CoasterPathTrackFrame.closestHitOnEdge(level, edge, from.getCenter().add(0, 0, 8));
    var body = CoasterCartSpawner.spawnMinimalContraption(level, hit.point(),
        CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(level, hit)),
        hit);
    var probe = new Probe(level, body);
    var start = new Vector3d();
    helper.runAtTickTime(40, () -> {
      helper.assertTrue(!body.getMassTracker().isInvalid(), "Native cart mass did not settle");
      var motion = MotionProviders.resolve(level, body.getUniqueId()).orElseThrow();
      helper.assertTrue(motion.provider().equals("simulatedcoasters:ordinary_0.1.5")
          && motion.bodies().equals(List.of(body.getUniqueId())), "Ordinary cart provider was not isolated");
      var physics = SubLevelContainer.getContainer(level).physicsSystem();
      physics.updatePose(body);
      start.set(body.logicalPose().position());
      probe.track = DirectedTrack.resolve(level, from, to).orElseThrow();
      var backwards = DirectedTrack.resolve(level, to, from).orElseThrow();
      helper.assertTrue(probe.track.fingerprint() == backwards.fingerprint(), "Direction changed rail identity");
      helper.assertTrue(Math.abs(probe.track.distance(hit) + backwards.distance(hit)
          - probe.track.length()) < .001, "Reversed span distance is not complementary");
      helper.assertTrue(probe.track.forward(level, hit).dot(backwards.forward(level, hit)) < -.999,
          "Reversed span does not reverse its force axis");
      PROBES.add(probe);
    });
    helper.runAtTickTime(80, () -> {
      var physics = SubLevelContainer.getContainer(level).physicsSystem();
      double speed = physics.getPipeline().getLinearVelocity(body, new Vector3d()).z;
      physics.updatePose(body);
      System.out.println("LS_NATIVE_POINT_FORCE phase=launch speed=" + speed + " seconds="
          + probe.seconds + " impulse=" + probe.impulse + " mass=" + body.getMassTracker().getMass()
          + " moved=" + body.logicalPose().position().distance(start));
      helper.assertTrue(speed > 2 && speed < 5, "Point force launch speed outside conservative envelope: " + speed);
      helper.assertTrue(body.logicalPose().position().distance(start) > 2, "Native cart did not move");
      helper.assertTrue(!CoasterCartTrackSnap.isBearingDisengaged(body), "Launch lost native engagement");
      probe.braking = true;
    });
    helper.runAtTickTime(120, () -> {
      var physics = SubLevelContainer.getContainer(level).physicsSystem();
      double speed = physics.getPipeline().getLinearVelocity(body, new Vector3d()).z;
      System.out.println("LS_NATIVE_POINT_FORCE phase=stop speed=" + speed + " impulse=" + probe.impulse);
      helper.assertTrue(Math.abs(speed) < .08, "Native friction stop failed: " + speed);
      helper.assertTrue(!CoasterCartTrackSnap.isBearingDisengaged(body), "Braking lost native engagement");
      probe.reverse = true;
      probe.braking = false;
      physics.updatePose(body);
      start.set(body.logicalPose().position());
    });
    helper.runAtTickTime(160, () -> {
      var physics = SubLevelContainer.getContainer(level).physicsSystem();
      double speed = physics.getPipeline().getLinearVelocity(body, new Vector3d()).z;
      physics.updatePose(body);
      System.out.println("LS_NATIVE_CONTACT phase=reverse speed=" + speed + " moved="
          + body.logicalPose().position().distance(start));
      helper.assertTrue(speed < -2 && speed > -5, "Reverse point force launch failed: " + speed);
      helper.assertTrue(body.logicalPose().position().distance(start) > 2, "Reverse launch did not move");
      probe.braking = true;
    });
    helper.runAtTickTime(200, () -> {
      var physics = SubLevelContainer.getContainer(level).physicsSystem();
      double speed = physics.getPipeline().getLinearVelocity(body, new Vector3d()).z;
      helper.assertTrue(Math.abs(speed) < .08, "Reverse friction stop failed: " + speed);
      helper.assertTrue(probe.missingSteps == 0 && probe.contactSteps >= 100 && probe.vetoSteps == 0,
          "Accepted native contact unavailable during drive: " + probe.missingSteps);
      System.out.println("LS_NATIVE_CONTACT phase=complete speed=" + speed
          + " contact_steps=" + probe.contactSteps + " missing_steps=" + probe.missingSteps);
      CoasterCartTrackSnap.markSpawnedDisengaged(body);
      helper.assertTrue(EngagedContacts.current(level, body).isEmpty(), "Disengaged cart retained force contact");
      PROBES.remove(probe);
      SubLevelContainer.getContainer(level).removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).removeAnchorPeerCurve(level, to);
      level.removeBlock(from, false); level.removeBlock(to, false);
      helper.succeed();
    });
  }

  static void registerPhysics() {
    if (!registered) {
      NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NativeForceGameTests::physics);
      registered = true;
    }
  }
}
