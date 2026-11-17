package com.alcatrazescapee.notreepunching.common.blocks;

import com.alcatrazescapee.alcatrazcore.tile.TileInventory;
import com.alcatrazescapee.notreepunching.common.container.ContainerLargeVessel;
import com.alcatrazescapee.notreepunching.common.tile.TileLargeVessel;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 1.12 BlockLargeVessel (ceramic_large_vessel): Material.GROUND, hardness 1, pickaxe; right click (not sneaking) opens the 9 slots. */
public class BlockLargeVessel extends BaseEntityBlock {
   public static final MapCodec<BlockLargeVessel> CODEC = simpleCodec(BlockLargeVessel::new);
   private static final VoxelShape AABB = Shapes.box(0.125, 0.0, 0.125, 0.875, 0.875, 0.875);

   public BlockLargeVessel(Properties props) {
      super(props);
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player playerIn, BlockHitResult hit) {
      if (!worldIn.isClientSide && !playerIn.isShiftKeyDown() && worldIn.getBlockEntity(pos) instanceof TileLargeVessel te) {
         playerIn.openMenu(new SimpleMenuProvider((id, inv, p) -> new ContainerLargeVessel(id, inv, te), Component.translatable(this.getDescriptionId())),
            pos);
      }
      return InteractionResult.sidedSuccess(worldIn.isClientSide);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileLargeVessel(pos, state);
   }

   @Override
   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return AABB;
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return AABB;
   }

   /** BlockTileCore.breakBlock -> TileInventory.onBreakBlock */
   @Override
   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TileInventory te) {
         te.onBreakBlock();
      }
      super.onRemove(state, level, pos, newState, moved);
   }
}
