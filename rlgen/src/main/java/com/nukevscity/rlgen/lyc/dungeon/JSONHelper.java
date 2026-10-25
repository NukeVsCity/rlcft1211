package com.nukevscity.rlgen.lyc.dungeon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nukevscity.rlgen.iaf.Mat112;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.rc.BiomeDict;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** The bits of Lycanites' JSONHelper the dungeon code uses. */
public final class JSONHelper {
   private JSONHelper() {
   }

   public static Vec3i getVec3i(JsonObject json, String memberName) {
      if (!json.has(memberName)) {
         return new Vec3i(0, 0, 0);
      }
      int[] coords = new int[3];
      int i = 0;
      for (JsonElement e : json.get(memberName).getAsJsonArray()) {
         if (i >= coords.length) {
            break;
         }
         coords[i++] = e.getAsInt();
      }
      return new Vec3i(coords[0], coords[1], coords[2]);
   }

   public static List<String> getJsonStrings(JsonArray jsonArray) {
      List<String> strings = new ArrayList<>();
      for (JsonElement e : jsonArray) {
         strings.add(e.getAsString());
      }
      return strings;
   }

   /** BiomeDictionary biomes for each tag ("-tag" removes, "ALL", "NONE"). */
   public static List<Biome> getBiomesFromTags(List<String> biomeTags) {
      List<Biome> biomeList = new ArrayList<>();
      for (String biomeEntry : biomeTags) {
         boolean additive = true;
         if (biomeEntry.charAt(0) == '-' || biomeEntry.charAt(0) == '+') {
            additive = biomeEntry.charAt(0) != '-';
            biomeEntry = biomeEntry.substring(1);
         }
         String type = biomeEntry.toUpperCase(Locale.ENGLISH);
         List<Biome> selected = new ArrayList<>();
         if (!"NONE".equals(type)) {
            for (Biome b : Biome.all()) {
               if (b != null && ("ALL".equals(type) ? !BiomeDict.TYPES.getOrDefault(b.getRegistryName(), Set.of()).isEmpty() : BiomeDict.hasType(b, type))) {
                  selected.add(b);
               }
            }
         }
         for (Biome b : selected) {
            if (additive) {
               if (!biomeList.contains(b)) {
                  biomeList.add(b);
               }
            } else {
               biomeList.remove(b);
            }
         }
      }
      return biomeList;
   }

   /** The material names of a placer's "materials" list. */
   public static Set<String> getJsonMaterials(JsonObject json) {
      Set<String> materials = new HashSet<>();
      if (json.has("materials")) {
         for (JsonElement e : json.getAsJsonArray("materials")) {
            materials.add(e.getAsString().toLowerCase(Locale.ENGLISH));
         }
      }
      return materials;
   }

   /** The 1.12 material name of a state, as the placer lists name them. */
   public static String material(BlockState s) {
      if (s.is(Blocks.CACTUS)) {
         return "cactus";
      }
      return Mat112.of(s).name().toLowerCase(Locale.ENGLISH);
   }
}
