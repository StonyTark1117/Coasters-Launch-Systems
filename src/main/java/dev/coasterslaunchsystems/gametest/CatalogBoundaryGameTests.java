package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;
import com.mojang.authlib.GameProfile;
import dev.coasterslaunchsystems.api.*;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.runtime.NativeForces;
import dev.coasterslaunchsystems.runtime.VehicleInterfaces;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Pinned FD native fixtures plus LS-specific certificate/appearance boundaries; no client claims. */
@GameTestHolder("coasterslaunchsystems_catalog")
public final class CatalogBoundaryGameTests {
  private static final String DATA="dev.fourthdimension.runtime.FoundationData";
  @GameTestGenerator public static List<TestFunction> boundaries() {
    if (!net.neoforged.fml.ModList.get().isLoaded("fourthdimension") || "appearance".equals(System.getenv("LS_CATALOG_PHASE"))) return List.of();
    return List.of(test("roller_ball", 350, CatalogBoundaryGameTests::roller),
        test("pax", 160, CatalogBoundaryGameTests::pax));
  }
  private static TestFunction test(String name,int ticks,java.util.function.Consumer<GameTestHelper> fn) {
    return new TestFunction("ls_catalog_"+name,"coasterslaunchsystems_catalog."+name,
        "coasterslaunchsystems_catalog:empty",ticks,0,true,fn);
  }
  static Collection<?> vehicles(ServerLevel level) {
    return ((Map<?,?>)read(invoke(DATA,"get",new Class<?>[]{ServerLevel.class},level),"vehicles")).values();
  }
  static Object family(ServerLevel level,String family) {
    return vehicles(level).stream().filter(v -> ((Enum<?>)read(v,"family")).name().equals(family)).findFirst().orElseThrow();
  }
  static ServerSubLevel body(ServerLevel level,Object v) {
    return (ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel((UUID)read(v,"chassis"));
  }
  static MotionVehicle motion(ServerLevel level,Object v) {
    return MotionProviders.resolve(level,(UUID)read(v,"chassis")).orElseThrow();
  }
  private static void roller(GameTestHelper h) {
    invoke("dev.fourthdimension.gametest.RollerBallGameTests","fourSeatNativeHingeAndLoad",new Class<?>[]{GameTestHelper.class},h);
    h.runAtTickTime(120,()-> installDeniedHardware(h,family(h.getLevel(),"ROLLER_BALL")));
    h.runAtTickTime(219,()-> {
      var v=family(h.getLevel(),"ROLLER_BALL");
      h.assertTrue(((List<?>)read(((List<?>)read(v,"carriers")).getFirst(),"occupants")).size()==4,"Loaded Roller Ball precondition");
      deny(h,v,"roller_chain_gravity_cycle");
    });
  }
  private static void deny(GameTestHelper h,Object v,String reason) {
    var level=h.getLevel();var id=(UUID)read(v,"chassis");var m=motion(level,v);
    long revision=((Number)read(v,"revision")).longValue();
    h.assertTrue(m.provider().equals("fourthdimension:authority1")&&m.interfaces().isEmpty(),"Excluded family fell through or acquired hardware certificate");
    h.assertTrue(!m.dispatchReady()&&!m.propulsionAllowed()&&!m.brakingAllowed(),"Excluded family acquired motion permission");
    h.assertTrue(!MotionProviders.requestDispatch(level,id)&&MotionProviders.captureState(level,id)==CaptureState.DENIED
        &&!MotionProviders.requestCapture(level,id,Set.copyOf(m.consist()))
        &&!MotionProviders.reserveAuthority(level,id,BlockPos.ZERO),"Excluded family operation accepted");
    h.assertTrue(((Number)read(v,"revision")).longValue()==revision,"Denied operation mutated native state");
    System.out.println("LS_CATALOG_DENIAL reason="+reason+" family="+read(v,"family")+" provider="+m.provider()+" bodies="+m.bodies().size()+" mass="+NativeForces.mass(level,m)+" owner="+m.exclusiveOperator()+" revisionUnchanged=true");
  }
  private static void pax(GameTestHelper h) {
    var level=h.getLevel();var from=new BlockPos(960,120,960);var to=from.offset(0,0,48);
    for(int x=59;x<=61;x++)for(int z=59;z<=64;z++)level.setChunkForced(x,z,true);
    for(var p:List.of(from,to))level.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"LSCatalogPax"));player.setGameMode(GameType.CREATIVE);
    player.setPos(to.getX()+2,to.getY()+1,to.getZ());
    var designType=type("dev.fourthdimension.track.TrackDesign");
    var design=Arrays.stream(designType.getEnumConstants()).filter(v->((Enum<?>)v).name().equals("PAX")).findFirst().orElseThrow();
    var item=(Item)((Supplier<?>)readStatic("dev.fourthdimension.track.TrackItems","ARROW")).get();player.setItemInHand(InteractionHand.MAIN_HAND,item.getDefaultInstance());
    invoke("dev.fourthdimension.track.TrackPlacement","connect",new Class<?>[]{ServerLevel.class,BlockPos.class,BlockPos.class,designType,Player.class,ItemStack.class},level,from,to,design,player,player.getMainHandItem());
    h.runAtTickTime(15,()-> {
      var edge=CoasterPathGraphManager.get(level).edges().stream().filter(e->e.from().equals(from)&&e.to().equals(to)||e.from().equals(to)&&e.to().equals(from)).findFirst().orElseThrow();
      var hit=new GraphHit(CoasterPathGraphManager.sampleEdgePosition(level,edge,.3),edge,.3);
      invoke("dev.fourthdimension.vehicle.VehicleFactory","create",new Class<?>[]{ServerLevel.class,designType,GraphHit.class},level,design,hit);
    });
    h.runAtTickTime(50,()-> installDeniedHardware(h,family(level,"PAX")));
    h.runAtTickTime(100,()-> {
      var v=family(level,"PAX");deny(h,v,"pax_exclusive_winch");
      h.assertTrue(motion(level,v).exclusiveOperator().equals("fourthdimension:pax_winch"),"PAX lost exclusive winch label");h.succeed();
    });
  }
  static void appearance(GameTestHelper h,boolean inverted) {
    invoke("dev.fourthdimension.gametest.BodyVariantGameTests",inverted?"fixedInvertedBodyTransactionsAndPersistence":"wingBodyTransactionsAndPersistence",new Class<?>[]{GameTestHelper.class},h);
    var initial=new MotionVehicle[1];var mass=new double[1];var contact=new String[1];
    h.runAtTickTime(60,()->install(h,family(h.getLevel(),inverted?"FIXED_INVERTED":"WING")));
    h.runAtTickTime(65,()-> {
      var level=h.getLevel();var v=family(level,inverted?"FIXED_INVERTED":"WING");
      var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"LSCatalogPalette"));player.setGameMode(GameType.CREATIVE);
      invoke("dev.fourthdimension.tools.PaletteService","apply",new Class<?>[]{ServerPlayer.class,type(DATA+"$Vehicle"),String.class},player,v,"OCEAN");
      initial[0]=motion(level,v);mass[0]=NativeForces.mass(level,initial[0]);contact[0]=contacts(body(level,v));
      h.assertTrue(!initial[0].interfaces().isEmpty(),"Body baseline has no positive LS interfaces");
    });
    h.runAtTickTime(180,()-> {
      var level=h.getLevel();var v=family(level,inverted?"FIXED_INVERTED":"WING");var after=motion(level,v);
      h.assertTrue(after.provider().equals("fourthdimension:authority1")&&after.interfaces().equals(initial[0].interfaces())
          &&after.bodies().equals(initial[0].bodies())&&after.consist().equals(initial[0].consist()),"Appearance changed certificate or body identities");
      h.assertTrue(Math.abs(NativeForces.mass(level,after)-mass[0])<1e-6&&contacts(body(level,v)).equals(contact[0]),"Appearance changed native mass/contact coordinates");
      h.assertTrue(read(v,"bodyId").equals(inverted?"fixed_inverted_open_frame":"wing_compact")&&((Enum<?>)read(v,"palette")).name().equals("OCEAN"),"Body/palette choice lost");
      var data=invoke(DATA,"get",new Class<?>[]{ServerLevel.class},level);
      try {
        var saved=(CompoundTag)data.getClass().getMethod("save",CompoundTag.class,HolderLookup.Provider.class).invoke(data,new CompoundTag(),level.registryAccess());
        var loaded=invoke(DATA,"load",new Class<?>[]{CompoundTag.class,HolderLookup.Provider.class},saved,level.registryAccess());
        var copy=((Map<?,?>)read(loaded,"vehicles")).get(read(v,"chassis"));
        h.assertTrue(read(copy,"bodyId").equals(read(v,"bodyId"))&&read(copy,"palette").equals(read(v,"palette"))&&read(copy,"variant").equals(read(v,"variant")),"Saved appearance/mechanical identity changed");
      } catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
      System.out.println("LS_CATALOG_BODY PASS family="+read(v,"family")+" body="+read(v,"bodyId")+" palette="+read(v,"palette")+" interfaces="+after.interfaces()+" mass="+mass[0]+" contactsUnchanged=true bodyIdsUnchanged=true saveLoad=true");
    });
  }
  private static String contacts(ServerSubLevel body) {
    return Arrays.asList(VehicleInterfaces.tirePoint(body),VehicleInterfaces.brakePoint(body),VehicleInterfaces.skidPoint(body),VehicleInterfaces.conductivePoint(body),VehicleInterfaces.linearPoint(body,Mechanism.LSM),VehicleInterfaces.linearPoint(body,Mechanism.LIM)).toString();
  }
  private static void installDeniedHardware(GameTestHelper h,Object v) {
    install(h,v);
    var plot=body(h.getLevel(),v).getPlot().getEmbeddedLevelAccessor();
    h.assertTrue(plot.getBlockState(new BlockPos(0,-2,0)).isAir(),"Socket replaces family hardware");
    plot.setBlock(new BlockPos(0,-2,0),EquipmentContent.CATCH_SOCKET.get().defaultBlockState(),3);
  }
  private static void install(GameTestHelper h,Object v) {
    var plot=body(h.getLevel(),v).getPlot().getEmbeddedLevelAccessor();
    var positions=List.of(new BlockPos(0,-1,0),new BlockPos(1,-1,0),new BlockPos(0,-2,-1),new BlockPos(-1,-1,0),new BlockPos(0,-1,-1),new BlockPos(0,-1,1));
    var blocks=List.of(EquipmentContent.TIRE_SURFACE,EquipmentContent.BRAKE_FIN,EquipmentContent.SKID_SHOE,EquipmentContent.CONDUCTIVE_FIN,EquipmentContent.LSM_PLATE,EquipmentContent.LIM_PLATE);
    var mechanisms=List.of(Mechanism.TIRE,Mechanism.FIN_BRAKE,Mechanism.SKID_BRAKE,Mechanism.EDDY_BRAKE,Mechanism.LSM,Mechanism.LIM);
    var certificate=dev.coasterslaunchsystems.integration.FourthDimensionCertificates.find(((Enum<?>)read(v,"family")).name(),(String)read(v,"variant"));
    if(certificate!=null&&certificate.longitudinalOffset()!=0) {
      for(int z=-1;z>=certificate.longitudinalOffset();z--) {
        var mount=new BlockPos(0,0,z);
        if(plot.getBlockState(mount).isAir())plot.setBlock(mount,net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState(),3);
      }
    }
    for(int i=0;i<positions.size();i++) {
      var position=certificate!=null&&certificate.allowed(mechanisms.get(i))?certificate.offset(mechanisms.get(i)):positions.get(i);
      h.assertTrue(plot.getBlockState(position).isAir(),"Certificate installation would replace native family hardware at "+position);
      plot.setBlock(position,blocks.get(i).get().defaultBlockState(),3);
    }
  }
}
