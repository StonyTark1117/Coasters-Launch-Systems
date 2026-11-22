package dev.coasterslaunchsystems.integration;

import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import dev.coasterslaunchsystems.runtime.EquipmentRuntime;
import dev.coasterslaunchsystems.runtime.RideControllers;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Optional, reflection-isolated API 1 bridge. Saved authority belongs to FD; live tokens belong here. */
final class FourthDimensionAuthority {
  static final String PROVIDER = "coasterslaunchsystems:local_controller";
  private static final Map<String, String> VARIANTS = Map.of("ARROW", "arrow_four_rail_v1",
      "ZACSPIN", "zacspin_legacy_v1", "FREE_SPIN", "sns_magnetic_v1", "ZACSPIN_REVISED", "zacspin_revised_v1");
  private final Class<?> api, vehicle, controllerType, tokenType;
  private final Map<ServerLevel, Map<UUID, Object>> tokens = new WeakHashMap<>();
  private final Map<ServerLevel, Set<BlockPos>> resumes = new WeakHashMap<>();

  FourthDimensionAuthority() throws ReflectiveOperationException {
    api = Class.forName("dev.fourthdimension.integration.RideAuthority");
    vehicle = Class.forName("dev.fourthdimension.runtime.FoundationData$Vehicle");
    controllerType = Class.forName("dev.fourthdimension.integration.RideAuthority$Controller");
    tokenType = Class.forName("dev.fourthdimension.integration.RideAuthority$Token");
    var external = Class.forName("dev.fourthdimension.integration.RideAuthority$ExternalProvider");
    if (api.getField("EXTERNAL_API").getInt(null) != 1) throw new ReflectiveOperationException("Unsupported external authority API");
    var bridge = Proxy.newProxyInstance(external.getClassLoader(), new Class<?>[] {external}, (proxy, method, args) -> switch (method.getName()) {
      case "supports" -> supported((ServerLevel) args[0], args[1]);
      case "controller" -> controller((ServerLevel) args[0], (BlockPos) args[1]);
      case "toString" -> PROVIDER;
      default -> throw new UnsupportedOperationException(method.getName());
    });
    api.getMethod("registerExternal", String.class, int.class, external).invoke(null, PROVIDER, 1, bridge);
  }

  private Object controller(ServerLevel level, BlockPos pos) {
    if (!(RideControllers.loadedBlockEntity(level, pos) instanceof RideControllerEntity bound)) return null;
    return Proxy.newProxyInstance(controllerType.getClassLoader(), new Class<?>[] {controllerType}, (proxy, method, args) -> switch (method.getName()) {
      case "authorityId" -> bound.identity();
      case "authorizesRide" -> bound.reservation().contains((UUID) args[0]);
      case "authorityAvailable" -> RideControllers.available(level, bound) && bound.armed() && bound.fault().isEmpty();
      case "validateRelease" -> { validateRelease(level, bound); yield null; }
      case "toString" -> PROVIDER + "@" + pos;
      default -> throw new UnsupportedOperationException(method.getName());
    });
  }

  private void validateRelease(ServerLevel level, RideControllerEntity bound) {
    if (!RideControllers.available(level, bound) || !bound.armed() || !bound.fault().isEmpty()
        || bound.reservation().isEmpty() || bound.resolve(level) == null || bound.modules().isEmpty())
      throw new IllegalArgumentException("Launch Systems controller is not ready for retained authority");
    for (var link : bound.modules()) {
      if (!(RideControllers.loadedBlockEntity(level, link.position()) instanceof EquipmentCabinetEntity module)
          || !link.identity().equals(module.identity()) || !EquipmentRuntime.available(level, module)
          || !module.armed() || !module.fault().isEmpty() || RideControllers.owner(level, module) != bound)
        throw new IllegalArgumentException("Launch Systems linked equipment is unavailable");
    }
  }

  boolean supported(ServerLevel level, Object v) {
    try {
      String expected = VARIANTS.get(((Enum<?>) field(v, "family")).name());
      return expected != null && expected.equals(field(v, "variant")) && field(v, "recoveryCart") == null
          && !(boolean) field(v, "alignmentRequired")
          && !(boolean) Class.forName("dev.fourthdimension.integration.RideLocalOperations")
              .getMethod("isManaged", ServerLevel.class, vehicle).invoke(null, level, v);
    } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Fourth Dimension eligibility unavailable", failure); }
  }

  boolean denied(ServerLevel level, Object v) throws ReflectiveOperationException {
    var state = field(v, "authority");
    if (field(state, "owner") == null) return false;
    call("holds", new Class<?>[] {ServerLevel.class, vehicle}, level, v); // Refresh lost/restored ownership first.
    return !PROVIDER.equals(field(state, "provider")) || (boolean) field(state, "suspended")
        || !Objects.equals(token(v), tokens.getOrDefault(level, Map.of()).get((UUID) field(v, "chassis")));
  }

  /** A retained owner may stop its train without reviving a suspended drive token. */
  boolean retainsBrakeLease(ServerLevel level, Object v, Set<UUID> consist) throws ReflectiveOperationException {
    var state = field(v, "authority");
    if (!(boolean) field(state, "suspended")) return false;
    if (!PROVIDER.equals(field(state, "provider")) || field(state, "owner") == null
        || !(field(state, "controller") instanceof BlockPos pos)
        || !(RideControllers.loadedBlockEntity(level, pos) instanceof RideControllerEntity bound)
        || !RideControllers.available(level, bound) || !bound.identity().equals(field(state, "owner"))
        || !bound.reservation().equals(consist)
        || !new HashSet<>((List<?>) field(state, "cars")).equals(consist)) return false;
    for (var id : consist) {
      var member = find(level, id);
      if (member == null || !supported(level, member)) return false;
      var other = field(member, "authority");
      if (!PROVIDER.equals(field(other, "provider")) || !bound.identity().equals(field(other, "owner"))
          || !pos.equals(field(other, "controller"))
          || !new HashSet<>((List<?>) field(other, "cars")).equals(consist)) return false;
    }
    return true;
  }

  boolean permitted(ServerLevel level, Object v) throws ReflectiveOperationException {
    var id = (UUID) field(v, "chassis"); var token = tokens.getOrDefault(level, Map.of()).get(id);
    return token != null && (boolean) call("permits", new Class<?>[] {ServerLevel.class, UUID.class, tokenType, String.class}, level, id, token, PROVIDER);
  }

  void requestResume(ServerLevel level, BlockPos pos) { resumes.computeIfAbsent(level, ignored -> new HashSet<>()).add(pos.immutable()); }

  boolean reserve(ServerLevel level, UUID id, BlockPos pos) {
    try {
      var v = find(level, id);
      if (v == null || !supported(level, v) || !(RideControllers.loadedBlockEntity(level, pos) instanceof RideControllerEntity bound)
          || !bound.reservation().contains(id)) return false;
      var state = field(v, "authority");
      if (field(state, "owner") == null) {
        var pending = resumes.get(level); if (pending != null) pending.remove(pos);
        remember(level, v, call("claim", new Class<?>[] {ServerLevel.class, BlockPos.class, UUID.class, String.class}, level, pos, id, PROVIDER));
        return true;
      }
      if (!PROVIDER.equals(field(state, "provider")) || !pos.equals(field(state, "controller"))
          || !bound.identity().equals(field(state, "owner"))) return false;
      call("holds", new Class<?>[] {ServerLevel.class, vehicle}, level, v);
      var pending = resumes.get(level);
      if (pending != null && pending.remove(pos)) {
        remember(level, v, call("resume", new Class<?>[] {ServerLevel.class, BlockPos.class, UUID.class, tokenType}, level, pos, id, token(v)));
      }
      return !denied(level, v);
    } catch (ReflectiveOperationException | IllegalArgumentException failure) {
      com.mojang.logging.LogUtils.getLogger().warn("Fourth Dimension authority reservation denied: {}", failure.getMessage());
      return false;
    }
  }

  boolean dispatch(ServerLevel level, Object v) throws ReflectiveOperationException {
    if (denied(level, v) || field(field(v, "authority"), "owner") == null) return false;
    var id = (UUID) field(v, "chassis"); var pos = (BlockPos) field(field(v, "authority"), "controller");
    try {
      call("dispatch", new Class<?>[] {ServerLevel.class, BlockPos.class, UUID.class, tokenType}, level, pos, id, tokens.get(level).get(id));
      remember(level, v, token(v)); return true;
    } catch (IllegalArgumentException rejected) { return false; }
  }

  boolean capture(ServerLevel level, Object v) throws ReflectiveOperationException {
    if (denied(level, v) || field(field(v, "authority"), "owner") == null) return false;
    var id = (UUID) field(v, "chassis"); var pos = (BlockPos) field(field(v, "authority"), "controller");
    try {
      remember(level, v, call("capture", new Class<?>[] {ServerLevel.class, BlockPos.class, UUID.class, tokenType}, level, pos, id, tokens.get(level).get(id)));
      return true;
    } catch (IllegalArgumentException rejected) { return false; }
  }

  boolean transfer(ServerLevel level, UUID id, BlockPos source, BlockPos target) {
    try {
      var v=find(level,id);
      if(v==null || denied(level,v) || !source.equals(field(field(v,"authority"),"controller"))) return false;
      var cars=new HashSet<UUID>();
      for(var car:(List<?>)field(field(v,"authority"),"cars"))cars.add((UUID)car);
      // This neutral operation proves every body is stopped under its native hold before capture.
      if(!dev.coasterslaunchsystems.api.MotionProviders.requestCapture(level,id,cars))return false;
      remember(level,v,call("transfer",new Class<?>[]{ServerLevel.class,BlockPos.class,UUID.class,tokenType,BlockPos.class},
          level,source,id,tokens.get(level).get(id),target));
      var pending=resumes.get(level); if(pending!=null)pending.remove(target);
      return true;
    } catch(ReflectiveOperationException | IllegalArgumentException rejected) { return false; }
  }

  private void remember(ServerLevel level, Object v, Object token) throws ReflectiveOperationException {
    var cache = tokens.computeIfAbsent(level, ignored -> new HashMap<>());
    for (var id : (List<?>) field(field(v, "authority"), "cars")) cache.put((UUID) id, token);
  }
  private Object find(ServerLevel level, UUID id) throws ReflectiveOperationException {
    var data = Class.forName("dev.fourthdimension.runtime.FoundationData");
    return ((Map<?, ?>) data.getField("vehicles").get(data.getMethod("get", ServerLevel.class).invoke(null, level))).get(id);
  }
  private Object token(Object v) throws ReflectiveOperationException { return call("token", new Class<?>[] {vehicle}, v); }
  private Object call(String name, Class<?>[] types, Object... args) throws ReflectiveOperationException {
    try { return api.getMethod(name, types).invoke(null, args); }
    catch (InvocationTargetException failure) {
      if (failure.getCause() instanceof RuntimeException runtime) throw runtime;
      throw failure;
    }
  }
  private static Object field(Object object, String name) throws ReflectiveOperationException { return object.getClass().getField(name).get(object); }
}
