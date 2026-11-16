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
import com.google.common.base.Predicate;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.EntityItemCustom;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import java.util.List;


public class EntitySalamander extends RideableCreatureEntity implements Enemy {
   public EntitySalamander(Level world) {
      super(LycEntities.type(EntitySalamander.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.spawnsOnLand = true;
      this.spawnsInWater = true;
      this.isLavaCreature = true;
      this.hasAttackSound = true;
      this.hasJumpSound = true;
      this.canGrow = true;
      this.babySpawnChance = 0.01;
      this.solidCollision = false;
      this.setupMob();
      this.setStepHeightCompat(1.0F);
      this.setPathfindingMalus(PathType.LAVA, 0.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
   }

   @Override
   public void aiStep() {
      super.aiStep();
   }

   @Override
   public void riderEffects(LivingEntity rider) {
      rider.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 105, 1));
      if (rider.hasEffect(ObjectManager.getEffect("penetration"))) {
         rider.removeEffect(ObjectManager.getEffect("penetration"));
      }

      if (rider.isOnFire()) {
         rider.clearFire();
      }
   }

   @Override
   public float getAISpeedModifier() {
      return this.lavaContact() ? 2.0F : 1.0F;
   }

   @Override
   public float getBlockPathWeight(int x, int y, int z) {
      int waterWeight = 10;
      BlockPos pos = LycPos.of(x, y, z);
      return this.level().getBlockState(pos).getBlock() == Blocks.LAVA
         ? (super.getBlockPathWeight(x, y, z) + 1.0F) * (float)(waterWeight + 1)
         : super.getBlockPathWeight(x, y, z);
   }

   @Override
   public boolean isPushedByFluid() {
      return false;
   }

   @Override
   public double getMountedYOffset() {
      return (double)this.getBbHeight() * 0.85;
   }

   public void specialAttack() {
      double distance = 5.0;
      List<LivingEntity> possibleTargets = this.level()
         .getEntitiesOfClass(
            LivingEntity.class,
            this.getBoundingBox().inflate(distance, distance, distance),
            new Predicate<LivingEntity>() {
               public boolean apply(LivingEntity possibleTarget) {
                  return possibleTarget.isAlive()
                     && possibleTarget != EntitySalamander.this
                     && !EntitySalamander.this.hasIndirectPassenger(possibleTarget)
                     && !EntitySalamander.this.isAlliedTo(possibleTarget)
                     && EntitySalamander.this.canAttackClass(possibleTarget.getClass())
                     && EntitySalamander.this.canAttackEntity(possibleTarget);
               }
            }
         );
      if (!possibleTargets.isEmpty()) {
         for (LivingEntity possibleTarget : possibleTargets) {
            boolean doDamage = true;
            if (this.getRider() instanceof Player && NeoForge.EVENT_BUS.post(new AttackEntityEvent((Player)this.getRider(), possibleTarget))) {
               doDamage = false;
            }

            if (doDamage) {
               possibleTarget.igniteForSeconds(5);
            }
         }
      }

      this.playAttackSound();
      this.triggerAttackCooldown();
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
   public void onDismounted(Entity entity) {
      super.onDismounted(entity);
      if (entity != null && entity instanceof LivingEntity) {
         ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 1));
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
   public boolean canBurn() {
      return false;
   }

   @Override
   public boolean canBreatheUnderlava() {
      return true;
   }

   @Override
   public boolean canBreatheAir() {
      return true;
   }

   @Override
   public float getFallResistance() {
      return 100.0F;
   }

   @Override
   public float getDamageModifier(DamageSource damageSrc) {
      return damageSrc.is(DamageTypeTags.IS_FIRE) ? 0.0F : super.getDamageModifier(damageSrc);
   }

   @Override
   public void applyDropEffects(EntityItemCustom entityItem) {
      entityItem.setCanBurn(false);
   }

   public float getBrightness() {
      return 1.0F;
   }

   public int getBrightnessForRender() {
      return 15728880;
   }

   @Override
   public boolean petControlsEnabled() {
      return true;
   }
}
