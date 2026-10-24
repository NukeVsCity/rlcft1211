package com.nukevscity.rlgen.ntp;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.deco.DWorld;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * No Tree Punching 2.0.21 WorldGenRocks (IWorldGenerator weight 3, overworld only) with RLCraft's looseRocksFrequency=8: tries at
 * random columns of the populate area, half of them place a loose rock on dirt / grass / sand / gravel / clay, its stone taken from 6
 * blocks below (Quark compat on: marble and limestone too).
 */
public final class LooseRocks {
   private static final int FREQUENCY = 8;

   private LooseRocks() {
   }

   public static void generate(DWorld w, ChunkPos cp) {
      long seed = w.seed();
      Random fml = new Random(seed);
      long xSeed = fml.nextLong() >> 2 + 1L;
      long zSeed = fml.nextLong() >> 2 + 1L;
      Random random = new Random(xSeed * (long)cp.x + zSeed * (long)cp.z ^ seed);
      for (int i = 0; i < FREQUENCY; i++) {
         int x = cp.x * 16 + random.nextInt(16) + 8;
         int z = cp.z * 16 + random.nextInt(16) + 8;
         if ((double)random.nextFloat() < 0.5) {
            placeRock(w, topSolidBlock(w, x, z));
         }
      }
   }

   /** CoreHelpers.getTopSolidBlock: down from above the highest section to a block that blocks movement and is not leaves, foliage or liquid. */
   private static BlockPos topSolidBlock(DWorld w, int x, int z) {
      BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos(x, Math.min(256, w.height(x, z) + 16), z);
      while (m.getY() > 0) {
         m.move(0, -1, 0);
         BlockState s = w.get(m);
         if (s.blocksMotion() && !s.is(BlockTags.LEAVES) && !DWorld.isFoliage(s) && s.getFluidState().isEmpty()) {
            break;
         }
      }
      return m.immutable();
   }

   private static void placeRock(DWorld w, BlockPos pos) {
      BlockState state = w.get(pos.above());
      if (state.getFluidState().isEmpty() && !state.isSolidRender(w.level, pos.above()) && (state.isAir() || state.canBeReplaced())) {
         BlockState down = w.get(pos);
         if (down.isSolidRender(w.level, pos) && isGroundMaterial(down)) {
            w.set(pos.above(), rockFor(w.get(pos.below(6))).defaultBlockState());
         }
      }
   }

   /** Material GROUND, SAND, GRASS, CLAY, GOURD (snow layers are never opaque). */
   private static boolean isGroundMaterial(BlockState s) {
      return s.is(BlockTags.DIRT) || s.is(BlockTags.SAND) || s.is(Blocks.GRAVEL) || s.is(Blocks.CLAY) || s.is(Blocks.PUMPKIN) || s.is(Blocks.MELON)
         || s.is(Blocks.FARMLAND) || s.is(Blocks.DIRT_PATH) || s.is(RLGen.GRASS_DEFILED.get()) || s.is(RLGen.DIRT_DEFILED.get())
         || s.is(RLGen.SAND_DEFILED.get()) || s.is(RLGen.GRAVEL_DEFILED.get());
   }

   private static Block rockFor(BlockState s) {
      String stone = "stone";
      if (s.is(Blocks.GRANITE)) {
         stone = "granite";
      } else if (s.is(Blocks.DIORITE)) {
         stone = "diorite";
      } else if (s.is(Blocks.ANDESITE)) {
         stone = "andesite";
      } else if (s.is(Blocks.SANDSTONE) || s.is(Blocks.CHISELED_SANDSTONE) || s.is(Blocks.CUT_SANDSTONE)) {
         stone = "sandstone";
      } else if (s.is(Blocks.RED_SANDSTONE) || s.is(Blocks.CHISELED_RED_SANDSTONE) || s.is(Blocks.CUT_RED_SANDSTONE)) {
         stone = "red_sandstone";
      } else if (s.is(RLGen.MARBLE.get())) {
         stone = "marble";
      } else if (s.is(RLGen.LIMESTONE.get())) {
         stone = "limestone";
      }
      return RLGen.LOOSE_ROCKS.get(stone).get();
   }
}
