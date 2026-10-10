package greymerk.roguelike.compat;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.rc.BiomeDict;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Forge 1.12 BiomeDictionary over the 1.12 (legacy) biomes of the rlgen world. */
public final class BiomeDictionary {
   private BiomeDictionary() {
   }

   public enum Type {
      HOT, COLD, SPARSE, DENSE, WET, DRY, SAVANNA, CONIFEROUS, JUNGLE, SPOOKY, DEAD, LUSH, NETHER, END, MUSHROOM, MAGICAL, RARE, OCEAN, RIVER, WATER,
      MESA, FOREST, PLAINS, MOUNTAIN, HILLS, SWAMP, SANDY, SNOWY, WASTELAND, BEACH, VOID;

      /** Type.getType: unknown names made a new (empty) type in Forge; here they give null and are dropped. */
      public static Type getType(String name, Type... subTypes) {
         try {
            return valueOf(name.toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException e) {
            return null;
         }
      }
   }

   public static boolean hasType(Biome biome, Type type) {
      if (biome == null || type == null) {
         return false;
      }
      Set<String> types = BiomeDict.TYPES.getOrDefault(biome.getRegistryName(), Set.of());
      return types.contains(type.name()) || type == Type.WATER && (types.contains("OCEAN") || types.contains("RIVER"));
   }

   public static List<Biome> getBiomes(Type type) {
      List<Biome> out = new ArrayList<>();
      if (type == null) {
         return out;
      }
      for (Biome b : Biome.all()) {
         if (hasType(b, type)) {
            out.add(b);
         }
      }
      return out;
   }
}
