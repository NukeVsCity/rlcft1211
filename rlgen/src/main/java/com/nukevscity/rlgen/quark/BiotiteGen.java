package com.nukevscity.rlgen.quark;

import com.nukevscity.rlgen.RLGen;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Quark's Biotite (vazkii.quark.world.feature.Biotite) with RLCraft's quark.cfg: "Generate naturally"=false, so BiotiteGenerator never
 * runs in End chunks; the ore comes from "Generate by dragon kill"=true: every death tick of the dragon ("Generation delay on dragon death"=1)
 * makes "Clusters generated per dragon death tick"=16 clusters of "Cluster size"=10 in end stone, within 128 blocks of the dragon at y 0..63.
 */
public final class BiotiteGen {
   static final boolean GENERATE_BY_DRAGON = true;
   static final int CLUSTER_SIZE = 10;
   static final int GENERATION_DELAY = 1;
   static final int CLUSTERS_PER_TICK = 16;

   private BiotiteGen() {
   }

   /** Biotite.onEntityTick (LivingUpdateEvent, which fired before the tick that counts deathTicks up). */
   public static void onEntityTick(EntityTickEvent.Pre event) {
      if (GENERATE_BY_DRAGON && event.getEntity() instanceof EnderDragon dragon && !dragon.level().isClientSide) {
         if (dragon.dragonDeathTime > 0 && dragon.dragonDeathTime % GENERATION_DELAY == 0) {
            Level level = dragon.level();
            RandomSource rand = level.random;
            BlockPos basePos = dragon.blockPosition();
            basePos = new BlockPos(basePos.getX() - 128, 0, basePos.getZ() - 128);
            for (int i = 0; i < CLUSTERS_PER_TICK; i++) {
               BlockPos pos = basePos.offset(rand.nextInt(256), rand.nextInt(64), rand.nextInt(256));
               minable(level, rand, pos, RLGen.BIOTITE_ORE.get().defaultBlockState(), CLUSTER_SIZE);
            }
         }
      }
   }

   /** WorldGenMinable(biotite_ore, clusterSize, BlockMatcher.forBlock(END_STONE)).generate. */
   static void minable(Level w, RandomSource random, BlockPos pos, BlockState ore, int size) {
      float f = random.nextFloat() * (float)Math.PI;
      double d0 = (float)(pos.getX() + 8) + Mth.sin(f) * (float)size / 8.0F;
      double d1 = (float)(pos.getX() + 8) - Mth.sin(f) * (float)size / 8.0F;
      double d2 = (float)(pos.getZ() + 8) + Mth.cos(f) * (float)size / 8.0F;
      double d3 = (float)(pos.getZ() + 8) - Mth.cos(f) * (float)size / 8.0F;
      double d4 = pos.getY() + random.nextInt(3) - 2;
      double d5 = pos.getY() + random.nextInt(3) - 2;
      for (int i = 0; i < size; i++) {
         float f1 = (float)i / (float)size;
         double d6 = d0 + (d1 - d0) * f1;
         double d7 = d4 + (d5 - d4) * f1;
         double d8 = d2 + (d3 - d2) * f1;
         double d9 = random.nextDouble() * size / 16.0;
         double d10 = (Mth.sin((float)Math.PI * f1) + 1.0F) * d9 + 1.0;
         double d11 = (Mth.sin((float)Math.PI * f1) + 1.0F) * d9 + 1.0;
         int j = Mth.floor(d6 - d10 / 2.0);
         int k = Mth.floor(d7 - d11 / 2.0);
         int l = Mth.floor(d8 - d10 / 2.0);
         int i1 = Mth.floor(d6 + d10 / 2.0);
         int j1 = Mth.floor(d7 + d11 / 2.0);
         int k1 = Mth.floor(d8 + d10 / 2.0);
         for (int l1 = j; l1 <= i1; l1++) {
            double d12 = (l1 + 0.5 - d6) / (d10 / 2.0);
            if (d12 * d12 < 1.0) {
               for (int i2 = k; i2 <= j1; i2++) {
                  double d13 = (i2 + 0.5 - d7) / (d11 / 2.0);
                  if (d12 * d12 + d13 * d13 < 1.0) {
                     for (int j2 = l; j2 <= k1; j2++) {
                        double d14 = (j2 + 0.5 - d8) / (d10 / 2.0);
                        if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0) {
                           BlockPos b = new BlockPos(l1, i2, j2);
                           if (b.getY() >= 0 && b.getY() < 256 && w.getBlockState(b).is(Blocks.END_STONE)) {
                              w.setBlock(b, ore, 2);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
