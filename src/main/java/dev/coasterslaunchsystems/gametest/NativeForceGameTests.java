package dev.coasterslaunchsystems.gametest;

import com.simibubi.create.content.trains.track.BezierConnection;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.runtime.NativeForces;
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
  private static final List<Probe> PROBES = new ArrayList<>();
  private static boolean registered;

  private static final class Probe {
    final ServerLevel level;
    final ServerSubLevel body;
    double seconds, impulse;
    boolean braking;
    Probe(ServerLevel level, ServerSubLevel body) { this.level = level; this.body = body; }
  }

  private static void physics(ForgeSablePrePhysicsTickEvent event) {
    for (var probe : List.copyOf(PROBES)) {
      if (probe.level != event.getPhysicsSystem().getLevel() || probe.body.isRemoved()) continue;
      var pipeline = event.getPhysicsSystem().getPipeline();
      double mass = probe.body.getMassTracker().getMass();
      double speed = pipeline.getLinearVelocity(probe.body, new Vector3d()).z;
      double dt = event.getTimeStep();
      double force = probe.braking ? DriveMath.brakeForce(speed, mass, mass * 6, dt) : mass * 2;
      NativeForces.pointForce(probe.body, probe.body.getMassTracker().getCenterOfMass(),
          new Vector3d(0, 0, force), dt);
      probe.seconds += dt;
      probe.impulse += force * dt;
    }
  }

  @GameTest(template = "empty", timeoutTicks = 160)
  public static void realRailPointForceLaunchAndFrictionStop(GameTestHelper helper) {
    if (!registered) {
      NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NativeForceGameTests::physics);
      registered = true;
    }
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
      var physics = SubLevelContainer.getContainer(level).physicsSystem();
      physics.updatePose(body);
      start.set(body.logicalPose().position());
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
      PROBES.remove(probe);
      SubLevelContainer.getContainer(level).removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).removeAnchorPeerCurve(level, to);
      level.removeBlock(from, false); level.removeBlock(to, false);
      helper.succeed();
    });
  }
}
