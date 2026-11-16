package com.lycanitesmobs.core.pets;

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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.CreatureStats;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.Subspecies;
import com.lycanitesmobs.core.info.Variant;
import java.util.UUID;


public class PetEntry {
   public PetManager petManager;
   public UUID petEntryID;
   private String type;
   public boolean active = true;
   public int respawnTime = 0;
   public int respawnTimeMax;
   public boolean isRespawning = false;
   public int spawnCount = 0;
   public boolean temporary = false;
   public int temporaryDuration = 100;
   public boolean spawningActive = true;
   public LivingEntity host;
   public SummonSet summonSet;
   public Entity entity;
   public CompoundTag entityNBT;
   public int entityTick = 0;
   public float entityHealth = 1.0F;
   public float entityMaxHealth = 1.0F;
   public int entityLevel = 1;
   public int entityExperience = 0;
   public int entityMaxExperience = CreatureStats.BASE_LEVELUP_EXPERIENCE;
   public String entityName = "";
   public int subspeciesIndex = 0;
   public int variantIndex = 0;
   public double entitySize = 1.0;
   public String color = "000000";
   public boolean teleportEntity = false;
   public boolean releaseEntity = false;

   public static PetEntry createFromEntity(Player player, BaseCreatureEntity entity, String petType) {
      CreatureInfo creatureInfo = entity.creatureInfo;
      PetEntry petEntry = new PetEntry(UUID.randomUUID(), petType, player, creatureInfo.getName());
      if (entity.hasCustomName()) {
         petEntry.setEntityName(entity.getCustomName().getString());
      }

      petEntry.setEntitySubspecies(entity.getSubspeciesIndex());
      petEntry.setEntityVariant(entity.getVariantIndex());
      petEntry.setEntitySize(entity.sizeScale);
      petEntry.setColor("000000");
      return petEntry;
   }

   public PetEntry(UUID petEntryID, String type, LivingEntity host, String summonType) {
      this.petEntryID = petEntryID;
      this.type = type;
      this.host = host;
      ExtendedPlayer playerExt = null;
      if (host instanceof Player) {
         playerExt = ExtendedPlayer.getForPlayer((Player)host);
      }

      this.summonSet = new SummonSet(playerExt);
      this.summonSet.summonableOnly = false;
      this.summonSet.setSummonType(summonType);
      this.respawnTimeMax = CreatureManager.getInstance().config.petRespawnTime;
      if ("minion".equalsIgnoreCase(this.type)) {
         this.temporary = true;
      }
   }

   public PetEntry setEntityName(String name) {
      this.entityName = name;
      return this;
   }

   public PetEntry setEntitySubspecies(int index) {
      this.subspeciesIndex = index;
      return this;
   }

   public PetEntry setEntityVariant(int index) {
      this.variantIndex = index;
      return this;
   }

   public PetEntry setEntitySize(double size) {
      this.entitySize = size;
      return this;
   }

   public PetEntry setColor(String color) {
      this.color = color;
      return this;
   }

   public PetEntry setOwner(LivingEntity owner) {
      this.host = owner;
      if (this.host != null && this.host instanceof Player) {
         ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer((Player)this.host);
         this.summonSet.playerExt = playerExt;
      }

      return this;
   }

   public PetEntry setSpawningActive(boolean spawningActive) {
      if (this.spawningActive == spawningActive) {
         return this;
      } else {
         if (!this.host.level().isClientSide) {
            if (!CreatureManager.getInstance().config.isSoulboundAllowed(this.host.level())) {
               spawningActive = false;
            }

            if (!spawningActive) {
               this.despawnEntity();
            } else if (this.usesSpirit() && this.summonSet.playerExt != null) {
               if (this.summonSet.playerExt.spirit < this.getSpiritCost()) {
                  this.spawningActive = false;
                  return this;
               }

               this.summonSet.playerExt.spirit = this.summonSet.playerExt.spirit - this.getSpiritCost();
               this.summonSet.playerExt.spiritReserved = this.summonSet.playerExt.spiritReserved + this.getSpiritCost();
            }
         }

         this.spawningActive = spawningActive;
         return this;
      }
   }

   public void setLevel(int level) {
      if (this.entity != null && this.entity instanceof BaseCreatureEntity) {
         ((BaseCreatureEntity)this.entity).setLevel(level);
      }

      this.entityLevel = level;
   }

   public int getLevel() {
      if (this.entity != null && this.entity instanceof BaseCreatureEntity) {
         this.entityLevel = ((BaseCreatureEntity)this.entity).getLevel();
      }

      return this.entityLevel;
   }

   public int getExperience() {
      if (this.entity != null && this.entity instanceof BaseCreatureEntity) {
         this.entityExperience = ((BaseCreatureEntity)this.entity).getExperience();
      }

      return this.entityExperience;
   }

   public void setExperience(int experience) {
      if (this.entity != null && this.entity instanceof BaseCreatureEntity) {
         ((BaseCreatureEntity)this.entity).setExperience(experience);
      }

      this.entityExperience = experience;
   }

   public int getMaxExperience() {
      return CreatureStats.BASE_LEVELUP_EXPERIENCE + Math.round((float)(CreatureStats.BASE_LEVELUP_EXPERIENCE * this.getLevel()) * 0.25F);
   }

   public CreatureInfo getCreatureInfo() {
      return this.summonSet != null && !"".equals(this.summonSet.summonType) ? CreatureManager.getInstance().getCreature(this.summonSet.summonType) : null;
   }

   public float getHealth() {
      if (this.entity != null && this.entity instanceof LivingEntity) {
         this.entityHealth = ((LivingEntity)this.entity).getHealth();
      }

      return this.entityHealth;
   }

   public float getMaxHealth() {
      if (this.entity != null && this.entity instanceof LivingEntity) {
         this.entityMaxHealth = ((LivingEntity)this.entity).getMaxHealth();
      }

      return this.entityMaxHealth;
   }

   public void copy(PetEntry copyEntry) {
      this.setEntityName(copyEntry.entityName);
      this.setEntitySubspecies(copyEntry.subspeciesIndex);
      this.setEntityVariant(copyEntry.variantIndex);
      this.setEntitySize(copyEntry.entitySize);
      this.setColor(copyEntry.color);
      if (copyEntry.summonSet != null) {
         this.summonSet.setSummonType(copyEntry.summonSet.summonType);
      }
   }

   public String getDisplayName() {
      String displayName = this.summonSet.getCreatureInfo().getTitle();
      if (this.entityName != null && !"".equals(this.entityName)) {
         displayName = this.entityName + " (" + displayName + ")";
      }

      return displayName;
   }

   public void onAdd(PetManager petManager) {
      this.petManager = petManager;
   }

   public void remove() {
      this.setSpawningActive(false);
      this.active = false;
   }

   public void onUpdate(Level world) {
      if (world.isClientSide) {
         if (this.isRespawning && this.respawnTime > 0) {
            this.respawnTime--;
         }
      } else if (this.active) {
         if (!this.isActive()) {
            this.remove();
         } else {
            if (!CreatureManager.getInstance().config.isSoulboundAllowed(this.host.level())) {
               this.setSpawningActive(false);
            }

            if (this.spawningActive) {
               if (this.entity != null && !this.entity.isAlive()) {
                  this.saveEntityNBT();
                  this.entity = null;
                  this.isRespawning = true;
                  this.respawnTime = this.respawnTimeMax;
                  if (this.summonSet.playerExt != null) {
                     this.summonSet.playerExt.sendPetEntryToPlayer(this);
                  }
               }

               if (this.entity == null) {
                  if (!this.isRespawning) {
                     this.respawnTime = 0;
                  }

                  if (this.respawnTime > this.respawnTimeMax) {
                     this.respawnTime = this.respawnTimeMax;
                  }

                  if (this.respawnTime-- <= 0) {
                     this.addFreshEntity();
                     this.isRespawning = false;
                  }
               }

               if (this.entity != null) {
                  this.entityTick++;

                  try {
                     if (this.teleportEntity) {
                        if (this.entity.level() != this.host.level()) {
                           this.entity.changeDimension(LycDim.id(this.host.level()));
                        }

                        this.entity.setPos(this.host.getX(), this.host.getY(), this.host.getZ());
                     }
                  } catch (Exception var3) {
                     LycanitesMobs.logDebug("Pet", "Unable to teleport a pet.");
                  }

                  if (this.entity instanceof LivingEntity) {
                     LivingEntity entityLiving = (LivingEntity)this.entity;
                     if (this.entityTick % 60 == 0 && entityLiving.getHealth() < entityLiving.getMaxHealth()) {
                        entityLiving.setHealth(Math.min(entityLiving.getHealth() + 1.0F, entityLiving.getMaxHealth()));
                     }

                     this.entityHealth = entityLiving.getHealth();
                     this.entityMaxHealth = entityLiving.getMaxHealth();
                  }

                  if (this.entity.hasCustomName()) {
                     this.entityName = this.entity.getCustomName().getString();
                  }
               }
            } else {
               if (this.entity != null) {
                  this.saveEntityNBT();
                  this.entity.discard();
                  this.entity = null;
               }

               if (this.respawnTime > 0) {
                  this.respawnTime--;
               }
            }

            this.teleportEntity = false;
         }
      }
   }

   public void onBehaviourUpdate() {
      if (this.entity != null && this.entity instanceof TameableCreatureEntity) {
         this.summonSet.applyBehaviour((TameableCreatureEntity)this.entity);
      }
   }

   public boolean isActive() {
      return this.entity != null || !this.temporary || this.spawnCount <= 0;
   }

   public void spawnEntity() {
      if (this.entity == null && this.host != null) {
         try {
            this.entity = (Entity)this.summonSet.getCreatureClass().getConstructor(Level.class).newInstance(this.host.level());
         } catch (Exception var5) {
            LycanitesMobs.logWarning(
               "Pets",
               "[Pet Entry] Unable to find an entity class for pet entry.  Type: "
                  + this.summonSet.summonType
                  + " Class: "
                  + this.summonSet.getCreatureClass()
                  + " ID: "
                  + this.petEntryID
            );
         }

         if (this.entity != null) {
            this.loadEntityNBT();
            this.entity.moveTo(this.host.getX(), this.host.getY(), this.host.getZ(), this.host.getYRot(), 0.0F);
            if (this.entity instanceof BaseCreatureEntity) {
               BaseCreatureEntity entityCreature = (BaseCreatureEntity)this.entity;
               entityCreature.applyLevel(this.entityLevel);
               entityCreature.setExperience(this.entityExperience);
               entityCreature.setMinion(true);
               entityCreature.setPetEntry(this);
               if (entityCreature instanceof TameableCreatureEntity) {
                  TameableCreatureEntity entityTameable = (TameableCreatureEntity)entityCreature;
                  this.summonSet.applyBehaviour(entityTameable);
               }

               float randomAngle = 45.0F + 45.0F * this.host.getRandom().nextFloat();
               if (this.host.getRandom().nextBoolean()) {
                  randomAngle = -randomAngle;
               }

               BlockPos spawnPos = entityCreature.getFacingPosition(this.host, -1.0, (double)randomAngle);
               if (!this.entity.level().isSideSolid(spawnPos, Direction.UP)) {
                  this.entity
                     .moveTo(
                        (double)spawnPos.getX(), (double)spawnPos.getY(), (double)spawnPos.getZ(), this.host.getYRot(), 0.0F
                     );
               } else {
                  spawnPos = entityCreature.getFacingPosition(this.host, -1.0, (double)(-randomAngle));
                  if (this.entity.level().isSideSolid(spawnPos, Direction.UP)) {
                     this.entity
                        .moveTo(
                           (double)spawnPos.getX(), (double)spawnPos.getY(), (double)spawnPos.getZ(), this.host.getYRot(), 0.0F
                        );
                  }
               }

               if (this.temporary) {
                  entityCreature.setTemporary(this.temporaryDuration);
               }

               if (this.entityName != null && !"".equals(this.entityName)) {
                  entityCreature.setCustomName(Component.literal(this.entityName));
               }

               entityCreature.setSizeScale(this.entitySize);
               entityCreature.setSubspecies(this.subspeciesIndex);
               entityCreature.applyVariant(this.variantIndex);
               if (entityCreature instanceof TameableCreatureEntity && this.host instanceof Player) {
                  TameableCreatureEntity entityTameable = (TameableCreatureEntity)entityCreature;
                  entityTameable.setPlayerOwner((Player)this.host);
                  this.summonSet.applyBehaviour(entityTameable);
               }
            }

            this.spawnCount++;
            if (this.entity instanceof LivingEntity && this.isRespawning) {
               LivingEntity entityLiving = (LivingEntity)this.entity;
               entityLiving.setHealth(entityLiving.getMaxHealth() / 2.0F);
            }

            this.onSpawnEntity(this.entity);
            this.host.level().addFreshEntity(this.entity);
            if (this.summonSet.playerExt != null) {
               this.summonSet.playerExt.sendPetEntryToPlayer(this);
            }
         }
      }
   }

   public void onSpawnEntity(Entity entity) {
   }

   public void despawnEntity() {
      if (this.entity != null) {
         this.onDespawnEntity(this.entity);
         this.saveEntityNBT();
         this.entity.discard();
         this.entity = null;
      }
   }

   public void onDespawnEntity(Entity entity) {
   }

   public void assignEntity(Entity entity) {
      if (this.entity != null) {
         this.despawnEntity();
      }

      this.setSpawningActive(true);
      this.entity = entity;
      if (this.entity instanceof BaseCreatureEntity) {
         BaseCreatureEntity entityCreature = (BaseCreatureEntity)this.entity;
         entityCreature.setMinion(true);
         entityCreature.setPetEntry(this);
         if (entityCreature instanceof TameableCreatureEntity) {
            TameableCreatureEntity entityTameable = (TameableCreatureEntity)entityCreature;
            this.summonSet.updateBehaviour(entityTameable);
         }

         if (this.temporary) {
            entityCreature.setTemporary(this.temporaryDuration);
         }

         if (this.entityName != null && !"".equals(this.entityName)) {
            entityCreature.setCustomName(Component.literal(this.entityName));
         }

         entityCreature.setSizeScale(this.entitySize);
         entityCreature.setSubspecies(this.subspeciesIndex);
         entityCreature.applyVariant(this.variantIndex);
      }

      this.spawnCount++;
      this.saveEntityNBT();
      this.onSpawnEntity(this.entity);
   }

   public String getType() {
      return this.type;
   }

   public boolean usesSpirit() {
      return "pet".equals(this.getType()) || "mount".equals(this.getType());
   }

   public int getSpiritCost() {
      return this.summonSet.playerExt == null ? 0 : this.summonSet.playerExt.spiritCharge * this.getCreatureInfo().summonCost;
   }

   public void readFromNBT(CompoundTag nbtTagCompound) {
      if (nbtTagCompound.hasUUID("UUID")) {
         this.petEntryID = nbtTagCompound.getUUID("UUID");
      }

      if (nbtTagCompound.contains("Type")) {
         this.type = nbtTagCompound.getString("Type");
      }

      if (nbtTagCompound.contains("Active")) {
         this.active = nbtTagCompound.getBoolean("Active");
      }

      if (nbtTagCompound.contains("RespawnTime")) {
         this.respawnTime = nbtTagCompound.getInt("RespawnTime");
      }

      if (nbtTagCompound.contains("Respawning")) {
         this.isRespawning = nbtTagCompound.getBoolean("Respawning");
      }

      if (nbtTagCompound.contains("SpawningActive")) {
         this.spawningActive = nbtTagCompound.getBoolean("SpawningActive");
      }

      this.summonSet.readFromNBT(nbtTagCompound);
      if (nbtTagCompound.contains("EntityName")) {
         this.setEntityName(nbtTagCompound.getString("EntityName"));
      }

      if (!"familiar".equals(this.getType())) {
         if (nbtTagCompound.contains("SubspeciesID")) {
            this.setEntitySubspecies(Subspecies.getIndexFromOld(nbtTagCompound.getInt("SubspeciesID")));
            this.setEntityVariant(Variant.getIndexFromOld(nbtTagCompound.getInt("SubspeciesID")));
         }

         if (nbtTagCompound.contains("Subspecies")) {
            this.setEntitySubspecies(nbtTagCompound.getInt("Subspecies"));
         }

         if (nbtTagCompound.contains("Variant")) {
            this.setEntityVariant(nbtTagCompound.getInt("Variant"));
         }

         if (nbtTagCompound.contains("EntitySize")) {
            this.setEntitySize(nbtTagCompound.getDouble("EntitySize"));
         }

         if (nbtTagCompound.contains("Color")) {
            this.setColor(nbtTagCompound.getString("Color"));
         }
      }

      if (nbtTagCompound.contains("EntityNBT")) {
         this.entityNBT = nbtTagCompound.getCompound("EntityNBT");
      }

      this.loadEntityNBT();
   }

   public void writeToNBT(CompoundTag nbtTagCompound) {
      nbtTagCompound.putUUID("UUID", this.petEntryID);
      nbtTagCompound.putString("Type", this.getType());
      nbtTagCompound.putBoolean("Active", this.active);
      nbtTagCompound.putInt("RespawnTime", this.respawnTime);
      nbtTagCompound.putBoolean("Respawning", this.isRespawning);
      nbtTagCompound.putBoolean("SpawningActive", this.spawningActive);
      this.summonSet.writeToNBT(nbtTagCompound);
      if (this.usesSpirit()) {
         nbtTagCompound.putString("EntityName", this.entityName);
         nbtTagCompound.putInt("Subspecies", this.subspeciesIndex);
         nbtTagCompound.putInt("Variant", this.variantIndex);
         nbtTagCompound.putDouble("EntitySize", this.entitySize);
         nbtTagCompound.putString("Color", this.color);
      }

      this.saveEntityNBT();
      nbtTagCompound.put("EntityNBT", this.entityNBT);
   }

   public void saveEntityNBT() {
      if (this.entityNBT == null) {
         this.entityNBT = new CompoundTag();
      }

      this.entityNBT.putInt("MobLevel", this.getLevel());
      this.entityNBT.putInt("Experience", this.getExperience());
      if (this.entity instanceof BaseCreatureEntity) {
         BaseCreatureEntity baseCreatureEntity = (BaseCreatureEntity)this.entity;
         baseCreatureEntity.inventory.writeToNBT(this.entityNBT);
         CompoundTag extTagCompound = new CompoundTag();
         baseCreatureEntity.extraMobBehaviour.writeToNBT(extTagCompound);
         this.entityNBT.put("ExtraBehaviour", extTagCompound);
         if (this.entity instanceof AgeableCreatureEntity) {
            AgeableCreatureEntity ageableCreatureEntity = (AgeableCreatureEntity)this.entity;
            this.entityNBT.putInt("Age", ageableCreatureEntity.getGrowingAge());
         }

         if (this.entity.hasCustomName()) {
            this.entityName = this.entity.getCustomName().getString();
         }

         this.entity.writeToNBT(this.entityNBT);
      }
   }

   public void loadEntityNBT() {
      if (this.entityNBT != null) {
         if (this.entityNBT.contains("MobLevel")) {
            this.setLevel(this.entityNBT.getInt("MobLevel"));
         }

         if (this.entityNBT.contains("Experience")) {
            this.setExperience(this.entityNBT.getInt("Experience"));
         }

         if (this.entity instanceof BaseCreatureEntity) {
            BaseCreatureEntity baseCreatureEntity = (BaseCreatureEntity)this.entity;
            baseCreatureEntity.inventory.readFromNBT(this.entityNBT);
            if (this.entity instanceof AgeableCreatureEntity) {
               AgeableCreatureEntity ageableCreatureEntity = (AgeableCreatureEntity)this.entity;
               if (this.entityNBT.contains("Age")) {
                  ageableCreatureEntity.setGrowingAge(this.entityNBT.getInt("Age"));
               } else {
                  ageableCreatureEntity.setGrowingAge(0);
               }
            }
         }
      }
   }
}
