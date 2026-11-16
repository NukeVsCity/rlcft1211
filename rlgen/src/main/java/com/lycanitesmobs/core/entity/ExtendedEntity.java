package com.lycanitesmobs.core.entity;

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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.network.MessageEntityPerched;
import com.lycanitesmobs.core.network.MessageEntityPickedUp;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Vector3d;


public class ExtendedEntity {
   public static String[] FORCE_REMOVE_ENTITY_IDS;
   public static int FORCE_REMOVE_ENTITY_TICKS = 40;
   public LivingEntity entity;
   protected Map<String, Integer> projectileCooldownsPrimary = new HashMap<>();
   protected Map<String, Integer> projectileCooldownsSecondary = new HashMap<>();
   Vector3d lastSafePos;
   private boolean playerAllowFlyingSnapshot;
   private boolean playerIsFlyingSnapshot;
   public LivingEntity lastAttackedEntity;
   public int lastAttackedTime = 0;
   public Entity pickedUpByEntity;
   public Entity perchedByEntity;
   public boolean perchedEntityNoclip = false;
   public FearEntity fearEntity;
   boolean forceRemoveChecked = false;
   boolean forceRemove = false;
   int forceRemoveTicks = FORCE_REMOVE_ENTITY_TICKS;

   public static ExtendedEntity getForEntity(LivingEntity entity) {
      if (entity == null) {
         return null;
      } else {
         ExtendedEntity extendedEntity = entity.getData(com.lycanitesmobs.LycAttachments.EXTENDED_ENTITY);
         if (extendedEntity.entity != entity) {
            extendedEntity.setEntity(entity);
         }

         return extendedEntity;
      }
   }

   public void setEntity(LivingEntity entity) {
      this.entity = entity;
   }

   public LivingEntity getEntity() {
      return this.entity;
   }

   public void setLastAttackedEntity(LivingEntity target) {
      this.lastAttackedEntity = target;
      this.lastAttackedTime = this.entity.tickCount;
   }

   public void onUpdate() {
      if (this.entity != null) {
         for (String cooldownName : this.projectileCooldownsPrimary.keySet()) {
            int cooldownValue = this.projectileCooldownsPrimary.get(cooldownName);
            if (cooldownValue > 0) {
               this.projectileCooldownsPrimary.put(cooldownName, cooldownValue - 1);
            }
         }

         for (String cooldownNamex : this.projectileCooldownsSecondary.keySet()) {
            int cooldownValue = this.projectileCooldownsSecondary.get(cooldownNamex);
            if (cooldownValue > 0) {
               this.projectileCooldownsSecondary.put(cooldownNamex, cooldownValue - 1);
            }
         }

         if (!this.entity.level().isClientSide && FORCE_REMOVE_ENTITY_IDS != null && FORCE_REMOVE_ENTITY_IDS.length > 0 && !this.forceRemoveChecked) {
            LycanitesMobs.logDebug("ForceRemoveEntity", "Forced entity removal, checking: " + this.entity.getName());

            for (String forceRemoveID : FORCE_REMOVE_ENTITY_IDS) {
               if (forceRemoveID.equalsIgnoreCase(this.entity.getName())) {
                  this.forceRemove = true;
                  break;
               }
            }

            this.forceRemoveChecked = true;
         }

         if (this.forceRemove && this.forceRemoveTicks-- <= 0) {
            this.entity.discard();
         }

         if (this.lastSafePos == null) {
            this.lastSafePos = new Vector3d(this.entity.getX(), this.entity.getY(), this.entity.getZ());
         }

         if (!Material.of(this.entity.level().getBlockState(this.entity.blockPosition())).isSolid()) {
            this.lastSafePos
               .set(
                  Math.floor(this.entity.getX()) + 0.5, (double)this.entity.blockPosition().getY(), Math.floor(this.entity.getZ()) + 0.5
               );
         }

         if (this.fearEntity != null && !this.fearEntity.isAlive()) {
            this.fearEntity = null;
         }

         try {
            this.updatePickedUpByEntity();
         } catch (Exception var6) {
         }

         try {
            this.updatedPerchedByEntity();
         } catch (Exception var5) {
         }
      }
   }

   public void onDeath() {
      this.setPickedUpByEntity(null);
   }

   public void updatePickedUpByEntity() {
      if (this.pickedUpByEntity != null) {
         if (!this.entity.level().isClientSide) {
            if (!this.pickedUpByEntity.isAlive()) {
               this.setPickedUpByEntity(null);
               return;
            }

            if (this.pickedUpByEntity instanceof LivingEntity && ((LivingEntity)this.pickedUpByEntity).getHealth() <= 0.0F) {
               this.setPickedUpByEntity(null);
               return;
            }

            Holder<MobEffect> weight = ObjectManager.getEffect("weight");
            if (weight != null && this.entity.hasEffect(weight)) {
               this.setPickedUpByEntity(null);
               return;
            }

            if ((double)this.entity.distanceTo(this.pickedUpByEntity) > 32.0) {
               this.setPickedUpByEntity(null);
               return;
            }
         }

         if (this.pickedUpByEntity != null) {
            double[] pickupOffset = this.getPickedUpOffset();
            this.entity
               .setPos(
                  this.pickedUpByEntity.getX() + pickupOffset[0],
                  this.pickedUpByEntity.getY() + pickupOffset[1],
                  this.pickedUpByEntity.getZ() + pickupOffset[2]
               );
            LycMotion.setX(this.entity, this.pickedUpByEntity.getDeltaMovement().x);
            LycMotion.setY(this.entity, this.pickedUpByEntity.getDeltaMovement().y);
            LycMotion.setZ(this.entity, this.pickedUpByEntity.getDeltaMovement().z);
            this.entity.fallDistance = 0.0F;
            if (!this.entity.level().isClientSide && this.entity instanceof Player) {
               Player player = (Player)this.entity;
               player.getAbilities().flying = true;
               this.entity.noPhysics = true;
            }
         }
      }
   }

   public void setPickedUpByEntity(Entity pickedUpByEntity) {
      if (this.pickedUpByEntity != pickedUpByEntity && this.entity != null) {
         if (this.entity.getVehicle() != null) {
            this.entity.stopRiding();
         }

         this.pickedUpByEntity = pickedUpByEntity;
         if (!this.entity.level().isClientSide) {
            if (this.entity instanceof Player) {
               if (pickedUpByEntity != null) {
                  this.playerAllowFlyingSnapshot = ((Player)this.entity).getAbilities().allowFlying;
                  this.playerIsFlyingSnapshot = ((Player)this.entity).getAbilities().flying;
               } else {
                  ((Player)this.entity).getAbilities().flying = this.playerIsFlyingSnapshot;
                  this.entity.noPhysics = false;
               }
            }

            if (this.pickedUpByEntity != null && !(this.entity instanceof Player)) {
               double[] pickupOffset = this.getPickedUpOffset();
               this.entity
                  .attemptTeleport(
                     this.pickedUpByEntity.getX() + pickupOffset[0],
                     this.pickedUpByEntity.getY() + pickupOffset[1],
                     this.pickedUpByEntity.getZ() + pickupOffset[2]
                  );
            }

            MessageEntityPickedUp message = new MessageEntityPickedUp(this.entity, pickedUpByEntity);
            LycanitesMobs.packetHandler.sendToDimension(message, this.entity.dimension);
         }

         if (pickedUpByEntity == null) {
            if (this.lastSafePos != null) {
               this.entity.setPos(this.lastSafePos.getX(), this.lastSafePos.getY(), this.lastSafePos.getZ());
            }

            LycMotion.setX(this.entity, 0.0);
            LycMotion.setY(this.entity, 0.0);
            LycMotion.setZ(this.entity, 0.0);
            this.entity.fallDistance = 0.0F;
         }
      }
   }

   public double[] getPickedUpOffset() {
      double[] pickupOffset = new double[]{0.0, 0.0, 0.0};
      if (this.pickedUpByEntity instanceof BaseCreatureEntity) {
         pickupOffset = ((BaseCreatureEntity)this.pickedUpByEntity).getPickupOffset(this.entity);
      }

      return CreatureManager.getInstance().config.disablePickupOffsets && this.entity instanceof Player ? new double[]{0.0, 0.0, 0.0} : pickupOffset;
   }

   public boolean isPickedUp() {
      return this.pickedUpByEntity != null;
   }

   public boolean isFeared() {
      return this.pickedUpByEntity instanceof FearEntity;
   }

   public void setPerchedByEntity(Entity perchedByEntity) {
      if (this.perchedByEntity != null) {
         this.perchedByEntity.noPhysics = this.perchedEntityNoclip;
         if (this.perchedByEntity instanceof BaseCreatureEntity) {
            ((BaseCreatureEntity)this.perchedByEntity).setPerchTarget(null);
         }
      }

      this.perchedByEntity = perchedByEntity;
      if (perchedByEntity != null) {
         this.perchedEntityNoclip = perchedByEntity.noPhysics;
         perchedByEntity.noPhysics = true;
         if (perchedByEntity instanceof BaseCreatureEntity) {
            ((BaseCreatureEntity)perchedByEntity).setPerchTarget(this.entity);
         }
      }

      if (!this.entity.level().isClientSide) {
         MessageEntityPerched message = new MessageEntityPerched(this.entity, this.perchedByEntity);
         LycanitesMobs.packetHandler.sendToDimension(message, this.entity.dimension);
      }
   }

   public Entity getPerchedByEntity() {
      return this.perchedByEntity;
   }

   public Vec3 getPerchPosition() {
      double angle = Math.toRadians((double)this.entity.getYRot()) + 90.0;
      double xPerchPos = this.entity.getX();
      double zPerchPos = this.entity.getZ();
      double distance = (double)this.entity.getBbWidth() * 0.7;
      if (distance != 0.0) {
         xPerchPos += distance * -Math.sin(angle);
         zPerchPos += distance * Math.cos(angle);
      }

      return new Vec3(xPerchPos, this.entity.getY() + (double)this.entity.getBbHeight() * 0.78, zPerchPos);
   }

   public void updatedPerchedByEntity() {
      Entity perchedByEntity = this.getPerchedByEntity();
      if (perchedByEntity != null) {
         Vec3 perchPosition = this.getPerchPosition();
         perchedByEntity.setPos(perchPosition.x, perchPosition.y, perchPosition.z);
         LycMotion.setX(perchedByEntity, this.entity.getDeltaMovement().x);
         LycMotion.setY(perchedByEntity, this.entity.getDeltaMovement().y);
         LycMotion.setZ(perchedByEntity, this.entity.getDeltaMovement().z);
         perchedByEntity.setYRot(this.entity.getYRot());
         perchedByEntity.noPhysics = true;
      }
   }

   public int getProjectileCooldown(int type, String projectileName) {
      if (type == 1) {
         return !this.projectileCooldownsPrimary.containsKey(projectileName) ? 0 : this.projectileCooldownsPrimary.get(projectileName);
      } else {
         return !this.projectileCooldownsSecondary.containsKey(projectileName) ? 0 : this.projectileCooldownsSecondary.get(projectileName);
      }
   }

   public void setProjectileCooldown(int type, String projectileName, int cooldown) {
      if (type == 1) {
         this.projectileCooldownsPrimary.put(projectileName, cooldown);
      }

      this.projectileCooldownsSecondary.put(projectileName, cooldown);
   }

   public void readNBT(CompoundTag nbtTagCompound) {
   }

   public void writeNBT(CompoundTag nbtTagCompound) {
   }
}
