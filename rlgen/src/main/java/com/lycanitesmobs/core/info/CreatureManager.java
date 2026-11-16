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
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.JSONLoader;
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.entity.CreatureStats;
import com.lycanitesmobs.core.spawner.SpawnerMobRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CreatureManager extends JSONLoader {
   public static CreatureManager INSTANCE;
   public CreatureConfig config;
   public CreatureSpawnConfig spawnConfig;
   public Map<String, CreatureType> creatureTypes = new HashMap<>();
   public Map<String, CreatureGroup> creatureGroups = new HashMap<>();
   public Map<String, CreatureInfo> creatures = new HashMap<>();
   public Map<Class, CreatureInfo> creatureClassMap = new HashMap<>();
   protected int nextCreatureNetworkId = 100;
   public List<ModInfo> loadedMods = new ArrayList<>();
   public Map<String, Double> difficultyMultipliers = new HashMap<>();
   public Map<String, Double> levelMultipliers = new HashMap<>();
   public double tamedHealthMultiplier = 3.0;
   public boolean dlDungeonsLoaded = false;

   public static CreatureManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new CreatureManager();
      }

      return INSTANCE;
   }

   public CreatureManager() {
      this.config = new CreatureConfig();
      this.spawnConfig = new CreatureSpawnConfig();
   }

   public void startup(ModInfo modInfo) {
      this.loadConfig();
      this.loadCreatureTypesFromJSON(modInfo);
      this.loadCreatureGroupsFromJSON(modInfo);
      this.loadCreaturesFromJSON(modInfo);

      for (CreatureType creatureType : this.creatureTypes.values()) {
         creatureType.load();
      }

      for (CreatureGroup creatureGroup : this.creatureGroups.values()) {
         creatureGroup.init();
      }

      for (CreatureInfo creatureInfo : this.creatures.values()) {
         creatureInfo.load();
      }
   }

   public void lateStartup(ModInfo modInfo) {
      for (CreatureInfo creatureInfo : this.creatures.values()) {
         creatureInfo.lateLoad();
      }
   }

   public void loadConfig() {
      ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "general");
      this.config.loadConfig(config);
      this.spawnConfig.loadConfig(ConfigBase.getConfig(LycanitesMobs.modInfo, "spawning"));
      String[] difficultyNames = new String[]{"easy", "normal", "hard"};
      double[] difficultyDefaults = new double[]{0.8, 1.0, 1.1};
      this.difficultyMultipliers = new HashMap<>();
      config.setCategoryComment(
         "Difficulty Multipliers",
         "Here you can scale the stats of every mob on a per difficulty basis. Note that on easy, speed is kept at 1.0 by default as 0.5 makes them stupidly slow."
      );
      int difficultyIndex = 0;

      for (String difficultyName : difficultyNames) {
         for (String statName : CreatureStats.STAT_NAMES) {
            double defaultValue = difficultyDefaults[difficultyIndex];
            if ("easy".equalsIgnoreCase(difficultyName) && "speed".equalsIgnoreCase(statName)) {
               defaultValue = 1.0;
            }

            if ("hard".equalsIgnoreCase(difficultyName) && ("attackSpeed".equalsIgnoreCase(statName) || "rangedSpeed".equalsIgnoreCase(statName))) {
               defaultValue = 1.5;
            }

            if ("armor".equalsIgnoreCase(statName)) {
               defaultValue = 1.0;
            }

            if ("sight".equalsIgnoreCase(statName)) {
               defaultValue = 1.0;
            }

            this.difficultyMultipliers
               .put(
                  (difficultyName + "-" + statName).toUpperCase(Locale.ENGLISH),
                  config.getDouble("Difficulty Multipliers", difficultyName + " " + statName, defaultValue)
               );
         }

         difficultyIndex++;
      }

      config.setCategoryComment(
         "Mob Level Multipliers",
         "Normally mobs are level 1, but Spawners can increase their level. Here you can adjust the percentage of each stat that is added per extra level. So by default at level 2 a mobs health is increased by 10%, at level 3 20% and so on."
      );

      for (String statName : CreatureStats.STAT_NAMES) {
         double levelValue = 0.01;
         if ("health".equalsIgnoreCase(statName)) {
            levelValue = 0.1;
         }

         if ("defense".equalsIgnoreCase(statName)) {
            levelValue = 0.01;
         }

         if ("armor".equalsIgnoreCase(statName)) {
            levelValue = 0.0;
         }

         if ("speed".equalsIgnoreCase(statName)) {
            levelValue = 0.01;
         }

         if ("damage".equalsIgnoreCase(statName)) {
            levelValue = 0.02;
         }

         if ("attackSpeed".equalsIgnoreCase(statName)) {
            levelValue = 0.01;
         }

         if ("rangedSpeed".equalsIgnoreCase(statName)) {
            levelValue = 0.01;
         }

         if ("effect".equalsIgnoreCase(statName)) {
            levelValue = 0.02;
         }

         if ("pierce".equalsIgnoreCase(statName)) {
            levelValue = 0.02;
         }

         if ("sight".equalsIgnoreCase(statName)) {
            levelValue = 0.0;
         }

         this.levelMultipliers.put(statName.toUpperCase(Locale.ENGLISH), config.getDouble("Mob Level Multipliers", statName, levelValue));
      }
   }

   public void loadCreatureTypesFromJSON(ModInfo groupInfo) {
      try {
         this.loadAllJson(groupInfo, "Creature Type", "creaturetypes", "name", true);
         LycanitesMobs.logDebug("Creature", "Complete! " + this.creatureTypes.size() + " JSON Creature Types Loaded In Total.");
      } catch (Exception var3) {
         LycanitesMobs.logWarning("", "No Creature Types loaded for: " + groupInfo.name);
      }
   }

   public void loadCreatureGroupsFromJSON(ModInfo groupInfo) {
      try {
         this.loadAllJson(groupInfo, "Creature Group", "creaturegroups", "name", true);
         LycanitesMobs.logDebug("Creature", "Complete! " + this.creatureGroups.size() + " JSON Creature Groups Loaded In Total.");
      } catch (Exception var3) {
         LycanitesMobs.logWarning("", "No Creature Groups loaded for: " + groupInfo.name);
      }
   }

   public void loadCreaturesFromJSON(ModInfo groupInfo) {
      try {
         if (!this.loadedMods.contains(groupInfo)) {
            this.loadedMods.add(groupInfo);
         }

         this.loadAllJson(groupInfo, "Creature", "creatures", "name", false);
         LycanitesMobs.logDebug("Creature", "Complete! " + this.creatures.size() + " JSON Creature Info Loaded In Total.");
      } catch (Exception var3) {
         LycanitesMobs.logWarning("", "No Creatures loaded for: " + groupInfo.name);
      }
   }

   @Override
   public void parseJson(ModInfo groupInfo, String loadGroup, JsonObject json) {
      if ("Creature Type".equals(loadGroup)) {
         CreatureType creatureType = new CreatureType(groupInfo);
         creatureType.loadFromJSON(json);
         if (creatureType.name == null) {
            LycanitesMobs.logWarning("", "[Creature] Unable to load " + loadGroup + " json due to missing name.");
         } else {
            if (this.creatureTypes.containsKey(creatureType.name)) {
               creatureType = this.creatureTypes.get(creatureType.name);
               creatureType.loadFromJSON(json);
            }

            this.creatureTypes.put(creatureType.name, creatureType);
         }
      } else if ("Creature Group".equals(loadGroup)) {
         CreatureGroup creatureGroup = new CreatureGroup();
         creatureGroup.loadFromJson(json);
         if (this.creatureGroups.containsKey(creatureGroup.name)) {
            creatureGroup = this.creatureGroups.get(creatureGroup.name);
            creatureGroup.loadFromJson(json);
         }

         this.creatureGroups.put(creatureGroup.name, creatureGroup);
      } else {
         if ("Creature".equals(loadGroup)) {
            CreatureInfo creatureInfo = new CreatureInfo(groupInfo);
            creatureInfo.loadFromJSON(json);
            if (creatureInfo.name == null) {
               LycanitesMobs.logWarning("", "[Creature] Unable to load " + loadGroup + " json due to missing name.");
               return;
            }

            if (this.creatures.containsKey(creatureInfo.name)) {
               creatureInfo = this.creatures.get(creatureInfo.name);
               creatureInfo.loadFromJSON(json);
            }

            this.creatures.put(creatureInfo.name, creatureInfo);
            this.creatureClassMap.put(creatureInfo.entityClass, creatureInfo);
         }
      }
   }

   public int getNextCreatureNetworkId() {
      return this.nextCreatureNetworkId++;
   }

   @SubscribeEvent
   public void registerEntities(Register<EntityEntry> event) {
      ModInfo modInfo = LycanitesMobs.modInfo;
      LycanitesMobs.logDebug("Creature", "Forge registering all " + this.creatures.size() + " creatures from the mod: " + modInfo.name + "...");

      for (CreatureInfo creatureInfo : this.creatures.values()) {
         if (creatureInfo.modInfo == modInfo) {
            try {
               EntityEntry entityEntry = EntityEntryBuilder.create()
                  .entity(creatureInfo.entityClass)
                  .id(creatureInfo.getId(), this.getNextCreatureNetworkId())
                  .name(creatureInfo.getName())
                  .tracker(creatureInfo.isBoss() ? 160 : 80, 3, false)
                  .build();
               event.getRegistry().register(entityEntry);
            } catch (Exception var6) {
               LycanitesMobs.logWarning("", "Unable to find entity class for: " + creatureInfo.getName() + ".");
               throw var6;
            }
         }
      }
   }

   public void reload() {
      this.loadConfig();
      SpawnerMobRegistry.SPAWNER_MOB_REGISTRIES.clear();

      for (ModInfo group : this.loadedMods) {
         this.loadCreaturesFromJSON(group);
      }
   }

   public CreatureType getCreatureType(String creatureTypeName) {
      return !this.creatureTypes.containsKey(creatureTypeName) ? null : this.creatureTypes.get(creatureTypeName);
   }

   public CreatureGroup getCreatureGroup(String creatureGroupName) {
      return !this.creatureGroups.containsKey(creatureGroupName) ? null : this.creatureGroups.get(creatureGroupName);
   }

   public CreatureInfo getCreature(String creatureName) {
      return !this.creatures.containsKey(creatureName) ? null : this.creatures.get(creatureName);
   }

   public CreatureInfo getCreature(Class creatureClass) {
      return !this.creatureClassMap.containsKey(creatureClass) ? null : this.creatureClassMap.get(creatureClass);
   }

   public CreatureInfo getCreatureFromId(String entityId) {
      entityId = entityId.replace(".", ":");
      String[] mobIdParts = entityId.toLowerCase().split(":");
      return this.getCreature(mobIdParts[mobIdParts.length - 1]);
   }

   public Class<? extends Mob> getEntityClass(String creatureName) {
      CreatureInfo creatureInfo = this.getCreature(creatureName);
      return creatureInfo == null ? null : creatureInfo.entityClass;
   }

   public double getDifficultyMultiplier(String difficultyName, String statName) {
      String key = difficultyName.toUpperCase(Locale.ENGLISH) + "-" + statName.toUpperCase(Locale.ENGLISH);
      return !this.difficultyMultipliers.containsKey(key) ? 1.0 : this.difficultyMultipliers.get(key);
   }

   public double getLevelMultiplier(String statName) {
      return !this.levelMultipliers.containsKey(statName.toUpperCase(Locale.ENGLISH)) ? 1.0 : this.levelMultipliers.get(statName.toUpperCase(Locale.ENGLISH));
   }
}
