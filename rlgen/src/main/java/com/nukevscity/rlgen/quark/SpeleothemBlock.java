package com.nukevscity.rlgen.quark;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Quark BlockSpeleothem: a stalactite / stalagmite piece, 2, 4 or 8 pixels wide. */
public class SpeleothemBlock extends Block {
   public enum Size implements StringRepresentable {
      SMALL(2), MEDIUM(4), BIG(8);

      final VoxelShape shape;

      Size(int width) {
         double pad = (16 - width) / 2.0;
         this.shape = Block.box(pad, 0, pad, 16 - pad, 16, 16 - pad);
      }

      @Override
      public String getSerializedName() {
         return this.name().toLowerCase(java.util.Locale.ROOT);
      }
   }

   public static final EnumProperty<Size> SIZE = EnumProperty.create("size", Size.class);

   public SpeleothemBlock(Properties p) {
      super(p);
      this.registerDefaultState(this.stateDefinition.any().setValue(SIZE, Size.MEDIUM));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(SIZE);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return state.getValue(SIZE).shape;
   }
}
