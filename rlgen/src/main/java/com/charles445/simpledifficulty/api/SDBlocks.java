package com.charles445.simpledifficulty.api;

import com.charles445.simpledifficulty.SimpleDifficulty;
import com.charles445.simpledifficulty.block.BlockCampfire;
import com.charles445.simpledifficulty.block.BlockRainCollector;
import com.charles445.simpledifficulty.block.BlockSpit;
import com.charles445.simpledifficulty.block.BlockTemperature;
import com.charles445.simpledifficulty.tileentity.TileEntitySpit;
import com.charles445.simpledifficulty.tileentity.TileEntityTemperature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 1.12 SDBlocks + RegisterBlocks. */
public class SDBlocks {
   public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SimpleDifficulty.MODID);
   public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SimpleDifficulty.MODID);

   // Material.CIRCUITS: no collision handled by the block, breaks by piston (destroy), not replaceable
   public static final DeferredBlock<Block> campfire = BLOCKS.registerBlock("campfire", BlockCampfire::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5F).sound(SoundType.WOOD).randomTicks().noOcclusion()
         .pushReaction(PushReaction.DESTROY).noCollission().lightLevel(s -> s.getValue(BlockCampfire.BURNING) ? 15 : 0));
   public static final DeferredBlock<Block> rainCollector = BLOCKS.registerBlock("rain_collector", BlockRainCollector::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0F).sound(SoundType.METAL).randomTicks().noOcclusion()
         .requiresCorrectToolForDrops());
   public static final DeferredBlock<Block> heater = BLOCKS.registerBlock("heater", p -> new BlockTemperature(1.0F, p), temperatureProps());
   public static final DeferredBlock<Block> chiller = BLOCKS.registerBlock("chiller", p -> new BlockTemperature(-1.0F, p), temperatureProps());
   public static final DeferredBlock<Block> spit = BLOCKS.registerBlock("spit", BlockSpit::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5F).sound(SoundType.WOOD).randomTicks().noOcclusion().noCollission()
         .ignitedByLava());

   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntitySpit>> SPIT_TILE = TILES.register("campfirespit",
      () -> BlockEntityType.Builder.of(TileEntitySpit::new, spit.get()).build(null));
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityTemperature>> TEMPERATURE_TILE = TILES.register("temperaturechanged",
      () -> BlockEntityType.Builder.of(TileEntityTemperature::new, heater.get(), chiller.get()).build(null));

   // Material.IRON: needs a pickaxe in 1.12 (iron material requires tool), hardness 0.5
   private static BlockBehaviour.Properties temperatureProps() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.5F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops()
         .lightLevel(s -> s.getValue(BlockTemperature.ENABLED) ? 7 : 0).emissiveRendering((s, l, p) -> s.getValue(BlockTemperature.ENABLED));
   }

   public static void register(IEventBus bus) {
      BLOCKS.register(bus);
      TILES.register(bus);
   }
}
