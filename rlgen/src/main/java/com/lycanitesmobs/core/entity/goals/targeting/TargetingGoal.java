package com.lycanitesmobs.core.entity.goals.targeting;

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
import com.google.common.base.Predicate;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.TargetSorterNearest;
import java.util.Collections;
import java.util.List;


public abstract class TargetingGoal extends EntityAIBase {
   protected BaseCreatureEntity host;
   protected LivingEntity target;
   protected Predicate<LivingEntity> targetSelector;
   protected Predicate<LivingEntity> allySelector;
   protected TargetSorterNearest nearestSorter;
   protected boolean checkSight = true;
   protected boolean nearbyOnly = false;
   protected boolean callForHelp = false;
   private int cantSeeTime;
   protected int cantSeeTimeMax = 60;
   protected double targetingRange = 0.0;
   private int targetSearchStatus;
   private int targetSearchDelay;

   public TargetingGoal(BaseCreatureEntity setHost) {
      this.host = setHost;
      this.targetSelector = entity -> {
         if (entity == null) {
            return false;
         } else if ((double)entity.distanceTo(this.host) > this.getTargetDistance() * (entity.isShiftKeyDown() && !this.host.trueSight ? 0.8F : 1.0)) {
            return false;
         } else if (!this.isEntityTargetable(entity, false)) {
            return false;
         } else if (this.shouldCheckSight() && !entity.isCurrentlyGlowing() && !this.host.hasLineOfSight(entity)) {
            return false;
         } else {
            if (this.shouldCheckSight() && !entity.isCurrentlyGlowing() && entity.isInvisible() && !this.host.trueSight) {
               if (!(entity instanceof Player)) {
                  return false;
               }

               double d0 = this.getTargetDistance();
               if (entity.isShiftKeyDown()) {
                  d0 *= 0.8F;
               }

               float f = ((Player)entity).getArmorCoverPercentage();
               if (f < 0.1F) {
                  f = 0.1F;
               }

               d0 *= (double)(0.7F * f);
               if ((double)entity.distanceTo(this.host) > d0) {
                  return false;
               }
            }

            return true;
         }
      };
      this.allySelector = entity -> {
         if (entity == null) {
            return false;
         } else if ((double)entity.distanceTo(this.host) > this.getTargetDistance()) {
            return false;
         } else {
            return !this.isAllyTarget(entity) ? false : !this.shouldCheckSight() || entity.isCurrentlyGlowing() || this.host.hasLineOfSight(entity);
         }
      };
      this.nearestSorter = new TargetSorterNearest(setHost);
   }

   public void startExecuting() {
      this.setTarget(this.target);
      this.targetSearchStatus = 0;
      this.targetSearchDelay = 0;
      this.cantSeeTime = 0;
   }

   public boolean shouldContinueExecuting() {
      if (this.getTarget() == null) {
         return false;
      } else if (!this.getTarget().isAlive()) {
         return false;
      } else if (this.shouldStopTargeting(this.getTarget())) {
         return false;
      } else {
         double distance = this.getTargetDistance() + 2.0;
         if ((double)this.host.distanceTo(this.getTarget()) > distance) {
            return false;
         } else {
            if (this.shouldCheckSight()) {
               if (this.host.getSensing().hasLineOfSight(this.getTarget())) {
                  this.cantSeeTime = 0;
               } else if (++this.cantSeeTime > this.cantSeeTimeMax) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public boolean shouldStopTargeting(LivingEntity target) {
      return false;
   }

   public void resetTask() {
      this.setTarget(null);
   }

   protected LivingEntity getTarget() {
      return null;
   }

   protected void setTarget(LivingEntity newTarget) {
   }

   public LivingEntity getNewTarget(double rangeX, double rangeY, double rangeZ) {
      LivingEntity newTarget = null;

      try {
         List<LivingEntity> possibleTargets = this.getPossibleTargets(LivingEntity.class, rangeX, rangeY, rangeZ);
         if (possibleTargets.isEmpty()) {
            return null;
         }

         Collections.sort(possibleTargets, this.nearestSorter);
         newTarget = possibleTargets.get(0);
      } catch (Exception var9) {
         LycanitesMobs.logWarning("", "An exception occurred when target selecting, this has been skipped to prevent a crash.");
         var9.printStackTrace();
      }

      return newTarget;
   }

   public <T extends LivingEntity> List<T> getPossibleTargets(Class<? extends T> clazz, double rangeX, double rangeY, double rangeZ) {
      return (List<T>)(List)this.host.level().getEntitiesOfClass((Class<LivingEntity>)clazz, this.host.getBoundingBox().inflate(rangeX, rangeY, rangeZ), (java.util.function.Predicate)this.targetSelector);
   }

   protected double getTargetDistance() {
      if (this.targetingRange > 0.0) {
         return this.targetingRange;
      } else {
         AttributeInstance attributeInstance = this.host.getAttribute(Attributes.FOLLOW_RANGE);
         return attributeInstance.getValue();
      }
   }

   public void callNearbyForHelp() {
      if (this.allySelector != null && this.target != null) {
         try {
            double targetDistance = this.getTargetDistance();

            for (Mob possibleAlly : this.host
               .level()
               .getEntitiesOfClass(BaseCreatureEntity.class, this.host.getBoundingBox().inflate(targetDistance, 4.0, targetDistance), this.allySelector)) {
               if (!possibleAlly.isAlliedTo(this.target) && possibleAlly.canAttackType(this.target.getType())) {
                  if (possibleAlly instanceof BaseCreatureEntity) {
                     BaseCreatureEntity possibleCreatureAlly = (BaseCreatureEntity)possibleAlly;
                     if (possibleCreatureAlly.getTarget() == null
                        && possibleCreatureAlly.canAttackEntity(this.target)
                        && possibleCreatureAlly.shouldCreatureGroupRevenge(this.target)) {
                        possibleCreatureAlly.setLastHurtByMob(this.target);
                     }
                  } else if (possibleAlly.getLastHurtByMob() == null) {
                     possibleAlly.setLastHurtByMob(this.target);
                  }
               }
            }
         } catch (Exception var7) {
            LycanitesMobs.logWarning("", "An exception occurred when calling for help, this has been skipped to prevent a crash.");
            var7.printStackTrace();
         }
      }
   }

   protected boolean isEntityTargetable(LivingEntity checkTarget, boolean targetCreative) {
      if (checkTarget == null) {
         return false;
      } else if (checkTarget == this.host) {
         return false;
      } else if (!checkTarget.isAlive()) {
         return false;
      } else {
         if (checkTarget instanceof Player) {
            if (!targetCreative && ((Player)checkTarget).isCreative()) {
               return false;
            }

            if (((Player)checkTarget).isSpectator()) {
               return false;
            }
         }

         if (!this.isValidTarget(checkTarget)) {
            return false;
         } else if (!this.host
            .positionNearHome(
               Mth.floor(checkTarget.getX()),
               Mth.floor(checkTarget.getY()),
               Mth.floor(checkTarget.getZ())
            )) {
            return false;
         } else if (this.shouldCheckSight() && !checkTarget.hasEffect(MobEffects.GLOWING) && !this.host.getSensing().hasLineOfSight(checkTarget)) {
            return false;
         } else {
            if (this.nearbyOnly) {
               if (--this.targetSearchDelay <= 0) {
                  this.targetSearchStatus = 0;
               }

               if (this.targetSearchStatus == 0) {
                  this.targetSearchStatus = this.isNearby(checkTarget) ? 1 : 2;
               }

               if (this.targetSearchStatus == 2) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   protected boolean shouldCheckSight() {
      return this.checkSight;
   }

   protected boolean isValidTarget(LivingEntity target) {
      return true;
   }

   protected boolean isAllyTarget(LivingEntity checkTarget) {
      if (checkTarget == null) {
         return false;
      } else if (checkTarget == this.host) {
         return false;
      } else if (!checkTarget.isAlive()) {
         return false;
      } else if (checkTarget instanceof Player) {
         return false;
      } else {
         if (checkTarget instanceof BaseCreatureEntity) {
            if (!((BaseCreatureEntity)checkTarget).isProtective(this.host)) {
               return false;
            }
         } else if (checkTarget.getClass() != this.host.getClass()) {
            return false;
         }

         return !this.shouldCheckSight() || this.host.getSensing().hasLineOfSight(checkTarget);
      }
   }

   private boolean isNearby(LivingEntity target) {
      this.targetSearchDelay = 10 + this.host.getRandom().nextInt(5);
      Path path = this.host.getNavigation().createPath(target, 0);
      if (path == null) {
         return false;
      } else {
         Node pathpoint = path.getEndNode();
         if (pathpoint == null) {
            return false;
         } else {
            int i = pathpoint.x - Mth.floor(target.getX());
            int j = pathpoint.z - Mth.floor(target.getZ());
            return (double)(i * i + j * j) <= 2.25;
         }
      }
   }
}
