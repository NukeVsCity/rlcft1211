package com.nukevscity.rlgen.block;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

/** Defiled Lands vilespine: a thorny reed-like column on defiled ground that hurts on touch. */
public class VilespineBlock extends Block {
   public static final BooleanProperty TOPMOST = BooleanProperty.create("topmost");
   private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
   private final Predicate<BlockState> soil;

   public VilespineBlock(Properties properties, Predicate<BlockState> soil) {
      super(properties);
      this.soil = soil;
      this.registerDefaultState(this.stateDefinition.any().setValue(TOPMOST, true));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(TOPMOST);
   }

   @Override
   protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState below = level.getBlockState(pos.below());
      return below.is(this) || this.soil.test(below);
   }

   @Override
   protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (!state.canSurvive(level, pos)) {
         return Blocks.AIR.defaultBlockState();
      }
      return dir == Direction.UP ? state.setValue(TOPMOST, !neighbor.is(this)) : state;
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   @Override
   protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      entity.hurt(level.damageSources().cactus(), 2.0F);
   }
}
