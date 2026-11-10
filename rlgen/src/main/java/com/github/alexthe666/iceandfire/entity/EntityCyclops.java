package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIFleeSun;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAILookIdle;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIRestrictSun;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWanderAvoidWater;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWatchClosest;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.ai.CyclopsAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.CyclopsAITargetSheepPlayers;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityCyclops. */
public class EntityCyclops extends Monster implements IAnimatedEntity, IBlacklistedFromStatues, IVillagerFear {
   /** 1.12 EntityLiving.tasks / targetTasks, run by the 1.12 scheduler. */
   public final EntityAITasks tasks = new EntityAITasks();
   public final EntityAITasks targetTasks = new EntityAITasks();

   private int animationTick;
   private Animation currentAnimation;
   public EntityCyclopsEye eyeEntity;
   private static final EntityDataAccessor<Boolean> BLINDED = SynchedEntityData.defineId(EntityCyclops.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(EntityCyclops.class, EntityDataSerializers.INT);
   public static final Animation ANIMATION_STOMP = Animation.create(27);
   public static final Animation ANIMATION_EATPLAYER = Animation.create(40);
   public static final Animation ANIMATION_KICK = Animation.create(20);
   public static final Animation ANIMATION_ROAR = Animation.create(30);
   public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("cyclops"));

   public EntityCyclops(EntityType<? extends EntityCyclops> type, Level worldIn) {
      super(type, worldIn);
      Legacy112.install(this, this.tasks, this.targetTasks);
      this.initEntityAI();
      this.setPathfindingMalus(PathType.WATER, -1.0F);
      this.setPathfindingMalus(PathType.FENCE, 0.0F);
      this.eyeEntity = new EntityCyclopsEye(this, 0.2F, 0.0F, 7.4F, 1.2F, 0.5F, 1.0F);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.35)
         .add(Attributes.ATTACK_DAMAGE, IceAndFire.CONFIG.cyclopsAttackStrength)
         .add(Attributes.MAX_HEALTH, IceAndFire.CONFIG.cyclopsMaxHealth)
         .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
         .add(Attributes.ARMOR, 20.0)
         .add(Attributes.STEP_HEIGHT, 2.0);
   }

   @Override
   protected int getBaseExperienceReward() {
      return 20 + this.level().random.nextInt(15);
   }

   /** 1.12 initEntityAI. */
   protected void initEntityAI() {
      this.tasks.addTask(2, new EntityAIRestrictSun(this));
      this.tasks.addTask(3, new EntityAIFleeSun(this, 1.0));
      this.tasks.addTask(3, new CyclopsAIAttackMelee(this, 1.0, false));
      this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, Player.class, 8.0F, 1.0F));
      this.tasks.addTask(6, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
      this.targetTasks
         .addTask(
            2,
            new EntityAINearestAttackableTarget<>(
               this,
               LivingEntity.class,
               0,
               true,
               true,
               entity -> !EntityGorgon.isStoneMob(entity)
                     && LandUtils.isAlive(entity)
                     && !(entity instanceof WaterAnimal)
                     && !(entity instanceof Player)
                     && !(entity instanceof EntityCyclops)
                     && !LandUtils.isAnimaniaSheep(entity)
                     && (!(entity instanceof Animal) || entity instanceof Wolf || entity instanceof PolarBear || LandUtils.isDragon(entity))
                  || entity instanceof EntityGorgon
                  || entity instanceof Villager
            )
         );
      this.targetTasks.addTask(3, new CyclopsAITargetSheepPlayers<>(this, Player.class, 0, true, true, entity -> true));
   }

   /** collideWithEntity: sheep are not pushed by the shepherd. */
   @Override
   protected void doPush(Entity entityIn) {
      if (!LandUtils.isAnimaniaSheep(entityIn)) {
         entityIn.push(this);
      }
   }

   @Override
   public boolean doHurtTarget(Entity entityIn) {
      int attackDescision = this.getRandom().nextInt(3);
      if (attackDescision == 0) {
         this.setAnimation(ANIMATION_STOMP);
         return true;
      } else if (attackDescision != 1) {
         this.setAnimation(ANIMATION_KICK);
         return true;
      } else {
         if (!entityIn.hasPassenger(this) && entityIn.getBbWidth() < 1.95F && !LandUtils.isDragon(entityIn)) {
            this.setAnimation(ANIMATION_EATPLAYER);
            entityIn.startRiding(this, true);
         } else {
            this.setAnimation(ANIMATION_STOMP);
         }

         return true;
      }
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(BLINDED, false);
      builder.define(VARIANT, 0);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putBoolean("Blind", this.isBlinded());
      compound.putInt("Variant", this.getVariant());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.setBlinded(compound.getBoolean("Blind"));
      this.setVariant(compound.getInt("Variant"));
   }

   public int getVariant() {
      return this.entityData.get(VARIANT);
   }

   public void setVariant(int variant) {
      this.entityData.set(VARIANT, variant);
   }

   public void setBlinded(boolean blind) {
      this.entityData.set(BLINDED, blind);
   }

   public boolean isBlinded() {
      return this.entityData.get(BLINDED);
   }

   @Override
   public boolean removeWhenFarAway(double distance) {
      return false;
   }

   /** updatePassenger */
   @Override
   protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
      super.positionRider(passenger, callback);
      if (this.hasPassenger(passenger)) {
         passenger.setDeltaMovement(0.0, passenger.getDeltaMovement().y, 0.0);
         this.setAnimation(ANIMATION_EATPLAYER);
         double raiseUp = this.getAnimationTick() < 10 ? 0.0 : Math.min((double)(this.getAnimationTick() * 3 - 30) * 0.2, 5.2F);
         float pullIn = this.getAnimationTick() < 15 ? 0.0F : Math.min((float)(this.getAnimationTick() - 15) * 0.15F, 0.75F);
         this.yBodyRot = this.getYRot();
         this.setYRot(this.getYRot() * 0.0F);
         float radius = -2.75F + pullIn;
         float angle = (float)(Math.PI / 180.0) * this.yBodyRot + 3.15F;
         double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
         double extraZ = (double)(radius * Mth.cos(angle));
         passenger.setPos(this.getX() + extraX, this.getY() + raiseUp, this.getZ() + extraZ);
         if (this.getAnimationTick() == 32) {
            passenger.hurt(
               this.damageSources().mobAttack(this),
               passenger instanceof Player
                  ? (float)IceAndFire.CONFIG.cyclopsBiteStrength
                  : (passenger instanceof LivingEntity ? ((LivingEntity)passenger).getMaxHealth() * 2.0F : (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F)
            );
            passenger.stopRiding();
         }
      }
   }

   @Override
   public void travel(Vec3 travelVector) {
      if (this.getAnimation() == ANIMATION_EATPLAYER) {
         super.travel(Vec3.ZERO);
      } else {
         super.travel(travelVector);
      }
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   public boolean shouldRiderSit() {
      return false;
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.isBlinded() && this.getTarget() != null && this.distanceToSqr(this.getTarget()) > 6.0) {
         this.setTarget(null);
      }

      if (this.getAnimation() == ANIMATION_ROAR && this.getAnimationTick() == 5) {
         this.playSound(IafLand.sound("cyclops_blinded"), 1.0F, 1.0F);
      }

      if (this.getAnimation() == ANIMATION_EATPLAYER && this.getAnimationTick() == 25) {
         this.playSound(IafLand.sound("cyclops_bite"), 1.0F, 1.0F);
      }

      if (this.getAnimation() == ANIMATION_STOMP && this.getTarget() != null && this.distanceToSqr(this.getTarget()) < 12.0 && this.getAnimationTick() == 14) {
         this.getTarget().hurt(this.damageSources().mobAttack(this), (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
      }

      if (this.getAnimation() == ANIMATION_KICK && this.getTarget() != null && this.distanceToSqr(this.getTarget()) < 14.0 && this.getAnimationTick() == 12) {
         this.getTarget().hurt(this.damageSources().mobAttack(this), (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
         this.getTarget().knockback(2.0, 1.0, 1.0);
      }

      if (this.getAnimation() != ANIMATION_EATPLAYER && this.getTarget() != null && !this.getPassengers().isEmpty() && this.getPassengers().contains(this.getTarget())) {
         this.setAnimation(ANIMATION_EATPLAYER);
      }

      if (this.getAnimation() == NO_ANIMATION && this.getTarget() != null && this.getRandom().nextInt(100) == 0) {
         this.setAnimation(ANIMATION_ROAR);
      }

      if (this.getAnimation() == ANIMATION_STOMP && this.getAnimationTick() == 14) {
         for (int i1 = 0; i1 < 20; i1++) {
            double motionX = this.getRandom().nextGaussian() * 0.07;
            double motionY = this.getRandom().nextGaussian() * 0.07;
            double motionZ = this.getRandom().nextGaussian() * 0.07;
            float radius = -1.5F;
            float angle = (float)(Math.PI / 180.0) * this.yBodyRot + (float)i1 * 1.0F;
            double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
            double extraY = 0.8F;
            double extraZ = (double)(radius * Mth.cos(angle));
            BlockState iblockstate = this.level()
               .getBlockState(new BlockPos(Mth.floor(this.getX() + extraX), Mth.floor(this.getY() + extraY) - 1, Mth.floor(this.getZ() + extraZ)));
            if (!iblockstate.isAir() && this.level().isClientSide) {
               this.level()
                  .addParticle(
                     new BlockParticleOption(ParticleTypes.BLOCK, iblockstate), true, this.getX() + extraX, this.getY() + extraY, this.getZ() + extraZ, motionX, motionY, motionZ
                  );
            }
         }
      }

      AnimationHandler.INSTANCE.updateAnimations(this);
      this.eyeEntity.onUpdate();
      this.breakBlock();
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData livingdata) {
      livingdata = super.finalizeSpawn(level, difficulty, spawnType, livingdata);
      this.setVariant(this.getRandom().nextInt(4));
      return livingdata;
   }

   public void breakBlock() {
      if (IceAndFire.CONFIG.cyclopsGriefing) {
         for (int a = (int)Math.round(this.getBoundingBox().minX) - 1; a <= (int)Math.round(this.getBoundingBox().maxX) + 1; a++) {
            for (int b = (int)Math.round(this.getBoundingBox().minY) + 1; b <= (int)Math.round(this.getBoundingBox().maxY) + 2 && b <= 127; b++) {
               for (int c = (int)Math.round(this.getBoundingBox().minZ) - 1; c <= (int)Math.round(this.getBoundingBox().maxZ) + 1; c++) {
                  BlockPos pos = new BlockPos(a, b, c);
                  BlockState state = this.level().getBlockState(pos);
                  Block block = state.getBlock();
                  if (!state.isAir()
                     && !(block instanceof BushBlock)
                     && !(block instanceof LiquidBlock)
                     && block != Blocks.BEDROCK
                     && (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS))) {
                     this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                     if (block != Blocks.AIR && !this.level().isClientSide) {
                        this.level().destroyBlock(pos, true);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public int getAnimationTick() {
      return this.animationTick;
   }

   @Override
   public void setAnimationTick(int tick) {
      this.animationTick = tick;
   }

   @Override
   public Animation getAnimation() {
      return this.currentAnimation;
   }

   @Override
   public void setAnimation(Animation animation) {
      this.currentAnimation = animation;
   }

   @Override
   public Animation[] getAnimations() {
      return new Animation[]{NO_ANIMATION, ANIMATION_STOMP, ANIMATION_EATPLAYER, ANIMATION_KICK, ANIMATION_ROAR};
   }

   public boolean isBlinking() {
      return this.tickCount % 50 > 40 && !this.isBlinded();
   }

   public void onHitEye(DamageSource source, float damage) {
      if (!this.isBlinded()) {
         this.setBlinded(true);
         this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(6.0);
         this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35);
         this.setAnimation(ANIMATION_ROAR);
         this.hurt(source, damage * 3.0F);
      }
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return LOOT;
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return IafLand.sound("cyclops_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return IafLand.sound("cyclops_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return IafLand.sound("cyclops_die");
   }

   @Override
   public boolean canBeTurnedToStone() {
      return !this.isBlinded();
   }
}
