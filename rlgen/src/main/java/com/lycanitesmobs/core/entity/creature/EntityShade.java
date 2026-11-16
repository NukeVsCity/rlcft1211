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
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import java.util.List;


public class EntityShade extends RideableCreatureEntity {
   public EntityShade(Level world) {
      super(LycEntities.type(EntityShade.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = true;
      this.hasJumpSound = true;
      this.canGrow = false;
      this.setupMob();
      this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1.0F);
      this.attackCooldownMax = 40;
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setSpeed(1.5));
   }

   @Override
   public void mountAbility(Entity rider) {
      if (!this.level().isClientSide) {
         if (!this.abilityToggled) {
            if (!(this.getStamina() < this.getStaminaCost())) {
               this.specialAttack();
               this.applyStaminaCost();
            }
         }
      }
   }

   @Override
   public float getStaminaCost() {
      return 100.0F;
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
   public double getMountedYOffset() {
      return (double)this.getBbHeight() * 0.85;
   }

   @Override
   public double getMountedZOffset() {
      return (double)this.getBbWidth() * 0.25;
   }

   @Override
   public boolean attackMelee(Entity target, double damageScale) {
      if (!super.attackMelee(target, damageScale)) {
         return false;
      } else {
         float leeching = (float)this.getEffectStrength(this.getAttackDamage(damageScale) / 4.0F);
         this.heal(leeching);
         if (this.getRandom().nextFloat() <= 0.1F) {
            this.specialAttack();
         }

         return true;
      }
   }

   public void specialAttack() {
      double distance = 5.0;
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
            if (this.getRider() instanceof Player && NeoForge.EVENT_BUS.post(new AttackEntityEvent((Player)this.getRider(), possibleTarget)).isCanceled()) {
               doDamage = false;
            }

            if (doDamage) {
               if (ObjectManager.getEffect("fear") != null) {
                  possibleTarget.addEffect(new MobEffectInstance(ObjectManager.getEffect("fear"), this.getEffectDuration(5), 1));
               } else {
                  possibleTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
               }
            }
         }
      }

      this.playAttackSound();
      this.triggerAttackCooldown();
   }

   @Override
   public boolean canBeTempted() {
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
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return type.equals("inWall") ? false : super.isDamageTypeApplicable(type, source, damage);
   }

   @Override
   public float getFallResistance() {
      return 10.0F;
   }
}
