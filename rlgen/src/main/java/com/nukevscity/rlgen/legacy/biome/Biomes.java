package com.nukevscity.rlgen.legacy.biome;

/**
 * 1.12.2 net.minecraft.init.Biomes plus the RLCraft mod biomes. Vanilla registrations are copied from
 * Biome.registerBiomes(); mod values are read from the Traverse and Defiled Lands 1.12 jars.
 * Mod biome ids only need to be unique (1.12 Forge assigned them from 40 up per world).
 */
public final class Biomes {
   public static final Biome OCEAN;
   public static final Biome DEFAULT;
   public static final Biome PLAINS;
   public static final Biome DESERT;
   public static final Biome EXTREME_HILLS;
   public static final Biome FOREST;
   public static final Biome TAIGA;
   public static final Biome SWAMPLAND;
   public static final Biome RIVER;
   public static final Biome HELL;
   public static final Biome SKY;
   public static final Biome FROZEN_OCEAN;
   public static final Biome FROZEN_RIVER;
   public static final Biome ICE_PLAINS;
   public static final Biome ICE_MOUNTAINS;
   public static final Biome MUSHROOM_ISLAND;
   public static final Biome MUSHROOM_ISLAND_SHORE;
   public static final Biome BEACH;
   public static final Biome DESERT_HILLS;
   public static final Biome FOREST_HILLS;
   public static final Biome TAIGA_HILLS;
   public static final Biome EXTREME_HILLS_EDGE;
   public static final Biome JUNGLE;
   public static final Biome JUNGLE_HILLS;
   public static final Biome JUNGLE_EDGE;
   public static final Biome DEEP_OCEAN;
   public static final Biome STONE_BEACH;
   public static final Biome COLD_BEACH;
   public static final Biome BIRCH_FOREST;
   public static final Biome BIRCH_FOREST_HILLS;
   public static final Biome ROOFED_FOREST;
   public static final Biome COLD_TAIGA;
   public static final Biome COLD_TAIGA_HILLS;
   public static final Biome REDWOOD_TAIGA;
   public static final Biome REDWOOD_TAIGA_HILLS;
   public static final Biome EXTREME_HILLS_WITH_TREES;
   public static final Biome SAVANNA;
   public static final Biome SAVANNA_PLATEAU;
   public static final Biome MESA;
   public static final Biome MESA_ROCK;
   public static final Biome MESA_CLEAR_ROCK;
   public static final Biome VOID;
   public static final Biome MUTATED_PLAINS;
   public static final Biome MUTATED_DESERT;
   public static final Biome MUTATED_EXTREME_HILLS;
   public static final Biome MUTATED_FOREST;
   public static final Biome MUTATED_TAIGA;
   public static final Biome MUTATED_SWAMPLAND;
   public static final Biome MUTATED_ICE_FLATS;
   public static final Biome MUTATED_JUNGLE;
   public static final Biome MUTATED_JUNGLE_EDGE;
   public static final Biome MUTATED_BIRCH_FOREST;
   public static final Biome MUTATED_BIRCH_FOREST_HILLS;
   public static final Biome MUTATED_ROOFED_FOREST;
   public static final Biome MUTATED_TAIGA_COLD;
   public static final Biome MUTATED_REDWOOD_TAIGA;
   public static final Biome MUTATED_REDWOOD_TAIGA_HILLS;
   public static final Biome MUTATED_EXTREME_HILLS_WITH_TREES;
   public static final Biome MUTATED_SAVANNA;
   public static final Biome MUTATED_SAVANNA_ROCK;
   public static final Biome MUTATED_MESA;
   public static final Biome MUTATED_MESA_ROCK;
   public static final Biome MUTATED_MESA_CLEAR_ROCK;
   public static final Biome DESERT_DEFILED;
   public static final Biome PLAINS_DEFILED;
   public static final Biome FOREST_TENEBRA;
   public static final Biome FOREST_VILESPINE;
   public static final Biome HILLS_DEFILED;
   public static final Biome SWAMP_DEFILED;
   public static final Biome ICE_PLAINS_DEFILED;
   public static final Biome AUTUMNAL_WOODS;
   public static final Biome WOODLANDS;
   public static final Biome MINI_JUNGLE;
   public static final Biome MEADOW;
   public static final Biome GREEN_SWAMP;
   public static final Biome RED_DESERT;
   public static final Biome TEMPERATE_RAINFOREST;
   public static final Biome BADLANDS;
   public static final Biome MOUNTAINOUS_DESERT;
   public static final Biome ROCKY_PLATEAU;
   public static final Biome FORESTED_HILLS;
   public static final Biome BIRCH_FORESTED_HILLS;
   public static final Biome AUTUMNAL_WOODED_HILLS;
   public static final Biome CLIFFS;
   public static final Biome GLACIER;
   public static final Biome GLACIER_SPIKES;
   public static final Biome SNOWY_CONIFEROUS_FOREST;
   public static final Biome LUSH_HILLS;
   public static final Biome CANYON;
   public static final Biome CRAG_CLIFFS;
   public static final Biome DESERT_SHRUBLAND;
   public static final Biome THICKET;
   public static final Biome ARID_HIGHLAND;
   public static final Biome ROCKY_PLAINS;

   private Biomes() {
   }

   static {
      // ---- vanilla 1.12.2 Biome.registerBiomes() ----

      Biome.registerVanillaBiome(0, "ocean", new BiomeOcean(new Biome.BiomeProperties("Ocean").setBaseHeight(-1.0F).setHeightVariation(0.1F)));
      Biome.registerVanillaBiome(
         1,
         "plains",
         new BiomePlains( new Biome.BiomeProperties("Plains").setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(0.8F).setRainfall(0.4F))
      );
      Biome.registerVanillaBiome(
         2,
         "desert",
         new BiomeDesert(
            new Biome.BiomeProperties("Desert").setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()
         )
      );
      Biome.registerVanillaBiome(
         3,
         "extreme_hills",
         new BiomeHills(
            BiomeHills.Type.NORMAL,
            new Biome.BiomeProperties("Extreme Hills").setBaseHeight(1.0F).setHeightVariation(0.5F).setTemperature(0.2F).setRainfall(0.3F)
         )
      );
      Biome.registerVanillaBiome(4, "forest", new BiomeForest( new Biome.BiomeProperties("Forest").setTemperature(0.7F).setRainfall(0.8F)));
      Biome.registerVanillaBiome(
         5,
         "taiga",
         new BiomeTaiga(
            BiomeTaiga.Type.NORMAL, new Biome.BiomeProperties("Taiga").setBaseHeight(0.2F).setHeightVariation(0.2F).setTemperature(0.25F).setRainfall(0.8F)
         )
      );
      Biome.registerVanillaBiome(
         6,
         "swampland",
         new BiomeSwamp(
            new Biome.BiomeProperties("Swampland").setBaseHeight(-0.2F).setHeightVariation(0.1F).setTemperature(0.8F).setRainfall(0.9F).setWaterColor(14745518)
         )
      );
      Biome.registerVanillaBiome(7, "river", new BiomeRiver(new Biome.BiomeProperties("River").setBaseHeight(-0.5F).setHeightVariation(0.0F)));
      Biome.registerVanillaBiome(8, "hell", new BiomeVoid(new Biome.BiomeProperties("Hell").setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
      Biome.registerVanillaBiome(9, "sky", new BiomeVoid(new Biome.BiomeProperties("The End").setRainDisabled()));
      Biome.registerVanillaBiome(
         10,
         "frozen_ocean",
         new BiomeOcean(
            new Biome.BiomeProperties("FrozenOcean").setBaseHeight(-1.0F).setHeightVariation(0.1F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()
         )
      );
      Biome.registerVanillaBiome(
         11,
         "frozen_river",
         new BiomeRiver(
            new Biome.BiomeProperties("FrozenRiver").setBaseHeight(-0.5F).setHeightVariation(0.0F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()
         )
      );
      Biome.registerVanillaBiome(
         12,
         "ice_flats",
         new BiomeSnow(
            false,
            new Biome.BiomeProperties("Ice Plains").setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()
         )
      );
      Biome.registerVanillaBiome(
         13,
         "ice_mountains",
         new BiomeSnow(
            false,
            new Biome.BiomeProperties("Ice Mountains").setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()
         )
      );
      Biome.registerVanillaBiome(
         14,
         "mushroom_island",
         new BiomeMushroomIsland(
            new Biome.BiomeProperties("MushroomIsland").setBaseHeight(0.2F).setHeightVariation(0.3F).setTemperature(0.9F).setRainfall(1.0F)
         )
      );
      Biome.registerVanillaBiome(
         15,
         "mushroom_island_shore",
         new BiomeMushroomIsland(
            new Biome.BiomeProperties("MushroomIslandShore").setBaseHeight(0.0F).setHeightVariation(0.025F).setTemperature(0.9F).setRainfall(1.0F)
         )
      );
      Biome.registerVanillaBiome(
         16,
         "beaches",
         new BiomeBeach(new Biome.BiomeProperties("Beach").setBaseHeight(0.0F).setHeightVariation(0.025F).setTemperature(0.8F).setRainfall(0.4F))
      );
      Biome.registerVanillaBiome(
         17,
         "desert_hills",
         new BiomeDesert(
            new Biome.BiomeProperties("DesertHills").setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()
         )
      );
      Biome.registerVanillaBiome(
         18,
         "forest_hills",
         new BiomeForest(
            new Biome.BiomeProperties("ForestHills").setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.7F).setRainfall(0.8F)
         )
      );
      Biome.registerVanillaBiome(
         19,
         "taiga_hills",
         new BiomeTaiga(
            BiomeTaiga.Type.NORMAL,
            new Biome.BiomeProperties("TaigaHills").setTemperature(0.25F).setRainfall(0.8F).setBaseHeight(0.45F).setHeightVariation(0.3F)
         )
      );
      Biome.registerVanillaBiome(
         20,
         "smaller_extreme_hills",
         new BiomeHills(
            BiomeHills.Type.EXTRA_TREES,
            new Biome.BiomeProperties("Extreme Hills Edge").setBaseHeight(0.8F).setHeightVariation(0.3F).setTemperature(0.2F).setRainfall(0.3F)
         )
      );
      Biome.registerVanillaBiome(21, "jungle", new BiomeJungle( new Biome.BiomeProperties("Jungle").setTemperature(0.95F).setRainfall(0.9F)));
      Biome.registerVanillaBiome(
         22,
         "jungle_hills",
         new BiomeJungle( new Biome.BiomeProperties("JungleHills").setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.95F).setRainfall(0.9F))
      );
      Biome.registerVanillaBiome(23, "jungle_edge", new BiomeJungle( new Biome.BiomeProperties("JungleEdge").setTemperature(0.95F).setRainfall(0.8F)));
      Biome.registerVanillaBiome(24, "deep_ocean", new BiomeOcean(new Biome.BiomeProperties("Deep Ocean").setBaseHeight(-1.8F).setHeightVariation(0.1F)));
      Biome.registerVanillaBiome(
         25,
         "stone_beach",
         new BiomeStoneBeach(new Biome.BiomeProperties("Stone Beach").setBaseHeight(0.1F).setHeightVariation(0.8F).setTemperature(0.2F).setRainfall(0.3F))
      );
      Biome.registerVanillaBiome(
         26,
         "cold_beach",
         new BiomeBeach(
            new Biome.BiomeProperties("Cold Beach").setBaseHeight(0.0F).setHeightVariation(0.025F).setTemperature(0.05F).setRainfall(0.3F).setSnowEnabled()
         )
      );
      Biome.registerVanillaBiome(
         27, "birch_forest", new BiomeForest( new Biome.BiomeProperties("Birch Forest").setTemperature(0.6F).setRainfall(0.6F))
      );
      Biome.registerVanillaBiome(
         28,
         "birch_forest_hills",
         new BiomeForest(
            new Biome.BiomeProperties("Birch Forest Hills").setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.6F).setRainfall(0.6F)
         )
      );
      Biome.registerVanillaBiome(
         29, "roofed_forest", new BiomeForest( new Biome.BiomeProperties("Roofed Forest").setTemperature(0.7F).setRainfall(0.8F))
      );
      Biome.registerVanillaBiome(
         30,
         "taiga_cold",
         new BiomeTaiga(
            BiomeTaiga.Type.NORMAL,
            new Biome.BiomeProperties("Cold Taiga").setBaseHeight(0.2F).setHeightVariation(0.2F).setTemperature(-0.5F).setRainfall(0.4F).setSnowEnabled()
         )
      );
      Biome.registerVanillaBiome(
         31,
         "taiga_cold_hills",
         new BiomeTaiga(
            BiomeTaiga.Type.NORMAL,
            new Biome.BiomeProperties("Cold Taiga Hills")
               .setBaseHeight(0.45F)
               .setHeightVariation(0.3F)
               .setTemperature(-0.5F)
               .setRainfall(0.4F)
               .setSnowEnabled()
         )
      );
      Biome.registerVanillaBiome(
         32,
         "redwood_taiga",
         new BiomeTaiga(
            BiomeTaiga.Type.MEGA, new Biome.BiomeProperties("Mega Taiga").setTemperature(0.3F).setRainfall(0.8F).setBaseHeight(0.2F).setHeightVariation(0.2F)
         )
      );
      Biome.registerVanillaBiome(
         33,
         "redwood_taiga_hills",
         new BiomeTaiga(
            BiomeTaiga.Type.MEGA,
            new Biome.BiomeProperties("Mega Taiga Hills").setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.3F).setRainfall(0.8F)
         )
      );
      Biome.registerVanillaBiome(
         34,
         "extreme_hills_with_trees",
         new BiomeHills(
            BiomeHills.Type.EXTRA_TREES,
            new Biome.BiomeProperties("Extreme Hills+").setBaseHeight(1.0F).setHeightVariation(0.5F).setTemperature(0.2F).setRainfall(0.3F)
         )
      );
      Biome.registerVanillaBiome(
         35,
         "savanna",
         new BiomeSavanna(
            new Biome.BiomeProperties("Savanna").setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(1.2F).setRainfall(0.0F).setRainDisabled()
         )
      );
      Biome.registerVanillaBiome(
         36,
         "savanna_rock",
         new BiomeSavanna(
            new Biome.BiomeProperties("Savanna Plateau")
               .setBaseHeight(1.5F)
               .setHeightVariation(0.025F)
               .setTemperature(1.0F)
               .setRainfall(0.0F)
               .setRainDisabled()
         )
      );
      Biome.registerVanillaBiome(37, "mesa", new BiomeMesa(false, false, new Biome.BiomeProperties("Mesa").setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
      Biome.registerVanillaBiome(
         38,
         "mesa_rock",
         new BiomeMesa(
            false,
            true,
            new Biome.BiomeProperties("Mesa Plateau F").setBaseHeight(1.5F).setHeightVariation(0.025F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()
         )
      );
      Biome.registerVanillaBiome(
         39,
         "mesa_clear_rock",
         new BiomeMesa(
            false,
            false,
            new Biome.BiomeProperties("Mesa Plateau").setBaseHeight(1.5F).setHeightVariation(0.025F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()
         )
      );
      Biome.registerVanillaBiome(127, "void", new BiomeVoid(new Biome.BiomeProperties("The Void").setRainDisabled()));
      Biome.registerVanillaBiome(
         129,
         "mutated_plains",
         new BiomePlains(
            new Biome.BiomeProperties("Sunflower Plains")
               .setBaseBiome("plains")
               .setBaseHeight(0.125F)
               .setHeightVariation(0.05F)
               .setTemperature(0.8F)
               .setRainfall(0.4F)
         )
      );
      Biome.registerVanillaBiome(
         130,
         "mutated_desert",
         new BiomeDesert(
            new Biome.BiomeProperties("Desert M")
               .setBaseBiome("desert")
               .setBaseHeight(0.225F)
               .setHeightVariation(0.25F)
               .setTemperature(2.0F)
               .setRainfall(0.0F)
               .setRainDisabled()
         )
      );
      Biome.registerVanillaBiome(
         131,
         "mutated_extreme_hills",
         new BiomeHills(
            BiomeHills.Type.MUTATED,
            new Biome.BiomeProperties("Extreme Hills M")
               .setBaseBiome("extreme_hills")
               .setBaseHeight(1.0F)
               .setHeightVariation(0.5F)
               .setTemperature(0.2F)
               .setRainfall(0.3F)
         )
      );
      Biome.registerVanillaBiome(
         132,
         "mutated_forest",
         new BiomeForest(
            new Biome.BiomeProperties("Flower Forest").setBaseBiome("forest").setHeightVariation(0.4F).setTemperature(0.7F).setRainfall(0.8F)
         )
      );
      Biome.registerVanillaBiome(
         133,
         "mutated_taiga",
         new BiomeTaiga(
            BiomeTaiga.Type.NORMAL,
            new Biome.BiomeProperties("Taiga M").setBaseBiome("taiga").setBaseHeight(0.3F).setHeightVariation(0.4F).setTemperature(0.25F).setRainfall(0.8F)
         )
      );
      Biome.registerVanillaBiome(
         134,
         "mutated_swampland",
         new BiomeSwamp(
            new Biome.BiomeProperties("Swampland M")
               .setBaseBiome("swampland")
               .setBaseHeight(-0.1F)
               .setHeightVariation(0.3F)
               .setTemperature(0.8F)
               .setRainfall(0.9F)
               .setWaterColor(14745518)
         )
      );
      Biome.registerVanillaBiome(
         140,
         "mutated_ice_flats",
         new BiomeSnow(
            true,
            new Biome.BiomeProperties("Ice Plains Spikes")
               .setBaseBiome("ice_flats")
               .setBaseHeight(0.425F)
               .setHeightVariation(0.45000002F)
               .setTemperature(0.0F)
               .setRainfall(0.5F)
               .setSnowEnabled()
         )
      );
      Biome.registerVanillaBiome(
         149,
         "mutated_jungle",
         new BiomeJungle(
            new Biome.BiomeProperties("Jungle M").setBaseBiome("jungle").setBaseHeight(0.2F).setHeightVariation(0.4F).setTemperature(0.95F).setRainfall(0.9F)
         )
      );
      Biome.registerVanillaBiome(
         151,
         "mutated_jungle_edge",
         new BiomeJungle(
            new Biome.BiomeProperties("JungleEdge M")
               .setBaseBiome("jungle_edge")
               .setBaseHeight(0.2F)
               .setHeightVariation(0.4F)
               .setTemperature(0.95F)
               .setRainfall(0.8F)
         )
      );
      Biome.registerVanillaBiome(
         155,
         "mutated_birch_forest",
         new BiomeForestMutated(
            new Biome.BiomeProperties("Birch Forest M")
               .setBaseBiome("birch_forest")
               .setBaseHeight(0.2F)
               .setHeightVariation(0.4F)
               .setTemperature(0.6F)
               .setRainfall(0.6F)
         )
      );
      Biome.registerVanillaBiome(
         156,
         "mutated_birch_forest_hills",
         new BiomeForestMutated(
            new Biome.BiomeProperties("Birch Forest Hills M")
               .setBaseBiome("birch_forest_hills")
               .setBaseHeight(0.55F)
               .setHeightVariation(0.5F)
               .setTemperature(0.6F)
               .setRainfall(0.6F)
         )
      );
      Biome.registerVanillaBiome(
         157,
         "mutated_roofed_forest",
         new BiomeForest(
            new Biome.BiomeProperties("Roofed Forest M")
               .setBaseBiome("roofed_forest")
               .setBaseHeight(0.2F)
               .setHeightVariation(0.4F)
               .setTemperature(0.7F)
               .setRainfall(0.8F)
         )
      );
      Biome.registerVanillaBiome(
         158,
         "mutated_taiga_cold",
         new BiomeTaiga(
            BiomeTaiga.Type.NORMAL,
            new Biome.BiomeProperties("Cold Taiga M")
               .setBaseBiome("taiga_cold")
               .setBaseHeight(0.3F)
               .setHeightVariation(0.4F)
               .setTemperature(-0.5F)
               .setRainfall(0.4F)
               .setSnowEnabled()
         )
      );
      Biome.registerVanillaBiome(
         160,
         "mutated_redwood_taiga",
         new BiomeTaiga(
            BiomeTaiga.Type.MEGA_SPRUCE,
            new Biome.BiomeProperties("Mega Spruce Taiga")
               .setBaseBiome("redwood_taiga")
               .setBaseHeight(0.2F)
               .setHeightVariation(0.2F)
               .setTemperature(0.25F)
               .setRainfall(0.8F)
         )
      );
      Biome.registerVanillaBiome(
         161,
         "mutated_redwood_taiga_hills",
         new BiomeTaiga(
            BiomeTaiga.Type.MEGA_SPRUCE,
            new Biome.BiomeProperties("Redwood Taiga Hills M")
               .setBaseBiome("redwood_taiga_hills")
               .setBaseHeight(0.2F)
               .setHeightVariation(0.2F)
               .setTemperature(0.25F)
               .setRainfall(0.8F)
         )
      );
      Biome.registerVanillaBiome(
         162,
         "mutated_extreme_hills_with_trees",
         new BiomeHills(
            BiomeHills.Type.MUTATED,
            new Biome.BiomeProperties("Extreme Hills+ M")
               .setBaseBiome("extreme_hills_with_trees")
               .setBaseHeight(1.0F)
               .setHeightVariation(0.5F)
               .setTemperature(0.2F)
               .setRainfall(0.3F)
         )
      );
      Biome.registerVanillaBiome(
         163,
         "mutated_savanna",
         new BiomeSavannaMutated(
            new Biome.BiomeProperties("Savanna M")
               .setBaseBiome("savanna")
               .setBaseHeight(0.3625F)
               .setHeightVariation(1.225F)
               .setTemperature(1.1F)
               .setRainfall(0.0F)
               .setRainDisabled()
         )
      );
      Biome.registerVanillaBiome(
         164,
         "mutated_savanna_rock",
         new BiomeSavannaMutated(
            new Biome.BiomeProperties("Savanna Plateau M")
               .setBaseBiome("savanna_rock")
               .setBaseHeight(1.05F)
               .setHeightVariation(1.2125001F)
               .setTemperature(1.0F)
               .setRainfall(0.0F)
               .setRainDisabled()
         )
      );
      Biome.registerVanillaBiome(
         165,
         "mutated_mesa",
         new BiomeMesa(true, false, new Biome.BiomeProperties("Mesa (Bryce)").setBaseBiome("mesa").setTemperature(2.0F).setRainfall(0.0F).setRainDisabled())
      );
      Biome.registerVanillaBiome(
         166,
         "mutated_mesa_rock",
         new BiomeMesa(
            false,
            true,
            new Biome.BiomeProperties("Mesa Plateau F M")
               .setBaseBiome("mesa_rock")
               .setBaseHeight(0.45F)
               .setHeightVariation(0.3F)
               .setTemperature(2.0F)
               .setRainfall(0.0F)
               .setRainDisabled()
         )
      );
      Biome.registerVanillaBiome(
         167,
         "mutated_mesa_clear_rock",
         new BiomeMesa(
            false,
            false,
            new Biome.BiomeProperties("Mesa Plateau M")
               .setBaseBiome("mesa_clear_rock")
               .setBaseHeight(0.45F)
               .setHeightVariation(0.3F)
               .setTemperature(2.0F)
               .setRainfall(0.0F)
               .setRainDisabled()
         )
      );

      // ---- Defiled Lands then Traverse ----
      Biome.registerBiome(40, "defiledlands:desert_defiled", new ModBiomes.BiomeDesertDefiled(new Biome.BiomeProperties("Defiled Desert").setTemperature(2.0F).setRainfall(0.0F).setBaseHeight(0.125F).setHeightVariation(0.05F)));
      Biome.registerBiome(41, "defiledlands:plains_defiled", new ModBiomes.BiomePlainsDefiled(new Biome.BiomeProperties("Defiled Plains").setTemperature(0.8F).setRainfall(0.4F).setBaseHeight(0.125F).setHeightVariation(0.05F)));
      Biome.registerBiome(42, "defiledlands:forest_tenebra", new ModBiomes.BiomeForestTenebra(new Biome.BiomeProperties("Tenebra Forest").setTemperature(0.7F).setRainfall(0.8F).setBaseHeight(0.1F).setHeightVariation(0.2F)));
      Biome.registerBiome(43, "defiledlands:forest_vilespine", new ModBiomes.BiomeForestVilespine(new Biome.BiomeProperties("Vilespine Forest").setTemperature(0.7F).setRainfall(0.8F).setBaseHeight(0.1F).setHeightVariation(0.2F)));
      Biome.registerBiome(44, "defiledlands:hills_defiled", new ModBiomes.BiomeHillsDefiled(new Biome.BiomeProperties("Defiled Hills").setTemperature(0.2F).setRainfall(0.3F).setBaseHeight(1.0F).setHeightVariation(0.5F)));
      Biome.registerBiome(45, "defiledlands:swamp_defiled", new ModBiomes.BiomeSwampDefiled(new Biome.BiomeProperties("Defiled Swamp").setTemperature(0.8F).setRainfall(0.9F).setBaseHeight(-0.2F).setHeightVariation(0.1F)));
      Biome.registerBiome(46, "defiledlands:ice_plains_defiled", new ModBiomes.BiomeIcePlainsDefiled(new Biome.BiomeProperties("Defiled Ice Plains").setTemperature(0.0F).setRainfall(0.5F).setBaseHeight(0.125F).setHeightVariation(0.05F)));
      Biome.registerBiome(47, "traverse:autumnal_woods", new ModBiomes.BiomeAutumnalWoods(new Biome.BiomeProperties("Autumnal Woods").setTemperature(0.7F).setRainfall(0.8F).setBaseHeight(0.1F).setHeightVariation(0.2F)));
      Biome.registerBiome(48, "traverse:woodlands", new ModBiomes.BiomeWoodlands(new Biome.BiomeProperties("Woodlands").setTemperature(0.8F).setRainfall(0.4F).setBaseHeight(0.2F).setHeightVariation(0.05F)));
      Biome.registerBiome(49, "traverse:mini_jungle", new ModBiomes.BiomeMiniJungle(new Biome.BiomeProperties("Mini Jungle").setTemperature(0.95F).setRainfall(0.9F).setBaseHeight(-0.1F).setHeightVariation(0.5F).setWaterColor(-14372836)));
      Biome.registerBiome(50, "traverse:meadow", new ModBiomes.BiomeMeadow(new Biome.BiomeProperties("Meadow").setTemperature(0.8F).setRainfall(0.7F).setBaseHeight(0.1F).setHeightVariation(0.0F)));
      Biome.registerBiome(51, "traverse:green_swamp", new ModBiomes.BiomeLushSwamp(new Biome.BiomeProperties("Lush Swamp").setTemperature(0.8F).setBaseHeight(-0.2F).setHeightVariation(0.1F)));
      Biome.registerBiome(52, "traverse:red_desert", new ModBiomes.BiomeRedDesert(new Biome.BiomeProperties("Red Desert").setTemperature(2.0F).setRainfall(0.0F).setRainDisabled().setBaseHeight(0.125F).setHeightVariation(0.05F)));
      Biome.registerBiome(53, "traverse:temperate_rainforest", new ModBiomes.BiomeConiferousForest(new Biome.BiomeProperties("Temperate Rainforest").setTemperature(0.6F).setRainfall(0.9F).setBaseHeight(0.4F).setHeightVariation(1.1F)));
      Biome.registerBiome(54, "traverse:badlands", new ModBiomes.BiomeBadlands(new Biome.BiomeProperties("Badlands").setTemperature(0.6F).setRainfall(0.1F).setBaseHeight(0.6F).setHeightVariation(0.55F)));
      Biome.registerBiome(55, "traverse:mountainous_desert", new ModBiomes.BiomeMountainousDesert(new Biome.BiomeProperties("Mountainous Desert").setTemperature(2.0F).setRainfall(0.0F).setRainDisabled().setBaseHeight(0.4F).setHeightVariation(1.1F)));
      Biome.registerBiome(56, "traverse:rocky_plateau", new ModBiomes.BiomeRockyPlateau(new Biome.BiomeProperties("Rocky Plateau").setTemperature(0.8F).setRainfall(0.2F).setBaseHeight(1.6F).setHeightVariation(0.0F)));
      Biome.registerBiome(57, "traverse:forested_hills", new ModBiomes.BiomeForestedHills(new Biome.BiomeProperties("Forested Hills").setTemperature(0.7F).setRainfall(0.8F).setBaseHeight(1.0F).setHeightVariation(0.5F)));
      Biome.registerBiome(58, "traverse:birch_forested_hills", new ModBiomes.BiomeForestedHills(new Biome.BiomeProperties("Birch Forested Hills").setTemperature(0.7F).setRainfall(0.8F).setBaseHeight(1.0F).setHeightVariation(0.5F)));
      Biome.registerBiome(59, "traverse:autumnal_wooded_hills", new ModBiomes.BiomeAutumnalWoodedHills(new Biome.BiomeProperties("Autumnal Wooded Hills").setTemperature(0.7F).setRainfall(0.8F).setBaseHeight(1.0F).setHeightVariation(0.5F)));
      Biome.registerBiome(60, "traverse:cliffs", new ModBiomes.BiomeCliffs(new Biome.BiomeProperties("Cliffs").setTemperature(0.4F).setRainfall(1.2F).setRainDisabled().setBaseHeight(3.6F).setHeightVariation(0.5F)));
      Biome.registerBiome(61, "traverse:glacier", new ModBiomes.BiomeGlacier(new Biome.BiomeProperties("Glacier").setBaseBiome("ice_flats").setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled().setBaseHeight(1.5F).setHeightVariation(0.5F)));
      Biome.registerBiome(62, "traverse:glacier_spikes", new ModBiomes.BiomeGlacier(new Biome.BiomeProperties("Glacier Spikes").setBaseBiome("glacier").setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled().setBaseHeight(1.5F).setHeightVariation(0.5F)));
      Biome.registerBiome(63, "traverse:snowy_coniferous_forest", new ModBiomes.BiomeConiferousForest(new Biome.BiomeProperties("Snowy Coniferous Forest").setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled().setBaseHeight(0.4F).setHeightVariation(1.1F)));
      Biome.registerBiome(64, "traverse:lush_hills", new ModBiomes.BiomeLushHills(new Biome.BiomeProperties("Lush Hills").setTemperature(0.5F).setRainfall(0.8F).setBaseHeight(0.4F).setHeightVariation(0.3F)));
      Biome.registerBiome(65, "traverse:canyon", new ModBiomes.BiomeCanyon(new Biome.BiomeProperties("Canyon").setTemperature(1.0F).setRainfall(0.0F).setBaseHeight(1.8F).setHeightVariation(1.0F)));
      Biome.registerBiome(66, "traverse:crag_cliffs", new ModBiomes.BiomeCragCliffs(new Biome.BiomeProperties("Crag Cliffs").setTemperature(0.4F).setRainfall(0.0F).setBaseHeight(3.7F).setHeightVariation(0.5F)));
      Biome.registerBiome(67, "traverse:desert_shrubland", new ModBiomes.BiomeDesertShrubland(new Biome.BiomeProperties("Desert Shrubland").setTemperature(2.0F).setRainfall(0.0F).setRainDisabled().setBaseHeight(0.125F).setHeightVariation(0.05F)));
      Biome.registerBiome(68, "traverse:thicket", new ModBiomes.BiomeThicket(new Biome.BiomeProperties("Thicket").setTemperature(0.8F).setRainfall(0.3F).setBaseHeight(0.2F).setHeightVariation(0.3F)));
      Biome.registerBiome(69, "traverse:arid_highland", new ModBiomes.BiomeAridHighland(new Biome.BiomeProperties("Arid Highland").setTemperature(2.0F).setRainfall(0.0F).setRainDisabled().setBaseHeight(1.3F).setHeightVariation(0.4F)));
      Biome.registerBiome(70, "traverse:rocky_plains", new ModBiomes.BiomeRockyPlains(new Biome.BiomeProperties("Rocky Plains").setTemperature(0.9F).setRainfall(0.7F).setBaseHeight(0.125F).setHeightVariation(0.15F)));
      // Ice and Fire 1.12.2: added in the biome registry event, after Defiled Lands (mods load alphabetically)
      Biome.registerBiome(71, "iceandfire:glacier", new ModBiomes.BiomeIceAndFireGlacier(new Biome.BiomeProperties("Glacier").setBaseHeight(2.125F).setHeightVariation(0.025F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()));

      OCEAN = byName("ocean");
      DEFAULT = byName("ocean");
      PLAINS = byName("plains");
      DESERT = byName("desert");
      EXTREME_HILLS = byName("extreme_hills");
      FOREST = byName("forest");
      TAIGA = byName("taiga");
      SWAMPLAND = byName("swampland");
      RIVER = byName("river");
      HELL = byName("hell");
      SKY = byName("sky");
      FROZEN_OCEAN = byName("frozen_ocean");
      FROZEN_RIVER = byName("frozen_river");
      ICE_PLAINS = byName("ice_flats");
      ICE_MOUNTAINS = byName("ice_mountains");
      MUSHROOM_ISLAND = byName("mushroom_island");
      MUSHROOM_ISLAND_SHORE = byName("mushroom_island_shore");
      BEACH = byName("beaches");
      DESERT_HILLS = byName("desert_hills");
      FOREST_HILLS = byName("forest_hills");
      TAIGA_HILLS = byName("taiga_hills");
      EXTREME_HILLS_EDGE = byName("smaller_extreme_hills");
      JUNGLE = byName("jungle");
      JUNGLE_HILLS = byName("jungle_hills");
      JUNGLE_EDGE = byName("jungle_edge");
      DEEP_OCEAN = byName("deep_ocean");
      STONE_BEACH = byName("stone_beach");
      COLD_BEACH = byName("cold_beach");
      BIRCH_FOREST = byName("birch_forest");
      BIRCH_FOREST_HILLS = byName("birch_forest_hills");
      ROOFED_FOREST = byName("roofed_forest");
      COLD_TAIGA = byName("taiga_cold");
      COLD_TAIGA_HILLS = byName("taiga_cold_hills");
      REDWOOD_TAIGA = byName("redwood_taiga");
      REDWOOD_TAIGA_HILLS = byName("redwood_taiga_hills");
      EXTREME_HILLS_WITH_TREES = byName("extreme_hills_with_trees");
      SAVANNA = byName("savanna");
      SAVANNA_PLATEAU = byName("savanna_rock");
      MESA = byName("mesa");
      MESA_ROCK = byName("mesa_rock");
      MESA_CLEAR_ROCK = byName("mesa_clear_rock");
      VOID = byName("void");
      MUTATED_PLAINS = byName("mutated_plains");
      MUTATED_DESERT = byName("mutated_desert");
      MUTATED_EXTREME_HILLS = byName("mutated_extreme_hills");
      MUTATED_FOREST = byName("mutated_forest");
      MUTATED_TAIGA = byName("mutated_taiga");
      MUTATED_SWAMPLAND = byName("mutated_swampland");
      MUTATED_ICE_FLATS = byName("mutated_ice_flats");
      MUTATED_JUNGLE = byName("mutated_jungle");
      MUTATED_JUNGLE_EDGE = byName("mutated_jungle_edge");
      MUTATED_BIRCH_FOREST = byName("mutated_birch_forest");
      MUTATED_BIRCH_FOREST_HILLS = byName("mutated_birch_forest_hills");
      MUTATED_ROOFED_FOREST = byName("mutated_roofed_forest");
      MUTATED_TAIGA_COLD = byName("mutated_taiga_cold");
      MUTATED_REDWOOD_TAIGA = byName("mutated_redwood_taiga");
      MUTATED_REDWOOD_TAIGA_HILLS = byName("mutated_redwood_taiga_hills");
      MUTATED_EXTREME_HILLS_WITH_TREES = byName("mutated_extreme_hills_with_trees");
      MUTATED_SAVANNA = byName("mutated_savanna");
      MUTATED_SAVANNA_ROCK = byName("mutated_savanna_rock");
      MUTATED_MESA = byName("mutated_mesa");
      MUTATED_MESA_ROCK = byName("mutated_mesa_rock");
      MUTATED_MESA_CLEAR_ROCK = byName("mutated_mesa_clear_rock");
      DESERT_DEFILED = byName("defiledlands:desert_defiled");
      PLAINS_DEFILED = byName("defiledlands:plains_defiled");
      FOREST_TENEBRA = byName("defiledlands:forest_tenebra");
      FOREST_VILESPINE = byName("defiledlands:forest_vilespine");
      HILLS_DEFILED = byName("defiledlands:hills_defiled");
      SWAMP_DEFILED = byName("defiledlands:swamp_defiled");
      ICE_PLAINS_DEFILED = byName("defiledlands:ice_plains_defiled");
      AUTUMNAL_WOODS = byName("traverse:autumnal_woods");
      WOODLANDS = byName("traverse:woodlands");
      MINI_JUNGLE = byName("traverse:mini_jungle");
      MEADOW = byName("traverse:meadow");
      GREEN_SWAMP = byName("traverse:green_swamp");
      RED_DESERT = byName("traverse:red_desert");
      TEMPERATE_RAINFOREST = byName("traverse:temperate_rainforest");
      BADLANDS = byName("traverse:badlands");
      MOUNTAINOUS_DESERT = byName("traverse:mountainous_desert");
      ROCKY_PLATEAU = byName("traverse:rocky_plateau");
      FORESTED_HILLS = byName("traverse:forested_hills");
      BIRCH_FORESTED_HILLS = byName("traverse:birch_forested_hills");
      AUTUMNAL_WOODED_HILLS = byName("traverse:autumnal_wooded_hills");
      CLIFFS = byName("traverse:cliffs");
      GLACIER = byName("traverse:glacier");
      GLACIER_SPIKES = byName("traverse:glacier_spikes");
      SNOWY_CONIFEROUS_FOREST = byName("traverse:snowy_coniferous_forest");
      LUSH_HILLS = byName("traverse:lush_hills");
      CANYON = byName("traverse:canyon");
      CRAG_CLIFFS = byName("traverse:crag_cliffs");
      DESERT_SHRUBLAND = byName("traverse:desert_shrubland");
      THICKET = byName("traverse:thicket");
      ARID_HIGHLAND = byName("traverse:arid_highland");
      ROCKY_PLAINS = byName("traverse:rocky_plains");
   }

   private static Biome byName(String name) {
      for (Biome b : Biome.all()) {
         if (name.equals(b.getRegistryName())) {
            return b;
         }
      }
      throw new IllegalStateException("Missing legacy biome " + name);
   }

   /** Forces class init (registration). */
   public static void init() {
   }
}