package com.github.alexthe666.iceandfire.entity;

import com.nukevscity.rlgen.rc.BiomeDict;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

/**
 * Forge 1.12 BiomeDictionary.hasType for the biome an Ice and Fire mob stands in. The rlgen:&lt;name&gt; biomes of the legacy world carry
 * their exact 1.12 types (rlgen BiomeDict); other 1.21 biomes get types from their 1.21 tags.
 */
public final class AirWaterBiomes {
   private static final Map<ResourceLocation, Set<String>> RLGEN = new HashMap<>();

   static {
      for (Map.Entry<String, Set<String>> e : BiomeDict.TYPES.entrySet()) {
         String name = e.getKey();
         String modern = name.equals("iceandfire:glacier") ? "iceandfire_glacier" : name.substring(name.indexOf(':') + 1);
         RLGEN.put(ResourceLocation.fromNamespaceAndPath("rlgen", modern), e.getValue());
      }
   }

   private AirWaterBiomes() {
   }

   public static Set<String> types(Holder<Biome> biome) {
      var key = biome.unwrapKey();
      if (key.isPresent()) {
         Set<String> exact = RLGEN.get(key.get().location());
         if (exact != null) {
            return exact;
         }
      }
      Set<String> t = new HashSet<>();
      if (biome.is(Tags.Biomes.IS_HOT)) t.add("HOT");
      if (biome.is(Tags.Biomes.IS_COLD)) t.add("COLD");
      if (biome.is(Tags.Biomes.IS_DRY)) t.add("DRY");
      if (biome.is(Tags.Biomes.IS_WET)) t.add("WET");
      if (biome.is(Tags.Biomes.IS_SPARSE_VEGETATION)) t.add("SPARSE");
      if (biome.is(Tags.Biomes.IS_DENSE_VEGETATION)) t.add("DENSE");
      if (biome.is(Tags.Biomes.IS_CONIFEROUS_TREE)) t.add("CONIFEROUS");
      if (biome.is(Tags.Biomes.IS_SPOOKY)) t.add("SPOOKY");
      if (biome.is(Tags.Biomes.IS_DEAD)) t.add("DEAD");
      if (biome.is(Tags.Biomes.IS_LUSH)) t.add("LUSH");
      if (biome.is(Tags.Biomes.IS_MUSHROOM)) t.add("MUSHROOM");
      if (biome.is(Tags.Biomes.IS_MAGICAL)) t.add("MAGICAL");
      if (biome.is(BiomeTags.IS_OCEAN)) t.add("OCEAN");
      if (biome.is(BiomeTags.IS_RIVER)) t.add("RIVER");
      if (biome.is(BiomeTags.IS_BEACH)) t.add("BEACH");
      if (biome.is(BiomeTags.IS_FOREST)) t.add("FOREST");
      if (biome.is(BiomeTags.IS_JUNGLE)) t.add("JUNGLE");
      if (biome.is(BiomeTags.IS_SAVANNA)) t.add("SAVANNA");
      if (biome.is(BiomeTags.IS_BADLANDS)) t.add("MESA");
      if (biome.is(BiomeTags.IS_HILL)) t.add("HILLS");
      if (biome.is(BiomeTags.IS_MOUNTAIN)) t.add("MOUNTAIN");
      if (biome.is(Tags.Biomes.IS_PLAINS)) t.add("PLAINS");
      if (biome.is(Tags.Biomes.IS_SWAMP)) t.add("SWAMP");
      if (biome.is(Tags.Biomes.IS_SNOWY)) t.add("SNOWY");
      if (biome.is(Tags.Biomes.IS_DESERT)) t.add("SANDY");
      if (biome.is(BiomeTags.IS_NETHER)) t.add("NETHER");
      if (biome.is(BiomeTags.IS_END)) t.add("END");
      return t;
   }

   public static boolean hasType(Holder<Biome> biome, String type) {
      return types(biome).contains(type);
   }
}
