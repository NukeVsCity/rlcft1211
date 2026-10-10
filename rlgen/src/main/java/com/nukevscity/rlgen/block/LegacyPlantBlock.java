package com.nukevscity.rlgen.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Cross-model plants from Traverse and Defiled Lands (dead/cold grass, blastem, scuronotte) with their 1.12 soil rules. */
public class LegacyPlantBlock extends BushBlock {
   private final Predicate<BlockState> soil;
   private final VoxelShape shape;

   public LegacyPlantBlock(Properties properties, Predicate<BlockState> soil, VoxelShape shape) {
      super(properties);
      this.soil = soil;
      this.shape = shape;
   }

   @Override
   protected MapCodec<? extends BushBlock> codec() {
      return simpleCodec(p -> new LegacyPlantBlock(p, this.soil, this.shape));
   }

   @Override
   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return this.soil.test(state);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return this.shape;
   }
}
