package com.nukevscity.rlgen.quark;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.rc.BiomeDict;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Quark r1.6 FairyRingGenerator (an IWorldGenerator, weight 5) with RLCraft's chances: forest 0.625%, plains 0.25% per chunk. A ring of
 * oxeye daisies on grass and a small pocket of emerald / diamond ore 25-35 blocks below (the Ice and Fire silver and Scaling Health
 * crystal choices place nothing until those mods are ported).
 */
public final class FairyRings {
   private FairyRings() {
   }

   private static boolean isGrass(BlockState s) {
      return s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.MYCELIUM) || s.is(RLGen.GRASS_DEFILED.get());
   }

   private static boolean isNaturalStone(BlockState s) {
      return s.is(Blocks.STONE) || s.is(Blocks.GRANITE) || s.is(Blocks.DIORITE) || s.is(Blocks.ANDESITE);
   }

   /** After the populate pass, with the chunk random Forge gave IWorldGenerators. */
   public static void generate(DWorld w, ChunkPos cp) {
      long seed = w.seed();
      Random fml = new Random(seed);
      long xSeed = fml.nextLong() >> 2 + 1L;
      long zSeed = fml.nextLong() >> 2 + 1L;
      Random random = new Random(xSeed * (long)cp.x + zSeed * (long)cp.z ^ seed);
      int x = cp.x * 16 + 8 + random.nextInt(16);
      int z = cp.z * 16 + 8 + random.nextInt(16);
      Set<String> types = BiomeDict.TYPES.getOrDefault(w.biome(new BlockPos(x, 0, z)).getRegistryName(), Set.of());
      double chance = 0.0;
      if (types.contains("FOREST")) {
         chance = 0.00625;
      } else if (types.contains("PLAINS")) {
         chance = 0.0025;
      }
      if (random.nextDouble() < chance) {
         BlockPos pos = new BlockPos(x, 128, z);
         BlockState state = w.get(pos);
         while (!isGrass(state) && pos.getY() > 30) {
            pos = pos.below();
            state = w.get(pos);
         }
         if (isGrass(state)) {
            spawnFairyRing(w, random, pos.below());
         }
      }
   }

   private static void spawnFairyRing(DWorld w, Random rand, BlockPos pos) {
      BlockState flower = Blocks.OXEYE_DAISY.defaultBlockState();
      for (int i = -3; i <= 3; i++) {
         for (int j = -3; j <= 3; j++) {
            float dist = (float)(i * i + j * j);
            if (!(dist < 7.0F) && !(dist > 10.0F)) {
               for (int k = 5; k > -4; k--) {
                  BlockPos fpos = pos.offset(i, k, j);
                  BlockPos fposUp = fpos.above();
                  BlockState state = w.get(fpos);
                  BlockState stateUp = w.get(fposUp);
                  if (isGrass(state) && (stateUp.isAir() || stateUp.canBeReplaced())) {
                     w.set(fposUp, flower);
                     break;
                  }
               }
            }
         }
      }
      // world.rand in 1.12; the chunk random goes on here
      BlockPos orePos = pos.below(rand.nextInt(10) + 25);
      BlockState stoneState = w.get(orePos);
      for (int down = 0; !isNaturalStone(stoneState) && down < 10; down++) {
         orePos = orePos.below();
         stoneState = w.get(orePos);
      }
      if (isNaturalStone(stoneState)) {
         int which = rand.nextInt(4);
         BlockState ore = which == 0 ? Blocks.EMERALD_ORE.defaultBlockState() : which == 1 ? Blocks.DIAMOND_ORE.defaultBlockState() : null;
         if (ore != null) {
            w.set(orePos, ore);
         }
         for (Direction face : Direction.values()) {
            if (rand.nextBoolean() && ore != null) {
               w.set(orePos.relative(face), ore);
            }
         }
      }
   }
}
