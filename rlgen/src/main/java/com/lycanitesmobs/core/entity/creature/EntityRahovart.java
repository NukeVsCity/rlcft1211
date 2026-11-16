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
import com.lycanitesmobs.api.IGroupBoss;
import com.lycanitesmobs.api.IGroupHeavy;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.GoalConditions;
import com.lycanitesmobs.core.entity.goals.actions.FindNearbyPlayersGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FaceTargetGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FireProjectilesGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.HealWhenNoPlayersGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.SummonMinionsGoal;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireBarrier;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireOrb;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireWave;
import com.lycanitesmobs.core.info.CreatureManager;
import java.util.ArrayList;
import java.util.List;


public class EntityRahovart extends BaseCreatureEntity implements Enemy, IGroupHeavy, IGroupBoss {
   public int hellfireEnergy = 0;
   public List<EntityHellfireOrb> hellfireOrbs = new ArrayList<>();
   protected static final EntityDataAccessor<Integer> HELLFIRE_ENERGY = SynchedEntityData.defineId(EntityRahovart.class, EntityDataSerializers.INT);
   public List<EntityBelph> hellfireBelphMinions = new ArrayList<>();
   public List<EntityBehemoth> hellfireBehemothMinions = new ArrayList<>();
   public int hellfireWallTime = 0;
   public int hellfireWallTimeMax = 400;
   public boolean hellfireWallClockwise = false;
   public EntityHellfireBarrier hellfireWallLeft;
   public EntityHellfireBarrier hellfireWallRight;
   public List<EntityHellfireBarrier> hellfireBarriers = new ArrayList<>();
   public int hellfireBarrierHealth = 100;

   public EntityRahovart(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.UNDEAD;
      this.hasAttackSound = false;
      this.setAttackCooldownMax(40);
      this.solidCollision = true;
      this.trueSight = true;
      this.entityCollisionReduction = 1.0F;
      this.setupMob();
      this.hitAreaWidthScale = 2.0F;
      this.damageMax = BaseCreatureEntity.BOSS_DAMAGE_LIMIT;
      this.damageLimit = (float)BaseCreatureEntity.BOSS_DAMAGE_LIMIT;
   }

   @Override
   protected void initEntityAI() {
      this.targetTasks.addTask(this.nextFindTargetIndex, new FindNearbyPlayersGoal(this));
      this.tasks.addTask(this.nextIdleGoalIndex, new FaceTargetGoal(this));
      this.tasks.addTask(this.nextIdleGoalIndex, new HealWhenNoPlayersGoal(this));
      this.tasks.addTask(this.nextIdleGoalIndex, new SummonMinionsGoal(this).setMinionInfo("wraith").setAntiFlight(true));
      this.tasks
         .addTask(
            this.nextIdleGoalIndex,
            new FireProjectilesGoal(this).setProjectile("hellfireball").setFireRate(40).setVelocity(1.0F).setScale(8.0F).setAllPlayers(true)
         );
      this.tasks
         .addTask(this.nextIdleGoalIndex, new FireProjectilesGoal(this).setProjectile("hellfireball").setFireRate(60).setVelocity(1.0F).setScale(8.0F));
      this.tasks
         .addTask(
            this.nextIdleGoalIndex,
            new SummonMinionsGoal(this)
               .setMinionInfo("archvile")
               .setSummonRate(200)
               .setSummonCap(1)
               .setPerPlayer(true)
               .setSizeScale(2.0)
               .setConditions(new GoalConditions().setBattlePhase(2))
         );
      super.initEntityAI();
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(HELLFIRE_ENERGY, this.hellfireEnergy);
   }

   @Override
   public AABB getRenderBoundingBox() {
      return this.getBoundingBox().inflate(200.0, 50.0, 200.0).offset(0.0, -25.0, 0.0);
   }

   @Override
   public void onFirstSpawn() {
      super.onFirstSpawn();
      if (this.getArenaCenter() == null) {
         this.setArenaCenter(this.blockPosition());
      }
   }

   @Override
   public void setArenaCenter(BlockPos pos) {
      super.setArenaCenter(pos);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.hasAttackTarget() && !this.level().isClientSide) {
         this.getLookControl().setLookAt(this.getTarget(), 30.0F, 30.0F);
      }

      if (this.hasArenaCenter()) {
         BlockPos arenaPos = this.getArenaCenter();
         double arenaY = this.getY();
         if (this.level().isEmptyBlock(arenaPos)) {
            arenaY = (double)arenaPos.getY();
         } else if (this.level().isEmptyBlock(arenaPos.offset(0, 1, 0))) {
            arenaY = (double)arenaPos.offset(0, 1, 0).getY();
         }

         if (this.getX() != (double)arenaPos.getX() || this.getY() != arenaY || this.getZ() != (double)arenaPos.getZ()) {
            this.set((double)arenaPos.getX(), arenaY, (double)arenaPos.getZ());
         }
      }

      if (!this.level().isClientSide) {
         this.entityData.set(HELLFIRE_ENERGY, this.hellfireEnergy);
      } else {
         this.hellfireEnergy = (Integer)this.entityData.get(HELLFIRE_ENERGY);
      }

      updateHellfireOrbs(this, this.updateTick, 5, this.hellfireEnergy, 10.0F, this.hellfireOrbs);
      if (!this.level().isClientSide) {
         this.updatePhases();
      }
   }

   @Override
   public boolean rollWanderChance() {
      return false;
   }

   public void updatePhases() {
      if (this.getBattlePhase() == 0) {
         if (!this.hellfireBehemothMinions.isEmpty()) {
            for (EntityBehemoth minion : this.hellfireBehemothMinions.toArray(new EntityBehemoth[this.hellfireBehemothMinions.size()])) {
               minion.hellfireEnergy = 0;
            }

            this.hellfireBehemothMinions = new ArrayList<>();
         }

         this.hellfireWallTime = 0;
         this.hellfireBarrierCleanup();
         if (this.updateTick % 20L == 0L) {
            for (EntityBelph minion : this.hellfireBelphMinions.toArray(new EntityBelph[this.hellfireBelphMinions.size()])) {
               if (!minion.isAlive()) {
                  this.onMinionDeath(minion, null);
               } else {
                  minion.hellfireEnergy += 5;
                  if (minion.hellfireEnergy >= 100) {
                     this.hellfireEnergy += 20;
                     this.onMinionDeath(minion, null);
                     this.level().createExplosion(minion, minion.getX(), minion.getY(), minion.getZ(), 1.0F, false);
                     minion.hellfireEnergy = 0;
                     minion.discard();
                  }
               }
            }
         }

         if (this.hellfireEnergy >= 100) {
            this.hellfireEnergy = 0;
            double angle = (double)(this.getRandom().nextFloat() * 360.0F);
            if (this.hasAttackTarget()) {
               double deltaX = this.getTarget().getX() - this.getX();
               double deltaZ = this.getTarget().getZ() - this.getZ();
               angle = Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI;
            }

            this.hellfireWaveAttack(angle);
         }

         if (this.updateTick % 100L == 0L) {
            int summonAmount = this.getRandom().nextInt(4);
            summonAmount *= this.playerTargets.size();
            if (summonAmount > 0) {
               for (int summonCount = 0; summonCount <= summonAmount; summonCount++) {
                  EntityBelph minionx = (EntityBelph)CreatureManager.getInstance().getCreature("belph").createEntity(this.level());
                  this.summonMinion(minionx, this.getRandom().nextDouble() * 360.0, 5.0);
                  this.hellfireBelphMinions.add(minionx);
               }
            }
         }
      }

      if (this.getBattlePhase() == 1) {
         if (!this.hellfireBelphMinions.isEmpty()) {
            for (EntityBelph minionx : this.hellfireBelphMinions.toArray(new EntityBelph[this.hellfireBelphMinions.size()])) {
               minionx.hellfireEnergy = 0;
            }

            this.hellfireBelphMinions = new ArrayList<>();
         }

         this.hellfireBarrierCleanup();
         if (this.hellfireWallTime <= 0 && this.updateTick % 20L == 0L) {
            for (EntityBehemoth minionx : this.hellfireBehemothMinions.toArray(new EntityBehemoth[this.hellfireBehemothMinions.size()])) {
               if (!minionx.isAlive()) {
                  this.onMinionDeath(minionx, null);
               } else {
                  minionx.hellfireEnergy += 5;
                  if (minionx.hellfireEnergy >= 100) {
                     this.hellfireEnergy += 20;
                     this.onMinionDeath(minionx, null);
                     this.level().createExplosion(minionx, minionx.getX(), minionx.getY(), minionx.getZ(), 1.0F, false);
                     minionx.hellfireEnergy = 0;
                     minionx.discard();
                  }
               }
            }
         }

         if (this.hellfireEnergy >= 100) {
            this.hellfireEnergy = 0;
            this.hellfireWallAttack((double)this.getYRot());
         }

         if (this.hellfireWallTime > 0) {
            this.hellfireWallUpdate();
            this.hellfireWallTime--;
         }

         if (this.updateTick % 400L == 0L) {
            int summonAmount = 2;
            summonAmount *= this.playerTargets.size();
            if (summonAmount > 0) {
               for (int summonCount = 0; summonCount <= summonAmount; summonCount++) {
                  EntityBehemoth minionxx = (EntityBehemoth)CreatureManager.getInstance().getCreature("behemoth").createEntity(this.level());
                  this.summonMinion(minionxx, this.getRandom().nextDouble() * 360.0, 5.0);
                  this.hellfireBehemothMinions.add(minionxx);
               }
            }
         }

         if (this.updateTick % 200L == 0L) {
            int summonAmount = this.getRandom().nextInt(4) - 1;

            for (int summonCount = 0; summonCount <= summonAmount; summonCount++) {
               EntityBelph minionxx = (EntityBelph)CreatureManager.getInstance().getCreature("belph").createEntity(this.level());
               this.summonMinion(minionxx, this.getRandom().nextDouble() * 360.0, 5.0);
            }
         }
      }

      if (this.getBattlePhase() >= 2) {
         if (!this.hellfireBelphMinions.isEmpty()) {
            for (EntityBelph minionxx : this.hellfireBelphMinions.toArray(new EntityBelph[this.hellfireBelphMinions.size()])) {
               minionxx.hellfireEnergy = 0;
            }

            this.hellfireBelphMinions = new ArrayList<>();
         }

         if (!this.hellfireBehemothMinions.isEmpty()) {
            for (EntityBehemoth minionxx : this.hellfireBehemothMinions.toArray(new EntityBehemoth[this.hellfireBehemothMinions.size()])) {
               minionxx.hellfireEnergy = 0;
            }

            this.hellfireBehemothMinions = new ArrayList<>();
         }

         this.hellfireWallTime = 0;
         if (this.updateTick % 20L == 0L && this.hellfireEnergy < 100) {
            this.hellfireEnergy += 5;
         }

         if (this.hellfireEnergy >= 100 && this.hellfireBarriers.size() < 20) {
            this.hellfireEnergy = 0;
            this.hellfireBarrierAttack((double)(360.0F * this.getRandom().nextFloat()));
         }

         if (this.hellfireBarriers.size() > 0) {
            this.hellfireBarrierUpdate();
         }

         if (this.updateTick % 200L == 0L) {
            int summonAmount = this.getRandom().nextInt(2);
            summonAmount *= this.playerTargets.size();
            if (summonAmount > 0) {
               for (int summonCount = 0; summonCount <= summonAmount; summonCount++) {
                  EntityBehemoth minionxx = (EntityBehemoth)CreatureManager.getInstance().getCreature("behemoth").createEntity(this.level());
                  this.summonMinion(minionxx, this.getRandom().nextDouble() * 360.0, 5.0);
                  this.hellfireBehemothMinions.add(minionxx);
               }
            }
         }

         if (this.updateTick % 400L == 0L) {
            int summonAmount = this.getRandom().nextInt(4);
            summonAmount *= this.playerTargets.size();
            if (summonAmount > 0) {
               for (int summonCount = 0; summonCount <= summonAmount; summonCount++) {
                  EntityBelph minionxx = (EntityBelph)CreatureManager.getInstance().getCreature("belph").createEntity(this.level());
                  this.summonMinion(minionxx, this.getRandom().nextDouble() * 360.0, 5.0);
               }
            }

            summonAmount = this.getRandom().nextInt(3);
            summonAmount *= this.playerTargets.size();
            if (summonAmount > 0) {
               for (int summonCount = 0; summonCount <= summonAmount; summonCount++) {
                  EntityWraith minionxx = (EntityWraith)CreatureManager.getInstance().getCreature("wraith").createEntity(this.level());
                  this.summonMinion(minionxx, this.getRandom().nextDouble() * 360.0, 5.0);
               }
            }
         }
      }

      if (this.hellfireWallTime <= 0) {
         this.hellfireWallCleanup();
      }
   }

   @Override
   public void onMinionDeath(LivingEntity minion, DamageSource damageSource) {
      if (minion instanceof EntityBelph && this.hellfireBelphMinions.contains(minion)) {
         this.hellfireBelphMinions.remove(minion);
      } else if (minion instanceof EntityBehemoth && this.hellfireBehemothMinions.contains(minion)) {
         this.hellfireBehemothMinions.remove(minion);
      } else {
         if (this.hellfireBarriers.size() > 0) {
            if (minion instanceof EntityBehemoth) {
               this.hellfireBarrierHealth -= 100;
            } else {
               this.hellfireBarrierHealth -= 50;
            }
         }

         super.onMinionDeath(minion, damageSource);
      }
   }

   @Override
   public void updateBattlePhase() {
      double healthNormal = (double)(this.getHealth() / this.getMaxHealth());
      if (healthNormal <= 0.2) {
         this.setBattlePhase(2);
      } else if (healthNormal <= 0.6) {
         this.setBattlePhase(1);
      } else {
         this.setBattlePhase(0);
      }
   }

   public static void updateHellfireOrbs(
      LivingEntity entity, long orbTick, int hellfireOrbMax, int hellfireOrbEnergy, float orbSize, List<EntityHellfireOrb> hellfireOrbs
   ) {
      if (entity.level().isClientSide) {
         int hellfireChargeCount = Math.round((float)Math.min(hellfireOrbEnergy, 100) / (100.0F / (float)hellfireOrbMax));
         int hellfireOrbRotationTime = 100;
         double hellfireOrbAngle = (double)(360.0F * ((float)(orbTick % (long)hellfireOrbRotationTime) / (float)hellfireOrbRotationTime));
         double hellfireOrbAngleOffset = 360.0 / (double)hellfireOrbMax;

         while (hellfireOrbs.size() < hellfireChargeCount) {
            EntityHellfireOrb hellfireOrb = new EntityHellfireOrb(entity.level(), entity);
            hellfireOrb.clientOnly = true;
            hellfireOrbs.add(hellfireOrb);
            entity.level().addFreshEntity(hellfireOrb);
            hellfireOrb.setProjectileScale(orbSize);
         }

         while (hellfireOrbs.size() > hellfireChargeCount) {
            hellfireOrbs.get(hellfireOrbs.size() - 1).discard();
            hellfireOrbs.remove(hellfireOrbs.size() - 1);
         }

         for (int i = 0; i < hellfireOrbs.size(); i++) {
            EntityHellfireOrb hellfireOrb = hellfireOrbs.get(i);
            double rotationRadians = Math.toRadians((hellfireOrbAngle + hellfireOrbAngleOffset * (double)i) % 360.0);
            double x = (double)entity.getBbWidth() * 1.25 * Math.cos(rotationRadians) + Math.sin(rotationRadians);
            double z = (double)entity.getBbWidth() * 1.25 * Math.sin(rotationRadians) - Math.cos(rotationRadians);
            LycMotion.setPosX(hellfireOrb, entity.getX() - x);
            LycMotion.setPosY(hellfireOrb, entity.getY() + (double)(entity.getBbHeight() * 0.75F));
            LycMotion.setPosZ(hellfireOrb, entity.getZ() - z);
            hellfireOrb.set(entity.getX() - x, entity.getY() + (double)(entity.getBbHeight() * 0.75F), entity.getZ() - z);
            hellfireOrb.projectileLife = 5;
         }
      }
   }

   @Override
   public boolean canAttackEntity(LivingEntity targetEntity) {
      if (!(targetEntity instanceof EntityBelph)
         && !(targetEntity instanceof EntityBehemoth)
         && !(targetEntity instanceof EntityArchvile)
         && !(targetEntity instanceof EntityWraith)) {
         return super.canAttackEntity(targetEntity);
      } else {
         return targetEntity instanceof TameableCreatureEntity ? ((TameableCreatureEntity)targetEntity).getOwner() instanceof Player : false;
      }
   }

   @Override
   public void attackRanged(Entity target, float range) {
      this.fireProjectile("hellfireball", target, range, 0.0F, new Vec3(0.0, 0.0, 0.0), 1.2F, 8.0F, 1.0F);
      super.attackRanged(target, range);
   }

   public void hellfireWaveAttack(double angle) {
      this.triggerAttackCooldown();
      this.playAttackSound();
      EntityHellfireWave hellfireWave = new EntityHellfireWave(this.level(), this);
      LycMotion.setPosY(hellfireWave, this.getY());
      hellfireWave.rotation = angle;
      this.level().addFreshEntity(hellfireWave);
   }

   public void hellfireWallAttack(double angle) {
      this.playAttackSound();
      this.triggerAttackCooldown();
      this.hellfireWallTime = this.hellfireWallTimeMax;
      this.hellfireWallClockwise = this.getRandom().nextBoolean();
   }

   public void hellfireWallUpdate() {
      this.triggerAttackCooldown();
      double hellfireWallNormal = (double)this.hellfireWallTime / (double)this.hellfireWallTimeMax;
      double hellfireWallAngle = 360.0;
      if (this.hellfireWallClockwise) {
         hellfireWallAngle = -360.0;
      }

      if (this.hellfireWallLeft == null) {
         this.hellfireWallLeft = new EntityHellfireBarrier(this.level(), this);
         this.hellfireWallLeft.wall = true;
         this.level().addFreshEntity(this.hellfireWallLeft);
      }

      this.hellfireWallLeft.time = 0;
      LycMotion.setPosX(this.hellfireWallLeft, this.getX());
      LycMotion.setPosY(this.hellfireWallLeft, this.getY());
      LycMotion.setPosZ(this.hellfireWallLeft, this.getZ());
      this.hellfireWallLeft.rotation = hellfireWallNormal * hellfireWallAngle;
      if (this.hellfireWallRight == null) {
         this.hellfireWallRight = new EntityHellfireBarrier(this.level(), this);
         this.hellfireWallRight.wall = true;
         this.level().addFreshEntity(this.hellfireWallRight);
      }

      this.hellfireWallRight.time = 0;
      LycMotion.setPosX(this.hellfireWallRight, this.getX());
      LycMotion.setPosY(this.hellfireWallRight, this.getY());
      LycMotion.setPosZ(this.hellfireWallRight, this.getZ());
      this.hellfireWallRight.rotation = 180.0 + hellfireWallNormal * hellfireWallAngle;
   }

   public void hellfireWallCleanup() {
      if (this.hellfireWallLeft != null) {
         this.hellfireWallLeft.discard();
         this.hellfireWallLeft = null;
      }

      if (this.hellfireWallRight != null) {
         this.hellfireWallRight.discard();
         this.hellfireWallRight = null;
      }
   }

   public void hellfireBarrierAttack(double angle) {
      if (this.hellfireBarriers.size() < 10) {
         this.triggerAttackCooldown();
         this.playAttackSound();
         EntityHellfireBarrier hellfireBarrier = new EntityHellfireBarrier(this.level(), this);
         this.level().addFreshEntity(hellfireBarrier);
         hellfireBarrier.time = 0;
         LycMotion.setPosX(hellfireBarrier, this.getX());
         LycMotion.setPosY(hellfireBarrier, this.getY());
         LycMotion.setPosZ(hellfireBarrier, this.getZ());
         hellfireBarrier.rotation = angle;
         this.hellfireBarriers.add(hellfireBarrier);
      }
   }

   public void hellfireBarrierUpdate() {
      if (this.hellfireBarrierHealth <= 0) {
         this.hellfireBarrierHealth = 100;
         if (this.hellfireBarriers.size() > 0) {
            EntityHellfireBarrier hellfireBarrier = this.hellfireBarriers.get(this.hellfireBarriers.size() - 1);
            hellfireBarrier.discard();
            this.hellfireBarriers.remove(this.hellfireBarriers.size() - 1);
         }
      }

      for (EntityHellfireBarrier hellfireBarrier : this.hellfireBarriers) {
         hellfireBarrier.time = 0;
         LycMotion.setPosX(hellfireBarrier, this.getX());
         LycMotion.setPosY(hellfireBarrier, this.getY());
         LycMotion.setPosZ(hellfireBarrier, this.getZ());
      }
   }

   public void hellfireBarrierCleanup() {
      if (!this.level().isClientSide && this.hellfireBarriers.size() >= 1) {
         for (EntityHellfireBarrier hellfireBarrier : this.hellfireBarriers) {
            hellfireBarrier.discard();
         }

         this.hellfireBarriers = new ArrayList<>();
         this.hellfireBarrierHealth = 100;
      }
   }

   @Override
   public boolean canBePushed() {
      return false;
   }

   @Override
   public boolean canBeAffected(MobEffectInstance potionEffect) {
      if (potionEffect.getEffect() == MobEffects.WITHER) {
         return false;
      } else if (ObjectManager.getEffect("decay") != null && potionEffect.getEffect() == ObjectManager.getEffect("decay")) {
         return false;
      } else {
         super.canBeAffected(potionEffect);
         return true;
      }
   }

   @Override
   public boolean canBurn() {
      return false;
   }

   @Override
   public boolean isDamageEntityApplicable(Entity entity) {
      if (entity instanceof ZombifiedPiglin) {
         entity.discard();
         return false;
      } else if (entity instanceof IronGolem) {
         entity.discard();
         return false;
      } else {
         if (entity instanceof Player) {
            Player player = (Player)entity;
            if (!player.getAbilities().invulnerable && player.getY() > this.getY() + CreatureManager.getInstance().config.bossAntiFlight) {
               return false;
            }
         }

         return super.isDamageEntityApplicable(entity);
      }
   }

   @Override
   public boolean hurt(DamageSource damageSrc, float damageAmount) {
      if (this.playerTargets != null
         && damageSrc.getEntity() != null
         && damageSrc.getEntity() instanceof Player
         && !this.playerTargets.contains(damageSrc.getEntity())) {
         this.playerTargets.add((Player)damageSrc.getEntity());
      }

      return super.hurt(damageSrc, damageAmount);
   }

   public void readFromNBT(CompoundTag nbtTagCompound) {
      super.readFromNBT(nbtTagCompound);
      if (nbtTagCompound.contains("HellfireEnergy")) {
         this.hellfireEnergy = nbtTagCompound.getInt("HellfireEnergy");
      }

      if (nbtTagCompound.contains("HellfireWallTime")) {
         this.hellfireWallTime = nbtTagCompound.getInt("HellfireWallTime");
      }

      if (nbtTagCompound.contains("BelphIDs")) {
         ListTag belphIDs = nbtTagCompound.getList("BelphIDs", 10);

         for (int i = 0; i < belphIDs.size(); i++) {
            CompoundTag belphID = belphIDs.getCompound(i);
            if (belphID.contains("ID")) {
               Entity entity = this.level().getEntity(belphID.getInt("ID"));
               if (entity != null && entity instanceof EntityBelph) {
                  this.hellfireBelphMinions.add((EntityBelph)entity);
               }
            }
         }
      }

      if (nbtTagCompound.contains("BehemothIDs")) {
         ListTag behemothIDs = nbtTagCompound.getList("BehemothIDs", 10);

         for (int ix = 0; ix < behemothIDs.size(); ix++) {
            CompoundTag behemothID = behemothIDs.getCompound(ix);
            if (behemothID.contains("ID")) {
               Entity entity = this.level().getEntity(behemothID.getInt("ID"));
               if (entity != null && entity instanceof EntityBehemoth) {
                  this.hellfireBehemothMinions.add((EntityBehemoth)entity);
               }
            }
         }
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.addAdditionalSaveData(nbtTagCompound);
      nbtTagCompound.putInt("HellfireEnergy", this.hellfireEnergy);
      nbtTagCompound.putInt("HellfireWallTime", this.hellfireWallTime);
      if (this.getBattlePhase() == 0) {
         ListTag belphIDs = new ListTag();

         for (EntityBelph entityBelph : this.hellfireBelphMinions) {
            CompoundTag belphID = new CompoundTag();
            belphID.putInt("ID", entityBelph.getId());
            belphIDs.add(belphID);
         }

         nbtTagCompound.put("BelphIDs", belphIDs);
      }

      if (this.getBattlePhase() == 1) {
         ListTag behemothIDs = new ListTag();

         for (EntityBehemoth entityBehemoth : this.hellfireBehemothMinions) {
            CompoundTag behemothID = new CompoundTag();
            behemothID.putInt("ID", entityBehemoth.getId());
            behemothIDs.add(behemothID);
         }

         nbtTagCompound.put("BehemothIDs", behemothIDs);
      }
   }

   @Override
   protected void playStepSound(BlockPos pos, Block block) {
      if (!this.hasArenaCenter()) {
         super.playStepSound(pos, block);
      }
   }

   public float getBrightness() {
      return 1.0F;
   }

   public int getBrightnessForRender() {
      return 15728880;
   }
}
