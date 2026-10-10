package com.nukevscity.rlgen.roguelike;

import com.github.fnar.minecraft.item.mapper.Mc;
import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import greymerk.roguelike.dungeon.Dungeon;
import greymerk.roguelike.util.RldRandom;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

/**
 * Roguelike Dungeons' IWorldGenerator (DungeonGenerator1_12): every 51st chunk on both axes (3 x spawnFrequency 17) tries a dungeon
 * within 408 blocks, with the chunk random Forge gave IWorldGenerators. The dungeon is built once over the 1.12 terrain and split by chunk.
 */
public final class RoguelikeDungeons {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final int SPACING = 51;

   /** One block or block entity write, in the order Roguelike made them. */
   record Slice(List<Map.Entry<Long, BlockState>> blocks, Map<Long, BufferedEditor.Tile> tiles) {
   }

   static final class Built {
      final LongSet chunks = new LongOpenHashSet();
      final Map<Long, Slice> slices = new HashMap<>();
   }

   private static final Map<String, LongSet> INDEX = new LinkedHashMap<>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, LongSet> e) {
         return this.size() > 512;
      }
   };
   private static final Map<String, Built> DATA = new LinkedHashMap<>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Built> e) {
         return this.size() > 6;
      }
   };
   private static final Map<String, Object> LOCKS = new HashMap<>();

   private RoguelikeDungeons() {
   }

   private static String key(long seed, int gx, int gz) {
      return seed + "/" + gx + "/" + gz;
   }

   /** Grid corners whose dungeon may reach this chunk (origin within 408 blocks of the corner, five levels drifting up to 80 each). */
   static List<int[]> cornersNear(int cx, int cz) {
      List<int[]> out = new ArrayList<>();
      int bx = Math.floorDiv(cx, SPACING) * SPACING;
      int bz = Math.floorDiv(cz, SPACING) * SPACING;
      for (int dx = -1; dx <= 2; dx++) {
         for (int dz = -1; dz <= 2; dz++) {
            int gx = bx + dx * SPACING;
            int gz = bz + dz * SPACING;
            if (Math.abs(gx - cx) <= 52 && Math.abs(gz - cz) <= 52) {
               out.add(new int[]{gx, gz});
            }
         }
      }
      return out;
   }

   static LongSet chunks(LegacyChunkGenerator gen, long seed, int gx, int gz, RegistryAccess access) {
      String k = key(seed, gx, gz);
      synchronized (INDEX) {
         LongSet s = INDEX.get(k);
         if (s != null) {
            return s;
         }
      }
      return built(gen, seed, gx, gz, access).chunks;
   }

   static Built built(LegacyChunkGenerator gen, long seed, int gx, int gz, RegistryAccess access) {
      String k = key(seed, gx, gz);
      Object lock;
      synchronized (LOCKS) {
         lock = LOCKS.computeIfAbsent(k, x -> new Object());
      }
      synchronized (lock) {
         synchronized (DATA) {
            Built b = DATA.get(k);
            if (b != null) {
               return b;
            }
         }
         Built b = build(gen, seed, gx, gz, access);
         synchronized (DATA) {
            DATA.put(k, b);
         }
         synchronized (INDEX) {
            INDEX.put(k, b.chunks);
         }
         return b;
      }
   }

   private static Built build(LegacyChunkGenerator gen, long seed, int gx, int gz, RegistryAccess access) {
      Built built = new Built();
      // GameRegistry.generateWorld: the chunk random every IWorldGenerator got ("nextLong() >> 2 + 1" shifts by 3)
      Random fml = new Random(seed);
      long xSeed = fml.nextLong() >> 2 + 1L;
      long zSeed = fml.nextLong() >> 2 + 1L;
      long chunkSeed = xSeed * (long)gx + zSeed * (long)gz ^ seed;
      Random random = new Random(chunkSeed);
      RldRandom.seed(chunkSeed);
      Mc.setAccess(access);
      BufferedEditor editor = new BufferedEditor(gen::legacyState, p -> gen.legacyBiome(p.getX(), p.getZ()), seed);
      try {
         new Dungeon(editor).spawnInChunk(random, gx, gz);
      } catch (RuntimeException e) {
         LOGGER.error("rlgen: roguelike dungeon at chunk {} {} failed", gx, gz, e);
      }
      Map<Long, List<Map.Entry<Long, BlockState>>> blocks = new HashMap<>();
      for (Map.Entry<Long, BlockState> e : editor.blocks.entrySet()) {
         BlockPos p = BlockPos.of(e.getKey());
         long chunk = ChunkPos.asLong(p.getX() >> 4, p.getZ() >> 4);
         blocks.computeIfAbsent(chunk, c -> new ArrayList<>()).add(e);
         built.chunks.add(chunk);
      }
      Map<Long, Map<Long, BufferedEditor.Tile>> tiles = new HashMap<>();
      editor.tiles.forEach((pos, tile) -> {
         BlockPos p = BlockPos.of(pos);
         tiles.computeIfAbsent(ChunkPos.asLong(p.getX() >> 4, p.getZ() >> 4), c -> new HashMap<>()).put(pos, tile);
      });
      for (long chunk : built.chunks) {
         built.slices.put(chunk, new Slice(blocks.getOrDefault(chunk, List.of()), tiles.getOrDefault(chunk, Map.of())));
      }
      if (!built.chunks.isEmpty()) {
         LOGGER.info("rlgen: roguelike dungeon from grid chunk {} {}: {} blocks in {} chunks", gx, gz, editor.blocks.size(), built.chunks.size());
      }
      return built;
   }
}
