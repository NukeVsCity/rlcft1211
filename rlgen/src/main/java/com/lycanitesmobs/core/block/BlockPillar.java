package com.lycanitesmobs.core.block;

import com.lycanitesmobs.compat.Material;
import com.lycanitesmobs.core.info.ModInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/** 1.12 BlockPillar: a BlockBase with an axis, placed along the clicked face's axis. */
public class BlockPillar extends BlockBase {
   public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

   public BlockPillar(Material material, ModInfo group, String name) {
      super(material, group, name);
      this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(AXIS);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
   }

   @Override
   protected BlockState rotate(BlockState state, Rotation rotation) {
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
         case CLOCKWISE_90:
            switch (state.getValue(AXIS)) {
               case X:
                  return state.setValue(AXIS, Direction.Axis.Z);
               case Z:
                  return state.setValue(AXIS, Direction.Axis.X);
               default:
                  return state;
            }
         default:
            return state;
      }
   }
}
