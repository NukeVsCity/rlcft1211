package com.nukevscity.rlgen.world;

import com.nukevscity.rlgen.legacy.biome.Biome;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/** Which 1.21 biome stands in for each 1.12 biome: every 1.12 biome has its own rlgen:<name> biome (see scripts/gen_worldgen_data.py). */
public final class LegacyBiomeMap {
   private static final Map<String, String> MAP = new LinkedHashMap<>();

   static {
      // Never generated in the overworld, but the registry needs a target.
      put("hell", "minecraft:nether_wastes");
      put("sky", "minecraft:the_end");
      put("void", "minecraft:the_void");
      put("iceandfire:glacier", "rlgen:iceandfire_glacier"); // traverse:glacier already is rlgen:glacier
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
         // extreme_hills -> rlgen:extreme_hills, traverse:cliffs -> rlgen:cliffs, defiledlands:hills_defiled -> rlgen:hills_defiled
         modern = "rlgen:" + name.substring(name.indexOf(':') + 1);
      }
      return ResourceKey.create(Registries.BIOME, ResourceLocation.parse(modern));
   }
}
