package com.nukevscity.rlgen.iaf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Ice and Fire BlockMyrmexBiolight: a glowing plant hanging from the ceiling of a myrmex hive; "down" when more hangs below. */
public class BiolightBlock extends Block {
   public static final BooleanProperty DOWN = BooleanProperty.create("down");
   private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);

   public BiolightBlock(Properties p) {
      super(p);
      this.registerDefaultState(this.stateDefinition.any().setValue(DOWN, false));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(DOWN);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return Shapes.empty();
   }

   @Override
   protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState above = level.getBlockState(pos.above());
      return above.is(this) || above.isFaceSturdy(level, pos.above(), Direction.DOWN);
   }

   @Override
   protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (dir == Direction.UP && !this.canSurvive(state, level, pos)) {
         return Blocks.AIR.defaultBlockState();
      }
      return dir == Direction.DOWN ? state.setValue(DOWN, neighbor.is(this)) : state;
   }
}
