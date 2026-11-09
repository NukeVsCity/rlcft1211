package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityLectern;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 1.12 BlockLectern: faces away from the placer, needs a solid top below (breaks on the next tick otherwise), opens the lectern GUI. */
public class BlockLectern extends BaseEntityBlock {
   public static final MapCodec<BlockLectern> CODEC = simpleCodec(BlockLectern::new);
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   private static final VoxelShape AABB = Block.box(2.0, 0.0, 2.0, 14.0, 23.0, 14.0);

   public BlockLectern(Properties props) {
      super(props.noOcclusion());
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
      IafNames.block(this, "iceandfire.lectern");
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return AABB;
   }

   @Override
   protected void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean moving) {
      if (!state.is(newState.getBlock())) {
         BlockEntity tileentity = worldIn.getBlockEntity(pos);
         if (tileentity instanceof TileEntityLectern lectern) {
            Containers.dropContents(worldIn, pos, lectern);
            worldIn.updateNeighbourForOutputSignal(pos, this);
         }
      }
      super.onRemove(state, worldIn, pos, newState, moving);
   }

   @Override
   protected boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      return worldIn.getBlockState(pos.below()).isFaceSturdy(worldIn, pos.below(), Direction.UP);
   }

   @Override
   protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean moving) {
      worldIn.scheduleTick(pos, this, 10);
   }

   @Override
   protected void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
      if (!state.canSurvive(worldIn, pos)) {
         worldIn.destroyBlock(pos, true);
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   @Override
   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player playerIn, BlockHitResult hit) {
      if (playerIn.isShiftKeyDown()) {
         return InteractionResult.PASS;
      }
      if (!worldIn.isClientSide && worldIn.getBlockEntity(pos) instanceof TileEntityLectern lectern) {
         playerIn.openMenu(lectern);
      }
      return InteractionResult.sidedSuccess(worldIn.isClientSide);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityLectern(pos, state);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return createTickerHelper(type, ModBlocks.LECTERN_TILE.get(), TileEntityLectern::tick);
   }
}
