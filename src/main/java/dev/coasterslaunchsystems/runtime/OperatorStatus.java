package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import dev.coasterslaunchsystems.integration.FourthDimensionRouteStatus;
import dev.coasterslaunchsystems.math.LinearMotor;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

/** The existing native panel and local block controls sample this same server-side state. */
public final class OperatorStatus {
  private OperatorStatus() {}

  public static CompoundTag sample(ServerLevel level, RideControllerEntity controller, UUID chassis) {
    var result = new CompoundTag();
    result.putUUID("owner", controller.identity());
    result.putString("provider", "coasterslaunchsystems:local_controller");
    result.putLong("controller_position", controller.getBlockPos().asLong());
    result.putString("phase", controller.phase().name());
    result.putLong("request_revision", controller.requestRevision());
    result.putBoolean("request_pending", controller.requestPending());
    result.putBoolean("fresh_request_required", !controller.requestPending());
    result.putString("request", controller.requestPending() ? "One fresh request pending"
        : controller.requestInputHigh() ? "Fresh panel request required; lower lever before another rising edge"
        : "Fresh panel request or new rising edge required");
    String reason = controller.blockedReason();
    if (!RideControllers.available(level, controller)) reason = "Bound controller is unavailable";
    else if (chassis != null && !controller.reservation().contains(chassis)) reason = "Controller does not reserve this train";
    double stored = 0, capacity = 0;
    int finite = 0;
    for (var link : controller.modules()) {
      if (!(RideControllers.loadedBlockEntity(level, link.position()) instanceof EquipmentCabinetEntity module)
          || !link.identity().equals(module.identity()) || RideControllers.owner(level, module) != controller) {
        reason = "Linked equipment identity or owner is unavailable"; continue;
      }
      if (module.mechanism().linear()) { stored += module.energy(); capacity += LinearMotor.CAPACITY; finite++; }
      else if (module.mechanism().cable()) {
        stored += module.cableEnergy(); capacity += switch (module.mechanism()) {
          case FLYWHEEL -> module.flywheel().capacity();
          case HYDRAULIC -> module.hydraulic().capacity();
          case PNEUMATIC -> module.pneumatic().capacity();
          default -> 0;
        }; finite++;
      }
    }
    result.putDouble("charge_stored", stored); result.putDouble("charge_capacity", capacity);
    result.putString("charge", finite == 0 ? "Continuous shaft supply; no finite store"
        : String.format(Locale.ROOT, "%.0f / %.0f work in %d stores", stored, capacity, finite));
    if (chassis != null) {
      var route = FourthDimensionRouteStatus.query(level, chassis);
      result.putBoolean("route_managed", route.managed());
      result.putInt("route_block", route.currentBlock()); result.putInt("route_next_block", route.nextBlock());
      if (route.owner() != null) result.putUUID("route_owner", route.owner());
      result.putString("route_denial", route.reason());
      if (route.managed() && !route.permitted()) reason = route.reason();
    }
    result.putString("blocked_reason", reason);
    result.putBoolean("dispatch_ready", reason.isEmpty() && controller.armed() && !controller.requestPending()
        && (controller.phase() == RideControllerEntity.State.READY || controller.phase() == RideControllerEntity.State.READY_EXIT));
    return result;
  }

  /** Native permission/range/owner checks run before this callback; local freshness remains authoritative. */
  public static void action(ServerLevel level, RideControllerEntity controller, UUID chassis, int action, long revision) {
    if (!RideControllers.available(level, controller) || !controller.reservation().contains(chassis))
      throw new IllegalArgumentException("Controller no longer reserves this train");
    controller.checkRequestRevision(revision);
    if (action == 13) {
      var state = sample(level, controller, chassis);
      if (!state.getBoolean("dispatch_ready")) throw new IllegalArgumentException(state.getString("blocked_reason").isBlank()
          ? "A request is already pending" : state.getString("blocked_reason"));
      controller.requestDispatch(revision);
    } else if (action == 61) controller.setArmed(true);
    else if (action == 10) controller.setArmed(false);
    else throw new IllegalArgumentException("Unsupported Launch Systems operator action");
  }

  public static String summary(ServerLevel level, RideControllerEntity controller) {
    UUID member = controller.reservation().stream().sorted().findFirst().orElse(null);
    var state = sample(level, controller, member);
    return "Owner " + state.getUUID("owner") + "; " + state.getString("phase") + "; "
        + (state.getString("blocked_reason").isEmpty() ? "ready" : state.getString("blocked_reason"))
        + "; " + state.getString("charge") + "; " + state.getString("request");
  }
}
