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
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ObjectLists;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;


public class EntitySpriggan extends TameableCreatureEntity implements Enemy {
   public int farmingRate = 20;
   private int farmingTick = 0;
   BaseProjectileEntity projectile = null;

   public EntitySpriggan(Level world) {
      super(LycEntities.type(EntitySpriggan.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = false;
      this.setupMob();
      this.setStepHeightCompat(1.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks
         .addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75).setStaminaTime(100).setRange(8.0F).setMinChaseDistance(4.0F));
   }

   @Override
   public void loadCreatureFlags() {
      this.farmingRate = this.creatureInfo.getFlag("farmingRate", this.farmingRate);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.getAirSupply() >= 0) {
         if (this.isInWater()) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 2));
         } else if (this.waterContact()) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
         }
      }

      int currentFarmingRate = this.farmingRate;
      if (this.isTamed() && currentFarmingRate > 0) {
         if (this.isPetType("familiar")) {
            currentFarmingRate = this.farmingRate * 10;
         }

         if (this.subspecies != null) {
            currentFarmingRate = Math.max(1, Math.round((float)this.farmingRate / 3.0F));
         }

         this.farmingTick++;
         int farmingRange = 16;
         int farmingHeight = 4;

         for (int x = (int)this.getX() - farmingRange; x <= (int)this.getX() + farmingRange; x++) {
            for (int y = (int)this.getY() - farmingHeight; y <= (int)this.getY() + farmingHeight; y++) {
               for (int z = (int)this.getZ() - farmingRange; z <= (int)this.getZ() + farmingRange; z++) {
                  BlockPos pos = LycPos.of(x, y, z);
                  Block farmingBlock = this.level().getBlockState(pos).getBlock();
                  if (farmingBlock instanceof IPlantable
                     && farmingBlock instanceof IGrowable
                     && farmingBlock != Blocks.SHORT_GRASS
                     && farmingBlock != Blocks.TALL_GRASS) {
                     if (!this.level().isClientSide && this.farmingTick % currentFarmingRate == 0 && farmingBlock.getTickRandomly()) {
                        this.level().scheduleTick(pos, farmingBlock, currentFarmingRate);
                     }

                     if (this.level().isClientSide && this.farmingTick % 40 == 0) {
                        double d0 = this.getRandom().nextGaussian() * 0.02;
                        double d1 = this.getRandom().nextGaussian() * 0.02;
                        double d2 = this.getRandom().nextGaussian() * 0.02;
                        this.level()
                           .addParticle(ParticleTypes.HAPPY_VILLAGER, (double)((float)x + this.getRandom().nextFloat()), (double)y + (double)this.getRandom().nextFloat(), (double)((float)z + this.getRandom().nextFloat()), d0, d1, d2);
                     }
                  }
               }
            }
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
   public void attackRanged(Entity target, float range) {
      ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("lifedrain");
      if (projectileInfo != null) {
         if (this.projectile != null && this.projectile.isAlive()) {
            this.projectile.projectileLife = 20;
         } else {
            this.projectile = null;
         }

         if (this.projectile == null) {
            this.projectile = projectileInfo.createProjectile(this.level(), this);
            this.playSound(this.projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level().addFreshEntity(this.projectile);
         }

         super.attackRanged(target, range);
      }
   }

   @Override
   public float getDamageModifier(DamageSource damageSrc) {
      if (damageSrc.is(DamageTypeTags.IS_FIRE)) {
         return 2.0F;
      } else {
         if (damageSrc.getEntity() != null) {
            ItemStack heldItem = ItemStack.EMPTY;
            if (damageSrc.getEntity() instanceof LivingEntity) {
               LivingEntity entityLiving = (LivingEntity)damageSrc.getEntity();
               if (!entityLiving.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                  heldItem = entityLiving.getItemInHand(InteractionHand.MAIN_HAND);
               }
            }

            if (ObjectLists.isAxe(heldItem)) {
               return 2.0F;
            }
         }

         return super.getDamageModifier(damageSrc);
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
   public boolean canBreatheUnderwater112() {
      return true;
   }
}
