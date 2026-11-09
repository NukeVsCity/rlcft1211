package com.github.alexthe666.iceandfire.core;

import com.github.alexthe666.iceandfire.IafRegistries;
import com.github.alexthe666.iceandfire.block.BlockCharedPath;
import com.github.alexthe666.iceandfire.block.BlockDragonOre;
import com.github.alexthe666.iceandfire.block.BlockEggInIce;
import com.github.alexthe666.iceandfire.block.BlockElementalFlower;
import com.github.alexthe666.iceandfire.block.BlockFallingGeneric;
import com.github.alexthe666.iceandfire.block.BlockGeneric;
import com.github.alexthe666.iceandfire.block.BlockGoldPile;
import com.github.alexthe666.iceandfire.block.BlockIceSpikes;
import com.github.alexthe666.iceandfire.block.BlockJar;
import com.github.alexthe666.iceandfire.block.BlockLectern;
import com.github.alexthe666.iceandfire.block.BlockMyrmexBiolight;
import com.github.alexthe666.iceandfire.block.BlockMyrmexCocoon;
import com.github.alexthe666.iceandfire.block.BlockMyrmexConnectedResin;
import com.github.alexthe666.iceandfire.block.BlockMyrmexResin;
import com.github.alexthe666.iceandfire.block.BlockPixieHouse;
import com.github.alexthe666.iceandfire.block.BlockPodium;
import com.github.alexthe666.iceandfire.block.BlockSilverPile;
import com.github.alexthe666.iceandfire.block.IafBlockProps;
import com.github.alexthe666.iceandfire.block.IafBlockProps.Mat;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityEggInIce;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityJar;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityLectern;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityMyrmexCocoon;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityPixieHouse;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityPodium;
import com.github.alexthe666.iceandfire.inventory.ContainerLectern;
import com.github.alexthe666.iceandfire.inventory.ContainerMyrmexCocoon;
import com.github.alexthe666.iceandfire.inventory.ContainerPodium;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 1.12 ModBlocks: the same public static fields (assigned when the block registry fires, so they are usable from then on) and the same
 * registry names. Their item blocks are registered by ModItems. Harvest tools/levels are block tags, drops are loot tables.
 */
public class ModBlocks {
   public static final SoundType SOUND_TYPE_GOLD = new DeferredSoundType(1.0F, 1.0F, () -> ModSounds.GOLD_PILE_BREAK, () -> ModSounds.GOLD_PILE_STEP,
      () -> ModSounds.GOLD_PILE_BREAK, () -> ModSounds.GOLD_PILE_STEP, () -> ModSounds.GOLD_PILE_STEP);
   /** Registry names in 1.12 field order (the order of the creative tab). */
   public static final List<String> NAMES = new ArrayList<>();

   public static Block lectern;
   public static Block podium;
   public static Block fire_lily;
   public static Block frost_lily;
   public static Block goldPile;
   public static Block silverPile;
   public static Block silverOre;
   public static Block sapphireOre;
   public static Block silverBlock;
   public static Block sapphireBlock;
   public static Block charedDirt;
   public static Block charedGrass;
   public static Block charedStone;
   public static Block charedCobblestone;
   public static Block charedGravel;
   public static Block charedGrassPath;
   public static Block ash;
   public static Block frozenDirt;
   public static Block frozenGrass;
   public static Block frozenStone;
   public static Block frozenCobblestone;
   public static Block frozenGravel;
   public static Block frozenGrassPath;
   public static Block frozenSplinters;
   public static Block dragon_ice;
   public static Block dragon_ice_spikes;
   public static Block nest;
   public static Block eggInIce;
   public static Block pixieHouse;
   public static Block jar_empty;
   public static Block jar_pixie;
   public static Block myrmex_resin;
   public static Block myrmex_resin_sticky;
   public static Block desert_myrmex_cocoon;
   public static Block jungle_myrmex_cocoon;
   public static Block myrmex_desert_biolight;
   public static Block myrmex_jungle_biolight;
   public static Block myrmex_desert_resin_block;
   public static Block myrmex_jungle_resin_block;
   public static Block myrmex_desert_resin_glass;
   public static Block myrmex_jungle_resin_glass;

   private static void reg(String name, Supplier<Block> block) {
      NAMES.add(name);
      IafRegistries.BLOCKS.register(name, block);
   }

   private static SoundType s(String name) {
      return IafBlockProps.sound(name);
   }

   static {
      reg("lectern", () -> lectern = new BlockLectern(IafBlockProps.hardThenRes(Mat.WOOD, 2.0F, 5.0F, SoundType.WOOD)));
      reg("podium", () -> podium = new BlockPodium(IafBlockProps.hard(Mat.WOOD, 2.0F, SoundType.WOOD)));
      reg("fire_lily", () -> fire_lily = new BlockElementalFlower(IafBlockProps.hard(Mat.PLANTS, 0.0F, SoundType.GRASS).randomTicks(), true));
      reg("frost_lily", () -> frost_lily = new BlockElementalFlower(IafBlockProps.hard(Mat.PLANTS, 0.0F, SoundType.GRASS).randomTicks(), false));
      reg("goldpile", () -> goldPile = new BlockGoldPile(IafBlockProps.hard(Mat.GROUND, 0.3F, SOUND_TYPE_GOLD).noOcclusion()));
      reg("silverpile", () -> silverPile = new BlockSilverPile(IafBlockProps.hard(Mat.GROUND, 0.3F, SOUND_TYPE_GOLD).noOcclusion()));
      reg("silver_ore", () -> silverOre = new BlockDragonOre(IafBlockProps.resThenHard(Mat.ROCK, 3.0F, 5.0F, SoundType.STONE), "iceandfire.silverOre"));
      reg("sapphire_ore", () -> sapphireOre = new BlockDragonOre(IafBlockProps.resThenHard(Mat.ROCK, 3.0F, 5.0F, SoundType.STONE), "iceandfire.sapphireOre"));
      reg("silver_block", () -> silverBlock = new BlockGeneric(IafBlockProps.hardThenRes(Mat.IRON, 3.0F, 10.0F, SoundType.METAL), "iceandfire.silverBlock"));
      reg("sapphire_block", () -> sapphireBlock = new BlockGeneric(IafBlockProps.hardThenRes(Mat.IRON, 3.0F, 10.0F, SoundType.METAL), "iceandfire.sapphireBlock"));
      reg("chared_dirt", () -> charedDirt = new BlockGeneric(IafBlockProps.hardThenRes(Mat.GROUND, 0.5F, 0.0F, s("GROUND")), "iceandfire.charedDirt"));
      reg("chared_grass", () -> charedGrass = new BlockGeneric(IafBlockProps.hardThenRes(Mat.GRASS, 0.6F, 0.0F, s("GROUND")), "iceandfire.charedGrass"));
      reg("chared_stone", () -> charedStone = new BlockGeneric(IafBlockProps.hardThenRes(Mat.ROCK, 1.5F, 10.0F, SoundType.STONE), "iceandfire.charedStone"));
      reg("chared_cobblestone", () -> charedCobblestone = new BlockGeneric(IafBlockProps.hardThenRes(Mat.ROCK, 2.0F, 10.0F, SoundType.STONE),
         "iceandfire.charedCobblestone"));
      reg("chared_gravel", () -> charedGravel = new BlockFallingGeneric(IafBlockProps.hardThenRes(Mat.GROUND, 0.6F, 0.0F, s("GROUND")), "iceandfire.charedGravel"));
      reg("chared_grass_path", () -> charedGrassPath = new BlockCharedPath(IafBlockProps.hard(Mat.GROUND, 0.6F, s("GROUND")), true));
      reg("ash", () -> ash = new BlockFallingGeneric(IafBlockProps.hardThenRes(Mat.SAND, 0.5F, 0.0F, SoundType.SAND), "iceandfire.ash"));
      reg("frozen_dirt", () -> frozenDirt = new BlockGeneric(IafBlockProps.hardThenRes(Mat.GROUND, 0.5F, 0.0F, SoundType.GLASS).friction(0.98F), "iceandfire.frozenDirt"));
      reg("frozen_grass", () -> frozenGrass = new BlockGeneric(IafBlockProps.hardThenRes(Mat.GRASS, 0.6F, 0.0F, SoundType.GLASS).friction(0.98F), "iceandfire.frozenGrass"));
      reg("frozen_stone", () -> frozenStone = new BlockGeneric(IafBlockProps.hardThenRes(Mat.ROCK, 1.5F, 10.0F, SoundType.GLASS).friction(0.98F), "iceandfire.frozenStone"));
      reg("frozen_cobblestone", () -> frozenCobblestone = new BlockGeneric(IafBlockProps.hardThenRes(Mat.ROCK, 2.0F, 10.0F, SoundType.GLASS).friction(0.98F),
         "iceandfire.frozenCobblestone"));
      reg("frozen_gravel", () -> frozenGravel = new BlockFallingGeneric(IafBlockProps.hardThenRes(Mat.GROUND, 0.6F, 0.0F, SoundType.GLASS).friction(0.98F),
         "iceandfire.frozenGravel"));
      reg("frozen_grass_path", () -> frozenGrassPath = new BlockCharedPath(IafBlockProps.hard(Mat.GROUND, 0.6F, SoundType.GLASS).friction(0.98F), false));
      reg("frozen_splinters", () -> frozenSplinters = new BlockGeneric(IafBlockProps.hardThenRes(Mat.WOOD, 2.0F, 10.0F, SoundType.GLASS).friction(0.98F),
         "iceandfire.frozenSplinters"));
      reg("dragon_ice", () -> dragon_ice = new BlockGeneric(IafBlockProps.hardThenRes(Mat.PACKED_ICE, 0.5F, 0.0F, SoundType.GLASS).friction(0.98F),
         "iceandfire.dragon_ice", true));
      reg("dragon_ice_spikes", () -> dragon_ice_spikes = new BlockIceSpikes(IafBlockProps.hard(Mat.PACKED_ICE, 2.5F, SoundType.GLASS)));
      reg("nest", () -> nest = new BlockGeneric(IafBlockProps.hardThenRes(Mat.GRASS, 0.5F, 0.0F, s("GROUND")), "iceandfire.nest"));
      reg("egginice", () -> eggInIce = new BlockEggInIce(IafBlockProps.hard(Mat.ICE, 0.5F, SoundType.GLASS).lightLevel(st -> 0)));
      reg("pixie_house", () -> pixieHouse = new BlockPixieHouse(IafBlockProps.hardThenRes(Mat.WOOD, 2.0F, 5.0F, SoundType.WOOD)));
      reg("jar_empty", () -> jar_empty = new BlockJar(IafBlockProps.hardThenRes(Mat.GLASS, 1.0F, 2.0F, SoundType.GLASS), true));
      reg("jar_pixie", () -> jar_pixie = new BlockJar(IafBlockProps.hardThenRes(Mat.GLASS, 1.0F, 2.0F, SoundType.GLASS).lightLevel(st -> 11), false));
      reg("myrmex_resin", () -> myrmex_resin = new BlockMyrmexResin(IafBlockProps.hard(Mat.CLAY, 2.5F, s("GROUND")), false));
      reg("myrmex_resin_sticky", () -> myrmex_resin_sticky = new BlockMyrmexResin(IafBlockProps.hard(Mat.CLAY, 2.5F, s("SLIME")), true));
      reg("desert_myrmex_cocoon", () -> desert_myrmex_cocoon = new BlockMyrmexCocoon(IafBlockProps.hard(Mat.GROUND, 2.5F, s("SLIME")), false));
      reg("jungle_myrmex_cocoon", () -> jungle_myrmex_cocoon = new BlockMyrmexCocoon(IafBlockProps.hard(Mat.GROUND, 2.5F, s("SLIME")), true));
      reg("myrmex_desert_biolight", () -> myrmex_desert_biolight = new BlockMyrmexBiolight(IafBlockProps.hard(Mat.PLANTS, 0.0F, SoundType.GRASS)
         .lightLevel(st -> 9), false));
      reg("myrmex_jungle_biolight", () -> myrmex_jungle_biolight = new BlockMyrmexBiolight(IafBlockProps.hard(Mat.PLANTS, 0.0F, SoundType.GRASS)
         .lightLevel(st -> 9), true));
      reg("myrmex_desert_resin_block", () -> myrmex_desert_resin_block = new BlockMyrmexConnectedResin(IafBlockProps.hard(Mat.ROCK, 3.5F, SoundType.STONE), false, false));
      reg("myrmex_jungle_resin_block", () -> myrmex_jungle_resin_block = new BlockMyrmexConnectedResin(IafBlockProps.hard(Mat.ROCK, 3.5F, SoundType.STONE), true, false));
      reg("myrmex_desert_resin_glass", () -> myrmex_desert_resin_glass = new BlockMyrmexConnectedResin(IafBlockProps.hard(Mat.ROCK, 1.5F, SoundType.GLASS), false, true));
      reg("myrmex_jungle_resin_glass", () -> myrmex_jungle_resin_glass = new BlockMyrmexConnectedResin(IafBlockProps.hard(Mat.ROCK, 1.5F, SoundType.GLASS), true, true));
   }

   // ---- tile entities (1.12 GameRegistry.registerTileEntity) ----
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityLectern>> LECTERN_TILE = IafRegistries.BLOCK_ENTITY_TYPES.register("lectern",
      () -> BlockEntityType.Builder.of(TileEntityLectern::new, lectern).build(null));
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityPodium>> PODIUM_TILE = IafRegistries.BLOCK_ENTITY_TYPES.register("podium",
      () -> BlockEntityType.Builder.of(TileEntityPodium::new, podium).build(null));
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityEggInIce>> EGG_IN_ICE_TILE = IafRegistries.BLOCK_ENTITY_TYPES.register("egginice",
      () -> BlockEntityType.Builder.of(TileEntityEggInIce::new, eggInIce).build(null));
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityJar>> JAR_TILE = IafRegistries.BLOCK_ENTITY_TYPES.register("jar",
      () -> BlockEntityType.Builder.of(TileEntityJar::new, jar_empty, jar_pixie).build(null));
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityPixieHouse>> PIXIE_HOUSE_TILE = IafRegistries.BLOCK_ENTITY_TYPES.register("pixie_house",
      () -> BlockEntityType.Builder.of(TileEntityPixieHouse::new, pixieHouse).build(null));
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMyrmexCocoon>> MYRMEX_COCOON_TILE = IafRegistries.BLOCK_ENTITY_TYPES.register("myrmex_cocoon",
      () -> BlockEntityType.Builder.of(TileEntityMyrmexCocoon::new, desert_myrmex_cocoon, jungle_myrmex_cocoon).build(null));

   // ---- menus (1.12 GuiHandler ids 1 podium, 2 lectern, 6 cocoon) ----
   public static final DeferredHolder<MenuType<?>, MenuType<ContainerLectern>> LECTERN_MENU = IafRegistries.MENUS.register("lectern",
      () -> new MenuType<>(ContainerLectern::new, FeatureFlags.DEFAULT_FLAGS));
   public static final DeferredHolder<MenuType<?>, MenuType<ContainerPodium>> PODIUM_MENU = IafRegistries.MENUS.register("podium",
      () -> new MenuType<>(ContainerPodium::new, FeatureFlags.DEFAULT_FLAGS));
   public static final DeferredHolder<MenuType<?>, MenuType<ContainerMyrmexCocoon>> MYRMEX_COCOON_MENU = IafRegistries.MENUS.register("myrmex_cocoon",
      () -> new MenuType<>(ContainerMyrmexCocoon::new, FeatureFlags.DEFAULT_FLAGS));

   /** Loads the class so the blocks are added to the register. */
   public static void init() {
   }
}
