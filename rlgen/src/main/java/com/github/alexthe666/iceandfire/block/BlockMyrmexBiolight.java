package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 1.12 BlockMyrmexBiolight: a hanging glow plant (light 9) that needs an opaque block or another biolight above; "down" when one hangs below. */
public class BlockMyrmexBiolight extends BushBlock {
   public static final MapCodec<BlockMyrmexBiolight> CODEC = simpleCodec(p -> new BlockMyrmexBiolight(p, false));
   public static final BooleanProperty CONNECTED_DOWN = BooleanProperty.create("down");
   protected static final VoxelShape BUSH_AABB = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

   public BlockMyrmexBiolight(Properties props, boolean jungle) {
      super(props);
      this.registerDefaultState(this.stateDefinition.any().setValue(CONNECTED_DOWN, false));
      IafNames.block(this, jungle ? "iceandfire.myrmex_jungle_biolight" : "iceandfire.myrmex_desert_biolight");
   }

   @Override
   protected MapCodec<? extends BushBlock> codec() {
      return CODEC;
   }

   @Override
   protected boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      BlockState up = worldIn.getBlockState(pos.above());
      return up.isSolidRender(worldIn, pos.above()) || up.getBlock() == this;
   }

   @Override
   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return BUSH_AABB;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return this.defaultBlockState().setValue(CONNECTED_DOWN, ctx.getLevel().getBlockState(ctx.getClickedPos().below()).getBlock() == this);
   }

   @Override
   protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (!state.canSurvive(level, pos)) {
         return Blocks.AIR.defaultBlockState();
      }
      return dir == Direction.DOWN ? state.setValue(CONNECTED_DOWN, neighbor.getBlock() == this) : state;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(CONNECTED_DOWN);
   }
}
