package com.lycanitesmobs.core.block;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.tileentity.TileEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 ITileEntityProvider (createNewTileEntity) on 1.21 EntityBlock. The tile entity class names the BlockEntityType
 * ObjectManager registered for it; 1.12 ITickable.update runs every tick on both sides like 1.12.
 */
public interface ITileEntityBlock extends EntityBlock {
   Class<? extends TileEntityBase> getTileEntityClass();

   @Override
   default BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      BlockEntityType<?> type = ObjectManager.getTileEntityType(this.getTileEntityClass());
      return type != null ? type.create(pos, state) : null;
   }

   @Override
   default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return (world, pos, blockState, blockEntity) -> {
         if (blockEntity instanceof TileEntityBase tileEntity) {
            tileEntity.update();
         }
      };
   }
}
