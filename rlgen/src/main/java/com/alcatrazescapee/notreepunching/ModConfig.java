package com.alcatrazescapee.notreepunching;

/** 1.12 ModConfig with the RLCraft 2.9.3 notreepunching.cfg values (not read from a file; edit here). */
public final class ModConfig {
   public static final Balance BALANCE = new Balance();
   public static final General GENERAL = new General();

   private ModConfig() {
   }

   public static final class Balance {
      public boolean canPickUpRocks = true;
      public int firePitCookTime = 400;
      public int firePitFuelMaxAmount = 800;
      public int firePitFuelMultiplier = 5;
      public double fireStarterFireStartChance = 0.5;
      public double flintKnappingChance = 0.6;
      public double flintKnappingSuccessChance = 1.0;
      public double leavesStickDropChance = 0.3;
      public double logChoppingChance = 0.7;
      public double tallGrassDropPlantFiberChance = 0.5;
   }

   public static final class General {
      public boolean allowTorchesFromFirePits = true;
      public String[] alwaysBreakable = {"minecraft:leaves", "minecraft:gravel", "minecraft:sand", "minecraft:dirt", "minecraft:grass", "minecraft:grass_path",
         "minecraft:mycelium", "minecraft:clay", "minecraft:snow", "minecraft:snow_layer", "iceandfire:ash", "defiledlands:grass_defiled",
         "defiledlands:dirt_defiled", "defiledlands:gravel_defiled", "defiledlands:sand_defiled", "minecraft:soul_sand", "minecraft:farmland", "minecraft:ice",
         "minecraft:packed_ice"};
      public boolean enableAdvancedRecipeReplacement = true;
      public boolean enableBreakingChanges = true;
      public boolean enableRandomStoneWorldGenVariants = false;
      public boolean enableStoneDropChanges = true;
      public int looseRocksFrequency = 8;
      public boolean looseRocksGeneration = true;
      public boolean replaceLogRecipes = true;
      public boolean replaceVanillaRecipes = false;
   }
}
