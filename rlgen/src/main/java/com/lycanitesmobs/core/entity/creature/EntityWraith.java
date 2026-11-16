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


public class EntityWraith extends TameableCreatureEntity implements Enemy {
   protected int detonateTimer = -1;

   public EntityWraith(Level world) {
      super(LycEntities.type(EntityWraith.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = true;
      this.setupMob();
      this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setSpeed(2.0).setLongMemory(false));
   }

   @Override
   public void aiStep() {
      if (!this.level().isClientSide) {
         if (this.detonateTimer == 0) {
            LycWorld.createExplosion(this.level(), this, this.getX(), this.getY(), this.getZ(), 1.0F, true);
            this.discard();
         } else if (this.detonateTimer > 0) {
            this.detonateTimer--;
            if (Material.of(this.level().getBlockState(this.blockPosition())).isSolid()) {
               this.detonateTimer = 0;
            } else {
               for (LivingEntity entity : this.getNearbyEntities(LivingEntity.class, null, 1.0)) {
                  if (this.getPlayerOwner() == null || entity != this.getPlayerOwner()) {
                     if (entity instanceof TameableCreatureEntity) {
                        TameableCreatureEntity entityCreature = (TameableCreatureEntity)entity;
                        if (entityCreature.getPlayerOwner() != null && entityCreature.getPlayerOwner() == this.getPlayerOwner()) {
                           continue;
                        }
                     }

                     this.detonateTimer = 0;
                     this.attackEntityAsMob(entity, 4.0);
                  }
               }
            }
         }
      }

      if (this.level().isClientSide && this.detonateTimer <= 5) {
         for (int i = 0; i < 2; i++) {
            this.level()
               .addParticle(ParticleTypes.SMOKE, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            this.level()
               .addParticle(ParticleTypes.FLAME, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
         }
      }

      super.aiStep();
   }

   @Override
   public boolean rollWanderChance() {
      return this.getRandom().nextDouble() <= 0.25;
   }

   public void chargeAttack() {
      this.leap(5.0, (double)this.getXRot());
      this.detonateTimer = 10;
   }

   @Override
   public boolean isFlying() {
      return true;
   }

   @Override
   public void die(DamageSource par1DamageSource) {
      if (!this.level().isClientSide && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
         int explosionRadius = 1;
         if (this.subspecies != null) {
            explosionRadius = 3;
         }

         explosionRadius = Math.max(1, Math.round((float)explosionRadius * (float)this.sizeScale));
         LycWorld.createExplosion(this.level(), this, this.getX(), this.getY(), this.getZ(), (float)explosionRadius, true);
      }

      super.die(par1DamageSource);
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
      return true;
   }

   public float getBrightness() {
      return -1; // 1.12 Entity.getBrightnessForRender (world light): the client renderer keeps its default
   }

   public int getBrightnessForRender() {
      return -1; // 1.12 Entity.getBrightnessForRender (world light): the client renderer keeps its default
   }

   public void load(CompoundTag nbtTagCompound) {
      super.load(nbtTagCompound);
      if (nbtTagCompound.contains("DetonateTimer")) {
         this.detonateTimer = nbtTagCompound.getInt("DetonateTimer");
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.addAdditionalSaveData(nbtTagCompound);
      if (this.detonateTimer > -1) {
         nbtTagCompound.putInt("DetonateTimer", this.detonateTimer);
      }
   }
}
