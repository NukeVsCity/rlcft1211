package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockGoldPile: 1..8 layers, box = SNOW_AABB[layers] (also the collision box, so a 1-layer pile is 2 px high), right click
 * with a pile adds a layer up to 8, a 1-layer pile is replaceable, drops layers+1 nuggets (loot table), mobs cannot spawn on it.
 * Placement needs an opaque movement-blocking block, leaves or a pile with 7+ layers below, never ice or packed ice.
 */
public class BlockGoldPile extends Block {
   public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 8);
   protected static final VoxelShape[] SNOW_AABB = new VoxelShape[]{
      Block.box(0.0, 0.0, 0.0, 16.0, 0.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
   };

   public BlockGoldPile(Properties props) {
      this(props, "iceandfire.goldpile");
   }

   protected BlockGoldPile(Properties props, String name) {
      super(props.isValidSpawn((s, l, p, t) -> false));
      this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
      IafNames.block(this, name);
   }

   protected Block pileBlock() {
      return ModBlocks.goldPile;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(LAYERS);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SNOW_AABB[state.getValue(LAYERS)];
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SNOW_AABB[state.getValue(LAYERS)];
   }

   @Override
   protected boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }

   /** 1.12 isPassable: fewer than 5 layers. */
   @Override
   protected boolean isPathfindable(BlockState state, PathComputationType type) {
      return type == PathComputationType.LAND && state.getValue(LAYERS) < 5;
   }

   /** 1.12 canPlaceBlockAt. */
   @Override
   protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState below = level.getBlockState(pos.below());
      if (below.is(Blocks.ICE) || below.is(Blocks.PACKED_ICE)) {
         return false;
      }
      if (below.is(net.minecraft.tags.BlockTags.LEAVES)) {
         return true;
      }
      if (below.getBlock() == this && below.getValue(LAYERS) >= 7) {
         return true;
      }
      return below.canOcclude() && below.isSolidRender(level, pos.below()) && below.blocksMotion();
   }

   @Override
   protected ItemInteractionResult useItemOn(ItemStack item, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
      if (!item.isEmpty() && item.getItem() == this.pileBlock().asItem() && state.getValue(LAYERS) - 1 < 7) {
         worldIn.setBlock(pos, this.pileBlock().defaultBlockState().setValue(LAYERS, state.getValue(LAYERS) + 1), 3);
         if (!playerIn.getAbilities().instabuild) {
            item.shrink(1);
         }
         return ItemInteractionResult.sidedSuccess(worldIn.isClientSide);
      }
      return super.useItemOn(item, state, worldIn, pos, playerIn, hand, hit);
   }

   /** 1.12 isReplaceable: a single layer. */
   @Override
   protected boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
      return state.getValue(LAYERS) == 1;
   }
}
