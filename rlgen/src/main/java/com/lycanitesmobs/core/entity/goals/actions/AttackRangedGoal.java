package com.lycanitesmobs.core.entity.goals.actions;

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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;


public class AttackRangedGoal extends EntityAIBase {
   private final BaseCreatureEntity host;
   private LivingEntity attackTarget;
   private int attackTime;
   private int attackStamina = 0;
   private int attackStaminaMax = 0;
   public boolean attackOnCooldown = false;
   private int staminaRecoverRate = 1;
   private int staminaDrainRate = 1;
   private double speed = 1.0;
   private int chaseTime;
   private int chaseTimeMax = -1;
   private float range = 6.0F;
   private float attackDistance = 5.0F;
   private float minChaseDistance = 3.0F;
   private float flyingHeight = 2.0F;
   private boolean longMemory = true;
   private boolean checkSight = true;
   private boolean mountedAttacking = true;
   public boolean enabled = true;
   private int repathTimeMax = 20;
   private int repathTime;

   public AttackRangedGoal(BaseCreatureEntity setHost) {
      this.host = setHost;
      this.setMutexBits(3);
   }

   public AttackRangedGoal setSpeed(double setSpeed) {
      this.speed = setSpeed;
      return this;
   }

   public AttackRangedGoal setLongMemory(boolean setLongMemory) {
      this.longMemory = setLongMemory;
      return this;
   }

   public AttackRangedGoal setCheckSight(boolean setCheckSight) {
      this.checkSight = setCheckSight;
      return this;
   }

   public AttackRangedGoal setStaminaTime(int setInt) {
      this.attackStaminaMax = setInt;
      this.attackStamina = this.attackStaminaMax;
      return this;
   }

   public AttackRangedGoal setStaminaRecoverRate(int rate) {
      this.staminaRecoverRate = rate;
      return this;
   }

   public AttackRangedGoal setStaminaDrainRate(int rate) {
      this.staminaDrainRate = rate;
      return this;
   }

   public AttackRangedGoal setRange(float setRange) {
      this.range = setRange;
      this.attackDistance = setRange;
      return this;
   }

   public AttackRangedGoal setMinChaseDistance(float setMinDist) {
      this.minChaseDistance = setMinDist;
      return this;
   }

   public AttackRangedGoal setChaseTime(int setChaseTime) {
      this.chaseTimeMax = setChaseTime;
      return this;
   }

   public AttackRangedGoal setFlyingHeight(float setFlyingHeight) {
      this.flyingHeight = setFlyingHeight;
      return this;
   }

   public AttackRangedGoal setMountedAttacking(boolean bool) {
      this.mountedAttacking = bool;
      return this;
   }

   public AttackRangedGoal setEnabled(boolean setEnabled) {
      this.enabled = setEnabled;
      return this;
   }

   public boolean shouldExecute() {
      if (this.attackStaminaMax > 0 && this.attackOnCooldown) {
         this.attackStamina = this.attackStamina + this.staminaRecoverRate;
         if (this.attackStamina >= this.attackStaminaMax) {
            this.attackOnCooldown = false;
         }
      }

      if (!this.enabled) {
         return false;
      } else if (this.host.hasPickupEntity() && !this.host.canAttackWithPickup()) {
         return false;
      } else {
         if (!this.mountedAttacking && this.host instanceof RideableCreatureEntity) {
            RideableCreatureEntity rideableHost = (RideableCreatureEntity)this.host;
            if (rideableHost.getControllingPassenger() instanceof Player) {
               return false;
            }
         }

         LivingEntity possibleAttackTarget = this.host.getTarget();
         if (possibleAttackTarget == null) {
            return false;
         } else if (!possibleAttackTarget.isAlive()) {
            return false;
         } else {
            this.attackTarget = possibleAttackTarget;
            return true;
         }
      }
   }

   public boolean shouldContinueExecuting() {
      if (!this.longMemory) {
         if (!this.host.useDirectNavigator() && !this.host.getNavigation().isDone()) {
            return this.shouldExecute();
         }

         if (this.host.useDirectNavigator() && this.host.directNavigator.targetPosition == null) {
            return this.shouldExecute();
         }
      }

      if (!this.enabled) {
         return false;
      } else {
         LivingEntity possibleAttackTarget = this.host.getTarget();
         if (possibleAttackTarget == null) {
            return false;
         } else if (!possibleAttackTarget.isAlive()) {
            return false;
         } else {
            this.attackTarget = possibleAttackTarget;
            return true;
         }
      }
   }

   public void resetTask() {
      this.attackTarget = null;
      this.chaseTime = 0;
      this.attackTime = -1;
   }

   public void updateTask() {
      boolean fixated = this.host.hasFixateTarget() && this.host.getFixateTarget() == this.attackTarget;
      double distance = (double)(this.host.distanceTo(this.attackTarget) - this.attackTarget.getBbWidth() / 2.0F);
      boolean hasSight = fixated || this.host.getSensing().hasLineOfSight(this.attackTarget);
      float flyingHeightOffset = this.flyingHeight;
      if (hasSight && this.chaseTimeMax >= 0 && !fixated) {
         this.chaseTime++;
      } else {
         this.chaseTime = 0;
      }

      if (!hasSight) {
         flyingHeightOffset = 0.0F;
      }

      if (distance <= (double)this.minChaseDistance || this.chaseTimeMax >= 0 && distance <= (double)this.attackDistance && this.chaseTime >= this.chaseTimeMax
         )
       {
         if (!this.host.useDirectNavigator()) {
            this.host.getNavigation().stop();
         } else {
            this.host.directNavigator.clearTargetPosition(1.0);
         }
      } else if (--this.repathTime <= 0) {
         this.repathTime = this.repathTimeMax;
         BlockPos targetPosition = this.attackTarget.blockPosition();
         if (this.host.isFlying()) {
            targetPosition = targetPosition.offset(0.0, (double)flyingHeightOffset, 0.0);
         }

         if (!this.host.useDirectNavigator()) {
            this.host
               .getNavigation()
               .moveTo((double)targetPosition.getX(), (double)targetPosition.getY(), (double)targetPosition.getZ(), this.speed);
         } else {
            this.host.directNavigator.setTargetPosition(targetPosition, this.speed);
         }
      }

      this.host.getLookControl().setLookAt(this.attackTarget, 30.0F, 30.0F);
      if (this.attackStaminaMax > 0) {
         if (!this.attackOnCooldown) {
            this.attackStamina = this.attackStamina - this.staminaDrainRate;
            if (this.attackStamina <= 0) {
               this.attackOnCooldown = true;
            }
         } else {
            this.attackStamina = this.attackStamina + this.staminaRecoverRate;
            if (this.attackStamina >= this.attackStaminaMax) {
               this.attackOnCooldown = false;
            }
         }
      } else if (this.attackOnCooldown) {
         this.attackOnCooldown = false;
      }

      if (!this.attackOnCooldown) {
         if (--this.attackTime == 0) {
            if (distance > (double)this.attackDistance || this.checkSight && !hasSight) {
               return;
            }

            float rangeFactor = (float)distance / this.range;
            float outerRangeFactor = rangeFactor;
            if (rangeFactor < 0.1F) {
               outerRangeFactor = 0.1F;
            }

            if (outerRangeFactor > 1.0F) {
               outerRangeFactor = 1.0F;
            }

            this.host.attackRanged(this.attackTarget, outerRangeFactor);
            this.attackTime = this.host.getRangedCooldown();
         } else if (this.attackTime < 0) {
            this.attackTime = this.host.getRangedCooldown();
         }
      }
   }
}
