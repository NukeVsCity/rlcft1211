package com.lycanitesmobs.compat;

import com.lycanitesmobs.core.block.BlockFluidBase;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * 1.12 net.minecraftforge.fluids.Fluid: the fluid's name, textures and physical properties, plus its 1.21 source/flowing fluids
 * (created right away so the LiquidBlock can be built in preInit; their FluidType is made from the 1.12 properties when the
 * FLUID_TYPES registry opens). Tick delay = viscosity / 200 like Forge BlockFluidBase.
 */
public class ForgeFluid {
   public final String fluidName;
   public final ResourceLocation still;
   public final ResourceLocation flowing;
   public int luminosity = 0;
   public int density = 1000;
   public int temperature = 300;
   public int viscosity = 1000;
   public boolean isGaseous = false;
   public FluidType fluidType;
   public LycSource sourceFluid;
   public LycFlowing flowingFluid;
   public BlockFluidBase block;
   public Item bucket;

   public ForgeFluid(String fluidName, ResourceLocation still, ResourceLocation flowing) {
      this.fluidName = fluidName;
      this.still = still;
      this.flowing = flowing;
      BaseFlowingFluid.Properties props = new BaseFlowingFluid.Properties(() -> this.fluidType, () -> this.sourceFluid, () -> this.flowingFluid)
         .slopeFindDistance(4)
         .levelDecreasePerBlock(1)
         .block(() -> this.block)
         .bucket(() -> this.bucket);
      this.sourceFluid = new LycSource(props, this);
      this.flowingFluid = new LycFlowing(props, this);
   }

   public String getName() {
      return this.fluidName;
   }

   public ForgeFluid setLuminosity(int luminosity) {
      this.luminosity = luminosity;
      return this;
   }

   public ForgeFluid setDensity(int density) {
      this.density = density;
      return this;
   }

   public ForgeFluid setTemperature(int temperature) {
      this.temperature = temperature;
      return this;
   }

   public ForgeFluid setViscosity(int viscosity) {
      this.viscosity = viscosity;
      return this;
   }

   public ForgeFluid setGaseous(boolean isGaseous) {
      this.isGaseous = isGaseous;
      return this;
   }

   public int getLuminosity() {
      return this.luminosity;
   }

   public int getDensity() {
      return this.density;
   }

   public int getTemperature() {
      return this.temperature;
   }

   public int getViscosity() {
      return this.viscosity;
   }

   public FlowingFluid getSource() {
      return this.sourceFluid;
   }

   public FlowingFluid getFlowing() {
      return this.flowingFluid;
   }

   int tickDelay() {
      return Math.max(1, this.viscosity / 200);
   }

   void onTick(Level level, BlockPos pos) {
      if (this.block != null && !level.isClientSide) {
         this.block.checkNeighbours(level, pos);
      }
   }

   public static class LycSource extends BaseFlowingFluid.Source {
      private final ForgeFluid forgeFluid;

      public LycSource(BaseFlowingFluid.Properties properties, ForgeFluid forgeFluid) {
         super(properties);
         this.forgeFluid = forgeFluid;
      }

      @Override
      public int getTickDelay(LevelReader level) {
         return this.forgeFluid.tickDelay();
      }

      @Override
      public void tick(Level level, BlockPos pos, FluidState state) {
         this.forgeFluid.onTick(level, pos);
         super.tick(level, pos, state);
      }
   }

   public static class LycFlowing extends BaseFlowingFluid.Flowing {
      private final ForgeFluid forgeFluid;

      public LycFlowing(BaseFlowingFluid.Properties properties, ForgeFluid forgeFluid) {
         super(properties);
         this.forgeFluid = forgeFluid;
      }

      @Override
      public int getTickDelay(LevelReader level) {
         return this.forgeFluid.tickDelay();
      }

      @Override
      public void tick(Level level, BlockPos pos, FluidState state) {
         this.forgeFluid.onTick(level, pos);
         super.tick(level, pos, state);
      }
   }
}
