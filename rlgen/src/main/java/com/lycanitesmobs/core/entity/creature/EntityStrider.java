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
import com.lycanitesmobs.api.IGroupHeavy;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;


public class EntityStrider extends RideableCreatureEntity implements IGroupHeavy {
   protected int pickupCooldown = 100;
   private int pickupTime = 0;

   public EntityStrider(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.spawnsOnLand = true;
      this.spawnsInWater = true;
      this.hasAttackSound = true;
      this.babySpawnChance = 0.0;
      this.canGrow = true;
      this.setupMob();
      this.hitAreaWidthScale = 1.5F;
      this.hitAreaHeightScale = 1.0F;
      this.setPathfindingMalus(PathType.WATER, 0.0F);
      this.setStepHeightCompat(4.0F);
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
         if (this.isTamed() && this.hasPickupEntity() && this.getPickupEntity() == this.getOwner()) {
            this.dropPickupEntity();
         }

         if (this.hasPickupEntity()) {
            ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.getPickupEntity());
            if (extendedEntity != null) {
               extendedEntity.setPickedUpByEntity(this);
            }

            if (this.isTamed() && !this.canAttackEntity(this.getPickupEntity())) {
               this.getPickupEntity().addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, this.getEffectDuration(5), 1));
            } else if (this.pickupTime++ % 40 == 0) {
               this.attackEntityAsMob(this.getPickupEntity(), 0.5);
               if (this.getPickupEntity() instanceof LivingEntity && ObjectManager.getEffect("penetration") != null) {
                  this.getPickupEntity().addEffect(new MobEffectInstance(ObjectManager.getEffect("penetration"), this.getEffectDuration(5), 1));
               }
            }
         } else {
            if (this.pickupCooldown > 0) {
               this.pickupCooldown--;
            }

            this.pickupTime = 0;
         }
      }
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

   @Override
   public float getAISpeedModifier() {
      if (this.isInWater()) {
         return 2.0F;
      } else {
         return this.waterContact() ? 1.5F : super.getAISpeedModifier();
      }
   }

   @Override
   public boolean isPushedByFluid() {
      return false;
   }

   @Override
   public float getBlockPathWeight(int x, int y, int z) {
      int waterWeight = 10;
      Block block = this.level().getBlockState(LycPos.of(x, y, z)).getBlock();
      if (block == Blocks.WATER) {
         return (super.getBlockPathWeight(x, y, z) + 1.0F) * (float)(waterWeight + 1);
      } else if (this.level().isRaining() && this.level().canSeeSky(LycPos.of(x, y, z))) {
         return (super.getBlockPathWeight(x, y, z) + 1.0F) * (float)(waterWeight + 1);
      } else if (this.getTarget() != null) {
         return super.getBlockPathWeight(x, y, z);
      } else {
         return this.waterContact() ? -999999.0F : super.getBlockPathWeight(x, y, z);
      }
   }

   @Override
   public BlockPos getWanderPosition(BlockPos wanderPosition) {
      BlockPos groundPos = wanderPosition.below();

      while (groundPos.getY() > 0 && !Material.of(this.level().getBlockState(groundPos)).isSolid()) {
         groundPos = groundPos.below();
      }

      return groundPos.above();
   }

   @Override
   public boolean attackMelee(Entity target, double damageScale) {
      if (!super.attackMelee(target, damageScale)) {
         return false;
      } else {
         if (target instanceof LivingEntity) {
            LivingEntity entityLivingBase = (LivingEntity)target;
            if (this.canPickupEntity(entityLivingBase) && this.hasLineOfSight(target)) {
               this.pickupEntity(entityLivingBase);
               this.pickupCooldown = 100;
            }
         }

         return true;
      }
   }

   @Override
   public void onDamage(DamageSource damageSrc, float damage) {
      if (this.hasPickupEntity() && !this.isTamed() && this.getRandom().nextFloat() <= 0.25F) {
         this.dropPickupEntity();
      }

      super.onDamage(damageSrc, damage);
   }

   @Override
   public boolean canBreatheUnderwater112() {
      return true;
   }

   @Override
   public float getFallResistance() {
      return 20.0F;
   }

   @Override
   public double[] getPickupOffset(Entity entity) {
      return new double[]{0.0, 5.5, 0.0};
   }

   @Override
   public boolean canPickupEntity(LivingEntity entity) {
      return !this.isTamed() && this.pickupCooldown > 0 ? false : super.canPickupEntity(entity);
   }

   @Override
   public void dropPickupEntity() {
      ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.getPickupEntity());
      if (extendedEntity != null) {
         extendedEntity.setPickedUpByEntity(null);
      }

      this.pickupEntity = null;
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
   public boolean petControlsEnabled() {
      return true;
   }
}
