package com.lycanitesmobs.core.entity.goals.actions;

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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;


public class AttackMeleeGoal extends EntityAIBase {
   private BaseCreatureEntity host;
   private LivingEntity attackTarget;
   private Path pathToTarget;
   private double speed = 1.0;
   private Class targetClass;
   private boolean longMemory = true;
   private int attackTime;
   private double attackRange = 0.5;
   private float maxChaseDistance = 1024.0F;
   private double damageScale = 1.0;
   public boolean enabled = true;
   protected int phase = -1;
   private int failedPathFindingPenalty;
   private int failedPathFindingPenaltyMax = 0;
   private int failedPathFindingPenaltyPlayerMax = 0;
   private int repathTime;

   public AttackMeleeGoal(BaseCreatureEntity setHost) {
      this.host = setHost;
      this.setMutexBits(3);
   }

   public AttackMeleeGoal setSpeed(double setSpeed) {
      this.speed = setSpeed;
      return this;
   }

   public AttackMeleeGoal setDamageScale(double scale) {
      this.damageScale = scale;
      return this;
   }

   public AttackMeleeGoal setTargetClass(Class setTargetClass) {
      this.targetClass = setTargetClass;
      return this;
   }

   public AttackMeleeGoal setLongMemory(boolean setMemory) {
      this.longMemory = setMemory;
      return this;
   }

   public AttackMeleeGoal setRange(double range) {
      this.attackRange = range;
      return this;
   }

   public AttackMeleeGoal setMaxChaseDistanceSq(float distance) {
      this.maxChaseDistance = distance * distance;
      return this;
   }

   public AttackMeleeGoal setMaxChaseDistance(float distance) {
      this.maxChaseDistance = distance;
      return this;
   }

   public AttackMeleeGoal setMissRate(int rate) {
      this.failedPathFindingPenaltyMax = rate;
      return this;
   }

   public AttackMeleeGoal setEnabled(boolean setEnabled) {
      this.enabled = setEnabled;
      return this;
   }

   public AttackMeleeGoal setPhase(int phase) {
      this.phase = phase;
      return this;
   }

   public boolean shouldExecute() {
      if (!this.enabled) {
         return false;
      } else if (this.phase >= 0 && this.phase != this.host.getBattlePhase()) {
         return false;
      } else if (this.host.hasPickupEntity() && !this.host.canAttackWithPickup()) {
         return false;
      } else {
         this.attackTarget = this.host.getTarget();
         if (this.attackTarget == null) {
            return false;
         } else if (!this.attackTarget.isAlive()) {
            return false;
         } else if (this.host.distanceToSqr(this.attackTarget.getX(), this.attackTarget.getBoundingBox().minY, this.attackTarget.getZ())
            > (double)this.maxChaseDistance) {
            return false;
         } else if (this.targetClass != null && !this.targetClass.isAssignableFrom(this.attackTarget.getClass())) {
            return false;
         } else if (--this.repathTime <= 0) {
            if (!this.host.useDirectNavigator()) {
               if (this.host.isCurrentlyFlying()) {
                  this.pathToTarget = this.host
                     .getNavigation()
                     .createPath(
                        this.attackTarget.getX(),
                        this.attackTarget.getBoundingBox().minY + this.host.getFlightOffset(),
                        this.attackTarget.getZ()
                     , 0);
               } else {
                  this.pathToTarget = this.host.getNavigation().createPath(this.attackTarget, 0);
               }

               this.repathTime = 4 + this.host.getRandom().nextInt(7);
               return this.pathToTarget != null;
            } else {
               return this.host
                  .directNavigator
                  .setTargetPosition(
                     LycPos.of(
                        (double)((int)this.attackTarget.getX()),
                        (double)((int)this.attackTarget.getY()) + this.host.getFlightOffset(),
                        (double)((int)this.attackTarget.getZ())
                     ),
                     this.speed
                  );
            }
         } else {
            return true;
         }
      }
   }

   public boolean shouldContinueExecuting() {
      if (!this.enabled) {
         return false;
      } else if (this.phase >= 0 && this.phase != this.host.getBattlePhase()) {
         return false;
      } else {
         this.attackTarget = this.host.getTarget();
         if (this.attackTarget == null) {
            return false;
         } else if (this.targetClass != null && !this.targetClass.isAssignableFrom(this.attackTarget.getClass())) {
            return false;
         } else if (this.host.isAlive() && this.attackTarget.isAlive()) {
            if (this.host.distanceToSqr(this.attackTarget.getX(), this.attackTarget.getBoundingBox().minY, this.attackTarget.getZ())
               > (double)this.maxChaseDistance) {
               return false;
            } else {
               if (!this.longMemory) {
                  if (!this.host.useDirectNavigator() && this.host.getNavigation().isDone()) {
                     return false;
                  }

                  if (this.host.useDirectNavigator() && (this.host.directNavigator.atTargetPosition() || !this.host.directNavigator.isTargetPositionValid())) {
                     return false;
                  }
               }

               return this.host
                  .positionNearHome(
                     Mth.floor(this.attackTarget.getX()),
                     Mth.floor(this.attackTarget.getY()),
                     Mth.floor(this.attackTarget.getZ())
                  );
            }
         } else {
            return false;
         }
      }
   }

   public void startExecuting() {
      if (!this.host.useDirectNavigator()) {
         this.host.getNavigation().moveTo(this.pathToTarget, this.speed);
      } else if (this.attackTarget != null) {
         this.host
            .directNavigator
            .setTargetPosition(
               LycPos.of(
                  (int)this.attackTarget.getX(),
                  (int)(this.attackTarget.getBoundingBox().minY + this.host.getFlightOffset()),
                  (int)this.attackTarget.getZ()
               ),
               this.speed
            );
      }

      this.repathTime = 0;
   }

   public void resetTask() {
      this.host.getNavigation().stop();
      this.host.directNavigator.clearTargetPosition(1.0);
      this.attackTarget = null;
   }

   public void updateTask() {
      this.host.getLookControl().setLookAt(this.attackTarget, 30.0F, 30.0F);
      if (this.longMemory || this.host.getSensing().hasLineOfSight(this.attackTarget)) {
         if (!this.host.useDirectNavigator() && --this.repathTime <= 0) {
            this.repathTime = this.failedPathFindingPenalty + 4 + this.host.getRandom().nextInt(7);
            if (this.host.isCurrentlyFlying()) {
               this.host
                  .getNavigation()
                  .moveTo(
                     this.attackTarget.getX(),
                     this.attackTarget.getBoundingBox().minY + this.host.getFlightOffset(),
                     this.attackTarget.getZ(),
                     this.speed
                  );
            } else {
               this.host.getNavigation().moveTo(this.attackTarget, this.speed);
            }

            if (this.host.getNavigation().getPath() != null) {
               Node finalPathPoint = this.host.getNavigation().getPath().getEndNode();
               if (finalPathPoint != null
                  && this.attackTarget
                        .distanceToSqr((double)finalPathPoint.x, (double)finalPathPoint.y, (double)finalPathPoint.z)
                     < 1.0) {
                  this.failedPathFindingPenalty = 0;
               } else {
                  this.failedPathFindingPenalty = this.failedPathFindingPenalty + this.getFailedPathFindingPenalty();
               }
            } else {
               this.failedPathFindingPenalty = this.failedPathFindingPenalty + this.getFailedPathFindingPenalty();
            }
         } else if (this.host.useDirectNavigator()) {
            this.host
               .directNavigator
               .setTargetPosition(
                  LycPos.of(
                     (int)this.attackTarget.getX(),
                     (int)(this.attackTarget.getBoundingBox().minY + this.host.getFlightOffset()),
                     (int)this.attackTarget.getZ()
                  ),
                  this.speed
               );
         }
      }

      LycanitesMobs.logDebug(
         "Attack",
         "Attack range: "
            + this.host.getMeleeAttackRange(this.attackTarget, this.attackRange)
            + "/"
            + this.host.distanceToSqr(this.attackTarget.getX(), this.attackTarget.getBoundingBox().minY, this.attackTarget.getZ())
      );
      if (this.host.distanceToSqr(this.attackTarget.getX(), this.attackTarget.getBoundingBox().minY, this.attackTarget.getZ())
         <= this.host.getMeleeAttackRange(this.attackTarget, this.attackRange)) {
         if (--this.attackTime <= 0) {
            this.attackTime = this.host.getMeleeCooldown();
            if (!this.host.getMainHandItem().isEmpty()) {
               this.host.swing(InteractionHand.MAIN_HAND);
            }

            this.host.attackMelee(this.attackTarget, this.damageScale);
         }

         double d0 = this.host.getX() - this.attackTarget.getX();
         double d1 = this.host.getZ() - this.attackTarget.getZ();
         float f = (float)(Math.atan2(d1, d0) * 180.0 / Math.PI) + 90.0F;
         f = Mth.wrapDegrees(f - this.host.getYRot());
         if (f < -30.0F) {
            f = -30.0F;
         }

         if (f > 30.0F) {
            f = 30.0F;
         }

         this.host.setYRot(this.host.getYRot() + (f));
      }
   }

   public int getFailedPathFindingPenalty() {
      return !(this.attackTarget instanceof Player) && !this.host.isTamed() ? this.failedPathFindingPenaltyMax : this.failedPathFindingPenaltyPlayerMax;
   }
}
