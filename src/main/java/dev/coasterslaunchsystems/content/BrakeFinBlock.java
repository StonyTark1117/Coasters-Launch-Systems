package dev.coasterslaunchsystems.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Install at the bearing's local east/below cell; the fin runs along the chassis local Z axis. */
public final class BrakeFinBlock extends Block {
  private static final VoxelShape SHAPE = Shapes.or(box(7, 0, 0, 9, 16, 16), box(3, 14, 1, 13, 16, 15));
  public BrakeFinBlock(Properties properties) { super(properties); }
  @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
}
