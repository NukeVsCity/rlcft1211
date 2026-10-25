package com.nukevscity.rlgen.charm;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Charm's crate (9 slots); a sealed crate can't be opened, it spills its contents when broken. */
public class CrateBlock extends BaseEntityBlock {
   private final boolean sealed;

   public CrateBlock(boolean sealed, Properties properties) {
      super(properties);
      this.sealed = sealed;
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return simpleCodec(p -> new CrateBlock(this.sealed, p));
   }

   @Override
   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CrateBlockEntity(pos, state);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (this.sealed) {
         return InteractionResult.PASS;
      }
      if (!level.isClientSide && level.getBlockEntity(pos) instanceof CrateBlockEntity crate) {
         player.openMenu(crate);
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   @Override
   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CrateBlockEntity crate) {
         crate.unpackLootTable(null);
         Containers.dropContents(level, pos, crate);
      }
      super.onRemove(state, level, pos, newState, moved);
   }
}
