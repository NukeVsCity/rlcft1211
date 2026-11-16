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
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ObjectLists;


public class EntityVapula extends TameableCreatureEntity implements Enemy {
   public int blockBreakRadius = 0;
   public float fireDamageAbsorbed = 0.0F;

   public EntityVapula(Level world) {
      super(LycEntities.type(EntityVapula.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = true;
      this.setupMob();
      this.setStepHeightCompat(1.0F);
      this.attackPhaseMax = 8;
      this.setAttackCooldownMax(60);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false).setMaxChaseDistanceSq(3.0F));
      this.tasks
         .addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75).setRange(18.0F).setMinChaseDistance(10.0F).setCheckSight(false));
   }

   @Override
   public void loadCreatureFlags() {
      this.blockBreakRadius = this.creatureInfo.getFlag("blockBreakRadius", this.blockBreakRadius);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide
         && this.isRareVariant()
         && !this.isPetType("familiar")
         && this.hasAttackTarget()
         && this.distanceTo(this.getTarget()) > 1.0F
         && this.getRandom().nextInt(20) == 0) {
         if (this.getY() - 1.0 > this.getTarget().getY()) {
            this.leap(6.0F, -1.0, this.getTarget());
         } else if (this.getY() + 1.0 < this.getTarget().getY()) {
            this.leap(6.0F, 1.0, this.getTarget());
         } else {
            this.leap(6.0F, 0.0, this.getTarget());
         }

         if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.blockBreakRadius > -1 && !this.isTamed()) {
            this.destroyArea((int)this.getX(), (int)this.getY(), (int)this.getZ(), 10.0F, true, this.blockBreakRadius);
         }
      }

      if (this.level().isClientSide && !CreatureManager.getInstance().config.disableBlockParticles) {
         for (int i = 0; i < 2; i++) {
            this.level()
               .addParticle(ParticleTypes.POOF, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
         }
      }
   }

   @Override
   public float getAISpeedModifier() {
      return this.hasAttackTarget() && this.getTarget() instanceof Silverfish ? 4.0F : super.getAISpeedModifier();
   }

   @Override
   public boolean attackMelee(Entity target, double damageScale) {
      if (!super.attackMelee(target, damageScale)) {
         return false;
      } else {
         if (target instanceof Silverfish) {
            target.discard();
         }

         return true;
      }
   }

   @Override
   public void attackRanged(Entity target, float range) {
      this.fireProjectile("crystalshard", target, range, 0.0F, new Vec3(0.0, 0.0, 0.0), 0.6F, 2.0F, 1.0F);
      this.nextAttackPhase();
      super.attackRanged(target, range);
   }

   @Override
   public int getRangedCooldown() {
      return this.getAttackPhase() < 7 ? super.getRangedCooldown() / 24 : super.getRangedCooldown();
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

   @Override
   public float getDamageModifier(DamageSource damageSrc) {
      if (damageSrc.getEntity() != null) {
         ItemStack heldItem = ItemStack.EMPTY;
         if (damageSrc.getEntity() instanceof LivingEntity) {
            LivingEntity entityLiving = (LivingEntity)damageSrc.getEntity();
            if (!entityLiving.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
               heldItem = entityLiving.getItemInHand(InteractionHand.MAIN_HAND);
            }
         }

         if (ObjectLists.isPickaxe(heldItem)) {
            return 3.0F;
         }
      }

      return super.getDamageModifier(damageSrc);
   }

   @Override
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      if (type.equals("cactus") || type.equals("inWall")) {
         return false;
      } else if (source.is(DamageTypeTags.IS_FIRE)) {
         this.fireDamageAbsorbed += damage;
         return false;
      } else {
         return super.isDamageTypeApplicable(type, source, damage);
      }
   }

   @Override
   public boolean canBurn() {
      return false;
   }

   @Override
   public boolean canBreatheUnderwater112() {
      return true;
   }
}
