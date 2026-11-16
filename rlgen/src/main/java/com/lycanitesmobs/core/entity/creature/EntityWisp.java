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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;


public class EntityWisp extends TameableCreatureEntity {
   EntityWisp playPartner = null;

   public EntityWisp(Level world) {
      super(LycEntities.type(EntityWisp.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = false;
      this.setupMob();
      this.setStepHeightCompat(1.0F);
      this.setAttackCooldownMax(this.getRangedCooldown());
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75).setRange(14.0F).setMinChaseDistance(10.0F));
   }

   @Override
   public void tick() {
      super.tick();
      if (this.playPartner != null && !this.hasAttackTarget()) {
         this.faceEntity(this.playPartner, 360.0F, 360.0F);
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         if (!this.isPetType("familiar") && this.updateTick % 10L == 0L) {
            for (Object entityObj : this.getNearbyEntities(LivingEntity.class, null, 2.0)) {
               LivingEntity target = (LivingEntity)entityObj;
               if (target != this) {
                  if (target instanceof EntityWisp) {
                     if (this.playPartner == null) {
                        this.playPartner = (EntityWisp)target;
                        if (((EntityWisp)target).playPartner == null) {
                           ((EntityWisp)target).playPartner = this;
                        }
                     }
                  } else {
                     this.applyBuffs(target, 1, 1);
                     this.applyDebuffs(target, 1, 1);
                     if (target instanceof Zombie || target instanceof Skeleton) {
                        target.igniteForSeconds(1);
                     }

                     if (target instanceof BaseCreatureEntity) {
                        BaseCreatureEntity targetCreature = (BaseCreatureEntity)target;
                        if (targetCreature.daylightBurns()) {
                           targetCreature.igniteForSeconds(1);
                        }
                     }
                  }
               }
            }
         }

         if (this.playPartner == null) {
            this.setAttackCooldownMax(this.getRangedCooldown());
            if (this.updateTick % 40L == 0L) {
               for (Object entityObjx : this.getNearbyEntities(EntityWisp.class, null, 30.0)) {
                  EntityWisp target = (EntityWisp)entityObjx;
                  if (target != this && this.getTarget() != target) {
                     this.playPartner = target;
                     if (target.playPartner == null) {
                        target.playPartner = this;
                     }
                     break;
                  }
               }
            }
         } else if (!this.playPartner.isAlive() || this.getTarget() == this.playPartner || this.distanceTo(this.playPartner) >= 100.0F) {
            this.playPartner = null;
         } else if (this.hasAttackTarget()) {
            this.setAttackCooldownMax(this.getRangedCooldown());
            if (this.getPlayerOwner() == this.playPartner.getPlayerOwner()) {
               this.playPartner.setTarget(this.getTarget());
            }
         } else if (!this.isAttackOnCooldown() && !this.hasAttackTarget() && this.updateTick % (long)this.getRangedCooldown() == 0L) {
            this.setAttackCooldownMax(160);
            this.playPartner.setAttackCooldownMax(160);
            this.playPartner.triggerAttackCooldown();
            this.attackRanged(this.playPartner, this.distanceTo(this.playPartner));
         }
      }
   }

   @Override
   public boolean rollWanderChance() {
      if (this.playPartner == null) {
         return super.rollWanderChance();
      } else {
         return this.isAttackOnCooldown() ? false : this.getRandom().nextDouble() <= 0.002;
      }
   }

   @Override
   public void attackRanged(Entity target, float range) {
      this.fireProjectile("lightball", target, range, 0.0F, new Vec3(0.0, 0.0, 0.0), 0.5F, 1.0F, 1.0F);
      super.attackRanged(target, range);
   }

   @Override
   public boolean canAttackEntity(LivingEntity targetEntity) {
      if (targetEntity == this.playPartner) {
         return false;
      } else {
         return targetEntity instanceof EntityWisp && this.getPlayerOwner() == ((EntityWisp)targetEntity).getPlayerOwner()
            ? false
            : super.canAttackEntity(targetEntity);
      }
   }

   @Override
   public boolean isFlying() {
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

   public float getBrightness() {
      return 1.0F;
   }

   public int getBrightnessForRender() {
      return 15728880;
   }
}
