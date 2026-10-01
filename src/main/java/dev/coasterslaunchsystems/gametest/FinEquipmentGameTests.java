package dev.coasterslaunchsystems.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.math.Mechanism;
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
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** Actual spring-applied cabinet brakes; fixture forces provide rolling entry and test released pads. */
@GameTestHolder("coasterslaunchsystems")
public final class FinEquipmentGameTests {
  private enum Fixture { FORWARD, REVERSE, SLOPE, MISSING_FIN, CONFLICT, OVERMASS }
  private static final List<Scene> SCENES = new ArrayList<>();
  private static boolean registered;
  private static final class Scene {
    final GameTestHelper helper;
    final ServerLevel level;
    final ServerSubLevel body;
    final Fixture fixture;
    final BlockPos from, to, module, motor, tire, tireMotor;
    DirectedTrack track;
    double force, distance, smallest = Double.POSITIVE_INFINITY;
    String failure = "";
    boolean stopping, closed;
    Scene(GameTestHelper helper, ServerSubLevel body, Fixture fixture, BlockPos from, BlockPos to, BlockPos module) {
      this.helper=helper;level=helper.getLevel();this.body=body;this.fixture=fixture;this.from=from;this.to=to;this.module=module;
      motor=module.east();tire=module.west(2);tireMotor=tire.west();
    }
    EquipmentCabinetEntity cabinet() { return (EquipmentCabinetEntity)level.getBlockEntity(module); }
    void rpm(int value) { ((CreativeMotorBlockEntity)level.getBlockEntity(motor)).generatedSpeed.setValue(value); }
    double speed() {
      var physics=SubLevelContainer.getContainer(level).physicsSystem(); physics.updatePose(body);
      return physics.getPipeline().getLinearVelocity(body,new Vector3d()).z;
    }
    void at(int tick,Runnable action) {
      helper.runAtTickTime(tick,()->{
        try { action.run(); }
        catch(RuntimeException failure) { cleanup();throw failure; }
      });
    }
    void cleanup() {
      if(closed)return;closed=true;SCENES.remove(this);
      SubLevelContainer.getContainer(level).removeSubLevel(body,SubLevelRemovalReason.REMOVED);
      if(level.getBlockEntity(from)instanceof CoasterAnchorpointBlockEntity anchor)anchor.removeAnchorPeerCurve(level,to);
      for(var pos:List.of(module,motor,tire,tireMotor,from,to))level.removeBlock(pos,false);
    }
  }
  private static void physics(ForgeSablePrePhysicsTickEvent event) {
    for(var s:List.copyOf(SCENES)) {
      if(s.level!=event.getPhysicsSystem().getLevel()||s.body.isRemoved())continue;
      try {
        var contact=EngagedContacts.current(s.level,s.body).orElse(null);
        if(contact==null||!s.track.matches(contact.hit()))continue;
        s.distance = s.track.distance(contact.hit());
        var axis=s.track.forward(s.level,contact.hit());var direction=new Vector3d(axis.x,axis.y,axis.z);
        double speed=event.getPhysicsSystem().getPipeline().getLinearVelocity(s.body,new Vector3d()).dot(direction);
        if(s.stopping)s.smallest=Math.min(s.smallest,speed*(s.fixture==Fixture.REVERSE?-1:1));
        if(s.force!=0)NativeForces.pointForce(s.body,s.body.getMassTracker().getCenterOfMass(),direction.mul(s.force),event.getTimeStep());
      }catch(RuntimeException failure){s.failure=failure.toString();}
    }
  }
  @GameTestGenerator
  public static List<TestFunction> equipmentTests() {
    var result=new ArrayList<TestFunction>();
    for(var fixture:Fixture.values())result.add(new TestFunction("launchsystems_fin","coasterslaunchsystems.fin_"+fixture.name().toLowerCase(java.util.Locale.ROOT),
        "coasterslaunchsystems:empty",440,0,true,helper->run(helper,fixture)));
    return result;
  }
  private static void run(GameTestHelper helper,Fixture fixture) {
    if(!registered){NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,FinEquipmentGameTests::physics);registered=true;}
    var level=helper.getLevel();boolean slope=fixture==Fixture.SLOPE;
    var from=new BlockPos(64+fixture.ordinal()*32,160,-896);var to=from.offset(0,slope?16:0,64);
    var module=from.offset(1,slope?0:-2,fixture==Fixture.REVERSE||slope?7:9);
    for(int x=from.getX()/16-1;x<=from.getX()/16+1;x++)for(int z=-57;z<=-51;z++)level.setChunkForced(x,z,true);
    for(var pos:List.of(from,to))level.setBlockAndUpdate(pos,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var tangent=new Vec3(0,slope?.25:0,1).normalize();var up=new Vec3(0,1,slope?-.25:0).normalize();
    var curve=new BezierConnection(Couple.create(from,to),Couple.create(CoasterAnchorpointBlockEntity.worldCenter(level,from),CoasterAnchorpointBlockEntity.worldCenter(level,to)),
        Couple.create(tangent,tangent.scale(-1)),Couple.create(up,up),true,false,CoasterTrackMaterials.COASTER);
    ((CoasterAnchorpointBlockEntity)level.getBlockEntity(from)).putAnchorPeerCurve(level,to,curve);
    var edge=CoasterPathEdge.curve(from,to,curve);var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,from.getCenter().add(0,slope?2:0,8));
    var body=CoasterCartSpawner.spawnMinimalContraption(level,hit.point(),CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(level,hit)),hit);
    if(fixture!=Fixture.MISSING_FIN)helper.assertTrue(body.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(1,-1,0),EquipmentContent.BRAKE_FIN.get().defaultBlockState(),3),"Brake fin did not install");
    int count=fixture==Fixture.OVERMASS?20:slope?15:6;
    for(int i=0;i<count;i++)helper.assertTrue(body.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(i%3-1,1+i/9,i/3%3-1),Blocks.IRON_BLOCK.defaultBlockState(),3),"Brake ballast did not install");
    if (slope) {
      helper.assertTrue(body.getPlot().getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.below(), EquipmentContent.TIRE_SURFACE.get().defaultBlockState(), 3), "Rated load strip did not install");
      helper.assertTrue(body.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(-1,-1,0), EquipmentContent.BRAKE_FIN.get().defaultBlockState(), 3), "Rated load balance fin did not install");
    }
    var scene=new Scene(helper,body,fixture,from,to,module);
    level.setBlockAndUpdate(module,EquipmentContent.FIN.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
    level.setBlockAndUpdate(scene.motor,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING,Direction.WEST));scene.rpm(slope?0:64);
    if(fixture==Fixture.CONFLICT) {
      body.getPlot().getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.below(),EquipmentContent.TIRE_SURFACE.get().defaultBlockState(),3);
      level.setBlockAndUpdate(scene.tire,EquipmentContent.TIRE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
      level.setBlockAndUpdate(scene.tireMotor,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING,Direction.EAST));
      ((CreativeMotorBlockEntity)level.getBlockEntity(scene.tireMotor)).generatedSpeed.setValue(0);
    }
    var stopped=new Vector3d();var entry=new Vector3d();
    scene.at(10,()->{
      scene.track=DirectedTrack.resolve(level,from,to).orElseThrow();SCENES.add(scene);
      scene.cabinet().bind(level,from,to);scene.cabinet().setArmed(true);
      if(fixture==Fixture.CONFLICT){var other=(EquipmentCabinetEntity)level.getBlockEntity(scene.tire);other.bind(level,from,to);other.setArmed(true);}
    });
    scene.at(40,()->{
      var motion=MotionProviders.resolve(level,body.getUniqueId()).orElseThrow();
      helper.assertTrue(motion.brakingAllowed(),"Ordinary provider did not explicitly permit braking");
      if (slope) helper.assertTrue(Math.abs(NativeForces.mass(level, motion) - 64) < 1e-6, "Slope fixture must exercise the maximum declared native mass");
      helper.assertTrue(motion.interfaces().contains(Mechanism.FIN_BRAKE)!=(fixture==Fixture.MISSING_FIN),"Fin certificate ignored physical hardware");
      if(!slope)scene.force=NativeForces.mass(level,motion)*2*(fixture==Fixture.REVERSE?-1:1);
    });
    scene.at(60,()->{
      if(!slope)helper.assertTrue(scene.speed()*(fixture==Fixture.REVERSE?-1:1)>1.5,"Released fin did not admit physical rolling entry");
      scene.force=0;scene.speed();entry.set(body.logicalPose().position());scene.stopping=true;scene.rpm(0);
      if(fixture==Fixture.CONFLICT)((CreativeMotorBlockEntity)level.getBlockEntity(scene.tireMotor)).generatedSpeed.setValue(64);
    });
    scene.at(100,()->{
      double speed=scene.speed();var cabinet=scene.cabinet();
      System.out.println("LS_FIN_EQUIPMENT fixture="+fixture+" phase=stop mass="+body.getMassTracker().getMass()+" speed="+speed+" moved="+body.logicalPose().position().distance(entry)+" fault="+cabinet.fault());
      helper.assertTrue(scene.failure.isEmpty(),"Fin equipment observer failed: "+scene.failure);
      if(fixture==Fixture.MISSING_FIN||fixture==Fixture.OVERMASS||fixture==Fixture.CONFLICT) {
        String reason=fixture==Fixture.MISSING_FIN?"certified friction fin":fixture==Fixture.OVERMASS?"mass/slope":"Overlapping";
        helper.assertTrue(cabinet.fault().contains(reason),"Rejected fin equipment did not report its cause: "+cabinet.fault());
        helper.assertTrue(Math.abs(speed)>1.5,"A rejected brake applied a partial holding force");
        if(fixture==Fixture.CONFLICT)helper.assertTrue(((EquipmentCabinetEntity)level.getBlockEntity(scene.tire)).fault().contains("Overlapping"),"Overlap did not fault both owners");
        scene.cleanup();helper.succeed();return;
      }
      helper.assertTrue(cabinet.fault().isEmpty()&&Math.abs(speed)<.03,"Placed fin cabinet failed to stop: "+cabinet.fault());
      if(!slope)helper.assertTrue(scene.smallest>-.02,"Fin stop reversed the native cart");
      helper.assertTrue(!CoasterCartTrackSnap.isBearingDisengaged(body),"Fin stop lost native engagement");stopped.set(body.logicalPose().position());
    });
    scene.at(300,()->{
      if(scene.closed)return;scene.speed();double creep=body.logicalPose().position().distance(stopped);
      System.out.println("LS_FIN_EQUIPMENT fixture="+fixture+" phase=hold creep="+creep+" distance="+scene.distance+" interval="+scene.cabinet().interval());
      helper.assertTrue(creep<.01&&Math.abs(scene.speed())<.03,"Placed spring fin did not maintain its rated hold");
      if(slope)scene.rpm(64);
    });
    scene.at(305,()->{
      if(scene.closed)return;
      if(slope){System.out.println("LS_FIN_EQUIPMENT fixture=SLOPE phase=powered_release speed="+scene.speed()+" rpm="+scene.cabinet().getSpeed()+" distance="+scene.distance);
        helper.assertTrue(scene.speed()<-.3 && scene.cabinet().interval().intersects(scene.distance, scene.distance, .6),
            "Shaft power did not release the fin clamp while its fin remained over the pad");scene.rpm(0);}
    });
    scene.at(330,()->{
      if(scene.closed)return;
      System.out.println("LS_FIN_EQUIPMENT fixture="+fixture+" phase=reapply speed="+scene.speed()+" rpm="+scene.cabinet().getSpeed()+" fault="+scene.cabinet().fault()+" drift="+body.logicalPose().position().distance(stopped)+" distance="+scene.distance);
      helper.assertTrue(scene.cabinet().fault().isEmpty()&&Math.abs(scene.speed())<.03,"Power loss did not reapply the spring fin clamp");
      body.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(1,-1,0),Blocks.AIR.defaultBlockState(),3);
      helper.assertTrue(!MotionProviders.resolve(level,body.getUniqueId()).orElseThrow().interfaces().contains(Mechanism.FIN_BRAKE),"Removed fin kept its certificate");
      scene.force=NativeForces.mass(level,MotionProviders.resolve(level,body.getUniqueId()).orElseThrow())*4;
    });
    scene.at(350,()->{
      if(scene.closed)return;
      try {
        helper.assertTrue(scene.speed()>.7,"Removed fin retained a stale native holding constraint");
        helper.assertTrue(scene.cabinet().fault().contains("certified friction fin"),"Removed fin did not invalidate cabinet ownership");
        System.out.println("LS_FIN_EQUIPMENT fixture="+fixture+" phase=hardware_removed speed="+scene.speed());
      }finally{scene.cleanup();}
      helper.succeed();
    });
  }
}
