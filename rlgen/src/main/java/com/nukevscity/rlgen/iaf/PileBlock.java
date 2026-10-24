package com.nukevscity.rlgen.iaf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Ice and Fire BlockGoldPile / BlockSilverPile: 1..8 layers of 2 pixels, like snow layers. */
public class PileBlock extends Block {
   public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 8);
   private static final VoxelShape[] SHAPES = new VoxelShape[9];

   static {
      for (int i = 0; i <= 8; i++) {
         SHAPES[i] = Block.box(0, 0, 0, 16, i * 2, 16);
      }
   }

   public PileBlock(Properties p) {
      super(p);
      this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(LAYERS);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPES[state.getValue(LAYERS)];
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPES[state.getValue(LAYERS) - 1];
   }

   @Override
   protected boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }
}
