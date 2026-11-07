package com.nukevscity.rlgen.rc;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import java.util.Random;
import java.util.Set;

/** The 1.12 dimensions RC generated in, with their DimensionDictionary types (DimensionDictionary.registerDefaults). */
public enum RcDim {
   OVERWORLD(0, Set.of("MC_DEFAULT", "REAL", "INFINITE", "NO_TOP_LIMIT", "BOTTOM_LIMIT", "PLANET_SURFACE", "EARTH")),
   NETHER(-1, Set.of("MC_DEFAULT", "REAL", "INFINITE", "TOP_LIMIT", "BOTTOM_LIMIT", "HELL")),
   END(1, Set.of("MC_DEFAULT", "REAL", "FINITE", "NO_TOP_LIMIT", "NO_BOTTOM_LIMIT", "BOSS_ARENA", "ENDER"));

   public final int id;
   final Set<String> types;

   RcDim(int id, Set<String> types) {
      this.id = id;
      this.types = types;
   }

   /** DimensionDictionary.dimensionMatchesType (only UNREAL has subtypes, none of which these dimensions have). */
   public boolean hasType(String type) {
      return this.types.contains(type);
   }

   public static RcDim byName(String s) {
      return switch (s) {
         case "end" -> END;
         case "nether" -> NETHER;
         default -> OVERWORLD;
      };
   }

   /** The single biome of the 1.12 End (BiomeProviderSingle "sky") and nether ("hell"). */
   public Biome fixedBiome() {
      return switch (this) {
         case END -> Biomes.SKY;
         case NETHER -> Biomes.HELL;
         default -> null;
      };
   }

   /**
    * The random ChunkGeneratorEnd/ChunkGeneratorHell.populate handed to PopulateChunkEvent.Pre: the generator's own field, never reseeded
    * for populating. It is left as generateChunk set it for the chunk whose generation triggered the populate (Chunk.populate: the last
    * of the 2x2 chunks x..x+1, z..z+1). That depends on exploration order; we take the one farthest from 0,0 (worlds explored outwards).
    * The End random has no world seed; the nether one has drawn the buildSurfaces randoms (which don't depend on the terrain).
    */
   public Random populateRandom(int cx, int cz) {
      int tx = cx >= 0 ? cx + 1 : cx;
      int tz = cz >= 0 ? cz + 1 : cz;
      Random rand = new Random((long)tx * 341873128712L + (long)tz * 132897987541L);
      if (this == NETHER) {
         // ChunkGeneratorHell.buildSurfaces
         for (int l = 0; l < 16; l++) {
            for (int i1 = 0; i1 < 16; i1++) {
               rand.nextDouble();
               rand.nextDouble();
               rand.nextDouble();
               for (int l1 = 127; l1 >= 0; l1--) {
                  if (l1 < 127 - rand.nextInt(5)) {
                     rand.nextInt(5);
                  }
               }
            }
         }
      }
      return rand;
   }
}
