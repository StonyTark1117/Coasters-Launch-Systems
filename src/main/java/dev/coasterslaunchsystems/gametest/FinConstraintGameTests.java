package dev.coasterslaunchsystems.gametest;

import com.simibubi.create.content.trains.track.BezierConnection;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.runtime.NativeFinClamp;
import dev.coasterslaunchsystems.runtime.NativeForces;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartSpawner;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.joml.Vector3d;

/** Native solver proof before fin equipment certification; no velocity or pose setters. */
@GameTestHolder("coasterslaunchsystems")
@PrefixGameTestTemplate(false)
public final class FinConstraintGameTests {
  private static final List<Probe> PROBES = new ArrayList<>();
  private static boolean registered;
  private enum Fixture { FORWARD, REVERSE, SLOPE, OVERLOAD }
  private static final class Probe {
    final ServerLevel level;
    final ServerSubLevel body;
    final Fixture fixture;
    DirectedTrack track;
    NativeFinClamp clamp;
    double force, maxStopSpeed, minimumDirectedSpeed = Double.POSITIVE_INFINITY;
    boolean engage;
    int contactSteps, missing;
    String failure = "";
    Probe(ServerLevel level, ServerSubLevel body, Fixture fixture) { this.level = level; this.body = body; this.fixture = fixture; }
  }
  private static void physics(ForgeSablePrePhysicsTickEvent event) {
    for (var p : List.copyOf(PROBES)) {
      if (p.level != event.getPhysicsSystem().getLevel() || p.body.isRemoved()) continue;
      try {
        var contact = EngagedContacts.current(p.level, p.body).orElse(null);
        if (contact == null || !p.track.matches(contact.hit())) { p.missing++; continue; }
        p.contactSteps++;
        var axis = p.track.forward(p.level, contact.hit()); var direction = new Vector3d(axis.x, axis.y, axis.z);
        if (p.engage && p.clamp == null) {
          var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(p.body);
          p.clamp = new NativeFinClamp(event.getPhysicsSystem().getPipeline(), p.body,
              new Vector3d(bearing.getX()+.5, bearing.getY()-.0625, bearing.getZ()+.5), direction, 128);
        }
        if (p.clamp != null) {
          p.clamp.align(direction);
          double speed = event.getPhysicsSystem().getPipeline().getLinearVelocity(p.body, new Vector3d()).dot(direction);
          p.maxStopSpeed = Math.max(p.maxStopSpeed, Math.abs(speed));
          p.minimumDirectedSpeed = Math.min(p.minimumDirectedSpeed, speed * (p.fixture == Fixture.REVERSE ? -1 : 1));
        }
        if (p.force != 0) NativeForces.pointForce(p.body, p.body.getMassTracker().getCenterOfMass(),
            direction.mul(p.force), event.getTimeStep());
      } catch (RuntimeException e) { p.failure = e.toString(); }
    }
  }
  @GameTest(template="empty", timeoutTicks=420)
  public static void boundedClampStopsLoadedForward(GameTestHelper helper) { run(helper, Fixture.FORWARD); }
  @GameTest(template="empty", timeoutTicks=420)
  public static void boundedClampStopsLoadedReverse(GameTestHelper helper) { run(helper, Fixture.REVERSE); }
  @GameTest(template="empty", timeoutTicks=420)
  public static void boundedClampHoldsLoadedSlopeAndReleases(GameTestHelper helper) { run(helper, Fixture.SLOPE); }
  @GameTest(template="empty", timeoutTicks=420)
  public static void boundedClampSlipsUnderExcessForce(GameTestHelper helper) { run(helper, Fixture.OVERLOAD); }

  private static void run(GameTestHelper helper, Fixture fixture) {
    if (!registered) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, FinConstraintGameTests::physics); registered = true; }
    var level = helper.getLevel(); boolean slope = fixture == Fixture.SLOPE;
    var from = new BlockPos(64 + fixture.ordinal() * 32, 150, -640); var to = from.offset(0, slope ? 16 : 0, 64);
    for (int x = from.getX()/16-1; x <= from.getX()/16+1; x++) for (int z=-41; z<=-35; z++) level.setChunkForced(x,z,true);
    level.setBlockAndUpdate(from, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    level.setBlockAndUpdate(to, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var tangent = new Vec3(0, slope ? .25 : 0, 1).normalize();
    var up = new Vec3(0, 1, slope ? -.25 : 0).normalize();
    var curve = new BezierConnection(Couple.create(from,to), Couple.create(CoasterAnchorpointBlockEntity.worldCenter(level,from),
        CoasterAnchorpointBlockEntity.worldCenter(level,to)), Couple.create(tangent,tangent.scale(-1)), Couple.create(up,up), true,false,CoasterTrackMaterials.COASTER);
    ((CoasterAnchorpointBlockEntity)level.getBlockEntity(from)).putAnchorPeerCurve(level,to,curve);
    var edge=CoasterPathEdge.curve(from,to,curve);
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,from.getCenter().add(0,slope?8:0,32));
    var body=CoasterCartSpawner.spawnMinimalContraption(level,hit.point(),CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(level,hit)),hit);
    for (var cell:List.of(new BlockPos(0,1,0),new BlockPos(0,2,0),new BlockPos(-1,1,0),new BlockPos(1,1,0),new BlockPos(0,1,-1),new BlockPos(0,1,1)))
      helper.assertTrue(body.getPlot().getEmbeddedLevelAccessor().setBlock(cell,Blocks.IRON_BLOCK.defaultBlockState(),3),"Fin ballast did not install");
    var probe=new Probe(level,body,fixture); var stopped=new Vector3d();
    helper.runAtTickTime(30,()->{
      probe.track=DirectedTrack.resolve(level,from,to).orElseThrow();
      var physics=SubLevelContainer.getContainer(level).physicsSystem(); physics.updatePose(body);
      var motion=MotionProviders.resolve(level,body.getUniqueId()).orElseThrow();
      helper.assertTrue(NativeForces.mass(level,motion)>=25,"Fin fixture omitted native ballast");
      PROBES.add(probe);
      if (slope) probe.engage=true;
      else probe.force=fixture==Fixture.REVERSE ? -52 : 52;
    });
    helper.runAtTickTime(70,()->{
      var physics=SubLevelContainer.getContainer(level).physicsSystem(); physics.updatePose(body);
      if (!slope) {
        double speed=physics.getPipeline().getLinearVelocity(body,new Vector3d()).z;
        helper.assertTrue(speed*(fixture==Fixture.REVERSE?-1:1)>3,"Fin fixture did not launch through physical forces: "+speed);
        probe.force=0;
        probe.engage=true;
      }
    });
    helper.runAtTickTime(110,()->{
      var physics=SubLevelContainer.getContainer(level).physicsSystem(); physics.updatePose(body);
      double speed=physics.getPipeline().getLinearVelocity(body,new Vector3d()).length();
      System.out.println("LS_FIN_CONSTRAINT fixture="+fixture+" phase=stop mass="+body.getMassTracker().getMass()+" speed="+speed+" min_directed="+probe.minimumDirectedSpeed);
      helper.assertTrue(probe.failure.isEmpty(),"Native clamp observer failed: "+probe.failure);
      helper.assertTrue(speed<.03,"Bounded clamp did not stop loaded native body: "+speed);
      if (!slope) helper.assertTrue(probe.minimumDirectedSpeed>-.02,"Fin stop reversed the cart");
      stopped.set(body.logicalPose().position());
    });
    helper.runAtTickTime(310,()->{
      var physics=SubLevelContainer.getContainer(level).physicsSystem(); physics.updatePose(body);
      double creep=body.logicalPose().position().distance(stopped);
      System.out.println("LS_FIN_CONSTRAINT fixture="+fixture+" phase=hold creep="+creep+" missing="+probe.missing);
      helper.assertTrue(probe.failure.isEmpty()&&probe.missing==0,"Fin hold lost native contact: "+probe.failure);
      helper.assertTrue(creep<.01,"Loaded fin hold crept more than its declared tolerance: "+creep);
      if (fixture==Fixture.OVERLOAD) probe.force=256;
      if (slope) { probe.engage=false; probe.clamp.close(); probe.clamp=null; }
    });
    helper.runAtTickTime(330,()->{
      try {
        var physics=SubLevelContainer.getContainer(level).physicsSystem();
        double speed=physics.getPipeline().getLinearVelocity(body,new Vector3d()).z;
        System.out.println("LS_FIN_CONSTRAINT fixture="+fixture+" phase=release_or_overload speed="+speed);
        helper.assertTrue(probe.failure.isEmpty(),"Native clamp failed: "+probe.failure);
        if (slope) helper.assertTrue(speed<-.5,"Released slope clamp kept holding the native body");
        if (fixture==Fixture.OVERLOAD) helper.assertTrue(speed>3&&speed<8,"Clamp force was unbounded or absent under overload: "+speed);
        helper.assertTrue(!CoasterCartTrackSnap.isBearingDisengaged(body),"Fin fixture lost native support");
      } finally {
        PROBES.remove(probe); if(probe.clamp!=null)probe.clamp.close();
        SubLevelContainer.getContainer(level).removeSubLevel(body,SubLevelRemovalReason.REMOVED);
        ((CoasterAnchorpointBlockEntity)level.getBlockEntity(from)).removeAnchorPeerCurve(level,to);
        level.removeBlock(from,false);level.removeBlock(to,false);
      }
      helper.succeed();
    });
  }
}
