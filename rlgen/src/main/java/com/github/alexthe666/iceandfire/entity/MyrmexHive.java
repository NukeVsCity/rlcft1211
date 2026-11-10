package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.structures.WorldGenMyrmexHive;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Ice and Fire 1.7.1 MyrmexHive: rooms, entrances, members, aggressors and player reputation of one colony. */
public class MyrmexHive {
   private Level world;
   private final List<BlockPos> foodRooms = Lists.newArrayList();
   private final List<BlockPos> babyRooms = Lists.newArrayList();
   private final List<BlockPos> miscRooms = Lists.newArrayList();
   private final List<BlockPos> allRooms = Lists.newArrayList();
   private final Map<BlockPos, Direction> entrances = Maps.newHashMap();
   private final Map<BlockPos, Direction> entranceBottoms = Maps.newHashMap();
   private BlockPos centerHelper = BlockPos.ZERO;
   private BlockPos center = BlockPos.ZERO;
   private int villageRadius;
   private int lastAddDoorTimestamp;
   private int tickCounter;
   private int numMyrmex;
   private int noBreedTicks;
   private final Map<UUID, Integer> playerReputation = Maps.newHashMap();
   private final List<MyrmexHive.HiveAggressor> villageAgressors = Lists.newArrayList();
   private final List<UUID> myrmexList = Lists.newArrayList();
   public UUID hiveUUID;
   public String colonyName = "";
   public boolean reproduces = true;
   public boolean hasOwner = false;
   public UUID ownerUUID = null;

   public MyrmexHive() {
      this.hiveUUID = UUID.randomUUID();
   }

   public MyrmexHive(Level worldIn) {
      this.world = worldIn;
      this.hiveUUID = UUID.randomUUID();
   }

   public MyrmexHive(Level worldIn, BlockPos center, int radius) {
      this.world = worldIn;
      this.center = center;
      this.villageRadius = radius;
      this.hiveUUID = UUID.randomUUID();
   }

   public void setWorld(Level worldIn) {
      this.world = worldIn;
   }

   public void tick(int tickCounterIn, Level world) {
      this.tickCounter++;
      this.removeDeadAndOldAgressors();
      if (this.tickCounter % 20 == 0) {
         this.updateNumMyrmex(world);
      }
   }

   private void updateNumMyrmex(Level world) {
      this.numMyrmex = this.myrmexList.size();
      if (this.numMyrmex == 0) {
         this.playerReputation.clear();
      }
   }

   @Nullable
   public EntityMyrmexQueen getQueen() {
      if (!(this.world instanceof ServerLevel server)) {
         return null;
      }
      List<EntityMyrmexQueen> ourQueens = new ArrayList<>();

      for (Entity e : server.getAllEntities()) {
         if (e instanceof EntityMyrmexQueen queen && !queen.isSpectator() && queen.getHive() != null && queen.getHive().equals(this)) {
            ourQueens.add(queen);
         }
      }

      return ourQueens.isEmpty() ? null : ourQueens.get(0);
   }

   public BlockPos getCenter() {
      return this.center;
   }

   public BlockPos getCenterGround() {
      return getGroundedPos(this.world, this.center);
   }

   public static BlockPos getGroundedPos(Level world, BlockPos pos) {
      BlockPos current = pos;

      while (world.isEmptyBlock(current.below()) && current.getY() > world.getMinBuildHeight()) {
         current = current.below();
      }

      return current;
   }

   public int getVillageRadius() {
      return this.villageRadius;
   }

   public int getNumMyrmex() {
      return this.numMyrmex;
   }

   public boolean isBlockPosWithinSqVillageRadius(BlockPos pos) {
      return this.center.distSqr(pos) < (double)(this.villageRadius * this.villageRadius);
   }

   public boolean isAnnihilated() {
      return false;
   }

   public void addOrRenewAgressor(LivingEntity entitylivingbaseIn, int agressiveLevel) {
      for (MyrmexHive.HiveAggressor aggressor : this.villageAgressors) {
         if (aggressor.agressor == entitylivingbaseIn) {
            aggressor.agressionTime = this.tickCounter;
            return;
         }
      }

      this.villageAgressors.add(new MyrmexHive.HiveAggressor(entitylivingbaseIn, this.tickCounter, agressiveLevel));
   }

   @Nullable
   public LivingEntity findNearestVillageAggressor(LivingEntity entitylivingbaseIn) {
      double d0 = Double.MAX_VALUE;
      int previousAgressionLevel = 0;
      MyrmexHive.HiveAggressor found = null;

      for (int i = 0; i < this.villageAgressors.size(); i++) {
         MyrmexHive.HiveAggressor aggressor = this.villageAgressors.get(i);
         double d1 = aggressor.agressor.distanceToSqr(entitylivingbaseIn);
         int agressionLevel = aggressor.agressionLevel;
         if (d1 <= d0 || agressionLevel > previousAgressionLevel) {
            found = aggressor;
            d0 = d1;
         }

         previousAgressionLevel = agressionLevel;
      }

      return found == null ? null : found.agressor;
   }

   @Nullable
   public Player getNearestTargetPlayer(LivingEntity villageDefender, Level world) {
      double d0 = Double.MAX_VALUE;
      Player entityplayer = null;

      for (UUID s : this.playerReputation.keySet()) {
         if (this.isPlayerReputationTooLowToFight(s)) {
            Player entityplayer1 = world.getPlayerByUUID(s);
            if (entityplayer1 != null) {
               double d1 = entityplayer1.distanceToSqr(villageDefender);
               if (d1 <= d0) {
                  entityplayer = entityplayer1;
                  d0 = d1;
               }
            }
         }
      }

      return entityplayer;
   }

   private void removeDeadAndOldAgressors() {
      Iterator<MyrmexHive.HiveAggressor> iterator = this.villageAgressors.iterator();

      while (iterator.hasNext()) {
         MyrmexHive.HiveAggressor aggressor = iterator.next();
         if (!aggressor.agressor.isAlive() || Math.abs(this.tickCounter - aggressor.agressionTime) > 300) {
            iterator.remove();
         }
      }
   }

   public int getPlayerReputation(UUID playerName) {
      Integer integer = this.playerReputation.get(playerName);
      return integer == null ? 0 : integer;
   }

   public int modifyPlayerReputation(UUID playerName, int reputation) {
      int i = this.getPlayerReputation(playerName);
      int j = Mth.clamp(i + reputation, 0, 100);
      if (this.hasOwner && playerName.equals(this.ownerUUID)) {
         j = 100;
      }

      Player player = null;

      try {
         player = this.world == null ? null : this.world.getPlayerByUUID(playerName);
      } catch (Exception e) {
         com.mojang.logging.LogUtils.getLogger().warn("Myrmex Hive could not find player with associated UUID");
      }

      if (player != null) {
         if (j - i != 0) {
            player.displayClientMessage(
               Component.translatable(j - i >= 0 ? "myrmex.message.raised_reputation" : "myrmex.message.lowered_reputation", Math.abs(j - i), j), true
            );
         }

         if (i < 25 && j >= 25) {
            player.displayClientMessage(Component.translatable("myrmex.message.peaceful"), false);
         }

         if (i >= 25 && j < 25) {
            player.displayClientMessage(Component.translatable("myrmex.message.hostile"), false);
         }

         if (i < 50 && j >= 50) {
            player.displayClientMessage(Component.translatable("myrmex.message.trade"), false);
         }

         if (i >= 50 && j < 50) {
            player.displayClientMessage(Component.translatable("myrmex.message.no_trade"), false);
         }

         if (i < 75 && j >= 75) {
            player.displayClientMessage(Component.translatable("myrmex.message.can_use_staff"), false);
         }

         if (i >= 75 && j < 75) {
            player.displayClientMessage(Component.translatable("myrmex.message.cant_use_staff"), false);
         }
      }

      this.playerReputation.put(playerName, j);
      MyrmexWorldData.markDirty(this.world);
      return j;
   }

   public boolean isPlayerReputationTooLowToTrade(UUID uuid) {
      return this.getPlayerReputation(uuid) < 50;
   }

   public boolean canPlayerCommandHive(UUID uuid) {
      return this.getPlayerReputation(uuid) >= 75;
   }

   public boolean isPlayerReputationTooLowToFight(UUID uuid) {
      return this.getPlayerReputation(uuid) < 25;
   }

   private static ListTag posList(List<BlockPos> list) {
      ListTag out = new ListTag();
      for (BlockPos pos : list) {
         CompoundTag t = new CompoundTag();
         t.putInt("X", pos.getX());
         t.putInt("Y", pos.getY());
         t.putInt("Z", pos.getZ());
         out.add(t);
      }
      return out;
   }

   private static void readPosList(CompoundTag compound, String key, List<BlockPos> list) {
      ListTag tags = compound.getList(key, Tag.TAG_COMPOUND);
      list.clear();
      for (int i = 0; i < tags.size(); i++) {
         CompoundTag t = tags.getCompound(i);
         list.add(new BlockPos(t.getInt("X"), t.getInt("Y"), t.getInt("Z")));
      }
   }

   private static ListTag facingMap(Map<BlockPos, Direction> map) {
      ListTag out = new ListTag();
      for (Entry<BlockPos, Direction> entry : map.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putInt("X", entry.getKey().getX());
         t.putInt("Y", entry.getKey().getY());
         t.putInt("Z", entry.getKey().getZ());
         t.putInt("Facing", entry.getValue().get2DDataValue());
         out.add(t);
      }
      return out;
   }

   private static void readFacingMap(CompoundTag compound, String key, Map<BlockPos, Direction> map) {
      ListTag tags = compound.getList(key, Tag.TAG_COMPOUND);
      map.clear();
      for (int i = 0; i < tags.size(); i++) {
         CompoundTag t = tags.getCompound(i);
         map.put(new BlockPos(t.getInt("X"), t.getInt("Y"), t.getInt("Z")), Direction.from2DDataValue(t.getInt("Facing")));
      }
   }

   public void readVillageDataFromNBT(CompoundTag compound) {
      this.numMyrmex = compound.getInt("PopSize");
      this.reproduces = compound.getBoolean("Reproduces");
      this.hasOwner = compound.getBoolean("HasOwner");
      this.ownerUUID = compound.hasUUID("OwnerUUID") ? compound.getUUID("OwnerUUID") : null;
      this.colonyName = compound.getString("ColonyName");
      this.villageRadius = compound.getInt("Radius");
      this.lastAddDoorTimestamp = compound.getInt("Stable");
      this.tickCounter = compound.getInt("Tick");
      this.noBreedTicks = compound.getInt("MTick");
      this.center = new BlockPos(compound.getInt("CX"), compound.getInt("CY"), compound.getInt("CZ"));
      this.centerHelper = new BlockPos(compound.getInt("ACX"), compound.getInt("ACY"), compound.getInt("ACZ"));
      ListTag hiveMembers = compound.getList("HiveMembers", Tag.TAG_COMPOUND);
      this.myrmexList.clear();

      for (int i = 0; i < hiveMembers.size(); i++) {
         this.myrmexList.add(hiveMembers.getCompound(i).getUUID("MyrmexUUID"));
      }

      readPosList(compound, "FoodRooms", this.foodRooms);
      readPosList(compound, "BabyRooms", this.babyRooms);
      readPosList(compound, "MiscRooms", this.miscRooms);
      readFacingMap(compound, "Entrances", this.entrances);
      readFacingMap(compound, "EntranceBottoms", this.entranceBottoms);
      this.hiveUUID = compound.getUUID("HiveUUID");
      ListTag players = compound.getList("Players", Tag.TAG_COMPOUND);

      for (int j = 0; j < players.size(); j++) {
         CompoundTag t = players.getCompound(j);
         if (t.contains("UUID")) {
            this.playerReputation.put(UUID.fromString(t.getString("UUID")), t.getInt("S"));
         }
      }
   }

   public void writeVillageDataToNBT(CompoundTag compound) {
      compound.putInt("PopSize", this.numMyrmex);
      compound.putBoolean("Reproduces", this.reproduces);
      compound.putBoolean("HasOwner", this.hasOwner);
      if (this.ownerUUID != null) {
         compound.putUUID("OwnerUUID", this.ownerUUID);
      }

      compound.putString("ColonyName", this.colonyName);
      compound.putInt("Radius", this.villageRadius);
      compound.putInt("Stable", this.lastAddDoorTimestamp);
      compound.putInt("Tick", this.tickCounter);
      compound.putInt("MTick", this.noBreedTicks);
      compound.putInt("CX", this.center.getX());
      compound.putInt("CY", this.center.getY());
      compound.putInt("CZ", this.center.getZ());
      compound.putInt("ACX", this.centerHelper.getX());
      compound.putInt("ACY", this.centerHelper.getY());
      compound.putInt("ACZ", this.centerHelper.getZ());
      ListTag hiveMembers = new ListTag();

      for (UUID memberUUID : this.myrmexList) {
         CompoundTag t = new CompoundTag();
         t.putUUID("MyrmexUUID", memberUUID);
         hiveMembers.add(t);
      }

      compound.put("HiveMembers", hiveMembers);
      compound.put("FoodRooms", posList(this.foodRooms));
      compound.put("BabyRooms", posList(this.babyRooms));
      compound.put("MiscRooms", posList(this.miscRooms));
      compound.put("Entrances", facingMap(this.entrances));
      compound.put("EntranceBottoms", facingMap(this.entranceBottoms));
      compound.putUUID("HiveUUID", this.hiveUUID);
      compound.put("Doors", new ListTag());
      ListTag players = new ListTag();

      for (UUID s : this.playerReputation.keySet()) {
         CompoundTag t = new CompoundTag();
         t.putString("UUID", s.toString());
         t.putInt("S", this.playerReputation.get(s));
         players.add(t);
      }

      compound.put("Players", players);
   }

   public void addRoom(BlockPos center, WorldGenMyrmexHive.RoomType roomType) {
      if (roomType == WorldGenMyrmexHive.RoomType.FOOD && !this.foodRooms.contains(center)) {
         this.foodRooms.add(center);
      } else if (roomType == WorldGenMyrmexHive.RoomType.NURSERY && !this.babyRooms.contains(center)) {
         this.babyRooms.add(center);
      } else if (!this.miscRooms.contains(center)) {
         this.miscRooms.add(center);
      }

      this.allRooms.add(center);
   }

   public void addRoomWithMessage(Player player, BlockPos center, WorldGenMyrmexHive.RoomType roomType) {
      List<BlockPos> allCurrentRooms = new ArrayList<>(this.getAllRooms());
      allCurrentRooms.addAll(this.getEntrances().keySet());
      allCurrentRooms.addAll(this.getEntranceBottoms().keySet());
      String key;
      if (roomType == WorldGenMyrmexHive.RoomType.FOOD) {
         if (!this.foodRooms.contains(center) && !allCurrentRooms.contains(center)) {
            this.foodRooms.add(center);
            key = "myrmex.message.added_food_room";
         } else {
            key = "myrmex.message.dupe_room";
         }
      } else if (roomType == WorldGenMyrmexHive.RoomType.NURSERY) {
         if (!this.babyRooms.contains(center) && !allCurrentRooms.contains(center)) {
            this.babyRooms.add(center);
            key = "myrmex.message.added_nursery_room";
         } else {
            key = "myrmex.message.dupe_room";
         }
      } else if (!this.miscRooms.contains(center) && !allCurrentRooms.contains(center)) {
         this.miscRooms.add(center);
         key = "myrmex.message.added_misc_room";
      } else {
         key = "myrmex.message.dupe_room";
      }
      player.displayClientMessage(Component.translatable(key, center.getX(), center.getY(), center.getZ()), false);
      MyrmexWorldData.markDirty(this.world);
   }

   public void addEnteranceWithMessage(Player player, boolean bottom, BlockPos center, Direction facing) {
      List<BlockPos> allCurrentRooms = new ArrayList<>(this.getAllRooms());
      allCurrentRooms.addAll(this.getEntrances().keySet());
      allCurrentRooms.addAll(this.getEntranceBottoms().keySet());
      String key;
      if (allCurrentRooms.contains(center)) {
         key = "myrmex.message.dupe_room";
      } else if (bottom) {
         this.getEntranceBottoms().put(center, facing);
         key = "myrmex.message.added_enterance_bottom";
      } else {
         this.getEntrances().put(center, facing);
         key = "myrmex.message.added_enterance_surface";
      }
      player.displayClientMessage(Component.translatable(key, center.getX(), center.getY(), center.getZ()), false);
      MyrmexWorldData.markDirty(this.world);
   }

   public List<BlockPos> getRooms(WorldGenMyrmexHive.RoomType roomType) {
      if (roomType == WorldGenMyrmexHive.RoomType.FOOD) {
         return this.foodRooms;
      } else {
         return roomType == WorldGenMyrmexHive.RoomType.NURSERY ? this.babyRooms : this.miscRooms;
      }
   }

   public List<BlockPos> getAllRooms() {
      this.allRooms.clear();
      this.allRooms.add(this.center);
      this.allRooms.addAll(this.foodRooms);
      this.allRooms.addAll(this.babyRooms);
      this.allRooms.addAll(this.miscRooms);
      return this.allRooms;
   }

   public BlockPos getRandomRoom(RandomSource random, BlockPos returnPos) {
      List<BlockPos> rooms = this.getAllRooms();
      return rooms.isEmpty() ? returnPos : rooms.get(random.nextInt(Math.max(rooms.size() - 1, 1)));
   }

   public BlockPos getRandomRoom(WorldGenMyrmexHive.RoomType roomType, RandomSource random, BlockPos returnPos) {
      List<BlockPos> rooms = this.getRooms(roomType);
      return rooms.isEmpty() ? returnPos : rooms.get(random.nextInt(Math.max(rooms.size() - 1, 1)));
   }

   public BlockPos getClosestEntranceToEntity(Entity entity, RandomSource random, boolean randomize) {
      Entry<BlockPos, Direction> closest = this.getClosestEntrance(entity);
      if (closest != null) {
         if (randomize) {
            BlockPos pos = closest.getKey().relative(closest.getValue(), random.nextInt(7) + 7).above(4);
            return pos.offset(10 - random.nextInt(20), 0, 10 - random.nextInt(20));
         } else {
            return closest.getKey().relative(closest.getValue(), 3);
         }
      } else {
         return entity.blockPosition();
      }
   }

   public BlockPos getClosestEntranceBottomToEntity(Entity entity, RandomSource random) {
      Entry<BlockPos, Direction> closest = null;

      for (Entry<BlockPos, Direction> entry : this.entranceBottoms.entrySet()) {
         if (closest == null || closest.getKey().distToLowCornerSqr(entity.getX(), entity.getY(), entity.getZ()) > entry.getKey().distToLowCornerSqr(entity.getX(), entity.getY(), entity.getZ())) {
            closest = entry;
         }
      }

      return closest != null ? closest.getKey() : entity.blockPosition();
   }

   @Nullable
   public Player getOwner(Level world) {
      return this.hasOwner ? world.getPlayerByUUID(this.ownerUUID) : null;
   }

   public Map<BlockPos, Direction> getEntrances() {
      return this.entrances;
   }

   public Map<BlockPos, Direction> getEntranceBottoms() {
      return this.entranceBottoms;
   }

   @Nullable
   private Entry<BlockPos, Direction> getClosestEntrance(Entity entity) {
      Entry<BlockPos, Direction> closest = null;

      for (Entry<BlockPos, Direction> entry : this.entrances.entrySet()) {
         if (closest == null || closest.getKey().distToLowCornerSqr(entity.getX(), entity.getY(), entity.getZ()) > entry.getKey().distToLowCornerSqr(entity.getX(), entity.getY(), entity.getZ())) {
            closest = entry;
         }
      }

      return closest;
   }

   public void setDefaultPlayerReputation(int defaultReputation) {
      for (UUID s : new ArrayList<>(this.playerReputation.keySet())) {
         this.modifyPlayerReputation(s, defaultReputation);
      }
   }

   public boolean repopulate() {
      int roomCount = this.getAllRooms().size();
      return this.numMyrmex < Math.min(IceAndFire.CONFIG.myrmexColonySize, roomCount * 9) && this.reproduces;
   }

   public void addMyrmex(EntityMyrmexBase myrmex) {
      if (!this.myrmexList.contains(myrmex.getUUID())) {
         this.myrmexList.add(myrmex.getUUID());
         MyrmexWorldData.markDirty(this.world);
      }
   }

   public void removeRoom(BlockPos pos) {
      this.foodRooms.remove(pos);
      this.miscRooms.remove(pos);
      this.babyRooms.remove(pos);
      this.allRooms.remove(pos);
      this.getEntrances().remove(pos);
      this.getEntranceBottoms().remove(pos);
   }

   @Override
   public String toString() {
      return "MyrmexHive(x=" + this.center.getX() + ",y=" + this.center.getY() + ",z=" + this.center.getZ() + "), population=" + this.getNumMyrmex() + "\nUUID: " + this.hiveUUID;
   }

   public boolean equals(MyrmexHive hive) {
      return hive != null && this.hiveUUID.equals(hive.hiveUUID);
   }

   class HiveAggressor {
      public LivingEntity agressor;
      public int agressionTime;
      public int agressionLevel;

      HiveAggressor(LivingEntity agressorIn, int agressionTimeIn, int agressionLevel) {
         this.agressor = agressorIn;
         this.agressionTime = agressionTimeIn;
         this.agressionLevel = agressionLevel;
      }
   }
}
