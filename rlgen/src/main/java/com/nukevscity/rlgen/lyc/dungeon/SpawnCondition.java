package com.nukevscity.rlgen.lyc.dungeon;

import com.google.gson.JsonObject;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.yungnickyoung.minecraft.bettermineshafts.mc.World;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

/**
 * Lycanites' WorldSpawnCondition as dungeon schematics use it: dimension ids (always the overworld, 0, here) and biome ids/tags with
 * white- or blacklists. Times, weather and difficulty don't appear in RLCraft's dungeon schematics.
 */
public class SpawnCondition {
   private int[] dimensionIds = new int[0];
   private String dimensionListType = "whitelist";
   private final List<String> biomeIds = new ArrayList<>();
   private String biomeIdListType = "whitelist";
   private final List<String> biomeTags = new ArrayList<>();
   private String biomeTagListType = "whitelist";
   private List<Biome> biomesFromTags;

   public static SpawnCondition createFromJSON(JsonObject json) {
      SpawnCondition c = new SpawnCondition();
      if (json.has("dimensionIds")) {
         c.dimensionIds = JSONHelper.getJsonStrings(json.getAsJsonArray("dimensionIds")).stream().mapToInt(Integer::parseInt).toArray();
      }
      if (json.has("dimensionListType")) {
         c.dimensionListType = json.get("dimensionListType").getAsString();
      }
      if (json.has("biomeIds")) {
         c.biomeIds.addAll(JSONHelper.getJsonStrings(json.getAsJsonArray("biomeIds")));
      }
      if (json.has("biomeIdListType")) {
         c.biomeIdListType = json.get("biomeIdListType").getAsString();
      }
      if (json.has("biomeTags")) {
         c.biomeTags.addAll(JSONHelper.getJsonStrings(json.getAsJsonArray("biomeTags")));
      }
      if (json.has("biomeTagListType")) {
         c.biomeTagListType = json.get("biomeTagListType").getAsString();
      }
      return c;
   }

   public boolean isMet(World world, Object player, BlockPos position) {
      if (this.dimensionIds.length > 0) {
         boolean found = false;
         for (int id : this.dimensionIds) {
            if (id == 0) {
               found = true;
               break;
            }
         }
         if (!found && "whitelist".equalsIgnoreCase(this.dimensionListType)) {
            return false;
         }
         if (found && "blacklist".equalsIgnoreCase(this.dimensionListType)) {
            return false;
         }
      }
      return this.isAllowedBiome(world, position);
   }

   private boolean isAllowedBiome(World world, BlockPos position) {
      if (position != null) {
         Biome biome = world.getBiome(position);
         if (!this.biomeIds.isEmpty()) {
            if (biome != null && this.biomeIds.contains(biome.getRegistryName().contains(":") ? biome.getRegistryName() : "minecraft:" + biome.getRegistryName())) {
               return !"blacklist".equalsIgnoreCase(this.biomeIdListType);
            }
         }
         if (!this.biomeTags.isEmpty()) {
            if (this.biomesFromTags == null) {
               this.biomesFromTags = JSONHelper.getBiomesFromTags(this.biomeTags);
            }
            if (this.biomesFromTags.contains(biome)) {
               return !"blacklist".equalsIgnoreCase(this.biomeTagListType);
            }
         }
      }
      return this.biomeIds.isEmpty() && this.biomeTags.isEmpty()
         ? true
         : "blacklist".equalsIgnoreCase(this.biomeIdListType) && "blacklist".equalsIgnoreCase(this.biomeTagListType);
   }
}
