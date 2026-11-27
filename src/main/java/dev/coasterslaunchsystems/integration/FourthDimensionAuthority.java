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
  private final Class<?> api, vehicle, controllerType, tokenType;
  private final boolean routes, moving;
  private final Map<ServerLevel,Map<BlockPos,Long>> movingAcknowledgements=new WeakHashMap<>();
  private final Map<ServerLevel, Map<UUID, Object>> tokens = new WeakHashMap<>();
  private final Map<ServerLevel, Set<BlockPos>> resumes = new WeakHashMap<>();
  private record RouteGeometry(List<dev.coasterslaunchsystems.track.DirectedRoute.Span> spans,
      int revision, long handles, Set<BlockPos> chunks) {}
  private final Map<ServerLevel, Map<UUID, RouteGeometry>> routeGeometry = new WeakHashMap<>();

  FourthDimensionAuthority() throws ReflectiveOperationException {
    api = Class.forName("dev.fourthdimension.integration.RideAuthority");
    vehicle = Class.forName("dev.fourthdimension.runtime.FoundationData$Vehicle");
    controllerType = Class.forName("dev.fourthdimension.integration.RideAuthority$Controller");
    tokenType = Class.forName("dev.fourthdimension.integration.RideAuthority$Token");
    boolean routeApi;
    try {
      routeApi = api.getField("EXTERNAL_ROUTE_API").getInt(null) == 1;
      if (routeApi && api.getMethod("dispatchReady", ServerLevel.class, UUID.class, tokenType).getReturnType() != boolean.class)
        throw new NoSuchMethodException("Expected boolean native dispatch readiness query");
    }
    catch (NoSuchFieldException oldApi) { routeApi = false; }
    catch (NoSuchMethodException incompleteApi) {
      routeApi = false;
      com.mojang.logging.LogUtils.getLogger().warn("Fourth Dimension managed routes disabled: native dispatch readiness query unavailable");
    }
    routes = routeApi;
    boolean movingApi;
    try { movingApi=api.getField("EXTERNAL_MOVING_API").getInt(null)==1;
      if(movingApi)api.getMethod("returnFromMoving",ServerLevel.class,UUID.class,tokenType,BlockPos.class);
    } catch(NoSuchFieldException oldApi) { movingApi=false; }
    moving=movingApi;
    var external = Class.forName("dev.fourthdimension.integration.RideAuthority$ExternalProvider");
    if (api.getField("EXTERNAL_API").getInt(null) != 1) throw new ReflectiveOperationException("Unsupported external authority API");
    var bridge = Proxy.newProxyInstance(external.getClassLoader(), new Class<?>[] {external}, (proxy, method, args) -> switch (method.getName()) {
      case "supports" -> supported((ServerLevel) args[0], args[1]);
      case "accessoryPowerAvailable" -> accessoryPowerAvailable((ServerLevel) args[0], args[1]);
      case "supportsManagedRoutes" -> routes;
      case "supportsMoving" -> supportsMoving((ServerLevel)args[0],(BlockPos)args[1],args[2]);
      case "routeDirection" -> routeDirection((ServerLevel) args[0], (BlockPos) args[1], args[2]);
      case "operatorStatus" -> {
        var bound = operatorController((ServerLevel) args[0], args[1]);
        yield dev.coasterslaunchsystems.runtime.OperatorStatus.sample((ServerLevel) args[0], bound,
            (UUID) field(args[1], "chassis"));
      }
      case "operatorAction" -> {
        var bound = operatorController((ServerLevel) args[0], args[1]);
        dev.coasterslaunchsystems.runtime.OperatorStatus.action((ServerLevel) args[0], bound,
            (UUID) field(args[1], "chassis"), (int) args[2], (long) args[3]);
        yield null;
      }
      case "controller" -> controller((ServerLevel) args[0], (BlockPos) args[1]);
      case "toString" -> PROVIDER;
      default -> throw new UnsupportedOperationException(method.getName());
    });
    api.getMethod("registerExternal", String.class, int.class, external).invoke(null, PROVIDER, 1, bridge);
  }

  private boolean supportsMoving(ServerLevel level,BlockPos pos,Object member) throws ReflectiveOperationException {
    if(!moving||!supported(level,member)||managed(level,member)
        ||!(RideControllers.loadedBlockEntity(level,pos) instanceof RideControllerEntity bound)
        ||!RideControllers.available(level,bound)||!bound.armed()||!bound.fault().isEmpty()
        ||bound.swingProgram()||bound.transferring()||bound.incoming()!=null
        ||bound.resolve(level)==null||bound.modules().isEmpty()
        ||!bound.reservation().equals(new HashSet<>((List<UUID>)field(field(member,"authority"),"cars"))))return false;
    for(var link:bound.modules()){
      if(!(RideControllers.loadedBlockEntity(level,link.position()) instanceof EquipmentCabinetEntity module)
          ||!link.identity().equals(module.identity())||!EquipmentRuntime.available(level,module)
          ||!module.armed()||!module.fault().isEmpty()||RideControllers.owner(level,module)!=bound
          ||module.mechanism().cable()
          ||!(module.mechanism()==dev.coasterslaunchsystems.math.Mechanism.TIRE
            ||module.mechanism()==dev.coasterslaunchsystems.math.Mechanism.LSM
            ||module.mechanism()==dev.coasterslaunchsystems.math.Mechanism.LIM
            ||module.mechanism()==dev.coasterslaunchsystems.math.Mechanism.FIN_BRAKE))return false;
    }
    return true;
  }
  boolean movingRetained(ServerLevel level,Object member) throws ReflectiveOperationException {
    return moving&&(boolean)call("movingRetained",new Class<?>[]{ServerLevel.class,UUID.class},level,field(member,"chassis"));
  }
  boolean movingDeparture(ServerLevel level,Object member) throws ReflectiveOperationException {
    return moving&&(boolean)call("movingDeparture",new Class<?>[]{ServerLevel.class,UUID.class},level,field(member,"chassis"));
  }
  private boolean movingService(ServerLevel level,Object member) throws ReflectiveOperationException {
    return moving&&(boolean)call("movingService",new Class<?>[]{ServerLevel.class,UUID.class},level,field(member,"chassis"));
  }

  /** The equipment owner declares direction, while FD alone decides whether that route is reserved. */
  private int routeDirection(ServerLevel level, BlockPos pos, Object member) {
    try {
      if (!routes || pos == null || !(RideControllers.loadedBlockEntity(level, pos) instanceof RideControllerEntity bound)
          || !RideControllers.available(level, bound) || bound.swingProgram()
          || dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.findSubLevelAt(level, pos) != null
          || !bound.reservation().contains(field(member, "chassis"))) return 0;
      var spans = bound.claimedSpans();
      if (spans.isEmpty()) return 0;
      int revision = dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.get(level).revision();
      long handles = dev.silvergold.simulatedcoasters.track.CoasterPeerCurveHandleLengths.revision();
      var cache = routeGeometry.computeIfAbsent(level, ignored -> new HashMap<>());
      var geometry = cache.get(bound.identity());
      if (geometry == null || geometry.revision() != revision || geometry.handles() != handles || !geometry.spans().equals(spans)) {
        var chunks = new HashSet<BlockPos>();
        for (var span : spans) {
          var track = dev.coasterslaunchsystems.track.DirectedTrack.resolve(level, span.from(), span.to()).orElse(null);
          if (track == null || track.fingerprint() != span.geometry()) return 0;
          chunks.add(span.from()); chunks.add(span.to());
          for (double s = 0; s <= track.length(); s += 1) {
            var point = BlockPos.containing(track.hitAtDistance(level, s).point());
            chunks.add(new BlockPos(Math.floorDiv(point.getX(), 16) * 16, point.getY(), Math.floorDiv(point.getZ(), 16) * 16));
          }
        }
        geometry = new RouteGeometry(List.copyOf(spans), revision, handles, Set.copyOf(chunks));
        cache.put(bound.identity(), geometry);
      }
      if (geometry.chunks().stream().anyMatch(chunk -> !level.hasChunkAt(chunk))) return 0;
      var dataType = Class.forName("dev.fourthdimension.runtime.FoundationData");
      var data = dataType.getMethod("get", ServerLevel.class).invoke(null, level);
      var a = (BlockPos) field(member, "boundFrom"); var b = (BlockPos) field(member, "boundTo");
      for (var rail : (List<?>) field(data, "rails")) {
        var from = (BlockPos) field(rail, "from"); var to = (BlockPos) field(rail, "to");
        if (!(from.equals(a) && to.equals(b) || from.equals(b) && to.equals(a))) continue;
        for (var span : spans) if (span.sameRail(from, to)) return span.from().equals(from) ? 1 : -1;
      }
      return 0;
    } catch (ReflectiveOperationException | IllegalArgumentException | IllegalStateException unavailable) { return 0; }
  }

  private RideControllerEntity operatorController(ServerLevel level, Object member) throws ReflectiveOperationException {
    var state = field(member, "authority");
    if (!PROVIDER.equals(field(state, "provider")) || !(field(state, "controller") instanceof BlockPos pos)
        || !(RideControllers.loadedBlockEntity(level, pos) instanceof RideControllerEntity bound)
        || !RideControllers.available(level, bound) || !bound.identity().equals(field(state, "owner"))
        || !bound.reservation().contains(field(member, "chassis")))
      throw new IllegalArgumentException("Launch Systems operator owner is unavailable or replaced");
    return bound;
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

  private boolean accessoryPowerAvailable(ServerLevel level, Object member) throws ReflectiveOperationException {
    var state = field(member, "authority");
    if (!PROVIDER.equals(field(state, "provider")) || !(field(state, "controller") instanceof BlockPos pos)
        || !(RideControllers.loadedBlockEntity(level, pos) instanceof RideControllerEntity bound)
        || !bound.identity().equals(field(state, "owner")) || !RideControllers.available(level, bound)
        || !bound.armed() || !bound.fault().isEmpty() || !bound.reservation().contains(field(member, "chassis"))) return false;
    for (var link : bound.modules()) {
      if (RideControllers.loadedBlockEntity(level, link.position()) instanceof EquipmentCabinetEntity module
          && link.identity().equals(module.identity()) && EquipmentRuntime.available(level, module)
          && RideControllers.owner(level, module) == bound && module.armed() && module.fault().isEmpty()
          && !module.isOverStressed() && Math.abs(module.getSpeed()) >= 1 && Math.abs(module.getSpeed()) <= 128) return true;
    }
    return false;
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
      var certificate = FourthDimensionCertificates.find(((Enum<?>) field(v, "family")).name(), (String) field(v, "variant"));
      return certificate != null && field(v, "recoveryCart") == null
          && !(boolean) field(v, "alignmentRequired")
          && (!managed(level, v) || routes);
    } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Fourth Dimension eligibility unavailable", failure); }
  }

  private boolean managed(ServerLevel level, Object v) throws ReflectiveOperationException {
    var operations = Class.forName("dev.fourthdimension.integration.RideLocalOperations");
    // Retained admission is still managed service after route configuration changes.
    return routes ? (boolean) operations.getMethod("hasRouteInterest", ServerLevel.class, UUID.class)
        .invoke(null, level, (UUID) field(v, "chassis"))
        : (boolean) operations.getMethod("isManaged", ServerLevel.class, vehicle).invoke(null, level, v);
  }

  boolean denied(ServerLevel level, Object v) throws ReflectiveOperationException {
    var state = field(v, "authority");
    if (field(state, "owner") == null) return false;
    call("holds", new Class<?>[] {ServerLevel.class, vehicle}, level, v); // Refresh lost/restored ownership first.
    return !PROVIDER.equals(field(state, "provider")) || (boolean) field(state, "suspended")
        || !Objects.equals(token(v), tokens.getOrDefault(level, Map.of()).get((UUID) field(v, "chassis")));
  }

  /** Native authority owns settling time; local READY must not get ahead of its release guard. */
  boolean dispatchReady(ServerLevel level, Object v) throws ReflectiveOperationException {
    if (!routes || !managed(level, v) || field(field(v, "authority"), "owner") == null) return true;
    var id = (UUID) field(v, "chassis"); var expected = tokens.getOrDefault(level, Map.of()).get(id);
    return expected != null && (boolean) call("dispatchReady", new Class<?>[] {ServerLevel.class, UUID.class, tokenType},
        level, id, expected);
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

  boolean retainedRouteService(ServerLevel level, Object v, Set<UUID> consist) throws ReflectiveOperationException {
    return routes && managed(level, v) && retainsBrakeLease(level, v, consist);
  }

  void requestResume(ServerLevel level, BlockPos pos) {
    resumes.computeIfAbsent(level, ignored -> new HashSet<>()).add(pos.immutable());
    movingAcknowledgements.computeIfAbsent(level,ignored->new HashMap<>()).put(pos.immutable(),level.getGameTime());
  }

  boolean reserve(ServerLevel level, UUID id, BlockPos pos) {
    try {
      var v = find(level, id);
      if (v == null || !supported(level, v) || !(RideControllers.loadedBlockEntity(level, pos) instanceof RideControllerEntity bound)
          || !bound.reservation().contains(id)) return false;
      var state = field(v, "authority");
      if (field(state, "owner") == null) {
        var pending = resumes.get(level); if (pending != null) pending.remove(pos);
        if (managed(level, v)) call("requestRoute", new Class<?>[] {ServerLevel.class, BlockPos.class, UUID.class, String.class},
            level, pos, id, PROVIDER);
        remember(level, v, call("claim", new Class<?>[] {ServerLevel.class, BlockPos.class, UUID.class, String.class}, level, pos, id, PROVIDER));
        return true;
      }
      // A fresh receiving-controller arm may accept a released native moving bridge.
      // FD validates the exact old token, whole consist, bridge locks and receiving reservation.
      if(movingService(level,v)&&PROVIDER.equals(field(state,"provider"))){
        var pending=movingAcknowledgements.getOrDefault(level,Map.of()).get(pos);
        boolean different=!pos.equals(field(state,"controller"))||!bound.identity().equals(field(state,"owner"));
        // Same controller can also return after a round trip to its original dock.
        var dataType=Class.forName("dev.fourthdimension.moving.MovingServiceRuntime");
        var assembly=dataType.getMethod("owner",ServerLevel.class,vehicle).invoke(null,level,v);
        boolean unreturned=assembly!=null&&field(field(assembly,"service"),"externalOwner")==null;
        if(different||unreturned){
          if(pending==null||level.getGameTime()-pending>5)return false;
          movingAcknowledgements.get(level).remove(pos);
          var oldPos=(BlockPos)field(state,"controller");var oldOwner=(UUID)field(state,"owner");
          var oldCars=new HashSet<>((List<UUID>)field(state,"cars"));
          var returned=call("returnFromMoving",new Class<?>[]{ServerLevel.class,UUID.class,tokenType,BlockPos.class},level,id,token(v),pos);
          remember(level,v,returned);resumes.getOrDefault(level,new HashSet<>()).remove(pos);
          if(!oldPos.equals(pos)&&RideControllers.loadedBlockEntity(level,oldPos) instanceof RideControllerEntity source
              &&source.identity().equals(oldOwner)&&source.reservation().equals(oldCars)){
            source.clearVerifiedReservation();source.setArmed(false);
          }
          return true;
        }
      }
      if (!PROVIDER.equals(field(state, "provider")) || !pos.equals(field(state, "controller"))
          || !bound.identity().equals(field(state, "owner"))) return false;
      call("holds", new Class<?>[] {ServerLevel.class, vehicle}, level, v);
      var pending = resumes.get(level);
      if (pending != null && pending.remove(pos)) {
        if (managed(level, v)) call("resumeRoute", new Class<?>[] {ServerLevel.class, BlockPos.class, UUID.class, tokenType},
            level, pos, id, token(v));
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
    if (field(field(v, "authority"), "owner") == null) return false;
    var cars = new HashSet<UUID>();
    for (var car : (List<?>) field(field(v, "authority"), "cars")) cars.add((UUID) car);
    boolean blocked = denied(level, v), retained = blocked && retainedRouteService(level, v, cars);
    if (blocked && !retained) return false;
    var id = (UUID) field(v, "chassis"); var pos = (BlockPos) field(field(v, "authority"), "controller");
    try {
      var expected = retained ? token(v) : tokens.getOrDefault(level, Map.of()).get(id);
      remember(level, v, call("capture", new Class<?>[] {ServerLevel.class, BlockPos.class, UUID.class, tokenType}, level, pos, id, expected));
      return true;
    } catch (IllegalArgumentException rejected) { return false; }
  }

  boolean transfer(ServerLevel level, UUID id, BlockPos source, BlockPos target) {
    try {
      var v=find(level,id);
      if(v==null || !source.equals(field(field(v,"authority"),"controller"))) return false;
      var cars=new HashSet<UUID>();
      for(var car:(List<?>)field(field(v,"authority"),"cars"))cars.add((UUID)car);
      if(denied(level,v) && !retainedRouteService(level,v,cars)) return false;
      // This neutral operation proves every body is stopped under its native hold before capture.
      if(!dev.coasterslaunchsystems.api.MotionProviders.requestCapture(level,id,cars))return false;
      // A managed capture rotates into a suspended hold. Handoff may adopt that current token
      // only after the same exact lease and complete stopped capture proof; it never resumes.
      var expected=retainedRouteService(level,v,cars)?token(v):tokens.getOrDefault(level,Map.of()).get(id);
      remember(level,v,call("transfer",new Class<?>[]{ServerLevel.class,BlockPos.class,UUID.class,tokenType,BlockPos.class},
          level,source,id,expected,target));
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
