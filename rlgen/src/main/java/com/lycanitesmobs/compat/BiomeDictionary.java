package com.lycanitesmobs.compat;

import com.nukevscity.rlgen.rc.BiomeDict;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.12 net.minecraftforge.common.BiomeDictionary for Lycanites. Lycanites only compares biomes, so a biome is its id
 * ({@link LycBiomes}: rlgen:<legacy name> for the RLCraft overworld, one id for the whole nether and the whole end like 1.12).
 * The types are the 1.12 Forge types of the 1.12 biomes (rlgen BiomeDict: vanilla, Traverse, Defiled Lands, Ice and Fire).
 */
public final class BiomeDictionary {
   private BiomeDictionary() {
   }

   public static final class Type {
      private static final Map<String, Type> BY_NAME = new LinkedHashMap<>();
      public static final Type HOT = new Type("HOT");
      public static final Type COLD = new Type("COLD");
      public static final Type SPARSE = new Type("SPARSE");
      public static final Type DENSE = new Type("DENSE");
      public static final Type WET = new Type("WET");
      public static final Type DRY = new Type("DRY");
      public static final Type SAVANNA = new Type("SAVANNA");
      public static final Type CONIFEROUS = new Type("CONIFEROUS");
      public static final Type JUNGLE = new Type("JUNGLE");
      public static final Type SPOOKY = new Type("SPOOKY");
      public static final Type DEAD = new Type("DEAD");
      public static final Type LUSH = new Type("LUSH");
      public static final Type NETHER = new Type("NETHER");
      public static final Type END = new Type("END");
      public static final Type MUSHROOM = new Type("MUSHROOM");
      public static final Type MAGICAL = new Type("MAGICAL");
      public static final Type RARE = new Type("RARE");
      public static final Type OCEAN = new Type("OCEAN");
      public static final Type RIVER = new Type("RIVER");
      public static final Type WATER = new Type("WATER");
      public static final Type MESA = new Type("MESA");
      public static final Type FOREST = new Type("FOREST");
      public static final Type PLAINS = new Type("PLAINS");
      public static final Type MOUNTAIN = new Type("MOUNTAIN");
      public static final Type HILLS = new Type("HILLS");
      public static final Type SWAMP = new Type("SWAMP");
      public static final Type SANDY = new Type("SANDY");
      public static final Type SNOWY = new Type("SNOWY");
      public static final Type WASTELAND = new Type("WASTELAND");
      public static final Type BEACH = new Type("BEACH");
      public static final Type VOID = new Type("VOID");

      private final String name;

      private Type(String name) {
         this.name = name;
         BY_NAME.put(name, this);
      }

      public String getName() {
         return this.name;
      }

      @Override
      public String toString() {
         return this.name;
      }

      /** 1.12 Type.getType: an unknown name creates a new (empty) type like Forge did. */
      public static Type getType(String name, Type... subTypes) {
         name = name.toUpperCase(Locale.ENGLISH);
         Type type = BY_NAME.get(name);
         return type != null ? type : new Type(name);
      }

      public static Collection<Type> getAll() {
         return BY_NAME.values();
      }
   }

   /** 1.12 getBiomes(type): the ids of every biome with that type. */
   public static Set<ResourceLocation> getBiomes(Type type) {
      Set<ResourceLocation> set = new HashSet<>();
      for (Map.Entry<String, Set<String>> e : BiomeDict.TYPES.entrySet()) {
         if (e.getValue().contains(type.getName())) {
            set.add(LycBiomes.fromLegacy(e.getKey()));
         }
      }
      return set;
   }

   /** 1.12 getTypes(biome) */
   public static Set<Type> getTypes(ResourceLocation biome) {
      Set<Type> types = new HashSet<>();
      String legacy = LycBiomes.toLegacy(biome);
      if (legacy != null) {
         for (String t : BiomeDict.TYPES.getOrDefault(legacy, Set.of())) {
            types.add(Type.getType(t));
         }
      }
      return types;
   }

   public static boolean hasType(ResourceLocation biome, Type type) {
      return getTypes(biome).contains(type);
   }
}
