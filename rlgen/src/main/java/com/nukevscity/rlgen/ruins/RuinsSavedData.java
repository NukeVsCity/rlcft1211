package com.nukevscity.rlgen.ruins;

import atomicstryker.ruins.common.RuinData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Ruins' RuinsPositionsFile.txt: every ruin built in this world, plus every try already turned down, so neither is decided twice. */
public final class RuinsSavedData extends SavedData {
   private static final String NAME = "rlgen_ruins";
   private static final Map<String, RuinsSavedData> CURRENT = new java.util.concurrent.ConcurrentHashMap<>();
   private final Map<String, Integer> decisions = new HashMap<>();
   private final List<RuinData> registered = new ArrayList<>();

   /** The data of a dimension (by its 1.12 DimensionType name), kept in that dimension's data folder like RuinsPositionsFile.txt. */
   public static RuinsSavedData current(String dimension) {
      return CURRENT.computeIfAbsent(dimension, d -> new RuinsSavedData());
   }

   /** Called when a level loads: the overworld, the nether and the End each have their own. */
   public static void attach(ServerLevel level) {
      String dim;
      if (level.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
         dim = RuinsTerrain.Dim.OVERWORLD.name;
      } else if (level.dimension() == net.minecraft.world.level.Level.NETHER) {
         dim = RuinsTerrain.Dim.NETHER.name;
      } else if (level.dimension() == net.minecraft.world.level.Level.END) {
         dim = RuinsTerrain.Dim.END.name;
      } else {
         return;
      }
      CURRENT.put(dim, level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(RuinsSavedData::new, RuinsSavedData::load, null), NAME));
   }

   Integer decision(String key) {
      return this.decisions.get(key);
   }

   void decide(String key, int y, RuinData data) {
      this.decisions.put(key, y);
      if (data != null) {
         this.registered.add(data);
      }
      this.setDirty();
   }

   boolean anyIntersecting(RuinData checkSelf, RuinData checkOther, String name) {
      for (RuinData r : this.registered) {
         if (r.name.equals(name) ? checkSelf.intersectsWith(r) : checkOther.intersectsWith(r)) {
            return true;
         }
      }
      return false;
   }

   private static RuinsSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
      RuinsSavedData d = new RuinsSavedData();
      CompoundTag dec = tag.getCompound("decisions");
      for (String k : dec.getAllKeys()) {
         d.decisions.put(k, dec.getInt(k));
      }
      ListTag list = tag.getList("ruins", Tag.TAG_STRING);
      for (int i = 0; i < list.size(); i++) {
         d.registered.add(new RuinData(list.getString(i)));
      }
      return d;
   }

   @Override
   public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
      CompoundTag dec = new CompoundTag();
      this.decisions.forEach(dec::putInt);
      tag.put("decisions", dec);
      ListTag list = new ListTag();
      for (RuinData r : this.registered) {
         list.add(net.minecraft.nbt.StringTag.valueOf(r.toString()));
      }
      tag.put("ruins", list);
      return tag;
   }
}
