package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.*;
import dev.coasterslaunchsystems.integration.FourthDimensionRouteStatus;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.runtime.*;
import dev.coasterslaunchsystems.track.*;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** Actual local equipment on FD's bounded eight-edge, six-block Arrow route. */
@GameTestHolder("coasterslaunchsystems_route_interop")
@net.neoforged.fml.common.EventBusSubscriber(modid="coasterslaunchsystems")
public final class RouteInteropGameTests {
  private static final boolean PLAYER="true".equals(System.getenv("LS_ROUTE_PLAYER"));
  private static final List<Scene> LIVE = new ArrayList<>();
  private static boolean listening;
  static final String DATA = "dev.fourthdimension.runtime.FoundationData";
  static final String LOCAL = "dev.fourthdimension.integration.RideLocalOperations";
  static final String OPS = "dev.fourthdimension.vehicle.VehicleOperations";
  static Class<?> vehicleType() { return type(DATA + "$Vehicle"); }

  static final class Scene {
    final GameTestHelper h;
    final String mode;
    Map<Integer,UUID> expectedReservations;
    UUID expectedAuthority;
    final ServerLevel level;
    final BlockPos origin = new BlockPos(320, 160, -640);
    final List<BlockPos> anchors = new ArrayList<>(), placed = new ArrayList<>();
    final List<Object> cars = new ArrayList<>();
    final List<Pig> riders = new ArrayList<>();
    final List<DirectedTrack> tracks = new ArrayList<>();
    final List<net.minecraft.world.level.ChunkPos> chunks = new ArrayList<>();
    RideControllerEntity source, receiver;
    Object data, route;
    UUID lead, blocker;
    double work, blockedWork, peakSpeed;
    Vector3d blockerPosition;
    int driveSteps, stage, conflictTicks, lastContactTrack=-1;
    boolean closed, captureRearmed;
    String failure = "";
    Scene(GameTestHelper h, String mode) {
      this.h = h; this.mode = mode; level = h.getLevel();
      for (int x = 19; x <= 23; x++) for (int z = -41; z <= -36; z++) {
        var chunk = new net.minecraft.world.level.ChunkPos(x, z); chunks.add(chunk); level.setChunkForced(x, z, true);
      }
      for (int[] p : new int[][] {{0,8},{0,44},{8,52},{40,52},{48,44},{48,8},{40,0},{8,0}}) anchors.add(origin.offset(p[0],0,p[1]));
      data = invoke(DATA, "get", new Class<?>[] {ServerLevel.class}, level); buildRoute(); route = read(data,"rideLocal");
      h.runAtTickTime(15, () -> {
        for (int i=0;i<8;i++) tracks.add(DirectedTrack.resolve(level, anchors.get(i),anchors.get((i+1)%8)).orElseThrow());
        cars.add(car(0,8)); cars.add(car(3,tracks.get(3).length()/2));
        lead = (UUID) read(cars.getFirst(),"chassis"); blocker=(UUID) read(cars.getLast(),"chassis");
        for (var car : cars) invoke(LOCAL,"admit",new Class<?>[]{ServerLevel.class,vehicleType()},level,car);
        h.assertTrue(reservations().get(0).equals(lead) && reservations().get(1).equals(lead)
            && reservations().get(3).equals(blocker) && reservations().get(4).equals(blocker),"Real trains did not establish disjoint native route pairs");
      });
      h.runAtTickTime(150, () -> {
        for (var car : cars) if(!PLAYER) {board(car);invoke(OPS,"restraints",new Class<?>[]{ServerLevel.class,vehicleType(),boolean.class},level,car,true);}
      });
      h.runAtTickTime(180, this::install);
    }
    /** Same native eight-edge construction as ExampleLayouts, with 32m receiving straights. */
    @SuppressWarnings("unchecked") void buildRoute() {
      var direction=new net.minecraft.world.phys.Vec3[]{new net.minecraft.world.phys.Vec3(0,0,1),new net.minecraft.world.phys.Vec3(0,0,1),
          new net.minecraft.world.phys.Vec3(1,0,0),new net.minecraft.world.phys.Vec3(1,0,0),new net.minecraft.world.phys.Vec3(0,0,-1),
          new net.minecraft.world.phys.Vec3(0,0,-1),new net.minecraft.world.phys.Vec3(-1,0,0),new net.minecraft.world.phys.Vec3(-1,0,0)};
      for(var p:anchors)level.setBlockAndUpdate(p,dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      var normals=new ArrayList<net.minecraft.world.phys.Vec3>();var centers=new ArrayList<net.minecraft.world.phys.Vec3>();
      for(int i=0;i<8;i++) {
        var normal=dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement.anchorFaceNormalForAxis(level,anchors.get(i),direction[i]);normals.add(normal);
        var anchor=(dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity)level.getBlockEntity(anchors.get(i));
        centers.add(dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity.worldCenterBase(level,anchors.get(i)).add(normal.scale(anchor.getLiftBlocks())));
      }
      var design=Arrays.stream(type("dev.fourthdimension.track.TrackDesign").getEnumConstants()).filter(v->((Enum<?>)v).name().equals("ARROW")).findFirst().orElseThrow();
      var ids=new ArrayList<UUID>();
      try {
        for(int i=0;i<8;i++) {
          int next=(i+1)%8;var from=anchors.get(i);var to=anchors.get(next);
          var curve=new com.simibubi.create.content.trains.track.BezierConnection(net.createmod.catnip.data.Couple.create(from,to),
              net.createmod.catnip.data.Couple.create(centers.get(i),centers.get(next)),net.createmod.catnip.data.Couple.create(direction[i],direction[next].scale(-1)),
              net.createmod.catnip.data.Couple.create(normals.get(i),normals.get(next)),true,false,dev.silvergold.simulatedcoasters.CoasterTrackMaterials.COASTER);
          ((dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity)level.getBlockEntity(from)).putAnchorPeerCurve(level,to,curve);
          var edge=dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge.curve(from,to,curve);
          var track=DirectedTrack.resolve(level,from,to).orElseThrow();
          var key=type("dev.fourthdimension.physics.RotationProfile$Key").getConstructor(double.class,double.class);
          var profile=type("dev.fourthdimension.physics.RotationProfile").getConstructor(List.class).newInstance(List.of(key.newInstance(0.0,0.0),key.newInstance(track.length(),0.0)));
          var id=UUID.randomUUID();ids.add(id);
          var hash=(long)invoke("dev.fourthdimension.track.TrackGeometry","fingerprint",new Class<?>[]{net.minecraft.world.level.Level.class,dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge.class},level,edge);
          var rail=type(DATA+"$Rail").getConstructor(BlockPos.class,BlockPos.class,type("dev.fourthdimension.contract.Family"),profile.getClass(),UUID.class,String.class,long.class,long.class,boolean.class)
              .newInstance(from,to,read(design,"family"),profile,id,read(design,"variant"),hash,0L,true);
          invoke("dev.fourthdimension.track.TrackPlacement","captureOriginalCurve",new Class<?>[]{type(DATA+"$Rail"),curve.getClass()},rail,curve);
          ((List<Object>)read(data,"rails")).add(rail);
        }
      } catch(ReflectiveOperationException failure) {throw new IllegalStateException(failure);}
      invoke("dev.fourthdimension.track.TrackGeometry","invalidate",new Class<?>[]{net.minecraft.world.level.Level.class},level);
      for(var rail:(List<?>)read(data,"rails")) if(ids.contains(read(rail,"id"))) {
        var edge=invoke("dev.fourthdimension.track.TrackGeometry","resolve",new Class<?>[]{net.minecraft.world.level.Level.class,type(DATA+"$Rail")},level,rail);
        write(rail,"geometryHash",invoke("dev.fourthdimension.track.TrackGeometry","fingerprint",new Class<?>[]{net.minecraft.world.level.Level.class,dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge.class},level,edge));
      }
      invoke(LOCAL,"configure",new Class<?>[]{ServerLevel.class,List.class},level,ids);
      System.out.println("LS_ROUTE_LAYOUT anchors="+anchors+" nativeRails="+ids+" capacity=3 blocks=6 receiver_length=32");
    }
    @SuppressWarnings("unchecked") Map<Integer,UUID> reservations() { return (Map<Integer,UUID>) read(route,"reservations"); }
    @SuppressWarnings("unchecked") Object train(UUID id) { return ((Map<UUID,Object>) read(route,"trains")).get(id); }
    ServerSubLevel body(UUID id) { return (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel(id); }
    Object car(int edge,double distance) {
      var design = Arrays.stream(type("dev.fourthdimension.track.TrackDesign").getEnumConstants()).filter(v -> ((Enum<?>)v).name().equals("ARROW")).findFirst().orElseThrow();
      var car = invoke("dev.fourthdimension.vehicle.VehicleFactory","create",new Class<?>[]{ServerLevel.class,design.getClass(),dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit.class},level,design,tracks.get(edge).hitAtDistance(level,distance));
      var plot = body((UUID)read(car,"chassis")).getPlot().getEmbeddedLevelAccessor();
      h.assertTrue(plot.getBlockState(BlockPos.ZERO.below()).isAir() && plot.getBlockState(new BlockPos(1,-1,0)).isAir(),"Native hardware occupies certified interfaces");
      plot.setBlock(BlockPos.ZERO.below(),EquipmentContent.TIRE_SURFACE.get().defaultBlockState(),3);
      plot.setBlock(new BlockPos(1,-1,0),EquipmentContent.BRAKE_FIN.get().defaultBlockState(),3);
      return car;
    }
    void board(Object car) {
      h.assertTrue((boolean)invoke("dev.fourthdimension.vehicle.Boarding","ready",new Class<?>[]{ServerLevel.class,vehicleType()},level,car),"Native route car did not settle for boarding");
      for (var carrier:(List<?>)read(car,"carriers")) for (var seat:(List<?>)read(carrier,"seats")) {
        var pig=EntityType.PIG.create(level); pig.setNoAi(true); pig.setPos(origin.getX(),origin.getY()+5,origin.getZ()); level.addFreshEntity(pig);
        SeatBlock.sitDown(level,body((UUID)read(carrier,"body")).getPlot().getCenterBlock().offset((BlockPos)seat),pig);
        h.assertTrue(pig.isPassenger(),"Native route seat did not retain rider"); riders.add(pig);
      }
    }
    RideControllerEntity controller(int edge,BlockPos pos) {
      placed.add(pos); level.setBlockAndUpdate(pos,EquipmentContent.CONTROLLER.get().defaultBlockState());
      var c=(RideControllerEntity)level.getBlockEntity(pos); c.bind(level,anchors.get(edge),anchors.get((edge+1)%8)); return c;
    }
    void install() {
      blockerPosition=new Vector3d(body(blocker).logicalPose().position());
      source=controller(0,anchors.get(0).offset(4,-2,8)); receiver=controller(2,anchors.get(2).offset(3,-2,4));
      for(int d=8;d<=12;d+=2) module(source,0,d,false,false);
      for(int d=7;d<=15;d+=2) module(source,0,d,true,false);
      for(int d=17;d<=35;d+=2) module(source,0,d,true,false);
      for(int d=11;d<=35;d+=2) module(source,0,d,false,true);
      module(receiver,2,PLAYER?1:2,false,false); module(receiver,2,4,false,false);
      for(int d=1;d<=31;d+=2) module(receiver,2,d,true,false);
      for(var c:List.of(source,receiver)) for(var link:c.modules()) {
        var module=(EquipmentCabinetEntity)level.getBlockEntity(link.position());
        System.out.println("LS_ROUTE_MODULE controller="+c.getBlockPos().toShortString()+" mechanism="+module.mechanism()+" exit="+link.exitDrive()+" span="+module.interval());
      }
      source.connectDestination(receiver); source.setStationCapture(true);
      source.setArmed(true); receiver.setArmed(true); LIVE.add(this);
      h.assertTrue(source.transferRoute(level,receiver).tracks().size()==3,"Physical route does not include the intermediate native curve");
    }
    void module(RideControllerEntity c,int edge,int d,boolean fin,boolean exit) {
      var from=anchors.get(edge); var along=edge==0?Direction.SOUTH:Direction.EAST;
      var side=edge==0?Direction.EAST:Direction.NORTH;
      var pos=from.relative(along,d).below(2).relative(side,fin?1:-1); var motor=pos.relative(side,fin?1:-1);
      placed.add(pos); placed.add(motor);
      level.setBlockAndUpdate(pos,(fin?EquipmentContent.FIN:EquipmentContent.TIRE).get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,side.getAxis()));
      level.setBlockAndUpdate(motor,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING,fin?side.getOpposite():side));
      ((CreativeMotorBlockEntity)level.getBlockEntity(motor)).generatedSpeed.setValue(edge==2 && !fin ? -32 : 32);
      var cabinet=(EquipmentCabinetEntity)level.getBlockEntity(pos); cabinet.bind(level,anchors.get(edge),anchors.get((edge+1)%8)); c.link(cabinet,exit); cabinet.setArmed(true);
    }
    void high(boolean high) { level.setBlockAndUpdate(source.getBlockPos().above(),(high?Blocks.REDSTONE_BLOCK:Blocks.AIR).defaultBlockState()); }
    void tick() {
      if(closed || source==null)return;
      h.assertTrue(failure.isEmpty(),failure);
      if(PLAYER)return;
      if(level.getGameTime()%40==0) System.out.println("LS_ROUTE stage="+stage+" source="+source.status()+" receiver="+receiver.status()+" native="+FourthDimensionRouteStatus.query(level,lead)+" work="+work);
      if(stage==1 && !captureRearmed && (boolean)read(train(lead),"braking")
          && (boolean)read(read(cars.getFirst(),"authority"),"suspended")) {
        var velocity=SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(body(lead),new Vector3d());
        if(velocity.length()<.03) {
          h.assertTrue(work>0 && lastContactTrack==0
              && ((List<?>)read(cars.getFirst(),"carriers")).stream().allMatch(c->(boolean)read(c,"capture")),"Initial route hold was not completed native station capture");
          captureRearmed=true;source.setArmed(false);source.setArmed(true);
          System.out.println("LS_ROUTE explicit_initial_capture_rearm=true held_high=true work="+work);
        }
        return;
      }
      if(stage==0 && source.phase()==RideControllerEntity.State.READY) {
        h.assertTrue(((Number)privateRead(read(cars.getFirst(),"authority"),"stoppedTicks")).intValue()>=5,"Local READY preceded native release readiness");
        stage=1; high(true);
      }
      else if(stage==1 && source.phase()==RideControllerEntity.State.READY_EXIT) {
        h.assertTrue(work>0 && driveSteps>10,"Admitted loaded local equipment did no physical work");
        if (!mode.equals("physical_conflict")) { injectGuard(); return; }
        stage=2; high(false); h.runAfterDelay(4,()->high(true));
      } else if(stage==2 && (boolean)read(train(lead),"braking")) {
        h.assertTrue(reservations().get(1).equals(lead) && reservations().get(2).equals(lead)
            && reservations().get(3).equals(blocker) && reservations().get(4).equals(blocker),"Physical conflict changed retained current/next owners");
        h.assertTrue(lastContactTrack==2,"Conflict was not caused by actual entry onto receiver rail");
        stage=3; blockedWork=work;
      } else if(stage==3 && ++conflictTicks>=40) {
        h.assertTrue(Math.abs(work-blockedWork)<1e-7,"Conflicting route continued positive equipment work");
        h.assertTrue(source.reservation().contains(lead) && receiver.reservation().contains(lead),"Conflict released local stopping ownership");
        h.assertTrue(riders.stream().allMatch(Pig::isPassenger),"Conflict lost a native passenger");
        var status=FourthDimensionRouteStatus.query(level,lead);
        h.assertTrue(status.managed()&&!status.permitted()&&status.freshRequestRequired(),"Native status concealed required fresh recovery");
        var local=OperatorStatus.sample(level,source,lead);
        h.assertTrue(local.getString("route_denial").equals(status.reason()) && local.getInt("route_block")==status.currentBlock(),"Local operator disagrees with native route decision");
        System.out.println("LS_ROUTE_PASS loaded_riders="+riders.size()+" work="+work+" drive_steps="+driveSteps+" peak_speed="+peakSpeed+" retained="+reservations()+" status="+status);
        cleanup(); h.succeed();
      }
      if(stage==4 && ++conflictTicks>=40) {
        h.assertTrue(Math.abs(work-blockedWork)<1e-7,"Guard continued positive equipment work: "+mode);
        h.assertTrue(reservations().equals(expectedReservations) && source.reservation().equals(Set.of(lead)),"Guard changed retained route/local reservations: "+mode);
        h.assertTrue(expectedAuthority.equals(read(read(cars.getFirst(),"authority"),"owner")),"Guard transferred native owner");
        h.assertTrue(!MotionProviders.requestDispatch(level,lead),"Guard admitted fresh propulsion");
        var velocity=SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(body(lead),new Vector3d());
        h.assertTrue(velocity.length()<.03,"Guard did not retain native stop: "+velocity);
        if(!mode.equals("missing_carrier"))h.assertTrue(riders.stream().allMatch(Pig::isPassenger),"Guard ejected native rider");
        System.out.println("LS_ROUTE_GUARD_PASS mode="+mode+" work="+work+" source="+source.status()+" status="+FourthDimensionRouteStatus.query(level,lead)+" retained="+reservations());
        cleanup();h.succeed();
      }
      if(stage<2) h.assertTrue(source.phase()!=RideControllerEntity.State.FAULT,"Unexpected local preparation fault: "+source.status());
    }
    void injectGuard() {
      stage=4;blockedWork=work;expectedAuthority=(UUID)read(read(cars.getFirst(),"authority"),"owner");
      switch(mode) {
        case "changed_owner" -> reservations().put(1,blocker);
        case "stale_controller" -> privateField(source,"identity",UUID.randomUUID());
        case "opposed_direction" -> {
          privateField(source,"from",anchors.get(1));privateField(source,"to",anchors.get(0));
          privateField(source,"cachedTrack",null);privateField(source,"graphRevision",Integer.MIN_VALUE);
        }
        case "rollback" -> {
          // Deliberate negative fault injection only; the successful movement uses real LS work.
          var pipeline=SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
          pipeline.addLinearAndAngularVelocity(body(lead),new Vector3d(0,0,-.2),new Vector3d());
        }
        case "missing_carrier" -> {
          var carrier=((List<?>)read(cars.getFirst(),"carriers")).getFirst();
          var container=SubLevelContainer.getContainer(level);
          container.removeSubLevel(body((UUID)read(carrier,"body")),SubLevelRemovalReason.UNLOADED);
        }
        default -> throw new IllegalArgumentException(mode);
      }
      expectedReservations=Map.copyOf(reservations());
      h.assertTrue(!MotionProviders.requestDispatch(level,lead),"Affected provider granted dispatch after "+mode);
    }

    static Object privateRead(Object object,String name) {
      try {var field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);}
      catch(ReflectiveOperationException failure){throw new IllegalStateException(failure);}
    }
    static void privateField(Object object,String name,Object value) {
      try {var field=object.getClass().getDeclaredField(name);field.setAccessible(true);field.set(object,value);}
      catch(ReflectiveOperationException failure){throw new IllegalStateException(failure);}
    }
    void cleanup() {
      if(closed)return; closed=true; LIVE.remove(this); if(source!=null)level.removeBlock(source.getBlockPos().above(),false);
      for(var pig:riders)pig.discard();
      var container=SubLevelContainer.getContainer(level);
      for(var car:cars) {
        for(var carrier:(List<?>)read(car,"carriers")) {var b=container.getSubLevel((UUID)read(carrier,"body"));if(b!=null)container.removeSubLevel(b,SubLevelRemovalReason.REMOVED);}
        var b=container.getSubLevel((UUID)read(car,"chassis"));if(b!=null)container.removeSubLevel(b,SubLevelRemovalReason.REMOVED);
      }
      placed.forEach(p->level.removeBlock(p,false)); chunks.forEach(p->level.setChunkForced(p.x,p.z,false));
    }
  }
  private static void observe(ForgeSablePrePhysicsTickEvent e) {
    for(var s:List.copyOf(LIVE)) if(s.level==e.getPhysicsSystem().getLevel()) try {
      var body=s.body(s.lead); var optional=EngagedContacts.current(s.level,body);
      if(optional.isEmpty()) {
        var absentGroups=body.getQueuedForceGroups();var absentDrive=absentGroups==null?null:absentGroups.get(ForceGroups.PROPULSION.get());
        s.h.assertTrue(absentDrive==null || absentDrive.getForceTotal().getLocalForce().lengthSquared()<1e-14,"Propulsion had no actual accepted contact");
        continue;
      }
      var contact=optional.get();
      var track=s.tracks.stream().filter(t->t.matches(contact.hit())).findFirst().orElseThrow();
      s.lastContactTrack=s.tracks.indexOf(track);
      var axis=track.forward(s.level,contact.hit()); var dir=new Vector3d(axis.x,axis.y,axis.z);
      var dynamics=NativeForces.pointDynamics(body,e.getPhysicsSystem().getPipeline(),VehicleInterfaces.tirePoint(body),dir);
      s.peakSpeed=Math.max(s.peakSpeed,Math.abs(dynamics.speed()));
      var groups=body.getQueuedForceGroups(); var group=groups==null?null:groups.get(ForceGroups.PROPULSION.get());
      double force=group==null?0:new Vector3d(group.getForceTotal().getLocalForce()).rotate(body.logicalPose().orientation()).dot(dir)/e.getTimeStep();
      s.h.assertTrue(new Vector3d(s.body(s.blocker).logicalPose().position()).distance(s.blockerPosition)<.05,"Unrequested second route train moved");
      if(force>1e-8) {
        var authority=read(s.cars.getFirst(),"authority");
        s.h.assertTrue(s.source.reservation().contains(s.lead) && s.source.identity().equals(read(authority,"owner"))
            && !(boolean)read(authority,"held") && !(boolean)read(authority,"suspended"),"Positive local force bypassed native/local owned grant");
        s.driveSteps++;s.work+=Math.max(0,DriveMath.incrementalWork(dynamics.effectiveMass(),dynamics.speed(),force,e.getTimeStep()));}
    } catch(RuntimeException failure) {s.failure=failure.toString();}
  }
  /** Samples the saved native/controller identities too, so it survives a server restart. */
  @net.neoforged.bus.api.SubscribeEvent public static void commands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
    if(!PLAYER || !Boolean.getBoolean("coasterslaunchsystems.validation"))return;
    event.getDispatcher().register(net.minecraft.commands.Commands.literal("lsroute").requires(s->s.hasPermission(2))
        .then(net.minecraft.commands.Commands.literal("status").executes(ctx->{
          var level=ctx.getSource().getLevel();var data=invoke(DATA,"get",new Class<?>[]{ServerLevel.class},level);
          var route=read(data,"rideLocal");var result=new LinkedHashMap<String,Object>();
          result.put("route",read(route,"routeId"));result.put("reservations",read(route,"reservations"));
          var rows=new ArrayList<Map<String,Object>>();
          for(var car:((Map<?,?>)read(data,"vehicles")).values()) {
            var row=new LinkedHashMap<String,Object>();var id=(UUID)read(car,"chassis");row.put("chassis",id);
            row.put("boundFrom",read(car,"boundFrom"));row.put("boundTo",read(car,"boundTo"));
            row.put("authority",read(car,"authority"));row.put("fault",read(car,"fault"));
            row.put("restraints",read(car,"restraints"));row.put("stopping",read(car,"stopping"));
            row.put("carriers",((List<?>)read(car,"carriers")).stream().map(c->Map.of("body",read(c,"body"),"occupants",read(c,"occupants"),"capture",read(c,"capture"))).toList());
            var body=SubLevelContainer.getContainer(level).getSubLevel(id);
            if(body instanceof ServerSubLevel nativeBody){row.put("position",body.logicalPose().position().toString());row.put("velocity",SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(nativeBody,new Vector3d()).toString());}
            rows.add(row);
          }
          result.put("cars",rows);
          for(var pos:List.of(new BlockPos(324,158,-624),new BlockPos(331,158,-584)))
            if(RideControllers.loadedBlockEntity(level,pos) instanceof RideControllerEntity c) result.put(pos.toShortString(),c.saveWithoutMetadata(level.registryAccess()).toString());
          result.put("players",level.players().stream().map(p->Map.of("name",p.getName().getString(),"seat",p.getRootVehicle().getUUID(),"riding",p.isPassenger())).toList());
          result.put("live",LIVE.stream().map(s->Map.of("work",s.work,"steps",s.driveSteps,"speed",s.peakSpeed)).toList());
          ctx.getSource().sendSuccess(()->net.minecraft.network.chat.Component.literal(new com.google.gson.Gson().toJson(result)),false);return rows.size();
        }))
        .then(net.minecraft.commands.Commands.literal("author-entry-drive").executes(ctx->authorEntryDrive(ctx.getSource().getLevel(),ctx.getSource()))));
  }
  /** Explicit validation-only setup correction; never a normal occupied-editing operation. */
  @SuppressWarnings("unchecked")
  private static int authorEntryDrive(ServerLevel level, net.minecraft.commands.CommandSourceStack output) {
    var source=(RideControllerEntity)RideControllers.loadedBlockEntity(level,new BlockPos(324,158,-624));
    var receiver=(RideControllerEntity)RideControllers.loadedBlockEntity(level,new BlockPos(331,158,-584));
    var data=invoke(DATA,"get",new Class<?>[]{ServerLevel.class},level);
    var cars=(Map<?,?>)read(data,"vehicles");
    if(!level.players().isEmpty() || cars.size()!=1 || source==null || receiver==null || source.armed() || receiver.armed()
        || source.phase()!=RideControllerEntity.State.INSPECTION || receiver.phase()!=RideControllerEntity.State.INSPECTION
        || !source.transferring() || source.reservation().size()!=1 || !source.reservation().equals(receiver.reservation())
        || source.destination()==null || !source.destination().identity().equals(receiver.identity())
        || receiver.incoming()==null || !receiver.incoming().identity().equals(source.identity()))
      throw new IllegalStateException("Entry fixture correction requires the offline-player stopped saved pair");
    var car=cars.values().iterator().next();var id=(UUID)read(car,"chassis");var authority=read(car,"authority");
    var container=SubLevelContainer.getContainer(level);var body=(ServerSubLevel)container.getSubLevel(id);
    if(!source.reservation().contains(id) || !(boolean)read(authority,"held") || !(boolean)read(authority,"suspended")
        || ((List<?>)read(car,"carriers")).stream().anyMatch(c->!(boolean)read(c,"capture"))
        || body==null || container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()>=.03)
      throw new IllegalStateException("Entry fixture correction requires unchanged captured native hold");
    var oldPos=new BlockPos(330,158,-587);var pos=oldPos.west();var motor=pos.south();
    if(!(RideControllers.loadedBlockEntity(level,oldPos) instanceof EquipmentCabinetEntity original) || !receiver.identity().equals(original.controllerIdentity()))
      throw new IllegalStateException("Expected first receiver tire is absent");
    var originalTag=original.saveWithoutMetadata(level.registryAccess());var originalId=original.identity();
    var originalLink=receiver.modules().stream().filter(m->m.position().equals(oldPos)&&m.identity().equals(originalId)).findFirst().orElseThrow();
    if(!source.receiverManifest().contains(originalLink))throw new IllegalStateException("Saved receiver manifest differs");
    if(!level.isEmptyBlock(pos) || !level.isEmptyBlock(motor))throw new IllegalStateException("Entry fixture cells are occupied");
    var gson=new com.google.gson.Gson();var nativeBefore=gson.toJson(cars);var routeBefore=gson.toJson(read(data,"rideLocal"));
    var bodiesBefore=fixtureBodyState(level,car);
    var sourceBefore=source.saveWithoutMetadata(level.registryAccess());var receiverBefore=receiver.saveWithoutMetadata(level.registryAccess());
    level.setBlockAndUpdate(pos,EquipmentContent.TIRE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z));
    level.setBlockAndUpdate(motor,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING,Direction.NORTH));
    ((CreativeMotorBlockEntity)level.getBlockEntity(motor)).generatedSpeed.setValue(-32);
    var cabinet=(EquipmentCabinetEntity)level.getBlockEntity(pos);
    cabinet.bind(level,new BlockPos(328,160,-588),new BlockPos(360,160,-588));
    originalTag.putDouble("ls_start",cabinet.interval().start());originalTag.putDouble("ls_end",cabinet.interval().end());
    cabinet.loadWithComponents(originalTag,level.registryAccess());
    var link=new RideControllerEntity.Module(pos,originalId,false);
    try {
      var modules=RideControllerEntity.class.getDeclaredField("modules");modules.setAccessible(true);
      var receiverLinks=(List<RideControllerEntity.Module>)modules.get(receiver);receiverLinks.set(receiverLinks.indexOf(originalLink),link);
      var manifest=RideControllerEntity.class.getDeclaredField("receiverManifest");manifest.setAccessible(true);
      var savedLinks=(List<RideControllerEntity.Module>)manifest.get(source);savedLinks.set(savedLinks.indexOf(originalLink),link);
    } catch(ReflectiveOperationException failure){throw new IllegalStateException(failure);}
    level.setBlockAndUpdate(oldPos,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(oldPos.south(),Blocks.AIR.defaultBlockState());
    cabinet.setChanged();source.setChanged();receiver.setChanged();
    var sourceAfter=source.saveWithoutMetadata(level.registryAccess());var receiverAfter=receiver.saveWithoutMetadata(level.registryAccess());
    sourceBefore.remove("receiver_manifest");sourceAfter.remove("receiver_manifest");receiverBefore.remove("modules");receiverAfter.remove("modules");
    if(!sourceBefore.equals(sourceAfter) || !receiverBefore.equals(receiverAfter) || !nativeBefore.equals(gson.toJson(cars))
        || !routeBefore.equals(gson.toJson(read(data,"rideLocal"))) || !bodiesBefore.equals(fixtureBodyState(level,car)))
      throw new IllegalStateException("Fixture correction changed state outside hardware manifests");
    output.sendSuccess(()->net.minecraft.network.chat.Component.literal("AUTHORED FIXTURE ONLY: existing tire/motor moved one block upstream (330,158,-587/-586) to (329,158,-587/-586), identity "+originalId+", -32 RPM, interval "+cabinet.interval()+"; only receiver modules/source receiver_manifest position changed; native cars, bodies, poses, velocity, route reservations, authority and controller/request state unchanged"),false);
    return 1;
  }
  private static Map<UUID,List<String>> fixtureBodyState(ServerLevel level,Object car) {
    var ids=new ArrayList<UUID>();ids.add((UUID)read(car,"chassis"));
    for(var carrier:(List<?>)read(car,"carriers"))ids.add((UUID)read(carrier,"body"));
    var result=new LinkedHashMap<UUID,List<String>>();var container=SubLevelContainer.getContainer(level);var pipeline=container.physicsSystem().getPipeline();
    for(var id:ids){var body=(ServerSubLevel)container.getSubLevel(id);if(body==null)throw new IllegalStateException("Native body missing");
      result.put(id,List.of(body.logicalPose().position().toString(),body.logicalPose().orientation().toString(),pipeline.getLinearVelocity(body,new Vector3d()).toString(),pipeline.getAngularVelocity(body,new Vector3d()).toString()));}
    return result;
  }
  @GameTestGenerator public static List<TestFunction> cases() {
    if(!ModList.get().isLoaded("fourthdimension"))return List.of();
    String mode=System.getenv().getOrDefault("LS_ROUTE_CASE","physical_conflict");
    if(!Set.of("physical_conflict","changed_owner","stale_controller","opposed_direction","rollback","missing_carrier").contains(mode))return List.of();
    return List.of(new TestFunction("launchsystems_route_interop","coasterslaunchsystems_route_interop.bounded_route_"+mode,"coasterslaunchsystems_route_interop:empty",PLAYER?72000:3000,0,true,h->{
      if(!listening){NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,RouteInteropGameTests::observe);listening=true;}
      var scene=new Scene(h,mode);h.onEachTick(()->{try{scene.tick();}catch(RuntimeException failure){failure.printStackTrace();scene.cleanup();throw failure;}});
    }));
  }
}
