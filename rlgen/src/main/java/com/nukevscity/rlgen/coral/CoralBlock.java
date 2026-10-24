package com.nukevscity.rlgen.coral;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Coral Reef 2.5 BlockCoral: an underwater cross plant (material water, light 15), on reef blocks or on top of a tall coral (types 5 and 6). */
public class CoralBlock extends Block implements LiquidBlockContainer {
   private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);
   public final boolean high;

   public CoralBlock(Properties p, boolean high) {
      super(p);
      this.high = high;
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return Shapes.empty();
   }

   @Override
   protected FluidState getFluidState(BlockState state) {
      return Fluids.WATER.getSource(false);
   }

   public static boolean isWaterMaterial(BlockState s) {
      return s.is(Blocks.WATER) || s.getBlock() instanceof CoralBlock;
   }

   @Override
   protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      if (!isWaterMaterial(level.getBlockState(pos.above()))) {
         return false;
      }
      BlockState below = level.getBlockState(pos.below());
      return below.getBlock() instanceof ReefBlock || below.getBlock() instanceof CoralBlock c && c.high;
   }

   @Override
   protected BlockState updateShape(BlockState state, Direction dir, BlockState other, LevelAccessor level, BlockPos pos, BlockPos otherPos) {
      if (!this.canSurvive(state, level, pos)) {
         return Blocks.WATER.defaultBlockState();
      }
      level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      return state;
   }

   @Override
   public boolean canPlaceLiquid(net.minecraft.world.entity.player.Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
      return false;
   }

   @Override
   public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
      return false;
   }
}
