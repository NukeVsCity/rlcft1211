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
import com.lycanitesmobs.api.IGroupBoss;
import com.lycanitesmobs.api.IGroupHeavy;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;


public class EntityRoa extends RideableCreatureEntity implements Enemy {
   protected int whirlpoolRange = 6;
   protected int whirlpoolEnergy = 0;
   protected int whirlpoolEnergyMax = 100;
   protected boolean whirlpoolRecharging = true;
   protected int mountedWhirlpool = 0;

   public EntityRoa(Level world) {
      super(LycEntities.type(EntityRoa.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.spawnsOnLand = false;
      this.spawnsInWater = true;
      this.hasAttackSound = true;
      this.babySpawnChance = 0.0;
      this.canGrow = true;
      this.setupMob();
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
   }

   @Override
   public void loadCreatureFlags() {
      this.whirlpoolRange = this.creatureInfo.getFlag("whirlpoolRange", this.whirlpoolRange);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         if (this.whirlpoolRecharging && ++this.whirlpoolEnergy >= this.whirlpoolEnergyMax) {
            this.whirlpoolRecharging = false;
         }

         this.whirlpoolEnergy = Math.min(this.whirlpoolEnergy, this.whirlpoolEnergyMax);
         if (this.canWhirlpool()) {
            for (Entity entity : this.getNearbyEntities(Entity.class, null, (double)this.whirlpoolRange)) {
               if (entity != this
                  && entity.getClass() != this.getClass()
                  && entity != this.getControllingPassenger()
                  && !(entity instanceof IGroupBoss)
                  && !(entity instanceof IGroupHeavy)) {
                  if (entity instanceof LivingEntity) {
                     LivingEntity entityLivingBase = (LivingEntity)entity;
                     if (entityLivingBase.hasEffect(ObjectManager.getEffect("weight"))
                        || !this.canAttackEntity(entityLivingBase)
                        || !entity.isInWater() && !this.spawnEventType.equalsIgnoreCase("sharknado")) {
                        continue;
                     }
                  }

                  ServerPlayer player = null;
                  if (entity instanceof ServerPlayer) {
                     player = (ServerPlayer)entity;
                     if (player.getAbilities().instabuild) {
                        continue;
                     }
                  }

                  double xDist = this.getX() - entity.getX();
                  double zDist = this.getZ() - entity.getZ();
                  double xzDist = Math.max((double)(float)Math.sqrt(xDist * xDist + zDist * zDist), 0.01);
                  double factor = 0.1;
                  double motionCap = 10.0;
                  if (entity.getDeltaMovement().x < motionCap
                     && entity.getDeltaMovement().x > -motionCap
                     && entity.getDeltaMovement().z < motionCap
                     && entity.getDeltaMovement().z > -motionCap) {
                     entity.push(xDist / xzDist * factor + entity.getDeltaMovement().x * factor, 0.0, zDist / xzDist * factor + entity.getDeltaMovement().z * factor);
                  }

                  if (player != null) {
                     player.connection.sendPacket(new SPacketEntityVelocity(entity));
                  }
               }
            }

            if (--this.whirlpoolEnergy <= 0) {
               this.whirlpoolRecharging = true;
            }
         }
      }

      if (this.mountedWhirlpool > 0) {
         this.mountedWhirlpool--;
      }
   }

   @Override
   public void riderEffects(LivingEntity rider) {
      rider.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 105, 1));
      super.riderEffects(rider);
   }

   @Override
   public boolean extraAnimation01() {
      return this.level().isClientSide ? super.extraAnimation01() : this.canWhirlpool();
   }

   public boolean canWhirlpool() {
      if (this.level().isClientSide) {
         return this.extraAnimation01();
      } else if ("sharknado".equals(this.spawnEventType)) {
         return true;
      } else if (!this.isInWater()) {
         return false;
      } else {
         return this.getControllingPassenger() != null && this.mountedWhirlpool > 0
            ? true
            : !this.whirlpoolRecharging && this.hasAttackTarget() && this.distanceTo(this.getTarget()) <= (float)(this.whirlpoolRange * 3);
      }
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
   public boolean isStrongSwimmer() {
      return true;
   }

   @Override
   public boolean canWalk() {
      return false;
   }

   @Override
   public boolean isFlying() {
      return "sharknado".equals(this.spawnEventType) ? true : super.isFlying();
   }

   @Override
   public double getMountedYOffset() {
      return (double)this.getBbHeight() * 0.25;
   }

   @Override
   public boolean canBreatheUnderwater112() {
      return true;
   }

   @Override
   public boolean canBreatheAir() {
      return "sharknado".equals(this.spawnEventType);
   }

   @Override
   public void mountAbility(Entity rider) {
      if (!this.level().isClientSide) {
         if (!(this.getStamina() < this.getStaminaCost())) {
            this.applyStaminaCost();
            this.mountedWhirlpool = 20;
         }
      }
   }

   @Override
   public float getStaminaCost() {
      return 2.0F;
   }

   @Override
   public int getStaminaRecoveryWarmup() {
      return 80;
   }

   @Override
   public float getStaminaRecoveryMax() {
      return 2.0F;
   }

   @Override
   public void onDismounted(Entity entity) {
      super.onDismounted(entity);
      if (entity != null && entity instanceof LivingEntity) {
         ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 100, 1));
      }
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
