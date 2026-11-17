package com.charles445.simpledifficulty.block;

import com.charles445.simpledifficulty.api.SDBlocks;
import com.charles445.simpledifficulty.tileentity.TileEntitySpit;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 1.12 BlockSpit: needs a campfire below (pops off otherwise), cooks food on the tile entity while the campfire burns. */
public class BlockSpit extends BaseEntityBlock {
   public static final MapCodec<BlockSpit> CODEC = simpleCodec(BlockSpit::new);
   private static final VoxelShape HITBOX = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.25, 1.0);

   public BlockSpit(Properties props) {
      super(props);
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntitySpit(pos, state);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide ? null : createTickerHelper(type, SDBlocks.SPIT_TILE.get(), (l, p, s, te) -> te.update());
   }

   @Override
   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (!world.isClientSide && world.getBlockEntity(pos) instanceof TileEntitySpit te) {
         te.handleRightClick(world, pos, state, player, hand);
      }
      return ItemInteractionResult.sidedSuccess(world.isClientSide);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
      if (!world.isClientSide && world.getBlockEntity(pos) instanceof TileEntitySpit te) {
         te.handleRightClick(world, pos, state, player, InteractionHand.MAIN_HAND);
      }
      return InteractionResult.sidedSuccess(world.isClientSide);
   }

   @Override
   protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
      this.checkCampfireOrDestroy(world, pos, state);
   }

   @Override
   protected boolean isRandomlyTicking(BlockState state) {
      return true;
   }

   @Override
   protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
      return world.getBlockState(pos.below()).getBlock() == SDBlocks.campfire.get();
   }

   @Override
   protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
      super.neighborChanged(state, world, pos, block, fromPos, moving);
      this.checkCampfireOrDestroy(world, pos, state);
   }

   public void checkCampfireOrDestroy(Level world, BlockPos pos, BlockState state) {
      if (!world.isClientSide && !this.canSurvive(state, world, pos)) {
         dropResources(state, world, pos, world.getBlockEntity(pos));
         world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
      }
   }

   @Override
   protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moving) {
      if (!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileEntitySpit te) {
         te.dumpItems(world, pos);
         te.dumpExperience(world, pos);
      }
      super.onRemove(state, world, pos, newState, moving);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return HITBOX;
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   @Override
   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }
}
