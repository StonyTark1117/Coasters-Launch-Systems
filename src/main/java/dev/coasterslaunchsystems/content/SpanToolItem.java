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
        stack.remove(EquipmentContent.FROM.get()); stack.remove(EquipmentContent.TO.get());
        player.displayClientMessage(Component.literal("Span selection cleared."), true);
        return InteractionResult.SUCCESS;
      }
      var from = stack.get(EquipmentContent.FROM.get()); var to = stack.get(EquipmentContent.TO.get());
      if (level.getBlockEntity(pos) instanceof EquipmentCabinetEntity cabinet) {
        if (from == null || to == null || !from.dimension().equals(level.dimension()) || !to.dimension().equals(level.dimension()))
          throw new IllegalArgumentException("Select two track anchors in this dimension first");
        if (!level.mayInteract(player, from.pos()) || !level.mayInteract(player, to.pos()))
          throw new IllegalArgumentException("Track endpoints are protected");
        cabinet.bind(level, from.pos(), to.pos());
        stack.remove(EquipmentContent.FROM.get()); stack.remove(EquipmentContent.TO.get());
        player.displayClientMessage(Component.literal(cabinet.status()), false);
      } else if (level.getBlockEntity(pos) instanceof CoasterAnchorpointBlockEntity) {
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
