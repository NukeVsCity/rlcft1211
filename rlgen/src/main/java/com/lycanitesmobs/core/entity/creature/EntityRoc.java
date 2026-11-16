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
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;


public class EntityRoc extends RideableCreatureEntity implements Enemy {
   public AttackMeleeGoal attackAI;
   public boolean creeperDropping = true;
   private int creeperDropCooldown = 0;

   public EntityRoc(Level world) {
      super(LycEntities.type(EntityRoc.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = true;
      this.flySoundSpeed = 20;
      this.setupMob();
      this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1.0F);
   }

   @Override
   protected void initEntityAI() {
      if (this.creeperDropping) {
         this.targetTasks.addTask(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(Creeper.class));
      }

      super.initEntityAI();
      this.attackAI = new AttackMeleeGoal(this).setLongMemory(false);
      this.tasks.addTask(this.nextCombatGoalIndex++, this.attackAI);
   }

   @Override
   public void loadCreatureFlags() {
      this.creeperDropping = this.creatureInfo.getFlag("creeperDropping", this.creeperDropping);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide && this.getControllingPassenger() == null) {
         this.attackAI.setEnabled(this.hasPickupEntity() ? this.getPickupEntity() instanceof Creeper : this.creeperDropCooldown <= 0);
         if (this.creeperDropCooldown > 0) {
            this.creeperDropCooldown--;
         }

         if (this.hasPickupEntity()) {
            ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.getPickupEntity());
            if (extendedEntity != null) {
               extendedEntity.setPickedUpByEntity(this);
            }

            if (this.getPickupEntity() instanceof Creeper && this.hasAttackTarget() && !(this.getTarget() instanceof Creeper)) {
               double distance = new Vec3(this.getX(), 0.0, this.getZ())
                  .distanceTo(new Vec3(this.getTarget().getX(), 0.0, this.getTarget().getZ()));
               if (distance <= 2.0 && this.getY() > this.getTarget().getY()) {
                  this.getPickupEntity().setLastHurtByMob(this.getTarget());
                  this.dropPickupEntity();
                  this.creeperDropCooldown = 120;
               }
            }

            if (this.hasPickupEntity() && this.tickCount % 100 == 0 && this.getRandom().nextBoolean()) {
               if (this.getPickupEntity() instanceof Player) {
                  for (int distToGround = 0; distToGround < 8; distToGround++) {
                     Block searchBlock = this.level()
                        .getBlockState(LycPos.of((int)this.getX(), (int)this.getY() - distToGround, (int)this.getZ()))
                        .getBlock();
                     if (searchBlock != null && searchBlock != Blocks.AIR) {
                        this.dropPickupEntity();
                        this.leap(1.0, 2.0);
                        break;
                     }
                  }
               } else if (!(this.getPickupEntity() instanceof Creeper)) {
                  this.dropPickupEntity();
               }
            }
         }
      }

      if (!this.level().isClientSide && this.getControllingPassenger() == null && this.getPickupEntity() instanceof Creeper) {
         ((Creeper)this.getPickupEntity()).setTarget(null);
      }
   }

   @Override
   public void riderEffects(LivingEntity rider) {
      if (rider.hasEffect(MobEffects.WEAKNESS)) {
         rider.removeEffect(MobEffects.WEAKNESS);
      }

      if (rider.hasEffect(MobEffects.DIG_SLOWDOWN)) {
         rider.removeEffect(MobEffects.DIG_SLOWDOWN);
      }
   }

   @Override
   public double getFlightOffset() {
      return this.hasPickupEntity() ? 5.0 : super.getFlightOffset();
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

            if (entityLivingBase instanceof Creeper) {
               entityLivingBase.setLastHurtByMob(null);
               ((Creeper)entityLivingBase).setTarget(null);
               this.setTarget(null);
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
         if (extendedEntity != null && extendedEntity.pickedUpByEntity != null) {
            return false;
         } else if (!this.creeperDropping && targetEntity instanceof Creeper) {
            return false;
         } else if (this.hasPickupEntity() && targetEntity instanceof Creeper) {
            return false;
         } else {
            return this.creeperDropCooldown > 0 ? false : super.canAttackEntity(targetEntity);
         }
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
      return true;
   }

   @Override
   public void pickupEntity(LivingEntity entity) {
      super.pickupEntity(entity);
      if (this.level().getBlockState(this.blockPosition()) != null && this.level().canSeeSky(this.blockPosition())) {
         this.leap(0.5, 4.0);
      }
   }

   @Override
   public double[] getPickupOffset(Entity entity) {
      return entity != null ? new double[]{0.0, (double)(1.0F - entity.getBbHeight()), 0.0} : new double[]{0.0, 0.0, 0.0};
   }

   @Override
   public boolean canPickupEntity(LivingEntity entity) {
      return this.creeperDropCooldown <= 0 && !this.hasPickupEntity() ? super.canPickupEntity(entity) : false;
   }

   @Override
   public boolean canAttackWithPickup() {
      return this.getPickupEntity() instanceof Creeper ? true : super.canAttackWithPickup();
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
   public BlockPos getWanderPosition(BlockPos wanderPosition) {
      return this.hasPickupEntity() && this.getPickupEntity() instanceof Player
         ? LycPos.of(wanderPosition.getX(), this.restrictYHeightFromGround(wanderPosition, 6, 14), wanderPosition.getZ())
         : super.getWanderPosition(wanderPosition);
   }

   @Override
   public void mountAbility(Entity rider) {
      if (!this.level().isClientSide) {
         if (!this.abilityToggled) {
            if (this.hasPickupEntity()) {
               if (this.getPickupEntity() instanceof Creeper) {
                  ((Creeper)this.getPickupEntity()).ignite();
               }

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
