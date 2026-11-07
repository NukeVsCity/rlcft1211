package com.nukevscity.rlgen.nether;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Quark BlockSmoker: netherrack with a vent on top; its TileSmoker puffs smoke on the client. */
public class SmokerBlock extends Block implements EntityBlock {
   public SmokerBlock(Properties p) {
      super(p);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new SmokerBlockEntity(pos, state);
   }

   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide && type == NetherGen.SMOKER_BE.get() ? (l, p, s, be) -> SmokerBlockEntity.clientTick(l, p) : null;
   }
}
