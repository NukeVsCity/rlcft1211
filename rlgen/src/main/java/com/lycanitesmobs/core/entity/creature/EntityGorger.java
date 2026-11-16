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
import com.lycanitesmobs.api.IGroupHeavy;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.StealthGoal;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;


public class EntityGorger extends TameableCreatureEntity implements IGroupHeavy {
   public EntityGorger(Level world) {
      super(LycEntities.type(EntityGorger.class), world);
      this.attribute = EnumCreatureAttribute.ARTHROPOD;
      this.spawnsOnLand = true;
      this.spawnsInWater = true;
      this.hasAttackSound = false;
      this.trueSight = true;
      this.babySpawnChance = 0.25;
      this.growthTime = -120000;
      this.setupMob();
      this.hitAreaWidthScale = 1.5F;
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextPriorityGoalIndex++, new StealthGoal(this).setStealthTime(60));
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(1.0).setRange(16.0F).setMinChaseDistance(8.0F));
   }

   @Override
   public float getAISpeedModifier() {
      return this.isInWater() ? 2.0F : 1.0F;
   }

   @Override
   public boolean isPushedByFluid() {
      return false;
   }

   @Override
   public void attackRanged(Entity target, float range) {
      ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("magma");
      if (projectileInfo != null) {
         BaseProjectileEntity projectile = projectileInfo.createProjectile(this.level(), this);
         projectile.setProjectileScale(2.0F);
         LycMotion.setPosY(projectile, projectile.getY() - (double)(this.getBbHeight() / 4.0F));
         float accuracy = 1.0F * (this.getRandom().nextFloat() - 0.5F);
         double d0 = target.getX() - this.getX() + (double)accuracy;
         double d1 = target.getY() + (double)target.getEyeHeight() - 1.1F - projectile.getY() + (double)accuracy;
         double d2 = target.getZ() - this.getZ() + (double)accuracy;
         float f1 = (float)Math.sqrt(d0 * d0 + d2 * d2) * 0.2F;
         float velocity = 1.2F;
         projectile.shoot(d0, d1 + (double)f1, d2, velocity, 6.0F);
         this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
         this.level().addFreshEntity(projectile);

         for (int i = 0; i < 10; i++) {
            projectile = projectileInfo.createProjectile(this.level(), this);
            projectile.setProjectileScale(2.0F);
            projectile.shoot(
               (double)(this.getRandom().nextFloat() - 0.5F),
               (double)this.getRandom().nextFloat(),
               (double)(this.getRandom().nextFloat() - 0.5F),
               0.5F,
               3.0F
            );
            this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level().addFreshEntity(projectile);
         }

         super.attackRanged(target, range);
      }
   }

   @Override
   public boolean canStealth() {
      if (this.isTamed() && this.isSitting()) {
         return false;
      } else {
         BlockState blockState = this.level().getBlockState(this.blockPosition().offset(0, -1, 0));
         if (blockState.getBlock() != Blocks.AIR) {
            if (Material.of(blockState) == Material.GROUND) {
               return true;
            }

            if (Material.of(blockState) == Material.GRASS) {
               return true;
            }

            if (Material.of(blockState) == Material.LEAVES) {
               return true;
            }

            if (Material.of(blockState) == Material.SAND) {
               return true;
            }

            if (Material.of(blockState) == Material.CLAY) {
               return true;
            }

            if (Material.of(blockState) == Material.SNOW) {
               return true;
            }

            if (Material.of(blockState) == Material.CRAFTED_SNOW) {
               return true;
            }
         }

         return blockState.getBlock() == Blocks.NETHERRACK;
      }
   }

   @Override
   public boolean canBeTempted() {
      return this.isBaby();
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
      if (type.equals("cactus")) {
         return false;
      } else {
         return type.equals("inWall") ? false : super.isDamageTypeApplicable(type, source, damage);
      }
   }

   @Override
   public boolean canBurn() {
      return false;
   }

   @Override
   public boolean waterDamage() {
      return true;
   }

   @Override
   public boolean canBreatheUnderwater112() {
      return true;
   }

   @Override
   public boolean canBreatheAir() {
      return true;
   }

   @Override
   public float getDamageModifier(DamageSource damageSrc) {
      return damageSrc.is(DamageTypeTags.IS_FIRE) ? 0.0F : super.getDamageModifier(damageSrc);
   }
}
