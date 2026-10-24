package com.nukevscity.rlgen.legacy.biome;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyWorld;
import java.util.Random;

/**
 * Traverse 1.12.2-1.6.0-69 and Defiled Lands 1.12.2-1.4.3 biome classes (terrain parts only),
 * from the jars in RLCraft 2.9.3. Each is its own class because GenLayer compares biome classes.
 */
public final class ModBiomes {
   private ModBiomes() {
   }

   // ---------------- Traverse ----------------

   public static class TraverseBiome extends Biome {
      public TraverseBiome(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeAutumnalWoods extends TraverseBiome {
      public BiomeAutumnalWoods(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeAutumnalWoodedHills extends TraverseBiome {
      public BiomeAutumnalWoodedHills(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeWoodlands extends TraverseBiome {
      public BiomeWoodlands(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeMiniJungle extends TraverseBiome {
      public BiomeMiniJungle(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeMeadow extends TraverseBiome {
      public BiomeMeadow(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeLushHills extends TraverseBiome {
      public BiomeLushHills(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeConiferousForest extends TraverseBiome {
      public BiomeConiferousForest(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeRockyPlateau extends TraverseBiome {
      public BiomeRockyPlateau(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeLushSwamp extends TraverseBiome {
      public BiomeLushSwamp(Biome.BiomeProperties p) {
         super(p);
      }

      @Override
      public void genTerrainBlocks(LegacyWorld world, Random rand, ChunkPrimer primer, int x, int z, double noiseVal) {
         BiomeSwamp.lilyPadPass(primer, x, z);
         this.generateBiomeTerrain(world, rand, primer, x, z, noiseVal);
      }
   }

   /** Traverse BiomeForestedHills extends BiomeForest, so getBiomeClass() is BiomeForest.class. */
   public static class BiomeForestedHills extends BiomeForest {
      public BiomeForestedHills(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeCliffs extends TraverseBiome {
      public BiomeCliffs(Biome.BiomeProperties p) {
         super(p);
         this.topBlock = LBlock.STONE;
         this.fillerBlock = LBlock.STONE;
      }
   }

   public static class BiomeGlacier extends TraverseBiome {
      public BiomeGlacier(Biome.BiomeProperties p) {
         super(p);
         this.topBlock = LBlock.SNOW;
         this.fillerBlock = LBlock.ICE;
      }
   }

   public static class BiomeBadlands extends TraverseBiome {
      public BiomeBadlands(Biome.BiomeProperties p) {
         super(p);
      }

      @Override
      public void genTerrainBlocks(LegacyWorld world, Random rand, ChunkPrimer primer, int x, int z, double noiseVal) {
         if (noiseVal > 1.5) {
            this.topBlock = LBlock.STONE;
            this.fillerBlock = LBlock.STONE;
         } else {
            this.topBlock = LBlock.GRASS;
            this.fillerBlock = LBlock.DIRT;
         }
         this.generateBiomeTerrain(world, rand, primer, x, z, noiseVal);
      }
   }

   public static class BiomeThicket extends TraverseBiome {
      public BiomeThicket(Biome.BiomeProperties p) {
         super(p);
      }

      @Override
      public void genTerrainBlocks(LegacyWorld world, Random rand, ChunkPrimer primer, int x, int z, double noiseVal) {
         if (noiseVal > 1.5) {
            this.topBlock = LBlock.GRASS;
            this.fillerBlock = LBlock.DIRT;
         } else {
            this.topBlock = LBlock.COARSE_DIRT;
            this.fillerBlock = LBlock.DIRT;
         }
         this.generateBiomeTerrain(world, rand, primer, x, z, noiseVal);
      }
   }

   public static class BiomeRockyPlains extends BiomePlains {
      public BiomeRockyPlains(Biome.BiomeProperties p) {
         super(p);
      }

      @Override
      public void genTerrainBlocks(LegacyWorld world, Random rand, ChunkPrimer primer, int x, int z, double noiseVal) {
         if (noiseVal > 1.6) {
            this.topBlock = LBlock.STONE;
            this.fillerBlock = LBlock.STONE;
         } else {
            this.topBlock = LBlock.GRASS;
            this.fillerBlock = LBlock.DIRT;
         }
         this.generateBiomeTerrain(world, rand, primer, x, z, noiseVal);
      }
   }

   public static class BiomeRedDesert extends BiomeDesert {
      public BiomeRedDesert(Biome.BiomeProperties p) {
         super(p);
         this.topBlock = LBlock.RED_SAND;
         this.fillerBlock = LBlock.RED_SAND;
      }
   }

   public static class BiomeMountainousDesert extends BiomeDesert {
      public BiomeMountainousDesert(Biome.BiomeProperties p) {
         super(p);
      }
   }

   /** Arid Highland (grass above noise 1.0) and Desert Shrubland (above 1.5). */
   public static class BiomeGrassyDesert extends BiomeDesert {
      private final double grassNoise;

      public BiomeGrassyDesert(double grassNoise, Biome.BiomeProperties p) {
         super(p);
         this.grassNoise = grassNoise;
      }

      @Override
      public void genTerrainBlocks(LegacyWorld world, Random rand, ChunkPrimer primer, int x, int z, double noiseVal) {
         if (noiseVal > this.grassNoise) {
            this.topBlock = LBlock.GRASS;
            this.fillerBlock = LBlock.SAND;
         } else {
            this.topBlock = LBlock.SAND;
            this.fillerBlock = LBlock.SAND;
         }
         this.generateBiomeTerrain(world, rand, primer, x, z, noiseVal);
      }
   }

   public static class BiomeAridHighland extends BiomeGrassyDesert {
      public BiomeAridHighland(Biome.BiomeProperties p) {
         super(1.0, p);
      }
   }

   public static class BiomeDesertShrubland extends BiomeGrassyDesert {
      public BiomeDesertShrubland(Biome.BiomeProperties p) {
         super(1.5, p);
      }
   }

   /** Canyon (red rock) and Crag Cliffs (blue rock): Traverse's copy of generateBiomeTerrain with extra rock layering. */
   public abstract static class BiomeRockTerrain extends TraverseBiome {
      private final LBlock rock;

      protected BiomeRockTerrain(LBlock rock, LBlock filler, Biome.BiomeProperties p) {
         super(p);
         this.rock = rock;
         this.topBlock = rock;
         this.fillerBlock = filler;
      }

      @Override
      public void genTerrainBlocks(LegacyWorld worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
         int i = worldIn.getSeaLevel();
         LBlock iblockstate = this.topBlock;
         LBlock iblockstate1 = this.fillerBlock;
         int j = -1;
         int k = (int)(noiseVal / 3.0 + 3.0 + rand.nextDouble() * 0.25);
         int l = x & 15;
         int i1 = z & 15;

         for (int j1 = 255; j1 >= 0; j1--) {
            if (j1 <= rand.nextInt(5)) {
               chunkPrimerIn.setBlockState(i1, j1, l, BEDROCK);
            } else {
               LBlock iblockstate2 = chunkPrimerIn.getBlockState(i1, j1, l);
               if (iblockstate2.isAir()) {
                  j = -1;
               } else if (iblockstate2.getBlock() == LBlock.STONE) {
                  if (j == -1) {
                     if (k <= 0) {
                        iblockstate = AIR;
                        iblockstate1 = STONE;
                     } else if (j1 >= i - 4 && j1 <= i + 1) {
                        iblockstate = this.topBlock;
                        iblockstate1 = this.fillerBlock;
                     }

                     if (j1 < i && (iblockstate == null || iblockstate.isAir())) {
                        if (this.getTemperature(x, j1, z) < 0.15F) {
                           iblockstate = ICE;
                        } else {
                           iblockstate = WATER;
                        }
                     }

                     j = k;
                     if (j1 >= i - 1) {
                        chunkPrimerIn.setBlockState(i1, j1, l, iblockstate);
                     } else if (j1 < i - 7 - k) {
                        iblockstate = AIR;
                        iblockstate1 = STONE;
                        chunkPrimerIn.setBlockState(i1, j1, l, GRAVEL);
                     } else {
                        chunkPrimerIn.setBlockState(i1, j1, l, iblockstate1);
                     }
                  } else if (j > 0) {
                     j--;
                     chunkPrimerIn.setBlockState(i1, j1, l, iblockstate1);
                     if (j == 0 && iblockstate1.getBlock() == LBlock.SAND && k > 1) {
                        j = rand.nextInt(4) + Math.max(0, j1 - 63);
                        iblockstate1 = iblockstate1 == LBlock.RED_SAND ? RED_SANDSTONE : SANDSTONE;
                     }

                     if (j == 0 && iblockstate == this.rock && k > 1) {
                        j = rand.nextInt(4) + Math.max(0, j1 - 63);
                        iblockstate = this.rock;
                     }

                     if (j == 0 && iblockstate1 == this.rock && k > 1) {
                        j = rand.nextInt(4) + Math.max(0, j1 - 63);
                        iblockstate1 = this.rock;
                     }
                  }
               }
            }
         }
      }
   }

   public static class BiomeCanyon extends BiomeRockTerrain {
      public BiomeCanyon(Biome.BiomeProperties p) {
         super(LBlock.TRAVERSE_RED_ROCK, LBlock.TRAVERSE_RED_ROCK, p);
      }
   }

   public static class BiomeCragCliffs extends BiomeRockTerrain {
      public BiomeCragCliffs(Biome.BiomeProperties p) {
         super(LBlock.TRAVERSE_BLUE_ROCK, LBlock.STONE, p);
      }
   }

   // ---------------- Defiled Lands ----------------

   public abstract static class BiomeDefiled extends Biome {
      public BiomeDefiled(Biome.BiomeProperties p) {
         super(p);
         this.topBlock = LBlock.DEFILED_GRASS;
         this.fillerBlock = LBlock.DEFILED_DIRT;
      }
   }

   public static class BiomeDesertDefiled extends BiomeDefiled {
      public BiomeDesertDefiled(Biome.BiomeProperties p) {
         super(p);
         this.topBlock = LBlock.DEFILED_SAND;
         this.fillerBlock = LBlock.DEFILED_SAND;
      }
   }

   public static class BiomePlainsDefiled extends BiomeDefiled {
      public BiomePlainsDefiled(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeForestTenebra extends BiomeDefiled {
      public BiomeForestTenebra(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeForestVilespine extends BiomeDefiled {
      public BiomeForestVilespine(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeSwampDefiled extends BiomeDefiled {
      public BiomeSwampDefiled(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeIcePlainsDefiled extends BiomeDefiled {
      public BiomeIcePlainsDefiled(Biome.BiomeProperties p) {
         super(p);
      }
   }

   public static class BiomeHillsDefiled extends BiomeDefiled {
      public BiomeHillsDefiled(Biome.BiomeProperties p) {
         super(p);
      }

      @Override
      public void genTerrainBlocks(LegacyWorld worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
         this.topBlock = LBlock.DEFILED_GRASS;
         this.fillerBlock = LBlock.DEFILED_DIRT;
         if (noiseVal > 1.0) {
            this.topBlock = STONE;
            this.fillerBlock = STONE;
         }
         this.generateBiomeTerrain(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
      }
   }

   /** Ice and Fire 1.12 BiomeGlacier (BiomeSnow without spikes): snow on packed ice with ice streaks. */
   public static class BiomeIceAndFireGlacier extends BiomeSnow {
      public BiomeIceAndFireGlacier(Biome.BiomeProperties p) {
         super(false, p);
         this.topBlock = LBlock.SNOW;
         this.fillerBlock = LBlock.PACKED_ICE;
      }

      @Override
      public void genTerrainBlocks(LegacyWorld worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
         int i = worldIn.getSeaLevel();
         LBlock iblockstate = this.topBlock;
         LBlock iblockstate1 = rand.nextInt(5) == 0 ? LBlock.ICE : this.fillerBlock;
         int j = -1;
         int k = (int)(noiseVal / 3.0 + 3.0 + rand.nextDouble() * 0.25);
         int l = x & 15;
         int i1 = z & 15;
         for (int j1 = 255; j1 >= 0; j1--) {
            if (j1 <= rand.nextInt(5)) {
               chunkPrimerIn.setBlockState(i1, j1, l, BEDROCK);
            } else {
               LBlock iblockstate2 = chunkPrimerIn.getBlockState(i1, j1, l);
               if (iblockstate2.isAir()) {
                  j = -1;
               } else if (iblockstate2.getBlock() == LBlock.STONE) {
                  if (j == -1) {
                     iblockstate = this.topBlock;
                     iblockstate1 = rand.nextInt(5) == 0 ? LBlock.ICE : this.fillerBlock;
                     if (j1 < i && (iblockstate == null || iblockstate.isAir())) {
                        iblockstate = this.getTemperature(x, j1, z) < 0.15F ? ICE : WATER;
                     }
                     j = k;
                     if (j1 >= i - 1) {
                        chunkPrimerIn.setBlockState(i1, j1, l, iblockstate);
                     } else if (j1 < i - 7 - k) {
                        iblockstate = this.topBlock;
                        iblockstate1 = rand.nextInt(5) == 0 ? LBlock.ICE : this.fillerBlock;
                        chunkPrimerIn.setBlockState(i1, j1, l, GRAVEL);
                     } else {
                        chunkPrimerIn.setBlockState(i1, j1, l, iblockstate1);
                     }
                  } else if (j > 0) {
                     j--;
                     chunkPrimerIn.setBlockState(i1, j1, l, iblockstate1);
                     if (j == 0 && (iblockstate1.getBlock() == LBlock.ICE || iblockstate1.getBlock() == this.fillerBlock.getBlock()) && k > 1) {
                        j = rand.nextInt(4) + Math.max(0, j1 - 63);
                     }
                  }
               }
            }
         }
      }
   }
}
