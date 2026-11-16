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
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import java.util.List;


public class MateGoal extends EntityAIBase {
   private AgeableCreatureEntity host;
   private AgeableCreatureEntity partner;
   private double speed = 1.0;
   private Class targetClass;
   private int mateTime;
   private int mateTimeMax = 60;
   private double mateDistance = 9.0;

   public MateGoal(AgeableCreatureEntity setHost) {
      this.host = setHost;
      this.targetClass = this.host.getClass();
      this.setMutexBits(3);
   }

   public MateGoal setSpeed(double setSpeed) {
      this.speed = setSpeed;
      return this;
   }

   public MateGoal setMateDistance(double setDouble) {
      this.mateDistance = setDouble * setDouble;
      return this;
   }

   public MateGoal setTargetClass(Class setTargetClass) {
      this.targetClass = setTargetClass;
      return this;
   }

   public MateGoal setMateTime(int setTime) {
      this.mateTimeMax = setTime;
      return this;
   }

   public boolean shouldExecute() {
      if (!this.host.canMate()) {
         return false;
      } else {
         this.partner = this.getPartner();
         return this.partner != null;
      }
   }

   public boolean shouldContinueExecuting() {
      return this.partner != null && this.partner.isAlive() && this.partner.isInLove() && this.mateTime < this.mateTimeMax;
   }

   public void resetTask() {
      this.partner = null;
      this.mateTime = 0;
   }

   public void updateTask() {
      this.host.getLookControl().setLookAt(this.partner, 10.0F, (float)this.host.getVerticalFaceSpeed());
      if (!this.host.useDirectNavigator()) {
         this.host.getNavigation().moveTo(this.partner, this.speed);
      } else {
         this.host
            .directNavigator
            .setTargetPosition(LycPos.of((int)this.partner.getX(), (int)this.partner.getY(), (int)this.partner.getZ()), this.speed);
      }

      if (this.host.distanceToSqr(this.partner) < this.mateDistance + this.host.getPhysicalRange()) {
         this.mateTime++;
         if (this.mateTime >= this.mateTimeMax) {
            this.host.procreate(this.partner);
         }
      }
   }

   private AgeableCreatureEntity getPartner() {
      float distance = 8.0F;
      List possibleMates = this.host
         .level()
         .getEntitiesOfClass(this.targetClass, this.host.getBoundingBox().inflate((double)distance, (double)distance, (double)distance));
      double closestDistance = Double.MAX_VALUE;
      AgeableCreatureEntity newMate = null;

      for (LivingEntity nextEntity : possibleMates) {
         if (nextEntity instanceof AgeableCreatureEntity) {
            AgeableCreatureEntity testMate = (AgeableCreatureEntity)nextEntity;
            if (this.host.canBreedWith(testMate) && (double)this.host.distanceTo(testMate) < closestDistance) {
               newMate = testMate;
               closestDistance = (double)this.host.distanceTo(testMate);
            }
         }
      }

      return newMate;
   }
}
