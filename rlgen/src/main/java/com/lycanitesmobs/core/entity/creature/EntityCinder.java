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
import com.lycanitesmobs.api.IFusable;
import com.lycanitesmobs.core.entity.EntityItemCustom;
import com.lycanitesmobs.core.entity.RapidFireProjectileEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import java.util.ArrayList;
import java.util.List;


public class EntityCinder extends TameableCreatureEntity implements Enemy, IFusable {
   public float inWallDamageAbsorbed = 0.0F;
   protected IFusable fusionTarget;

   public EntityCinder(Level world) {
      super(LycEntities.type(EntityCinder.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.spawnsInBlock = false;
      this.hasAttackSound = false;
      this.setupMob();
      this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks
         .addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75).setStaminaTime(100).setRange(5.0F).setMinChaseDistance(2.0F));
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide && this.inWallDamageAbsorbed >= 10.0F) {
         this.transform(CreatureManager.getInstance().getEntityClass("volcan"), null, false);
      }

      if (this.level().isClientSide) {
         for (int i = 0; i < 2; i++) {
            this.level()
               .addParticle(ParticleTypes.LARGE_SMOKE, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            this.level()
               .addParticle(ParticleTypes.FLAME, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
         }
      }
   }

   @Override
   public void attackRanged(Entity target, float range) {
      List<RapidFireProjectileEntity> projectiles = new ArrayList<>();
      ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("ember");
      if (projectileInfo != null) {
         RapidFireProjectileEntity projectileEntry = new RapidFireProjectileEntity(projectileInfo, this.level(), this, 15, 3);
         projectiles.add(projectileEntry);
         RapidFireProjectileEntity projectileEntry2 = new RapidFireProjectileEntity(projectileInfo, this.level(), this, 15, 3);
         projectileEntry2.offsetX++;
         projectiles.add(projectileEntry2);
         RapidFireProjectileEntity projectileEntry3 = new RapidFireProjectileEntity(projectileInfo, this.level(), this, 15, 3);
         projectileEntry3.offsetX--;
         projectiles.add(projectileEntry3);
         RapidFireProjectileEntity projectileEntry4 = new RapidFireProjectileEntity(projectileInfo, this.level(), this, 15, 3);
         projectileEntry4.offsetZ++;
         projectiles.add(projectileEntry4);
         RapidFireProjectileEntity projectileEntry5 = new RapidFireProjectileEntity(projectileInfo, this.level(), this, 15, 3);
         projectileEntry5.offsetZ--;
         projectiles.add(projectileEntry5);
         RapidFireProjectileEntity projectileEntry6 = new RapidFireProjectileEntity(projectileInfo, this.level(), this, 15, 3);
         projectileEntry6.offsetY++;
         projectiles.add(projectileEntry6);
         RapidFireProjectileEntity projectileEntry7 = new RapidFireProjectileEntity(projectileInfo, this.level(), this, 15, 3);
         projectileEntry7.offsetY--;
         projectiles.add(projectileEntry7);

         for (RapidFireProjectileEntity projectile : projectiles) {
            projectile.setProjectileScale(1.0F);
            LycMotion.setPosY(projectile, projectile.getY() - (double)(this.getBbHeight() / 4.0F));
            float accuracy = 1.0F * (this.getRandom().nextFloat() - 0.5F);
            double d0 = target.getX() - this.getX() + (double)accuracy;
            double d1 = target.getY() + (double)target.getEyeHeight() - 1.1F - projectile.getY() + (double)accuracy;
            double d2 = target.getZ() - this.getZ() + (double)accuracy;
            float f1 = (float)Math.sqrt(d0 * d0 + d2 * d2) * 0.2F;
            float velocity = 0.6F;
            projectile.shoot(d0, d1 + (double)f1, d2, velocity, 6.0F);
            this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level().addFreshEntity(projectile);
         }

         super.attackRanged(target, range);
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

   @Override
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return super.isDamageTypeApplicable(type, source, damage);
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
   public float getDamageModifier(DamageSource damageSrc) {
      return damageSrc.is(DamageTypeTags.IS_FIRE) ? 0.0F : super.getDamageModifier(damageSrc);
   }

   @Override
   public void applyDropEffects(EntityItemCustom entityitem) {
      entityitem.setCanBurn(false);
   }

   public float getBrightness() {
      return 1.0F;
   }

   public int getBrightnessForRender() {
      return 15728880;
   }

   @Override
   public IFusable getFusionTarget() {
      return this.fusionTarget;
   }

   @Override
   public void setFusionTarget(IFusable fusionTarget) {
      this.fusionTarget = fusionTarget;
   }

   @Override
   public Class getFusionClass(IFusable fusable) {
      if (fusable instanceof EntityJengu) {
         return CreatureManager.getInstance().getEntityClass("xaphan");
      } else if (fusable instanceof EntityGeonach) {
         return CreatureManager.getInstance().getEntityClass("volcan");
      } else if (fusable instanceof EntityDjinn) {
         return CreatureManager.getInstance().getEntityClass("zephyr");
      } else if (fusable instanceof EntityAegis) {
         return CreatureManager.getInstance().getEntityClass("wisp");
      } else {
         return fusable instanceof EntityArgus ? CreatureManager.getInstance().getEntityClass("grue") : null;
      }
   }
}
