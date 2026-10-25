package com.nukevscity.rlgen;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.world.LegacyBiomeSource;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import com.nukevscity.rlgen.world.RcCommand;
import com.nukevscity.rlgen.deco.LegacyDecorationFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.nukevscity.rlgen.world.RcPiece;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import com.nukevscity.rlgen.world.RcStructure;
import com.nukevscity.rlgen.world.LegacyVillageStructure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
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
import net.minecraft.tags.BlockTags;
import com.nukevscity.rlgen.block.LegacyPlantBlock;
import com.nukevscity.rlgen.block.VilespineBlock;

@Mod(RLGen.MODID)
public final class RLGen {
   public static final String MODID = "rlgen";

   private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
      DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID);
   private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
      DeferredRegister.create(Registries.BIOME_SOURCE, MODID);
   private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, MODID);
   private static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, MODID);
   /** Recurrent Complex natural generation (data/rlgen/rc). */
   public static final DeferredHolder<StructureType<?>, StructureType<RcStructure>> RC_STRUCTURE =
      STRUCTURE_TYPES.register("recurrent_complex", () -> () -> RcStructure.CODEC);
   public static final DeferredHolder<StructurePieceType, StructurePieceType> RC_PIECE =
      STRUCTURE_PIECES.register("recurrent_complex", () -> RcPiece::new);
   /** Roguelike Dungeons (Fnar's edition 2.4.4 with RLCraft's settings). */
   public static final DeferredHolder<StructureType<?>, StructureType<com.nukevscity.rlgen.roguelike.RoguelikeStructure>> ROGUELIKE =
      STRUCTURE_TYPES.register("roguelike", () -> () -> com.nukevscity.rlgen.roguelike.RoguelikeStructure.CODEC);
   public static final DeferredHolder<StructurePieceType, StructurePieceType> ROGUELIKE_PIECE =
      STRUCTURE_PIECES.register("roguelike", () -> com.nukevscity.rlgen.roguelike.RoguelikePiece::new);
   /** Doomlike Dungeons (JaredBGreat 1.14.12 with RLCraft's themes). */
   public static final DeferredHolder<StructureType<?>, StructureType<com.nukevscity.rlgen.doomlike.DoomlikeStructure>> DOOMLIKE =
      STRUCTURE_TYPES.register("doomlike", () -> () -> com.nukevscity.rlgen.doomlike.DoomlikeStructure.CODEC);
   public static final DeferredHolder<StructurePieceType, StructurePieceType> DOOMLIKE_PIECE =
      STRUCTURE_PIECES.register("doomlike", () -> com.nukevscity.rlgen.doomlike.DoomlikePiece::new);
   /** Ruins (AtomicStryker) with RLCraft's templates. */
   public static final DeferredHolder<StructureType<?>, StructureType<com.nukevscity.rlgen.ruins.RuinsStructure>> RUINS =
      STRUCTURE_TYPES.register("ruins", () -> () -> com.nukevscity.rlgen.ruins.RuinsStructure.CODEC);
   public static final DeferredHolder<StructurePieceType, StructurePieceType> RUINS_PIECE =
      STRUCTURE_PIECES.register("ruins", () -> com.nukevscity.rlgen.ruins.RuinsPiece::new);
   /** Battle Towers (AtomicStryker). */
   public static final DeferredHolder<StructureType<?>, StructureType<com.nukevscity.rlgen.towers.BattleTowerStructure>> BATTLE_TOWER =
      STRUCTURE_TYPES.register("battle_tower", () -> () -> com.nukevscity.rlgen.towers.BattleTowerStructure.CODEC);
   public static final DeferredHolder<StructurePieceType, StructurePieceType> BATTLE_TOWER_PIECE =
      STRUCTURE_PIECES.register("battle_tower", () -> com.nukevscity.rlgen.towers.BattleTowerPiece::new);
   /** YUNG's Better Mineshafts 2.2.1 (replaced 1.12's mineshafts). */
   public static final DeferredHolder<StructureType<?>, StructureType<com.nukevscity.rlgen.bms.MineshaftStructure>> MINESHAFT =
      STRUCTURE_TYPES.register("better_mineshaft", () -> () -> com.nukevscity.rlgen.bms.MineshaftStructure.CODEC);
   public static final DeferredHolder<StructurePieceType, StructurePieceType> MINESHAFT_PIECE =
      STRUCTURE_PIECES.register("better_mineshaft", () -> com.nukevscity.rlgen.bms.MineshaftHolderPiece::new);
   /** Lycanites Mobs dungeons. */
   public static final DeferredHolder<StructureType<?>, StructureType<com.nukevscity.rlgen.lyc.dungeon.LycDungeonStructure>> LYC_DUNGEON =
      STRUCTURE_TYPES.register("lycanites_dungeon", () -> () -> com.nukevscity.rlgen.lyc.dungeon.LycDungeonStructure.CODEC);
   public static final DeferredHolder<StructurePieceType, StructurePieceType> LYC_DUNGEON_PIECE =
      STRUCTURE_PIECES.register("lycanites_dungeon", () -> com.nukevscity.rlgen.lyc.dungeon.LycDungeonPiece::new);
   /** 1.12 MapGenVillage (with Recurrent Complex village pieces). */
   public static final DeferredHolder<StructureType<?>, StructureType<LegacyVillageStructure>> VILLAGE_STRUCTURE =
      STRUCTURE_TYPES.register("village", () -> () -> LegacyVillageStructure.CODEC);
   public static final DeferredHolder<StructurePieceType, StructurePieceType> VILLAGE_PIECE =
      STRUCTURE_PIECES.register("village", () -> com.nukevscity.rlgen.village.VillagePieces::load);
   private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MODID);
   static {
      FEATURES.register("legacy_lakes", () -> new LegacyDecorationFeature(LegacyDecorationFeature.Step.LAKES));
      FEATURES.register("legacy_ores", () -> new LegacyDecorationFeature(LegacyDecorationFeature.Step.ORES));
      FEATURES.register("legacy_plants", () -> new LegacyDecorationFeature(LegacyDecorationFeature.Step.PLANTS));
   }
   private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
   private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

   // Traverse and Defiled Lands terrain blocks (plain blocks for now; the mods' behaviour comes later).
   public static final DeferredBlock<Block> RED_ROCK = stone("red_rock", MapColor.COLOR_ORANGE);
   public static final DeferredBlock<Block> BLUE_ROCK = stone("blue_rock", MapColor.COLOR_LIGHT_BLUE);
   /** No Tree Punching loose rocks, by stone (andesite... limestone as Quark compat was on in RLCraft). */
   public static final java.util.Map<String, DeferredBlock<com.nukevscity.rlgen.ntp.LooseRockBlock>> LOOSE_ROCKS = new java.util.LinkedHashMap<>();
   static {
      for (String s : new String[]{"stone", "andesite", "granite", "diorite", "sandstone", "red_sandstone", "marble", "limestone"}) {
         LOOSE_ROCKS.put(s, BLOCKS.registerBlock("loose_rock_" + s, com.nukevscity.rlgen.ntp.LooseRockBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).noCollission().instabreak().sound(SoundType.STONE).noOcclusion()
               .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
      }
   }
   /** Coral Reef 2.5: reef rock and six corals (light 15, underwater). */
   public static final DeferredBlock<com.nukevscity.rlgen.coral.ReefBlock> REEF = BLOCKS.registerBlock("reef", com.nukevscity.rlgen.coral.ReefBlock::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).requiresCorrectToolForDrops().strength(1.5F, 10.0F).sound(SoundType.STONE));
   public static final DeferredBlock<com.nukevscity.rlgen.coral.ReefBlock> REEF_ROCK = BLOCKS.registerBlock("reef_rock", com.nukevscity.rlgen.coral.ReefBlock::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).requiresCorrectToolForDrops().strength(1.5F, 10.0F).sound(SoundType.STONE));
   public static final java.util.List<DeferredBlock<com.nukevscity.rlgen.coral.CoralBlock>> CORALS = new java.util.ArrayList<>();
   static {
      for (int i = 0; i < 6; i++) {
         boolean high = i > 3;
         CORALS.add(BLOCKS.registerBlock("coral_" + (i + 1), p -> new com.nukevscity.rlgen.coral.CoralBlock(p, high),
            BlockBehaviour.Properties.of().mapColor(MapColor.WATER).noCollission().instabreak().sound(SoundType.WET_GRASS).lightLevel(s -> 15)
               .noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
      }
   }
   // Ice and Fire 1.7.1 world-gen blocks (dragon roosts and dens, cyclops caves, ores, lilies).
   private static BlockBehaviour.Properties iaf(MapColor color, float hardness, float resistance, SoundType sound, boolean frozen) {
      BlockBehaviour.Properties p = BlockBehaviour.Properties.of().mapColor(color).strength(hardness, resistance).sound(sound);
      return frozen ? p.friction(0.98F) : p;
   }
   public static final DeferredBlock<ColoredFallingBlock> ASH = BLOCKS.registerBlock("ash", p -> new ColoredFallingBlock(new ColorRGBA(0x5B5B5B), p),
      iaf(MapColor.COLOR_GRAY, 0.5F, 0.5F, SoundType.SAND, false));
   public static final DeferredBlock<Block> CHARED_DIRT = BLOCKS.registerSimpleBlock("chared_dirt", iaf(MapColor.COLOR_BLACK, 0.5F, 0.0F, SoundType.GRAVEL, false));
   public static final DeferredBlock<Block> CHARED_GRASS = BLOCKS.registerSimpleBlock("chared_grass", iaf(MapColor.COLOR_BLACK, 0.6F, 0.0F, SoundType.GRAVEL, false));
   public static final DeferredBlock<Block> CHARED_STONE = BLOCKS.registerSimpleBlock("chared_stone",
      iaf(MapColor.COLOR_BLACK, 1.5F, 10.0F, SoundType.STONE, false).requiresCorrectToolForDrops());
   public static final DeferredBlock<Block> CHARED_COBBLESTONE = BLOCKS.registerSimpleBlock("chared_cobblestone",
      iaf(MapColor.COLOR_BLACK, 2.0F, 10.0F, SoundType.STONE, false).requiresCorrectToolForDrops());
   public static final DeferredBlock<ColoredFallingBlock> CHARED_GRAVEL = BLOCKS.registerBlock("chared_gravel",
      p -> new ColoredFallingBlock(new ColorRGBA(0x333333), p), iaf(MapColor.COLOR_BLACK, 0.6F, 0.0F, SoundType.GRAVEL, false));
   public static final DeferredBlock<com.nukevscity.rlgen.iaf.PathBlock> CHARED_GRASS_PATH = BLOCKS.registerBlock("chared_grass_path",
      com.nukevscity.rlgen.iaf.PathBlock::new, iaf(MapColor.COLOR_BLACK, 0.6F, 0.6F, SoundType.GRAVEL, false));
   public static final DeferredBlock<Block> FROZEN_DIRT = BLOCKS.registerSimpleBlock("frozen_dirt", iaf(MapColor.ICE, 0.5F, 0.0F, SoundType.GLASS, true));
   public static final DeferredBlock<Block> FROZEN_GRASS = BLOCKS.registerSimpleBlock("frozen_grass", iaf(MapColor.ICE, 0.6F, 0.0F, SoundType.GLASS, true));
   public static final DeferredBlock<Block> FROZEN_STONE = BLOCKS.registerSimpleBlock("frozen_stone",
      iaf(MapColor.ICE, 1.5F, 10.0F, SoundType.GLASS, true).requiresCorrectToolForDrops());
   public static final DeferredBlock<Block> FROZEN_COBBLESTONE = BLOCKS.registerSimpleBlock("frozen_cobblestone",
      iaf(MapColor.ICE, 2.0F, 10.0F, SoundType.GLASS, true).requiresCorrectToolForDrops());
   public static final DeferredBlock<ColoredFallingBlock> FROZEN_GRAVEL = BLOCKS.registerBlock("frozen_gravel",
      p -> new ColoredFallingBlock(new ColorRGBA(0x9DB8C9), p), iaf(MapColor.ICE, 0.6F, 0.0F, SoundType.GLASS, true));
   public static final DeferredBlock<com.nukevscity.rlgen.iaf.PathBlock> FROZEN_GRASS_PATH = BLOCKS.registerBlock("frozen_grass_path",
      com.nukevscity.rlgen.iaf.PathBlock::new, iaf(MapColor.ICE, 0.6F, 0.6F, SoundType.GLASS, true));
   public static final DeferredBlock<Block> FROZEN_SPLINTERS = BLOCKS.registerSimpleBlock("frozen_splinters", iaf(MapColor.ICE, 2.0F, 10.0F, SoundType.GLASS, true));
   public static final DeferredBlock<com.nukevscity.rlgen.iaf.PileBlock> GOLD_PILE = BLOCKS.registerBlock("gold_pile", com.nukevscity.rlgen.iaf.PileBlock::new,
      iaf(MapColor.GOLD, 0.3F, 0.3F, SoundType.CHAIN, false).noOcclusion());
   public static final DeferredBlock<com.nukevscity.rlgen.iaf.PileBlock> SILVER_PILE = BLOCKS.registerBlock("silver_pile", com.nukevscity.rlgen.iaf.PileBlock::new,
      iaf(MapColor.METAL, 0.3F, 0.3F, SoundType.CHAIN, false).noOcclusion());
   public static final DeferredBlock<Block> SILVER_ORE = BLOCKS.registerSimpleBlock("silver_ore",
      iaf(MapColor.STONE, 3.0F, 5.0F, SoundType.STONE, false).requiresCorrectToolForDrops());
   public static final DeferredBlock<Block> SAPPHIRE_ORE = BLOCKS.registerSimpleBlock("sapphire_ore",
      iaf(MapColor.STONE, 3.0F, 5.0F, SoundType.STONE, false).requiresCorrectToolForDrops());
   public static final DeferredBlock<com.nukevscity.rlgen.iaf.LilyBlock> FIRE_LILY = BLOCKS.registerBlock("fire_lily",
      p -> new com.nukevscity.rlgen.iaf.LilyBlock(p, true), BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY));
   public static final DeferredBlock<com.nukevscity.rlgen.iaf.LilyBlock> FROST_LILY = BLOCKS.registerBlock("frost_lily",
      p -> new com.nukevscity.rlgen.iaf.LilyBlock(p, false), BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY));
   private static BlockBehaviour.Properties resin(boolean sticky) {
      BlockBehaviour.Properties p = BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(2.5F).sound(sticky ? SoundType.SLIME_BLOCK : SoundType.GRAVEL);
      return sticky ? p.speedFactor(0.4F) : p;
   }
   public static final DeferredBlock<Block> DESERT_MYRMEX_RESIN = BLOCKS.registerSimpleBlock("desert_myrmex_resin", resin(false));
   public static final DeferredBlock<Block> JUNGLE_MYRMEX_RESIN = BLOCKS.registerSimpleBlock("jungle_myrmex_resin", resin(false));
   public static final DeferredBlock<Block> DESERT_MYRMEX_RESIN_STICKY = BLOCKS.registerSimpleBlock("desert_myrmex_resin_sticky", resin(true));
   public static final DeferredBlock<Block> JUNGLE_MYRMEX_RESIN_STICKY = BLOCKS.registerSimpleBlock("jungle_myrmex_resin_sticky", resin(true));
   public static final DeferredBlock<com.nukevscity.rlgen.iaf.BiolightBlock> MYRMEX_DESERT_BIOLIGHT = BLOCKS.registerBlock("myrmex_desert_biolight",
      com.nukevscity.rlgen.iaf.BiolightBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).lightLevel(s -> 9).noOcclusion());
   public static final DeferredBlock<com.nukevscity.rlgen.iaf.BiolightBlock> MYRMEX_JUNGLE_BIOLIGHT = BLOCKS.registerBlock("myrmex_jungle_biolight",
      com.nukevscity.rlgen.iaf.BiolightBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).lightLevel(s -> 9).noOcclusion());
   public static final DeferredBlock<com.nukevscity.rlgen.iaf.CocoonBlock> DESERT_MYRMEX_COCOON = BLOCKS.registerBlock("desert_myrmex_cocoon",
      com.nukevscity.rlgen.iaf.CocoonBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(2.5F).sound(SoundType.SLIME_BLOCK).noOcclusion());
   public static final DeferredBlock<com.nukevscity.rlgen.iaf.CocoonBlock> JUNGLE_MYRMEX_COCOON = BLOCKS.registerBlock("jungle_myrmex_cocoon",
      com.nukevscity.rlgen.iaf.CocoonBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(2.5F).sound(SoundType.SLIME_BLOCK).noOcclusion());
   public static final DeferredBlock<com.nukevscity.rlgen.iaf.PixieHouseBlock> PIXIE_HOUSE = BLOCKS.registerBlock("pixie_house",
      com.nukevscity.rlgen.iaf.PixieHouseBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(2.0F).sound(SoundType.WOOD).noOcclusion());
   private static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
   public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>, net.minecraft.world.level.block.entity.BlockEntityType<com.nukevscity.rlgen.iaf.CocoonBlockEntity>> COCOON_BE =
      BLOCK_ENTITIES.register("myrmex_cocoon", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(
         com.nukevscity.rlgen.iaf.CocoonBlockEntity::new, DESERT_MYRMEX_COCOON.get(), JUNGLE_MYRMEX_COCOON.get()).build(null));
   /** Bountiful's bounty board (decorative; the village piece uses Bountiful's own board when that mod is installed). */
   public static final DeferredBlock<Block> BOUNTY_BOARD = BLOCKS.registerSimpleBlock("bounty_board",
      BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD));
   /** Charm crates and sealed crates (oak, spruce, birch, jungle, acacia, dark oak). */
   public static final java.util.List<DeferredBlock<com.nukevscity.rlgen.charm.CrateBlock>> CRATES = new java.util.ArrayList<>();
   public static final java.util.List<DeferredBlock<com.nukevscity.rlgen.charm.CrateBlock>> SEALED_CRATES = new java.util.ArrayList<>();
   static {
      for (String w : new String[]{"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"}) {
         CRATES.add(BLOCKS.registerBlock("crate_" + w, p -> new com.nukevscity.rlgen.charm.CrateBlock(false, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
         SEALED_CRATES.add(BLOCKS.registerBlock("crate_sealed_" + w, p -> new com.nukevscity.rlgen.charm.CrateBlock(true, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
      }
   }
   public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>, net.minecraft.world.level.block.entity.BlockEntityType<com.nukevscity.rlgen.charm.CrateBlockEntity>> CRATE_BE =
      BLOCK_ENTITIES.register("crate", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(
         com.nukevscity.rlgen.charm.CrateBlockEntity::new, java.util.stream.Stream.concat(CRATES.stream(), SEALED_CRATES.stream()).map(DeferredBlock::get)
            .toArray(net.minecraft.world.level.block.Block[]::new)).build(null));
   // Quark r1.6 world-gen blocks (crystal caves, speleothems, cave roots, monster boxes, underground biomes, blossom trees)
   public static final java.util.List<DeferredBlock<net.minecraft.world.level.block.HalfTransparentBlock>> CRYSTALS = new java.util.ArrayList<>();
   static {
      for (String c : new String[]{"white", "red", "orange", "yellow", "green", "blue", "indigo", "violet", "black"}) {
         CRYSTALS.add(BLOCKS.registerBlock("crystal_" + c, net.minecraft.world.level.block.HalfTransparentBlock::new,
            BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.GLASS).lightLevel(s -> 11).noOcclusion()
               .isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)));
      }
   }
   public static final java.util.Map<String, DeferredBlock<com.nukevscity.rlgen.quark.SpeleothemBlock>> SPELEOTHEMS = new java.util.LinkedHashMap<>();
   static {
      for (String s : new String[]{"stone", "granite", "diorite", "andesite", "marble", "limestone", "jasper", "slate"}) {
         SPELEOTHEMS.put(s, BLOCKS.registerBlock(s + "_speleothem", com.nukevscity.rlgen.quark.SpeleothemBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(1.5F).sound(SoundType.STONE).noOcclusion()));
      }
   }
   private static BlockBehaviour.Properties roots() {
      return BlockBehaviour.Properties.ofFullCopy(Blocks.VINE).mapColor(MapColor.DIRT);
   }
   public static final DeferredBlock<com.nukevscity.rlgen.quark.RootsBlock> ROOTS = BLOCKS.registerBlock("roots", com.nukevscity.rlgen.quark.RootsBlock::new, roots());
   public static final DeferredBlock<com.nukevscity.rlgen.quark.RootsBlock> ROOTS_BLUE_FLOWER = BLOCKS.registerBlock("roots_blue_flower",
      com.nukevscity.rlgen.quark.RootsBlock::new, roots());
   public static final DeferredBlock<com.nukevscity.rlgen.quark.RootsBlock> ROOTS_BLACK_FLOWER = BLOCKS.registerBlock("roots_black_flower",
      com.nukevscity.rlgen.quark.RootsBlock::new, roots());
   public static final DeferredBlock<com.nukevscity.rlgen.quark.RootsBlock> ROOTS_WHITE_FLOWER = BLOCKS.registerBlock("roots_white_flower",
      com.nukevscity.rlgen.quark.RootsBlock::new, roots());
   public static final DeferredBlock<Block> MONSTER_BOX = BLOCKS.registerSimpleBlock("monster_box",
      BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(25.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
   public static final DeferredBlock<Block> FIRE_STONE = stone("fire_stone", MapColor.COLOR_RED);
   public static final DeferredBlock<Block> ICY_STONE = stone("icy_stone", MapColor.ICE);
   public static final DeferredBlock<Block> COBBED_STONE = stone("cobbed_stone", MapColor.STONE);
   public static final DeferredBlock<Block> ELDER_PRISMARINE = stone("elder_prismarine", MapColor.COLOR_LIGHT_GRAY);
   public static final DeferredBlock<Block> ELDER_SEA_LANTERN = BLOCKS.registerSimpleBlock("elder_sea_lantern",
      BlockBehaviour.Properties.ofFullCopy(Blocks.SEA_LANTERN));
   public static final DeferredBlock<Block> GLOWCELIUM = BLOCKS.registerSimpleBlock("glowcelium",
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.2F).sound(SoundType.GRASS).lightLevel(s -> 7));
   public static final DeferredBlock<com.nukevscity.rlgen.quark.GlowshroomBlock> GLOWSHROOM = BLOCKS.registerBlock("glowshroom",
      com.nukevscity.rlgen.quark.GlowshroomBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM).lightLevel(s -> 14));
   public static final DeferredBlock<LeavesBlock> SAKURA_LEAVES = leaves("sakura_leaves");
   /** Modded building blocks from RLCraft's structures (data/rlgen/mod_blocks.json, made by scripts/gen_mod_blocks.py). */
   public static final java.util.Map<String, DeferredBlock<Block>> MOD_BLOCKS = new java.util.LinkedHashMap<>();
   static {
      try (java.io.Reader r = new java.io.InputStreamReader(RLGen.class.getResourceAsStream("/data/rlgen/mod_blocks.json"), java.nio.charset.StandardCharsets.UTF_8)) {
         for (com.google.gson.JsonElement e : com.google.gson.JsonParser.parseReader(r).getAsJsonArray()) {
            com.google.gson.JsonObject o = e.getAsJsonObject();
            String name = o.get("name").getAsString();
            String type = o.get("type").getAsString();
            net.minecraft.resources.ResourceLocation like = net.minecraft.resources.ResourceLocation.withDefaultNamespace(o.get("like").getAsString());
            java.util.function.Supplier<Block> base = () -> net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(like);
            java.util.function.Function<BlockBehaviour.Properties, Block> make = switch (type) {
               case "stairs" -> p -> new net.minecraft.world.level.block.StairBlock(base.get().defaultBlockState(), p);
               case "slab" -> net.minecraft.world.level.block.SlabBlock::new;
               case "wall" -> net.minecraft.world.level.block.WallBlock::new;
               case "fence" -> net.minecraft.world.level.block.FenceBlock::new;
               case "pane" -> net.minecraft.world.level.block.IronBarsBlock::new;
               case "ladder" -> net.minecraft.world.level.block.LadderBlock::new;
               case "pillar" -> RotatedPillarBlock::new;
               default -> Block::new;
            };
            MOD_BLOCKS.put(name, BLOCKS.register(name, () -> make.apply(BlockBehaviour.Properties.ofFullCopy(base.get()))));
         }
      } catch (java.io.IOException | RuntimeException ex) {
         throw new IllegalStateException("rlgen: could not read mod_blocks.json", ex);
      }
   }
   // Quark's Revamp stone gen stones (generation only; their crafting variants are not ported).
   public static final DeferredBlock<Block> MARBLE = stone("marble", MapColor.QUARTZ);
   public static final DeferredBlock<Block> LIMESTONE = stone("limestone", MapColor.STONE);
   public static final DeferredBlock<Block> JASPER = stone("jasper", MapColor.TERRACOTTA_RED);
   public static final DeferredBlock<Block> SLATE = stone("slate", MapColor.COLOR_GRAY);
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
   // Rustic olive and ironwood (Dynamic Trees species in 1.12 through Rustic's DT compat)
   public static final DeferredBlock<RotatedPillarBlock> OLIVE_LOG = log("olive_log", MapColor.WOOD);
   public static final DeferredBlock<RotatedPillarBlock> IRONWOOD_LOG = log("ironwood_log", MapColor.COLOR_GRAY);
   public static final DeferredBlock<LeavesBlock> OLIVE_LEAVES = leaves("olive_leaves");
   public static final DeferredBlock<LeavesBlock> IRONWOOD_LEAVES = leaves("ironwood_leaves");

   // Defiled Lands / Traverse blocks used by their 1.12 decoration
   public static final DeferredBlock<ColoredFallingBlock> GRAVEL_DEFILED = BLOCKS.registerBlock(
      "gravel_defiled", p -> new ColoredFallingBlock(new ColorRGBA(0x3D3247), p), BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL).mapColor(MapColor.COLOR_PURPLE));
   public static final DeferredBlock<Block> SANDSTONE_DEFILED = stone("sandstone_defiled", MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> STONE_BRICKS_DEFILED = stone("stone_bricks_defiled", MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> STONE_BRICKS_DEFILED_CRACKED = stone("stone_bricks_defiled_cracked", MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> STONE_BRICKS_DEFILED_MOSSY = stone("stone_bricks_defiled_mossy", MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> STONE_DEFILED_MOSSY = stone("stone_defiled_mossy", MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> HEPHAESTITE_ORE = stone("hephaestite_ore", MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> UMBRIUM_ORE = stone("umbrium_ore", MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> SCARLITE_ORE = stone("scarlite_ore", MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> BLUE_ROCK_COBBLESTONE = stone("blue_rock_cobblestone", MapColor.COLOR_LIGHT_BLUE);
   public static final DeferredBlock<Block> CONJURING_ALTAR = BLOCKS.registerBlock("conjuring_altar", p -> new Block(p) {
      private static final net.minecraft.world.phys.shapes.VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

      @Override
      protected net.minecraft.world.phys.shapes.VoxelShape getShape(net.minecraft.world.level.block.state.BlockState s, net.minecraft.world.level.BlockGetter l,
                                                                  net.minecraft.core.BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext c) {
         return SHAPE;
      }
   }, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.COLOR_PURPLE).noOcclusion());
   public static final DeferredBlock<LegacyPlantBlock> DEAD_GRASS = plant("dead_grass", s -> s.is(BlockTags.DIRT) || s.is(BlockTags.SAND)
      || s.is(BlockTags.TERRACOTTA) || s.getBlock() == RED_ROCK.get() || s.getBlock() == BLUE_ROCK.get(), Block.box(2, 0, 2, 14, 13, 14));
   public static final DeferredBlock<LegacyPlantBlock> COLD_GRASS = plant("cold_grass", s -> s.is(BlockTags.DIRT) || s.is(Blocks.SNOW_BLOCK)
      || s.getBlock() == RED_ROCK.get() || s.getBlock() == BLUE_ROCK.get() || s.is(Blocks.STONE), Block.box(2, 0, 2, 14, 13, 14));
   public static final DeferredBlock<LegacyPlantBlock> BLASTEM = plant("blastem", s -> s.getBlock() == DIRT_DEFILED.get() || s.getBlock() == GRASS_DEFILED.get(),
      Block.box(2, 0, 2, 14, 14, 14));
   public static final DeferredBlock<LegacyPlantBlock> SCURONOTTE = plant("scuronotte", s -> s.isSolid() && !s.is(Blocks.ICE),
      Block.box(4, 0, 4, 12, 8.4, 12));
   /** Rustic 1.1.7 herbs (fully grown, as its world gen places them) with their 1.12 EnumPlantType soil: Desert, Plains or Cave. */
   public static final java.util.Map<String, DeferredBlock<LegacyPlantBlock>> RUSTIC_HERBS = new java.util.LinkedHashMap<>();
   public static final DeferredBlock<com.nukevscity.rlgen.rustic.WildberryBushBlock> WILDBERRY_BUSH = BLOCKS.registerBlock("wildberry_bush",
      com.nukevscity.rlgen.rustic.WildberryBushBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak()
         .sound(SoundType.GRASS).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));

   static {
      java.util.function.Predicate<net.minecraft.world.level.block.state.BlockState> desert = s -> s.is(BlockTags.SAND) || s.is(Blocks.TERRACOTTA)
         || s.is(BlockTags.TERRACOTTA);
      java.util.function.Predicate<net.minecraft.world.level.block.state.BlockState> plains = s -> s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT)
         || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.FARMLAND);
      java.util.function.Predicate<net.minecraft.world.level.block.state.BlockState> cave = s -> s.isSolid();
      for (String n : new String[]{"aloe_vera", "blood_orchid", "chamomile", "cloudsbluff", "cohosh", "core_root", "ginseng", "horsetail",
         "marsh_mallow", "mooncap_mushroom", "wind_thistle"}) {
         var soil = n.equals("aloe_vera") ? desert : n.equals("core_root") || n.equals("mooncap_mushroom") ? cave : plains;
         RUSTIC_HERBS.put(n, BLOCKS.registerBlock(n, p -> new LegacyPlantBlock(p, soil, Block.box(2, 0, 2, 14, 14, 14)),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.CROP)
               .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
      }
   }
   public static final DeferredBlock<VilespineBlock> VILESPINE = BLOCKS.registerBlock("vilespine", p -> new VilespineBlock(p,
         s -> s.getBlock() == DIRT_DEFILED.get() || s.getBlock() == GRASS_DEFILED.get() || s.getBlock() == SAND_DEFILED.get()),
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().strength(0.1F, 0.5F).sound(SoundType.GRASS).noOcclusion());

   static {
      CHUNK_GENERATORS.register("legacy", () -> LegacyChunkGenerator.CODEC);
      BIOME_SOURCES.register("legacy", () -> LegacyBiomeSource.CODEC);
      for (DeferredBlock<?> b : new DeferredBlock<?>[]{RED_ROCK, BLUE_ROCK, STONE_DEFILED, GRASS_DEFILED, DIRT_DEFILED, SAND_DEFILED,
         FIR_LOG, TENEBRA_LOG, FIR_LEAVES, RED_AUTUMNAL_LEAVES, ORANGE_AUTUMNAL_LEAVES, YELLOW_AUTUMNAL_LEAVES, BROWN_AUTUMNAL_LEAVES, TENEBRA_LEAVES,
         OLIVE_LOG, IRONWOOD_LOG, OLIVE_LEAVES, IRONWOOD_LEAVES,
         GRAVEL_DEFILED, SANDSTONE_DEFILED, STONE_BRICKS_DEFILED, STONE_BRICKS_DEFILED_CRACKED, STONE_BRICKS_DEFILED_MOSSY, STONE_DEFILED_MOSSY,
         HEPHAESTITE_ORE, UMBRIUM_ORE, SCARLITE_ORE, MARBLE, LIMESTONE, JASPER, SLATE, BLUE_ROCK_COBBLESTONE, CONJURING_ALTAR, DEAD_GRASS, COLD_GRASS, BLASTEM, SCURONOTTE, VILESPINE,
         ASH, CHARED_DIRT, CHARED_GRASS, CHARED_STONE, CHARED_COBBLESTONE, CHARED_GRAVEL, CHARED_GRASS_PATH, FROZEN_DIRT, FROZEN_GRASS, FROZEN_STONE,
         FROZEN_COBBLESTONE, FROZEN_GRAVEL, FROZEN_GRASS_PATH, FROZEN_SPLINTERS, GOLD_PILE, SILVER_PILE, SILVER_ORE, SAPPHIRE_ORE, FIRE_LILY, FROST_LILY,
         DESERT_MYRMEX_RESIN, JUNGLE_MYRMEX_RESIN, DESERT_MYRMEX_RESIN_STICKY, JUNGLE_MYRMEX_RESIN_STICKY, MYRMEX_DESERT_BIOLIGHT, MYRMEX_JUNGLE_BIOLIGHT,
         DESERT_MYRMEX_COCOON, JUNGLE_MYRMEX_COCOON, PIXIE_HOUSE,
         ROOTS, ROOTS_BLUE_FLOWER, ROOTS_BLACK_FLOWER, ROOTS_WHITE_FLOWER, MONSTER_BOX, FIRE_STONE, ICY_STONE, COBBED_STONE, ELDER_PRISMARINE, ELDER_SEA_LANTERN,
         GLOWCELIUM, GLOWSHROOM, SAKURA_LEAVES}) {
         ITEMS.registerSimpleBlockItem(b);
      }
      LOOSE_ROCKS.values().forEach(ITEMS::registerSimpleBlockItem);
      ITEMS.registerSimpleBlockItem(REEF);
      ITEMS.registerSimpleBlockItem(REEF_ROCK);
      CORALS.forEach(ITEMS::registerSimpleBlockItem);
      CRYSTALS.forEach(ITEMS::registerSimpleBlockItem);
      CRATES.forEach(ITEMS::registerSimpleBlockItem);
      ITEMS.registerSimpleBlockItem(BOUNTY_BOARD);
      ITEMS.registerSimpleBlockItem(WILDBERRY_BUSH);
      RUSTIC_HERBS.values().forEach(ITEMS::registerSimpleBlockItem);
      SEALED_CRATES.forEach(ITEMS::registerSimpleBlockItem);
      SPELEOTHEMS.values().forEach(ITEMS::registerSimpleBlockItem);
      MOD_BLOCKS.values().forEach(ITEMS::registerSimpleBlockItem);
   }

   private static DeferredBlock<LegacyPlantBlock> plant(String name, java.util.function.Predicate<net.minecraft.world.level.block.state.BlockState> soil,
                                                        net.minecraft.world.phys.shapes.VoxelShape shape) {
      return BLOCKS.registerBlock(name, p -> new LegacyPlantBlock(p, soil, shape), BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS));
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
      STRUCTURE_TYPES.register(modBus);
      FEATURES.register(modBus);
      NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> RcCommand.register(e.getDispatcher()));
      NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.LevelEvent.Load e) -> {
         if (e.getLevel() instanceof net.minecraft.server.level.ServerLevel sl && sl.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
            com.nukevscity.rlgen.ruins.RuinsSavedData.attach(sl);
            com.nukevscity.rlgen.iaf.MyrmexSavedData.attach(sl);
            com.nukevscity.rlgen.lyc.dungeon.LycDungeons.attach(sl);
         }
      });
      if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
         RLGenClient.init(modBus);
      }
      STRUCTURE_PIECES.register(modBus);
      BLOCKS.register(modBus);
      BLOCK_ENTITIES.register(modBus);
      ITEMS.register(modBus);
      com.nukevscity.rlgen.lyc.LycFluids.register(modBus);
   }
}
