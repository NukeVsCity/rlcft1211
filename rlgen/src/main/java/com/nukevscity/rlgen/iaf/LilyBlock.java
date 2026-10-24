package com.nukevscity.rlgen.iaf;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Ice and Fire BlockElementalFlower: placed on sand/netherrack (fire) or ice (frost), then stays on anything (canSustainBush true). */
public class LilyBlock extends BushBlock {
   private static final VoxelShape SHAPE = BushBlock.box(5, 0, 5, 11, 10, 11);
   private final boolean fire;

   public LilyBlock(Properties p, boolean fire) {
      super(p);
      this.fire = fire;
   }

   @Override
   protected MapCodec<? extends BushBlock> codec() {
      return simpleCodec(p -> new LilyBlock(p, this.fire));
   }

   /** BlockElementalFlower.canPlaceBlockAt (the soil test at world gen). */
   public boolean canPlaceOn(BlockState soil) {
      if (this.fire) {
         return soil.is(BlockTags.SAND) || soil.is(Blocks.NETHERRACK) || soil.is(com.nukevscity.rlgen.RLGen.SAND_DEFILED.get());
      }
      return soil.is(Blocks.ICE) || soil.is(Blocks.PACKED_ICE) || soil.is(Blocks.FROSTED_ICE) || soil.is(Blocks.BLUE_ICE);
   }

   @Override
   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }

   @Override
   protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return !level.getBlockState(pos.below()).isAir();
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }
}
