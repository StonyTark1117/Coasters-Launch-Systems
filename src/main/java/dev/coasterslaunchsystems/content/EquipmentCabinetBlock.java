package dev.coasterslaunchsystems.content;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.coasterslaunchsystems.math.Mechanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class EquipmentCabinetBlock extends RotatedPillarKineticBlock implements IBE<EquipmentCabinetEntity> {
  public final Mechanism mechanism;
  public EquipmentCabinetBlock(Properties properties, Mechanism mechanism) {
    super(properties); this.mechanism = mechanism;
  }
  @Override public Direction.Axis getRotationAxis(BlockState state) { return state.getValue(AXIS); }
  @Override public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
    return face.getAxis() == state.getValue(AXIS);
  }
  @Override public Class<EquipmentCabinetEntity> getBlockEntityClass() { return EquipmentCabinetEntity.class; }
  @Override public BlockEntityType<? extends EquipmentCabinetEntity> getBlockEntityType() { return EquipmentContent.ENTITY.get(); }
  @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (level.isClientSide) return InteractionResult.SUCCESS;
    if (!player.mayBuild() || !level.mayInteract(player, pos)) return InteractionResult.FAIL;
    var cabinet = getBlockEntity(level, pos);
    if (cabinet == null) return InteractionResult.FAIL;
    try {
      if (player.isShiftKeyDown()) cabinet.setArmed(!cabinet.armed());
      player.displayClientMessage(Component.literal(cabinet.status()), false);
      return InteractionResult.SUCCESS;
    } catch (IllegalArgumentException failure) {
      player.displayClientMessage(Component.literal(failure.getMessage()), true);
      return InteractionResult.FAIL;
    }
  }
}
