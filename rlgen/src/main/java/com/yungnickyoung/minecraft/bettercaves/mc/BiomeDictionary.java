package com.yungnickyoung.minecraft.bettercaves.mc;

import com.yungnickyoung.minecraft.bettercaves.mc.BiomeDictionary;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.rc.BiomeDict;
import java.util.Set;

/** rlgen: Forge BiomeDictionary over the 1.12 biome types of the rlgen world. */
public final class BiomeDictionary {
   public enum Type {
      OCEAN
   }

   private BiomeDictionary() {
   }

   public static boolean hasType(Biome biome, Type type) {
      return BiomeDict.TYPES.getOrDefault(biome.getRegistryName(), Set.of()).contains(type.name());
   }
}
