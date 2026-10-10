package com.nukevscity.rlgen.legacy.gen;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyWorld;
import com.nukevscity.rlgen.legacy.MathHelper;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import java.util.Random;


/** 1.12.2 MapGenRavine with Forge's patch. */
public class MapGenRavine extends MapGenBase {
   protected static final LBlock FLOWING_LAVA = LBlock.FLOWING_LAVA;
   protected static final LBlock AIR = LBlock.AIR;
   private final float[] rs = new float[1024];

   protected void addTunnel(
      long i, int j, int k, ChunkPrimer chunkprimer, double d0, double d1, double d2, float f, float f1, float f2, int l, int i1, double d3
   ) {
      Random random = new Random(i);
      double d4 = (double)(j * 16 + 8);
      double d5 = (double)(k * 16 + 8);
      float f3 = 0.0F;
      float f4 = 0.0F;
      if (i1 <= 0) {
         int j1 = this.range * 16 - 16;
         i1 = j1 - random.nextInt(j1 / 4);
      }

      boolean flag1 = false;
      if (l == -1) {
         l = i1 / 2;
         flag1 = true;
      }

      float f5 = 1.0F;

      for (int k1 = 0; k1 < 256; k1++) {
         if (k1 == 0 || random.nextInt(3) == 0) {
            f5 = 1.0F + random.nextFloat() * random.nextFloat();
         }

         this.rs[k1] = f5 * f5;
      }

      for (; l < i1; l++) {
         double d13 = 1.5 + (double)(MathHelper.sin((float)l * (float) Math.PI / (float)i1) * f);
         double d6 = d13 * d3;
         d13 *= (double)random.nextFloat() * 0.25 + 0.75;
         d6 *= (double)random.nextFloat() * 0.25 + 0.75;
         float f6 = MathHelper.cos(f2);
         float f7 = MathHelper.sin(f2);
         d0 += (double)(MathHelper.cos(f1) * f6);
         d1 += (double)f7;
         d2 += (double)(MathHelper.sin(f1) * f6);
         f2 *= 0.7F;
         f2 += f4 * 0.05F;
         f1 += f3 * 0.05F;
         f4 *= 0.8F;
         f3 *= 0.5F;
         f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
         f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
         if (flag1 || random.nextInt(4) != 0) {
            double d7 = d0 - d4;
            double d8 = d2 - d5;
            double d9 = (double)(i1 - l);
            double d10 = (double)(f + 2.0F + 16.0F);
            if (d7 * d7 + d8 * d8 - d9 * d9 > d10 * d10) {
               return;
            }

            if (!(d0 < d4 - 16.0 - d13 * 2.0) && !(d2 < d5 - 16.0 - d13 * 2.0) && !(d0 > d4 + 16.0 + d13 * 2.0) && !(d2 > d5 + 16.0 + d13 * 2.0)) {
               int l3 = MathHelper.floor(d0 - d13) - j * 16 - 1;
               int l1 = MathHelper.floor(d0 + d13) - j * 16 + 1;
               int i4 = MathHelper.floor(d1 - d6) - 1;
               int i2 = MathHelper.floor(d1 + d6) + 1;
               int j4 = MathHelper.floor(d2 - d13) - k * 16 - 1;
               int j2 = MathHelper.floor(d2 + d13) - k * 16 + 1;
               if (l3 < 0) {
                  l3 = 0;
               }

               if (l1 > 16) {
                  l1 = 16;
               }

               if (i4 < 1) {
                  i4 = 1;
               }

               if (i2 > 248) {
                  i2 = 248;
               }

               if (j4 < 0) {
                  j4 = 0;
               }

               if (j2 > 16) {
                  j2 = 16;
               }

               boolean flag2 = false;

               for (int k2 = l3; !flag2 && k2 < l1; k2++) {
                  for (int l2 = j4; !flag2 && l2 < j2; l2++) {
                     for (int i3 = i2 + 1; !flag2 && i3 >= i4 - 1; i3--) {
                        if (i3 >= 0 && i3 < 256) {
                           if (this.isOceanBlock(chunkprimer, k2, i3, l2, j, k)) {
                              flag2 = true;
                           }

                           if (i3 != i4 - 1 && k2 != l3 && k2 != l1 - 1 && l2 != j4 && l2 != j2 - 1) {
                              i3 = i4;
                           }
                        }
                     }
                  }
               }

               if (!flag2) {
                  for (int k4 = l3; k4 < l1; k4++) {
                     double d14 = ((double)(k4 + j * 16) + 0.5 - d0) / d13;

                     for (int j3 = j4; j3 < j2; j3++) {
                        double d11 = ((double)(j3 + k * 16) + 0.5 - d2) / d13;
                        boolean flag = false;
                        if (d14 * d14 + d11 * d11 < 1.0) {
                           for (int k3 = i2; k3 > i4; k3--) {
                              double d12 = ((double)(k3 - 1) + 0.5 - d1) / d6;
                              if ((d14 * d14 + d11 * d11) * (double)this.rs[k3 - 1] + d12 * d12 / 6.0 < 1.0) {
                                 if (this.isTopBlock(chunkprimer, k4, k3, j3, j, k)) {
                                    flag = true;
                                 }

                                 this.digBlock(chunkprimer, k4, k3, j3, j, k, flag);
                              }
                           }
                        }
                     }
                  }

                  if (flag1) {
                     break;
                  }
               }
            }
         }
      }
   }

   @Override
   protected void recursiveGenerate(LegacyWorld world, int i, int j, int k, int l, ChunkPrimer chunkprimer) {
      if (this.rand.nextInt(50) == 0) {
         double d0 = (double)(i * 16 + this.rand.nextInt(16));
         double d1 = (double)(this.rand.nextInt(this.rand.nextInt(40) + 8) + 20);
         double d2 = (double)(j * 16 + this.rand.nextInt(16));
         int i1 = 1;

         for (int j1 = 0; j1 < 1; j1++) {
            float f = this.rand.nextFloat() * (float) (Math.PI * 2);
            float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
            float f2 = (this.rand.nextFloat() * 2.0F + this.rand.nextFloat()) * 2.0F;
            this.addTunnel(this.rand.nextLong(), k, l, chunkprimer, d0, d1, d2, f2, f, f1, 0, 0, 3.0);
         }
      }
   }

   // ---- Forge 1.12.2 patch (MapGenRavine.java.patch) ----

   protected boolean isOceanBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
      LBlock block = data.getBlockState(x, y, z).getBlock();
      return block == LBlock.FLOWING_WATER || block == LBlock.WATER;
   }

   private boolean isExceptionBiome(Biome biome) {
      return biome == Biomes.BEACH || biome == Biomes.DESERT || biome == Biomes.MUSHROOM_ISLAND || biome == Biomes.MUSHROOM_ISLAND_SHORE;
   }

   /** Same Forge Block-vs-IBlockState comparison as in caves: only the exception biomes can match. */
   private boolean isTopBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
      Biome biome = this.world.getBiome(x + chunkX * 16, z + chunkZ * 16);
      LBlock state = data.getBlockState(x, y, z);
      return isExceptionBiome(biome) && state.getBlock() == LBlock.GRASS;
   }

   protected void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop) {
      Biome biome = this.world.getBiome(x + chunkX * 16, z + chunkZ * 16);
      LBlock state = data.getBlockState(x, y, z);
      LBlock top = isExceptionBiome(biome) ? LBlock.GRASS : biome.topBlock;
      LBlock filler = isExceptionBiome(biome) ? LBlock.DIRT : biome.fillerBlock;
      if (state.getBlock() == LBlock.STONE || state.getBlock() == top.getBlock() || state.getBlock() == filler.getBlock()) {
         if (y - 1 < 10) {
            data.setBlockState(x, y, z, FLOWING_LAVA);
         } else {
            data.setBlockState(x, y, z, AIR);
            if (foundTop && data.getBlockState(x, y - 1, z).getBlock() == filler.getBlock()) {
               data.setBlockState(x, y - 1, z, top.getBlock());
            }
         }
      }
   }
}
