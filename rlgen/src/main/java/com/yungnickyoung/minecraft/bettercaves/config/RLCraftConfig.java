package com.yungnickyoung.minecraft.bettercaves.config;

import com.yungnickyoung.minecraft.bettercaves.enums.RegionSize;

/** rlgen: RLCraft 2.9.3 config/bettercaves-1_12_2.cfg (advanced settings are the defaults, the file does not set them). */
public final class RLCraftConfig {
   private static boolean applied;

   private RLCraftConfig() {
   }

   public static synchronized void apply() {
      if (applied) {
         return;
      }
      applied = true;
      Configuration.enableGlobalWhitelist = false;
      Configuration.whitelistedDimensionIDs = new int[]{0, 10};
      var c = Configuration.caveSettings.caves;
      c.caveRegionSize = RegionSize.Small;
      c.customRegionSize = 0.008F;
      c.caveSpawnChance = 95.0F;
      c.cubicCave.xzCompression = 1.5F;
      c.cubicCave.yCompression = 3.4F;
      c.cubicCave.caveTop = 80;
      c.cubicCave.caveBottom = 1;
      c.cubicCave.cavePriority = 10;
      c.cubicCave.caveSurfaceCutoff = 15;
      c.simplexCave.xzCompression = 1.5F;
      c.simplexCave.yCompression = 3.4F;
      c.simplexCave.caveTop = 128;
      c.simplexCave.caveBottom = 10;
      c.simplexCave.cavePriority = 0;
      c.simplexCave.caveSurfaceCutoff = 15;
      c.surfaceCave.enableSurfaceCaves = true;
      c.surfaceCave.caveDensity = 17;
      c.surfaceCave.caveTop = 128;
      c.surfaceCave.caveBottom = 40;
      c.vanillaCave.caveDensity = 14;
      c.vanillaCave.caveTop = 128;
      c.vanillaCave.caveBottom = 8;
      c.vanillaCave.cavePriority = 3;
      var v = Configuration.caveSettings.caverns;
      v.cavernRegionSize = RegionSize.Small;
      v.customRegionSize = 0.009999999776482582F;
      v.cavernSpawnChance = 20.0F;
      v.liquidCavern.xzCompression = 0.4F;
      v.liquidCavern.yCompression = 1.0F;
      v.liquidCavern.cavernTop = 50;
      v.liquidCavern.cavernBottom = 28;
      v.liquidCavern.cavernPriority = 10;
      v.flooredCavern.xzCompression = 0.4F;
      v.flooredCavern.yCompression = 1.0F;
      v.flooredCavern.cavernTop = 28;
      v.flooredCavern.cavernBottom = 1;
      v.flooredCavern.cavernPriority = 10;
      var w = Configuration.caveSettings.waterRegions;
      w.waterRegionSize = RegionSize.Medium;
      w.waterRegionCustomSize = 0.004000000189989805F;
      w.waterRegionSpawnChance = 40.0F;
      Configuration.caveSettings.ravines.enableFloodedRavines = false;
      Configuration.caveSettings.ravines.enableVanillaRavines = true;
      var m = Configuration.caveSettings.miscellaneous;
      m.enableFloodedUnderground = false;
      m.lavaBlock = "minecraft:lava";
      m.liquidAltitude = 10;
      m.overrideSurfaceDetection = false;
      m.replaceFloatingGravel = true;
      m.waterBlock = "minecraft:water";
      Configuration.bedrockSettings.bedrockWidth = 1;
      Configuration.bedrockSettings.flattenBedrock = false;
      Configuration.debugsettings.debugVisualizer = false;
   }
}
