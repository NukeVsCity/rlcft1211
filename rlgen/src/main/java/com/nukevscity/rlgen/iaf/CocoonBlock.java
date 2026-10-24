package com.nukevscity.rlgen.iaf;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Ice and Fire BlockMyrmexCocoon: an 18-slot loot container made of sticky resin. */
public class CocoonBlock extends BaseEntityBlock {
   private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);
   private static final MapCodec<CocoonBlock> CODEC = simpleCodec(CocoonBlock::new);

   public CocoonBlock(Properties p) {
      super(p);
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CocoonBlockEntity(pos, state);
   }

   @Override
   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (!level.isClientSide && level.getBlockEntity(pos) instanceof CocoonBlockEntity be) {
         player.openMenu(be);
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   @Override
   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CocoonBlockEntity be) {
         Containers.dropContents(level, pos, be);
      }
      super.onRemove(state, level, pos, newState, moved);
   }
}
