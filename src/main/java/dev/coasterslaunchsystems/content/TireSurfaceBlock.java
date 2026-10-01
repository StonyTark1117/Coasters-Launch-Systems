package dev.coasterslaunchsystems.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Thin chassis drive strip; install directly beneath the representative native cart bearing. */
public final class TireSurfaceBlock extends Block {
  private static final VoxelShape SHAPE = box(1, 14, 1, 15, 16, 15);
  public TireSurfaceBlock(Properties properties) { super(properties); }
  @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }
}
