package com.github.alexthe666.iceandfire.world;

import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Ice and Fire 1.7.1 MyrmexWorldData ("iceandfire_myrmex", per dimension): the hives of a world. 1.12 never ticked it; a hive
 * ticks only through its queen. On the client (and for client callers) an empty instance is used.
 */
public class MyrmexWorldData extends SavedData {
   private static final String IDENTIFIER = "iceandfire_myrmex";
   private Level world;
   private final List<MyrmexHive> hiveList = Lists.newArrayList();
   private int tickCounter;
   private static final MyrmexWorldData CLIENT = new MyrmexWorldData();

   public MyrmexWorldData() {
   }

   public void setWorldsForAll(Level worldIn) {
      this.world = worldIn;

      for (MyrmexHive village : this.hiveList) {
         village.setWorld(worldIn);
      }
   }

   public void tick() {
      this.tickCounter++;

      for (MyrmexHive hive : this.hiveList) {
         hive.tick(this.tickCounter, this.world);
      }
   }

   public List<MyrmexHive> getHivelist() {
      return this.hiveList;
   }

   @Nullable
   public MyrmexHive getNearestHive(BlockPos doorBlock, int radius) {
      MyrmexHive village = null;
      double d0 = Float.MAX_VALUE;

      for (MyrmexHive village1 : this.hiveList) {
         double d1 = village1.getCenter().distSqr(doorBlock);
         if (d1 < d0) {
            float f = (float)(radius + village1.getVillageRadius());
            if (d1 <= (double)(f * f)) {
               village = village1;
               d0 = d1;
            }
         }
      }

      return village;
   }

   private static MyrmexWorldData load(CompoundTag nbt, HolderLookup.Provider provider) {
      MyrmexWorldData data = new MyrmexWorldData();
      data.tickCounter = nbt.getInt("Tick");
      ListTag list = nbt.getList("Hives", Tag.TAG_COMPOUND);

      for (int i = 0; i < list.size(); i++) {
         MyrmexHive village = new MyrmexHive();
         village.readVillageDataFromNBT(list.getCompound(i));
         data.hiveList.add(village);
      }

      return data;
   }

   @Override
   public CompoundTag save(CompoundTag compound, HolderLookup.Provider provider) {
      compound.putInt("Tick", this.tickCounter);
      ListTag list = new ListTag();

      for (MyrmexHive village : this.hiveList) {
         CompoundTag t = new CompoundTag();
         village.writeVillageDataToNBT(t);
         list.add(t);
      }

      compound.put("Hives", list);
      return compound;
   }

   public static MyrmexWorldData get(Level world) {
      if (!(world instanceof ServerLevel server)) {
         return CLIENT;
      }
      MyrmexWorldData instance = server.getDataStorage().computeIfAbsent(new SavedData.Factory<>(MyrmexWorldData::new, MyrmexWorldData::load, null), IDENTIFIER);
      if (instance.world != world) {
         instance.setWorldsForAll(world);
      }
      instance.setDirty();
      return instance;
   }

   public static void markDirty(@Nullable Level world) {
      if (world instanceof ServerLevel) {
         get(world).setDirty();
      }
   }

   public static void addHive(Level world, MyrmexHive hive) {
      hive.setWorld(world);
      get(world).hiveList.add(hive);
   }

   @Nullable
   public MyrmexHive getHiveFromUUID(UUID id) {
      for (MyrmexHive hive : this.hiveList) {
         if (hive.hiveUUID != null && hive.hiveUUID.equals(id)) {
            return hive;
         }
      }

      // a hive world generation built (WorldGenMyrmexHive called addHive before spawning its myrmexes)
      MyrmexHive pending = id == null ? null : WORLDGEN_PENDING.remove(id);
      if (pending != null && this.world != null) {
         addHive(this.world, pending);
         return pending;
      }
      return null;
   }

   /**
    * Hives built by world generation threads (rlgen's planned WorldGenMyrmexHive), waiting for the server thread: added on the next
    * server task, or as soon as one of their myrmexes is loaded and asks for its hive, whichever comes first.
    */
   public static final java.util.Map<UUID, MyrmexHive> WORLDGEN_PENDING = new java.util.concurrent.ConcurrentHashMap<>();
}
