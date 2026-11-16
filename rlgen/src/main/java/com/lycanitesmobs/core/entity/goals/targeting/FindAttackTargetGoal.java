package com.lycanitesmobs.core.entity.goals.targeting;

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
import com.lycanitesmobs.core.entity.CreatureRelationshipEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class FindAttackTargetGoal extends TargetingGoal {
   private List<Class<? extends Entity>> targetClasses = new ArrayList<>();
   protected boolean targetPlayers;
   private boolean requirePack = false;
   protected boolean tameTargeting = false;

   public FindAttackTargetGoal(BaseCreatureEntity setHost) {
      super(setHost);
      this.setMutexBits(1);
   }

   public FindAttackTargetGoal setCheckSight(boolean bool) {
      this.checkSight = bool;
      return this;
   }

   public FindAttackTargetGoal addTargets(Class<? extends Entity>... targets) {
      this.targetClasses.addAll(Arrays.asList(targets));

      for (Class<? extends Entity> targetType : targets) {
         this.host.setHostileTo(targetType);
         if (targetType.isAssignableFrom(Player.class)) {
            this.targetPlayers = true;
         }
      }

      return this;
   }

   public FindAttackTargetGoal setOnlyNearby(boolean setNearby) {
      this.nearbyOnly = setNearby;
      return this;
   }

   public FindAttackTargetGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
      this.cantSeeTimeMax = setCantSeeTimeMax;
      return this;
   }

   public FindAttackTargetGoal setRange(double range) {
      this.targetingRange = range;
      return this;
   }

   public FindAttackTargetGoal setHelpCall(boolean setHelp) {
      this.callForHelp = setHelp;
      return this;
   }

   public FindAttackTargetGoal setTameTargetting(boolean setTargetting) {
      this.tameTargeting = setTargetting;
      return this;
   }

   public FindAttackTargetGoal requiresPack() {
      this.requirePack = true;
      return this;
   }

   @Override
   protected LivingEntity getTarget() {
      return this.host.getTarget();
   }

   @Override
   protected void setTarget(LivingEntity newTarget) {
      this.host.setTarget(newTarget);
   }

   @Override
   protected boolean isValidTarget(LivingEntity target) {
      if (!this.targetClasses.isEmpty()) {
         boolean isTargetClass = false;

         for (Class<? extends Entity> targetClass : this.targetClasses) {
            if (targetClass.isAssignableFrom(target.getClass())) {
               isTargetClass = true;
               break;
            }
         }

         if (!isTargetClass) {
            return false;
         }
      }

      if (!this.tameTargeting && this.host.isTamed()) {
         return false;
      } else if (!this.host.canAttackClass(target.getClass())) {
         return false;
      } else if (!this.host.canAttackEntity(target)) {
         return false;
      } else {
         CreatureRelationshipEntry relationshipEntry = this.host.relationships.getEntry(target);
         return relationshipEntry != null && !relationshipEntry.canHunt() ? false : !this.requirePack || this.host.isInPack();
      }
   }

   public boolean shouldExecute() {
      if (this.host.isAggressive() && !this.host.hasFixateTarget()) {
         if (this.targetPlayers) {
            if (this.host.updateTick % 5L != 0L) {
               return false;
            }
         } else if (this.host.updateTick % 40L != 0L) {
            return false;
         }

         this.target = null;
         double distance = this.getTargetDistance();
         double heightDistance = 4.0 + (double)this.host.getBbHeight();
         if (this.host.useDirectNavigator()) {
            heightDistance = distance;
         }

         this.target = this.getNewTarget(distance, heightDistance, distance);
         if (this.target != null && this.host.rollAttackTargetChance(this.target)) {
            if (this.callForHelp) {
               this.callNearbyForHelp();
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldStopTargeting(LivingEntity target) {
      return !this.isValidTarget(target);
   }

   @Override
   public LivingEntity getNewTarget(double rangeX, double rangeY, double rangeZ) {
      if (this.targetPlayers) {
         LivingEntity newTarget = null;

         try {
            List<? extends Player> players = this.host.level().getPlayers(Player.class, this.targetSelector);
            if (!players.isEmpty()) {
               List<Player> possibleTargets = new ArrayList<>();

               for (Player player : players) {
                  if (this.isValidTarget(player)) {
                     possibleTargets.add(player);
                  }
               }

               if (!possibleTargets.isEmpty()) {
                  Collections.sort(possibleTargets, this.nearestSorter);
                  newTarget = (LivingEntity)possibleTargets.get(0);
               }
            }
         } catch (Exception var12) {
            LycanitesMobs.logWarning("", "An exception occurred when player target selecting, this has been skipped to prevent a crash.");
            var12.printStackTrace();
         }

         if (newTarget != null) {
            return newTarget;
         }
      }

      return this.host.updateTick % 40L == 0L ? super.getNewTarget(rangeX, rangeY, rangeZ) : null;
   }
}
