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
 */
public final class LycDungeons extends SavedData {
   private static final String NAME = "rlgen_lyc_dungeons";
   private static volatile LycDungeons current = new LycDungeons();

   private record Entry(BlockPos origin, String schematic, long seed) {
   }

   private final List<Entry> entries = new ArrayList<>();
   private final List<DungeonInstance> instances = new ArrayList<>();
   private LegacyChunkGenerator generator;

   public static void attach(ServerLevel overworld) {
      current = overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(LycDungeons::new, LycDungeons::load, null), NAME);
   }

   private static World planWorld(LegacyChunkGenerator generator) {
      return new World(null, generator, null);
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
            d.init(planWorld(generator), new Random(e.seed));
         }
         this.instances.add(d);
      }
      return this.instances;
   }

   /** WorldGeneratorDungeon.generate for a newly generated chunk, then the dungeons whose sectors 1.12 built from chunks around it. */
   public static synchronized List<Integer> reaching(LegacyChunkGenerator generator, long seed, ChunkPos chunkPos) {
      LycDungeons d = current;
      List<DungeonInstance> all = d.instances(generator);
      boolean nearby = false;
      for (DungeonInstance i : all) {
         if (i.layout != null && i.isChunkPosWithin(chunkPos, Lyc.DUNGEON_DISTANCE * 2)) {
            nearby = true;
            break;
         }
      }
      if (!nearby) {
         Random worldRand = new Random(seed ^ chunkPos.toLong() * 0x9E3779B97F4A7C15L);
         // a layout that runs out of connectors throws; WorldGeneratorDungeon caught it, which ended the loop for this chunk
         try {
         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               if ((x != 0 || z != 0) && (x == 0 || z == 0)) {
                  DungeonInstance instance = new DungeonInstance();
                  BlockPos origin = new ChunkPos(chunkPos.x + Lyc.DUNGEON_DISTANCE * x, chunkPos.z + Lyc.DUNGEON_DISTANCE * z).getBlockAt(7, 64, 7);
                  instance.setOrigin(origin);
                  if (instance.init(planWorld(generator), worldRand)) {
                     d.entries.add(new Entry(origin, instance.schematic.name, instance.getSeed()));
                     d.instances.add(instance);
                     com.mojang.logging.LogUtils.getLogger().info("rlgen: Lycanites dungeon {} at {} (from chunk {})", instance.schematic.name, instance.originPos, chunkPos);
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

   static synchronized DungeonInstance instance(LegacyChunkGenerator generator, int index) {
      List<DungeonInstance> all = current.instances(generator);
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
