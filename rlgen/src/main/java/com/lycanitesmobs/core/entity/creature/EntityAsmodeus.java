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
import com.lycanitesmobs.api.IGroupBoss;
import com.lycanitesmobs.api.IGroupHeavy;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.goals.GoalConditions;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.actions.FindNearbyPlayersGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FaceTargetGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.HealWhenNoPlayersGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.SummonMinionsGoal;
import com.lycanitesmobs.core.entity.navigate.ArenaNode;
import com.lycanitesmobs.core.entity.navigate.ArenaNodeNetwork;
import com.lycanitesmobs.core.entity.navigate.ArenaNodeNetworkGrid;
import com.lycanitesmobs.core.entity.projectile.EntityDevilGatling;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import java.util.ArrayList;
import java.util.List;


public class EntityAsmodeus extends BaseCreatureEntity implements Enemy, IGroupHeavy, IGroupBoss {
   protected static final EntityDataAccessor<Byte> ANIMATION_STATES = SynchedEntityData.defineId(EntityAsmodeus.class, EntityDataSerializers.BYTE);
   public AttackRangedGoal aiRangedAttack;
   public boolean firstPlayerTargetCheck = false;
   public List<EntityAstaroth> astarothMinions = new ArrayList<>();
   public List<EntityGrell> grellMinions = new ArrayList<>();
   public int devilstarStreamTime = 0;
   public int devilstarStreamTimeMax = 100;
   public int devilstarStreamCharge = 400;
   public int devilstarStreamChargeMax = 400;
   public int hellshieldAstarothRespawnTime = 0;
   public int hellshieldAstarothRespawnTimeMax = 30;
   public int rebuildAstarothRespawnTime = 0;
   public int rebuildAstarothRespawnTimeMax = 40;
   public ArenaNodeNetwork arenaNodeNetwork;
   public ArenaNode currentArenaNode;
   public int arenaNodeChangeCooldown = 0;
   public int arenaNodeChangeCooldownMax = 200;
   public int arenaJumpingTime = 0;
   public int arenaJumpingTimeMax = 60;
   protected double jumpHeight = 6.0;

   public EntityAsmodeus(Level world) {
      super(LycEntities.type(EntityAsmodeus.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = true;
      this.setAttackCooldownMax(30);
      this.hasJumpSound = true;
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
      this.tasks.addTask(this.nextIdleGoalIndex, new SummonMinionsGoal(this).setMinionInfo("grigori").setAntiFlight(true));
      this.aiRangedAttack = new AttackRangedGoal(this)
         .setSpeed(1.0)
         .setStaminaTime(200)
         .setStaminaDrainRate(3)
         .setRange(90.0F)
         .setChaseTime(0)
         .setCheckSight(false);
      this.tasks.addTask(this.nextCombatGoalIndex++, this.aiRangedAttack);
      this.tasks
         .addTask(
            this.nextIdleGoalIndex,
            new SummonMinionsGoal(this)
               .setMinionInfo("trite")
               .setSummonRate(60)
               .setSummonCap(3)
               .setPerPlayer(true)
               .setConditions(new GoalConditions().setBattlePhase(0))
         );
      super.initEntityAI();
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(ANIMATION_STATES, (byte)0);
   }

   @Override
   public AABB getRenderBoundingBox() {
      return this.getBoundingBox().inflate(200.0, 50.0, 200.0).offset(0.0, -25.0, 0.0);
   }

   @Override
   public boolean isPersistant() {
      return this.getMasterTarget() != null && this.getMasterTarget() instanceof EntityAsmodeus ? true : super.isPersistant();
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
      this.arenaNodeNetwork = new ArenaNodeNetworkGrid(this.level(), pos, 3, 1, 3, 60);
      this.currentArenaNode = this.arenaNodeNetwork.getClosestNode(this.blockPosition());
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         this.updatePhases();
         this.updateCurrentArenaNode();
         this.updateArenaMovement();
      }

      if (this.level().isClientSide && this.updateTick % 200L == 0L) {
         if (!this.astarothMinions.isEmpty()) {
            for (EntityAstaroth minion : this.astarothMinions.toArray(new EntityAstaroth[this.astarothMinions.size()])) {
               if (minion == null || !minion.isAlive() || minion.getMasterTarget() != this) {
                  this.astarothMinions.remove(minion);
               }
            }
         }

         if (!this.grellMinions.isEmpty()) {
            for (EntityGrell minionx : this.grellMinions.toArray(new EntityGrell[this.grellMinions.size()])) {
               if (minionx == null || !minionx.isAlive() || minionx.getMasterTarget() != this) {
                  this.grellMinions.remove(minionx);
               }
            }
         }
      }

      if (!this.level().isClientSide) {
         byte animationState = 0;
         if (this.aiRangedAttack != null && this.aiRangedAttack.attackOnCooldown) {
            animationState += EntityAsmodeus.ANIMATION_STATES_ID.COOLDOWN.id;
         }

         this.entityData.set(ANIMATION_STATES, animationState);
      }

      if (this.level().isClientSide
         && ((Byte)this.entityData.get(ANIMATION_STATES) & EntityAsmodeus.ANIMATION_STATES_ID.COOLDOWN.id) > 0) {
         BlockPos particlePos = this.getFacingPosition(this, 13.0, (double)(this.getRotationYawHead() - this.getYRot()));

         for (int i = 0; i < 4; i++) {
            this.level()
               .addParticle(ParticleTypes.LARGE_SMOKE, (double)particlePos.getX() + (this.random.nextDouble() - 0.5) * 2.0, (double)particlePos.getY() + (double)this.getBbHeight() * 0.2 + this.random.nextDouble() * 2.0, (double)particlePos.getZ() + (this.random.nextDouble() - 0.5) * 2.0, 0.0, 0.0, 0.0);
         }
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

   public void updateCurrentArenaNode() {
      if (this.hasArenaCenter()) {
         if (this.arenaNodeChangeCooldown > 0) {
            this.arenaNodeChangeCooldown--;
         } else if (this.getTarget() != null && this.getTarget().isAlive()) {
            if (this.currentArenaNode != null) {
               this.setCurrentArenaNode(this.currentArenaNode.getClosestAdjacentNode(this.getTarget().blockPosition()));
            } else {
               this.setCurrentArenaNode(this.arenaNodeNetwork.getClosestNode(this.getTarget().blockPosition()));
            }
         } else {
            this.setCurrentArenaNode(this.arenaNodeNetwork.centralNode);
         }
      }
   }

   public void setCurrentArenaNode(ArenaNode arenaNode) {
      this.arenaNodeChangeCooldown = this.arenaNodeChangeCooldownMax;
      if (this.currentArenaNode != arenaNode) {
         this.currentArenaNode = arenaNode;
         if (this.currentArenaNode != null && this.currentArenaNode.pos != null) {
            this.arenaJumpingTime = this.arenaJumpingTimeMax;
            this.leap(200.0F, this.jumpHeight, this.currentArenaNode.pos);
            if (this.hasJumpSound) {
               this.playJumpSound();
            }
         }
      }
   }

   public void updateArenaMovement() {
      this.noPhysics = false;
      if (this.currentArenaNode != null && this.currentArenaNode.pos != null) {
         if (this.arenaJumpingTime > 0) {
            this.arenaJumpingTime--;
            if (this.updateTick % 4L == 0L) {
               double dropForce = -0.5;
               this.noPhysics = this.getY() > (double)(this.currentArenaNode.pos.getY() + 8);
               if (this.getY() < (double)this.currentArenaNode.pos.getY()) {
                  this.setPos(this.getX(), (double)this.currentArenaNode.pos.getY(), this.getZ());
                  dropForce = 0.0;
               }

               this.leap(200.0F, dropForce, this.currentArenaNode.pos);
            }

            if (this.arenaJumpingTime == 0) {
               this.playStepSound(
                  this.currentArenaNode.pos.below(), this.level().getBlockState(this.currentArenaNode.pos.below()).getBlock()
               );
            }
         } else {
            BlockPos arenaPos = this.currentArenaNode.pos;
            double arenaY = this.getY();
            if (this.level().isEmptyBlock(arenaPos)) {
               arenaY = (double)arenaPos.getY();
            } else if (this.level().isEmptyBlock(arenaPos.offset(0, 1, 0))) {
               arenaY = (double)arenaPos.offset(0, 1, 0).getY();
            }

            if (this.getX() != (double)arenaPos.getX()
               || this.getY() != arenaY
               || this.getZ() != (double)arenaPos.getZ()) {
               this.setPos((double)arenaPos.getX(), arenaY, (double)arenaPos.getZ());
            }
         }
      }
   }

   public void updatePhases() {
      int playerCount = Math.max(this.playerTargets.size(), 1);
      if (this.getBattlePhase() == 0) {
         if (this.devilstarStreamTime > 0) {
            this.devilstarStreamTime--;
            if (this.updateTick % 10L == 0L) {
               for (float angle = 0.0F; angle < 360.0F; angle += 10.0F) {
                  this.attackDevilstar(angle);
               }
            }
         } else if (this.devilstarStreamCharge > 0) {
            this.devilstarStreamCharge--;
         } else {
            this.devilstarStreamCharge = this.devilstarStreamChargeMax;
            this.devilstarStreamTime = this.devilstarStreamTimeMax;
         }
      } else if (this.getBattlePhase() == 1 && this.updateTick % 20L == 0L) {
         if (this.astarothMinions.isEmpty() && this.hellshieldAstarothRespawnTime-- <= 0) {
            for (int i = 0; i < 2 * playerCount; i++) {
               EntityAstaroth minion = (EntityAstaroth)CreatureManager.getInstance().getCreature("astaroth").createEntity(this.level());
               this.summonMinion(minion, this.getRandom().nextDouble() * 360.0, 0.0);
               minion.setSizeScale(2.5);
               this.astarothMinions.add(minion);
            }

            this.hellshieldAstarothRespawnTime = this.hellshieldAstarothRespawnTimeMax;
         }
      } else if (this.updateTick % 20L == 0L) {
         if (this.astarothMinions.size() < playerCount * 4 && this.rebuildAstarothRespawnTime-- <= 0) {
            for (int i = 0; i < playerCount; i++) {
               EntityAstaroth minion = (EntityAstaroth)CreatureManager.getInstance().getCreature("astaroth").createEntity(this.level());
               this.summonMinion(minion, this.getRandom().nextDouble() * 360.0, 0.0);
               minion.setSizeScale(2.5);
               this.astarothMinions.add(minion);
            }

            this.rebuildAstarothRespawnTime = this.rebuildAstarothRespawnTimeMax;
         }

         if (this.grellMinions.size() < playerCount * 6 && this.updateTick % 10L * 20L == 0L) {
            for (int i = 0; i < 5 * playerCount; i++) {
               EntityGrell minion = (EntityGrell)CreatureManager.getInstance().getCreature("grell").createEntity(this.level());
               this.summonMinion(minion, this.getRandom().nextDouble() * 360.0, 10.0);
               LycMotion.setPosY(minion, minion.getY() + (double)(10 + this.getRandom().nextInt(20)));
               this.grellMinions.add(minion);
            }
         }

         if (!this.astarothMinions.isEmpty()) {
            float healAmount = (float)this.astarothMinions.size();
            if ((double)((this.getHealth() + healAmount) / this.getMaxHealth()) <= 0.2) {
               this.heal(healAmount * 2.0F);
            }
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
   public boolean canAttackEntity(LivingEntity target) {
      return !(target instanceof EntityTrite) && !(target instanceof EntityGrell) && !(target instanceof EntityAstaroth)
         ? super.canAttackEntity(target)
         : false;
   }

   @Override
   public void attackRanged(Entity target, float range) {
      for (int i = 0; i < 5; i++) {
         this.fireProjectile(EntityDevilGatling.class, target, range, 0.0F, new Vec3(0.0, -24.0, 0.0), 4.0F, 2.0F, 8.0F);
      }

      this.attackHitscan(target, target instanceof Player ? 1.0 : 10.0);
   }

   public void attackDevilstar(float angle) {
      ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("devilstar");
      if (projectileInfo != null) {
         BaseProjectileEntity projectile = projectileInfo.createProjectile(this.level(), this);
         projectile.setProjectileScale(4.0F);
         BlockPos offset = this.getFacingPosition(this, 8.0, (double)angle);
         LycMotion.setPosX(projectile, (double)offset.getX());
         LycMotion.setPosY(projectile, (double)offset.getY() + (double)this.getBbHeight() * 0.5);
         LycMotion.setPosZ(projectile, (double)offset.getZ());
         float range = 20.0F + 20.0F * this.getRandom().nextFloat();
         BlockPos target = this.getFacingPosition(this, (double)range, (double)angle);
         double d0 = (double)target.getX() - projectile.getX();
         double d1 = (double)target.getY() - projectile.getY();
         double d2 = (double)target.getZ() - projectile.getZ();
         float f1 = (float)Math.sqrt(d0 * d0 + d2 * d2) * 0.1F;
         float velocity = 1.2F;
         projectile.shoot(d0, d1 + (double)f1, d2, velocity, 0.0F);
         this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
         this.level().addFreshEntity(projectile);
      }
   }

   @Override
   public void die(DamageSource damageSource) {
      if (!this.level().isClientSide && CreatureManager.getInstance().getCreature("trite").enabled) {
         int j = 6 + this.random.nextInt(20) + this.level().getDifficulty().getId() * 4;

         for (int k = 0; k < j; k++) {
            float f = ((float)(k % 2) - 0.5F) * this.getBbWidth() / 4.0F;
            float f1 = ((float)(k / 2) - 0.5F) * this.getBbWidth() / 4.0F;
            EntityTrite trite = (EntityTrite)CreatureManager.getInstance().getCreature("trite").createEntity(this.level());
            trite.moveTo(
               this.getX() + (double)f, this.getY() + 0.5, this.getZ() + (double)f1, this.random.nextFloat() * 360.0F, 0.0F
            );
            trite.setMinion(true);
            trite.applyVariant(this.getVariantIndex());
            this.level().addFreshEntity(trite);
            if (this.getTarget() != null) {
               trite.setLastHurtByMob(this.getTarget());
            }
         }
      }

      super.die(damageSource);
   }

   @Override
   public void onMinionDeath(LivingEntity minion, DamageSource damageSource) {
      if (minion instanceof EntityAstaroth && this.astarothMinions.contains(minion)) {
         this.astarothMinions.remove(minion);
      }

      if (minion instanceof EntityGrell && this.grellMinions.contains(minion)) {
         this.grellMinions.remove(minion);
      }

      super.onMinionDeath(minion, damageSource);
   }

   @Override
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return this.isBlocking() ? false : super.isDamageTypeApplicable(type, source, damage);
   }

   @Override
   public boolean canBurn() {
      return false;
   }

   @Override
   public boolean isBlocking() {
      return this.level().isClientSide ? super.isBlocking() : this.getBattlePhase() == 1 && !this.astarothMinions.isEmpty();
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

   public void readFromNBT(CompoundTag nbtTagCompound) {
      super.readFromNBT(nbtTagCompound);
      if (nbtTagCompound.contains("DevilstarStreamCharge")) {
         this.devilstarStreamCharge = nbtTagCompound.getInt("DevilstarStreamCharge");
      }

      if (nbtTagCompound.contains("DevilstarStreamTime")) {
         this.devilstarStreamTime = nbtTagCompound.getInt("DevilstarStreamTime");
      }

      if (nbtTagCompound.contains("AstarothIDs")) {
         ListTag astarothIDs = nbtTagCompound.getList("AstarothIDs", 10);

         for (int i = 0; i < astarothIDs.size(); i++) {
            CompoundTag astarothID = astarothIDs.getCompound(i);
            if (astarothID.contains("ID")) {
               Entity entity = this.level().getEntity(astarothID.getInt("ID"));
               if (entity != null && entity instanceof EntityAstaroth) {
                  this.astarothMinions.add((EntityAstaroth)entity);
               }
            }
         }
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.addAdditionalSaveData(nbtTagCompound);
      nbtTagCompound.putInt("DevilstarStreamCharge", this.devilstarStreamCharge);
      nbtTagCompound.putInt("DevilstarStreamTime", this.devilstarStreamTime);
      if (this.getBattlePhase() > 0) {
         ListTag astarothIDs = new ListTag();

         for (EntityAstaroth entityAstaroth : this.astarothMinions) {
            CompoundTag astarothID = new CompoundTag();
            astarothID.putInt("ID", entityAstaroth.getId());
            astarothIDs.add(astarothID);
         }

         nbtTagCompound.put("AstarothIDs", astarothIDs);
      }
   }

   public float getBrightness() {
      return 1.0F;
   }

   public int getBrightnessForRender() {
      return 15728880;
   }

   public static enum ANIMATION_STATES_ID {
      SNAP_TO_ARENA((byte)1),
      COOLDOWN((byte)2);

      public final byte id;

      private ANIMATION_STATES_ID(byte value) {
         this.id = value;
      }

      public byte getAttributeValue() {
         return this.id;
      }
   }
}
