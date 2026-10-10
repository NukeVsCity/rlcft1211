package com.nukevscity.rlgen.quark;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.rc.BiomeDict;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Quark r1.6 "Revamp stone gen" with RLCraft's quark.cfg: big round clusters of granite, diorite, andesite, marble, limestone, jasper and
 * slate replacing stone, picked by biome type at the cluster centre (StoneInfoBasedGenerator over MultiChunkFeatureGenerator).
 * Quark ran it from the DIRT OreGenEvent, i.e. just before the dirt blobs of BiomeDecorator.generateOres.
 */
public final class RevampStoneGen {
   private record StoneInfo(String name, Supplier<BlockState> state, int clusterSize, int clusterRarity, int upperBound, int lowerBound,
                            Set<String> allowedBiomes, long seedXor) {
      StoneInfo(String name, Supplier<BlockState> state, int clusterSize, int clusterRarity, int upperBound, int lowerBound, Set<String> allowedBiomes) {
         this(name, state, clusterSize, clusterRarity, upperBound, lowerBound, allowedBiomes, (long)name.hashCode());
      }
   }

   /** In the order Quark registered them (vanilla stones first, then its own). */
   private static final List<StoneInfo> STONES = List.of(
      new StoneInfo("granite", Blocks.GRANITE::defaultBlockState, 10, 5, 128, 20, Set.of("MOUNTAIN", "HILLS")),
      new StoneInfo("diorite", Blocks.DIORITE::defaultBlockState, 10, 5, 128, 20, Set.of("SAVANNA", "JUNGLE", "MUSHROOM")),
      new StoneInfo("andesite", Blocks.ANDESITE::defaultBlockState, 10, 5, 128, 20, Set.of("FOREST")),
      new StoneInfo("marble", () -> RLGen.MARBLE.get().defaultBlockState(), 14, 16, 80, 20, Set.of("PLAINS")),
      new StoneInfo("limestone", () -> RLGen.LIMESTONE.get().defaultBlockState(), 14, 16, 80, 20, Set.of("SWAMP", "OCEAN")),
      new StoneInfo("jasper", () -> RLGen.JASPER.get().defaultBlockState(), 14, 16, 80, 20, Set.of("MESA", "SANDY")),
      new StoneInfo("slate", () -> RLGen.SLATE.get().defaultBlockState(), 14, 16, 80, 20, Set.of("COLD")));

   private RevampStoneGen() {
   }

   public static void generate(DWorld w, ChunkPos cp) {
      for (StoneInfo info : STONES) {
         generate(w, info, cp.x, cp.z);
      }
   }

   /** MultiChunkFeatureGenerator.generate */
   private static void generate(DWorld w, StoneInfo info, int chunkX, int chunkZ) {
      int radius = info.clusterSize;
      int chunkRadius = (int)Math.ceil((double)radius / 16.0);
      long worldSeed = w.seed() ^ info.seedXor;
      Random worldRandom = new Random(worldSeed);
      long xSeed = worldRandom.nextLong() >> 3;
      long zSeed = worldRandom.nextLong() >> 3;
      BlockState state = null;
      for (int x = chunkX - chunkRadius; x <= chunkX + chunkRadius; x++) {
         for (int z = chunkZ - chunkRadius; z <= chunkZ + chunkRadius; z++) {
            Random chunkRandom = new Random(xSeed * (long)x + zSeed * (long)z ^ worldSeed);
            BlockPos source = source(info, chunkRandom, x, z);
            if (source != null && valid(w, info, source)) {
               if (state == null) {
                  state = info.state.get();
               }
               generateChunkPart(w, info, state, source, chunkX, chunkZ);
            }
         }
      }
   }

   /** StoneInfoBasedGenerator.getSourcesInChunk (one source: "Invert Cluster Rarity" is off for all of them). */
   private static BlockPos source(StoneInfo info, Random random, int chunkX, int chunkZ) {
      int chance = info.clusterRarity;
      if (chance > 0 && random.nextInt(chance) == 0) {
         int lower = Math.abs(info.lowerBound);
         int range = Math.abs(info.upperBound - info.lowerBound);
         if (range > 0) {
            int x = chunkX * 16 + random.nextInt(16) + 8;
            int y = random.nextInt(range) + lower;
            int z = chunkZ * 16 + random.nextInt(16) + 8;
            return new BlockPos(x, y, z);
         }
      }
      return null;
   }

   /** BiomeTypeConfigHandler.biomeTypeIntersectCheck on the biome at the centre. */
   private static boolean valid(DWorld w, StoneInfo info, BlockPos source) {
      Set<String> types = BiomeDict.TYPES.getOrDefault(w.biome(source).getRegistryName(), Set.of());
      for (String t : info.allowedBiomes) {
         if (types.contains(t)) {
            return true;
         }
      }
      return false;
   }

   /** forEachChunkBlock over the populate area (offset 8), stone within clusterSize of the centre becomes the new stone. */
   private static void generateChunkPart(DWorld w, StoneInfo info, BlockState state, BlockPos src, int chunkX, int chunkZ) {
      int r = info.clusterSize;
      int minY = Math.max(Math.max(1, info.lowerBound - r), src.getY() - r);
      int maxY = Math.min(Math.min(255, info.upperBound + r), src.getY() + r + 1);
      int x0 = Math.max(chunkX * 16 + 8, src.getX() - r);
      int x1 = Math.min(chunkX * 16 + 24, src.getX() + r + 1);
      int z0 = Math.max(chunkZ * 16 + 8, src.getZ() - r);
      int z1 = Math.min(chunkZ * 16 + 24, src.getZ() + r + 1);
      double r2 = (double)(r * r);
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      for (int x = x0; x < x1; x++) {
         for (int y = minY; y < maxY; y++) {
            for (int z = z0; z < z1; z++) {
               pos.set(x, y, z);
               if (pos.distSqr(src) < r2 && w.get(pos).is(Blocks.STONE)) {
                  w.set(pos.immutable(), state);
               }
            }
         }
      }
   }
}
