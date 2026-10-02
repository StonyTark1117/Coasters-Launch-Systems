package dev.coasterslaunchsystems.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Install at the bearing's local north/below cell; this sacrificial steel shoe is separate from a fin. */
public final class SkidShoeBlock extends Block {
  private static final VoxelShape SHAPE = Shapes.or(box(1, 0, 5, 15, 3, 11), box(3, 3, 6, 13, 5, 10));
  public SkidShoeBlock(Properties properties) { super(properties); }
  @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
}
