package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 BlockElementalFlower (fire_lily, frost_lily). Placement needs sand-material soil or netherrack (fire) / ice or packed ice
 * (frost), like canPlaceBlockAt; once placed it stays on anything (1.12 canSustainBush returns true).
 */
public class BlockElementalFlower extends BushBlock {
   public static final MapCodec<BlockElementalFlower> CODEC = simpleCodec(p -> new BlockElementalFlower(p, true));
   private final boolean isFire;

   public BlockElementalFlower(Properties props, boolean isFire) {
      super(props);
      this.isFire = isFire;
      IafNames.block(this, isFire ? "iceandfire.fire_lily" : "iceandfire.frost_lily");
   }

   @Override
   protected MapCodec<? extends BushBlock> codec() {
      return CODEC;
   }

   /** 1.12 Material.SAND: sand, red sand, soul sand; Material.ICE/PACKED_ICE: ice, frosted ice, packed ice. */
   public boolean canPlaceOn(BlockState soil) {
      if (this.isFire) {
         return soil.is(BlockTags.SAND) || soil.is(Blocks.SOUL_SAND) || soil.is(Blocks.NETHERRACK);
      }
      return soil.is(Blocks.ICE) || soil.is(Blocks.PACKED_ICE) || soil.is(Blocks.FROSTED_ICE);
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return this.canPlaceOn(ctx.getLevel().getBlockState(ctx.getClickedPos().below())) ? this.defaultBlockState() : null;
   }

   @Override
   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }

   @Override
   protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return true;
   }
}
