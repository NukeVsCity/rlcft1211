package com.nukevscity.rlgen.legacy;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import com.nukevscity.rlgen.legacy.gen.MapGenBase;
import com.nukevscity.rlgen.legacy.gen.MapGenCaves;
import com.nukevscity.rlgen.legacy.gen.MapGenRavine;
import com.nukevscity.rlgen.legacy.layer.BiomeLists;
import com.nukevscity.rlgen.legacy.noise.NoiseGeneratorOctaves;
import com.nukevscity.rlgen.legacy.noise.NoiseGeneratorPerlin;
import java.util.Random;

/**
 * 1.12.2 ChunkGeneratorOverworld (WorldType.DEFAULT, empty generator options, Forge 14.23.5) up to
 * the end of generateChunk: terrain noise, biome surfaces, caves and ravines. Method bodies are
 * copied from the decompiled 1.12.2 jar. Not thread-safe: the 1.12 code keeps state in fields
 * and in the shared Biome objects, so callers synchronize on {@link #LOCK}.
 */
public final class LegacyGenerator implements LegacyWorld {
   public static final Object LOCK = new Object();

   private final long seed;
   private final Random rand;
   private final NoiseGeneratorOctaves minLimitPerlinNoise;
   private final NoiseGeneratorOctaves maxLimitPerlinNoise;
   private final NoiseGeneratorOctaves mainPerlinNoise;
   private final NoiseGeneratorPerlin surfaceNoise;
   public NoiseGeneratorOctaves scaleNoise;
   public NoiseGeneratorOctaves depthNoise;
   public NoiseGeneratorOctaves forestNoise;
   private final double[] heightMap;
   private final float[] biomeWeights;
   private final ChunkGeneratorSettings settings = new ChunkGeneratorSettings();
   private double[] depthBuffer = new double[256];
   private MapGenBase caveGenerator = new MapGenCaves();
   private MapGenBase ravineGenerator = new MapGenRavine();
   private final LegacyBiomeProvider biomeProvider;
   private Biome[] biomesForGeneration;
   double[] mainNoiseRegion;
   double[] minLimitRegion;
   double[] maxLimitRegion;
   double[] depthRegion;

   public LegacyGenerator(long i, BiomeLists lists) {
      Biomes.init();
      this.seed = i;
      this.biomeProvider = new LegacyBiomeProvider(i, lists);
      this.rand = new Random(i);
      this.minLimitPerlinNoise = new NoiseGeneratorOctaves(this.rand, 16);
      this.maxLimitPerlinNoise = new NoiseGeneratorOctaves(this.rand, 16);
      this.mainPerlinNoise = new NoiseGeneratorOctaves(this.rand, 8);
      this.surfaceNoise = new NoiseGeneratorPerlin(this.rand, 4);
      this.scaleNoise = new NoiseGeneratorOctaves(this.rand, 10);
      this.depthNoise = new NoiseGeneratorOctaves(this.rand, 16);
      this.forestNoise = new NoiseGeneratorOctaves(this.rand, 8);
      this.heightMap = new double[825];
      this.biomeWeights = new float[25];

      for (int j = -2; j <= 2; j++) {
         for (int k = -2; k <= 2; k++) {
            float f = 10.0F / MathHelper.sqrt((float)(j * j + k * k) + 0.2F);
            this.biomeWeights[j + 2 + (k + 2) * 5] = f;
         }
      }
   }

   /** RLCraft: Better Caves replaced the cave and ravine MapGens (InitMapGenEvent CAVE / RAVINE). */
   public LegacyGenerator withBetterCaves() {
      this.caveGenerator = new com.yungnickyoung.minecraft.bettercaves.world.MapGenBetterCaves();
      this.ravineGenerator = new com.yungnickyoung.minecraft.bettercaves.world.ravine.MapGenBetterRavine();
      return this;
   }

   @Override
   public long getSeed() {
      return this.seed;
   }

   @Override
   public int getSeaLevel() {
      return this.settings.seaLevel;
   }

   @Override
   public Biome getBiome(int x, int z) {
      return this.biomeProvider.getBiome(x, z);
   }

   public LegacyBiomeProvider getBiomeProvider() {
      return this.biomeProvider;
   }

   /** Result of {@link #generateChunk}: the primer and the 16x16 biome array (index x + z * 16). */
   public static final class GeneratedChunk {
      public final ChunkPrimer primer;
      public final Biome[] biomes;

      GeneratedChunk(ChunkPrimer primer, Biome[] biomes) {
         this.primer = primer;
         this.biomes = biomes;
      }
   }

   /** ChunkGeneratorOverworld.generateChunk without the structure MapGens (they place no blocks here). */
   public GeneratedChunk generateChunk(int i, int j) {
      synchronized (LOCK) {
         this.rand.setSeed((long)i * 341873128712L + (long)j * 132897987541L);
         ChunkPrimer chunkprimer = new ChunkPrimer();
         this.setBlocksInChunk(i, j, chunkprimer);
         this.biomesForGeneration = this.biomeProvider.getBiomes(this.biomesForGeneration, i * 16, j * 16, 16, 16);
         this.replaceBiomeBlocks(i, j, chunkprimer, this.biomesForGeneration);
         if (this.settings.useCaves) {
            this.caveGenerator.generate(this, i, j, chunkprimer);
         }

         if (this.settings.useRavines) {
            this.ravineGenerator.generate(this, i, j, chunkprimer);
         }

         return new GeneratedChunk(chunkprimer, this.biomesForGeneration.clone());
      }
   }

   public void setBlocksInChunk(int i, int j, ChunkPrimer chunkprimer) {
      this.biomesForGeneration = this.biomeProvider.getBiomesForGeneration(this.biomesForGeneration, i * 4 - 2, j * 4 - 2, 10, 10);
      this.generateHeightmap(i * 4, 0, j * 4);

      for (int k = 0; k < 4; k++) {
         int l = k * 5;
         int i1 = (k + 1) * 5;

         for (int j1 = 0; j1 < 4; j1++) {
            int k1 = (l + j1) * 33;
            int l1 = (l + j1 + 1) * 33;
            int i2 = (i1 + j1) * 33;
            int j2 = (i1 + j1 + 1) * 33;

            for (int k2 = 0; k2 < 32; k2++) {
               double d0 = 0.125;
               double d1 = this.heightMap[k1 + k2];
               double d2 = this.heightMap[l1 + k2];
               double d3 = this.heightMap[i2 + k2];
               double d4 = this.heightMap[j2 + k2];
               double d5 = (this.heightMap[k1 + k2 + 1] - d1) * 0.125;
               double d6 = (this.heightMap[l1 + k2 + 1] - d2) * 0.125;
               double d7 = (this.heightMap[i2 + k2 + 1] - d3) * 0.125;
               double d8 = (this.heightMap[j2 + k2 + 1] - d4) * 0.125;

               for (int l2 = 0; l2 < 8; l2++) {
                  double d9 = 0.25;
                  double d10 = d1;
                  double d11 = d2;
                  double d12 = (d3 - d1) * 0.25;
                  double d13 = (d4 - d2) * 0.25;

                  for (int i3 = 0; i3 < 4; i3++) {
                     double d14 = 0.25;
                     double d16 = (d11 - d10) * 0.25;
                     double d15 = d10 - d16;

                     for (int j3 = 0; j3 < 4; j3++) {
                        if ((d15 += d16) > 0.0) {
                           chunkprimer.setBlockState(k * 4 + i3, k2 * 8 + l2, j1 * 4 + j3, LBlock.STONE);
                        } else if (k2 * 8 + l2 < this.settings.seaLevel) {
                           chunkprimer.setBlockState(k * 4 + i3, k2 * 8 + l2, j1 * 4 + j3, LBlock.WATER);
                        }
                     }

                     d10 += d12;
                     d11 += d13;
                  }

                  d1 += d5;
                  d2 += d6;
                  d3 += d7;
                  d4 += d8;
               }
            }
         }
      }
   }

   public void replaceBiomeBlocks(int i, int j, ChunkPrimer chunkprimer, Biome[] abiome) {
      double d0 = 0.03125;
      this.depthBuffer = this.surfaceNoise.getRegion(this.depthBuffer, (double)(i * 16), (double)(j * 16), 16, 16, 0.0625, 0.0625, 1.0);

      for (int k = 0; k < 16; k++) {
         for (int l = 0; l < 16; l++) {
            Biome biome = abiome[l + k * 16];
            biome.genTerrainBlocks(this, this.rand, chunkprimer, i * 16 + k, j * 16 + l, this.depthBuffer[l + k * 16]);
         }
      }
   }

   private void generateHeightmap(int i, int j, int k) {
      this.depthRegion = this.depthNoise
         .generateNoiseOctaves(
            this.depthRegion,
            i,
            k,
            5,
            5,
            (double)this.settings.depthNoiseScaleX,
            (double)this.settings.depthNoiseScaleZ,
            (double)this.settings.depthNoiseScaleExponent
         );
      float f = this.settings.coordinateScale;
      float f1 = this.settings.heightScale;
      this.mainNoiseRegion = this.mainPerlinNoise
         .generateNoiseOctaves(
            this.mainNoiseRegion,
            i,
            j,
            k,
            5,
            33,
            5,
            (double)(f / this.settings.mainNoiseScaleX),
            (double)(f1 / this.settings.mainNoiseScaleY),
            (double)(f / this.settings.mainNoiseScaleZ)
         );
      this.minLimitRegion = this.minLimitPerlinNoise.generateNoiseOctaves(this.minLimitRegion, i, j, k, 5, 33, 5, (double)f, (double)f1, (double)f);
      this.maxLimitRegion = this.maxLimitPerlinNoise.generateNoiseOctaves(this.maxLimitRegion, i, j, k, 5, 33, 5, (double)f, (double)f1, (double)f);
      int l = 0;
      int i1 = 0;

      for (int j1 = 0; j1 < 5; j1++) {
         for (int k1 = 0; k1 < 5; k1++) {
            float f2 = 0.0F;
            float f3 = 0.0F;
            float f4 = 0.0F;
            int l1 = 2;
            Biome biome = this.biomesForGeneration[j1 + 2 + (k1 + 2) * 10];

            for (int i2 = -2; i2 <= 2; i2++) {
               for (int j2 = -2; j2 <= 2; j2++) {
                  Biome biome1 = this.biomesForGeneration[j1 + i2 + 2 + (k1 + j2 + 2) * 10];
                  float f5 = this.settings.biomeDepthOffSet + biome1.getBaseHeight() * this.settings.biomeDepthWeight;
                  float f6 = this.settings.biomeScaleOffset + biome1.getHeightVariation() * this.settings.biomeScaleWeight;
                  if (false && f5 > 0.0F) {
                     f5 = 1.0F + f5 * 2.0F;
                     f6 = 1.0F + f6 * 4.0F;
                  }

                  float f7 = this.biomeWeights[i2 + 2 + (j2 + 2) * 5] / (f5 + 2.0F);
                  if (biome1.getBaseHeight() > biome.getBaseHeight()) {
                     f7 /= 2.0F;
                  }

                  f2 += f6 * f7;
                  f3 += f5 * f7;
                  f4 += f7;
               }
            }

            f2 /= f4;
            f3 /= f4;
            f2 = f2 * 0.9F + 0.1F;
            f3 = (f3 * 4.0F - 1.0F) / 8.0F;
            double d7 = this.depthRegion[i1] / 8000.0;
            if (d7 < 0.0) {
               d7 = -d7 * 0.3;
            }

            d7 = d7 * 3.0 - 2.0;
            if (d7 < 0.0) {
               d7 /= 2.0;
               if (d7 < -1.0) {
                  d7 = -1.0;
               }

               d7 /= 1.4;
               d7 /= 2.0;
            } else {
               if (d7 > 1.0) {
                  d7 = 1.0;
               }

               d7 /= 8.0;
            }

            i1++;
            double d8 = (double)f3;
            double d9 = (double)f2;
            d8 += d7 * 0.2;
            d8 = d8 * (double)this.settings.baseSize / 8.0;
            double d0 = (double)this.settings.baseSize + d8 * 4.0;

            for (int k2 = 0; k2 < 33; k2++) {
               double d1 = ((double)k2 - d0) * (double)this.settings.stretchY * 128.0 / 256.0 / d9;
               if (d1 < 0.0) {
                  d1 *= 4.0;
               }

               double d2 = this.minLimitRegion[l] / (double)this.settings.lowerLimitScale;
               double d3 = this.maxLimitRegion[l] / (double)this.settings.upperLimitScale;
               double d4 = (this.mainNoiseRegion[l] / 10.0 + 1.0) / 2.0;
               double d5 = MathHelper.clampedLerp(d2, d3, d4) - d1;
               if (k2 > 29) {
                  double d6 = (double)((float)(k2 - 29) / 3.0F);
                  d5 = d5 * (1.0 - d6) + -10.0 * d6;
               }

               this.heightMap[l] = d5;
               l++;
            }
         }
      }
   }
}
