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
import com.lycanitesmobs.api.Targeting;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;


public class DefendOwnerGoal extends TargetingGoal {
   private TameableCreatureEntity tamedHost;

   public DefendOwnerGoal(TameableCreatureEntity setHost) {
      super(setHost);
      this.tamedHost = setHost;
      this.setMutexBits(1);
   }

   public DefendOwnerGoal setSightCheck(boolean setSightCheck) {
      this.checkSight = setSightCheck;
      return this;
   }

   public DefendOwnerGoal setOnlyNearby(boolean setNearby) {
      this.nearbyOnly = setNearby;
      return this;
   }

   public DefendOwnerGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
      this.cantSeeTimeMax = setCantSeeTimeMax;
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

   protected Entity getOwner() {
      return this.tamedHost.getPlayerOwner();
   }

   @Override
   protected boolean isValidTarget(LivingEntity target) {
      if (target == null) {
         return false;
      } else if (!this.tamedHost.isTamed() || this.getOwner() == null) {
         return false;
      } else if (this.tamedHost.isPassive()) {
         return false;
      } else if (!this.host.isAggressive()) {
         return false;
      } else if (this.host.isAlliedTo(target)) {
         return false;
      } else {
         if (target instanceof Mob) {
            Mob Mob = (Mob)target;
            if (!this.host.canAttackType(EntityType.PLAYER)) {
               return false;
            }
         }

         if (!Targeting.isValidTarget(this.host, target)) {
            return false;
         } else if (target instanceof Enemy && !(target instanceof OwnableEntity) && !(target instanceof BaseCreatureEntity)) {
            return true;
         } else if (target instanceof BaseCreatureEntity && ((BaseCreatureEntity)target).isHostileTo(this.getOwner())) {
            return true;
         } else {
            return target instanceof Mob && ((Mob)target).getTarget() == this.getOwner()
               ? true
               : target.getLastHurtByMob() == this.getOwner();
         }
      }
   }

   public boolean shouldExecute() {
      this.target = null;
      if (!this.tamedHost.isTamed()) {
         return false;
      } else if (this.tamedHost.isPassive()) {
         return false;
      } else if (!this.host.isAggressive()) {
         return false;
      } else {
         double distance = this.getTargetDistance() - (double)this.host.getBbWidth();
         double heightDistance = 4.0 - (double)this.host.getBbHeight();
         if (this.host.useDirectNavigator()) {
            heightDistance = distance;
         }

         this.target = this.getNewTarget(distance, heightDistance, distance);
         if (this.callForHelp) {
            this.callNearbyForHelp();
         }

         return this.target != null;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.isValidTarget(this.getTarget()) ? false : super.shouldContinueExecuting();
   }
}
