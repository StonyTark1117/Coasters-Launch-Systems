package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import dev.coasterslaunchsystems.runtime.OrdinaryMotionProvider;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Explicit, isolated release-candidate gaps; no replay of accepted family matrices. */
@GameTestHolder("coasterslaunchsystems_lifecycle")
public final class LifecycleMatrixGameTests {
  private static final BlockPos CONTROLLER = new BlockPos(516, 206, -888);
  @GameTestGenerator public static List<TestFunction> lifecycle() {
    String phase = System.getenv("LS_LIFECYCLE_PHASE");
    if (phase == null) return List.of();
    if (phase.equals("mixed_provider")) return List.of(test("mixed_provider", h -> mixed(h, false)));
    if (phase.startsWith("mixed")) {
      var cases = new ArrayList<>(List.of(test("mixed_provider", h -> mixed(h, false)), test("mixed_family", h -> mixed(h, true))));
      if (phase.equals("mixed")) cases.add(test("owner_lifecycle", LifecycleMatrixGameTests::owners));
      return cases;
    }
    return List.of(test(phase, h -> { if (phase.equals("prepare")) prepare(h); else reopen(h, phase); }));
  }
  private static TestFunction test(String name, java.util.function.Consumer<GameTestHelper> action) {
    return new TestFunction("ls_lifecycle_" + name, "coasterslaunchsystems_lifecycle." + name,
        "coasterslaunchsystems_lifecycle:empty", 400, 0, true, action);
  }
  private static Path receipt() { return Path.of(Objects.requireNonNull(System.getenv("LS_LIFECYCLE_ROOT")), "identities.properties"); }
  private static void prepare(GameTestHelper h) {
    h.assertTrue(ModList.get().isLoaded("fourthdimension"), "Prepare requires actual optional provider");
    var s = new RideControllerGameTests.Scene(h, 10, "wing_forward", 208);
    s.at(55, () -> { s.install(false); s.controller.setArmed(true); });
    s.at(110, () -> {
      var id = s.base.parents.getFirst().getUniqueId();
      var m = MotionProviders.resolve(s.base.level, id).orElseThrow();
      h.assertTrue(s.controller.phase() == RideControllerEntity.State.READY && m.provider().equals("fourthdimension:authority1"), "Native Wing installation not ready: " + s.controller.status());
      // Keep the actual saved bodies loaded even without FD's runtime or a connected player.
      // This ticket changes availability only; it creates no forces or ownership.
      var container = SubLevelContainer.getContainer(s.base.level);
      for (var bodyId : m.bodies()) container.addForceLoadTicket((ServerSubLevel) container.getSubLevel(bodyId),
          dev.coasterslaunchsystems.runtime.CableTickets.LEGACY_CARRIAGE, id);
      var p = new Properties(); p.setProperty("chassis", id.toString()); p.setProperty("controller", s.controller.identity().toString()); p.setProperty("provider", m.provider());
      try (var out = Files.newOutputStream(receipt())) { p.store(out, "Actual saved Wing installation"); } catch (java.io.IOException e) { throw new IllegalStateException(e); }
      System.out.println("LS902_PROVIDER prepare actualWing=true controller=" + s.controller.identity() + " chassis=" + id + " provider=" + m.provider());
      h.succeed();
    });
  }
  private static void reopen(GameTestHelper h, String phase) {
    boolean absent = phase.equals("absent");
    h.assertTrue(absent != ModList.get().isLoaded("fourthdimension"), "Wrong actual optional mod classpath for " + phase);
    var p = new Properties();
    try (var in = Files.newInputStream(receipt())) { p.load(in); } catch (java.io.IOException e) { throw new IllegalStateException(e); }
    var id = UUID.fromString(p.getProperty("chassis"));
    h.getLevel().getChunkAt(CONTROLLER);
    h.runAtTickTime(100, () -> {
      var c = (RideControllerEntity) h.getLevel().getBlockEntity(CONTROLLER);
      h.assertTrue(c != null && c.identity().toString().equals(p.getProperty("controller")) && c.reservation().equals(Set.of(id))
          && !c.armed() && !c.requestPending() && (c.phase() == RideControllerEntity.State.INSPECTION || c.phase() == RideControllerEntity.State.FAULT), "Saved controller identity/inspection/reservation changed: " + (c == null ? "missing" : c.status()));
      var m = MotionProviders.resolve(h.getLevel(), id).orElseThrow();
      System.out.println("LS902_PROVIDER " + phase + " actualModLoaded=" + ModList.get().isLoaded("fourthdimension") + " chassis=" + id + " snapshot=" + m);
      h.assertTrue(m.provider().equals(p.getProperty("provider")), "Saved optional vehicle became " + m.provider());
      h.assertTrue(!m.propulsionAllowed() && !MotionProviders.requestDispatch(h.getLevel(), id), "Reopen replayed propulsion/dispatch");
      if (absent) h.assertTrue(!m.dispatchReady() && !m.brakingAllowed() && m.interfaces().isEmpty(), "Missing provider granted hardware authority");
      else h.assertTrue(CatalogBoundaryGameTests.vehicles(h.getLevel()).stream().anyMatch(v -> id.equals(read(v, "chassis")) && "wing_fixed_v1".equals(read(v, "variant"))), "Restored provider lost original mechanical identity");
      h.succeed();
    });
  }
  private static void mixed(GameTestHelper h, boolean nativeFamily) {
    var s = new RideControllerGameTests.Scene(h, nativeFamily ? 6 : 4, "wing_forward", 208);
    // Each actual family is created on its own native supported rail. No coupling
    // guard is bypassed to manufacture an unsupported mixed consist.
    var otherScene = nativeFamily ? new RideControllerGameTests.Scene(h, 8, "classic_spinning_forward", 208) : null;
    if (!nativeFamily) s.at(40, () -> s.base.cart(14));
    s.at(110, () -> {
      var first = s.base.parents.getFirst();
      var second = nativeFamily ? otherScene.base.parents.getFirst() : s.base.parents.getLast();
      var beforeA = MotionProviders.resolve(s.base.level, first.getUniqueId()).orElseThrow();
      var beforeB = MotionProviders.resolve(s.base.level, second.getUniqueId()).orElseThrow();
      h.assertTrue(beforeA.provider().equals("fourthdimension:authority1")
          && beforeB.provider().equals(nativeFamily ? "fourthdimension:authority1" : OrdinaryMotionProvider.ID), "Fixture provider identities differ");
      h.assertTrue(!CoasterCartTrainLinkConstraint.linkCarts(s.base.level, first, second, 6)
          && !CoasterCartTrainLinkConstraint.linkCarts(s.base.level, second, first, 6), "Native mixed coupling was accepted");
      h.assertTrue(MotionProviders.resolve(s.base.level, first.getUniqueId()).orElseThrow().equals(beforeA)
          && MotionProviders.resolve(s.base.level, second.getUniqueId()).orElseThrow().equals(beforeB)
          && !first.isRemoved() && !second.isRemoved(), "Rejected coupling changed provider, body, permissions or consist");
      System.out.println("LS901_MIXED PASS kind=" + (nativeFamily ? "Wing+ClassicSpinning" : "Wing+ordinary") + " normalNativeLinkDenied=true bothDirections=true originalBodiesOwnersPermissionsConsistsUnchanged=true");
      if (otherScene != null) otherScene.cleanup(); s.cleanup(); h.succeed();
    });
  }
  private static void owners(GameTestHelper h) {
    var s = new OrdinaryPassengerGameTests.Scene(h, 12, 208, -896);
    var body = s.cart(8); var active = new HashSet<UUID>(); active.add(body.getUniqueId());
    var otherActive = new HashSet<UUID>();
    var primary = owner("launchsystems:test_primary", active);
    var other = owner("launchsystems:test_takeover", otherActive);
    MotionProviders.register(primary); MotionProviders.register(other);
    s.at(40, () -> {
      var id = body.getUniqueId();
      try {
        var type = Class.forName("dev.coasterslaunchsystems.api.SavedMotionOwners");
        var get = type.getDeclaredMethod("get", net.minecraft.server.level.ServerLevel.class); get.setAccessible(true);
        var data = get.invoke(null, s.level);
        var mapField = type.getDeclaredField("owners"); mapField.setAccessible(true);
        @SuppressWarnings("unchecked") var saved = (Map<UUID,String>) mapField.get(data);
        var registry = MotionProviders.class.getDeclaredField("PROVIDERS"); registry.setAccessible(true);
        @SuppressWarnings("unchecked") var providers = (List<dev.coasterslaunchsystems.api.VehicleMotionProvider>) registry.get(null);
        h.assertTrue(MotionProviders.resolve(s.level,id).orElseThrow().provider().equals(primary.id()), "Primary claim missing");
        otherActive.add(id);
        try { MotionProviders.resolve(s.level,id); h.fail("Simultaneous providers were accepted"); } catch (IllegalStateException expected) { }
        providers.remove(primary);
        try {
          var denied=MotionProviders.resolve(s.level,id).orElseThrow();
          h.assertTrue(denied.provider().equals(primary.id())&&!denied.propulsionAllowed()&&!denied.dispatchReady(),"Absent owner permitted takeover");
        } finally { providers.add(primary); otherActive.clear(); }
        active.clear(); // Explicit test-provider lifecycle release, as in nativePassengerMassLifecycle.
        h.assertTrue(MotionProviders.resolve(s.level,id).orElseThrow().provider().equals(OrdinaryMotionProvider.ID)&&!saved.containsKey(id),"Present owner's deliberate release broke ordinary fallback");
        saved.put(id,"absent:unknown_provider");
        h.assertTrue(!MotionProviders.resolve(s.level,id).orElseThrow().propulsionAllowed(),"Unknown persisted provider failed open"); saved.remove(id);
        var load=type.getDeclaredMethod("load",net.minecraft.nbt.CompoundTag.class,net.minecraft.core.HolderLookup.Provider.class);load.setAccessible(true);
        var valid=type.getDeclaredMethod("valid");valid.setAccessible(true);
        for(int mode=0;mode<3;mode++) {
          var tag=new net.minecraft.nbt.CompoundTag();tag.putInt("schema",mode==0?99:1);
          if(mode==2){var rows=new net.minecraft.nbt.ListTag();var bad=new net.minecraft.nbt.CompoundTag();bad.putString("provider","bad provider");rows.add(bad);tag.put("owners",rows);}
          var bad=(net.minecraft.world.level.saveddata.SavedData)load.invoke(null,tag,s.level.registryAccess());
          h.assertTrue(!(boolean)valid.invoke(bad)&&bad.save(new net.minecraft.nbt.CompoundTag(),s.level.registryAccess()).equals(tag),"Malformed/unknown schema silently reset");
        }
        var rejected=type.getDeclaredField("rejectedData");rejected.setAccessible(true);
        rejected.set(data,new net.minecraft.nbt.CompoundTag());
        try {var denied=MotionProviders.resolve(s.level,id).orElseThrow();h.assertTrue(!denied.dispatchReady()&&!denied.propulsionAllowed()&&!denied.brakingAllowed(),"Malformed ledger allowed ordinary fallback");}
        finally {rejected.set(data,null);}
        active.add(id); MotionProviders.resolve(s.level,id);
        var container=SubLevelContainer.getContainer(s.level);
        // UNLOADED retains identity, whereas permanent removal retires it. Use a separate
        // body for unload so the saved native Wing world remains untouched.
        container.removeSubLevel(body,dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.UNLOADED);
        active.clear(); // Real native providers return empty while their chassis is unloaded.
        h.assertTrue(MotionProviders.resolve(s.level,id).isEmpty(),"Unloaded body was fabricated");
        h.assertTrue(saved.get(id).equals(primary.id()),"Unload forgot provider identity");
        var removed=s.cart(20);active.add(removed.getUniqueId());MotionProviders.resolve(s.level,removed.getUniqueId());
        container.removeSubLevel(removed,dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
        h.assertTrue(!saved.containsKey(removed.getUniqueId()),"Permanent removal leaked provider identity");
        System.out.println("LS902_OWNER_CONTRACT PASS simultaneousConflict=true absentTakeoverDenied=true registeredRelease=true unknownProviderDenied=true malformedPreserved=true unloadRetains=true permanentRemovalForgets=true");
        h.succeed();
      } catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
    });
  }
  private static dev.coasterslaunchsystems.api.VehicleMotionProvider owner(String name, Set<UUID> ids) {
    return new dev.coasterslaunchsystems.api.VehicleMotionProvider() {
      public int apiVersion(){return API_VERSION;} public String id(){return name;}
      public Optional<dev.coasterslaunchsystems.api.MotionVehicle> resolve(net.minecraft.server.level.ServerLevel level,UUID id){
        return ids.contains(id)?Optional.of(new dev.coasterslaunchsystems.api.MotionVehicle(name,id,List.of(id),List.of(id),Set.of(),false,false,false,"test:retained",2,256,0)):Optional.empty();
      }
    };
  }
}
