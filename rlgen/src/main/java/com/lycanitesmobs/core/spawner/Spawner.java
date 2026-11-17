package com.lycanitesmobs.core.spawner;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.mobevent.MobEvent;
import com.lycanitesmobs.core.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.spawner.condition.SpawnCondition;
import com.lycanitesmobs.core.spawner.location.SpawnLocation;
import com.lycanitesmobs.core.spawner.trigger.ChunkSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.SpawnTrigger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Spawner {
   public List<SpawnCondition> conditions = new ArrayList<>();
   public List<SpawnTrigger> triggers = new ArrayList<>();
   public List<SpawnLocation> locations = new ArrayList<>();
   public List<MobSpawn> mobSpawns = new ArrayList<>();
   public String name;
   public String sharedName;
   public boolean loadDefault = true;
   public boolean enabled = true;
   public int conditionsRequired = 0;
   public int triggersRequired = 1;
   public Map<Integer, String> triggerCountMessages = new HashMap<>();
   public String multipleLocations = "combine";
   public int mobCountMin = 1;
   public int mobCountMax = 1;
   public boolean mobCountPlayerScaling = true;
   public int mobCountPlayerRange = 64;
   public boolean ignoreDimensions = false;
   public boolean ignoreBiomes = false;
   public boolean ignoreCollision = false;
   public boolean ignoreLightLevel = false;
   public boolean ignoreGroupLimit = false;
   protected double groupLimitRange = 32.0;
   protected boolean ignoreForgeCanSpawnEvent = false;
   public boolean forceNoDespawn = false;
   public boolean chainSpawning = true;
   public boolean enableWithoutMobs = false;
   public String eventName = "";
   public int blockBreakRadius = -1;
   protected boolean dropsRequirePlayerDamage = false;
   protected Map<Player, Integer> triggerCounts = new HashMap<>();

   public void loadFromJSON(JsonObject json) {
      this.name = json.get("name").getAsString();
      this.sharedName = this.name;
      if (json.has("sharedName")) {
         this.sharedName = json.get("sharedName").getAsString();
      }

      if (json.has("loadDefault")) {
         this.loadDefault = json.get("loadDefault").getAsBoolean();
      }

      if (json.has("enabled")) {
         this.enabled = json.get("enabled").getAsBoolean();
      }

      if (json.has("conditionsRequired")) {
         this.conditionsRequired = json.get("conditionsRequired").getAsInt();
      }

      if (json.has("triggersRequired")) {
         this.triggersRequired = json.get("triggersRequired").getAsInt();
      }

      if (json.has("triggerCountMessages")) {
         JsonArray jsonArray = json.get("triggerCountMessages").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject tcmJson = jsonIterator.next().getAsJsonObject();
            this.triggerCountMessages.put(tcmJson.get("count").getAsInt(), tcmJson.get("message").getAsString());
         }
      }

      if (json.has("multipleLocations")) {
         this.multipleLocations = json.get("multipleLocations").getAsString();
      }

      if (json.has("mobCountMin")) {
         this.mobCountMin = json.get("mobCountMin").getAsInt();
      }

      if (json.has("mobCountMax")) {
         this.mobCountMax = json.get("mobCountMax").getAsInt();
      }

      if (json.has("mobCountPlayerScaling")) {
         this.mobCountPlayerScaling = json.get("mobCountPlayerScaling").getAsBoolean();
      }

      if (json.has("mobCountPlayerRange")) {
         this.mobCountPlayerRange = json.get("mobCountPlayerRange").getAsInt();
      }

      if (json.has("ignoreDimensions")) {
         this.ignoreDimensions = json.get("ignoreDimensions").getAsBoolean();
      }

      if (json.has("ignoreBiomes")) {
         this.ignoreBiomes = json.get("ignoreBiomes").getAsBoolean();
      }

      if (json.has("ignoreCollision")) {
         this.ignoreCollision = json.get("ignoreCollision").getAsBoolean();
      }

      if (json.has("ignoreLightLevel")) {
         this.ignoreLightLevel = json.get("ignoreLightLevel").getAsBoolean();
      }

      if (json.has("ignoreGroupLimit")) {
         this.ignoreGroupLimit = json.get("ignoreGroupLimit").getAsBoolean();
      }

      if (json.has("groupLimitRange")) {
         this.groupLimitRange = json.get("groupLimitRange").getAsDouble();
      }

      if (json.has("ignoreForgeCanSpawnEvent")) {
         this.ignoreForgeCanSpawnEvent = json.get("ignoreForgeCanSpawnEvent").getAsBoolean();
      }

      if (json.has("forceNoDespawn")) {
         this.forceNoDespawn = json.get("forceNoDespawn").getAsBoolean();
      }

      if (json.has("chainSpawning")) {
         this.chainSpawning = json.get("chainSpawning").getAsBoolean();
      }

      if (json.has("enableWithoutMobs")) {
         this.enableWithoutMobs = json.get("enableWithoutMobs").getAsBoolean();
      }

      if (json.has("eventName")) {
         this.eventName = json.get("eventName").getAsString();
      }

      if (json.has("blockBreakRadius")) {
         this.blockBreakRadius = json.get("blockBreakRadius").getAsInt();
      }

      if (json.has("dropsRequirePlayerDamage")) {
         this.dropsRequirePlayerDamage = json.get("dropsRequirePlayerDamage").getAsBoolean();
      }

      if (json.has("conditions")) {
         JsonArray jsonArray = json.get("conditions").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject conditionJson = jsonIterator.next().getAsJsonObject();
            SpawnCondition spawnCondition = SpawnCondition.createFromJSON(conditionJson);
            this.conditions.add(spawnCondition);
         }
      }

      if (json.has("triggers")) {
         JsonArray jsonArray = json.get("triggers").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject triggerJson = jsonIterator.next().getAsJsonObject();
            SpawnTrigger spawnTrigger = SpawnTrigger.createFromJSON(triggerJson, this);
            this.triggers.add(spawnTrigger);
            if (this.enabled) {
               SpawnerEventListener.getInstance().addTrigger(spawnTrigger);
            }
         }
      }

      if (json.has("locations")) {
         JsonArray jsonArray = json.get("locations").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject locationJson = jsonIterator.next().getAsJsonObject();
            SpawnLocation spawnLocation = SpawnLocation.createFromJSON(locationJson);
            this.locations.add(spawnLocation);
         }
      }

      if (json.has("mobSpawns")) {
         JsonArray jsonArray = json.get("mobSpawns").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject mobSpawnJson = jsonIterator.next().getAsJsonObject();
            MobSpawn mobSpawn = MobSpawn.createFromJSON(mobSpawnJson);
            if (mobSpawn != null) {
               this.mobSpawns.add(mobSpawn);
            }
         }
      }
   }

   public void destroy() {
      for (SpawnTrigger spawnTrigger : this.triggers) {
         SpawnerEventListener.getInstance().removeTrigger(spawnTrigger);
      }

      SpawnerManager.getInstance().removeSpawner(this);
   }

   public boolean isEnabled(Level world, Player player) {
      if (this.enabled && !CreatureManager.getInstance().spawnConfig.disableAllSpawning && world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
         if (!"".equals(this.eventName)) {
            ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
            if (worldExt == null) {
               return false;
            }

            if (worldExt.getMobEventPlayerServer(this.eventName) == null) {
               return false;
            }
         }

         return this.enableWithoutMobs || !this.mobSpawns.isEmpty() || SpawnerMobRegistry.getMobSpawns(this.sharedName) != null;
      } else {
         return false;
      }
   }

   public boolean canSpawn(Level world, Player player, BlockPos triggerPos) {
      if (!SpawnerManager.getInstance().globalSpawnConditions.isEmpty()) {
         LycanitesMobs.logDebug("JSONSpawner", "Global Conditions Required: " + SpawnerManager.getInstance().globalSpawnConditions.size());

         for (SpawnCondition condition : SpawnerManager.getInstance().globalSpawnConditions) {
            if (!condition.isMet(world, player, triggerPos)) {
               LycanitesMobs.logDebug("JSONSpawner", "Global Condition: " + condition + "Failed");
               return false;
            }
         }
      }

      if (this.conditions.isEmpty()) {
         LycanitesMobs.logDebug("JSONSpawner", "No Conditions");
         return true;
      } else {
         LycanitesMobs.logDebug("JSONSpawner", "Conditions Required: " + (this.conditionsRequired > 0 ? this.conditionsRequired : "All"));
         int conditionsMet = 0;
         int conditionsRequired = this.conditionsRequired > 0 ? this.conditionsRequired : this.conditions.size();

         for (SpawnCondition conditionx : this.conditions) {
            boolean met = conditionx.isMet(world, player, triggerPos);
            LycanitesMobs.logDebug("JSONSpawner", "Condition: " + conditionx + " " + (met ? "Passed" : "Failed"));
            if (met) {
               if (++conditionsMet >= conditionsRequired) {
                  LycanitesMobs.logDebug("JSONSpawner", "Sufficient Conditions Met");
                  return true;
               }
            }
         }

         LycanitesMobs.logDebug("JSONSpawner", "Insufficient Conditions Met: " + conditionsMet + "/" + conditionsRequired);
         return false;
      }
   }

   public boolean trigger(Level world, Player player, SpawnTrigger spawnTrigger, BlockPos triggerPos, int level, int countAmount, int chain) {
      if (!this.isEnabled(world, player)) {
         return false;
      } else {
         LycanitesMobs.logDebug("JSONSpawner", "~O==================== Spawner Triggered: " + this.name + " ====================O~");
         if (!this.canSpawn(world, player, triggerPos)) {
            LycanitesMobs.logDebug("JSONSpawner", "This Spawner Cannot Spawn");
            return false;
         } else if (this.triggersRequired > 1 && player != null) {
            if (!this.triggerCounts.containsKey(player)) {
               this.triggerCounts.put(player, Math.max(countAmount, 0));
            }

            int currentCount = this.triggerCounts.get(player);
            if (countAmount == 0) {
               currentCount = 0;
            } else {
               currentCount += countAmount;
            }

            if (currentCount != currentCount && this.triggerCountMessages.containsKey(currentCount)) {
               String message = LanguageManager.translate(this.triggerCountMessages.get(currentCount));
               player.displayClientMessage(Component.literal(message), true);
            }

            LycanitesMobs.logDebug("JSONSpawner", "Trigger " + currentCount + "/" + this.triggersRequired);
            if (currentCount >= this.triggersRequired) {
               this.triggerCounts.put(player, 0);
               return this.doSpawn(world, player, spawnTrigger, triggerPos, level, chain);
            } else {
               this.triggerCounts.put(player, currentCount);
               return false;
            }
         } else {
            LycanitesMobs.logDebug("JSONSpawner", "Only one trigger required.");
            return this.doSpawn(world, player, spawnTrigger, triggerPos, level, chain);
         }
      }
   }

   public boolean doSpawn(Level world, Player player, SpawnTrigger spawnTrigger, BlockPos triggerPos, int level, int chain) {
      int mobCount = this.mobCountMin;
      if (this.mobCountMax > this.mobCountMin) {
         mobCount = world.random.nextInt(this.mobCountMax - this.mobCountMin) + this.mobCountMin;
      }

      if (player != null && this.mobCountPlayerScaling) {
         List<Player> nearbyPlayers = LycWorld.getPlayers(world, 
            Player.class,
            targetPlayer -> (targetPlayer.isCreative() || targetPlayer.isSpectator()) && !SpawnerEventListener.testOnCreative
                  ? false
                  : !(targetPlayer.distanceTo(player) > (float)this.mobCountPlayerRange)
         );
         LycanitesMobs.logDebug("JSONSpawner", "Nearby Players: " + nearbyPlayers.size());
         if (!nearbyPlayers.isEmpty()) {
            mobCount = (int)Math.ceil((double)mobCount / (double)nearbyPlayers.size());
         }
      }

      ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
      LycanitesMobs.logDebug("JSONSpawner", "Spawning Wave: " + mobCount + " Mob(s)");
      LycanitesMobs.logDebug("JSONSpawner", "Trigger Level: " + world);
      LycanitesMobs.logDebug("JSONSpawner", "Trigger Player: " + player);
      LycanitesMobs.logDebug("JSONSpawner", "Trigger Position: " + triggerPos);
      List<BlockPos> spawnPositions = this.getSpawnPos(world, player, triggerPos);
      if (spawnTrigger instanceof ChunkSpawnTrigger) {
         for (BlockPos spawnPos : spawnPositions.toArray(new BlockPos[spawnPositions.size()])) {
            int chunkOffset = 8;
            if (spawnPos.getX() < triggerPos.getX() + chunkOffset || spawnPos.getX() > triggerPos.getX() + chunkOffset) {
               LycanitesMobs.logDebug("JSONSpawner", "A spawn position exceeds chunk x boundaries and will not be used for worldgen triggered spawning.");
               spawnPositions.remove(spawnPos);
            } else if (spawnPos.getZ() < triggerPos.getZ() + chunkOffset
               || spawnPos.getZ() > triggerPos.getZ() + chunkOffset) {
               LycanitesMobs.logDebug("JSONSpawner", "A spawn position exceeds chunk z boundaries and will not be used for worldgen triggered spawning.");
               spawnPositions.remove(spawnPos);
            }
         }
      }

      if (spawnPositions.isEmpty()) {
         LycanitesMobs.logDebug("JSONSpawner", "No Spawn Positions Found From Spawn Location");
         return false;
      } else {
         Collections.shuffle(spawnPositions);
         LycanitesMobs.logDebug("JSONSpawner", "Positions Found: " + spawnPositions.size());
         List<ResourceLocation> biomes = null;
         if (!this.ignoreBiomes) {
            biomes = new ArrayList<>();

            for (BlockPos spawnPosx : spawnPositions) {
               ResourceLocation biome = LycBiomes.at(world, spawnPosx);
               if (!biomes.contains(biome)) {
                  biomes.add(biome);
               }
            }

            LycanitesMobs.logDebug("JSONSpawner", "Biomes Found: " + biomes.size());
         } else {
            LycanitesMobs.logDebug("JSONSpawner", "All biome checks are ignored by this Spawner.");
         }

         Map<ResourceLocation, List<MobSpawn>> mobSpawns = this.getMobSpawns(world, player, spawnPositions.size(), biomes);
         LycanitesMobs.logDebug("JSONSpawner", "Spawnable Mobs Found: " + mobSpawns.size());
         if (mobSpawns.size() == 0) {
            return false;
         } else {
            int mobsSpawned = 0;

            for (BlockPos spawnPosxx : spawnPositions) {
               if (mobsSpawned >= mobCount) {
                  break;
               }

               LycanitesMobs.logDebug("JSONSpawner", "---------- Spawn Iteration: " + mobsSpawned + " ----------");
               LycanitesMobs.logDebug("JSONSpawner", "Spawn Position: " + spawnPosxx);
               MobSpawn mobSpawn = null;
               ResourceLocation spawnBiome = LycBiomes.at(world, spawnPosxx);
               if (mobSpawns.containsKey(null)) {
                  mobSpawn = this.chooseMobToSpawn(world, mobSpawns.get(null));
               } else if (mobSpawns.containsKey(spawnBiome)) {
                  mobSpawn = this.chooseMobToSpawn(world, mobSpawns.get(spawnBiome));
               }

               if (mobSpawn == null) {
                  LycanitesMobs.logDebug("JSONSpawner", "No Mob Spawn Chosen");
               } else {
                  LycanitesMobs.logDebug("JSONSpawner", "Spawn Mob: " + mobSpawn);
                  Mob entityLiving = mobSpawn.createEntity(world);
                  if (entityLiving == null) {
                     LycanitesMobs.logWarning("JSONSpawner", "Unable to instantiate an entity. Class: " + mobSpawn.entityClass);
                  } else {
                     BaseCreatureEntity entityCreature = null;
                     if (entityLiving instanceof BaseCreatureEntity) {
                        entityCreature = (BaseCreatureEntity)entityLiving;
                        LycanitesMobs.logDebug("JSONSpawner", "Mob is a Lycanites Mob");
                     } else {
                        LycanitesMobs.logDebug("JSONSpawner", "Mob is not a Lycanites Mob");
                     }

                     LycanitesMobs.logDebug("JSONSpawner", "Attempting to spawn " + entityLiving + "...");
                     entityLiving.moveTo(
                        (double)spawnPosxx.getX() + 0.5,
                        (double)spawnPosxx.getY(),
                        (double)spawnPosxx.getZ() + 0.5,
                        world.random.nextFloat() * 360.0F,
                        0.0F
                     );
                     // 1.12 ForgeEventFactory.canEntitySpawn (LivingSpawnEvent.CheckSpawn): 1.21 MobSpawnEvent.PositionCheck
                     net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck checkEvent =
                        new net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck(entityLiving, (ServerLevel)world, MobSpawnType.NATURAL, null);
                     net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(checkEvent);
                     net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck.Result canSpawn = checkEvent.getResult();
                     if (canSpawn == net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck.Result.FAIL && !this.ignoreForgeCanSpawnEvent && !mobSpawn.ignoreForgeCanSpawnEvent) {
                        LycanitesMobs.logDebug("JSONSpawner", "Spawn Check Failed! Spawning blocked by Forge Can Spawn Event, this is caused another mod.");
                     } else if (!this.mobInstanceSpawnCheck(entityLiving, mobSpawn, world, player, spawnPosxx, level, canSpawn == net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck.Result.SUCCEED)) {
                        LycanitesMobs.logDebug("JSONSpawner", "Mob Instance Spawn Check Failed!");
                     } else {
                        this.spawnEntity(world, worldExt, entityLiving, level, mobSpawn, player, chain);
                        // 1.12 ForgeEventFactory.doSpecialSpawn then onInitialSpawn for Lycanites mobs (after spawnEntity, like 1.12)
                        if (entityCreature != null) {
                           net.neoforged.neoforge.event.EventHooks.finalizeMobSpawn(entityLiving, (ServerLevel)world, world.getCurrentDifficultyAt(spawnPosxx), MobSpawnType.NATURAL, null);
                        }

                        LycanitesMobs.logDebug("JSONSpawner", "Spawn Checks Passed! Mob Spawned!");
                        mobsSpawned++;
                     }
                  }
               }
            }

            return mobsSpawned > 0;
         }
      }
   }

   public List<BlockPos> getSpawnPos(Level world, Player player, BlockPos triggerPos) {
      LycanitesMobs.logDebug("JSONSpawner", "Searching for Spawn Positions...");
      List<BlockPos> spawnPositions = new ArrayList<>();
      if (this.locations.isEmpty()) {
         LycanitesMobs.logDebug("JSONSpawner", "No Spawn Locations, Using Trigger Position");
         spawnPositions.add(triggerPos);
         return spawnPositions;
      } else if (this.locations.size() == 1) {
         LycanitesMobs.logDebug("JSONSpawner", "Only One Spawn Location Type");
         return this.locations.get(0).getSpawnPositions(world, player, triggerPos);
      } else {
         LycanitesMobs.logDebug("JSONSpawner", "Multiple Spawn Location Types, Mode: " + this.multipleLocations);
         if ("order".equals(this.multipleLocations)) {
            for (SpawnLocation location : this.locations) {
               spawnPositions = location.getSpawnPositions(world, player, triggerPos);
               if (!spawnPositions.isEmpty()) {
                  return spawnPositions;
               }
            }
         }

         if ("random".equals(this.multipleLocations)) {
            List<List<BlockPos>> possibleSpawnPosLists = new ArrayList<>();

            for (SpawnLocation locationx : this.locations) {
               spawnPositions = locationx.getSpawnPositions(world, player, triggerPos);
               if (!spawnPositions.isEmpty()) {
                  possibleSpawnPosLists.add(spawnPositions);
               }
            }

            if (possibleSpawnPosLists.isEmpty()) {
               return spawnPositions;
            } else if (possibleSpawnPosLists.size() == 1) {
               return possibleSpawnPosLists.get(0);
            } else {
               int randomIndex = world.random.nextInt(possibleSpawnPosLists.size());
               return possibleSpawnPosLists.get(randomIndex);
            }
         } else if (!"combine".equals(this.multipleLocations)) {
            return spawnPositions;
         } else {
            for (SpawnLocation locationxx : this.locations) {
               spawnPositions.addAll(locationxx.getSpawnPositions(world, player, triggerPos));
            }

            return spawnPositions;
         }
      }
   }

   public Map<ResourceLocation, List<MobSpawn>> getMobSpawns(Level world, Player player, int blockCount, List<ResourceLocation> biomes) {
      Map<ResourceLocation, List<MobSpawn>> mobSpawns = new HashMap<>();
      if (biomes == null) {
         mobSpawns.put(null, this.getBiomeSpawns(world, player, blockCount, null));
         return mobSpawns;
      } else {
         for (ResourceLocation biome : biomes) {
            mobSpawns.put(biome, this.getBiomeSpawns(world, player, blockCount, biome));
         }

         return mobSpawns;
      }
   }

   public List<MobSpawn> getBiomeSpawns(Level world, Player player, int blockCount, ResourceLocation biome) {
      List<MobSpawn> biomeSpawns = new ArrayList<>();
      Collection<MobSpawn> globalSpawns = SpawnerMobRegistry.getMobSpawns(this.sharedName);
      if (globalSpawns != null) {
         biomeSpawns.addAll(globalSpawns);
      }

      biomeSpawns.addAll(this.mobSpawns);
      List<MobSpawn> viableMobSpawns = new ArrayList<>();

      for (MobSpawn possibleMobSpawn : biomeSpawns) {
         if (possibleMobSpawn.canSpawn(world, blockCount, biome, this.ignoreDimensions)) {
            viableMobSpawns.add(possibleMobSpawn);
         }
      }

      return viableMobSpawns;
   }

   public MobSpawn chooseMobToSpawn(Level world, List<MobSpawn> mobSpawns) {
      int totalWeights = 0;

      for (MobSpawn mobSpawn : mobSpawns) {
         totalWeights += mobSpawn.getWeight();
      }

      if (totalWeights <= 0) {
         return null;
      } else {
         int randomWeight = 1;
         if (totalWeights > 1) {
            randomWeight = world.random.nextInt(totalWeights);
         }

         int searchWeight = 0;
         MobSpawn chosenMobSpawn = null;

         for (MobSpawn mobSpawn : mobSpawns) {
            chosenMobSpawn = mobSpawn;
            if (mobSpawn.getWeight() + searchWeight > randomWeight) {
               break;
            }

            searchWeight += mobSpawn.getWeight();
         }

         return chosenMobSpawn;
      }
   }

   public boolean mobInstanceSpawnCheck(
      Mob entityLiving, MobSpawn mobSpawn, Level world, Player player, BlockPos spawnPos, int level, boolean forgeForced
   ) {
      if (entityLiving instanceof BaseCreatureEntity) {
         BaseCreatureEntity entityCreature = (BaseCreatureEntity)entityLiving;
         LycanitesMobs.logDebug("JSONSpawner", "Checking Mob Collision...");
         if (!this.ignoreCollision && !entityCreature.checkSpawnCollision(world, spawnPos)) {
            return false;
         } else {
            LycanitesMobs.logDebug("JSONSpawner", "Checking For Nearby Boss...");
            if (!entityCreature.checkSpawnBoss(world, spawnPos)) {
               return false;
            } else if (mobSpawn.ignoreMobInstanceConditions) {
               LycanitesMobs.logDebug("JSONSpawner", "All Mob Instance Checks Ignored");
               return true;
            } else {
               if (!this.ignoreLightLevel && !mobSpawn.ignoreLightLevel) {
                  LycanitesMobs.logDebug("JSONSpawner", "Checking Light Level...");
                  if (!entityCreature.checkSpawnLightLevel(world, spawnPos)) {
                     return false;
                  }
               }

               if (!this.ignoreGroupLimit && !mobSpawn.ignoreGroupLimit) {
                  LycanitesMobs.logDebug("JSONSpawner", "Checking Group Limit...");
                  if (!entityCreature.checkSpawnGroupLimit(world, spawnPos, this.groupLimitRange)) {
                     return false;
                  }
               }

               return true;
            }
         }
      } else if (!mobSpawn.ignoreMobInstanceConditions) {
         LycanitesMobs.logDebug("JSONSpawner", "None Lycanites Mobs Checks...");
         return entityLiving.checkSpawnRules(world, MobSpawnType.NATURAL) && entityLiving.checkSpawnObstruction(world); // 1.12 getCanSpawnHere
      } else {
         LycanitesMobs.logDebug("JSONSpawner", "All Mob Instance Checks Ignored");
         return true;
      }
   }

   public void spawnEntity(Level world, ExtendedWorld worldExt, Mob entityLiving, int level, MobSpawn mobSpawn, Player player, int chain) {
      entityLiving.setPortalCooldown();
      if (entityLiving instanceof BaseCreatureEntity) {
         BaseCreatureEntity entityCreature = (BaseCreatureEntity)entityLiving;
         entityCreature.forceNoDespawn = this.forceNoDespawn;
         entityCreature.spawnedRare = level > 0;
         if (this.blockBreakRadius > -1 && chain == 0) {
            BlockPos creaturePos = entityLiving.blockPosition();
            entityCreature.destroyArea(
               creaturePos.getX(),
               creaturePos.getY() - 1,
               creaturePos.getZ(),
               100.0F,
               true,
               this.blockBreakRadius,
               this.chainSpawning ? player : null,
               chain + 1
            );
         }

         entityCreature.setDropsRequirePlayerDamage(this.dropsRequirePlayerDamage);
         if (!"".equals(this.eventName) && worldExt != null) {
            MobEventPlayerServer mobEventPlayerServer = worldExt.getMobEventPlayerServer(this.eventName);
            if (mobEventPlayerServer != null) {
               entityCreature.spawnEventType = mobEventPlayerServer.mobEvent.title;
               if (mobEventPlayerServer.mobEvent.name.equals(worldExt.getWorldEventName())) {
                  entityCreature.spawnEventCount = worldExt.getWorldEventCount();
               }
            }
         }
      }

      LycanitesMobs.logDebug("JSONSpawner", "Adding Entity To Level: " + world + " - " + entityLiving);
      world.addFreshEntity(entityLiving);
      mobSpawn.onSpawned(entityLiving, player);
      if (!"".equals(this.eventName) && worldExt != null) {
         MobEventPlayerServer mobEventPlayerServer = worldExt.getMobEventPlayerServer(this.eventName);
         if (mobEventPlayerServer != null) {
            MobEvent mobEvent = mobEventPlayerServer.mobEvent;
            mobEvent.onSpawn(
               entityLiving,
               mobEventPlayerServer.world,
               mobEventPlayerServer.player,
               mobEventPlayerServer.origin,
               mobEventPlayerServer.level,
               mobEventPlayerServer.ticks,
               -1
            );
         }
      }
   }
}
