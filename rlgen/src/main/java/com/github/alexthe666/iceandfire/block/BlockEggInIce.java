package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityEggInIce;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

/**
 * 1.12 BlockEggInIce: slippery translucent ice holding an ice dragon egg. Harvesting it gives the egg back and it melts to water above
 * a solid/liquid block (like ice), no drops; pick block gives ice; pistons can push it (1.12 mobility flag 0).
 */
public class BlockEggInIce extends BaseEntityBlock {
   public static final MapCodec<BlockEggInIce> CODEC = simpleCodec(BlockEggInIce::new);

   public BlockEggInIce(Properties props) {
      super(props.friction(0.98F).noOcclusion().pushReaction(PushReaction.NORMAL).isRedstoneConductor((s, l, p) -> false)
         .isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false));
      IafNames.block(this, "iceandfire.egginice");
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityEggInIce(pos, state);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return createTickerHelper(type, ModBlocks.EGG_IN_ICE_TILE.get(), TileEntityEggInIce::tick);
   }

   @Override
   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
      return new ItemStack(Blocks.ICE);
   }

   @Override
   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   protected boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
      return adjacent.is(this) || super.skipRendering(state, adjacent, side);
   }

   @Override
   protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
      return 1.0F;
   }

   @Override
   public void playerDestroy(Level worldIn, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
      if (te instanceof TileEntityEggInIce tile) {
         tile.spawnEgg();
      }
      super.playerDestroy(worldIn, player, pos, state, te, stack);
      // 1.12 canSilkHarvest is false for blocks with a tile entity, so the silk touch branch never ran: it always melts
      {
         if (worldIn.dimensionType().ultraWarm()) {
            worldIn.removeBlock(pos, false);
            return;
         }
         BlockState below = worldIn.getBlockState(pos.below());
         if (below.blocksMotion() || below.liquid()) {
            worldIn.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
         }
      }
   }
}
