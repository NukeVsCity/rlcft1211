package com.lycanitesmobs.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * 1.12 net.minecraftforge.fluids.Fluid: the fluid's name, textures and physical properties. ObjectManager turns it into a 1.21
 * FluidType + source/flowing fluids when the registries open (see ObjectManager.registerFluids).
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
   public BaseFlowingFluid.Source sourceFluid;
   public BaseFlowingFluid.Flowing flowingFluid;
   public Object block;

   public ForgeFluid(String fluidName, ResourceLocation still, ResourceLocation flowing) {
      this.fluidName = fluidName;
      this.still = still;
      this.flowing = flowing;
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
}
