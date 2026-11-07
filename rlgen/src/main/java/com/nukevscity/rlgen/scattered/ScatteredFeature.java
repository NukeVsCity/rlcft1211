package com.nukevscity.rlgen.scattered;

import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 1.12 MapGenScatteredFeature (no RLCraft mod changed its biome list; Recurrent Complex hooks it but has no scattered_feature
 * decorations, so vanilla generates). Starts are found like 1.12 (spacing 32, separation 8, salt 14357617, biome at the chunk's
 * centre on the full-resolution 1.12 biome map) and built with the MapGenBase random; their pieces are placed in
 * {@link #generate} (MapGenStructure.generateStructure in populate) with the shared populate random. The pieces' state
 * (ground level, placed chests, witch) is kept with the world like 1.12's MapGenStructureData "Temple".
 */
public final class ScatteredFeature {
   static final int SPACING = 32;
   static final int SEPARATION = 8;
   static final int SALT = 14357617;
   private static final List<Biome> BIOMELIST = List.of(Biomes.DESERT, Biomes.DESERT_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.SWAMPLAND,
      Biomes.ICE_PLAINS, Biomes.COLD_TAIGA);

   private ScatteredFeature() {
   }

   /** MapGenScatteredFeature.canSpawnStructureAtCoords */
   static boolean isStartChunk(long seed, int cx, int cz) {
      int i = cx;
      int j = cz;
      if (i < 0) {
         i -= SPACING - 1;
      }
      if (j < 0) {
         j -= SPACING - 1;
      }
      int i1 = i / SPACING;
      int j1 = j / SPACING;
      // World.setRandomSeed
      Random random = new Random((long)i1 * 341873128712L + (long)j1 * 132897987541L + seed + (long)SALT);
      i1 *= SPACING;
      j1 *= SPACING;
      i1 += random.nextInt(SPACING - SEPARATION);
      j1 += random.nextInt(SPACING - SEPARATION);
      return cx == i1 && cz == j1;
   }

   /** The start of chunk cx, cz (MapGenBase.generate + MapGenStructure.recursiveGenerate + Start), or null. */
   public static ScatteredPieces.Feature start(LegacyGenerator legacy, int cx, int cz) {
      long seed = legacy.getSeed();
      if (!isStartChunk(seed, cx, cz)) {
         return null;
      }
      Biome biome;
      synchronized (LegacyGenerator.LOCK) {
         biome = legacy.getBiome(cx * 16 + 8, cz * 16 + 8);
      }
      if (!BIOMELIST.contains(biome)) {
         return null;
      }
      Random rand = new Random(seed);
      long l = rand.nextLong();
      long i1 = rand.nextLong();
      rand.setSeed((long)cx * l ^ (long)cz * i1 ^ seed);
      rand.nextInt();
      // MapGenScatteredFeature.Start
      if (biome == Biomes.JUNGLE || biome == Biomes.JUNGLE_HILLS) {
         return new ScatteredPieces.JunglePyramid(rand, cx * 16, cz * 16);
      } else if (biome == Biomes.SWAMPLAND) {
         return new ScatteredPieces.SwampHut(rand, cx * 16, cz * 16);
      } else if (biome == Biomes.DESERT || biome == Biomes.DESERT_HILLS) {
         return new ScatteredPieces.DesertPyramid(rand, cx * 16, cz * 16);
      } else {
         return new ScatteredPieces.Igloo(rand, cx * 16, cz * 16);
      }
   }

   /** MapGenStructure.generateStructure(world, rand, chunkPos) for the populate box of chunk cp. */
   public static void generate(DWorld w, Random rand, ChunkPos cp, LegacyGenerator legacy) {
      int x = (cp.x << 4) + 8;
      int z = (cp.z << 4) + 8;
      ScatteredPieces.Box box = ScatteredPieces.Box.populate(x, z, x + 15, z + 15);
      Data data = Data.current;
      synchronized (data) {
         // a start's pieces lie in its own chunk and the next one, so only these four starts can reach the box
         for (int sx = cp.x; sx <= cp.x + 1; sx++) {
            for (int sz = cp.z; sz <= cp.z + 1; sz++) {
               ScatteredPieces.Feature f = data.get(legacy, sx, sz);
               if (f != null && f.boundingBox.intersects(box)) {
                  if (f.addComponentParts(w, rand, box)) {
                     data.store(sx, sz, f);
                  }
               }
            }
         }
      }
   }

   /** MapGenStructureData "Temple": the starts' state, plus the starts built so far. */
   public static final class Data extends SavedData {
      private static final String NAME = "rlgen_scattered";
      static volatile Data current = new Data();
      private final Map<Long, CompoundTag> saved = new HashMap<>();
      private final Map<Long, ScatteredPieces.Feature> starts = new HashMap<>();
      private final Map<Long, Boolean> none = new HashMap<>();

      /** Called when the overworld loads. */
      public static void attach(ServerLevel overworld) {
         current = overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Data::new, Data::load, null), NAME);
      }

      ScatteredPieces.Feature get(LegacyGenerator legacy, int cx, int cz) {
         if (!isStartChunk(legacy.getSeed(), cx, cz)) {
            return null;
         }
         long key = ChunkPos.asLong(cx, cz);
         ScatteredPieces.Feature f = this.starts.get(key);
         if (f != null || this.none.containsKey(key)) {
            return f;
         }
         f = start(legacy, cx, cz);
         if (f == null) {
            this.none.put(key, Boolean.TRUE);
            return null;
         }
         CompoundTag tag = this.saved.get(key);
         if (tag != null) {
            f.load(tag);
         }
         this.starts.put(key, f);
         return f;
      }

      void store(int cx, int cz, ScatteredPieces.Feature f) {
         CompoundTag tag = new CompoundTag();
         f.save(tag);
         this.saved.put(ChunkPos.asLong(cx, cz), tag);
         this.setDirty();
      }

      private static Data load(CompoundTag tag, HolderLookup.Provider provider) {
         Data d = new Data();
         CompoundTag starts = tag.getCompound("starts");
         for (String k : starts.getAllKeys()) {
            d.saved.put(Long.parseLong(k), starts.getCompound(k));
         }
         return d;
      }

      @Override
      public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
         CompoundTag starts = new CompoundTag();
         synchronized (this) {
            this.saved.forEach((k, v) -> starts.put(Long.toString(k), v.copy()));
         }
         tag.put("starts", starts);
         return tag;
      }
   }
}
