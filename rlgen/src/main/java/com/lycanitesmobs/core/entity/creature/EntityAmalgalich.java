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
import com.lycanitesmobs.api.IGroupBoss;
import com.lycanitesmobs.api.IGroupHeavy;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.GoalConditions;
import com.lycanitesmobs.core.entity.goals.actions.FindNearbyPlayersGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.EffectAuraGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FaceTargetGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FireProjectilesGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.ForceGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.GrowGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.HealWhenNoPlayersGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.SuicideGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.SummonMinionsGoal;
import com.lycanitesmobs.core.entity.goals.targeting.CopyMasterAttackTargetGoal;
import com.lycanitesmobs.core.info.CreatureManager;


public class EntityAmalgalich extends BaseCreatureEntity implements Enemy, IGroupHeavy, IGroupBoss {
   private ForceGoal consumptionGoalP0;
   private ForceGoal consumptionGoalP2;
   private int consumptionDuration = 300;
   private int consumptionWindUp = 60;
   private int consumptionAnimationTime = 0;

   public EntityAmalgalich(Level world) {
      super(LycEntities.type(EntityAmalgalich.class), world);
      this.attribute = EnumCreatureAttribute.UNDEAD;
      this.hasAttackSound = true;
      this.setAttackCooldownMax(30);
      this.hasJumpSound = false;
      this.trueSight = true;
      this.setupMob();
      this.hitAreaWidthScale = 2.0F;
      this.damageMax = BaseCreatureEntity.BOSS_DAMAGE_LIMIT;
      this.damageLimit = (float)BaseCreatureEntity.BOSS_DAMAGE_LIMIT;
   }

   @Override
   protected void initEntityAI() {
      this.targetTasks.addTask(this.nextFindTargetIndex, new FindNearbyPlayersGoal(this));
      this.consumptionDuration = 300;
      this.consumptionWindUp = 60;
      this.consumptionAnimationTime = 0;
      int consumptionGoalCooldown = 400;
      this.tasks.addTask(this.nextIdleGoalIndex, new FaceTargetGoal(this));
      this.tasks.addTask(this.nextIdleGoalIndex, new HealWhenNoPlayersGoal(this));
      this.tasks.addTask(this.nextIdleGoalIndex, new SummonMinionsGoal(this).setMinionInfo("banshee").setAntiFlight(true));
      this.tasks
         .addTask(
            this.nextIdleGoalIndex,
            new FireProjectilesGoal(this)
               .setProjectile("spectralbolt")
               .setFireRate(40)
               .setVelocity(1.0F)
               .setScale(8.0F)
               .setAllPlayers(true)
               .setOverhead(true)
               .setOffset(new Vec3(0.0, 6.0, 0.0))
         );
      this.tasks
         .addTask(
            this.nextIdleGoalIndex,
            new FireProjectilesGoal(this)
               .setProjectile("spectralbolt")
               .setFireRate(60)
               .setVelocity(1.0F)
               .setScale(8.0F)
               .setOverhead(true)
               .setOffset(new Vec3(0.0, 8.0, 0.0))
         );
      this.tasks
         .addTask(
            this.nextIdleGoalIndex, new EffectAuraGoal(this).setEffect("decay").setAmplifier(0).setEffectSeconds(5).setRange(52.0F).setCheckSight(false)
         );
      this.tasks
         .addTask(
            this.nextIdleGoalIndex,
            new SummonMinionsGoal(this)
               .setMinionInfo("reaper")
               .setSummonRate(100)
               .setSummonCap(5)
               .setPerPlayer(true)
               .setConditions(new GoalConditions().setBattlePhase(0))
         );
      this.consumptionGoalP0 = new ForceGoal(this)
         .setRange(64.0F)
         .setCooldown(consumptionGoalCooldown)
         .setDuration(this.consumptionDuration)
         .setWindUp(this.consumptionWindUp)
         .setForce(-0.75F)
         .setPhase(0)
         .setDismount(true);
      this.tasks
         .addTask(
            this.nextIdleGoalIndex,
            new EffectAuraGoal(this)
               .setRange(1.0F)
               .setCooldown(consumptionGoalCooldown + this.consumptionWindUp)
               .setDuration(this.consumptionDuration - this.consumptionWindUp)
               .setTickRate(5)
               .setDamageAmount(1000.0F)
               .setCheckSight(false)
               .setTargetTypes((byte)(BaseCreatureEntity.TARGET_TYPES.ALLY.id | BaseCreatureEntity.TARGET_TYPES.ENEMY.id))
               .setConditions(new GoalConditions().setBattlePhase(0))
         );
      this.tasks.addTask(this.nextIdleGoalIndex, this.consumptionGoalP0);
      this.tasks
         .addTask(
            this.nextIdleGoalIndex,
            new SummonMinionsGoal(this)
               .setMinionInfo("geist")
               .setSummonRate(100)
               .setSummonCap(5)
               .setPerPlayer(true)
               .setConditions(new GoalConditions().setBattlePhase(1))
         );
      this.tasks
         .addTask(
            this.nextIdleGoalIndex,
            new SummonMinionsGoal(this)
               .setMinionInfo("epion")
               .setSummonRate(100)
               .setSummonCap(3)
               .setPerPlayer(true)
               .setConditions(new GoalConditions().setBattlePhase(1))
         );
      this.consumptionGoalP2 = new ForceGoal(this)
         .setRange(64.0F)
         .setCooldown(consumptionGoalCooldown)
         .setDuration(this.consumptionDuration)
         .setWindUp(this.consumptionWindUp)
         .setForce(-0.75F)
         .setPhase(2)
         .setDismount(true);
      this.tasks.addTask(this.nextIdleGoalIndex, this.consumptionGoalP2);
      this.tasks
         .addTask(
            this.nextIdleGoalIndex,
            new EffectAuraGoal(this)
               .setRange(1.0F)
               .setCooldown(consumptionGoalCooldown + this.consumptionWindUp)
               .setDuration(this.consumptionDuration - this.consumptionWindUp)
               .setTickRate(5)
               .setDamageAmount(1000.0F)
               .setCheckSight(false)
               .setTargetTypes((byte)(BaseCreatureEntity.TARGET_TYPES.ALLY.id | BaseCreatureEntity.TARGET_TYPES.ENEMY.id))
               .setConditions(new GoalConditions().setBattlePhase(2))
         );
      this.tasks
         .addTask(
            this.nextIdleGoalIndex,
            new FireProjectilesGoal(this)
               .setProjectile("lobdarklings")
               .setFireRate(200)
               .setVelocity(0.8F)
               .setScale(2.0F)
               .setRandomCount(3)
               .setAngle(360)
               .setPhase(2)
         );
      super.initEntityAI();
   }

   @Override
   public AABB getRenderBoundingBox() {
      return this.getBoundingBox().inflate(200.0, 50.0, 200.0).move(0.0, -25.0, 0.0);
   }

   @Override
   public void onFirstSpawn() {
      super.onFirstSpawn();
      if (this.getArenaCenter() == null) {
         this.setArenaCenter(this.blockPosition());
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.hasArenaCenter()) {
         BlockPos arenaPos = this.getArenaCenter();
         double arenaY = this.getY();
         if (this.level().isEmptyBlock(arenaPos)) {
            arenaY = (double)arenaPos.getY();
         } else if (this.level().isEmptyBlock(arenaPos.offset(0, 1, 0))) {
            arenaY = (double)arenaPos.offset(0, 1, 0).getY();
         }

         if (this.getX() != (double)arenaPos.getX() || this.getY() != arenaY || this.getZ() != (double)arenaPos.getZ()) {
            this.setPos((double)arenaPos.getX(), arenaY, (double)arenaPos.getZ());
         }
      }

      if (this.level().isClientSide) {
         if (!this.extraAnimation01()) {
            this.consumptionAnimationTime = this.consumptionDuration;
         } else {
            this.consumptionAnimationTime--;
         }
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

   @Override
   public boolean rollWanderChance() {
      return false;
   }

   @Override
   public boolean canBePushed() {
      return false;
   }

   @Override
   public boolean canAttackEntity(LivingEntity target) {
      return !(target instanceof EntityBanshee) && !(target instanceof EntityReaper) && !(target instanceof EntityGeist)
         ? super.canAttackEntity(target)
         : false;
   }

   @Override
   public void attackRanged(Entity target, float range) {
      this.fireProjectile("spectralbolt", target, range, 0.0F, new Vec3(0.0, 8.0, 0.0), 1.2F, 6.0F, 0.0F);
      super.attackRanged(target, range);
   }

   public float getConsumptionAnimation() {
      if (this.consumptionAnimationTime >= this.consumptionDuration) {
         return 0.0F;
      } else {
         int windUpThreshhold = this.consumptionDuration - this.consumptionWindUp;
         if (this.consumptionAnimationTime > windUpThreshhold) {
            return 1.0F - (float)(this.consumptionAnimationTime - windUpThreshhold) / (float)this.consumptionWindUp;
         } else {
            float finishingTime = (float)this.consumptionWindUp / 2.0F;
            return (float)this.consumptionAnimationTime < finishingTime ? (float)this.consumptionAnimationTime / finishingTime : 1.0F;
         }
      }
   }

   @Override
   public boolean extraAnimation01() {
      if (this.level().isClientSide) {
         return super.extraAnimation01();
      } else if (this.getBattlePhase() == 0) {
         return this.consumptionGoalP0.cooldownTime <= 0;
      } else {
         return this.getBattlePhase() == 2 ? this.consumptionGoalP2.cooldownTime <= 0 : super.extraAnimation01();
      }
   }

   @Override
   public boolean addMinion(LivingEntity minion) {
      boolean minionAdded = super.addMinion(minion);
      if (minionAdded && minion instanceof BaseCreatureEntity) {
         BaseCreatureEntity minionCreature = (BaseCreatureEntity)minion;
         minionCreature.targetTasks.addTask(minionCreature.nextFindTargetIndex++, new CopyMasterAttackTargetGoal(minionCreature));
         if (minion instanceof EntityGeist) {
            minionCreature.tasks.addTask(minionCreature.nextIdleGoalIndex++, new GrowGoal(minionCreature).setGrowthAmount(0.1F).setTickRate(20));
            minionCreature.tasks.addTask(minionCreature.nextIdleGoalIndex++, new SuicideGoal(minionCreature).setCountdown(400));
         }
      }

      return minionAdded;
   }

   @Override
   public void onMinionDeath(LivingEntity minion, DamageSource damageSource) {
      super.onMinionDeath(minion, damageSource);
      if (minion instanceof EntityEpion) {
         int extinguishWidth = 10;
         int extinguishHeight = 30;
         if (!this.level().isClientSide) {
            for (int x = (int)minion.getX() - extinguishWidth; x <= (int)minion.getX() + extinguishWidth; x++) {
               for (int y = (int)minion.getY() - extinguishHeight; y <= (int)minion.getY() + 2; y++) {
                  for (int z = (int)minion.getZ() - extinguishWidth; z <= (int)minion.getZ() + extinguishWidth; z++) {
                     Block block = this.level().getBlockState(LycPos.of(x, y, z)).getBlock();
                     if (block == ObjectManager.getBlock("shadowfire")) {
                        BlockPos placePos = LycPos.of(x, y, z);
                        this.level().removeBlock(placePos, false);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void onTryToDamageMinion(LivingEntity minion, float damageAmount) {
      super.onTryToDamageMinion(minion, damageAmount);
      if (damageAmount >= 1000.0F) {
         minion.discard();
         this.heal(25.0F);
      }
   }

   @Override
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return this.isBlocking() ? true : super.isDamageTypeApplicable(type, source, damage);
   }

   @Override
   public boolean canBurn() {
      return false;
   }

   @Override
   public boolean isBlocking() {
      return super.isBlocking();
   }

   @Override
   public boolean canAttackWhileBlocking() {
      return true;
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

   public void load(CompoundTag nbtTagCompound) {
      super.load(nbtTagCompound);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.addAdditionalSaveData(nbtTagCompound);
   }

   public float getBrightness() {
      return 1.0F;
   }

   public int getBrightnessForRender() {
      return 15728880;
   }
}
