package dev.coasterslaunchsystems.content;

import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public final class SpanToolItem extends Item {
  public SpanToolItem(Properties properties) { super(properties); }
  @Override public InteractionResult useOn(UseOnContext context) {
    if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
    var player = context.getPlayer(); var pos = context.getClickedPos(); var stack = context.getItemInHand();
    if (player == null || !player.mayBuild() || !level.mayInteract(player, pos)
        || player.distanceToSqr(pos.getCenter()) > 64) return InteractionResult.FAIL;
    try {
      if (player.isShiftKeyDown()) {
        var selectedController = stack.get(EquipmentContent.CONTROLLER_SELECTION.get());
        if (level.getBlockEntity(pos) instanceof EquipmentCabinetEntity cabinet && selectedController != null) {
          if (!selectedController.dimension().equals(level.dimension()) || !level.hasChunkAt(selectedController.pos())
              || !level.mayInteract(player, selectedController.pos()) || !(level.getBlockEntity(selectedController.pos()) instanceof RideControllerEntity controller))
            throw new IllegalArgumentException("Selected controller is unavailable or protected");
          controller.unlink(cabinet); player.displayClientMessage(Component.literal("Module unlinked from the empty controller."), true);
          return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof RideControllerEntity controller) {
          stack.set(EquipmentContent.CONTROLLER_SELECTION.get(), GlobalPos.of(level.dimension(), pos));
          stack.remove(EquipmentContent.FROM.get()); stack.remove(EquipmentContent.TO.get());
          player.displayClientMessage(Component.literal("Controller selected. Use the tool on its disarmed, bound modules to link them."), true);
          return InteractionResult.SUCCESS;
        }
        stack.remove(EquipmentContent.CONTROLLER_SELECTION.get());
        stack.remove(EquipmentContent.FROM.get()); stack.remove(EquipmentContent.TO.get());
        player.displayClientMessage(Component.literal("Span selection cleared."), true);
        return InteractionResult.SUCCESS;
      }
      var from = stack.get(EquipmentContent.FROM.get()); var to = stack.get(EquipmentContent.TO.get());
      var selected = stack.get(EquipmentContent.CONTROLLER_SELECTION.get());
      if (level.getBlockEntity(pos) instanceof RideControllerEntity controller) {
        if (from == null || to == null || !from.dimension().equals(level.dimension()) || !to.dimension().equals(level.dimension()))
          throw new IllegalArgumentException("Select two section anchors first; shift-use selects a bound controller for module linking");
        if (!level.mayInteract(player, from.pos()) || !level.mayInteract(player, to.pos())) throw new IllegalArgumentException("Track endpoints are protected");
        controller.bind(level, from.pos(), to.pos());
        stack.remove(EquipmentContent.FROM.get()); stack.remove(EquipmentContent.TO.get());
        player.displayClientMessage(Component.literal(controller.status()), false);
      } else if (level.getBlockEntity(pos) instanceof EquipmentCabinetEntity cabinet && selected != null) {
        if (!selected.dimension().equals(level.dimension()) || !level.hasChunkAt(selected.pos())
            || !level.mayInteract(player, selected.pos()) || !(level.getBlockEntity(selected.pos()) instanceof RideControllerEntity controller))
          throw new IllegalArgumentException("Selected controller is unavailable or protected");
        controller.link(cabinet); player.displayClientMessage(Component.literal("Module linked to selected controller."), true);
      } else if (level.getBlockEntity(pos) instanceof EquipmentCabinetEntity cabinet) {
        if (from == null || to == null || !from.dimension().equals(level.dimension()) || !to.dimension().equals(level.dimension()))
          throw new IllegalArgumentException("Select two track anchors in this dimension first");
        if (!level.mayInteract(player, from.pos()) || !level.mayInteract(player, to.pos()))
          throw new IllegalArgumentException("Track endpoints are protected");
        cabinet.bind(level, from.pos(), to.pos());
        stack.remove(EquipmentContent.FROM.get()); stack.remove(EquipmentContent.TO.get());
        player.displayClientMessage(Component.literal(cabinet.status()), false);
      } else if (level.getBlockEntity(pos) instanceof CoasterAnchorpointBlockEntity) {
        stack.remove(EquipmentContent.CONTROLLER_SELECTION.get());
        if (from == null || to != null || !from.dimension().equals(level.dimension())) {
          stack.set(EquipmentContent.FROM.get(), GlobalPos.of(level.dimension(), pos)); stack.remove(EquipmentContent.TO.get());
          player.displayClientMessage(Component.literal("Start selected. Select the other anchor; their order sets drive direction."), true);
        } else {
          if (from.pos().equals(pos)) throw new IllegalArgumentException("Select a different end anchor");
          stack.set(EquipmentContent.TO.get(), GlobalPos.of(level.dimension(), pos));
          player.displayClientMessage(Component.literal("End selected. Use the tool on a disarmed nearby module."), true);
        }
      } else throw new IllegalArgumentException("Use this tool on coaster anchors or launch/brake modules");
      return InteractionResult.SUCCESS;
    } catch (IllegalArgumentException failure) {
      player.displayClientMessage(Component.literal(failure.getMessage()), true);
      return InteractionResult.FAIL;
    }
  }
}
