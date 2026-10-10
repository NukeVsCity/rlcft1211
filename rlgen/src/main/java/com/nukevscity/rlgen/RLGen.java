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
   /** Battle Towers (AtomicStryker). */
   public static final DeferredHolder<StructureType<?>, StructureType<com.nukevscity.rlgen.towers.BattleTowerStructure>> BATTLE_TOWER =
      STRUCTURE_TYPES.register("battle_tower", () -> () -> com.nukevscity.rlgen.towers.BattleTowerStructure.CODEC);
   public static final DeferredHolder<StructurePieceType, StructurePieceType> BATTLE_TOWER_PIECE =
      STRUCTURE_PIECES.register("battle_tower", () -> com.nukevscity.rlgen.towers.BattleTowerPiece::new);
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
   public static final DeferredBlock<VilespineBlock> VILESPINE = BLOCKS.registerBlock("vilespine", p -> new VilespineBlock(p,
         s -> s.getBlock() == DIRT_DEFILED.get() || s.getBlock() == GRASS_DEFILED.get() || s.getBlock() == SAND_DEFILED.get()),
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().strength(0.1F, 0.5F).sound(SoundType.GRASS).noOcclusion());

   static {
      CHUNK_GENERATORS.register("legacy", () -> LegacyChunkGenerator.CODEC);
      BIOME_SOURCES.register("legacy", () -> LegacyBiomeSource.CODEC);
      for (DeferredBlock<?> b : new DeferredBlock<?>[]{RED_ROCK, BLUE_ROCK, STONE_DEFILED, GRASS_DEFILED, DIRT_DEFILED, SAND_DEFILED,
         FIR_LOG, TENEBRA_LOG, FIR_LEAVES, RED_AUTUMNAL_LEAVES, ORANGE_AUTUMNAL_LEAVES, YELLOW_AUTUMNAL_LEAVES, BROWN_AUTUMNAL_LEAVES, TENEBRA_LEAVES,
         GRAVEL_DEFILED, SANDSTONE_DEFILED, STONE_BRICKS_DEFILED, STONE_BRICKS_DEFILED_CRACKED, STONE_BRICKS_DEFILED_MOSSY, STONE_DEFILED_MOSSY,
         HEPHAESTITE_ORE, UMBRIUM_ORE, SCARLITE_ORE, BLUE_ROCK_COBBLESTONE, CONJURING_ALTAR, DEAD_GRASS, COLD_GRASS, BLASTEM, SCURONOTTE, VILESPINE}) {
         ITEMS.registerSimpleBlockItem(b);
      }
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
      if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
         RLGenClient.init(modBus);
      }
      STRUCTURE_PIECES.register(modBus);
      BLOCKS.register(modBus);
      ITEMS.register(modBus);
   }
}
