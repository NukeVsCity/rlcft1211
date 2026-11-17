package com.alcatrazescapee.notreepunching.common.blocks;

import com.alcatrazescapee.notreepunching.ModConfig;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.util.types.Stone;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** 1.12 BlockRock (loose_rock/*): picked up with a right click, pops off when the block below is no longer a normal cube. */
public class BlockRock extends Block {
   private static final VoxelShape SHAPE = Shapes.box(0.375, 0.0, 0.375, 0.615, 0.0625, 0.625);
   private final Stone type;

   public BlockRock(Stone type, Properties props) {
      super(props);
      this.type = type;
   }

   public Stone getType() {
      return this.type;
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return Shapes.empty();
   }

   /** 1.12 neighborChanged: stateUnder.isNormalCube() */
   @Override
   protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
      if (!world.isClientSide && !world.getBlockState(pos.below()).isRedstoneConductor(world, pos.below())) {
         dropResources(state, world, pos);
         world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
      if (ModConfig.BALANCE.canPickUpRocks) {
         if (!world.isClientSide) {
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(NoTreePunching.ROCKS.get(this.type).get()));
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
         }
         return InteractionResult.sidedSuccess(world.isClientSide);
      }
      return InteractionResult.PASS;
   }

   @Override
   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
      return new ItemStack(NoTreePunching.ROCKS.get(this.type).get());
   }
}
