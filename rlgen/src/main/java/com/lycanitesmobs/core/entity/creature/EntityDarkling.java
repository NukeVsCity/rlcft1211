package com.lycanitesmobs.core.entity.creature;

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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.StealthGoal;


public class EntityDarkling extends TameableCreatureEntity implements Enemy {
   protected static final EntityDataAccessor<Integer> LATCH_TARGET = SynchedEntityData.defineId(EntityDarkling.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Float> LATCH_HEIGHT = SynchedEntityData.defineId(EntityDarkling.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Float> LATCH_ANGLE = SynchedEntityData.defineId(EntityDarkling.class, EntityDataSerializers.FLOAT);
   LivingEntity latchEntity = null;
   int latchEntityID = 0;
   double latchHeight = 0.5;
   double latchAngle = 90.0;

   public EntityDarkling(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.UNDEAD;
      this.hasAttackSound = true;
      this.setupMob();
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextPriorityGoalIndex++, new StealthGoal(this).setStealthTime(20).setStealthAttack(true).setStealthMove(true));
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(LATCH_TARGET, 0);
      builder.define(LATCH_HEIGHT, (float)this.latchHeight);
      builder.define(LATCH_ANGLE, (float)this.latchAngle);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide
         && this.hasAttackTarget()
         && !this.hasLatchTarget()
         && this.onGround()
         && !this.level().isClientSide
         && this.random.nextInt(10) == 0) {
         this.leap(6.0F, 0.6, this.getTarget());
      }

      if (this.hasLatchTarget()) {
         this.noPhysics = true;
         Vec3 latchPos = this.getFacingPositionDouble(
            this.getLatchTarget().getX(),
            this.getLatchTarget().getY() + (double)this.getLatchTarget().height * this.latchHeight,
            this.getLatchTarget().getZ(),
            (double)this.getLatchTarget().width * 0.5,
            this.latchAngle
         );
         this.set(latchPos.x, latchPos.y, latchPos.z);
         double distanceX = this.getLatchTarget().getX() - this.getX();
         double distanceZ = this.getLatchTarget().getZ() - this.getZ();
         this.yBodyRot = this.setYRot(-((float)Mth.atan2(distanceX, distanceZ)) * (180.0F / (float)Math.PI));
         if (!this.level().isClientSide) {
            if (this.getLatchTarget().isAlive() && !this.isInWater()) {
               this.setTarget(this.getLatchTarget());
               if (this.updateTick % 40L == 0L) {
                  float damage = this.getAttackDamage(1.0);
                  if (this.attackMelee(this.getLatchTarget(), (double)damage)) {
                     this.heal(damage * 2.0F);
                  }
               }
            } else {
               this.set(this.getLatchTarget().getX(), this.getLatchTarget().getY(), this.getLatchTarget().getZ());
               this.setLatchTarget(null);
               this.noPhysics = false;
            }
         } else {
            for (int i = 0; i < 2; i++) {
               this.level()
                  .addParticle(DustParticleOptions.REDSTONE, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            }
         }
      } else {
         this.noPhysics = false;
      }
   }

   public LivingEntity getLatchTarget() {
      try {
         if (this.level().isClientSide) {
            this.latchHeight = (double)((Float)this.entityData.get(LATCH_HEIGHT)).floatValue();
            this.latchAngle = (double)((Float)this.entityData.get(LATCH_ANGLE)).floatValue();
            int latchEntityID = (Integer)this.getEntityData().get(LATCH_TARGET);
            if (latchEntityID != this.latchEntityID) {
               this.latchEntity = null;
               this.latchEntityID = latchEntityID;
               if (latchEntityID != 0) {
                  Entity possilbeLatchEntity = this.level().getEntity(latchEntityID);
                  if (possilbeLatchEntity != null && possilbeLatchEntity instanceof LivingEntity) {
                     this.latchEntity = (LivingEntity)possilbeLatchEntity;
                  }
               }
            }
         }
      } catch (Exception var3) {
      }

      return this.latchEntity;
   }

   public void setLatchTarget(LivingEntity entity) {
      this.latchEntity = entity;
      if (!this.level().isClientSide) {
         if (entity == null) {
            this.getEntityData().set(LATCH_TARGET, 0);
         } else if (ObjectManager.getEffect("repulsion") != null && entity.hasEffect(ObjectManager.getEffect("repulsion"))) {
            this.latchEntity = null;
            this.getEntityData().set(LATCH_TARGET, 0);
         } else {
            this.getEntityData().set(LATCH_TARGET, entity.getId());
            this.latchHeight = 0.25 + 0.75 * this.getRandom().nextDouble();
            this.latchAngle = 360.0 * this.getRandom().nextDouble();
            this.entityData.set(LATCH_HEIGHT, (float)this.latchHeight);
            this.entityData.set(LATCH_ANGLE, (float)this.latchAngle);
         }
      }
   }

   public boolean hasLatchTarget() {
      return this.getLatchTarget() != null;
   }

   @Override
   public boolean canAttackClass(Class targetType) {
      return this.hasLatchTarget() ? false : super.canAttackClass(targetType);
   }

   @Override
   public boolean attackMelee(Entity target, double damageScale) {
      double targetKnockbackResistance = 0.0;
      if (target instanceof LivingEntity) {
         targetKnockbackResistance = ((LivingEntity)target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
         ((LivingEntity)target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
      }

      if (!super.attackMelee(target, damageScale)) {
         return false;
      } else {
         if (target instanceof LivingEntity) {
            ((LivingEntity)target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(targetKnockbackResistance);
         }

         if (!this.hasLatchTarget() && target instanceof LivingEntity && !this.isInWater()) {
            this.setLatchTarget((LivingEntity)target);
         }

         return true;
      }
   }

   @Override
   public boolean canEntityBeSeen(Entity target) {
      return target == this.getLatchTarget() ? true : super.hasLineOfSight(target);
   }

   @Override
   public boolean canClimb() {
      return true;
   }

   @Override
   public boolean petControlsEnabled() {
      return true;
   }

   @Override
   public int getNoBagSize() {
      return 0;
   }

   @Override
   public int getBagSize() {
      return this.creatureInfo.bagSize;
   }

   @Override
   public boolean canStealth() {
      if (this.level().isClientSide) {
         return false;
      } else {
         return this.isMoving() ? false : this.testLightLevel() <= 0;
      }
   }

   @Override
   public void startStealth() {
      if (this.level().isClientSide) {
         EnumParticleTypes particle = ParticleTypes.WITCH;
         double d0 = this.random.nextGaussian() * 0.02;
         double d1 = this.random.nextGaussian() * 0.02;
         double d2 = this.random.nextGaussian() * 0.02;

         for (int i = 0; i < 100; i++) {
            this.level()
               .addParticle(particle, this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), this.getY() + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()), this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), d0, d1, d2);
         }
      }

      super.startStealth();
   }

   @Override
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return type.equals("inWall") ? false : super.isDamageTypeApplicable(type, source, damage);
   }

   @Override
   public float getFallResistance() {
      return 10.0F;
   }
}
