package com.nukevscity.rlgen;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.world.LegacyBiomeSource;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.MapColor;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
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

   // Traverse fir / autumnal and Defiled Lands tenebra trees: primitive blocks for the Dynamic Trees species.
   public static final DeferredBlock<RotatedPillarBlock> FIR_LOG = log("fir_log", MapColor.COLOR_BROWN);
   public static final DeferredBlock<RotatedPillarBlock> TENEBRA_LOG = log("tenebra_log", MapColor.COLOR_BLACK);
   public static final DeferredBlock<LeavesBlock> FIR_LEAVES = leaves("fir_leaves");
   public static final DeferredBlock<LeavesBlock> RED_AUTUMNAL_LEAVES = leaves("red_autumnal_leaves");
   public static final DeferredBlock<LeavesBlock> ORANGE_AUTUMNAL_LEAVES = leaves("orange_autumnal_leaves");
   public static final DeferredBlock<LeavesBlock> YELLOW_AUTUMNAL_LEAVES = leaves("yellow_autumnal_leaves");
   public static final DeferredBlock<LeavesBlock> BROWN_AUTUMNAL_LEAVES = leaves("brown_autumnal_leaves");
   public static final DeferredBlock<LeavesBlock> TENEBRA_LEAVES = leaves("tenebra_leaves");

   static {
      CHUNK_GENERATORS.register("legacy", () -> LegacyChunkGenerator.CODEC);
      BIOME_SOURCES.register("legacy", () -> LegacyBiomeSource.CODEC);
      for (DeferredBlock<?> b : new DeferredBlock<?>[]{RED_ROCK, BLUE_ROCK, STONE_DEFILED, GRASS_DEFILED, DIRT_DEFILED, SAND_DEFILED,
         FIR_LOG, TENEBRA_LOG, FIR_LEAVES, RED_AUTUMNAL_LEAVES, ORANGE_AUTUMNAL_LEAVES, YELLOW_AUTUMNAL_LEAVES, BROWN_AUTUMNAL_LEAVES, TENEBRA_LEAVES}) {
         ITEMS.registerSimpleBlockItem(b);
      }
   }

   private static DeferredBlock<Block> stone(String name, MapColor color) {
      return BLOCKS.registerSimpleBlock(name, BlockBehaviour.Properties.of().mapColor(color).requiresCorrectToolForDrops().strength(1.5F, 6.0F));
   }

   private static DeferredBlock<RotatedPillarBlock> log(String name, MapColor color) {
      return BLOCKS.registerBlock(name, RotatedPillarBlock::new,
         BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LOG).mapColor(color));
   }

   private static DeferredBlock<LeavesBlock> leaves(String name) {
      return BLOCKS.registerBlock(name, LeavesBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
   }

   public RLGen(IEventBus modBus) {
      // Lets Dynamic Trees create branch/leaves blocks for the rlgen tree pack (trees/rlgen).
      NeoForgeRegistryHandler.setup(MODID, modBus);
      CHUNK_GENERATORS.register(modBus);
      BIOME_SOURCES.register(modBus);
      BLOCKS.register(modBus);
      ITEMS.register(modBus);
   }
}
