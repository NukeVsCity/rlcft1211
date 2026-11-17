package com.charles445.simpledifficulty.block;

import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.register.SDParticles;
import com.charles445.simpledifficulty.tileentity.TileEntityTemperature;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** 1.12 BlockTemperature (heater +1, chiller -1): on while powered by redstone, the tile entity heats/cools the area. */
public class BlockTemperature extends BaseEntityBlock {
   public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");
   private final float temperature;
   private final MapCodec<BlockTemperature> codec;

   public BlockTemperature(float temperature, Properties props) {
      super(props);
      this.temperature = temperature;
      this.codec = simpleCodec(p -> new BlockTemperature(temperature, p));
      this.registerDefaultState(this.stateDefinition.any().setValue(ENABLED, false));
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return this.codec;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(ENABLED);
   }

   public float getActiveTemperatureMult() {
      return this.temperature;
   }

   @Override
   protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moving) {
      if (!world.isClientSide && !oldState.is(this)) {
         boolean enabled = state.getValue(ENABLED);
         boolean powered = world.hasNeighborSignal(pos);
         if (enabled && !powered) {
            world.setBlock(pos, state.setValue(ENABLED, false), 2);
         } else if (!enabled && powered) {
            world.setBlock(pos, state.setValue(ENABLED, true), 2);
         }
      }
   }

   @Override
   protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
      if (!world.isClientSide) {
         boolean enabled = state.getValue(ENABLED);
         boolean powered = world.hasNeighborSignal(pos);
         if (enabled && !powered) {
            world.scheduleTick(pos, this, 4);
         } else if (!enabled && powered) {
            world.setBlock(pos, state.setValue(ENABLED, true), 2);
         }
      }
   }

   @Override
   protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
      if (state.getValue(ENABLED) && !world.hasNeighborSignal(pos)) {
         world.setBlock(pos, state.setValue(ENABLED, false), 2);
      }
   }

   @Override
   public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
      if (state.getValue(ENABLED) && rand.nextFloat() <= 0.33F) {
         double x_a = 0.125;
         double z_a = 0.125;
         double x_b = 0.0;
         double z_b = 0.0;
         double endoff = 0.875;
         double thickness = 0.2;
         switch (rand.nextInt(4)) {
            case 0 -> {
               x_b = endoff;
               z_b = thickness;
            }
            case 1 -> {
               x_a = endoff - thickness;
               x_b = endoff;
               z_b = endoff;
            }
            case 2 -> {
               x_b = endoff;
               z_a = endoff - thickness;
               z_b = endoff;
            }
            default -> {
               x_b = thickness;
               z_b = endoff;
            }
         }
         double x_r = rand.nextDouble() * (x_b - x_a);
         double z_r = rand.nextDouble() * (z_b - z_a);
         if (ModConfig.client.heaterParticles) {
            world.addParticle(this.temperature >= 0.0F ? SDParticles.HEATER.get() : SDParticles.CHILLER.get(), x_a + x_r + pos.getX(), 0.775 + pos.getY(),
               z_a + z_r + pos.getZ(), 0.0, 0.05, 0.0);
         }
      }
   }

   @Override
   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEntityTemperature(pos, state);
   }
}
