package com.nukevscity.rlgen.legacy;

/**
 * The 1.12.2 block states that terrain generation can place, as a small enum so the port can
 * keep using 1.12-style checks: {@code getBlock()} compares the block (ignoring variants such as
 * red sand or stained clay colour) and {@code isAir()} stands in for {@code getMaterial() == Material.AIR}.
 */
public enum LBlock {
   AIR(null, "minecraft:air"),
   STONE(null, "minecraft:stone"),
   GRASS(null, "minecraft:grass_block"),
   DIRT(null, "minecraft:dirt"),
   COARSE_DIRT(DIRT, "minecraft:coarse_dirt"),
   PODZOL(DIRT, "minecraft:podzol"),
   BEDROCK(null, "minecraft:bedrock"),
   WATER(null, "minecraft:water"),
   FLOWING_WATER(null, "minecraft:water"),
   LAVA(null, "minecraft:lava"),
   FLOWING_LAVA(null, "minecraft:lava"),
   GRAVEL(null, "minecraft:gravel"),
   SAND(null, "minecraft:sand"),
   RED_SAND(SAND, "minecraft:red_sand"),
   SANDSTONE(null, "minecraft:sandstone"),
   RED_SANDSTONE(null, "minecraft:red_sandstone"),
   ICE(null, "minecraft:ice"),
   PACKED_ICE(null, "minecraft:packed_ice"),
   SNOW(null, "minecraft:snow_block"),
   SNOW_LAYER(null, "minecraft:snow"),
   MYCELIUM(null, "minecraft:mycelium"),
   HARDENED_CLAY(null, "minecraft:terracotta"),
   STAINED_HARDENED_CLAY(null, "minecraft:white_terracotta"),
   ORANGE_STAINED_HARDENED_CLAY(STAINED_HARDENED_CLAY, "minecraft:orange_terracotta"),
   YELLOW_STAINED_HARDENED_CLAY(STAINED_HARDENED_CLAY, "minecraft:yellow_terracotta"),
   BROWN_STAINED_HARDENED_CLAY(STAINED_HARDENED_CLAY, "minecraft:brown_terracotta"),
   RED_STAINED_HARDENED_CLAY(STAINED_HARDENED_CLAY, "minecraft:red_terracotta"),
   SILVER_STAINED_HARDENED_CLAY(STAINED_HARDENED_CLAY, "minecraft:light_gray_terracotta"),
   WATERLILY(null, "minecraft:lily_pad"),
   // Traverse
   TRAVERSE_RED_ROCK(null, "rlgen:red_rock"),
   TRAVERSE_BLUE_ROCK(null, "rlgen:blue_rock"),
   // Defiled Lands
   DEFILED_GRASS(null, "rlgen:grass_defiled"),
   DEFILED_DIRT(null, "rlgen:dirt_defiled"),
   DEFILED_SAND(null, "rlgen:sand_defiled"),
   DEFILED_STONE(null, "rlgen:stone_defiled"),
   // Better Caves puts andesite (a stone variant) under gravel it would leave floating
   ANDESITE(STONE, "minecraft:andesite");

   private static final LBlock[] VALUES = values();

   private final LBlock block;
   public final String modernId;

   LBlock(LBlock block, String modernId) {
      this.block = block;
      this.modernId = modernId;
   }

   /** The 1.12 Block of this state (variants share their base block). */
   public LBlock getBlock() {
      return this.block == null ? this : this.block;
   }

   public boolean isAir() {
      return this == AIR;
   }

   public boolean isWaterMaterial() {
      return this == WATER || this == FLOWING_WATER;
   }

   public static LBlock byId(int id) {
      return VALUES[id];
   }
}
