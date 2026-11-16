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
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;


public class EntityEpion extends RideableCreatureEntity implements Enemy {
   public boolean griefing = true;

   public EntityEpion(Level world) {
      super(LycEntities.type(EntityEpion.class), world);
      this.attribute = EnumCreatureAttribute.UNDEAD;
      this.hasAttackSound = false;
      this.flySoundSpeed = 20;
      this.trueSight = true;
      this.setupMob();
      this.setStepHeightCompat(1.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75).setRange(14.0F).setMinChaseDistance(6.0F));
   }

   @Override
   public void loadCreatureFlags() {
      this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
   }

   @Override
   public float getStafeSpeed() {
      return 1.0F;
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide
         && !this.isTamed()
         && !this.isMinion()
         && !this.isRareVariant()
         && !this.isFlying()
         && (this.onGround() || this.isInWater())
         && this.isAlive()) {
         int explosionRadius = 2;
         if (this.subspecies != null) {
            explosionRadius = 3;
         }

         explosionRadius = Math.max(2, Math.round((float)explosionRadius * (float)this.sizeScale));
         if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.griefing) {
            this.level().createExplosion(this, this.getX(), this.getY(), this.getZ(), (float)explosionRadius, true);
         } else {
            this.level().createExplosion(this, this.getX(), this.getY(), this.getZ(), (float)explosionRadius, false);
         }

         this.discard();
      }

      if (this.level().isClientSide) {
         for (int i = 0; i < 2; i++) {
            this.level()
               .addParticle(ParticleTypes.WITCH, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
         }
      }
   }

   @Override
   public boolean canEntityBeSeen(Entity target) {
      return this.isRareVariant() ? true : super.hasLineOfSight(target);
   }

   @Override
   public void attackRanged(Entity target, float range) {
      this.fireProjectile("bloodleech", target, range, 0.0F, new Vec3(0.0, 0.0, 0.0), 1.2F, 2.0F, 1.0F);
      super.attackRanged(target, range);
   }

   @Override
   public boolean isFlying() {
      if (this.level().isClientSide) {
         return true;
      } else {
         if (this.daylightBurns() && this.level().isDay() && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.griefing) {
            float brightness = this.getLightLevelDependentMagicValue();
            if (brightness > 0.5F && this.level().canSeeSky(this.blockPosition())) {
               return false;
            }
         }

         return true;
      }
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
   public boolean daylightBurns() {
      return !this.isMinion() && !this.hasMaster() && !this.isTamed() && !this.isRareVariant();
   }

   @Override
   public float getFallResistance() {
      return 100.0F;
   }

   @Override
   public void mountAbility(Entity rider) {
      if (!this.level().isClientSide && this.updateTick % 2L != 0L) {
         if (!(this.getStamina() < this.getStaminaCost())) {
            if (rider instanceof Player) {
               Player player = (Player)rider;
               ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("bloodleech");
               if (projectileInfo != null) {
                  BaseProjectileEntity projectile = projectileInfo.createProjectile(this.level(), player);
                  this.level().addFreshEntity(projectile);
                  this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                  this.triggerAttackCooldown();
               }
            }

            this.applyStaminaCost();
         }
      }
   }

   @Override
   public float getStaminaCost() {
      return 5.0F;
   }

   @Override
   public int getStaminaRecoveryWarmup() {
      return 0;
   }

   @Override
   public float getStaminaRecoveryMax() {
      return 1.0F;
   }

   @Override
   public ResourceLocation getTexture() {
      if (!"Vampire Bat".equals(this.getCustomName().getString())) {
         return super.getTexture();
      } else {
         String textureName = this.getTextureName() + "_vampirebat";
         if (AssetManager.getTexture(textureName) == null) {
            AssetManager.addTexture(textureName, this.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
         }

         return AssetManager.getTexture(textureName);
      }
   }

   @Override
   public AABB getRenderBoundingBox() {
      return this.getBoundingBox().inflate(10.0, 10.0, 10.0).offset(0.0, -5.0, 0.0);
   }
}
