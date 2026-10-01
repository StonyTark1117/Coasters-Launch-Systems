package dev.coasterslaunchsystems.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Low bed carrying the carriage's constrained guide below its steel ballast. */
public final class CableGuideBlock extends Block {
  private static final VoxelShape SHAPE = box(0, 0, 0, 16, 3, 16);
  public CableGuideBlock(Properties properties) { super(properties); }
  @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
}
