package dev.coasterslaunchsystems.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.mojang.serialization.MapCodec;

public final class RideControllerBlock extends BaseEntityBlock {
  public static final MapCodec<RideControllerBlock> CODEC = simpleCodec(RideControllerBlock::new);
  public RideControllerBlock(Properties properties) { super(properties); }
  @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
  @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
  @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new RideControllerEntity(pos, state); }
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    return level.isClientSide ? null : createTickerHelper(type, EquipmentContent.CONTROLLER_ENTITY.get(),
        (world, pos, block, controller) -> controller.tick());
  }
  @Override protected boolean isSignalSource(BlockState state) { return true; }
  @Override protected int getSignal(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, Direction face) {
    return level.getBlockEntity(pos) instanceof RideControllerEntity controller ? controller.signal(face) : 0;
  }
  /** Allows the Span Tool to bind this controller before the status interaction consumes the click. */
  @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
      Player player, InteractionHand hand, BlockHitResult hit) {
    return stack.getItem() instanceof SpanToolItem
        ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
        : super.useItemOn(stack, state, level, pos, player, hand, hit);
  }
  @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (level.isClientSide) return InteractionResult.SUCCESS;
    if (!player.mayBuild() || !level.mayInteract(player, pos)) return InteractionResult.FAIL;
    if (!(level.getBlockEntity(pos) instanceof RideControllerEntity controller)) return InteractionResult.FAIL;
    try {
      if (player.isShiftKeyDown()) controller.setArmed(!controller.armed());
      player.displayClientMessage(Component.literal(controller.status()), false);
      return InteractionResult.SUCCESS;
    } catch (IllegalArgumentException failure) {
      player.displayClientMessage(Component.literal(failure.getMessage()), true); return InteractionResult.FAIL;
    }
  }
}
