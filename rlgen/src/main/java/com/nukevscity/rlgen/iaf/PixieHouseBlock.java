package com.nukevscity.rlgen.iaf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Ice and Fire BlockPixieHouse: a mushroom house (six looks); the 1.12 tile entity model is a block model here. Pixies are not ported. */
public class PixieHouseBlock extends HorizontalDirectionalBlock {
   public static final IntegerProperty TYPE = IntegerProperty.create("house", 0, 5);
   private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);
   private static final com.mojang.serialization.MapCodec<PixieHouseBlock> CODEC = simpleCodec(PixieHouseBlock::new);

   public PixieHouseBlock(Properties p) {
      super(p);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, 0));
   }

   @Override
   protected com.mojang.serialization.MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return CODEC;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(FACING, TYPE);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }
}
