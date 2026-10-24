package com.nukevscity.rlgen.coral;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.noise.NoiseGeneratorOctaves;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Coral Reef 2.5 GeneratorReef (IWorldGenerator weight 1) with RLCraft's coralreef.cfg: reef sparsity 0.9, coral sparsity 0.9, rock
 * chance 0.5, biomes "ocean", "deep ocean", "beach". Biomes are matched against the registry path, so only "ocean" ever matches
 * (deep_ocean and beaches are spelled differently): kept.
 */
public final class CoralReefs {
   private static final NoiseGeneratorOctaves CORAL_REEF_NOISE = new NoiseGeneratorOctaves(new Random(3364L), 2);
   private static final double REEF_SPARSITY = 0.9;
   private static final double CORAL_SPARSITY = 0.9;
   private static final double ROCK_CHANCE = 0.5;

   private CoralReefs() {
   }

   public static void generate(DWorld w, ChunkPos cp) {
      long seed = w.seed();
      Random fml = new Random(seed);
      long xSeed = fml.nextLong() >> 2 + 1L;
      long zSeed = fml.nextLong() >> 2 + 1L;
      Random random = new Random(xSeed * (long)cp.x + zSeed * (long)cp.z ^ seed);
      reef(w, random, cp.x, cp.z);
      if (random.nextDouble() <= ROCK_CHANCE) {
         int x = cp.x * 16 + 8 + random.nextInt(16);
         int z = cp.z * 16 + 8 + random.nextInt(16);
         rock(w, random, getTop(w, x, z).above());
      }
   }

   private static void reef(DWorld w, Random rand, int chunkX, int chunkZ) {
      double[] noise = CORAL_REEF_NOISE.generateNoiseOctaves(new double[256], chunkX * 16, 0, chunkZ * 16, 16, 1, 16, 0.03125, 1.0, 0.03125);
      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            BlockPos pos = getTop(w, chunkX * 16 + x + 8, chunkZ * 16 + z + 8);
            if (reefFromNoise(noise, x, pos.getY(), z, rand) && "ocean".equals(path(w.biome(pos).getRegistryName()))) {
               genReef(w, rand, pos);
            }
         }
      }
   }

   private static String path(String name) {
      return name.substring(name.indexOf(':') + 1).toLowerCase();
   }

   private static boolean reefFromNoise(double[] noise, int x, int y, int z, Random rand) {
      double d = noise[x * 16 + z] / 2.0;
      int seaLevel = 64;
      double ybelow = (double)Math.max(0, seaLevel - y);
      d *= 1.81E-4 * ybelow * ybelow - 0.1705 * ybelow + 4.0;
      d -= rand.nextDouble() * 0.1;
      d = Math.pow(d, 5.0);
      return d > 0.005;
   }

   /** GeneratorReef.getTop: from the top solid-or-liquid block down to a block that blocks movement and is not ice, leaves or foliage. */
   private static BlockPos getTop(DWorld w, int x, int z) {
      BlockPos.MutableBlockPos p = w.topSolidOrLiquid(new BlockPos(x, 0, z)).mutable();
      while (p.getY() >= 0) {
         BlockState s = w.get(p);
         if (s.blocksMotion() && !s.is(BlockTags.ICE) && !s.is(BlockTags.LEAVES) && !DWorld.isFoliage(s)) {
            break;
         }
         p.setY(p.getY() - 1);
      }
      return p.immutable();
   }

   private static boolean isWater(DWorld w, BlockPos p) {
      return CoralBlock.isWaterMaterial(w.get(p));
   }

   /** WorldGenReef */
   private static void genReef(DWorld w, Random rand, BlockPos pos) {
      if (isWater(w, pos.above(1)) && isWater(w, pos.above(2)) && !isWater(w, pos) && rand.nextDouble() <= REEF_SPARSITY) {
         w.set(pos, RLGen.REEF.get().defaultBlockState());
         if (rand.nextDouble() <= CORAL_SPARSITY) {
            placeCoral(w, rand, pos.above());
         }
      }
   }

   /** BlockCoral.placeAt (world.rand in 1.12; the chunk random here). */
   private static void placeCoral(DWorld w, Random rand, BlockPos bottom) {
      if (canPlaceCoral(w, bottom)) {
         int variant = rand.nextInt(6);
         if (variant > 3) {
            int height = rand.nextInt(4);
            for (int i = 0; i < height; i++) {
               BlockPos bp = bottom.above(i);
               if (!isWater(w, bp.above())) {
                  break;
               }
               w.set(bp, RLGen.CORALS.get(variant).get().defaultBlockState());
            }
         } else {
            w.set(bottom, RLGen.CORALS.get(variant).get().defaultBlockState());
         }
      }
   }

   private static boolean canPlaceCoral(DWorld w, BlockPos pos) {
      if (!isWater(w, pos.above())) {
         return false;
      }
      BlockState below = w.get(pos.below());
      return below.getBlock() instanceof ReefBlock || below.getBlock() instanceof CoralBlock c && c.high;
   }

   /** WorldGenRock: a small reef-rock boulder on a reef block. */
   private static void rock(DWorld w, Random rand, BlockPos position) {
      while (position.getY() > 3) {
         if (!w.isAir(position.below()) && !w.isAir(position)) {
            if (w.get(position.below()).getBlock() instanceof ReefBlock) {
               int b = rand.nextInt(2);
               for (int n = 0; n < 3; n++) {
                  int i = b + rand.nextInt(2);
                  int j = b + rand.nextInt(2);
                  int k = b + rand.nextInt(2);
                  double f = (double)(i + j + k) / 3.0 + 0.5;
                  if (isWater(w, position.above(j + 1))) {
                     for (BlockPos bp : BlockPos.betweenClosed(position.offset(-i, -j, -k), position.offset(i, j, k))) {
                        if (bp.distSqr(position) <= f * f) {
                           w.set(bp.immutable(), RLGen.REEF_ROCK.get().defaultBlockState());
                        }
                     }
                     int xzstep = rand.nextInt(2 + b * 2) - b - 1;
                     position = position.offset(xzstep, -rand.nextInt(2), xzstep);
                  }
               }
               return;
            }
         }
         position = position.below();
      }
   }
}
