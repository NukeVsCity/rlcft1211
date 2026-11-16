package com.lycanitesmobs.core.entity.creature;

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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import java.util.List;


public class EntityBarghest extends RideableCreatureEntity {
   protected boolean leapedAbilityQueued = false;
   protected boolean leapedAbilityReady = false;

   public EntityBarghest(Level world) {
      super(LycEntities.type(EntityBarghest.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = true;
      this.spreadFire = false;
      this.canGrow = true;
      this.babySpawnChance = 0.1;
      this.setupMob();
      this.setStepHeightCompat(1.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.isTamed() && this.onGround() && !this.level().isClientSide && this.hasAttackTarget() && this.random.nextInt(10) == 0) {
         this.leap(4.0F, 0.5, this.getTarget());
      }

      if (this.leapedAbilityQueued && !this.onGround() && !this.level().isClientSide) {
         this.leapedAbilityQueued = false;
         this.leapedAbilityReady = true;
      }

      if (this.leapedAbilityReady && this.onGround() && !this.level().isClientSide) {
         this.leapedAbilityReady = false;
         double distance = 4.0;
         List<LivingEntity> possibleTargets = this.level()
            .getEntitiesOfClass(
               LivingEntity.class,
               this.getBoundingBox().inflate(distance, distance, distance),
               possibleTargetx -> possibleTargetx.isAlive()
                     && possibleTargetx != this
                     && !this.hasIndirectPassenger(possibleTargetx)
                     && !this.isAlliedTo(possibleTargetx)
                     && this.canAttackClass(possibleTargetx.getClass())
                     && this.canAttackEntity(possibleTargetx)
            );
         if (!possibleTargets.isEmpty()) {
            for (LivingEntity possibleTarget : possibleTargets) {
               boolean doDamage = true;
               if (this.getRider() instanceof Player
                  && NeoForge.EVENT_BUS.post(new AttackEntityEvent((Player)this.getRider(), possibleTarget))) {
                  doDamage = false;
               }

               if (doDamage) {
                  if (ObjectManager.getEffect("weight") != null) {
                     possibleTarget.addEffect(new MobEffectInstance(ObjectManager.getEffect("weight"), this.getEffectDuration(5), 1));
                  } else {
                     possibleTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
                  }
               }
            }
         }

         this.playAttackSound();
      }
   }

   @Override
   public void riderEffects(LivingEntity rider) {
      if (rider.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
         rider.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
      }

      if (rider.hasEffect(ObjectManager.getEffect("weight"))) {
         rider.removeEffect(ObjectManager.getEffect("weight"));
      }
   }

   @Override
   public float getAISpeedModifier() {
      return !this.onGround() ? 2.0F : 1.0F;
   }

   @Override
   public double getMountedYOffset() {
      return (double)this.getBbHeight() * 0.8;
   }

   @Override
   public void leap(double distance, double leapHeight) {
      super.leap(distance, leapHeight);
      if (!this.level().isClientSide) {
         this.leapedAbilityQueued = true;
      }
   }

   @Override
   public void leap(float range, double leapHeight, Entity target) {
      super.leap(range, leapHeight, target);
      if (!this.level().isClientSide) {
         this.leapedAbilityQueued = true;
      }
   }

   @Override
   public void mountAbility(Entity rider) {
      if (this.onGround()) {
         if (!this.abilityToggled) {
            if (!(this.getStamina() < this.getStaminaCost())) {
               this.playJumpSound();
               this.leap(2.0, 1.5);
               this.applyStaminaCost();
            }
         }
      }
   }

   @Override
   public float getStaminaCost() {
      return 15.0F;
   }

   @Override
   public int getStaminaRecoveryWarmup() {
      return 100;
   }

   @Override
   public float getStaminaRecoveryMax() {
      return 1.0F;
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
   public boolean canClimb() {
      return true;
   }

   @Override
   public boolean petControlsEnabled() {
      return true;
   }

   @Override
   public float getFallResistance() {
      return 20.0F;
   }
}
