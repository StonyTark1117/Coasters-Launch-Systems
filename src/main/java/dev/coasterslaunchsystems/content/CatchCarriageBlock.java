package dev.coasterslaunchsystems.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Lower carriage jaw leaves clearance below the socket's upper jaw. */
public final class CatchCarriageBlock extends Block {
  private static final VoxelShape SHAPE = box(2, 0, 2, 14, 6, 14);
  public CatchCarriageBlock(Properties properties) { super(properties); }
  @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
}
