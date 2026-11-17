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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.EntityItemCustom;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;


public class EntityLobber extends BaseCreatureEntity implements Enemy {
   public int blockMeltingRadius = 2;

   public EntityLobber(Level world) {
      super(LycEntities.type(EntityLobber.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.spawnsOnLand = true;
      this.spawnsInWater = true;
      this.isLavaCreature = true;
      this.hasAttackSound = false;
      this.setupMob();
      this.setPathfindingMalus(PathType.LAVA, 0.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(1.0).setRange(16.0F).setMinChaseDistance(8.0F));
   }

   @Override
   public void loadCreatureFlags() {
      this.blockMeltingRadius = this.creatureInfo.getFlag("blockMeltingRadius", this.blockMeltingRadius);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide && this.isMoving() && this.tickCount % 5 == 0) {
         int trailHeight = 1;
         int trailWidth = 1;
         if (this.isRareVariant()) {
            trailWidth = 3;
         }

         for (int y = 0; y < trailHeight; y++) {
            Block block = this.level().getBlockState(this.blockPosition().offset(0, y, 0)).getBlock();
            if (block == Blocks.AIR
               || block == Blocks.FIRE
               || block == Blocks.SNOW_BLOCK
               || block == Blocks.SHORT_GRASS
               || block == ObjectManager.getBlock("frostfire")
               || block == ObjectManager.getBlock("icefire")) {
               if (trailWidth == 1) {
                  this.level().setBlock(this.blockPosition().offset(0, y, 0), Blocks.FIRE.defaultBlockState(), 3);
               } else {
                  for (int x = -(trailWidth / 2); x < trailWidth / 2 + 1; x++) {
                     for (int z = -(trailWidth / 2); z < trailWidth / 2 + 1; z++) {
                        this.level().setBlock(this.blockPosition().offset(x, y, z), Blocks.FIRE.defaultBlockState(), 3);
                     }
                  }
               }
            }
         }
      }

      if (!this.level().isClientSide
         && this.isRareVariant()
         && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
         && this.blockMeltingRadius > 0
         && this.tickCount % 10 == 0) {
         int range = this.blockMeltingRadius;

         for (int w = -((int)Math.ceil((double)this.getBbWidth()) + range); (double)w <= Math.ceil((double)this.getBbWidth()) + (double)range; w++) {
            for (int d = -((int)Math.ceil((double)this.getBbWidth()) + range); (double)d <= Math.ceil((double)this.getBbWidth()) + (double)range; d++) {
               for (int h = 0; (double)h <= Math.ceil((double)this.getBbHeight()); h++) {
                  Block block = this.level().getBlockState(this.blockPosition().offset(w, h, d)).getBlock();
                  if (block == Blocks.OBSIDIAN
                     || block == Blocks.COBBLESTONE
                     || block == Blocks.DIRT
                     || block == Blocks.GRAVEL
                     || block == Blocks.SAND) {
                     BlockState blockState = Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 11);
                     this.level().setBlock(this.blockPosition().offset(w, h, d), blockState, 3);
                  }
               }
            }
         }

         if (this.tickCount % 40 == 0) {
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("magma");
            if (projectileInfo != null) {
               BaseProjectileEntity projectile = projectileInfo.createProjectile(this.level(), this);
               projectile.setProjectileScale(2.0F);
               projectile.shoot(
                  (double)(2.0F * this.getRandom().nextFloat() - 1.0F),
                  (double)this.getRandom().nextFloat(),
                  (double)(2.0F * this.getRandom().nextFloat() - 1.0F),
                  1.2F,
                  6.0F
               );
               this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
               this.level().addFreshEntity(projectile);
            }
         }
      }

      if (this.level().isClientSide) {
         for (int i = 0; i < 2; i++) {
            this.level()
               .addParticle(ParticleTypes.FLAME, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            this.level()
               .addParticle(ParticleTypes.DRIPPING_LAVA, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
         }

         if (this.tickCount % 10 == 0) {
            for (int i = 0; i < 2; i++) {
               this.level()
                  .addParticle(ParticleTypes.FLAME, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            }
         }
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
      if (this.level().getBlockState(pos).getBlock() == Blocks.LAVA) {
         return (super.getBlockPathWeight(x, y, z) + 1.0F) * (float)(waterWeight + 1);
      } else if (this.getTarget() != null) {
         return super.getBlockPathWeight(x, y, z);
      } else {
         return this.lavaContact() ? -999999.0F : super.getBlockPathWeight(x, y, z);
      }
   }

   @Override
   public boolean isPushedByFluid() {
      return false;
   }

   @Override
   public void attackRanged(Entity target, float range) {
      this.fireProjectile("magma", target, range, 0.0F, new Vec3(0.0, 0.0, 0.0), 1.2F, 2.0F, 1.0F);
      super.attackRanged(target, range);
   }

   @Override
   public boolean isAggressive() {
      return this.getAirSupply() <= -100 ? false : super.isAggressive();
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
   public boolean waterDamage() {
      return !this.isRareVariant();
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
}
