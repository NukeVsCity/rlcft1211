package com.charles445.simpledifficulty.api;

import com.charles445.simpledifficulty.SimpleDifficulty;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 1.12 SDFluids + RegisterFluids: Forge fluid "purifiedwater" (default Fluid properties = water-like) in a BlockFluidClassic of
 * Material.WATER with light value 1. The fluids are in the minecraft:water tag, like 1.12 Material.WATER (swimming, glass bottles).
 */
public class SDFluids {
   public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, SimpleDifficulty.MODID);
   public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, SimpleDifficulty.MODID);

   public static final DeferredHolder<FluidType, FluidType> purifiedWaterType = TYPES.register("purifiedwater",
      () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.purifiedwater").lightLevel(1).canExtinguish(true).canHydrate(true)
         .supportsBoating(true).canConvertToSource(true).fallDistanceModifier(0.0F)));
   private static BaseFlowingFluid.Properties props() {
      return new BaseFlowingFluid.Properties(purifiedWaterType, SDFluids.purifiedWater, SDFluids.flowingPurifiedWater)
         .block(SDFluids.blockPurifiedWater).bucket(SDItems.purifiedWaterBucket);
   }

   public static final DeferredHolder<Fluid, FlowingFluid> purifiedWater = FLUIDS.register("purifiedwater", () -> new BaseFlowingFluid.Source(props()));
   public static final DeferredHolder<Fluid, FlowingFluid> flowingPurifiedWater = FLUIDS.register("flowing_purifiedwater",
      () -> new BaseFlowingFluid.Flowing(props()));
   public static final DeferredBlock<LiquidBlock> blockPurifiedWater = SDBlocks.BLOCKS.registerBlock("purifiedwater",
      p -> new LiquidBlock(purifiedWater.get(), p),
      BlockBehaviour.Properties.of().mapColor(MapColor.WATER).replaceable().noCollission().strength(100.0F).pushReaction(PushReaction.DESTROY)
         .noLootTable().liquid().lightLevel(s -> 1));

   public static void register(IEventBus bus) {
      TYPES.register(bus);
      FLUIDS.register(bus);
   }
}
