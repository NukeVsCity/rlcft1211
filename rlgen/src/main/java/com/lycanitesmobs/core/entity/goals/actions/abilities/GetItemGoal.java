package com.lycanitesmobs.core.entity.goals.actions.abilities;

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
import com.google.common.base.Predicate;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.TargetSorterNearest;
import java.util.Collections;
import java.util.List;


public class GetItemGoal extends EntityAIBase {
   private BaseCreatureEntity host;
   private ItemEntity target;
   private Predicate<ItemEntity> targetSelector;
   private TargetSorterNearest targetSorter;
   private double distanceMax = 1024.0;
   double speed = 1.0;
   private boolean checkSight = true;
   private int cantSeeTime = 0;
   protected int cantSeeTimeMax = 60;
   private int updateRate = 0;
   private int recheckTime = 0;
   public boolean tamedLooting = true;

   public GetItemGoal(BaseCreatureEntity setHost) {
      this.setMutexBits(3);
      this.host = setHost;
      this.targetSelector = input -> true;
      this.targetSorter = new TargetSorterNearest(setHost);
   }

   public GetItemGoal setDistanceMax(double setDouble) {
      this.distanceMax = setDouble * setDouble;
      return this;
   }

   public GetItemGoal setSpeed(double setDouble) {
      this.speed = setDouble;
      return this;
   }

   public GetItemGoal setCheckSight(boolean setBool) {
      this.checkSight = setBool;
      return this;
   }

   public GetItemGoal setTamedLooting(boolean bool) {
      this.tamedLooting = bool;
      return this;
   }

   public boolean shouldExecute() {
      if (!this.host.canPickupItems()) {
         return false;
      } else if (this.recheckTime++ < 40) {
         return false;
      } else {
         this.recheckTime = 0;
         if (!this.tamedLooting && this.host instanceof TameableCreatureEntity && this.host.isTamed()) {
            return false;
         } else {
            double heightDistance = 4.0;
            if (this.host.useDirectNavigator()) {
               heightDistance = this.distanceMax;
            }

            List<ItemEntity> possibleTargets = this.host
               .level()
               .getEntitiesOfClass(
                  ItemEntity.class, this.host.getBoundingBox().inflate(this.distanceMax, heightDistance, this.distanceMax), this.targetSelector
               );
            if (possibleTargets.isEmpty()) {
               return false;
            } else {
               Collections.sort(possibleTargets, this.targetSorter);
               this.target = possibleTargets.get(0);
               return this.shouldContinueExecuting();
            }
         }
      }
   }

   public boolean shouldContinueExecuting() {
      if (this.target == null) {
         return false;
      } else if (!this.target.isAlive()) {
         return false;
      } else {
         double distance = (double)this.host.distanceTo(this.target);
         if (distance > this.distanceMax) {
            return false;
         } else {
            if (this.checkSight) {
               if (this.host.getSensing().hasLineOfSight(this.target)) {
                  this.cantSeeTime = 0;
               } else if (++this.cantSeeTime > this.cantSeeTimeMax) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public void resetTask() {
      this.target = null;
      this.host.clearMovement();
   }

   public void startExecuting() {
      this.updateRate = 0;
   }

   public void updateTask() {
      if (this.updateRate-- <= 0) {
         this.updateRate = 20;
         if (!this.host.useDirectNavigator()) {
            this.host.getNavigation().moveTo(this.target, this.speed);
         } else {
            this.host
               .directNavigator
               .setTargetPosition(LycPos.of((int)this.target.getX(), (int)this.target.getY(), (int)this.target.getZ()), this.speed);
         }
      }
   }
}
