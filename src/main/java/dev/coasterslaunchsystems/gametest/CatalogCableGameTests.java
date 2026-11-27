package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Finite new central-socket certificates; same real constrained store/pull/return fixture. */
@GameTestHolder("coasterslaunchsystems_catalog_cable")
public final class CatalogCableGameTests {
  @GameTestGenerator public static List<TestFunction> cables() {
    if (!net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) return List.of();
    var result=new ArrayList<TestFunction>();int slot=0;
    var selected=System.getenv("LS_CATALOG_CABLE_CASES");
    for(var family:List.of("legacy","free_spin","revised","axis","dive","fixed_inverted","suspended","pipeline","flying","new_generation_flying")) {
      int index=slot++;
      if(selected==null&&index>=5 || selected!=null&&!Arrays.asList(selected.split(",")).contains(family))continue;
      var original=CableControllerGameTests.currentCase(family+"_controller_dispatch",index);
      result.add(new TestFunction("ls_catalog_cable","coasterslaunchsystems_catalog_cable."+family,
          "coasterslaunchsystems_catalog_cable:empty",original.maxTicks(),0,true,h->{
            if(List.of("suspended","new_generation_flying").contains(family)) { guideDenial(h,family,index); return; }
            original.function().accept(h);
            if(!List.of("legacy","free_spin","revised","fixed_inverted").contains(family))prepareDock(h,family);
          }));
    }
    return result;
  }
  private static void guideDenial(GameTestHelper h,String family,int slot) {
    var r=new RideControllerGameTests.Scene(h,slot,family+"_controller_dispatch",224);
    prepareDock(h,family);
    h.runAtTickTime(230,()->r.boardFourthDimension());
    h.runAtTickTime(250,()-> {
      var track=dev.coasterslaunchsystems.track.DirectedTrack.resolve(r.base.level,r.base.from,r.base.to).orElseThrow();
      var v=r.vehicles.getFirst();var certificate=dev.coasterslaunchsystems.integration.FourthDimensionCertificates.find(((Enum<?>)read(v,"family")).name(),(String)read(v,"variant"));
      var socket=certificate.offset(dev.coasterslaunchsystems.math.Mechanism.FLYWHEEL);
      r.base.parents.getFirst().getPlot().getEmbeddedLevelAccessor().setBlock(socket,dev.coasterslaunchsystems.content.EquipmentContent.CATCH_SOCKET.get().defaultBlockState(),3);
      for(int step=0;step<=128;step++) r.base.level.setBlockAndUpdate(dev.coasterslaunchsystems.runtime.NativeCableRig.guideBlockAt(r.base.level,track,track.length()*step/128),dev.coasterslaunchsystems.content.EquipmentContent.CABLE_GUIDE.get().defaultBlockState());
    });
    h.runAtTickTime(300,()-> {
      var v=r.vehicles.getFirst();var motion=CatalogBoundaryGameTests.motion(r.base.level,v);
      var fault=(String)read(v,"fault");
      h.assertTrue(!r.base.riders.isEmpty()&&r.base.riders.stream().allMatch(p->p.isPassenger()),"Guide denial did not retain actual native riders");
      h.assertTrue(fault.contains(family.equals("suspended")?"sweep obstructed":"COMBINED_SWEEP_OBSTRUCTED"),"Guide did not trip native full-envelope guard: "+fault);
      h.assertTrue(!motion.dispatchReady()&&!motion.propulsionAllowed()&&!motion.interfaces().contains(dev.coasterslaunchsystems.math.Mechanism.FLYWHEEL),"Obstructed guide retained a cable certificate or dispatch");
      var track=dev.coasterslaunchsystems.track.DirectedTrack.resolve(r.base.level,r.base.from,r.base.to).orElseThrow();var hit=track.hitAtDistance(r.base.level,8);
      var bed=dev.coasterslaunchsystems.runtime.NativeCableRig.guideBlockAt(r.base.level,track,8);
      double clear=hit.point().y-bed.getY()-.1875;
      h.assertTrue(clear<(family.equals("suspended")?4.7:5.25),"Measured guide did not intrude native lower envelope");
      System.out.println("LS_CABLE_GUIDE_DENIAL family="+family+" riders="+r.base.riders.size()+" lower_clearance="+clear+" native_fault="+fault+" certificate_denied=true");
      r.cleanup();h.succeed();
    });
  }
  private static void prepareDock(GameTestHelper h,String family) {
    var level=h.getLevel();String name=family.equals("new_generation_flying")?"FLYING":family.toUpperCase(java.util.Locale.ROOT);
    h.runAtTickTime(25,()-> {
      var v=vehicle(level,family);var body=CatalogBoundaryGameTests.body(level,v);
      var center=dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(body);
      var pos=BlockPos.containing(center.x+7,center.y-3,center.z+8);
      var station=(Block)((java.util.function.Supplier<?>)readStatic("dev.fourthdimension.station.StationContent","STATION")).get();
      level.setBlockAndUpdate(pos,station.defaultBlockState());write(level.getBlockEntity(pos),"vehicle",read(v,"chassis"));write(v,"dock",pos);
    });
    h.runAtTickTime(100,()-> {
      var v=vehicle(level,family);
      invoke("dev.fourthdimension.vehicle."+switch(family) { case "axis" -> "AxisRuntime"; case "dive" -> "DiveRuntime"; case "suspended" -> "SuspendedRuntime"; case "pipeline" -> "PipelineRuntime"; case "flying" -> "FlyingRuntime"; case "new_generation_flying" -> "NewGenerationFlyingRuntime"; default -> throw new IllegalArgumentException(family); },"rebindDock",
          new Class<?>[]{ServerLevel.class,type("dev.fourthdimension.runtime.FoundationData$Vehicle"),BlockPos.class},level,v,read(v,"dock"));
    });
    h.runAtTickTime(240,()-> {
      var v=vehicle(level,family);
      if(family.contains("flying")) {
        for(boolean body:new boolean[]{true,false}) invoke(runtime(family),"restraints",
            new Class<?>[]{ServerLevel.class,type("dev.fourthdimension.runtime.FoundationData$Vehicle"),boolean.class,boolean.class},level,v,body,true);
        if(family.equals("new_generation_flying")) nativeProgram(level,v);
      } else invoke("dev.fourthdimension.vehicle.VehicleOperations","restraints",new Class<?>[]{ServerLevel.class,type("dev.fourthdimension.runtime.FoundationData$Vehicle"),boolean.class},level,v,true);
    });
    if(family.equals("flying")) h.runAtTickTime(260,()->invoke(runtime(family),"requestPosture",
        new Class<?>[]{ServerLevel.class,type("dev.fourthdimension.runtime.FoundationData$Vehicle"),boolean.class,boolean.class},level,vehicle(level,family),true,false));
    if(List.of("suspended","pipeline","flying","new_generation_flying").contains(family)) for(int tick:new int[]{159,229,539}) h.runAtTickTime(tick,()-> {
      var v=vehicle(level,family);var body=CatalogBoundaryGameTests.body(level,v);
      var track=dev.coasterslaunchsystems.track.DirectedTrack.resolve(level,(BlockPos)read(v,"boundFrom"),(BlockPos)read(v,"boundTo")).orElseThrow();
      var center=dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(body);
      var hit=dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.closestHitOnEdge(level,track.edge(),new net.minecraft.world.phys.Vec3(center.x,center.y,center.z));
      var bed=dev.coasterslaunchsystems.runtime.NativeCableRig.guideBlockAt(level,
          dev.coasterslaunchsystems.track.DirectedTrack.resolve(level,hit.edge().from(),hit.edge().to()).orElseThrow(),8);
      var blocked=invoke("dev.fourthdimension.vehicle."+switch(family){case "new_generation_flying"->"NewGenerationFlying";default->family.substring(0,1).toUpperCase()+family.substring(1);}+"Track","obstruction",
          new Class<?>[]{net.minecraft.world.level.Level.class,dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit.class},level,hit);
      System.out.println("LS_CABLE_NATIVE family="+family+" tick="+tick+" fault="+read(v,"fault")+" boarding="+read(v,"boardingReady")+" obstruction="+blocked+" rail_y="+hit.point().y+" bed="+bed+" bed_top="+(bed.getY()+.1875));
    });
  }
  private static Object vehicle(ServerLevel level,String family) {
    var name=family.equals("new_generation_flying")?"FLYING":family.toUpperCase(Locale.ROOT);
    return CatalogBoundaryGameTests.vehicles(level).stream().filter(v->((Enum<?>)read(v,"family")).name().equals(name))
        .filter(v->!name.equals("FLYING")||read(v,"variant").equals(family.equals("flying")?"station_changeover_v1":"route_changeover_yaw_v1"))
        .findFirst().orElseThrow();
  }
  private static String runtime(String family){return "dev.fourthdimension.vehicle."+(family.equals("flying")?"FlyingRuntime":"NewGenerationFlyingRuntime");}
  private static void nativeProgram(ServerLevel level,Object v) {
    var rail=invoke(runtime("new_generation_flying"),"rail",new Class<?>[]{ServerLevel.class,type("dev.fourthdimension.runtime.FoundationData$Vehicle")},level,v);
    var tag=new net.minecraft.nbt.CompoundTag();tag.putInt("version",1);tag.putLong("revision",1);tag.putLong("geometry",((Number)read(rail,"geometryHash")).longValue());
    var owner=UUID.randomUUID();tag.putUUID("owner",owner);write(read(v,"yaw"),"owner",owner);
    var list=new net.minecraft.nbt.ListTag();var interval=new net.minecraft.nbt.CompoundTag();
    interval.putUUID("id",UUID.randomUUID());interval.putString("name","LS cable native yaw profile");
    interval.putDouble("start",0);interval.putDouble("end",64);interval.putString("mode","PROFILE");
    interval.putDouble("phi_from",0);interval.putDouble("phi_to",Math.PI/2);interval.putString("winding","NEAREST");
    interval.putDouble("speed",.45);interval.putDouble("acceleration",.45);var keys=new net.minecraft.nbt.ListTag();
    for(double at:new double[]{0,64}) {var key=new net.minecraft.nbt.CompoundTag();key.putDouble("distance",at);key.putDouble("angle",at/64*Math.PI/2);keys.add(key);}
    interval.put("keys",keys);list.add(interval);tag.put("intervals",list);
    write(rail,"yaw",invoke("dev.fourthdimension.vehicle.YawSection","read",new Class<?>[]{net.minecraft.nbt.CompoundTag.class},tag));
  }
}
