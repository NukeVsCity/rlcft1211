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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;


public class EntityRaiko extends RideableCreatureEntity implements Enemy {
   protected int waterTime = 0;
   protected boolean wantsToLand;
   protected boolean isLanded;

   public EntityRaiko(Level world) {
      super(LycEntities.type(EntityRaiko.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = true;
      this.flySoundSpeed = 20;
      this.setupMob();
      this.setStepHeightCompat(1.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         if (this.isLanded) {
            this.wantsToLand = false;
            if (this.hasPickupEntity()
               || this.getControllingPassenger() != null
               || this.isLeashed()
               || this.isInWater()
               || !this.isTamed() && this.updateTick % 100L == 0L && this.getRandom().nextBoolean()) {
               this.leap(1.0, 1.0);
               this.isLanded = false;
            }
         } else if (this.wantsToLand) {
            if (!this.isLanded && this.isSafeToLand()) {
               this.isLanded = true;
            }
         } else if (!this.hasPickupEntity() && !this.hasAttackTarget() && this.updateTick % 100L == 0L && this.getRandom().nextBoolean()) {
            this.wantsToLand = true;
         }

         if (this.hasPickupEntity() || this.getControllingPassenger() != null || this.hasAttackTarget() || this.isInWater()) {
            this.wantsToLand = false;
         } else if (this.isTamed() && !this.isLeashed()) {
            this.wantsToLand = true;
         }
      }

      if (!this.level().isClientSide && this.getControllingPassenger() == null) {
         if (!this.isInWater()) {
            this.waterTime = 0;
            if (this.hasPickupEntity()) {
               ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.getPickupEntity());
               if (extendedEntity != null) {
                  extendedEntity.setPickedUpByEntity(this);
               }

               if (this.tickCount % 100 == 0 && this.getRandom().nextBoolean()) {
                  this.dropPickupEntity();
               }
            }
         } else {
            this.waterTime++;
            if (!this.hasPickupEntity() && this.getAirSupply() > 40) {
               if (this.hasAttackTarget()) {
                  if (this.waterTime >= 320) {
                     this.waterTime = 80;
                     this.leap(0.5, 2.0);
                  }
               } else if (this.waterTime >= 160) {
                  this.waterTime = 80;
                  this.leap(0.5, 2.0);
               }
            } else if (this.waterTime >= 40) {
               this.waterTime = 0;
               this.leap(0.5, 2.0);
            }
         }
      }
   }

   @Override
   public BlockPos getWanderPosition(BlockPos wanderPosition) {
      if (this.wantsToLand || !this.isLanded) {
         BlockPos groundPos = wanderPosition.below();

         while (groundPos.getY() > 0 && this.level().getBlockState(groundPos).getBlock() == Blocks.AIR) {
            groundPos = groundPos.below();
         }

         if (Material.of(this.level().getBlockState(groundPos)).isSolid()) {
            return groundPos.above();
         }
      }

      if (this.hasPickupEntity() && this.getPickupEntity() instanceof Player) {
         wanderPosition = LycPos.of(wanderPosition.getX(), this.restrictYHeightFromGround(wanderPosition, 6, 14), wanderPosition.getZ());
      }

      return wanderPosition;
   }

   @Override
   public double getFlightOffset() {
      if (!this.wantsToLand) {
         super.getFlightOffset();
      }

      return 0.0;
   }

   @Override
   public boolean rollWanderChance() {
      return this.isFlying() ? this.getRandom().nextDouble() <= 0.25 : this.getRandom().nextDouble() <= 0.008;
   }

   @Override
   public boolean attackMelee(Entity target, double damageScale) {
      if (!super.attackMelee(target, damageScale)) {
         return false;
      } else {
         if (target instanceof LivingEntity && this.getControllingPassenger() == null) {
            LivingEntity entityLivingBase = (LivingEntity)target;
            if (this.canPickupEntity(entityLivingBase)) {
               this.pickupEntity(entityLivingBase);
            }
         }

         return true;
      }
   }

   @Override
   public boolean canAttackEntity(LivingEntity targetEntity) {
      if (this.isTamed()) {
         return super.canAttackEntity(targetEntity);
      } else {
         ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(targetEntity);
         return extendedEntity != null && extendedEntity.pickedUpByEntity != null ? false : super.canAttackEntity(targetEntity);
      }
   }

   @Override
   public boolean isAggressive() {
      if (this.isTamed()) {
         return super.isAggressive();
      } else {
         return "".equals(this.spawnEventType) && this.level().isDay() && this.testLightLevel() >= 2 ? false : super.isAggressive();
      }
   }

   @Override
   public boolean isFlying() {
      return !this.isLanded || this.hasPickupEntity();
   }

   @Override
   public boolean isStrongSwimmer() {
      return false;
   }

   @Override
   public void pickupEntity(LivingEntity entity) {
      super.pickupEntity(entity);
      if (this.level().getBlockState(this.blockPosition()) != null && this.level().canSeeSky(this.blockPosition())) {
         this.leap(1.0, 2.0);
      }
   }

   @Override
   public void dropPickupEntity() {
      if (this.hasPickupEntity() && ObjectManager.getEffect("weight") != null) {
         this.getPickupEntity().addEffect(new MobEffectInstance(ObjectManager.getEffect("weight"), this.getEffectDuration(5), 1));
      }

      super.dropPickupEntity();
   }

   @Override
   public double[] getPickupOffset(Entity entity) {
      return entity != null ? new double[]{0.0, (double)(1.0F - entity.getBbHeight()), 0.0} : new double[]{0.0, 0.0, 0.0};
   }

   @Override
   public boolean petControlsEnabled() {
      return true;
   }

   @Override
   public float getFallResistance() {
      return 100.0F;
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
   public double getMountedYOffset() {
      return (double)this.getBbHeight() * 0.9;
   }

   @Override
   public void mountAbility(Entity rider) {
      if (!this.level().isClientSide) {
         if (!this.abilityToggled) {
            if (this.hasPickupEntity()) {
               this.dropPickupEntity();
            } else if (!(this.getStamina() < this.getStaminaCost())) {
               LivingEntity nearestTarget = this.getNearestEntity(LivingEntity.class, null, 4.0, false);
               if (this.canPickupEntity(nearestTarget)) {
                  this.pickupEntity(nearestTarget);
               }

               this.applyStaminaCost();
            }
         }
      }
   }

   @Override
   public float getStaminaCost() {
      return 20.0F;
   }

   @Override
   public int getStaminaRecoveryWarmup() {
      return 100;
   }

   @Override
   public float getStaminaRecoveryMax() {
      return 1.0F;
   }
}
