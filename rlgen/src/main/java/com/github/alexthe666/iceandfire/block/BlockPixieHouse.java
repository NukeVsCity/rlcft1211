package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityPixieHouse;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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

/**
 * 1.12 BlockPixieHouse: house type = item metadata 0..5 (kept in the tile entity), rendered by its block entity renderer
 * (ENTITYBLOCK_ANIMATED), faces away from the placer, drops itself with its house type, releases its pixie when broken.
 */
public class BlockPixieHouse extends BaseEntityBlock {
   public static final MapCodec<BlockPixieHouse> CODEC = simpleCodec(BlockPixieHouse::new);
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

   public BlockPixieHouse(Properties props) {
      super(props.noOcclusion());
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
      IafNames.block(this, "iceandfire.pixie_house");
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Override
   protected boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      return worldIn.getBlockState(pos.below()).isFaceSturdy(worldIn, pos.below(), Direction.UP);
   }

   @Override
   protected void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean moving) {
      if (!state.is(newState.getBlock())) {
         this.dropPixie(worldIn, pos);
         int meta = 0;
         if (worldIn.getBlockEntity(pos) instanceof TileEntityPixieHouse house) {
            meta = house.houseType;
         }
         Block.popResource(worldIn, pos, IafMeta.stack(ModBlocks.pixieHouse, 1, meta));
      }
      super.onRemove(state, worldIn, pos, newState, moving);
   }

   @Override
   protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean moving) {
      worldIn.scheduleTick(pos, this, 10);
   }

   @Override
   protected void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
      if (!state.canSurvive(worldIn, pos)) {
         worldIn.destroyBlock(pos, true);
         this.dropPixie(worldIn, pos);
      }
   }

   public void dropPixie(Level world, BlockPos pos) {
      if (world.getBlockEntity(pos) instanceof TileEntityPixieHouse house && house.hasPixie) {
         house.releasePixie();
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
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   @Override
   public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      if (world.getBlockEntity(pos) instanceof TileEntityPixieHouse house) {
         house.houseType = IafMeta.getMetadata(stack);
         house.sync();
      }
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityPixieHouse(pos, state);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return createTickerHelper(type, ModBlocks.PIXIE_HOUSE_TILE.get(), TileEntityPixieHouse::tick);
   }
}
