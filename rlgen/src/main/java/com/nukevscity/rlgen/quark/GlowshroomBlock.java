package com.nukevscity.rlgen.quark;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Quark BlockGlowshroom: a glowing blue mushroom on glowcelium. */
public class GlowshroomBlock extends BushBlock {
   private static final VoxelShape SHAPE = BushBlock.box(3.2, 0, 3.2, 12.8, 9.6, 12.8);
   private static final MapCodec<GlowshroomBlock> CODEC = simpleCodec(GlowshroomBlock::new);

   public GlowshroomBlock(Properties p) {
      super(p);
   }

   @Override
   protected MapCodec<? extends BushBlock> codec() {
      return CODEC;
   }

   @Override
   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return state.isSolidRender(level, pos);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }
}
