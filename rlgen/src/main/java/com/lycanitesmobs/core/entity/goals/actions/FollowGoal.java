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


public abstract class FollowGoal extends EntityAIBase {
   BaseCreatureEntity host;
   double speed = 1.0;
   Class targetClass;
   private int updateRate;
   double strayDistance = 4.0;
   double lostDistance = 32.0;
   double behindDistance = 0.0;

   public FollowGoal(BaseCreatureEntity setHost) {
      this.host = setHost;
      this.targetClass = this.host.getClass();
      this.setMutexBits(1);
   }

   public abstract Entity getTarget();

   public abstract void setTarget(Entity var1);

   public FollowGoal setSpeed(double setSpeed) {
      this.speed = setSpeed;
      return this;
   }

   public FollowGoal setTargetClass(Class setTargetClass) {
      this.targetClass = setTargetClass;
      return this;
   }

   public FollowGoal setStrayDistance(double setDist) {
      this.strayDistance = setDist;
      return this;
   }

   public FollowGoal setLostDistance(double setDist) {
      this.lostDistance = setDist;
      return this;
   }

   public FollowGoal setFollowBehind(double setDist) {
      this.behindDistance = setDist;
      return this;
   }

   public boolean shouldExecute() {
      Entity target = this.getTarget();
      if (target == null || target == this.host) {
         return false;
      } else if (!target.isAlive()) {
         return false;
      } else {
         double distance = (double)this.host.distanceTo(target);
         return distance > this.lostDistance && this.lostDistance != 0.0 ? false : !(distance <= this.strayDistance) || this.strayDistance == 0.0;
      }
   }

   public boolean shouldContinueExecuting() {
      Entity target = this.getTarget();
      if (target == null) {
         return false;
      } else if (!target.isAlive()) {
         return false;
      } else {
         double distance = (double)this.host.distanceTo(target);
         if (distance > this.lostDistance && this.lostDistance != 0.0) {
            this.setTarget(null);
            return false;
         } else if (distance <= this.strayDistance / 2.0 && this.strayDistance != 0.0) {
            return false;
         } else {
            this.onTargetDistance(distance, target);
            return this.getTarget() != null;
         }
      }
   }

   public void startExecuting() {
      this.updateRate = 0;
   }

   public void updateTask() {
      if (this.updateRate-- <= 0) {
         Entity target = this.getTarget();
         if (target instanceof Player) {
            this.updateRate = 10;
         } else {
            this.updateRate = 60;
         }

         if (!this.host.useDirectNavigator()) {
            if (this.behindDistance != 0.0 && target instanceof BaseCreatureEntity) {
               BlockPos pos = ((BaseCreatureEntity)target).getFacingPosition(-this.behindDistance);
               this.host.getNavigation().moveTo((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), this.speed);
            } else {
               this.host.getNavigation().moveTo(target, this.speed);
            }
         } else if (this.behindDistance != 0.0 && target instanceof BaseCreatureEntity) {
            BlockPos pos = ((BaseCreatureEntity)target).getFacingPosition(-this.behindDistance);
            this.host.directNavigator.setTargetPosition(pos, this.speed);
         } else {
            this.host
               .directNavigator
               .setTargetPosition(LycPos.of((int)target.getX(), (int)target.getY(), (int)target.getZ()), this.speed);
         }
      }
   }

   public void resetTask() {
      this.host.clearMovement();
   }

   public void onTargetDistance(double distance, Entity followTarget) {
   }
}
