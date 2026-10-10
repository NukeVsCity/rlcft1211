package com.nukevscity.rlgen.legacy.gen;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyWorld;
import com.nukevscity.rlgen.legacy.MathHelper;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import java.util.Random;

/** 1.12.2 MapGenCaves with Forge's patch. */
public class MapGenCaves extends MapGenBase {
   protected static final LBlock BLK_LAVA = LBlock.LAVA;
   protected static final LBlock BLK_AIR = LBlock.AIR;
   protected static final LBlock BLK_SANDSTONE = LBlock.SANDSTONE;
   protected static final LBlock BLK_RED_SANDSTONE = LBlock.RED_SANDSTONE;

   protected void addRoom(long i, int j, int k, ChunkPrimer chunkprimer, double d0, double d1, double d2) {
      this.addTunnel(i, j, k, chunkprimer, d0, d1, d2, 1.0F + this.rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5);
   }

   protected void addTunnel(
      long i, int j, int k, ChunkPrimer chunkprimer, double d0, double d1, double d2, float f, float f1, float f2, int l, int i1, double d3
   ) {
      double d4 = (double)(j * 16 + 8);
      double d5 = (double)(k * 16 + 8);
      float f3 = 0.0F;
      float f4 = 0.0F;
      Random random = new Random(i);
      if (i1 <= 0) {
         int j1 = this.range * 16 - 16;
         i1 = j1 - random.nextInt(j1 / 4);
      }

      boolean flag2 = false;
      if (l == -1) {
         l = i1 / 2;
         flag2 = true;
      }

      int k1 = random.nextInt(i1 / 2) + i1 / 4;

      for (boolean flag = random.nextInt(6) == 0; l < i1; l++) {
         double d6 = 1.5 + (double)(MathHelper.sin((float)l * (float) Math.PI / (float)i1) * f);
         double d7 = d6 * d3;
         float f5 = MathHelper.cos(f2);
         float f6 = MathHelper.sin(f2);
         d0 += (double)(MathHelper.cos(f1) * f5);
         d1 += (double)f6;
         d2 += (double)(MathHelper.sin(f1) * f5);
         if (flag) {
            f2 *= 0.92F;
         } else {
            f2 *= 0.7F;
         }

         f2 += f4 * 0.1F;
         f1 += f3 * 0.1F;
         f4 *= 0.9F;
         f3 *= 0.75F;
         f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
         f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
         if (!flag2 && l == k1 && f > 1.0F && i1 > 0) {
            this.addTunnel(
               random.nextLong(), j, k, chunkprimer, d0, d1, d2, random.nextFloat() * 0.5F + 0.5F, f1 - (float) (Math.PI / 2), f2 / 3.0F, l, i1, 1.0
            );
            this.addTunnel(
               random.nextLong(), j, k, chunkprimer, d0, d1, d2, random.nextFloat() * 0.5F + 0.5F, f1 + (float) (Math.PI / 2), f2 / 3.0F, l, i1, 1.0
            );
            return;
         }

         if (flag2 || random.nextInt(4) != 0) {
            double d8 = d0 - d4;
            double d9 = d2 - d5;
            double d10 = (double)(i1 - l);
            double d11 = (double)(f + 2.0F + 16.0F);
            if (d8 * d8 + d9 * d9 - d10 * d10 > d11 * d11) {
               return;
            }

            if (!(d0 < d4 - 16.0 - d6 * 2.0) && !(d2 < d5 - 16.0 - d6 * 2.0) && !(d0 > d4 + 16.0 + d6 * 2.0) && !(d2 > d5 + 16.0 + d6 * 2.0)) {
               int l3 = MathHelper.floor(d0 - d6) - j * 16 - 1;
               int l1 = MathHelper.floor(d0 + d6) - j * 16 + 1;
               int i4 = MathHelper.floor(d1 - d7) - 1;
               int i2 = MathHelper.floor(d1 + d7) + 1;
               int j4 = MathHelper.floor(d2 - d6) - k * 16 - 1;
               int j2 = MathHelper.floor(d2 + d6) - k * 16 + 1;
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

               boolean flag3 = false;

               for (int k2 = l3; !flag3 && k2 < l1; k2++) {
                  for (int l2 = j4; !flag3 && l2 < j2; l2++) {
                     for (int i3 = i2 + 1; !flag3 && i3 >= i4 - 1; i3--) {
                        if (i3 >= 0 && i3 < 256) {
                           if (this.isOceanBlock(chunkprimer, k2, i3, l2, j, k)) {
                              flag3 = true;
                           }

                           if (i3 != i4 - 1 && k2 != l3 && k2 != l1 - 1 && l2 != j4 && l2 != j2 - 1) {
                              i3 = i4;
                           }
                        }
                     }
                  }
               }

               if (!flag3) {
                  for (int k4 = l3; k4 < l1; k4++) {
                     double d14 = ((double)(k4 + j * 16) + 0.5 - d0) / d6;

                     for (int j3 = j4; j3 < j2; j3++) {
                        double d12 = ((double)(j3 + k * 16) + 0.5 - d2) / d6;
                        boolean flag1 = false;
                        if (d14 * d14 + d12 * d12 < 1.0) {
                           for (int k3 = i2; k3 > i4; k3--) {
                              double d13 = ((double)(k3 - 1) + 0.5 - d1) / d7;
                              if (d13 > -0.7 && d14 * d14 + d13 * d13 + d12 * d12 < 1.0) {
                                 LBlock iblockstate1 = chunkprimer.getBlockState(k4, k3, j3);
                                 LBlock iblockstate2 = chunkprimer.getBlockState(k4, k3 + 1, j3);
                                 if (this.isTopBlock(chunkprimer, k4, k3, j3, j, k)) {
                                    flag1 = true;
                                 }

                                 this.digBlock(chunkprimer, k4, k3, j3, j, k, flag1, iblockstate1, iblockstate2);
                              }
                           }
                        }
                     }
                  }

                  if (flag2) {
                     break;
                  }
               }
            }
         }
      }
   }

   protected boolean canReplaceBlock(LBlock iblockstate, LBlock iblockstate1) {
      if (iblockstate.getBlock() == LBlock.STONE) {
         return true;
      } else if (iblockstate.getBlock() == LBlock.DIRT) {
         return true;
      } else if (iblockstate.getBlock() == LBlock.GRASS) {
         return true;
      } else if (iblockstate.getBlock() == LBlock.HARDENED_CLAY) {
         return true;
      } else if (iblockstate.getBlock() == LBlock.STAINED_HARDENED_CLAY) {
         return true;
      } else if (iblockstate.getBlock() == LBlock.SANDSTONE) {
         return true;
      } else if (iblockstate.getBlock() == LBlock.RED_SANDSTONE) {
         return true;
      } else if (iblockstate.getBlock() == LBlock.MYCELIUM) {
         return true;
      } else {
         return iblockstate.getBlock() == LBlock.SNOW_LAYER
            ? true
            : (iblockstate.getBlock() == LBlock.SAND || iblockstate.getBlock() == LBlock.GRAVEL) && iblockstate1.isWaterMaterial() == false;
      }
   }

   @Override
   protected void recursiveGenerate(LegacyWorld world, int i, int j, int k, int l, ChunkPrimer chunkprimer) {
      int i1 = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(15) + 1) + 1);
      if (this.rand.nextInt(7) != 0) {
         i1 = 0;
      }

      for (int j1 = 0; j1 < i1; j1++) {
         double d0 = (double)(i * 16 + this.rand.nextInt(16));
         double d1 = (double)this.rand.nextInt(this.rand.nextInt(120) + 8);
         double d2 = (double)(j * 16 + this.rand.nextInt(16));
         int k1 = 1;
         if (this.rand.nextInt(4) == 0) {
            this.addRoom(this.rand.nextLong(), k, l, chunkprimer, d0, d1, d2);
            k1 += this.rand.nextInt(4);
         }

         for (int l1 = 0; l1 < k1; l1++) {
            float f = this.rand.nextFloat() * (float) (Math.PI * 2);
            float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
            float f2 = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();
            if (this.rand.nextInt(10) == 0) {
               f2 *= this.rand.nextFloat() * this.rand.nextFloat() * 3.0F + 1.0F;
            }

            this.addTunnel(this.rand.nextLong(), k, l, chunkprimer, d0, d1, d2, f2, f, f1, 0, 0, 1.0);
         }
      }
   }

   // ---- Forge 1.12.2 patch (MapGenCaves.java.patch) ----

   protected boolean isOceanBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
      LBlock block = data.getBlockState(x, y, z).getBlock();
      return block == LBlock.FLOWING_WATER || block == LBlock.WATER;
   }

   private boolean isExceptionBiome(Biome biome) {
      return biome == Biomes.BEACH || biome == Biomes.DESERT;
   }

   /** Forge compares a Block with the biome's top IBlockState here, which is never equal, so only the exception biomes ever match. */
   private boolean isTopBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
      Biome biome = this.world.getBiome(x + chunkX * 16, z + chunkZ * 16);
      LBlock state = data.getBlockState(x, y, z);
      return isExceptionBiome(biome) && state.getBlock() == LBlock.GRASS;
   }

   protected void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop, LBlock state, LBlock up) {
      Biome biome = this.world.getBiome(x + chunkX * 16, z + chunkZ * 16);
      LBlock top = biome.topBlock;
      LBlock filler = biome.fillerBlock;
      if (this.canReplaceBlock(state, up) || state.getBlock() == top.getBlock() || state.getBlock() == filler.getBlock()) {
         if (y - 1 < 10) {
            data.setBlockState(x, y, z, BLK_LAVA);
         } else {
            data.setBlockState(x, y, z, BLK_AIR);
            if (foundTop && data.getBlockState(x, y - 1, z).getBlock() == filler.getBlock()) {
               data.setBlockState(x, y - 1, z, top.getBlock());
            }
         }
      }
   }
}
