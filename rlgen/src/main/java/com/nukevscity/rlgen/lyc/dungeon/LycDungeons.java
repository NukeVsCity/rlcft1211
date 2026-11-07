package com.nukevscity.rlgen.lyc.dungeon;

import com.lycanitesmobs.core.dungeon.DungeonManager;
import com.lycanitesmobs.core.dungeon.instance.DungeonInstance;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import com.yungnickyoung.minecraft.bettermineshafts.mc.World;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Lycanites Mobs WorldGeneratorDungeon with RLCraft's settings (distance 45 chunks, tower chance 0.1). When a chunk is generated with
 * no dungeon within 90 chunks, four dungeons are planned 45 chunks north, south, east and west of it (so where dungeons end up
 * depends on where the world was explored first, like 1.12). The dungeons are kept with the world (rlgen_lyc_dungeons) and their
 * layouts are rebuilt from their seeds. world.rand (schematic and seed) is replaced by a random seeded from the chunk.
 * 1.12 ran the same generator in every dimension with one ExtendedWorld each: the nether and End have their own list
 * (rlgen_lyc_dungeons in their data folders) and are planned on the 1.21 terrain there.
 */
public final class LycDungeons extends SavedData {
   private static final String NAME = "rlgen_lyc_dungeons";
   private static volatile LycDungeons current = new LycDungeons();
   private static volatile LycDungeons nether;
   private static volatile LycDungeons end;

   private record Entry(BlockPos origin, String schematic, long seed) {
   }

   private final List<Entry> entries = new ArrayList<>();
   private final List<DungeonInstance> instances = new ArrayList<>();
   private LegacyChunkGenerator generator;

   public static void attach(ServerLevel overworld) {
      current = overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(LycDungeons::new, LycDungeons::load, null), NAME);
   }

   /** The nether and End lists, only in worlds whose overworld is the 1.12 one. */
   public static void attachDimension(ServerLevel level) {
      if (!(level.getServer().overworld().getChunkSource().getGenerator() instanceof LegacyChunkGenerator)) {
         return;
      }
      LycDungeons d = level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(LycDungeons::new, LycDungeons::load, null), NAME);
      if (level.dimension() == net.minecraft.world.level.Level.NETHER) {
         d.dimension = -1;
         nether = d;
      } else if (level.dimension() == net.minecraft.world.level.Level.END) {
         d.dimension = 1;
         end = d;
      }
   }

   private static LycDungeons of(int dimension) {
      return dimension == 0 ? current : dimension == -1 ? nether : end;
   }

   private int dimension;

   private World planWorld(LegacyChunkGenerator generator) {
      return this.dimension == 0 ? new World(null, generator, null) : new LycWorld(null, null, this.dimension);
   }

   /** The planned dungeons (layouts built on first use). */
   private List<DungeonInstance> instances(LegacyChunkGenerator generator) {
      if (this.generator != generator) {
         this.generator = generator;
         this.instances.clear();
      }
      while (this.instances.size() < this.entries.size()) {
         Entry e = this.entries.get(this.instances.size());
         DungeonInstance d = new DungeonInstance();
         d.setOrigin(e.origin);
         d.schematic = DungeonManager.getInstance().getSchematic(e.schematic);
         d.setSeed(e.seed);
         if (d.schematic != null) {
            d.init(this.planWorld(generator), new Random(e.seed));
         }
         this.instances.add(d);
      }
      return this.instances;
   }

   /** WorldGeneratorDungeon.generate for a newly generated chunk, then the dungeons whose sectors 1.12 built from chunks around it. */
   public static synchronized List<Integer> reaching(LegacyChunkGenerator generator, long seed, ChunkPos chunkPos) {
      return reaching(0, generator, seed, chunkPos);
   }

   /** generator is null in the nether and End. */
   public static synchronized List<Integer> reaching(int dimension, LegacyChunkGenerator generator, long seed, ChunkPos chunkPos) {
      LycDungeons d = of(dimension);
      if (d == null) {
         return List.of();
      }
      List<DungeonInstance> all = d.instances(generator);
      boolean nearby = false;
      for (DungeonInstance i : all) {
         if (i.layout != null && i.isChunkPosWithin(chunkPos, Lyc.DUNGEON_DISTANCE * 2)) {
            nearby = true;
            break;
         }
      }
      if (!nearby) {
         // each dimension had its own world.rand
         Random worldRand = new Random(seed ^ chunkPos.toLong() * 0x9E3779B97F4A7C15L ^ (long)dimension * 0x632BE59BD9B4E019L);
         // a layout that runs out of connectors throws; WorldGeneratorDungeon caught it, which ended the loop for this chunk
         try {
         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               if ((x != 0 || z != 0) && (x == 0 || z == 0)) {
                  DungeonInstance instance = new DungeonInstance();
                  BlockPos origin = new ChunkPos(chunkPos.x + Lyc.DUNGEON_DISTANCE * x, chunkPos.z + Lyc.DUNGEON_DISTANCE * z).getBlockAt(7, 64, 7);
                  instance.setOrigin(origin);
                  if (instance.init(d.planWorld(generator), worldRand)) {
                     d.entries.add(new Entry(origin, instance.schematic.name, instance.getSeed()));
                     d.instances.add(instance);
                     com.mojang.logging.LogUtils.getLogger().info("rlgen: Lycanites dungeon {} at {} in dimension {} (from chunk {})", instance.schematic.name,
                        instance.originPos, dimension, chunkPos);
                     // the UUID 1.12 gave it
                     worldRand.nextLong();
                     worldRand.nextLong();
                  }
               }
            }
         }
         } catch (RuntimeException e) {
            Lyc.warn("Dungeon", "An exception occurred when trying to generate a dungeon.");
         }
         d.setDirty();
      }
      List<Integer> out = new ArrayList<>();
      for (int n = 0; n < all.size(); n++) {
         DungeonInstance i = all.get(n);
         if (i.layout == null) {
            continue;
         }
         for (int px = chunkPos.x - 1; px <= chunkPos.x && !out.contains(n); px++) {
            for (int pz = chunkPos.z - 1; pz <= chunkPos.z; pz++) {
               if (i.layout.sectorChunkMap.containsKey(new ChunkPos(px, pz))) {
                  out.add(n);
                  break;
               }
            }
         }
      }
      return out;
   }

   static synchronized DungeonInstance instance(int dimension, LegacyChunkGenerator generator, int index) {
      LycDungeons d = of(dimension);
      if (d == null) {
         return null;
      }
      List<DungeonInstance> all = d.instances(generator);
      return index < all.size() ? all.get(index) : null;
   }

   private static LycDungeons load(CompoundTag tag, HolderLookup.Provider provider) {
      LycDungeons d = new LycDungeons();
      for (Tag t : tag.getList("dungeons", Tag.TAG_COMPOUND)) {
         CompoundTag c = (CompoundTag)t;
         d.entries.add(new Entry(BlockPos.of(c.getLong("origin")), c.getString("schematic"), c.getLong("seed")));
      }
      return d;
   }

   @Override
   public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
      ListTag list = new ListTag();
      for (Entry e : this.entries) {
         CompoundTag c = new CompoundTag();
         c.putLong("origin", e.origin.asLong());
         c.putString("schematic", e.schematic);
         c.putLong("seed", e.seed);
         list.add(c);
      }
      tag.put("dungeons", list);
      return tag;
   }

}
