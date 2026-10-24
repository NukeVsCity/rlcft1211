package com.nukevscity.rlgen.legacy.layer;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Forge 1.12.2 BiomeManager weighted lists, as GenLayerBiome sees them.
 * Order matters: vanilla entries, then mod entries in the order they were added,
 * then (desert only) the desert/savanna/plains entries GenLayerBiome appends itself.
 */
public final class BiomeLists {
   public enum Type { DESERT, WARM, COOL, ICY }

   public static final class Entry {
      public final Biome biome;
      public final int itemWeight;

      public Entry(Biome biome, int weight) {
         this.biome = biome;
         this.itemWeight = weight;
      }
   }

   private final Map<Type, List<Entry>> lists = new EnumMap<>(Type.class);
   private final Map<Type, Boolean> modded = new EnumMap<>(Type.class);

   private BiomeLists() {
      for (Type t : Type.values()) {
         lists.put(t, new ArrayList<>());
         modded.put(t, false);
      }
      // BiomeManager.setupBiomes()
      lists.get(Type.WARM).add(new Entry(Biomes.FOREST, 10));
      lists.get(Type.WARM).add(new Entry(Biomes.ROOFED_FOREST, 10));
      lists.get(Type.WARM).add(new Entry(Biomes.EXTREME_HILLS, 10));
      lists.get(Type.WARM).add(new Entry(Biomes.PLAINS, 10));
      lists.get(Type.WARM).add(new Entry(Biomes.BIRCH_FOREST, 10));
      lists.get(Type.WARM).add(new Entry(Biomes.SWAMPLAND, 10));
      lists.get(Type.COOL).add(new Entry(Biomes.FOREST, 10));
      lists.get(Type.COOL).add(new Entry(Biomes.EXTREME_HILLS, 10));
      lists.get(Type.COOL).add(new Entry(Biomes.TAIGA, 10));
      lists.get(Type.COOL).add(new Entry(Biomes.PLAINS, 10));
      lists.get(Type.ICY).add(new Entry(Biomes.ICE_PLAINS, 30));
      lists.get(Type.ICY).add(new Entry(Biomes.COLD_TAIGA, 10));
   }

   /** BiomeManager.addBiome: marks the list as modded (TrackedList). */
   public BiomeLists add(Type type, Biome biome, int weight) {
      if (weight > 0) {
         lists.get(type).add(new Entry(biome, weight));
         modded.put(type, true);
      }
      return this;
   }

   /** Called once all mods have added their biomes: GenLayerBiome's constructor appends the vanilla desert entries. */
   private BiomeLists finish() {
      List<Entry> desert = lists.get(Type.DESERT);
      desert.add(new Entry(Biomes.DESERT, 30));
      desert.add(new Entry(Biomes.SAVANNA, 20));
      desert.add(new Entry(Biomes.PLAINS, 10));
      return this;
   }

   public List<Entry> get(Type type) {
      return lists.get(type);
   }

   public boolean isModded(Type type) {
      return modded.get(type);
   }

   public static BiomeLists vanilla() {
      return new BiomeLists().finish();
   }

   /**
    * RLCraft 2.9.3: Defiled Lands adds its biomes during the biome registry event (weights from
    * defiled_lands.cfg, all 1), then Ice and Fire its Glacier, Traverse adds its biomes later at init and again on server start
    * (re-added at the end, same order). So the order is vanilla, Defiled Lands, Traverse.
    */
   public static BiomeLists rlcraft() {
      BiomeLists l = new BiomeLists();
      // Defiled Lands ModBiomes.registerBiomes
      l.add(Type.DESERT, Biomes.DESERT_DEFILED, 1);
      l.add(Type.WARM, Biomes.PLAINS_DEFILED, 1);
      l.add(Type.WARM, Biomes.FOREST_TENEBRA, 1);
      l.add(Type.WARM, Biomes.FOREST_VILESPINE, 1);
      l.add(Type.COOL, Biomes.HILLS_DEFILED, 1);
      l.add(Type.WARM, Biomes.SWAMP_DEFILED, 1);
      l.add(Type.ICY, Biomes.ICE_PLAINS_DEFILED, 1);
      // Ice and Fire CommonProxy.registerBiomes: Glacier as a COOL biome, "Glacier Spawn Weight"=4
      l.add(Type.COOL, com.nukevscity.rlgen.legacy.biome.Biome.byName("iceandfire:glacier"), 4);
      // Traverse TraverseWorld.registerBiomes order, weights as registered
      l.add(Type.COOL, Biomes.AUTUMNAL_WOODS, 8);
      l.add(Type.WARM, Biomes.WOODLANDS, 9);
      l.add(Type.WARM, Biomes.MINI_JUNGLE, 3);
      l.add(Type.COOL, Biomes.MEADOW, 7);
      l.add(Type.WARM, Biomes.GREEN_SWAMP, 6);
      l.add(Type.DESERT, Biomes.RED_DESERT, 6);
      l.add(Type.COOL, Biomes.TEMPERATE_RAINFOREST, 8);
      l.add(Type.WARM, Biomes.BADLANDS, 5);
      l.add(Type.DESERT, Biomes.MOUNTAINOUS_DESERT, 2);
      l.add(Type.WARM, Biomes.ROCKY_PLATEAU, 4);
      l.add(Type.COOL, Biomes.FORESTED_HILLS, 6);
      l.add(Type.COOL, Biomes.BIRCH_FORESTED_HILLS, 2);
      l.add(Type.COOL, Biomes.AUTUMNAL_WOODED_HILLS, 1);
      l.add(Type.COOL, Biomes.CLIFFS, 2);
      l.add(Type.ICY, Biomes.GLACIER, 6);
      l.add(Type.ICY, Biomes.GLACIER_SPIKES, 2);
      l.add(Type.ICY, Biomes.SNOWY_CONIFEROUS_FOREST, 5);
      l.add(Type.COOL, Biomes.LUSH_HILLS, 6);
      l.add(Type.DESERT, Biomes.CANYON, 5);
      l.add(Type.COOL, Biomes.CRAG_CLIFFS, 4);
      l.add(Type.DESERT, Biomes.DESERT_SHRUBLAND, 5);
      l.add(Type.WARM, Biomes.THICKET, 6);
      l.add(Type.DESERT, Biomes.ARID_HIGHLAND, 4);
      l.add(Type.COOL, Biomes.ROCKY_PLAINS, 4);
      return l.finish();
   }

   public static int getTotalWeight(List<Entry> list) {
      int i = 0;
      for (Entry e : list) {
         i += e.itemWeight;
      }
      return i;
   }

   public static Entry getRandomItem(List<Entry> list, int i) {
      for (Entry t : list) {
         i -= t.itemWeight;
         if (i < 0) {
            return t;
         }
      }
      return null;
   }
}
