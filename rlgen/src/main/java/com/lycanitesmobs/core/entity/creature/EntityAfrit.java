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
import com.lycanitesmobs.core.entity.EntityItemCustom;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;


public class EntityAfrit extends TameableCreatureEntity implements Enemy {
   protected boolean wantsToLand;
   protected boolean isLanded;

   public EntityAfrit(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.spawnsOnLand = true;
      this.spawnsInWater = true;
      this.isLavaCreature = true;
      this.hasAttackSound = false;
      this.flySoundSpeed = 20;
      this.setupMob();
      this.setStepHeightCompat(1.0F);
      this.setPathfindingMalus(PathType.LAVA, 0.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks
         .addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75).setRange(14.0F).setMinChaseDistance(5.0F).setCheckSight(false));
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         if (this.isLanded) {
            this.wantsToLand = false;
            if (!this.isSitting() && this.updateTick % 100L == 0L && this.getRandom().nextBoolean()) {
               this.leap(1.0, 1.0);
               this.isLanded = false;
            }
         } else if (this.wantsToLand) {
            if (!this.isLanded && this.isSafeToLand()) {
               this.isLanded = true;
            }
         } else if (this.updateTick % 100L == 0L && this.getRandom().nextBoolean()) {
            this.wantsToLand = true;
         }
      }

      if (this.level().isClientSide && !this.hasPerchTarget()) {
         for (int i = 0; i < 2; i++) {
            this.level()
               .addParticle(ParticleTypes.SMOKE, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            this.level()
               .addParticle(ParticleTypes.FLAME, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
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

      return super.getWanderPosition(wanderPosition);
   }

   @Override
   public double getFlightOffset() {
      if (!this.wantsToLand) {
         super.getFlightOffset();
      }

      return 0.0;
   }

   @Override
   public void attackRanged(Entity target, float range) {
      this.fireProjectile("scorchfireball", target, range, 0.0F, new Vec3(0.0, 0.0, 0.0), 0.8F, 2.0F, 6.0F);
      super.attackRanged(target, range);
   }

   @Override
   public boolean isFlying() {
      return !this.isLanded;
   }

   @Override
   public boolean isStrongSwimmer() {
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
   public boolean canBurn() {
      return false;
   }

   @Override
   public boolean canBreatheUnderlava() {
      return true;
   }

   @Override
   public float getFallResistance() {
      return 100.0F;
   }

   @Override
   public void applyDropEffects(EntityItemCustom entityitem) {
      entityitem.setCanBurn(false);
   }

   @Override
   public float getDamageModifier(DamageSource damageSrc) {
      return damageSrc.is(DamageTypeTags.IS_FIRE) ? 0.0F : super.getDamageModifier(damageSrc);
   }
}
