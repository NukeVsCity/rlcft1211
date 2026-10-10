package com.nukevscity.rlgen;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.world.LegacyBiomeSource;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.util.ColorRGBA;

@Mod(RLGen.MODID)
public final class RLGen {
   public static final String MODID = "rlgen";

   private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
      DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID);
   private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
      DeferredRegister.create(Registries.BIOME_SOURCE, MODID);
   private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
   private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

   // Traverse and Defiled Lands terrain blocks (plain blocks for now; the mods' behaviour comes later).
   public static final DeferredBlock<Block> RED_ROCK = stone("red_rock", MapColor.COLOR_ORANGE);
   public static final DeferredBlock<Block> BLUE_ROCK = stone("blue_rock", MapColor.COLOR_LIGHT_BLUE);
   public static final DeferredBlock<Block> STONE_DEFILED = stone("stone_defiled", MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> GRASS_DEFILED = BLOCKS.registerSimpleBlock(
      "grass_defiled", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.6F).sound(SoundType.GRASS));
   public static final DeferredBlock<Block> DIRT_DEFILED = BLOCKS.registerSimpleBlock(
      "dirt_defiled", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.5F).sound(SoundType.GRAVEL));
   public static final DeferredBlock<ColoredFallingBlock> SAND_DEFILED = BLOCKS.registerBlock(
      "sand_defiled", p -> new ColoredFallingBlock(new ColorRGBA(0x4F3B5A), p),
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.5F).sound(SoundType.SAND));

   static {
      CHUNK_GENERATORS.register("legacy", () -> LegacyChunkGenerator.CODEC);
      BIOME_SOURCES.register("legacy", () -> LegacyBiomeSource.CODEC);
      for (DeferredBlock<?> b : new DeferredBlock<?>[]{RED_ROCK, BLUE_ROCK, STONE_DEFILED, GRASS_DEFILED, DIRT_DEFILED, SAND_DEFILED}) {
         ITEMS.registerSimpleBlockItem(b);
      }
   }

   private static DeferredBlock<Block> stone(String name, MapColor color) {
      return BLOCKS.registerSimpleBlock(name, BlockBehaviour.Properties.of().mapColor(color).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
   }

   public RLGen(IEventBus modBus) {
      CHUNK_GENERATORS.register(modBus);
      BIOME_SOURCES.register(modBus);
      BLOCKS.register(modBus);
      ITEMS.register(modBus);
   }
}
