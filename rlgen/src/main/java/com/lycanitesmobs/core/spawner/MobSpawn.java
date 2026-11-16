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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ItemDrop;
import java.util.ArrayList;
import java.util.List;


public class MobSpawn {
   public CreatureInfo creatureInfo;
   public Class entityClass;
   protected boolean ignoreForgeCanSpawnEvent = false;
   protected boolean ignoreMobInstanceConditions = false;
   protected boolean ignoreDimension = false;
   protected String biomeCheck = "default";
   protected boolean ignoreLightLevel = false;
   protected boolean ignoreGroupLimit = false;
   protected int weight = -1;
   protected double chance = -1.0;
   protected int blockCost = -1;
   protected String mobNameTag = "";
   protected String naturalDespawn = "default";
   protected int temporary = -1;
   protected double mobSizeScale = -1.0;
   protected int subspecies = -1;
   protected int variant = -1;
   protected boolean fixate = false;
   protected double home = -1.0;
   protected int mobLevel = 0;
   public boolean spawnAsBoss = false;
   protected List<ItemDrop> mobDrops = new ArrayList<>();
   public int dungeonLevelMin = -1;
   public int dungeonLevelMax = -1;
   public int blockProtection = 0;
   public boolean dungeonBoss = false;

   public static MobSpawn createFromJSON(JsonObject json) {
      MobSpawn mobSpawn = null;
      if (json.has("mobId")) {
         String mobId = json.get("mobId").getAsString();
         CreatureInfo creatureInfo = CreatureManager.getInstance().getCreatureFromId(mobId);
         if (creatureInfo != null) {
            mobSpawn = new MobSpawn(creatureInfo);
            mobSpawn.loadFromJSON(json);
         } else {
            // 1.12 ForgeRegistries.ENTITIES entry class: the 1.21 entity type (no class is registered)
            ResourceLocation typeId = ResourceLocation.tryParse(mobId);
            EntityType<?> type = typeId != null && BuiltInRegistries.ENTITY_TYPE.containsKey(typeId) ? BuiltInRegistries.ENTITY_TYPE.get(typeId) : null;
            mobSpawn = new MobSpawn((Class)null);
            mobSpawn.entityType = type;
         }

         if (mobSpawn != null) {
            mobSpawn.loadFromJSON(json);
         } else {
            LycanitesMobs.logWarning("", "[JSONSpawner] Unable to find a Lycanites Mob from the mob id: " + mobId + " Mob Spawn entry ignored.");
         }
      }

      return mobSpawn;
   }

   public MobSpawn(CreatureInfo creatureInfo) {
      this.creatureInfo = creatureInfo;
      this.entityClass = creatureInfo.entityClass;
   }

   /** Non-Lycanites mob of this spawn (1.21 has no entity classes in its registry). */
   public EntityType<?> entityType;

   public MobSpawn(Class entityClass) {
      this.entityClass = entityClass;
   }

   public void loadFromJSON(JsonObject json) {
      if (json.has("ignoreForgeCanSpawnEvent")) {
         this.ignoreForgeCanSpawnEvent = json.get("ignoreForgeCanSpawnEvent").getAsBoolean();
      }

      if (json.has("ignoreDimension")) {
         this.ignoreDimension = json.get("ignoreDimension").getAsBoolean();
      }

      if (json.has("biomeCheck")) {
         this.biomeCheck = json.get("biomeCheck").getAsString();
      }

      if (json.has("ignoreLightLevel")) {
         this.ignoreLightLevel = json.get("ignoreLightLevel").getAsBoolean();
      }

      if (json.has("ignoreGroupLimit")) {
         this.ignoreGroupLimit = json.get("ignoreGroupLimit").getAsBoolean();
      }

      if (json.has("ignoreMobInstanceConditions")) {
         this.ignoreMobInstanceConditions = json.get("ignoreMobInstanceConditions").getAsBoolean();
      }

      if (json.has("weight")) {
         this.weight = json.get("weight").getAsInt();
      }

      if (json.has("chance")) {
         this.chance = json.get("chance").getAsDouble();
      }

      if (json.has("blockCost")) {
         this.blockCost = json.get("blockCost").getAsInt();
      }

      if (json.has("mobNameTag")) {
         this.mobNameTag = json.get("mobNameTag").getAsString();
      }

      if (json.has("naturalDespawn")) {
         this.naturalDespawn = json.get("naturalDespawn").getAsString();
      }

      if (json.has("temporary")) {
         this.temporary = json.get("temporary").getAsInt();
      }

      if (json.has("mobSizeScale")) {
         this.mobSizeScale = json.get("mobSizeScale").getAsDouble();
      }

      if (json.has("subspecies")) {
         this.subspecies = json.get("subspecies").getAsInt();
      }

      if (json.has("variant")) {
         this.variant = json.get("variant").getAsInt();
      }

      if (json.has("fixate")) {
         this.fixate = json.get("fixate").getAsBoolean();
      }

      if (json.has("home")) {
         this.home = json.get("home").getAsDouble();
      }

      if (json.has("mobLevel")) {
         this.mobLevel = json.get("mobLevel").getAsInt();
      }

      if (json.has("spawnAsBoss")) {
         this.spawnAsBoss = json.get("spawnAsBoss").getAsBoolean();
      }

      if (json.has("mobDrops")) {
         this.mobDrops.clear();

         for (JsonElement mobDropJson : json.getAsJsonArray("mobDrops")) {
            ItemDrop itemDrop = ItemDrop.createFromJSON(mobDropJson.getAsJsonObject());
            if (itemDrop != null) {
               this.mobDrops.add(itemDrop);
            }
         }
      }

      if (json.has("dungeonLevelMin")) {
         this.dungeonLevelMin = json.get("dungeonLevelMin").getAsInt();
      }

      if (json.has("dungeonLevelMax")) {
         this.dungeonLevelMax = json.get("dungeonLevelMax").getAsInt();
      }

      if (json.has("blockProtection")) {
         this.blockProtection = json.get("blockProtection").getAsInt();
      }

      if (json.has("dungeonBoss")) {
         this.dungeonBoss = json.get("dungeonBoss").getAsBoolean();
      }
   }

   public boolean canSpawn(Level world, int blockCount, ResourceLocation biome, boolean forceIgnoreDimension) {
      if (!CreatureManager.getInstance().spawnConfig.isAllowedGlobal(world)) {
         return false;
      } else {
         if (this.creatureInfo != null) {
            if (!this.creatureInfo.enabled || !this.creatureInfo.creatureSpawn.enabled) {
               return false;
            }

            if (world.getDifficulty() == Difficulty.PEACEFUL && !this.creatureInfo.peaceful) {
               return false;
            }
         }

         if (this.getWeight() <= 0) {
            return false;
         } else if (blockCount < this.getBlockCost()) {
            return false;
         } else {
            if (this.creatureInfo != null) {
               if (this.creatureInfo.creatureSpawn.worldDayMin > 0.0) {
                  ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
                  if (worldExt != null) {
                     int day = (int)Math.floor((double)(worldExt.useTotalWorldTime ? world.getGameTime() : world.getDayTime()) / 24000.0);
                     if ((double)day < this.creatureInfo.creatureSpawn.worldDayMin) {
                        return false;
                     }
                  }
               }

               if (!forceIgnoreDimension && !this.ignoreDimension && !this.creatureInfo.creatureSpawn.isAllowedDimension(world)) {
                  return false;
               }

               if (biome != null && this.shouldCheckBiome() && !this.creatureInfo.creatureSpawn.isValidBiome(biome)) {
                  return false;
               }
            }

            return !(this.getChance() < 1.0) || !(world.random.nextDouble() > this.getChance());
         }
      }
   }

   public int getBlockCost() {
      return this.blockCost > -1 ? this.blockCost : 0;
   }

   public double getChance() {
      return this.chance > -1.0 ? this.chance : 1.0;
   }

   public int getWeight() {
      if (this.weight > -1) {
         return this.weight;
      } else {
         return this.creatureInfo != null ? this.creatureInfo.creatureSpawn.spawnWeight : 8;
      }
   }

   public boolean getNaturalDespawn() {
      if ("true".equalsIgnoreCase(this.naturalDespawn)) {
         return true;
      } else if ("false".equalsIgnoreCase(this.naturalDespawn)) {
         return false;
      } else {
         return this.creatureInfo != null ? this.creatureInfo.creatureSpawn.despawnNatural : true;
      }
   }

   public boolean shouldCheckBiome() {
      if ("ignore".equalsIgnoreCase(this.biomeCheck)) {
         return false;
      } else if ("check".equalsIgnoreCase(this.biomeCheck)) {
         return true;
      } else {
         return this.creatureInfo != null ? !this.creatureInfo.creatureSpawn.ignoreBiome : false;
      }
   }

   public Mob createEntity(Level world) {
      try {
         if (this.creatureInfo != null) {
            return this.creatureInfo.createEntity(world);
         } else {
            if (this.entityType != null) {
               return this.entityType.create(world) instanceof Mob mob ? mob : null;
            }
            return this.entityClass == null ? null : (Mob)this.entityClass.getConstructor(Level.class).newInstance(world);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public void onSpawned(Mob entityLiving, Player player) {
      if (!"".equals(this.mobNameTag)) {
         entityLiving.setCustomName(Component.literal(this.mobNameTag));
      }

      if (!this.getNaturalDespawn()) {
         entityLiving.setPersistenceRequired();
      }

      if (entityLiving instanceof BaseCreatureEntity) {
         BaseCreatureEntity entityCreature = (BaseCreatureEntity)entityLiving;
         boolean firstSpawn = true;
         if (this.mobSizeScale > -1.0) {
            entityCreature.setSizeScale(this.mobSizeScale);
            firstSpawn = false;
         }

         if (this.subspecies > -1) {
            entityCreature.setSubspecies(this.subspecies);
            firstSpawn = false;
         }

         if (this.variant > -1) {
            entityCreature.applyVariant(this.variant);
            firstSpawn = false;
         }

         if (this.fixate && player != null) {
            entityCreature.setFixateTarget(player);
         }

         if (this.home >= 0.0) {
            entityCreature.setHome((int)entityCreature.getX(), (int)entityCreature.getY(), (int)entityCreature.getZ(), (float)this.home);
         }

         if (this.mobLevel > 0) {
            entityCreature.addLevel(this.mobLevel);
         }

         if (this.temporary > -1) {
            entityCreature.setTemporary(this.temporary);
         }

         for (ItemDrop itemDrop : this.mobDrops) {
            entityCreature.addSavedItemDrop(itemDrop);
         }

         entityCreature.spawnedAsBoss = this.spawnAsBoss;
         if (this.blockProtection > 0) {
            entityCreature.spawnedWithBlockProtection = this.blockProtection;
            ExtendedWorld.getForWorld(entityCreature.level()).bossUpdate(entityCreature);
         }

         entityCreature.firstSpawn = firstSpawn;
      }
   }

   @Override
   public String toString() {
      if (this.creatureInfo != null) {
         return this.creatureInfo.getName();
      } else {
         return this.entityType != null ? this.entityType.toString() : this.entityClass != null ? this.entityClass.toString() : "Invalid Creature ID or Class";
      }
   }
}
