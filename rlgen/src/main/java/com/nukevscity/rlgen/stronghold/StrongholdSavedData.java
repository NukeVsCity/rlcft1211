package com.nukevscity.rlgen.stronghold;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/** The stronghold components 1.12 had dropped from their start (addComponentParts found liquid), saved like MapGenStructureData. */
public final class StrongholdSavedData extends SavedData {
   private static final String NAME = "rlgen_strongholds";
   private static volatile StrongholdSavedData current = new StrongholdSavedData();
   private final Map<Long, BitSet> removed = new HashMap<>();

   static StrongholdSavedData current() {
      return current;
   }

   /** Called when the overworld loads. */
   public static void attach(ServerLevel overworld) {
      current = overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(StrongholdSavedData::new, StrongholdSavedData::load, null), NAME);
   }

   synchronized boolean isRemoved(ChunkPos start, int index) {
      BitSet b = this.removed.get(start.toLong());
      return b != null && b.get(index);
   }

   synchronized void remove(ChunkPos start, int index) {
      this.removed.computeIfAbsent(start.toLong(), k -> new BitSet()).set(index);
      this.setDirty();
   }

   private static StrongholdSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
      StrongholdSavedData d = new StrongholdSavedData();
      CompoundTag r = tag.getCompound("removed");
      for (String k : r.getAllKeys()) {
         d.removed.put(Long.parseLong(k), BitSet.valueOf(r.getLongArray(k)));
      }
      return d;
   }

   @Override
   public synchronized CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
      CompoundTag r = new CompoundTag();
      for (Map.Entry<Long, BitSet> e : this.removed.entrySet()) {
         r.putLongArray(Long.toString(e.getKey()), e.getValue().toLongArray());
      }
      tag.put("removed", r);
      return tag;
   }
}
