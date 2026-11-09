package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityMyrmexCocoon;
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

/** 1.12 BlockMyrmexCocoon: an 18-slot loot container, slime sounds, drops its contents. */
public class BlockMyrmexCocoon extends BaseEntityBlock {
   public static final MapCodec<BlockMyrmexCocoon> CODEC = simpleCodec(p -> new BlockMyrmexCocoon(p, false));

   public BlockMyrmexCocoon(Properties props, boolean jungle) {
      super(props.noOcclusion());
      IafNames.block(this, jungle ? "iceandfire.jungle_myrmex_cocoon" : "iceandfire.desert_myrmex_cocoon");
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Override
   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   protected void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean moving) {
      if (!state.is(newState.getBlock())) {
         if (worldIn.getBlockEntity(pos) instanceof TileEntityMyrmexCocoon te) {
            Containers.dropContents(worldIn, pos, te);
            worldIn.updateNeighbourForOutputSignal(pos, this);
         }
      }
      super.onRemove(state, worldIn, pos, newState, moving);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
      if (player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      }
      if (!world.isClientSide && world.getBlockEntity(pos) instanceof TileEntityMyrmexCocoon te) {
         player.openMenu(te);
      }
      return InteractionResult.sidedSuccess(world.isClientSide);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityMyrmexCocoon(pos, state);
   }
}
