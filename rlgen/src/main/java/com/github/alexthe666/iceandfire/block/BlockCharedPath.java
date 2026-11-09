package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 1.12 BlockCharedPath (extends BlockGrassPath): 15/16 high, turns into chared/frozen dirt when a solid block is above, drops the dirt. */
public class BlockCharedPath extends Block {
   protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
   boolean isFire;

   public BlockCharedPath(Properties props, boolean isFire) {
      super(props.isRedstoneConductor((s, l, p) -> false));
      this.isFire = isFire;
      IafNames.block(this, isFire ? "iceandfire.charedGrassPath" : "iceandfire.frozenGrassPath");
   }

   private BlockState dirt() {
      return this.isFire ? ModBlocks.charedDirt.defaultBlockState() : ModBlocks.frozenDirt.defaultBlockState();
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }

   @Override
   protected boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }

   @Override
   protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
      return false;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return this.defaultBlockState();
   }

   /** 1.12 neighborChanged: a solid block above turns the path into dirt. */
   @Override
   protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
      super.neighborChanged(state, level, pos, block, fromPos, moving);
      if (level.getBlockState(pos.above()).isSolid()) {
         level.setBlockAndUpdate(pos, this.dirt());
      }
   }

   @Override
   protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      return state;
   }

   @Override
   protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
   }
}
