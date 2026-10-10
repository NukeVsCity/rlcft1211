package com.nukevscity.rlgen.legacy.biome;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyWorld;
import com.nukevscity.rlgen.legacy.noise.NoiseGeneratorPerlin;
import java.util.Arrays;
import java.util.Random;

/** 1.12.2 BiomeMesa terrain, copied as-is (including the pillar noise being seeded before worldSeed is set). */
public class BiomeMesa extends Biome {
   protected static final LBlock COARSE_DIRT = LBlock.COARSE_DIRT;
   protected static final LBlock GRASS = LBlock.GRASS;
   protected static final LBlock HARDENED_CLAY = LBlock.HARDENED_CLAY;
   protected static final LBlock STAINED_HARDENED_CLAY = LBlock.STAINED_HARDENED_CLAY;
   protected static final LBlock ORANGE_STAINED_HARDENED_CLAY = LBlock.ORANGE_STAINED_HARDENED_CLAY;
   protected static final LBlock RED_SAND = LBlock.RED_SAND;
   private LBlock[] clayBands;
   private long worldSeed;
   private NoiseGeneratorPerlin pillarNoise;
   private NoiseGeneratorPerlin pillarRoofNoise;
   private NoiseGeneratorPerlin clayBandsOffsetNoise;
   private final boolean brycePillars;
   private final boolean hasForest;

   public BiomeMesa(boolean flag, boolean flag1, Biome.BiomeProperties p) {
      super(p);
      this.brycePillars = flag;
      this.hasForest = flag1;
      this.topBlock = RED_SAND;
      this.fillerBlock = STAINED_HARDENED_CLAY;
   }

   @Override
   public void genTerrainBlocks(LegacyWorld world, Random random, ChunkPrimer chunkprimer, int i, int j, double d0) {
      if (this.clayBands == null || this.worldSeed != world.getSeed()) {
         this.generateBands(world.getSeed());
      }

      if (this.pillarNoise == null || this.pillarRoofNoise == null || this.worldSeed != world.getSeed()) {
         Random random1 = new Random(this.worldSeed);
         this.pillarNoise = new NoiseGeneratorPerlin(random1, 4);
         this.pillarRoofNoise = new NoiseGeneratorPerlin(random1, 1);
      }

      this.worldSeed = world.getSeed();
      double d5 = 0.0;
      if (this.brycePillars) {
         int k = (i & -16) + (j & 15);
         int l = (j & -16) + (i & 15);
         double d1 = Math.min(Math.abs(d0), this.pillarNoise.getValue((double)k * 0.25, (double)l * 0.25));
         if (d1 > 0.0) {
            double d3 = Math.abs(this.pillarRoofNoise.getValue((double)k * 0.001953125, (double)l * 0.001953125));
            d5 = d1 * d1 * 2.5;
            double d4 = Math.ceil(d3 * 50.0) + 14.0;
            if (d5 > d4) {
               d5 = d4;
            }

            d5 += 64.0;
         }
      }

      int i2 = i & 15;
      int j2 = j & 15;
      int k2 = world.getSeaLevel();
      LBlock iblockstate = STAINED_HARDENED_CLAY;
      LBlock iblockstate3 = this.fillerBlock;
      int i1 = (int)(d0 / 3.0 + 3.0 + random.nextDouble() * 0.25);
      boolean flag = Math.cos(d0 / 3.0 * Math.PI) > 0.0;
      int j1 = -1;
      boolean flag1 = false;
      int k1 = 0;

      for (int l1 = 255; l1 >= 0; l1--) {
         if (chunkprimer.getBlockState(j2, l1, i2).isAir() && l1 < (int)d5) {
            chunkprimer.setBlockState(j2, l1, i2, STONE);
         }

         if (l1 <= random.nextInt(5)) {
            chunkprimer.setBlockState(j2, l1, i2, BEDROCK);
         } else if (k1 < 15 || this.brycePillars) {
            LBlock iblockstate1 = chunkprimer.getBlockState(j2, l1, i2);
            if (iblockstate1.isAir()) {
               j1 = -1;
            } else if (iblockstate1.getBlock() == LBlock.STONE) {
               if (j1 == -1) {
                  flag1 = false;
                  if (i1 <= 0) {
                     iblockstate = AIR;
                     iblockstate3 = STONE;
                  } else if (l1 >= k2 - 4 && l1 <= k2 + 1) {
                     iblockstate = STAINED_HARDENED_CLAY;
                     iblockstate3 = this.fillerBlock;
                  }

                  if (l1 < k2 && (iblockstate == null || iblockstate.isAir())) {
                     iblockstate = WATER;
                  }

                  j1 = i1 + Math.max(0, l1 - k2);
                  if (l1 < k2 - 1) {
                     chunkprimer.setBlockState(j2, l1, i2, iblockstate3);
                     if (iblockstate3.getBlock() == LBlock.STAINED_HARDENED_CLAY) {
                        chunkprimer.setBlockState(j2, l1, i2, ORANGE_STAINED_HARDENED_CLAY);
                     }
                  } else if (!this.hasForest || l1 <= 86 + i1 * 2) {
                     if (l1 <= k2 + 3 + i1) {
                        chunkprimer.setBlockState(j2, l1, i2, this.topBlock);
                        flag1 = true;
                     } else {
                        LBlock iblockstate2;
                        if (l1 < 64 || l1 > 127) {
                           iblockstate2 = ORANGE_STAINED_HARDENED_CLAY;
                        } else if (flag) {
                           iblockstate2 = HARDENED_CLAY;
                        } else {
                           iblockstate2 = this.getBand(i, l1, j);
                        }

                        chunkprimer.setBlockState(j2, l1, i2, iblockstate2);
                     }
                  } else if (flag) {
                     chunkprimer.setBlockState(j2, l1, i2, COARSE_DIRT);
                  } else {
                     chunkprimer.setBlockState(j2, l1, i2, GRASS);
                  }
               } else if (j1 > 0) {
                  j1--;
                  if (flag1) {
                     chunkprimer.setBlockState(j2, l1, i2, ORANGE_STAINED_HARDENED_CLAY);
                  } else {
                     chunkprimer.setBlockState(j2, l1, i2, this.getBand(i, l1, j));
                  }
               }

               k1++;
            }
         }
      }
   }

   private void generateBands(long i) {
      this.clayBands = new LBlock[64];
      Arrays.fill(this.clayBands, HARDENED_CLAY);
      Random random = new Random(i);
      this.clayBandsOffsetNoise = new NoiseGeneratorPerlin(random, 1);

      for (int i2 = 0; i2 < 64; i2++) {
         i2 += random.nextInt(5) + 1;
         if (i2 < 64) {
            this.clayBands[i2] = ORANGE_STAINED_HARDENED_CLAY;
         }
      }

      int j2 = random.nextInt(4) + 2;

      for (int j = 0; j < j2; j++) {
         int k = random.nextInt(3) + 1;
         int l = random.nextInt(64);

         for (int i1 = 0; l + i1 < 64 && i1 < k; i1++) {
            this.clayBands[l + i1] = LBlock.YELLOW_STAINED_HARDENED_CLAY;
         }
      }

      int k2 = random.nextInt(4) + 2;

      for (int l2 = 0; l2 < k2; l2++) {
         int j3 = random.nextInt(3) + 2;
         int i4 = random.nextInt(64);

         for (int j1 = 0; i4 + j1 < 64 && j1 < j3; j1++) {
            this.clayBands[i4 + j1] = LBlock.BROWN_STAINED_HARDENED_CLAY;
         }
      }

      int i3 = random.nextInt(4) + 2;

      for (int k3 = 0; k3 < i3; k3++) {
         int j4 = random.nextInt(3) + 1;
         int l4 = random.nextInt(64);

         for (int k1 = 0; l4 + k1 < 64 && k1 < j4; k1++) {
            this.clayBands[l4 + k1] = LBlock.RED_STAINED_HARDENED_CLAY;
         }
      }

      int l3 = random.nextInt(3) + 3;
      int k4 = 0;

      for (int i5 = 0; i5 < l3; i5++) {
         k4 += random.nextInt(16) + 4;

         for (int l1 = 0; k4 + l1 < 64 && l1 < 1; l1++) {
            this.clayBands[k4 + l1] = STAINED_HARDENED_CLAY;
            if (k4 + l1 > 1 && random.nextBoolean()) {
               this.clayBands[k4 + l1 - 1] = LBlock.SILVER_STAINED_HARDENED_CLAY;
            }

            if (k4 + l1 < 63 && random.nextBoolean()) {
               this.clayBands[k4 + l1 + 1] = LBlock.SILVER_STAINED_HARDENED_CLAY;
            }
         }
      }
   }

   private LBlock getBand(int i, int j, int k) {
      int l = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)i / 512.0, (double)i / 512.0) * 2.0);
      return this.clayBands[(j + l + 64) % 64];
   }
}
