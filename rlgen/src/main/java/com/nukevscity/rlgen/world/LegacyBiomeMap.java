package com.nukevscity.rlgen.world;

import com.nukevscity.rlgen.legacy.biome.Biome;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/** Which 1.21 biome stands in for each 1.12 biome (vanilla ones map to the closest 1.21 biome, mod ones to rlgen biomes). */
public final class LegacyBiomeMap {
   private static final Map<String, String> MAP = new LinkedHashMap<>();

   static {
      put("ocean", "minecraft:ocean");
      put("plains", "minecraft:plains");
      put("desert", "minecraft:desert");
      put("extreme_hills", "minecraft:windswept_hills");
      put("forest", "minecraft:forest");
      put("taiga", "minecraft:taiga");
      put("swampland", "minecraft:swamp");
      put("river", "minecraft:river");
      put("hell", "minecraft:nether_wastes");
      put("sky", "minecraft:the_end");
      put("frozen_ocean", "minecraft:frozen_ocean");
      put("frozen_river", "minecraft:frozen_river");
      put("ice_flats", "minecraft:snowy_plains");
      put("ice_mountains", "minecraft:snowy_plains");
      put("mushroom_island", "minecraft:mushroom_fields");
      put("mushroom_island_shore", "minecraft:mushroom_fields");
      put("beaches", "minecraft:beach");
      put("desert_hills", "minecraft:desert");
      put("forest_hills", "minecraft:forest");
      put("taiga_hills", "minecraft:taiga");
      put("smaller_extreme_hills", "minecraft:windswept_hills");
      put("jungle", "minecraft:jungle");
      put("jungle_hills", "minecraft:jungle");
      put("jungle_edge", "minecraft:sparse_jungle");
      put("deep_ocean", "minecraft:deep_ocean");
      put("stone_beach", "minecraft:stony_shore");
      put("cold_beach", "minecraft:snowy_beach");
      put("birch_forest", "minecraft:birch_forest");
      put("birch_forest_hills", "minecraft:birch_forest");
      put("roofed_forest", "minecraft:dark_forest");
      put("taiga_cold", "minecraft:snowy_taiga");
      put("taiga_cold_hills", "minecraft:snowy_taiga");
      put("redwood_taiga", "minecraft:old_growth_pine_taiga");
      put("redwood_taiga_hills", "minecraft:old_growth_pine_taiga");
      put("extreme_hills_with_trees", "minecraft:windswept_forest");
      put("savanna", "minecraft:savanna");
      put("savanna_rock", "minecraft:savanna_plateau");
      put("mesa", "minecraft:badlands");
      put("mesa_rock", "minecraft:wooded_badlands");
      put("mesa_clear_rock", "minecraft:badlands");
      put("void", "minecraft:the_void");
      put("mutated_plains", "minecraft:sunflower_plains");
      put("mutated_desert", "minecraft:desert");
      put("mutated_extreme_hills", "minecraft:windswept_gravelly_hills");
      put("mutated_forest", "minecraft:flower_forest");
      put("mutated_taiga", "minecraft:taiga");
      put("mutated_swampland", "minecraft:swamp");
      put("mutated_ice_flats", "minecraft:ice_spikes");
      put("mutated_jungle", "minecraft:jungle");
      put("mutated_jungle_edge", "minecraft:sparse_jungle");
      put("mutated_birch_forest", "minecraft:old_growth_birch_forest");
      put("mutated_birch_forest_hills", "minecraft:old_growth_birch_forest");
      put("mutated_roofed_forest", "minecraft:dark_forest");
      put("mutated_taiga_cold", "minecraft:snowy_taiga");
      put("mutated_redwood_taiga", "minecraft:old_growth_spruce_taiga");
      put("mutated_redwood_taiga_hills", "minecraft:old_growth_spruce_taiga");
      put("mutated_extreme_hills_with_trees", "minecraft:windswept_gravelly_hills");
      put("mutated_savanna", "minecraft:windswept_savanna");
      put("mutated_savanna_rock", "minecraft:windswept_savanna");
      put("mutated_mesa", "minecraft:eroded_badlands");
      put("mutated_mesa_rock", "minecraft:wooded_badlands");
      put("mutated_mesa_clear_rock", "minecraft:badlands");
   }

   private LegacyBiomeMap() {
   }

   private static void put(String legacy, String modern) {
      MAP.put(legacy, modern);
   }

   public static ResourceKey<net.minecraft.world.level.biome.Biome> modern(Biome legacy) {
      String name = legacy.getRegistryName();
      String modern = MAP.get(name);
      if (modern == null) {
         // traverse:cliffs -> rlgen:cliffs, defiledlands:hills_defiled -> rlgen:hills_defiled
         modern = "rlgen:" + name.substring(name.indexOf(':') + 1);
      }
      return ResourceKey.create(Registries.BIOME, ResourceLocation.parse(modern));
   }
}
