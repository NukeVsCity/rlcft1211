package com.github.fnar.minecraft.block;

import com.github.fnar.minecraft.block.decorative.AnvilBlock;
import com.github.fnar.minecraft.block.decorative.BedBlock;
import com.github.fnar.minecraft.block.decorative.CropBlock;
import com.github.fnar.minecraft.block.decorative.Plant;
import com.github.fnar.minecraft.block.decorative.PlantBlock;
import com.github.fnar.minecraft.block.decorative.PumpkinBlock;
import com.github.fnar.minecraft.block.decorative.TallPlant;
import com.github.fnar.minecraft.block.decorative.TallPlantBlock;
import com.github.fnar.minecraft.block.decorative.TorchBlock;
import com.github.fnar.minecraft.block.normal.ColoredBlock;
import com.github.fnar.minecraft.block.normal.InfestedBlock;
import com.github.fnar.minecraft.block.normal.Quartz;
import com.github.fnar.minecraft.block.normal.SlabBlock;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import com.github.fnar.minecraft.block.redstone.ComparatorBlock;
import com.github.fnar.minecraft.block.redstone.DoorBlock;
import com.github.fnar.minecraft.block.redstone.LeverBlock;
import com.github.fnar.minecraft.block.redstone.RepeaterBlock;
import com.github.fnar.minecraft.block.redstone.TrapdoorBlock;
import com.github.fnar.minecraft.block.redstone.TripwireHookBlock;
import com.github.fnar.minecraft.material.Wood;
import com.github.fnar.util.ReportThisIssueException;
import com.google.gson.JsonElement;
import greymerk.roguelike.config.RogueConfig;
import greymerk.roguelike.util.DyeColor;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.MetaBlock1_12;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AnvilBlock.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * rlgen: Roguelike Dungeons' 1.12 block mapper rewritten for 1.21 blocks. Same choices as the 1.12 one, quirks included
 * (coal ore gives a coal block, "chiseled" quartz ends up a pillar, light gray dye colours fall back to white).
 */
public class BlockMapper1_12 {
   public static MetaBlock1_12 map(SingleBlockBrush block) {
      Direction facing = block.getFacing();
      net.minecraft.core.Direction enumFacing = getFacing(facing);
      switch (block.getBlockType()) {
         case ANVIL:
            return getAnvil((AnvilBlock)block);
         case ACACIA_LOG:
            return getLogMetaBlock(Wood.ACACIA, facing);
         case BIRCH_LOG:
            return getLogMetaBlock(Wood.BIRCH, facing);
         case DARK_OAK_LOG:
            return getLogMetaBlock(Wood.DARK_OAK, facing);
         case JUNGLE_LOG:
            return getLogMetaBlock(Wood.JUNGLE, facing);
         case OAK_LOG:
            return getLogMetaBlock(Wood.OAK, facing);
         case SPRUCE_LOG:
            return getLogMetaBlock(Wood.SPRUCE, facing);
         case ACACIA_FENCE:
            return getFenceMetaBlock(Wood.ACACIA);
         case BIRCH_FENCE:
            return getFenceMetaBlock(Wood.BIRCH);
         case DARK_OAK_FENCE:
            return getFenceMetaBlock(Wood.DARK_OAK);
         case JUNGLE_FENCE:
            return getFenceMetaBlock(Wood.JUNGLE);
         case OAK_FENCE:
            return getFenceMetaBlock(Wood.OAK);
         case SPRUCE_FENCE:
            return getFenceMetaBlock(Wood.SPRUCE);
         case ACACIA_PLANK:
            return getPlankMetaBlock(Wood.ACACIA);
         case BIRCH_PLANK:
            return getPlankMetaBlock(Wood.BIRCH);
         case DARK_OAK_PLANK:
            return getPlankMetaBlock(Wood.DARK_OAK);
         case JUNGLE_PLANK:
            return getPlankMetaBlock(Wood.JUNGLE);
         case OAK_PLANK:
            return getPlankMetaBlock(Wood.OAK);
         case SPRUCE_PLANK:
            return getPlankMetaBlock(Wood.SPRUCE);
         case OAK_LEAVES:
            return getLeaves(Wood.OAK, false);
         case SPRUCE_LEAVES:
            return getLeaves(Wood.SPRUCE, false);
         case BIRCH_LEAVES:
            return getLeaves(Wood.BIRCH, false);
         case JUNGLE_LEAVES:
            return getLeaves(Wood.JUNGLE, false);
         case ACACIA_LEAVES2:
            return getLeaves(Wood.ACACIA, false);
         case DARK_OAK_LEAVES:
            return getLeaves(Wood.DARK_OAK, false);
         case CARPET:
         case CONCRETE:
         case CONCRETE_POWDER:
         case STAINED_GLASS:
         case STAINED_GLASS_PANE:
         case STAINED_HARDENED_CLAY:
         case TERRACOTTA:
         case WOOL:
            return getColoredBlock((ColoredBlock)block);
         case ACACIA_STAIRS:
         case BIRCH_STAIRS:
         case BRICK_STAIRS:
         case DARK_OAK_STAIRS:
         case JUNGLE_STAIRS:
         case NETHER_BRICK_STAIRS:
         case OAK_STAIRS:
         case PURPUR_STAIRS:
         case QUARTZ_STAIRS:
         case RED_SANDSTONE_STAIRS:
         case SANDSTONE_STAIRS:
         case SPRUCE_STAIRS:
         case STONE_BRICK_STAIRS:
         case STONE_STAIRS:
            return mapStairs((StairsBlock)block);
         case ACACIA_SLAB:
         case BIRCH_SLAB:
         case BRICK_SLAB:
         case COBBLE_SLAB:
         case DARK_OAK_SLAB:
         case JUNGLE_SLAB:
         case LEGACY_OAK_SLAB:
         case NETHERBRICK_SLAB:
         case OAK_SLAB:
         case QUARTZ_SLAB:
         case RED_SANDSTONE_SLAB:
         case SANDSTONE_SLAB:
         case SMOOTH_RED_SANDSTONE_SLAB:
         case SPRUCE_SLAB:
         case STONEBRICK_SLAB:
         case STONE_SLAB:
            return getSlab((SlabBlock)block);
         case IRON_DOOR:
         case OAK_DOOR:
         case BIRCH_DOOR:
         case SPRUCE_DOOR:
         case JUNGLE_DOOR:
         case ACACIA_DOOR:
         case DARK_OAK_DOOR:
         case WARPED_DOOR:
            return mapDoor((DoorBlock)block);
         case CHISELED_QUARTZ:
            return getQuartz(Quartz.CHISELED, facing);
         case PILLAR_QUARTZ:
            return getQuartz(Quartz.PILLAR, facing);
         case SMOOTH_QUARTZ:
            return getQuartz(Quartz.SMOOTH, facing);
         case VINE:
            return createVine(facing);
         case CROP:
            return getCrop((CropBlock)block);
         case PUMPKIN:
            return getPumpkin((PumpkinBlock)block);
         case FLOWER_POT:
            return new MetaBlock1_12(Blocks.FLOWER_POT);
         case BROWN_MUSHROOM:
            return new MetaBlock1_12(Blocks.BROWN_MUSHROOM);
         case RED_MUSHROOM:
            return new MetaBlock1_12(Blocks.RED_MUSHROOM);
         case PLANT:
            return getPlant(((PlantBlock)block).getPlant());
         case TALL_PLANT:
            return getTallPlant((TallPlantBlock)block);
         case TRAPDOOR:
            return getTrapdoor((TrapdoorBlock)block);
         case LEVER:
            return getLever((LeverBlock)block);
         case TORCH:
         case REDSTONE_TORCH:
            return getTorch((TorchBlock)block);
         case BED:
            return getBed((BedBlock)block);
         case FURNACE:
            if (!RogueConfig.FURNITURE.getBoolean()) {
               return new MetaBlock1_12(Blocks.COBBLESTONE);
            }
            return new MetaBlock1_12(Blocks.FURNACE).withProperty(HorizontalDirectionalBlock.FACING, enumFacing);
         case DISPENSER:
            return new MetaBlock1_12(Blocks.DISPENSER).withProperty(BlockStateProperties.FACING, enumFacing);
         case DROPPER:
            return new MetaBlock1_12(Blocks.DROPPER).withProperty(BlockStateProperties.FACING, enumFacing);
         case HOPPER:
            return new MetaBlock1_12(Blocks.HOPPER).withProperty(BlockStateProperties.FACING_HOPPER, enumFacing);
         case COMPARATOR:
            return getComparator((ComparatorBlock)block);
         case REPEATER:
            return getRepeater((RepeaterBlock)block);
         case PISTON:
            return new MetaBlock1_12(Blocks.PISTON).withProperty(BlockStateProperties.FACING, enumFacing);
         case STICKY_PISTON:
            return new MetaBlock1_12(Blocks.STICKY_PISTON).withProperty(BlockStateProperties.FACING, enumFacing);
         case INFESTED_BLOCK:
            return getInfestedBlock((InfestedBlock)block);
         case CHEST:
            return new MetaBlock1_12(Blocks.CHEST).withProperty(HorizontalDirectionalBlock.FACING, enumFacing);
         case TRAPPED_CHEST:
            return new MetaBlock1_12(Blocks.TRAPPED_CHEST).withProperty(HorizontalDirectionalBlock.FACING, enumFacing);
         case NETHER_PORTAL:
            return new MetaBlock1_12(Blocks.NETHER_PORTAL).withProperty(BlockStateProperties.HORIZONTAL_AXIS, enumFacing.getAxis());
         case TRIPWIRE:
            return new MetaBlock1_12(Blocks.TRIPWIRE);
         case TRIPWIRE_HOOK:
            return new MetaBlock1_12(Blocks.TRIPWIRE_HOOK).withProperty(BlockStateProperties.ATTACHED, ((TripwireHookBlock)block).isAttached())
               .withProperty(HorizontalDirectionalBlock.FACING, enumFacing);
         case LADDER:
            return new MetaBlock1_12(Blocks.LADDER).withProperty(HorizontalDirectionalBlock.FACING, enumFacing);
         default:
            return map(block.getBlockType());
      }
   }

   private static MetaBlock1_12 getCrop(CropBlock cropBlock) {
      switch (cropBlock.getCrop()) {
         case BEETROOTS:
            return new MetaBlock1_12(Blocks.BEETROOTS);
         case CARROTS:
            return new MetaBlock1_12(Blocks.CARROTS);
         case COCOA:
            return new MetaBlock1_12(Blocks.COCOA).withProperty(HorizontalDirectionalBlock.FACING, getFacing(cropBlock.getFacing().reverse()))
               .withProperty(BlockStateProperties.AGE_2, 2);
         case MELON:
            return new MetaBlock1_12(Blocks.MELON_STEM);
         case NETHER_WART:
            return new MetaBlock1_12(Blocks.NETHER_WART);
         case POTATOES:
            return new MetaBlock1_12(Blocks.POTATOES);
         case PUMPKIN:
            return new MetaBlock1_12(Blocks.PUMPKIN_STEM);
         case WHEAT:
         default:
            return new MetaBlock1_12(Blocks.WHEAT);
      }
   }

   private static MetaBlock1_12 getRepeater(RepeaterBlock block) {
      return new MetaBlock1_12(Blocks.REPEATER).withProperty(BlockStateProperties.POWERED, block.isPowered())
         .withProperty(BlockStateProperties.DELAY, block.getDelay().asInt())
         .withProperty(HorizontalDirectionalBlock.FACING, getFacing(block.getFacing()));
   }

   private static MetaBlock1_12 getComparator(ComparatorBlock block) {
      return new MetaBlock1_12(Blocks.COMPARATOR).withProperty(BlockStateProperties.POWERED, block.isPowered())
         .withProperty(HorizontalDirectionalBlock.FACING, getFacing(block.getFacing()))
         .withProperty(BlockStateProperties.MODE_COMPARATOR, block.getMode() == ComparatorBlock.Mode.SUBTRACTION ? ComparatorMode.SUBTRACT : ComparatorMode.COMPARE);
   }

   /** BlockLever.EnumOrientation: walls by facing, UP_X / DOWN_X lie along X on the floor / ceiling. */
   private static MetaBlock1_12 getLever(LeverBlock leverBlock) {
      Direction dir = leverBlock.getFacing();
      MetaBlock1_12 lever = new MetaBlock1_12(Blocks.LEVER).withProperty(BlockStateProperties.POWERED, leverBlock.isActive());
      switch (dir) {
         case UP:
            return lever.withProperty(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR).withProperty(HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.EAST);
         case DOWN:
            return lever.withProperty(BlockStateProperties.ATTACH_FACE, AttachFace.CEILING).withProperty(HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.EAST);
         case EAST:
            return lever.withProperty(BlockStateProperties.ATTACH_FACE, AttachFace.WALL).withProperty(HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.EAST);
         case SOUTH:
            return lever.withProperty(BlockStateProperties.ATTACH_FACE, AttachFace.WALL).withProperty(HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.SOUTH);
         case WEST:
            return lever.withProperty(BlockStateProperties.ATTACH_FACE, AttachFace.WALL).withProperty(HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.WEST);
         case NORTH:
         default:
            return lever.withProperty(BlockStateProperties.ATTACH_FACE, AttachFace.WALL).withProperty(HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.NORTH);
      }
   }

   private static Block named(String name) {
      return BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(name));
   }

   public static MetaBlock1_12 map(BlockType blockType) {
      switch (blockType) {
         case CARPET:
            return new MetaBlock1_12(Blocks.WHITE_CARPET);
         case CONCRETE:
            return new MetaBlock1_12(Blocks.WHITE_CONCRETE);
         case STAINED_GLASS:
            return new MetaBlock1_12(Blocks.WHITE_STAINED_GLASS);
         case STAINED_GLASS_PANE:
            return new MetaBlock1_12(Blocks.WHITE_STAINED_GLASS_PANE);
         case WOOL:
            return new MetaBlock1_12(Blocks.WHITE_WOOL);
         case VINE:
            return new MetaBlock1_12(Blocks.VINE);
         case PUMPKIN:
            return new MetaBlock1_12(Blocks.CARVED_PUMPKIN);
         case BROWN_MUSHROOM:
            return new MetaBlock1_12(Blocks.BROWN_MUSHROOM);
         case RED_MUSHROOM:
            return new MetaBlock1_12(Blocks.RED_MUSHROOM);
         case TALL_PLANT:
            return new MetaBlock1_12(Blocks.DEAD_BUSH); // Blocks.TALLGRASS default (meta 0) was the dead shrub
         case TORCH:
            return new MetaBlock1_12(Blocks.TORCH);
         case REDSTONE_TORCH:
            return new MetaBlock1_12(Blocks.REDSTONE_TORCH);
         case BED:
            return new MetaBlock1_12(Blocks.RED_BED);
         case FURNACE:
            return new MetaBlock1_12(Blocks.FURNACE);
         case DISPENSER:
            return new MetaBlock1_12(Blocks.DISPENSER);
         case HOPPER:
            return new MetaBlock1_12(Blocks.HOPPER);
         case COMPARATOR:
            return new MetaBlock1_12(Blocks.COMPARATOR);
         case PISTON:
            return new MetaBlock1_12(Blocks.PISTON);
         case CHEST:
            return new MetaBlock1_12(Blocks.CHEST);
         case TRAPPED_CHEST:
            return new MetaBlock1_12(Blocks.TRAPPED_CHEST);
         case TRIPWIRE:
            return new MetaBlock1_12(Blocks.TRIPWIRE);
         case AIR:
            return new MetaBlock1_12(Blocks.AIR);
         case WATER_STILL:
         case WATER_FLOWING:
            return new MetaBlock1_12(Blocks.WATER);
         case LAVA_STILL:
         case LAVA_FLOWING:
            return new MetaBlock1_12(Blocks.LAVA);
         case FIRE:
            return new MetaBlock1_12(Blocks.FIRE);
         case IRON_BAR:
            return new MetaBlock1_12(Blocks.IRON_BARS);
         case STONE_SMOOTH:
            return new MetaBlock1_12(Blocks.STONE);
         case GRANITE:
            return new MetaBlock1_12(Blocks.GRANITE);
         case GRANITE_POLISHED:
            return new MetaBlock1_12(Blocks.POLISHED_GRANITE);
         case DIORITE:
            return new MetaBlock1_12(Blocks.DIORITE);
         case DIORITE_POLISHED:
            return new MetaBlock1_12(Blocks.POLISHED_DIORITE);
         case ANDESITE:
            return new MetaBlock1_12(Blocks.ANDESITE);
         case ANDESITE_POLISHED:
            return new MetaBlock1_12(Blocks.POLISHED_ANDESITE);
         case GRASS:
            return new MetaBlock1_12(Blocks.GRASS_BLOCK);
         case DIRT:
            return new MetaBlock1_12(Blocks.DIRT);
         case DIRT_COARSE:
            return new MetaBlock1_12(Blocks.COARSE_DIRT);
         case DIRT_PODZOL:
            return new MetaBlock1_12(Blocks.PODZOL);
         case COBBLESTONE:
            return new MetaBlock1_12(Blocks.COBBLESTONE);
         case COBBLESTONE_WALL:
            return new MetaBlock1_12(Blocks.COBBLESTONE_WALL);
         case BEDROCK:
            return new MetaBlock1_12(Blocks.BEDROCK);
         case SAND:
            return new MetaBlock1_12(Blocks.SAND);
         case SAND_RED:
            return new MetaBlock1_12(Blocks.RED_SAND);
         case GRAVEL:
            return new MetaBlock1_12(Blocks.GRAVEL);
         case ORE_GOLD:
            return new MetaBlock1_12(Blocks.GOLD_ORE);
         case ORE_IRON:
            return new MetaBlock1_12(Blocks.IRON_ORE);
         case ORE_COAL:
            return new MetaBlock1_12(Blocks.COAL_BLOCK);
         case GLASS:
            return new MetaBlock1_12(Blocks.GLASS);
         case GLASS_PANE:
            return new MetaBlock1_12(Blocks.GLASS_PANE);
         case ORE_LAPIS:
            return new MetaBlock1_12(Blocks.LAPIS_ORE);
         case LAPIS_BLOCK:
            return new MetaBlock1_12(Blocks.LAPIS_BLOCK);
         case ORE_EMERALD:
            return new MetaBlock1_12(Blocks.EMERALD_ORE);
         case SANDSTONE:
            return new MetaBlock1_12(Blocks.SANDSTONE);
         case CHISELED_SANDSTONE:
            return new MetaBlock1_12(Blocks.CHISELED_SANDSTONE);
         case SANDSTONE_SMOOTH:
            return new MetaBlock1_12(Blocks.CUT_SANDSTONE);
         case GOLD_BLOCK:
            return new MetaBlock1_12(Blocks.GOLD_BLOCK);
         case IRON_BLOCK:
            return new MetaBlock1_12(Blocks.IRON_BLOCK);
         case BRICK:
            return new MetaBlock1_12(Blocks.BRICKS);
         case COBBLESTONE_MOSSY:
            return new MetaBlock1_12(Blocks.MOSSY_COBBLESTONE);
         case OBSIDIAN:
            return new MetaBlock1_12(Blocks.OBSIDIAN);
         case ORE_DIAMOND:
            return new MetaBlock1_12(Blocks.DIAMOND_ORE);
         case DIAMOND_BLOCK:
            return new MetaBlock1_12(Blocks.DIAMOND_BLOCK);
         case FARMLAND:
            return new MetaBlock1_12(Blocks.FARMLAND);
         case ORE_REDSTONE:
            return new MetaBlock1_12(Blocks.REDSTONE_ORE);
         case ICE:
            return new MetaBlock1_12(Blocks.ICE);
         case SNOW:
            return new MetaBlock1_12(Blocks.SNOW_BLOCK);
         case CLAY:
            return new MetaBlock1_12(Blocks.CLAY);
         case NETHERRACK:
            return new MetaBlock1_12(Blocks.NETHERRACK);
         case SOUL_SAND:
            return new MetaBlock1_12(Blocks.SOUL_SAND);
         case GLOWSTONE:
            return new MetaBlock1_12(Blocks.GLOWSTONE);
         case STONE_BRICK:
            return new MetaBlock1_12(Blocks.STONE_BRICKS);
         case STONE_BRICK_MOSSY:
            return new MetaBlock1_12(Blocks.MOSSY_STONE_BRICKS);
         case STONE_BRICK_CRACKED:
            return new MetaBlock1_12(Blocks.CRACKED_STONE_BRICKS);
         case STONE_BRICK_CHISELED:
            return new MetaBlock1_12(Blocks.CHISELED_STONE_BRICKS);
         case MYCELIUM:
            return new MetaBlock1_12(Blocks.MYCELIUM);
         case NETHERBRICK:
            return new MetaBlock1_12(Blocks.NETHER_BRICKS);
         case END_STONE:
            return new MetaBlock1_12(Blocks.END_STONE);
         case EMERALD_BLOCK:
            return new MetaBlock1_12(Blocks.EMERALD_BLOCK);
         case ORE_QUARTZ:
            return new MetaBlock1_12(Blocks.NETHER_QUARTZ_ORE);
         case PRISMITE:
            return new MetaBlock1_12(Blocks.PRISMARINE);
         case PRISMARINE:
            return new MetaBlock1_12(Blocks.PRISMARINE_BRICKS);
         case PRISMARINE_DARK:
            return new MetaBlock1_12(Blocks.DARK_PRISMARINE);
         case SEA_LANTERN:
            return new MetaBlock1_12(Blocks.SEA_LANTERN);
         case COAL_BLOCK:
            return new MetaBlock1_12(Blocks.COAL_BLOCK);
         case ICE_PACKED:
            return new MetaBlock1_12(Blocks.PACKED_ICE);
         case RED_SANDSTONE:
            return new MetaBlock1_12(Blocks.RED_SANDSTONE);
         case CHISELED_RED_SANDSTONE:
            return new MetaBlock1_12(Blocks.CHISELED_RED_SANDSTONE);
         case SMOOTH_RED_SANDSTONE:
            return new MetaBlock1_12(Blocks.CUT_RED_SANDSTONE);
         case QUARTZ:
            return new MetaBlock1_12(Blocks.QUARTZ_BLOCK);
         case REDSTONE_BLOCK:
            return new MetaBlock1_12(Blocks.REDSTONE_BLOCK);
         case PRESSURE_PLATE_STONE:
            return new MetaBlock1_12(Blocks.STONE_PRESSURE_PLATE);
         case PRESSURE_PLATE_WOODEN:
            return new MetaBlock1_12(Blocks.OAK_PRESSURE_PLATE);
         case BOOKSHELF:
            return new MetaBlock1_12(Blocks.BOOKSHELF);
         case REDSTONE_WIRE:
            return new MetaBlock1_12(Blocks.REDSTONE_WIRE);
         case REEDS:
            return new MetaBlock1_12(Blocks.SUGAR_CANE);
         case CRAFTING_TABLE:
            return !RogueConfig.FURNITURE.getBoolean() ? getPlankMetaBlock(Wood.OAK) : new MetaBlock1_12(Blocks.CRAFTING_TABLE);
         case NOTEBLOCK:
            return new MetaBlock1_12(Blocks.NOTE_BLOCK);
         case REDSTONE_LAMP:
            return new MetaBlock1_12(Blocks.REDSTONE_LAMP);
         case REDSTONE_LAMP_LIT:
            return new MetaBlock1_12(Blocks.REDSTONE_LAMP).withProperty(BlockStateProperties.LIT, true);
         case JUKEBOX:
            return new MetaBlock1_12(Blocks.JUKEBOX);
         case TNT:
            return new MetaBlock1_12(Blocks.TNT);
         case ENCHANTING_TABLE:
            return new MetaBlock1_12(Blocks.ENCHANTING_TABLE);
         case FENCE_NETHER_BRICK:
            return new MetaBlock1_12(Blocks.NETHER_BRICK_FENCE);
         case WEB:
            return new MetaBlock1_12(Blocks.COBWEB);
         case PURPUR_BLOCK:
            return new MetaBlock1_12(Blocks.PURPUR_BLOCK);
         case PURPUR_PILLAR:
            return new MetaBlock1_12(Blocks.PURPUR_PILLAR);
         case PURPUR_STAIR:
            return new MetaBlock1_12(Blocks.PURPUR_STAIRS);
         case PURPUR_DOUBLE_SLAB:
            return new MetaBlock1_12(Blocks.PURPUR_SLAB).withProperty(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE);
         case PURPUR_SLAB:
            return new MetaBlock1_12(Blocks.PURPUR_SLAB);
         case ENDER_BRICK:
            return new MetaBlock1_12(Blocks.END_STONE_BRICKS);
         case MAGMA:
            return new MetaBlock1_12(Blocks.MAGMA_BLOCK);
         case RED_NETHERBRICK:
            return new MetaBlock1_12(Blocks.RED_NETHER_BRICKS);
         case NETHER_WART_BLOCK:
            return new MetaBlock1_12(Blocks.NETHER_WART_BLOCK);
         case OAK_SAPLING:
            return getSapling(Wood.OAK);
         case SPRUCE_SAPLING:
            return getSapling(Wood.SPRUCE);
         case BIRCH_SAPLING:
            return getSapling(Wood.BIRCH);
         case BONE_BLOCK:
            return new MetaBlock1_12(Blocks.BONE_BLOCK);
         case BREWING_STAND:
            return new MetaBlock1_12(Blocks.BREWING_STAND);
         case CACTUS:
            return new MetaBlock1_12(Blocks.CACTUS);
         case CAKE:
            return new MetaBlock1_12(Blocks.CAKE);
         case MOB_SPAWNER:
            return new MetaBlock1_12(Blocks.SPAWNER);
         case ENDER_CHEST:
            return new MetaBlock1_12(Blocks.ENDER_CHEST);
         case RED_FLOWER:
            return new MetaBlock1_12(Blocks.POPPY);
         case SKULL:
            return new MetaBlock1_12(Blocks.SKELETON_SKULL);
         case YELLOW_FLOWER:
            return new MetaBlock1_12(Blocks.DANDELION);
         case JUNGLE_SAPLING:
            return getSapling(Wood.JUNGLE);
         case ACACIA_SAPLING:
            return getSapling(Wood.ACACIA);
         case DARK_OAK_SAPLING:
            return getSapling(Wood.DARK_OAK);
         case WHEAT:
            return new MetaBlock1_12(Blocks.WHEAT);
         default:
            throw new ReportThisIssueException(new UnmappedBlockException(blockType));
      }
   }

   /** EnumDyeColor.valueOf(name): 1.12 called light gray "SILVER", so LIGHT_GRAY fell back to white. */
   private static String dye(DyeColor color) {
      return color == DyeColor.LIGHT_GRAY ? "white" : color.name().toLowerCase(java.util.Locale.ROOT);
   }

   private static MetaBlock1_12 getColoredBlock(ColoredBlock coloredBlock) {
      if (coloredBlock.getBlockType() == BlockType.TERRACOTTA) {
         DyeColor c = coloredBlock.getColor();
         return new MetaBlock1_12(named((c == DyeColor.LIGHT_GRAY ? "light_gray" : c.name().toLowerCase(java.util.Locale.ROOT)) + "_glazed_terracotta"));
      }
      String color = dye(coloredBlock.getColor());
      String suffix;
      switch (coloredBlock.getBlockType()) {
         case CARPET:
            suffix = "_carpet";
            break;
         case CONCRETE:
            suffix = "_concrete";
            break;
         case CONCRETE_POWDER:
            suffix = "_concrete_powder";
            break;
         case STAINED_GLASS:
            suffix = "_stained_glass";
            break;
         case STAINED_GLASS_PANE:
            suffix = "_stained_glass_pane";
            break;
         case STAINED_HARDENED_CLAY:
            suffix = "_terracotta";
            break;
         case WOOL:
         default:
            suffix = "_wool";
      }
      return new MetaBlock1_12(named(color + suffix));
   }

   private static MetaBlock1_12 getBed(BedBlock bedBlock) {
      return new MetaBlock1_12(Blocks.RED_BED).withProperty(HorizontalDirectionalBlock.FACING, getFacing(bedBlock.getFacing()))
         .withProperty(BlockStateProperties.BED_PART, bedBlock.isHead() ? BedPart.HEAD : BedPart.FOOT);
   }

   private static MetaBlock1_12 getPumpkin(PumpkinBlock block) {
      return new MetaBlock1_12(block.isLit() ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN)
         .withProperty(HorizontalDirectionalBlock.FACING, getFacing(block.getFacing()));
   }

   private static MetaBlock1_12 getAnvil(AnvilBlock block) {
      if (!RogueConfig.FURNITURE.getBoolean()) {
         return map(BlockType.ANDESITE_POLISHED);
      }
      Block anvil = switch (block.getDamage().ordinal()) {
         case 0 -> Blocks.ANVIL;
         case 1 -> Blocks.CHIPPED_ANVIL;
         default -> Blocks.DAMAGED_ANVIL;
      };
      return new MetaBlock1_12(anvil).withProperty(HorizontalDirectionalBlock.FACING, getFacing(block.getFacing()));
   }

   public static MetaBlock1_12 mapStairs(StairsBlock block) {
      JsonElement json = block.getJson();
      MetaBlock1_12 stairs;
      try {
         stairs = json != null ? new MetaBlock1_12(json) : new MetaBlock1_12(getBlockForStairs(block.getBlockType()));
      } catch (greymerk.roguelike.dungeon.settings.DungeonSettingParseException e) {
         stairs = new MetaBlock1_12(getBlockForStairs(block.getBlockType()));
      }
      return stairs.withProperty(HorizontalDirectionalBlock.FACING, getFacing(block.getFacing()))
         .withProperty(BlockStateProperties.HALF, block.isUpsideDown() ? Half.TOP : Half.BOTTOM);
   }

   private static Block getBlockForStairs(BlockType stairs) {
      switch (stairs) {
         case ACACIA_STAIRS:
            return Blocks.ACACIA_STAIRS;
         case BIRCH_STAIRS:
            return Blocks.BIRCH_STAIRS;
         case BRICK_STAIRS:
            return Blocks.BRICK_STAIRS;
         case DARK_OAK_STAIRS:
            return Blocks.DARK_OAK_STAIRS;
         case JUNGLE_STAIRS:
            return Blocks.JUNGLE_STAIRS;
         case NETHER_BRICK_STAIRS:
            return Blocks.NETHER_BRICK_STAIRS;
         case OAK_STAIRS:
            return Blocks.OAK_STAIRS;
         case PURPUR_STAIRS:
            return Blocks.PURPUR_STAIRS;
         case QUARTZ_STAIRS:
            return Blocks.QUARTZ_STAIRS;
         case RED_SANDSTONE_STAIRS:
            return Blocks.RED_SANDSTONE_STAIRS;
         case SANDSTONE_STAIRS:
            return Blocks.SANDSTONE_STAIRS;
         case SPRUCE_STAIRS:
            return Blocks.SPRUCE_STAIRS;
         case STONE_BRICK_STAIRS:
            return Blocks.STONE_BRICK_STAIRS;
         case STONE_STAIRS:
         default:
            return Blocks.COBBLESTONE_STAIRS;
      }
   }

   private static MetaBlock1_12 getTrapdoor(TrapdoorBlock block) {
      return new MetaBlock1_12(block.getMaterial() == Material.METAL ? Blocks.IRON_TRAPDOOR : Blocks.OAK_TRAPDOOR)
         .withProperty(BlockStateProperties.HALF, block.isFlushWithTop() ? Half.TOP : Half.BOTTOM)
         .withProperty(BlockStateProperties.OPEN, block.isOpen())
         .withProperty(HorizontalDirectionalBlock.FACING, getFacing(block.getFacing()));
   }

   public static MetaBlock1_12 mapDoor(DoorBlock block) {
      JsonElement json = block.getJson();
      MetaBlock1_12 door;
      try {
         door = json != null ? new MetaBlock1_12(json) : getDoorMetaBlock(block.getBlockType());
      } catch (greymerk.roguelike.dungeon.settings.DungeonSettingParseException e) {
         door = getDoorMetaBlock(block.getBlockType());
      }
      return door.withProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, block.isTop() ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER)
         .withProperty(HorizontalDirectionalBlock.FACING, getFacing(block.getFacing()))
         .withProperty(BlockStateProperties.OPEN, block.isOpen())
         .withProperty(BlockStateProperties.DOOR_HINGE, block.isHingeLeft() ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT);
   }

   private static MetaBlock1_12 getDoorMetaBlock(BlockType type) {
      switch (type) {
         case IRON_DOOR:
            return new MetaBlock1_12(Blocks.IRON_DOOR);
         case BIRCH_DOOR:
            return new MetaBlock1_12(Blocks.BIRCH_DOOR);
         case SPRUCE_DOOR:
            return new MetaBlock1_12(Blocks.SPRUCE_DOOR);
         case JUNGLE_DOOR:
            return new MetaBlock1_12(Blocks.JUNGLE_DOOR);
         case ACACIA_DOOR:
            return new MetaBlock1_12(Blocks.ACACIA_DOOR);
         case DARK_OAK_DOOR:
            return new MetaBlock1_12(Blocks.DARK_OAK_DOOR);
         case OAK_DOOR:
         default:
            return new MetaBlock1_12(Blocks.OAK_DOOR);
      }
   }

   private static MetaBlock1_12 getSlab(SlabBlock slabBlock) {
      Block slab = switch (slabBlock.getBlockType()) {
         case ACACIA_SLAB -> Blocks.ACACIA_SLAB;
         case BIRCH_SLAB -> Blocks.BIRCH_SLAB;
         case BRICK_SLAB -> Blocks.BRICK_SLAB;
         case COBBLE_SLAB -> Blocks.COBBLESTONE_SLAB;
         case DARK_OAK_SLAB -> Blocks.DARK_OAK_SLAB;
         case JUNGLE_SLAB -> Blocks.JUNGLE_SLAB;
         case LEGACY_OAK_SLAB -> Blocks.PETRIFIED_OAK_SLAB;
         case NETHERBRICK_SLAB -> Blocks.NETHER_BRICK_SLAB;
         case OAK_SLAB -> Blocks.OAK_SLAB;
         case QUARTZ_SLAB -> Blocks.QUARTZ_SLAB;
         case RED_SANDSTONE_SLAB, SMOOTH_RED_SANDSTONE_SLAB -> Blocks.RED_SANDSTONE_SLAB;
         case SANDSTONE_SLAB -> Blocks.SANDSTONE_SLAB;
         case SPRUCE_SLAB -> Blocks.SPRUCE_SLAB;
         case STONEBRICK_SLAB -> Blocks.STONE_BRICK_SLAB;
         default -> Blocks.SMOOTH_STONE_SLAB;
      };
      if (slabBlock.isFullBlock()) {
         // 1.12 "seamless" double stone slabs looked like smooth stone
         if (slabBlock.isSeamless() && slab == Blocks.SMOOTH_STONE_SLAB) {
            return new MetaBlock1_12(Blocks.SMOOTH_STONE);
         }
         if (slabBlock.isSeamless() && slab == Blocks.SANDSTONE_SLAB) {
            return new MetaBlock1_12(Blocks.SMOOTH_SANDSTONE);
         }
         return new MetaBlock1_12(slab).withProperty(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE);
      }
      return new MetaBlock1_12(slab).withProperty(BlockStateProperties.SLAB_TYPE, slabBlock.isTop() ? SlabType.TOP : SlabType.BOTTOM);
   }

   private static MetaBlock1_12 getTorch(TorchBlock torchBlock) {
      boolean redstone = !torchBlock.isLit() || torchBlock.getBlockType() == BlockType.REDSTONE_TORCH;
      Direction dir = torchBlock.getFacing();
      if (dir == Direction.UP || dir == Direction.DOWN) {
         MetaBlock1_12 t = new MetaBlock1_12(redstone ? Blocks.REDSTONE_TORCH : Blocks.TORCH);
         return redstone ? t.withProperty(BlockStateProperties.LIT, torchBlock.isLit()) : t;
      }
      MetaBlock1_12 t = new MetaBlock1_12(redstone ? Blocks.REDSTONE_WALL_TORCH : Blocks.WALL_TORCH)
         .withProperty(HorizontalDirectionalBlock.FACING, getFacing(dir.reverse()));
      return redstone ? t.withProperty(BlockStateProperties.LIT, torchBlock.isLit()) : t;
   }

   private static MetaBlock1_12 getLogMetaBlock(Wood type, Direction facing) {
      Block log = switch (type) {
         case SPRUCE -> Blocks.SPRUCE_LOG;
         case BIRCH -> Blocks.BIRCH_LOG;
         case JUNGLE -> Blocks.JUNGLE_LOG;
         case ACACIA -> Blocks.ACACIA_LOG;
         case DARK_OAK -> Blocks.DARK_OAK_LOG;
         default -> Blocks.OAK_LOG;
      };
      switch (facing) {
         case NORTH:
         case SOUTH:
            return new MetaBlock1_12(log).withProperty(BlockStateProperties.AXIS, net.minecraft.core.Direction.Axis.Z);
         case EAST:
         case WEST:
            return new MetaBlock1_12(log).withProperty(BlockStateProperties.AXIS, net.minecraft.core.Direction.Axis.X);
         default:
            return new MetaBlock1_12(log).withProperty(BlockStateProperties.AXIS, net.minecraft.core.Direction.Axis.Y);
      }
   }

   private static MetaBlock1_12 getPlankMetaBlock(Wood type) {
      return new MetaBlock1_12(switch (type) {
         case JUNGLE -> Blocks.JUNGLE_PLANKS;
         case BIRCH -> Blocks.BIRCH_PLANKS;
         case SPRUCE -> Blocks.SPRUCE_PLANKS;
         case ACACIA -> Blocks.ACACIA_PLANKS;
         case DARK_OAK -> Blocks.DARK_OAK_PLANKS;
         default -> Blocks.OAK_PLANKS;
      });
   }

   private static MetaBlock1_12 getFenceMetaBlock(Wood type) {
      return new MetaBlock1_12(switch (type) {
         case JUNGLE -> Blocks.JUNGLE_FENCE;
         case BIRCH -> Blocks.BIRCH_FENCE;
         case SPRUCE -> Blocks.SPRUCE_FENCE;
         case ACACIA -> Blocks.ACACIA_FENCE;
         case DARK_OAK -> Blocks.DARK_OAK_FENCE;
         default -> Blocks.OAK_FENCE;
      });
   }

   private static MetaBlock1_12 getSapling(Wood type) {
      return new MetaBlock1_12(switch (type) {
         case JUNGLE -> Blocks.JUNGLE_SAPLING;
         case BIRCH -> Blocks.BIRCH_SAPLING;
         case SPRUCE -> Blocks.SPRUCE_SAPLING;
         case ACACIA -> Blocks.ACACIA_SAPLING;
         case DARK_OAK -> Blocks.DARK_OAK_SAPLING;
         default -> Blocks.OAK_SAPLING;
      });
   }

   private static MetaBlock1_12 getLeaves(Wood type, boolean decay) {
      Block leaves = switch (type) {
         case JUNGLE -> Blocks.JUNGLE_LEAVES;
         case BIRCH -> Blocks.BIRCH_LEAVES;
         case SPRUCE -> Blocks.SPRUCE_LEAVES;
         case ACACIA -> Blocks.ACACIA_LEAVES;
         case DARK_OAK -> Blocks.DARK_OAK_LEAVES;
         default -> Blocks.OAK_LEAVES;
      };
      return new MetaBlock1_12(leaves).withProperty(BlockStateProperties.PERSISTENT, !decay);
   }

   private static MetaBlock1_12 createVine(Direction dir) {
      return new MetaBlock1_12(Blocks.VINE).withProperty(BlockStateProperties.UP, dir == Direction.UP)
         .withProperty(BlockStateProperties.NORTH, dir == Direction.NORTH).withProperty(BlockStateProperties.EAST, dir == Direction.EAST)
         .withProperty(BlockStateProperties.SOUTH, dir == Direction.SOUTH).withProperty(BlockStateProperties.WEST, dir == Direction.WEST);
   }

   public static MetaBlock1_12 getPlant(Plant type) {
      switch (type) {
         case POPPY:
            return new MetaBlock1_12(Blocks.POPPY);
         case ORCHID:
            return new MetaBlock1_12(Blocks.BLUE_ORCHID);
         case ALLIUM:
            return new MetaBlock1_12(Blocks.ALLIUM);
         case BLUET:
            return new MetaBlock1_12(Blocks.AZURE_BLUET);
         case REDTULIP:
            return new MetaBlock1_12(Blocks.RED_TULIP);
         case ORANGETULIP:
            return new MetaBlock1_12(Blocks.ORANGE_TULIP);
         case WHITETULIP:
            return new MetaBlock1_12(Blocks.WHITE_TULIP);
         case PINKTULIP:
            return new MetaBlock1_12(Blocks.PINK_TULIP);
         case DAISY:
            return new MetaBlock1_12(Blocks.OXEYE_DAISY);
         case RED_MUSHROOM:
            return new MetaBlock1_12(Blocks.RED_MUSHROOM);
         case BROWN_MUSHROOM:
            return new MetaBlock1_12(Blocks.BROWN_MUSHROOM);
         case CACTUS:
            return new MetaBlock1_12(Blocks.CACTUS);
         case OAK_SAPLING:
            return getSapling(Wood.OAK);
         case SPRUCE_SAPLING:
            return getSapling(Wood.SPRUCE);
         case BIRCH_SAPLING:
            return getSapling(Wood.BIRCH);
         case JUNGLE_SAPLING:
            return getSapling(Wood.JUNGLE);
         case ACACIA_SAPLING:
            return getSapling(Wood.ACACIA);
         case DARKOAK_SAPLING:
            return getSapling(Wood.DARK_OAK);
         case SHRUB:
            return new MetaBlock1_12(Blocks.DEAD_BUSH);
         case FERN:
            return new MetaBlock1_12(Blocks.FERN);
         case DANDELION:
         default:
            return new MetaBlock1_12(Blocks.DANDELION);
      }
   }

   private static MetaBlock1_12 getInfestedBlock(InfestedBlock block) {
      return new MetaBlock1_12(switch (block.getStone()) {
         case COBBLE -> Blocks.INFESTED_COBBLESTONE;
         case STONEBRICK -> Blocks.INFESTED_STONE_BRICKS;
         case STONEBRICK_MOSSY -> Blocks.INFESTED_MOSSY_STONE_BRICKS;
         case STONEBRICK_CRACKED -> Blocks.INFESTED_CRACKED_STONE_BRICKS;
         case STONEBRICK_CHISELED -> Blocks.INFESTED_CHISELED_STONE_BRICKS;
         default -> Blocks.INFESTED_STONE;
      });
   }

   /** CHISELED set the variant, then addPillarLines overwrote it: chiseled quartz came out as a pillar. */
   private static MetaBlock1_12 getQuartz(Quartz type, Direction facing) {
      if (type == Quartz.SMOOTH) {
         return new MetaBlock1_12(Blocks.QUARTZ_BLOCK);
      }
      net.minecraft.core.Direction.Axis axis = switch (facing) {
         case NORTH, SOUTH -> net.minecraft.core.Direction.Axis.Z;
         case EAST, WEST -> net.minecraft.core.Direction.Axis.X;
         default -> net.minecraft.core.Direction.Axis.Y;
      };
      return new MetaBlock1_12(Blocks.QUARTZ_PILLAR).withProperty(BlockStateProperties.AXIS, axis);
   }

   private static MetaBlock1_12 getTallPlant(TallPlantBlock tallPlantBlock) {
      Block plant = switch (tallPlantBlock.getTallPlant()) {
         case SUNFLOWER -> Blocks.SUNFLOWER;
         case LILAC -> Blocks.LILAC;
         case LARGE_FERN -> Blocks.LARGE_FERN;
         case ROSE_BUSH -> Blocks.ROSE_BUSH;
         case PEONY -> Blocks.PEONY;
         default -> Blocks.TALL_GRASS;
      };
      return new MetaBlock1_12(plant).withProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, tallPlantBlock.isTop() ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER);
   }

   /** Roguelike's own Direction is the reverse of Minecraft's facing. */
   public static net.minecraft.core.Direction getFacing(Direction direction) {
      switch (direction) {
         case EAST:
            return net.minecraft.core.Direction.WEST;
         case SOUTH:
            return net.minecraft.core.Direction.NORTH;
         case WEST:
            return net.minecraft.core.Direction.EAST;
         case UP:
            return net.minecraft.core.Direction.UP;
         case DOWN:
            return net.minecraft.core.Direction.DOWN;
         case NORTH:
         default:
            return net.minecraft.core.Direction.SOUTH;
      }
   }
}
