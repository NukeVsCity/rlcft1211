package com.github.alexthe666.iceandfire.world;

import com.nukevscity.rlgen.rc.BiomeDict;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Forge 1.12 BiomeDictionary types of a 1.21 biome: the rlgen:<name> biomes of the legacy world are the 1.12 biomes (types
 * from rlgen's generated table, incl. Traverse, Defiled Lands and the Ice and Fire glacier); any other biome falls back to the
 * c: convention tags.
 */
public final class LegacyBiomeTypes {
   private LegacyBiomeTypes() {
   }

   private static final Map<String, Set<String>> BY_MODERN = new HashMap<>();

   static {
      for (Map.Entry<String, Set<String>> e : BiomeDict.TYPES.entrySet()) {
         String name = e.getKey();
         String modern = name.equals("iceandfire:glacier") ? "iceandfire_glacier" : name.substring(name.indexOf(':') + 1);
         BY_MODERN.put("rlgen:" + modern, e.getValue());
      }
   }

   private static final String[][] FALLBACK = {
      {"FOREST", "is_forest"}, {"SNOWY", "is_snowy"}, {"MOUNTAIN", "is_mountain"}, {"SANDY", "is_sandy"}, {"DRY", "is_dry"},
      {"BEACH", "is_beach"}, {"MESA", "is_badlands"}, {"SAVANNA", "is_savanna"}, {"SPARSE", "is_sparse_vegetation"},
      {"JUNGLE", "is_jungle"}, {"HILLS", "is_hill"}, {"HOT", "is_hot"}, {"COLD", "is_cold"}, {"OCEAN", "is_ocean"}
   };

   public static Set<String> types(Holder<Biome> biome) {
      String key = biome.unwrapKey().map(k -> k.location().toString()).orElse("");
      Set<String> legacy = BY_MODERN.get(key);
      if (legacy != null) {
         return legacy;
      }
      Set<String> out = new HashSet<>();
      for (String[] f : FALLBACK) {
         if (biome.is(TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", f[1])))) {
            out.add(f[0]);
         }
      }
      return out;
   }

   public static boolean hasType(Holder<Biome> biome, String type) {
      return types(biome).contains(type);
   }
}
