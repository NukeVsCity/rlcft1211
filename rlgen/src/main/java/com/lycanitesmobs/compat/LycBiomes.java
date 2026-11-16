package com.lycanitesmobs.compat;

import com.nukevscity.rlgen.rc.BiomeDict;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

/**
 * Biome ids standing in for 1.12 Biome objects in the Lycanites code. 1.12 had one nether biome (hell) and one end biome (sky);
 * every 1.21 nether/end biome answers that id so nether/end spawns behave like 1.12.
 */
public final class LycBiomes {
   public static final ResourceLocation HELL = ResourceLocation.withDefaultNamespace("nether_wastes");
   public static final ResourceLocation SKY = ResourceLocation.withDefaultNamespace("the_end");
   public static final ResourceLocation VOID = ResourceLocation.withDefaultNamespace("the_void");
   private static final Map<ResourceLocation, String> MODERN_TO_LEGACY = new HashMap<>();

   private LycBiomes() {
   }

   /** 1.12 biome registry name (minecraft:plains, traverse:cliffs, plains...) to the id used here. */
   public static ResourceLocation fromLegacy(String legacy) {
      if (legacy.startsWith("minecraft:")) {
         legacy = legacy.substring("minecraft:".length());
      }
      switch (legacy) {
         case "hell": return HELL;
         case "sky": return SKY;
         case "void": return VOID;
         case "iceandfire:glacier": return ResourceLocation.fromNamespaceAndPath("rlgen", "iceandfire_glacier");
         default: return ResourceLocation.fromNamespaceAndPath("rlgen", legacy.substring(legacy.indexOf(':') + 1));
      }
   }

   /** The 1.12 biome name (BiomeDict key) for an id, or null. */
   public static String toLegacy(ResourceLocation id) {
      if (MODERN_TO_LEGACY.isEmpty()) {
         for (String legacy : BiomeDict.TYPES.keySet()) {
            MODERN_TO_LEGACY.put(fromLegacy(legacy), legacy);
         }
      }
      return MODERN_TO_LEGACY.get(id);
   }

   /** 1.12 World.getBiome(pos) */
   public static ResourceLocation at(LevelReader level, BlockPos pos) {
      Holder<Biome> holder = level.getBiome(pos);
      if (level instanceof Level l) {
         if (l.dimension() == Level.NETHER) {
            return HELL;
         }
         if (l.dimension() == Level.END) {
            return SKY;
         }
      }
      return holder.unwrapKey().map(k -> k.location()).orElse(VOID);
   }

   public static ResourceLocation parse(String id) {
      return fromLegacy(id);
   }

   /** 1.12 biome registry getValue(id): the id of a known 1.12 biome, null for an unknown one. */
   public static ResourceLocation fromId(String id) {
      String legacy = id.startsWith("minecraft:") ? id.substring("minecraft:".length()) : id;
      if (legacy.equals("hell") || legacy.equals("sky") || legacy.equals("void") || BiomeDict.TYPES.containsKey(legacy)) {
         return fromLegacy(id);
      }
      return null;
   }
}
