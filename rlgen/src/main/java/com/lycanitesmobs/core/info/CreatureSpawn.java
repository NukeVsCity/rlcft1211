package com.lycanitesmobs.core.info;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.helpers.JSONHelper;
import com.lycanitesmobs.core.spawner.SpawnerMobRegistry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class CreatureSpawn {
   public boolean enabled = true;
   public boolean disableVariants = false;
   public List<String> spawners = new ArrayList<>();
   public List<MobCategory> vanillaSpawnerTypes = new ArrayList<>();
   public int[] dimensionIds;
   public String dimensionListType = "whitelist";
   public List<String> biomeTags = new ArrayList<>();
   public List<Biome> biomesFromTags = null;
   public List<String> biomeIds = new ArrayList<>();
   public List<Biome> biomes = null;
   public boolean ignoreBiome = false;
   public int spawnWeight = 8;
   public int dungeonWeight = 200;
   public int spawnAreaLimit = 5;
   public int spawnGroupMin = 1;
   public int spawnGroupMax = 3;
   public boolean spawnsInLight = false;
   public boolean spawnsInDark = true;
   public double worldDayMin = -1.0;
   public boolean despawnNatural = true;
   public boolean despawnForced = false;

   public void loadFromJSON(CreatureInfo creatureInfo, JsonObject json) {
      if (json.has("enabled")) {
         this.enabled = json.get("enabled").getAsBoolean();
      }

      if (json.has("disableSubspecies")) {
         this.disableVariants = json.get("disableSubspecies").getAsBoolean();
      }

      this.spawners.clear();
      this.vanillaSpawnerTypes.clear();
      if (json.has("spawners")) {
         this.spawners = JSONHelper.getJsonStrings(json.get("spawners").getAsJsonArray());

         for (String spawner : this.spawners) {
            LycanitesMobs.logDebug("Creature", "Adding " + creatureInfo.getName() + " to " + spawner + " global spawn list.");
            SpawnerMobRegistry.createSpawn(creatureInfo, spawner);
            if ("monster".equalsIgnoreCase(spawner)) {
               this.vanillaSpawnerTypes.add(MobCategory.MONSTER);
            } else if ("creature".equalsIgnoreCase(spawner)) {
               this.vanillaSpawnerTypes.add(MobCategory.CREATURE);
            } else if ("watercreature".equalsIgnoreCase(spawner)) {
               this.vanillaSpawnerTypes.add(MobCategory.WATER_CREATURE);
            } else if ("ambient".equalsIgnoreCase(spawner)) {
               this.vanillaSpawnerTypes.add(MobCategory.AMBIENT);
            }
         }
      }

      if (json.has("dimensionIds")) {
         JsonArray jsonArray = json.get("dimensionIds").getAsJsonArray();
         this.dimensionIds = new int[jsonArray.size()];
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         for (int i = 0; jsonIterator.hasNext(); i++) {
            this.dimensionIds[i] = jsonIterator.next().getAsInt();
         }
      }

      if (json.has("dimensionListType")) {
         this.dimensionListType = json.get("dimensionListType").getAsString();
      }

      if (json.has("ignoreBiome")) {
         this.ignoreBiome = json.get("ignoreBiome").getAsBoolean();
      }

      if (json.has("biomeTags")) {
         this.biomeTags.clear();
         this.biomesFromTags = null;
         this.biomeTags = JSONHelper.getJsonStrings(json.get("biomeTags").getAsJsonArray());
      } else if (json.has("biomes")) {
         this.biomeTags.clear();
         this.biomesFromTags = null;
         this.biomeTags = JSONHelper.getJsonStrings(json.get("biomes").getAsJsonArray());
      }

      if (json.has("biomeIds")) {
         this.biomeIds.clear();
         this.biomes = null;
         this.biomeIds = JSONHelper.getJsonStrings(json.get("biomeIds").getAsJsonArray());
      }

      if (json.has("spawnWeight")) {
         this.spawnWeight = json.get("spawnWeight").getAsInt();
      }

      if (json.has("dungeonWeight")) {
         this.dungeonWeight = json.get("dungeonWeight").getAsInt();
      }

      if (json.has("spawnAreaLimit")) {
         this.spawnAreaLimit = json.get("spawnAreaLimit").getAsInt();
      }

      if (json.has("spawnGroupMin")) {
         this.spawnGroupMin = json.get("spawnGroupMin").getAsInt();
      }

      if (json.has("spawnGroupMax")) {
         this.spawnGroupMax = json.get("spawnGroupMax").getAsInt();
      }

      if (json.has("spawnsInLight")) {
         this.spawnsInLight = json.get("spawnsInLight").getAsBoolean();
      }

      if (json.has("spawnsInDark")) {
         this.spawnsInDark = json.get("spawnsInDark").getAsBoolean();
      }

      if (json.has("worldDayMin")) {
         this.worldDayMin = (double)json.get("worldDayMin").getAsInt();
      }

      if (json.has("despawnNatural")) {
         this.despawnNatural = json.get("despawnNatural").getAsBoolean();
      }

      if (json.has("despawnForced")) {
         this.despawnForced = json.get("despawnForced").getAsBoolean();
      }
   }

   public void registerVanillaSpawns(CreatureInfo creatureInfo) {
      LycanitesMobs.logDebug("MobSetup", "========== Post Init Setup: " + creatureInfo.name + " ==========");
      List<Biome> vanillaBiomes = new ArrayList<>();
      if (this.biomesFromTags == null) {
         this.biomesFromTags = JSONHelper.getBiomesFromTags(this.biomeTags);
      }

      vanillaBiomes.addAll(this.biomesFromTags);
      if (this.biomes == null) {
         this.biomes = JSONHelper.getBiomes(this.biomeIds);
      }

      vanillaBiomes.addAll(this.biomes);
      if (!CreatureManager.getInstance().spawnConfig.disableAllSpawning
         && creatureInfo.enabled
         && this.enabled
         && this.spawnWeight > 0
         && this.spawnGroupMax > 0
         && CreatureManager.getInstance().spawnConfig.spawnWeightScale > 0.0) {
         int vanillaWeight = (int)Math.ceil(CreatureManager.getInstance().spawnConfig.spawnWeightScale * (double)this.spawnWeight);

         for (MobCategory creatureType : this.vanillaSpawnerTypes) {
            EntityRegistry.addSpawn(
               creatureInfo.entityClass,
               vanillaWeight,
               CreatureManager.getInstance().spawnConfig.ignoreWorldGenSpawning ? 0 : this.spawnGroupMin,
               CreatureManager.getInstance().spawnConfig.ignoreWorldGenSpawning ? 0 : this.spawnGroupMax,
               creatureType,
               this.biomesFromTags.toArray(new Biome[this.biomesFromTags.size()])
            );

            for (Biome biome : vanillaBiomes) {
               if (biome == Biomes.HELL) {
                  EntityRegistry.addSpawn(
                     creatureInfo.entityClass,
                     vanillaWeight * 10,
                     CreatureManager.getInstance().spawnConfig.ignoreWorldGenSpawning ? 0 : this.spawnGroupMin,
                     CreatureManager.getInstance().spawnConfig.ignoreWorldGenSpawning ? 0 : this.spawnGroupMax,
                     creatureType,
                     new Biome[]{biome}
                  );
                  break;
               }

               LycanitesMobs.logDebug("MobSetup", "Vanilla Spawn Biome Added: " + biome);
            }
         }

         LycanitesMobs.logDebug("MobSetup", "Vanilla Spawns Added - Weight: " + vanillaWeight);
      }

      if (!CreatureManager.getInstance().spawnConfig.disableDungeonSpawners && this.dungeonWeight > 0) {
         DungeonHooks.addDungeonMob(creatureInfo.getResourceLocation(), this.dungeonWeight);
         LycanitesMobs.logDebug("MobSetup", "Dungeon Spawn Added - Weight: " + this.dungeonWeight);
      }
   }

   public boolean isAllowedDimension(Level world) {
      if (world != null && world.provider != null) {
         if (!CreatureManager.getInstance().spawnConfig.isAllowedGlobal(world)) {
            return false;
         } else if (this.dimensionIds.length == 0) {
            return true;
         } else {
            for (int dimensionId : this.dimensionIds) {
               if (LycDim.id(world) == dimensionId) {
                  LycanitesMobs.logDebug("MobSpawns", "Dimension is in " + this.dimensionListType + ".");
                  return this.dimensionListType.equalsIgnoreCase("whitelist");
               }
            }

            LycanitesMobs.logDebug("MobSpawns", "Dimension was not in " + this.dimensionListType + ".");
            return this.dimensionListType.equalsIgnoreCase("blacklist");
         }
      } else {
         LycanitesMobs.logDebug("MobSpawns", "No world or dimension spawn settings were found, defaulting to valid.");
         return true;
      }
   }

   public boolean isValidBiome(Biome biome) {
      if (this.ignoreBiome) {
         return true;
      } else {
         if (!this.biomeTags.isEmpty()) {
            if (this.biomesFromTags == null) {
               this.biomesFromTags = JSONHelper.getBiomesFromTags(this.biomeTags);
            }

            if (this.biomesFromTags.contains(biome)) {
               return true;
            }
         }

         if (!this.biomeIds.isEmpty()) {
            if (this.biomes == null) {
               this.biomes = JSONHelper.getBiomes(this.biomeIds);
            }

            if (this.biomes.contains(biome)) {
               return true;
            }
         }

         return false;
      }
   }
}
