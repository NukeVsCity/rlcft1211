package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafNames;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * 1.12 BlockMyrmexConnectedResin (hardened resin and resin glass): connected-texture block (up/down/north/east/south/west true when
 * the same block is there), translucent, faces between two of them hidden.
 */
public class BlockMyrmexConnectedResin extends Block {
   public static final BooleanProperty UP = BooleanProperty.create("up");
   public static final BooleanProperty DOWN = BooleanProperty.create("down");
   public static final BooleanProperty NORTH = BooleanProperty.create("north");
   public static final BooleanProperty EAST = BooleanProperty.create("east");
   public static final BooleanProperty SOUTH = BooleanProperty.create("south");
   public static final BooleanProperty WEST = BooleanProperty.create("west");

   public BlockMyrmexConnectedResin(Properties props, boolean jungle, boolean glass) {
      super(props.noOcclusion().isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false));
      this.registerDefaultState(this.stateDefinition.any().setValue(UP, false).setValue(DOWN, false).setValue(NORTH, false).setValue(EAST, false)
         .setValue(SOUTH, false).setValue(WEST, false));
      if (glass) {
         IafNames.block(this, jungle ? "iceandfire.myrmex_jungle_resin_glass" : "iceandfire.myrmex_desert_resin_glass");
      } else {
         IafNames.block(this, jungle ? "iceandfire.myrmex_jungle_resin_block" : "iceandfire.myrmex_desert_resin_block");
      }
   }

   private BlockState connect(BlockState state, BlockGetter world, BlockPos pos) {
      for (Direction d : Direction.values()) {
         state = state.setValue(prop(d), world.getBlockState(pos.relative(d)).getBlock() == this);
      }
      return state;
   }

   private static BooleanProperty prop(Direction d) {
      return switch (d) {
         case UP -> UP;
         case DOWN -> DOWN;
         case NORTH -> NORTH;
         case SOUTH -> SOUTH;
         case EAST -> EAST;
         case WEST -> WEST;
      };
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return this.connect(this.defaultBlockState(), ctx.getLevel(), ctx.getClickedPos());
   }

   @Override
   protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      return state.setValue(prop(dir), neighbor.getBlock() == this);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
   }

   @Override
   protected boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
      return adjacent.getBlock() == this || super.skipRendering(state, adjacent, side);
   }

   @Override
   protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
      return 1.0F;
   }

   @Override
   protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }
}
