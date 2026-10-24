package com.nukevscity.rlgen.iaf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Ice and Fire BlockCharedPath: a grass path 15 pixels high that stays as it is. */
public class PathBlock extends Block {
   private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);

   public PathBlock(Properties p) {
      super(p);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }

   @Override
   protected boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }
}
