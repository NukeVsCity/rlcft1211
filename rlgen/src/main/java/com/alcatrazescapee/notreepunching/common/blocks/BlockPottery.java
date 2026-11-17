package com.alcatrazescapee.notreepunching.common.blocks;

import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.util.types.Pottery;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockPottery (pottery/worked ... pottery/flower_pot): Material.CLAY, hardness 0.8 - 0.1 * ordinal, shovel, the 1.12 boxes as
 * shape and collision. Worked clay drops 3 clay balls (loot table) and picks as a clay block; the others drop themselves and are
 * fired (fire pit, furnace) into getFiredType.
 */
public class BlockPottery extends Block {
   private static final Map<Pottery, BlockPottery> MAP = new EnumMap<>(Pottery.class);
   private static final VoxelShape[] AABB = new VoxelShape[] {
      Shapes.box(0.0625, 0.0, 0.0625, 0.9375, 0.9375, 0.9375),
      Shapes.box(0.125, 0.0, 0.125, 0.875, 0.875, 0.875),
      Shapes.box(0.25, 0.0, 0.25, 0.75, 0.625, 0.75),
      Shapes.box(0.3125, 0.0, 0.3125, 0.6875, 0.625, 0.6875),
      Shapes.box(0.3125, 0.0, 0.3125, 0.6875, 0.375, 0.6875)
   };
   private final Pottery type;

   public static BlockPottery get(Pottery type) {
      return MAP.get(type);
   }

   public BlockPottery(Pottery type, Properties props) {
      super(props);
      this.type = type;
      MAP.put(type, this);
   }

   public Pottery getType() {
      return this.type;
   }

   public ItemStack getFiredType() {
      return switch (this.type) {
         case LARGE_VESSEL -> new ItemStack(NoTreePunching.CERAMIC_LARGE_VESSEL.get());
         case BUCKET -> new ItemStack(NoTreePunching.CERAMIC_BUCKET.get());
         case SMALL_VESSEL -> new ItemStack(NoTreePunching.CERAMIC_SMALL_VESSEL.get());
         case FLOWER_POT -> new ItemStack(Items.FLOWER_POT);
         default -> ItemStack.EMPTY;
      };
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return AABB[this.type.ordinal()];
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return AABB[this.type.ordinal()];
   }

   @Override
   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
      return this.type == Pottery.WORKED ? new ItemStack(Blocks.CLAY) : super.getCloneItemStack(level, pos, state);
   }
}
