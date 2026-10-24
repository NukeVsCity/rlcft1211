package com.nukevscity.rlgen.iaf;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Ice and Fire MyrmexWorldData: the hives of this world, so a new one is never within 600 blocks of another (first come, first served). */
public final class MyrmexSavedData extends SavedData {
   private static final String NAME = "rlgen_myrmex";
   /** getNearestHive(pos, 500): 500 plus the hive radius of 100 */
   private static final double MIN_DIST_SQ = 600.0 * 600.0;
   private static volatile MyrmexSavedData current = new MyrmexSavedData();
   private final List<BlockPos> hives = new ArrayList<>();

   public static void attach(ServerLevel overworld) {
      current = overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(MyrmexSavedData::new, MyrmexSavedData::load, null), NAME);
   }

   /** True if a hive may be built at this centre (and remembers it), false if another hive is too close. */
   static synchronized boolean claim(BlockPos center) {
      MyrmexSavedData d = current;
      for (BlockPos h : d.hives) {
         if (h.equals(center)) {
            return true;
         }
      }
      for (BlockPos h : d.hives) {
         if (h.distSqr(center) <= MIN_DIST_SQ) {
            return false;
         }
      }
      d.hives.add(center);
      d.setDirty();
      return true;
   }

   private static MyrmexSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
      MyrmexSavedData d = new MyrmexSavedData();
      for (long l : tag.getLongArray("hives")) {
         d.hives.add(BlockPos.of(l));
      }
      return d;
   }

   @Override
   public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
      tag.put("hives", new LongArrayTag(this.hives.stream().mapToLong(BlockPos::asLong).toArray()));
      return tag;
   }
}
