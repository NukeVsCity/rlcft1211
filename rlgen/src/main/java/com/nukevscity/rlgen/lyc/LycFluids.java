package com.nukevscity.rlgen.lyc;

import com.nukevscity.rlgen.RLGen;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Lycanites Mobs' ooze, acid and poison: water-material fluids with the Lycanites textures (only what world gen needs, no effects yet). */
public final class LycFluids {
   private static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, RLGen.MODID);
   private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, RLGen.MODID);
   private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RLGen.MODID);

   public static final List<Kind> ALL = new ArrayList<>();
   public static final Kind OOZE = new Kind("ooze", MapColor.COLOR_LIGHT_BLUE, 10);
   public static final Kind ACID = new Kind("acid", MapColor.COLOR_LIGHT_GREEN, 10);
   public static final Kind POISON = new Kind("poison", MapColor.COLOR_PURPLE, 0);

   public static final class Kind {
      public final String name;
      public final DeferredHolder<FluidType, FluidType> type;
      public final DeferredHolder<Fluid, BaseFlowingFluid.Source> source;
      public final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing;
      public final DeferredBlock<LiquidBlock> block;

      Kind(String name, MapColor color, int light) {
         this.name = name;
         this.type = TYPES.register(name, () -> new FluidType(FluidType.Properties.create().descriptionId("block.rlgen." + name)
            .canSwim(true).canDrown(true).canExtinguish(true).supportsBoating(true).lightLevel(light)));
         Supplier<BaseFlowingFluid.Properties> props = () -> new BaseFlowingFluid.Properties(this.type, this.source(), this.flowing())
            .block(this.block()).slopeFindDistance(4).levelDecreasePerBlock(1);
         this.source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(props.get()));
         this.flowing = FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(props.get()));
         this.block = BLOCKS.registerBlock(name, p -> new LiquidBlock((FlowingFluid)this.source.get(), p),
            BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(color).lightLevel(s -> light));
         ALL.add(this);
      }

      private Supplier<Fluid> source() {
         return () -> this.source.get();
      }

      private Supplier<Fluid> flowing() {
         return () -> this.flowing.get();
      }

      private Supplier<LiquidBlock> block() {
         return () -> this.block.get();
      }
   }

   private LycFluids() {
   }

   public static void register(IEventBus modBus) {
      TYPES.register(modBus);
      FLUIDS.register(modBus);
      BLOCKS.register(modBus);
   }
}
