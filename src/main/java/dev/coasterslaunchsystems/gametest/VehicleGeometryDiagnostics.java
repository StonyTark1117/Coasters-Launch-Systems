package dev.coasterslaunchsystems.gametest;

import com.google.gson.Gson;
import dev.coasterslaunchsystems.LaunchSystems;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import dev.coasterslaunchsystems.runtime.RideControllers;
import dev.coasterslaunchsystems.runtime.VehicleInterfaces;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Opt-in, read-only diagnosis. No forces, authority operations, loading tickets or world writes. */
@EventBusSubscriber(modid = LaunchSystems.ID)
public final class VehicleGeometryDiagnostics {
  private static final Gson JSON = new Gson();
  private VehicleGeometryDiagnostics() {}

  @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
    if (!Boolean.getBoolean("coasterslaunchsystems.validation")) return;
    event.getDispatcher().register(Commands.literal("lsdiagnose").requires(s -> s.hasPermission(2))
        .then(Commands.literal("geometry").then(Commands.argument("chassis", UuidArgument.uuid())
        .then(Commands.argument("source", BlockPosArgument.blockPos()).executes(ctx -> {
          try {
            var report = inspect(ctx.getSource().getLevel(), UuidArgument.getUuid(ctx, "chassis"),
                BlockPosArgument.getLoadedBlockPos(ctx, "source"));
            for (var line : report) ctx.getSource().sendSuccess(() -> Component.literal(JSON.toJson(line)), false);
            return report.size();
          } catch (ReflectiveOperationException | IllegalArgumentException | IllegalStateException failure) {
            ctx.getSource().sendFailure(Component.literal("Geometry diagnosis unavailable: " + failure));
            return 0;
          }
        })))));
  }

  private static Object field(Object object, String name) throws ReflectiveOperationException {
    return object.getClass().getField(name).get(object);
  }
  private static List<Map<String, Object>> inspect(ServerLevel level, UUID chassis, BlockPos sourcePos)
      throws ReflectiveOperationException {
    var foundation = Class.forName("dev.fourthdimension.runtime.FoundationData");
    var data = foundation.getMethod("get", ServerLevel.class).invoke(null, level);
    var vehicle = ((Map<?, ?>) field(data, "vehicles")).get(chassis);
    if (vehicle == null) throw new IllegalArgumentException("No registered native chassis " + chassis);
    var ids = new LinkedHashSet<UUID>(); ids.add(chassis);
    for (var carrier : (List<?>) field(vehicle, "carriers")) ids.add((UUID) field(carrier, "body"));
    var container = SubLevelContainer.getContainer(level); var pipeline = container.physicsSystem().getPipeline();
    var result = new ArrayList<Map<String, Object>>();
    result.add(Map.of("kind", "header", "tick", level.getGameTime(), "chassis", chassis,
        "family", field(vehicle, "family").toString(), "variant", field(vehicle, "variant"),
        "note", "Current static-world intersections, not swept or body/body contacts; unloaded cells reported, never loaded"));
    for (var id : ids) {
      if (!(container.getSubLevel(id) instanceof ServerSubLevel body)) throw new IllegalArgumentException("Missing body " + id);
      var pose = body.logicalPose(); var bounds = body.getPlot().getBoundingBox();
      long cells = (long) (bounds.maxX() - bounds.minX() + 1) * (bounds.maxY() - bounds.minY() + 1) * (bounds.maxZ() - bounds.minZ() + 1);
      if (cells > 4096 || cells < 0) throw new IllegalArgumentException("Diagnostic body scan exceeds 4096 cells");
      var bodyReport = new LinkedHashMap<String, Object>();
      bodyReport.put("kind", "body"); bodyReport.put("id", id); bodyReport.put("position", vec(pose.position()));
      var q = pose.orientation(); bodyReport.put("orientation", new double[] {q.x(), q.y(), q.z(), q.w()});
      bodyReport.put("rotationPoint", vec(pose.rotationPoint())); bodyReport.put("centerOfMass", vec(body.getMassTracker().getCenterOfMass()));
      bodyReport.put("mass", body.getMassTracker().getMass()); bodyReport.put("linearVelocity", vec(pipeline.getLinearVelocity(body, new Vector3d())));
      bodyReport.put("angularVelocity", vec(pipeline.getAngularVelocity(body, new Vector3d())));
      var boxes = new ArrayList<Map<String, Object>>(); var collisions = new ArrayList<Map<String, Object>>();
      var unloaded = new LinkedHashSet<String>();
      for (var pos : BlockPos.betweenClosed(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
        var state = level.getBlockState(pos);
        for (var local : state.getCollisionShape(level, pos).toAabbs()) {
          var points = corners(local.move(pos));
          for (var point : points) pose.transformPosition(point);
          var aabb = enclosing(points);
          boxes.add(Map.of("block", pos.toShortString(), "type", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(),
              "localBox", box(local), "worldCorners", Arrays.stream(points).map(VehicleGeometryDiagnostics::vec).toList(), "worldAabb", box(aabb)));
          if (aabb.getSize() > 32) throw new IllegalArgumentException("Diagnostic shape exceeds supported size");
          for (var world : BlockPos.betweenClosed(BlockPos.containing(aabb.minX - 1, aabb.minY - 1, aabb.minZ - 1), BlockPos.containing(aabb.maxX + 1, aabb.maxY + 1, aabb.maxZ + 1))) {
            if (!level.hasChunkAt(world)) { unloaded.add(world.toShortString()); continue; }
            var fixed = level.getBlockState(world);
            for (var shape : fixed.getCollisionShape(level, world).toAabbs()) {
              var fixedBox = shape.move(world);
              if (overlap(points, fixedBox)) collisions.add(Map.of("movingBlock", pos.toShortString(),
                  "movingType", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(), "worldBlock", world.toShortString(),
                  "worldType", BuiltInRegistries.BLOCK.getKey(fixed.getBlock()).toString(), "worldShape", box(fixedBox)));
            }
          }
        }
      }
      bodyReport.put("boxes", boxes); bodyReport.put("intersections", collisions); bodyReport.put("unloaded", unloaded); result.add(bodyReport);
    }
    if (level.getBlockEntity(sourcePos) instanceof RideControllerEntity source && source.destination() != null) {
      var target = RideControllers.controller(level, source.destination());
      if (target != null && container.getSubLevel(chassis) instanceof ServerSubLevel body) {
        // Peek the observed contact without current(), whose stale-entry cleanup mutates the cache.
        var contactsField = EngagedContacts.class.getDeclaredField("CONTACTS"); contactsField.setAccessible(true);
        var worlds = (Map<?, ?>) contactsField.get(null); var contacts = (Map<?, ?>) worlds.get(level);
        var contact = contacts == null ? null : (EngagedContacts.Contact) contacts.get(chassis);
        var row = new LinkedHashMap<String, Object>(); row.put("kind", "motion"); row.put("sourcePhase", source.phase().name());
        row.put("contactPresent", contact != null);
        if (contact != null) {
          row.put("contactAgeTicks", level.getGameTime() - contact.tick()); row.put("contactT", contact.hit().t());
          row.put("contactPoint", List.of(contact.hit().point().x, contact.hit().point().y, contact.hit().point().z));
          var route = dev.coasterslaunchsystems.track.DirectedRoute.between(level, source.from(), source.to(), target.from(), target.to()); var axis = route.forward(level, contact.hit());
          var direction = new Vector3d(axis.x, axis.y, axis.z); var linear = pipeline.getLinearVelocity(body, new Vector3d());
          var tire = VehicleInterfaces.tirePoint(body);
          row.put("routeDistance", route.distance(contact.hit())); row.put("direction", vec(direction));
          row.put("translationProjection", linear.dot(direction));
          if (tire != null) {
            var arm = new Vector3d(tire).sub(body.getMassTracker().getCenterOfMass()).rotate(body.logicalPose().orientation());
            var angular = pipeline.getAngularVelocity(body, new Vector3d()).cross(arm);
            row.put("tirePoint", vec(tire)); row.put("worldArm", vec(arm)); row.put("angularPointProjection", angular.dot(direction));
            row.put("pointProjection", angular.add(linear).dot(direction));
          }
        }
        result.add(row);
      }
    }
    return result;
  }

  private static double[] vec(Vector3dc v) { return new double[] {v.x(), v.y(), v.z()}; }
  private static double[] box(AABB b) { return new double[] {b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ}; }
  private static Vector3d[] corners(AABB b) {
    var out = new Vector3d[8];
    for (int i = 0; i < 8; i++) out[i] = new Vector3d((i & 1) == 0 ? b.minX : b.maxX,
        (i & 2) == 0 ? b.minY : b.maxY, (i & 4) == 0 ? b.minZ : b.maxZ);
    return out;
  }
  private static AABB enclosing(Vector3d[] p) {
    var lo = new Vector3d(Double.POSITIVE_INFINITY); var hi = new Vector3d(Double.NEGATIVE_INFINITY);
    for (var v : p) { lo.min(v); hi.max(v); }
    return new AABB(lo.x, lo.y, lo.z, hi.x, hi.y, hi.z);
  }
  /** Separating-axis test for one transformed box and a static world box; mere touching is excluded. */
  private static boolean overlap(Vector3d[] moving, AABB fixed) {
    var axes = new ArrayList<Vector3d>();
    var worldAxes = List.of(new Vector3d(1, 0, 0), new Vector3d(0, 1, 0), new Vector3d(0, 0, 1));
    axes.addAll(worldAxes);
    for (int i : new int[] {1, 2, 4}) {
      var edge = new Vector3d(moving[i]).sub(moving[0]);
      if (edge.lengthSquared() < 1e-16) return false;
      edge.normalize(); axes.add(edge);
      for (var world : worldAxes) axes.add(new Vector3d(edge).cross(world));
    }
    var fixedPoints = corners(fixed);
    for (var axis : axes) {
      if (axis.lengthSquared() < 1e-16) continue;
      axis.normalize(); double minA = Double.POSITIVE_INFINITY, maxA = Double.NEGATIVE_INFINITY;
      double minB = Double.POSITIVE_INFINITY, maxB = Double.NEGATIVE_INFINITY;
      for (var p : moving) { double x = p.dot(axis); minA = Math.min(minA, x); maxA = Math.max(maxA, x); }
      for (var p : fixedPoints) { double x = p.dot(axis); minB = Math.min(minB, x); maxB = Math.max(maxB, x); }
      if (Math.min(maxA, maxB) - Math.max(minA, minB) <= 1e-5) return false;
    }
    return true;
  }
}
