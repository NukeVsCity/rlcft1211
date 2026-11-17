package com.lycanitesmobs;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
import com.lycanitesmobs.compat.*;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.lycanitesmobs.core.config.ConfigSpawning;
import com.lycanitesmobs.core.dungeon.instance.DungeonInstance;
import com.lycanitesmobs.core.entity.BossEntry;
import com.lycanitesmobs.core.mobevent.MobEvent;
import com.lycanitesmobs.core.mobevent.MobEventManager;
import com.lycanitesmobs.core.mobevent.MobEventPlayerClient;
import com.lycanitesmobs.core.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.network.MessageMobEvent;
import com.lycanitesmobs.core.network.MessageWorldEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


/** 1.12 ExtendedWorld (WorldSavedData in the per-dimension storage "LycanitesMobs"; 1.21 SavedData in the level's DataStorage). */
public class ExtendedWorld extends net.minecraft.world.level.saveddata.SavedData {
   private static final String EXT_PROP_NAME = "LycanitesMobs";
   private static final Map<Level, ExtendedWorld> loadedExtWorlds = new HashMap<>();
   public Level world;
   public boolean initialized = false;
   public long lastSpawnerTime = 0L;
   public long lastEventScheduleTime = 0L;
   public long lastEventUpdateTime = 0L;
   public boolean useTotalWorldTime = true;
   public Map<String, MobEventPlayerServer> serverMobEventPlayers = new HashMap<>();
   public Map<String, MobEventPlayerClient> clientMobEventPlayers = new HashMap<>();
   public MobEventPlayerServer serverWorldEventPlayer = null;
   public MobEventPlayerClient clientWorldEventPlayer = null;
   private long worldEventStartTargetTime = 0L;
   private long worldEventLastStartedTime = 0L;
   private String worldEventName = "";
   private int worldEventCount = -1;
   public Map<UUID, BossEntry> bosses = new HashMap<>();
   public Map<UUID, DungeonInstance> dungeons = new HashMap<>();

   public static ExtendedWorld getForWorld(Level world) {
      return world == null ? null : loadedExtWorlds.computeIfAbsent(world, k -> {
         ExtendedWorld worldExt;
         if (world instanceof ServerLevel serverLevel) {
            boolean[] loaded = new boolean[1];
            worldExt = serverLevel.getDataStorage().computeIfAbsent(new net.minecraft.world.level.saveddata.SavedData.Factory<>(
               () -> new ExtendedWorld(world),
               (tag, provider) -> {
                  ExtendedWorld ext = new ExtendedWorld("LycanitesMobs");
                  ext.readFromNBT(tag);
                  loaded[0] = true;
                  return ext;
               }, null), "LycanitesMobs");
            if (loaded[0]) {
               worldExt.world = world;
               worldExt.init();
            }
         } else {
            // 1.12 client worlds had their own (never saved) storage
            worldExt = new ExtendedWorld(world);
         }

         return worldExt;
      });
   }

   public static void remove(Level world) {
      loadedExtWorlds.remove(world);
   }

   public ExtendedWorld(String prop_name) {
   }

   public ExtendedWorld(Level world) {
      this.world = world;
   }

   @Override
   public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
      return this.writeToNBT(tag);
   }

   public void init() {
      if (!this.initialized) {
         this.initialized = true;
         this.lastSpawnerTime = this.world.getGameTime() - 1L;
         this.lastEventScheduleTime = this.world.getGameTime() - 1L;
         this.lastEventUpdateTime = this.world.getGameTime() - 1L;
         if (!this.world.isClientSide && !"".equals(this.getWorldEventName()) && this.serverWorldEventPlayer == null) {
            long savedLastStartedTime = this.getWorldEventLastStartedTime();
            this.startMobEvent(this.getWorldEventName(), null, LycPos.of(0, 0, 0), 1, -1);
            if (this.serverWorldEventPlayer != null) {
               this.serverWorldEventPlayer.changeStartedWorldTime(savedLastStartedTime);
            }
         }
      }
   }

   public void loadConfig() {
      ConfigSpawning config = ConfigSpawning.getConfig(LycanitesMobs.modInfo, "mobevents");
   }

   protected String getConfigEntryName(String name) {
      return name + " " + this.world.dimension().location().getPath() + " (" + LycDim.id(this.world) + ")";
   }

   public long getWorldEventStartTargetTime() {
      return this.worldEventStartTargetTime;
   }

   public long getWorldEventLastStartedTime() {
      return this.worldEventLastStartedTime;
   }

   public String getWorldEventName() {
      return this.worldEventName;
   }

   public MobEvent getWorldEvent() {
      return this.getWorldEventName() != null && !"".equals(this.getWorldEventName())
         ? MobEventManager.getInstance().getMobEvent(this.getWorldEventName())
         : null;
   }

   public int getWorldEventCount() {
      return this.worldEventCount;
   }

   public void setWorldEventStartTargetTime(long setLong) {
      if (this.worldEventStartTargetTime != setLong) {
         this.setDirty();
      }

      this.worldEventStartTargetTime = setLong;
      if (setLong > 0L) {
         LycanitesMobs.logDebug("MobEvents", "Next random mob will start after " + (this.worldEventStartTargetTime - this.world.getGameTime()) / 20L + "secs.");
      }
   }

   public void setWorldEventLastStartedTime(long setLong) {
      if (this.worldEventLastStartedTime != setLong) {
         this.setDirty();
      }

      this.worldEventLastStartedTime = setLong;
   }

   public void setWorldEventName(String setString) {
      if (!this.worldEventName.equals(setString)) {
         this.setDirty();
      }

      this.worldEventName = setString;
   }

   public void increaseMobEventCount() {
      this.worldEventCount++;
   }

   public int getRandomEventDelay(RandomSource random) {
      int min = Math.max(200, MobEventManager.getInstance().minTicksUntilEvent);
      int max = Math.max(200, MobEventManager.getInstance().maxTicksUntilEvent);
      return max <= min ? min : min + random.nextInt(max - min);
   }

   public void startWorldEvent(MobEvent mobEvent) {
      if (mobEvent == null) {
         LycanitesMobs.logWarning("", "Tried to start a null world event, stopping any event instead.");
         this.stopWorldEvent();
      } else {
         if (!this.world.isClientSide) {
            boolean extended = false;
            if (this.serverWorldEventPlayer != null) {
               extended = this.serverWorldEventPlayer.mobEvent == mobEvent;
            }

            if (!extended) {
               this.serverWorldEventPlayer = mobEvent.getServerEventPlayer(this.world);
            }

            this.serverWorldEventPlayer.extended = extended;
            this.setWorldEventName(mobEvent.name);
            this.increaseMobEventCount();
            this.setWorldEventStartTargetTime(0L);
            this.setWorldEventLastStartedTime(this.world.getGameTime());
            this.serverWorldEventPlayer.onStart();
            this.updateAllClientsEvents();
         }

         if (this.world.isClientSide) {
            boolean extendedx = false;
            if (this.clientWorldEventPlayer != null) {
               extendedx = this.clientWorldEventPlayer.mobEvent == mobEvent;
            }

            if (!extendedx) {
               this.clientWorldEventPlayer = mobEvent.getClientEventPlayer(this.world);
            }

            this.clientWorldEventPlayer.extended = extendedx;
            if (LycanitesMobs.proxy.getClientPlayer() != null) {
               this.clientWorldEventPlayer.onStart(LycanitesMobs.proxy.getClientPlayer());
            }
         }
      }
   }

   public void stopWorldEvent() {
      if (this.serverWorldEventPlayer != null) {
         this.serverWorldEventPlayer.onFinish();
         this.setWorldEventName("");
         this.serverWorldEventPlayer = null;
         this.updateAllClientsEvents();
      }

      if (this.clientWorldEventPlayer != null) {
         if (LycanitesMobs.proxy.getClientPlayer() != null) {
            this.clientWorldEventPlayer.onFinish(LycanitesMobs.proxy.getClientPlayer());
         }

         this.clientWorldEventPlayer = null;
      }
   }

   public void startMobEvent(MobEvent mobEvent, Player player, BlockPos pos, int level, int variant) {
      if (mobEvent == null) {
         LycanitesMobs.logWarning("", "Tried to start a null mob event.");
      } else {
         if (!this.world.isClientSide) {
            MobEventPlayerServer mobEventPlayerServer = mobEvent.getServerEventPlayer(this.world);
            this.serverMobEventPlayers.put(mobEvent.name, mobEventPlayerServer);
            mobEventPlayerServer.player = player;
            mobEventPlayerServer.origin = pos;
            mobEventPlayerServer.level = level;
            mobEventPlayerServer.variant = variant;
            mobEventPlayerServer.onStart();
            this.updateAllClientsEvents();
         }

         if (this.world.isClientSide) {
            MobEventPlayerClient mobEventPlayerClient;
            if (this.clientMobEventPlayers.containsKey(mobEvent.name)) {
               mobEventPlayerClient = this.clientMobEventPlayers.get(mobEvent.name);
            } else {
               mobEventPlayerClient = mobEvent.getClientEventPlayer(this.world);
               this.clientMobEventPlayers.put(mobEvent.name, mobEventPlayerClient);
            }

            if (LycanitesMobs.proxy.getClientPlayer() != null) {
               mobEventPlayerClient.onStart(LycanitesMobs.proxy.getClientPlayer());
            }
         }
      }
   }

   public MobEvent startMobEvent(String mobEventName, Player player, BlockPos pos, int level, int subspecies) {
      if (MobEventManager.getInstance().mobEvents.containsKey(mobEventName)) {
         MobEvent mobEvent = MobEventManager.getInstance().mobEvents.get(mobEventName);
         if (!mobEvent.isEnabled()) {
            LycanitesMobs.logWarning(
               "", "Tried to start a mob event that was disabled with the name: '" + mobEventName + "' on " + (this.world.isClientSide ? "Client" : "Server")
            );
            return null;
         } else {
            mobEvent.trigger(this.world, player, pos, level, subspecies);
            return mobEvent;
         }
      } else {
         LycanitesMobs.logWarning(
            "", "Tried to start a mob event with the invalid name: '" + mobEventName + "' on " + (this.world.isClientSide ? "Client" : "Server")
         );
         return null;
      }
   }

   public void stopMobEvent(String mobEventName) {
      if (!this.world.isClientSide && this.serverMobEventPlayers.containsKey(mobEventName)) {
         this.serverMobEventPlayers.get(mobEventName).onFinish();
         this.serverMobEventPlayers.remove(mobEventName);
         this.updateAllClientsEvents();
      }

      if (this.world.isClientSide && this.clientMobEventPlayers.containsKey(mobEventName)) {
         if (LycanitesMobs.proxy.getClientPlayer() != null) {
            this.clientMobEventPlayers.get(mobEventName).onFinish(LycanitesMobs.proxy.getClientPlayer());
         }

         this.clientMobEventPlayers.remove(mobEventName);
      }
   }

   public MobEventPlayerServer getMobEventPlayerServer(String mobEventName) {
      if (mobEventName == null || "".equals(mobEventName)) {
         return null;
      } else if (mobEventName.equals(this.getWorldEventName())) {
         return this.serverWorldEventPlayer;
      } else {
         return this.serverMobEventPlayers.containsKey(mobEventName) ? this.serverMobEventPlayers.get(mobEventName) : null;
      }
   }

   public MobEventPlayerClient getMobEventPlayerClient(String mobEventName) {
      if (mobEventName == null || "".equals(mobEventName)) {
         return null;
      } else if (mobEventName.equals(this.worldEventName)) {
         return this.clientWorldEventPlayer;
      } else {
         return this.clientMobEventPlayers.containsKey(mobEventName) ? this.clientMobEventPlayers.get(mobEventName) : null;
      }
   }

   public void bossUpdate(Entity entity) {
      if (entity.isAlive()) {
         if (!this.bosses.containsKey(entity.getUUID())) {
            this.bosses.put(entity.getUUID(), new BossEntry());
         }

         this.bosses.get(entity.getUUID()).update(entity);
      }
   }

   public void overrideBossRange(Entity entity, int rangeOverride) {
      if (this.bosses.containsKey(entity.getUUID())) {
         this.bosses.get(entity.getUUID()).nearbyRange = rangeOverride;
      }
   }

   public void bossRemoved(Entity entity) {
      this.bosses.remove(entity.getUUID());
   }

   public boolean isBossNearby(Vec3 pos) {
      for (BossEntry bossEntry : this.bosses.values()) {
         if (bossEntry != null
            && bossEntry.entity != null
            && bossEntry.entity.isAlive()
            && bossEntry.entity.distanceToSqr(pos.x, pos.y, pos.z)
               <= (double)(bossEntry.nearbyRange * bossEntry.nearbyRange)) {
            return true;
         }
      }

      return false;
   }

   public void updateAllClientsEvents() {
      BlockPos pos = this.serverWorldEventPlayer != null ? this.serverWorldEventPlayer.origin : LycPos.of(0, 0, 0);
      int level = this.serverWorldEventPlayer != null ? this.serverWorldEventPlayer.level : 0;
      int subspecies = this.serverWorldEventPlayer != null ? this.serverWorldEventPlayer.variant : -1;
      MessageWorldEvent message = new MessageWorldEvent(this.getWorldEventName(), pos, level, subspecies);
      LycanitesMobs.packetHandler.sendToDimension(message, LycDim.id(this.world));

      for (MobEventPlayerServer mobEventPlayerServer : this.serverMobEventPlayers.values()) {
         MessageMobEvent messageMobEvent = new MessageMobEvent(
            mobEventPlayerServer.mobEvent != null ? mobEventPlayerServer.mobEvent.name : "",
            mobEventPlayerServer.origin,
            mobEventPlayerServer.level,
            mobEventPlayerServer.variant
         );
         LycanitesMobs.packetHandler.sendToDimension(messageMobEvent, LycDim.id(this.world));
      }
   }

   public void addDungeonInstance(DungeonInstance dungeonInstance, UUID uuid) {
      dungeonInstance.uuid = uuid;
      this.dungeons.put(dungeonInstance.uuid, dungeonInstance);
      this.setDirty();
   }

   public List<DungeonInstance> getNearbyDungeonInstances(ChunkPos chunkPos, int range) {
      List<DungeonInstance> nearbyDungeons = new ArrayList<>();

      for (DungeonInstance dungeonInstance : this.dungeons.values()) {
         if (dungeonInstance.isChunkPosWithin(chunkPos, range)) {
            nearbyDungeons.add(dungeonInstance);
         }
      }

      return nearbyDungeons;
   }

   public void readFromNBT(CompoundTag nbtTagCompound) {
      if (nbtTagCompound.contains("WorldEventStartTargetTime")) {
         this.worldEventStartTargetTime = nbtTagCompound.getLong("WorldEventStartTargetTime");
      }

      if (nbtTagCompound.contains("WorldEventLastStartedTime")) {
         this.worldEventLastStartedTime = nbtTagCompound.getLong("WorldEventLastStartedTime");
      }

      if (nbtTagCompound.contains("WorldEventName")) {
         this.worldEventName = nbtTagCompound.getString("WorldEventName");
      }

      if (nbtTagCompound.contains("WorldEventCount")) {
         this.worldEventCount = nbtTagCompound.getInt("WorldEventCount");
      }

      if (nbtTagCompound.contains("Dungeons")) {
         // 1.12 DungeonInstance list: dungeons are placed and saved by rlgen (lyc/dungeon); keep the tag as read
         this.dungeonsTag = nbtTagCompound.getList("Dungeons", 10);
      }
   }

   private ListTag dungeonsTag = new ListTag();

   public CompoundTag writeToNBT(CompoundTag nbtTagCompound) {
      nbtTagCompound.putLong("WorldEventStartTargetTime", this.worldEventStartTargetTime);
      nbtTagCompound.putLong("WorldEventLastStartedTime", this.worldEventLastStartedTime);
      nbtTagCompound.putString("WorldEventName", this.worldEventName);
      nbtTagCompound.putInt("WorldEventCount", this.worldEventCount);
      nbtTagCompound.put("Dungeons", this.dungeonsTag);
      return nbtTagCompound;
   }
}
