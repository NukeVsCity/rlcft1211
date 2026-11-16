package com.lycanitesmobs.core.mobevent;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.Utilities;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.JSONLoader;
import com.lycanitesmobs.core.config.ConfigSpawning;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.spawner.condition.SpawnCondition;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MobEventManager extends JSONLoader {
   protected static MobEventManager INSTANCE;
   public Map<String, MobEvent> mobEvents = new HashMap<>();
   public List<MobEventSchedule> mobEventSchedules = new ArrayList<>();
   public List<SpawnCondition> globalEventConditions = new ArrayList<>();
   public boolean mobEventsEnabled = true;
   public boolean mobEventsRandom = false;
   public int defaultMobDuration = 12000;
   public int minEventsRandomDay = 0;
   public int minTicksUntilEvent = 72000;
   public int maxTicksUntilEvent = 144000;

   public static MobEventManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new MobEventManager();
      }

      return INSTANCE;
   }

   public void loadConfig() {
      ConfigSpawning config = ConfigSpawning.getConfig(LycanitesMobs.modInfo, "mobevents");
      config.setCategoryComment("Global", "These are various settings that apply to all events.");
      this.mobEventsEnabled = config.getBool(
         "Global", "Mob Events Enabled", this.mobEventsEnabled, "Set to false to completely disable the entire event system for every world."
      );
      this.mobEventsRandom = config.getBool(
         "Global", "Random Mob Events Enabled", this.mobEventsRandom, "Set to false to disable random mob events for every world."
      );
      this.defaultMobDuration = config.getInt(
         "Global",
         "Default Mob Duration",
         this.defaultMobDuration,
         "The default temporary time applied to mobs spawned from events, where it will forcefully despawn after the specified time (in ticks). MobSpawns can override this."
      );
      this.minEventsRandomDay = config.getInt(
         "Global",
         "Default RandomSource Mob Events Day Minimum",
         this.minEventsRandomDay,
         "If random events are enabled, they wont occur until this day is reached. Set to 0 to have random events enabled from the start of a world."
      );
      this.minTicksUntilEvent = config.getInt(
         "Global",
         "Default Min Ticks Until RandomSource Event",
         this.minTicksUntilEvent,
         "Minimum time in ticks until a random event can occur. 20 Ticks = 1 Second."
      );
      this.maxTicksUntilEvent = config.getInt(
         "Global",
         "Default Max Ticks Until RandomSource Event",
         this.maxTicksUntilEvent,
         "Maximum time in ticks until a random event can occur. 20 Ticks = 1 Second."
      );
   }

   public void loadAllFromJSON(ModInfo groupInfo) {
      LycanitesMobs.logDebug("MobEvents", "Loading JSON Mob Events!");
      Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
      Map<String, JsonObject> mobEventJSONs = new HashMap<>();
      Path path = Utilities.getAssetPath(groupInfo.getClass(), groupInfo.modid, "mobevents");
      Map<String, JsonObject> defaultMobEventJSONs = new HashMap<>();
      this.loadJsonObjects(gson, path, defaultMobEventJSONs, "name", "event");
      String configPath = LycanitesMobs.proxy.getMinecraftDir() + "/config/" + "lycanitesmobs" + "/";
      File customDir = new File(configPath + "mobevents");
      customDir.mkdirs();
      path = customDir.toPath();
      Map<String, JsonObject> customMobEventJSONs = new HashMap<>();
      this.loadJsonObjects(gson, path, customMobEventJSONs, "name", "event");
      this.writeDefaultJSONObjects(gson, defaultMobEventJSONs, customMobEventJSONs, mobEventJSONs, true, "mobevents");
      LycanitesMobs.logDebug("MobEvents", "Loading " + mobEventJSONs.size() + " Mob Events...");

      for (String spawnerJSONName : mobEventJSONs.keySet()) {
         try {
            JsonObject spawnerJSON = mobEventJSONs.get(spawnerJSONName);
            LycanitesMobs.logDebug("MobEvents", "Loading Mob Event JSON: " + spawnerJSON);
            MobEvent mobEvent = new MobEvent();
            mobEvent.loadFromJSON(spawnerJSON);
            this.addMobEvent(mobEvent);
         } catch (JsonParseException var23) {
            LycanitesMobs.logWarning("", "Parsing error loading JSON Mob Event: " + spawnerJSONName);
            var23.printStackTrace();
         } catch (Exception var24) {
            LycanitesMobs.logWarning("", "There was a problem loading JSON Mob Event: " + spawnerJSONName);
            var24.printStackTrace();
         }
      }

      LycanitesMobs.logDebug("MobEvents", "Complete! " + this.mobEvents.size() + " JSON Mob Events Loaded In Total.");
      this.mobEventSchedules.clear();
      Path defaultSchedulePath = Utilities.getAssetPath(groupInfo.getClass(), groupInfo.modid, "mobeventschedule.json");
      JsonObject defaultScheduleJson = this.loadJsonObject(gson, defaultSchedulePath);
      File customScheduleFile = new File(configPath + "mobeventschedule.json");
      JsonObject customScheduleJson = null;
      if (customScheduleFile.exists()) {
         customScheduleJson = this.loadJsonObject(gson, customScheduleFile.toPath());
      }

      JsonObject scheduleJson = this.writeDefaultJSONObject(gson, "mobeventschedule", defaultScheduleJson, customScheduleJson);
      if (scheduleJson.has("schedules")) {
         JsonArray jsonArray = scheduleJson.get("schedules").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject scheduleEntryJson = jsonIterator.next().getAsJsonObject();
            MobEventSchedule mobEventSchedule = MobEventSchedule.createFromJSON(scheduleEntryJson);
            this.mobEventSchedules.add(mobEventSchedule);
         }
      }

      if (this.mobEventSchedules.size() > 0) {
         LycanitesMobs.logDebug("MobEvents", "Loaded " + this.mobEventSchedules.size() + " Mob Event Schedules.");
      }

      this.globalEventConditions.clear();
      Path defaultGlobalPath = Utilities.getAssetPath(this.getClass(), LycanitesMobs.modInfo.modid, "globalmobevent.json");
      JsonObject defaultGlobalJson = this.loadJsonObject(gson, defaultGlobalPath);
      File customGlobalFile = new File(configPath + "globalmobevent.json");
      JsonObject customGlobalJson = null;
      if (customGlobalFile.exists()) {
         customGlobalJson = this.loadJsonObject(gson, customGlobalFile.toPath());
      }

      JsonObject globalJson = this.writeDefaultJSONObject(gson, "globalmobevent", defaultGlobalJson, customGlobalJson);
      if (globalJson.has("conditions")) {
         JsonArray jsonArray = globalJson.get("conditions").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject spawnConditionJson = jsonIterator.next().getAsJsonObject();
            SpawnCondition spawnCondition = SpawnCondition.createFromJSON(spawnConditionJson);
            this.globalEventConditions.add(spawnCondition);
         }
      }

      if (this.globalEventConditions.size() > 0) {
         LycanitesMobs.logDebug("JSONSpawner", "Loaded " + this.globalEventConditions.size() + " Global Mob Event Conditions.");
      }
   }

   @Override
   public void parseJson(ModInfo groupInfo, String name, JsonObject json) {
   }

   public void reload() {
      LycanitesMobs.logDebug("MobEvents", "Destroying JSON Mob Events!");

      for (MobEvent mobEvent : this.mobEvents.values().toArray(new MobEvent[this.mobEvents.size()])) {
         mobEvent.destroy();
      }

      this.loadAllFromJSON(LycanitesMobs.modInfo);
   }

   public void addMobEvent(MobEvent mobEvent) {
      if (mobEvent != null) {
         this.mobEvents.put(mobEvent.name, mobEvent);

         try {
            AssetManager.addSound("mobevent_" + mobEvent.title.toLowerCase(), LycanitesMobs.modInfo, "mobevent." + mobEvent.title.toLowerCase());
         } catch (Exception var3) {
         }
      }
   }

   public void removeMobEvent(MobEvent mobEvent) {
      if (!this.mobEvents.containsKey(mobEvent.name)) {
         LycanitesMobs.logWarning("", "[MobEvents] Tried to remove a Mob Event that hasn't been added: " + mobEvent.name);
      } else {
         this.mobEvents.remove(mobEvent.name);
      }
   }

   public MobEvent getMobEvent(String mobEventName) {
      return !this.mobEvents.containsKey(mobEventName) ? null : this.mobEvents.get(mobEventName);
   }

   @SubscribeEvent
   public void onWorldUpdate(WorldTickEvent event) {
      Level world = event.world;
      if (!world.isClientSide) {
         ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
         if (worldExt != null) {
            if (worldExt.lastEventUpdateTime != world.getGameTime()) {
               worldExt.lastEventUpdateTime = world.getGameTime();
               if (worldExt.serverWorldEventPlayer != null) {
                  worldExt.serverWorldEventPlayer.onUpdate();
               }

               if (worldExt.serverMobEventPlayers.size() > 0) {
                  for (MobEventPlayerServer mobEventPlayerServer : worldExt.serverMobEventPlayers
                     .values()
                     .toArray(new MobEventPlayerServer[worldExt.serverMobEventPlayers.size()])) {
                     mobEventPlayerServer.onUpdate();
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onClientUpdate(ClientTickEvent event) {
      if (LycanitesMobs.proxy.getClientPlayer() != null) {
         Level world = LycanitesMobs.proxy.getClientPlayer().level();
         if (world.isClientSide) {
            ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
            if (worldExt != null) {
               for (MobEventPlayerClient mobEventPlayerClient : worldExt.clientMobEventPlayers.values()) {
                  mobEventPlayerClient.onUpdate();
               }

               if (worldExt.clientWorldEventPlayer != null) {
                  worldExt.clientWorldEventPlayer.onUpdate();
               }
            }
         }
      }
   }
}
