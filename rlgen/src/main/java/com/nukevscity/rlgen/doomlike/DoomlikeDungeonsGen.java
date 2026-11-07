package com.nukevscity.rlgen.doomlike;

import com.github.fnar.minecraft.item.mapper.Mc;
import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import jaredbgreat.dldungeons.DoomlikeDungeons;
import jaredbgreat.dldungeons.GenerationHandler;
import jaredbgreat.dldungeons.builder.Builder;
import jaredbgreat.dldungeons.compat.World;
import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.themes.Sizes;
import jaredbgreat.dldungeons.util.cache.Coords;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.WorldGenLevel;
import org.slf4j.Logger;

/**
 * Doomlike Dungeons' GenerationHandler: each populated chunk looked for dungeon centres in the 13x13 chunks around it and built its
 * share of each (the populate area, offset 8 blocks) straight into the world. Planning and building take one lock, as 1.12 was single threaded.
 */
public final class DoomlikeDungeonsGen {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final Object LOCK = new Object();
   private static final Map<String, Dungeon> CACHE = new LinkedHashMap<>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Dungeon> e) {
         return this.size() > 24;
      }
   };

   private DoomlikeDungeonsGen() {
   }

   /** Centres whose dungeon has a part in this chunk, in the order 1.12 built them (Coords.HashCompare). */
   static List<Coords> centresFor(LegacyChunkGenerator generator, long seed, int chunkX, int chunkZ) {
      return centresFor(new World(seed, generator), seed, chunkX, chunkZ, 0);
   }

   private static List<Coords> centresFor(World world, long seed, int chunkX, int chunkZ, int dim) {
      DoomlikeDungeons.init();
      List<Coords> out = new ArrayList<>();
      int r = Sizes.HUGE.chunkRadius;
      for (int i = chunkX - r; i <= chunkX + r; i++) {
         for (int j = chunkZ - r; j <= chunkZ + r; j++) {
            if (GenerationHandler.isDungeonCenter(world, seed, i, j, dim)) {
               out.add(new Coords(i, j, dim));
            }
         }
      }
      out.sort(Coords.HashCompare.C);
      return out;
   }

   /** GenerationHandler.generate in dimension -1 (RLCraft lists dimensions 0 and -1): plans and builds on the 1.21 nether terrain. */
   public static void netherChunk(WorldGenLevel level, int chunkX, int chunkZ) {
      long seed = level.getSeed();
      List<Coords> centres;
      synchronized (LOCK) {
         centres = centresFor(new World(seed, -1), seed, chunkX, chunkZ, -1);
      }
      for (Coords c : centres) {
         build(level, () -> new World(seed, -1), -1, c.getX(), c.getZ(), chunkX, chunkZ);
      }
   }

   static void buildInChunk(WorldGenLevel level, LegacyChunkGenerator generator, int centreX, int centreZ, int chunkX, int chunkZ) {
      long seed = level.getSeed();
      build(level, () -> new World(seed, generator), 0, centreX, centreZ, chunkX, chunkZ);
   }

   private static void build(WorldGenLevel level, Supplier<World> worlds, int dim, int centreX, int centreZ, int chunkX, int chunkZ) {
      long seed = level.getSeed();
      RegistryAccess access = level.registryAccess();
      synchronized (LOCK) {
         DoomlikeDungeons.init();
         Mc.setAccess(access);
         String key = seed + "/" + dim + "/" + centreX + "/" + centreZ;
         Dungeon dungeon = CACHE.get(key);
         if (dungeon == null) {
            try {
               dungeon = new Dungeon(worlds.get(), new Coords(centreX, centreZ, dim));
               if (dungeon.theme != null) {
                  LOGGER.info("rlgen: doomlike dungeon at chunk {} {} (dim {}): theme {}, size {}", centreX, centreZ, dim, dungeon.theme.name, dungeon.size);
               }
            } catch (Throwable e) {
               LOGGER.error("rlgen: doomlike dungeon at chunk {} {} failed to plan", centreX, centreZ, e);
               return;
            }
            CACHE.put(key, dungeon);
         }
         if (dungeon.theme == null || dungeon.map == null) {
            return;
         }
         World world = dungeon.map.world;
         world.bind(level, chunkX, chunkZ);
         try {
            Builder.buildDungeonChunk(chunkX, chunkZ, dungeon);
         } catch (RuntimeException e) {
            LOGGER.error("rlgen: doomlike dungeon {} {} failed in chunk {} {}", centreX, centreZ, chunkX, chunkZ, e);
         } finally {
            world.unbind();
         }
      }
   }
}
