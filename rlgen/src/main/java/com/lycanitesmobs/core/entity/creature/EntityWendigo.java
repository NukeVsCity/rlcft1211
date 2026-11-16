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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.actions.WanderGoal;


public class EntityWendigo extends BaseCreatureEntity implements Enemy {
   WanderGoal wanderAI;

   public EntityWendigo(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.UNDEAD;
      this.spawnsOnLand = true;
      this.spawnsInWater = true;
      this.hasAttackSound = false;
      this.setupMob();
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(1.0).setRange(16.0F).setMinChaseDistance(8.0F));
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
            if (block != null
               && (
                  block == Blocks.AIR
                     || block == Blocks.FIRE
                     || block == Blocks.SNOW_BLOCK
                     || block == Blocks.SHORT_GRASS
                     || block == ObjectManager.getBlock("scorchfire")
                     || block == ObjectManager.getBlock("doomfire")
               )) {
               if (trailWidth == 1) {
                  this.level().setBlock(this.blockPosition().offset(0, y, 0), ObjectManager.getBlock("frostfire").defaultBlockState(), 3);
               } else {
                  for (int x = -(trailWidth / 2); x < trailWidth / 2 + 1; x++) {
                     for (int z = -(trailWidth / 2); z < trailWidth / 2 + 1; z++) {
                        this.level().setBlock(this.blockPosition().offset(x, y, z), ObjectManager.getBlock("frostfire").defaultBlockState(), 3);
                     }
                  }
               }
            }
         }
      }

      if (!this.level().isClientSide && this.isMoving() && this.tickCount % 5 == 0) {
         Block block = this.level().getBlockState(this.blockPosition().offset(0, -1, 0)).getBlock();
         if (block == Blocks.WATER) {
            this.level().setBlock(this.blockPosition().offset(0, -1, 0), Blocks.ICE.defaultBlockState(), 3);
         }
      }

      if (this.level().isClientSide) {
         for (int i = 0; i < 2; i++) {
            this.level()
               .addParticle(ParticleTypes.ITEM_SNOWBALL, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            this.level()
               .addParticle(ParticleTypes.ITEM_SNOWBALL, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
         }

         if (this.tickCount % 10 == 0) {
            for (int i = 0; i < 2; i++) {
               this.level()
                  .addParticle(ParticleTypes.ITEM_SNOWBALL, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            }
         }
      }
   }

   @Override
   public float getAISpeedModifier() {
      return this.isInWater() ? 2.0F : 1.0F;
   }

   @Override
   public float getBlockPathWeight(int x, int y, int z) {
      int waterWeight = 10;
      BlockPos pos = LycPos.of(x, y, z);
      if (ObjectManager.getBlock("ooze") != null && this.level().getBlockState(pos).getBlock() == ObjectManager.getBlock("ooze")) {
         return (super.getBlockPathWeight(x, y, z) + 1.0F) * (float)(waterWeight + 1);
      } else if (this.getTarget() != null) {
         return super.getBlockPathWeight(x, y, z);
      } else {
         return this.isInWater() ? -999999.0F : super.getBlockPathWeight(x, y, z);
      }
   }

   @Override
   public boolean isPushedByFluid() {
      return false;
   }

   @Override
   public void attackRanged(Entity target, float range) {
      this.fireProjectile("tundra", target, range, 0.0F, new Vec3(0.0, 0.0, 0.0), 1.2F, 2.0F, 1.0F);
      super.attackRanged(target, range);
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
      return type.equals("ooze") ? false : super.isDamageTypeApplicable(type, source, damage);
   }

   @Override
   public boolean canBreatheUnderwater112() {
      return true;
   }
}
