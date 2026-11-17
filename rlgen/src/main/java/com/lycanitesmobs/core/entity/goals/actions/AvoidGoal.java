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


public class AvoidGoal extends EntityAIBase {
   private BaseCreatureEntity host;
   private LivingEntity avoidTarget;
   private double farSpeed = 1.0;
   private double nearSpeed = 1.2;
   private double farDistance = 4096.0;
   private double nearDistance = 49.0;
   private Class targetClass;
   private float distanceFromEntity = 6.0F;
   private Path pathEntity;

   public AvoidGoal(BaseCreatureEntity setHost) {
      this.host = setHost;
      this.setMutexBits(3);
   }

   public AvoidGoal setFarSpeed(double setSpeed) {
      this.farSpeed = setSpeed;
      return this;
   }

   public AvoidGoal setNearSpeed(double setSpeed) {
      this.nearSpeed = setSpeed;
      return this;
   }

   public AvoidGoal setFarDistance(double dist) {
      this.farDistance = dist * dist;
      return this;
   }

   public AvoidGoal setNearDistance(double dist) {
      this.nearDistance = dist * dist;
      return this;
   }

   public AvoidGoal setTargetClass(Class setTargetClass) {
      this.targetClass = setTargetClass;
      return this;
   }

   public boolean shouldExecute() {
      this.avoidTarget = this.host.getAvoidTarget();
      if (this.avoidTarget == null) {
         return false;
      } else if (!this.avoidTarget.isAlive()) {
         return false;
      } else if (this.targetClass != null && !this.targetClass.isAssignableFrom(this.avoidTarget.getClass())) {
         return false;
      } else {
         Vec3 avoidVector = RandomPositionGenerator.findRandomTargetAwayFrom(
            this.host,
            (int)Math.sqrt(this.farDistance),
            7,
            new Vec3(this.avoidTarget.getX(), this.avoidTarget.getY(), this.avoidTarget.getZ())
         );
         if (avoidVector == null) {
            return false;
         } else if (this.avoidTarget.distanceToSqr(avoidVector.x, avoidVector.y, avoidVector.z)
            < this.avoidTarget.distanceToSqr(this.host)) {
            return false;
         } else {
            if (!this.host.useDirectNavigator()) {
               this.pathEntity = this.host.getNavigation().createPath(avoidVector.x, avoidVector.y, avoidVector.z, 0);
               if (this.pathEntity == null) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public boolean shouldContinueExecuting() {
      return !this.host.useDirectNavigator() && this.host.getNavigation().isDone()
         ? false
         : !this.host.useDirectNavigator() || !this.host.directNavigator.atTargetPosition();
   }

   public void startExecuting() {
      if (!this.host.useDirectNavigator()) {
         this.host.getNavigation().moveTo(this.pathEntity, this.farSpeed);
      } else {
         this.host.directNavigator.setTargetPosition(this.avoidTarget, this.farSpeed);
      }
   }

   public void resetTask() {
      this.avoidTarget = null;
   }

   public void updateTask() {
      if ((double)this.host.distanceTo(this.avoidTarget) < this.nearDistance) {
         if (!this.host.useDirectNavigator()) {
            this.host.getNavigation().setSpeedModifier(this.nearSpeed);
         } else {
            this.host.directNavigator.speedModifier = this.nearSpeed;
         }
      } else if (!this.host.useDirectNavigator()) {
         this.host.getNavigation().setSpeedModifier(this.farSpeed);
      } else {
         this.host.directNavigator.speedModifier = this.farSpeed;
      }
   }
}
