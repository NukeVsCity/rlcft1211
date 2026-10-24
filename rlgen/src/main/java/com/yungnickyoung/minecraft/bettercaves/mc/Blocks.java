package com.yungnickyoung.minecraft.bettercaves.mc;

import com.nukevscity.rlgen.legacy.LBlock;

/** rlgen: 1.12 blocks as the legacy LBlock states; blocks that never occur in a chunk primer are null. */
public final class Blocks {
   public static final LBlock AIR = LBlock.AIR;
   public static final LBlock STONE = LBlock.STONE;
   public static final LBlock WATER = LBlock.WATER;
   public static final LBlock LAVA = LBlock.LAVA;
   public static final LBlock BEDROCK = LBlock.BEDROCK;
   public static final LBlock SAND = LBlock.SAND;
   public static final LBlock SANDSTONE = LBlock.SANDSTONE;
   public static final LBlock RED_SANDSTONE = LBlock.RED_SANDSTONE;
   public static final LBlock GRAVEL = LBlock.GRAVEL;
   public static final LBlock DIRT = LBlock.DIRT;
   public static final LBlock GRASS = LBlock.GRASS;
   public static final LBlock HARDENED_CLAY = LBlock.HARDENED_CLAY;
   public static final LBlock STAINED_HARDENED_CLAY = LBlock.STAINED_HARDENED_CLAY;
   public static final LBlock MYCELIUM = LBlock.MYCELIUM;
   public static final LBlock SNOW_LAYER = LBlock.SNOW_LAYER;
   public static final LBlock LEAVES = null;
   public static final LBlock LEAVES2 = null;
   public static final LBlock LOG = null;
   public static final LBlock LOG2 = null;
   // debug visualiser blocks (never used: debug view is off)
   public static final LBlock GOLD_BLOCK = null;
   public static final LBlock PLANKS = null;
   public static final LBlock COBBLESTONE = null;
   public static final LBlock REDSTONE_BLOCK = null;
   public static final LBlock EMERALD_BLOCK = null;
   public static final LBlock BRICK_BLOCK = null;

   private Blocks() {
   }

   /** Material.ROCK among the blocks a primer can hold. */
   public static boolean isRock(LBlock s) {
      LBlock b = s.getBlock();
      return b == LBlock.STONE || b == LBlock.BEDROCK || b == LBlock.SANDSTONE || b == LBlock.RED_SANDSTONE || b == LBlock.HARDENED_CLAY
         || b == LBlock.STAINED_HARDENED_CLAY || b == LBlock.TRAVERSE_RED_ROCK || b == LBlock.TRAVERSE_BLUE_ROCK || b == LBlock.DEFILED_STONE;
   }
}
