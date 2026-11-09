package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.EntityAIWatchClosestIgnoreRider;
import com.github.alexthe666.iceandfire.entity.ai.FlyingAITarget;
import com.github.alexthe666.iceandfire.entity.ai.PathNavigateAmphibious;
import com.github.alexthe666.iceandfire.entity.ai.SeaSerpentAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.SeaSerpentAIGetInWater;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.LegacyMoveControl;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.LegacyTasksGoal;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.ilexiconn.llibrary.server.entity.multipart.IMultipartEntity;
import net.ilexiconn.llibrary.server.entity.multipart.PartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntitySeaSerpent. */
public class EntitySeaSerpent extends Animal implements IAnimatedEntity, IMultipartEntity, IVillagerFear, IAnimalFear {
   public static final String[] VARIANTS = {"blue", "bronze", "deepblue", "green", "purple", "red", "teal"};
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(EntitySeaSerpent.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(EntitySeaSerpent.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Boolean> JUMPING = SynchedEntityData.defineId(EntitySeaSerpent.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> BREATHING = SynchedEntityData.defineId(EntitySeaSerpent.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> ANCIENT = SynchedEntityData.defineId(EntitySeaSerpent.class, EntityDataSerializers.BOOLEAN);
   public int swimCycle;
   private int animationTick;
   private Animation currentAnimation;
   private final SerpentPart[] segments = new SerpentPart[9];
   private float lastScale;
   private boolean isLandNavigator;
   private SwimBehavior swimBehavior = SwimBehavior.WANDER;
   private boolean changedSwimBehavior = false;
   private int ticksCircling;
   public float orbitRadius = 0.0F;
   /** client only: IFChainBuffer */
   public com.github.alexthe666.iceandfire.client.model.IFChainBuffer roll_buffer;
   public com.github.alexthe666.iceandfire.client.model.IFChainBuffer tail_buffer;
   public com.github.alexthe666.iceandfire.client.model.IFChainBuffer head_buffer;
   public com.github.alexthe666.iceandfire.client.model.IFChainBuffer pitch_buffer;
   @Nullable
   public BlockPos orbitPos = null;
   public static final Animation ANIMATION_BITE = Animation.create(15);
   public static final Animation ANIMATION_SPEAK = Animation.create(15);
   public static final Animation ANIMATION_ROAR = Animation.create(40);
   private boolean isArcing = false;
   private float arcingYAdditive = 0.0F;
   public float jumpProgress = 0.0F;
   public float wantJumpProgress = 0.0F;
   public float jumpRot = 0.0F;
   public float breathProgress = 0.0F;
   private int ticksSinceJump = 0;
   private int ticksSinceRoar = 0;
   private int ticksJumping = 0;
   public static final int TIME_BETWEEN_JUMPS = 170;
   public static final int TIME_BETWEEN_ROARS = 300;
   private static final Predicate<Entity> NOT_SEA_SERPENT = e -> e instanceof LivingEntity l && !(e instanceof EntitySeaSerpent) && AirWaterUtils.isAlive(l);
   public boolean attackDecision = false;
   private boolean isBreathing;
   public static final ResourceKey<LootTable> LOOT = AirWaterEntities.loot("sea_serpent");
   public final EntityAITasks tasks = new EntityAITasks();
   public final EntityAITasks targetTasks = new EntityAITasks();

   public EntitySeaSerpent(EntityType<? extends EntitySeaSerpent> type, Level level) {
      super(type, level);
      this.switchNavigator(true);
      this.noCulling = true;
      for (int i = 0; i < this.segments.length; i++) {
         this.segments[i] = new SerpentPart(this);
      }
      this.resetParts(1.0F);
      // parts get the ids right after the serpent's (as the 1.21 ender dragon), so hits on them reach the server
      this.setId(ENTITY_COUNTER.getAndAdd(this.segments.length + 1) + 1);
      this.initEntityAI();
      this.goalSelector.addGoal(0, new LegacyTasksGoal(this, this.targetTasks, this.tasks));
      if (level.isClientSide) {
         this.roll_buffer = AirWaterEntities.clientChainBuffer();
         this.pitch_buffer = AirWaterEntities.clientChainBuffer();
         this.tail_buffer = AirWaterEntities.clientChainBuffer();
         this.head_buffer = AirWaterEntities.clientChainBuffer();
      }
   }

   @Override
   public void setId(int id) {
      super.setId(id);
      for (int i = 0; i < this.segments.length; i++) {
         if (this.segments[i] != null) {
            this.segments[i].setId(id + i + 1);
         }
      }
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createMobAttributes()
         .add(Attributes.ATTACK_DAMAGE, 1.0)
         .add(Attributes.MOVEMENT_SPEED, 0.15)
         .add(Attributes.ARMOR, 3.0)
         .add(Attributes.FOLLOW_RANGE, (double)Math.min(2048, com.github.alexthe666.iceandfire.IceAndFire.CONFIG.dragonTargetSearchLength));
   }

   @Override
   protected void registerGoals() {
   }

   protected void initEntityAI() {
      this.tasks.addTask(0, new AISwimBite());
      this.tasks.addTask(0, new AISwimWander());
      this.tasks.addTask(0, new AISwimCircle());
      this.tasks.addTask(1, new SeaSerpentAIAttackMelee(this, 1.0, true));
      this.tasks.addTask(2, new SeaSerpentAIGetInWater(this, 1.0));
      this.tasks.addTask(3, new EntityAIWatchClosestIgnoreRider(this, LivingEntity.class, 6.0F));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
      this.targetTasks.addTask(2, new FlyingAITarget<>(this, LivingEntity.class, 0, true, false, NOT_SEA_SERPENT));
   }

   private LegacyMoveControl move() {
      return (LegacyMoveControl)this.moveControl;
   }

   private void switchNavigator(boolean onLand) {
      if (onLand) {
         this.moveControl = new LegacyMoveControl(this);
         this.navigation = new PathNavigateAmphibious(this, this.level());
         this.isLandNavigator = true;
      } else {
         this.moveControl = new SwimmingMoveHelper();
         this.navigation = new WaterBoundPathNavigation(this, this.level());
         this.isLandNavigator = false;
      }
   }

   public boolean isDirectPathBetweenPoints(BlockPos pos) {
      return AirWaterUtils.rayTraceBlocks(this, this.position().add(0.0, (double)this.getBbHeight() * 0.5, 0.0),
         new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), false) == null;
   }

   /** EntitySeaSerpent.resetParts: in 1.12 new parts; here the same nine parts get the new radius and size (the level keeps part ids). */
   public void resetParts(float scale) {
      for (int i = 0; i < this.segments.length; i++) {
         if (i > 3) {
            this.segments[i].reset((2.0F - (float)(i + 1) * 0.55F) * scale, 0.5F * scale, 0.5F * scale);
         } else {
            this.segments[i].reset((1.8F - (float)i * 0.5F) * scale, 0.45F * scale, 0.4F * scale);
         }
      }
   }

   @Override
   public boolean isMultipartEntity() {
      return true;
   }

   @Override
   public net.neoforged.neoforge.entity.PartEntity<?>[] getParts() {
      return this.segments;
   }

   @Override
   public void onUpdateParts() {
      for (SerpentPart part : this.segments) {
         part.onUpdate();
      }
   }

   /** EntityAgeable.setScale: the 0.5 x 0.5 box times the serpent's scale */
   @Override
   public EntityDimensions getDefaultDimensions(Pose pose) {
      float s = this.entityData == null ? 1.0F : Math.max(0.01F, this.getSeaSerpentScale());
      return super.getDefaultDimensions(pose).scale(s);
   }

   public void setScaleForAge(boolean par1) {
      if (this.getSeaSerpentScale() != this.lastScale) {
         this.refreshDimensions();
         this.resetParts(this.getSeaSerpentScale());
      }
      this.lastScale = this.getSeaSerpentScale();
   }

   @Override
   public boolean doHurtTarget(Entity entityIn) {
      if (this.getAnimation() != ANIMATION_BITE) {
         this.setAnimation(ANIMATION_BITE);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void tick() {
      super.tick();
      this.setScaleForAge(true);
      this.onUpdateParts();
      if (this.isInWater()) {
         this.spawnParticlesAroundEntity(ParticleTypes.BUBBLE, this, (int)this.getSeaSerpentScale());
         for (Entity entity : this.segments) {
            this.spawnParticlesAroundEntity(ParticleTypes.BUBBLE, entity, (int)this.getSeaSerpentScale());
         }
      }
      if (!this.level().isClientSide && this.level().getDifficulty() == Difficulty.PEACEFUL) {
         this.discard();
      }
      AnimationHandler.INSTANCE.updateAnimations(this);
   }

   private void spawnParticlesAroundEntity(ParticleOptions type, Entity entity, int count) {
      for (int i = 0; i < count; i++) {
         double x = entity.getX() + (double)(this.random.nextFloat() * entity.getBbWidth() * 2.0F) - (double)entity.getBbWidth();
         double y = entity.getY() + 0.5 + (double)(this.random.nextFloat() * entity.getBbHeight());
         double z = entity.getZ() + (double)(this.random.nextFloat() * entity.getBbWidth() * 2.0F) - (double)entity.getBbWidth();
         if (isWaterBlock(this.level(), BlockPos.containing(x, y, z))) {
            this.level().addParticle(type, x, y, z, 0.0, 0.0, 0.0);
         }
      }
   }

   private void spawnSlamParticles(ParticleOptions type) {
      for (int i = 0; (float)i < this.getSeaSerpentScale() * 3.0F; i++) {
         for (int i1 = 0; i1 < 20; i1++) {
            double motionX = this.getRandom().nextGaussian() * 0.07;
            double motionY = this.getRandom().nextGaussian() * 0.07;
            double motionZ = this.getRandom().nextGaussian() * 0.07;
            float radius = 1.25F * this.getSeaSerpentScale();
            float angle = (float)(Math.PI / 180.0) * this.yBodyRot + (float)i1 * 1.0F;
            double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
            double extraY = 0.8F;
            double extraZ = (double)(radius * Mth.cos(angle));
            if (this.level().isClientSide) {
               this.level().addAlwaysVisibleParticle(type, true, this.getX() + extraX, this.getY() + extraY, this.getZ() + extraZ, motionX, motionY, motionZ);
            }
         }
      }
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VARIANT, 0);
      builder.define(SCALE, 0.0F);
      builder.define(JUMPING, false);
      builder.define(BREATHING, false);
      builder.define(ANCIENT, false);
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (SCALE.equals(key)) {
         this.refreshDimensions();
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putInt("Variant", this.getVariant());
      compound.putInt("TicksSinceRoar", this.ticksSinceRoar);
      compound.putFloat("Scale", this.getSeaSerpentScale());
      compound.putBoolean("JumpingOutOfWater", this.isJumpingOutOfWater());
      compound.putBoolean("AttackDecision", this.attackDecision);
      compound.putBoolean("Breathing", this.isBreathing());
      compound.putBoolean("Ancient", this.isAncient());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.setVariant(compound.getInt("Variant"));
      this.ticksSinceRoar = compound.getInt("TicksSinceRoar");
      this.setSeaSerpentScale(compound.getFloat("Scale"));
      this.setJumpingOutOfWater(compound.getBoolean("JumpingOutOfWater"));
      this.attackDecision = compound.getBoolean("AttackDecision");
      this.setBreathing(compound.getBoolean("Breathing"));
      this.setAncient(compound.getBoolean("Ancient"));
   }

   public void setSeaSerpentScale(float scale) {
      this.entityData.set(SCALE, scale);
      this.updateAttributes();
   }

   private void updateAttributes() {
      this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Math.min(0.25, 0.15 * (double)this.getSeaSerpentScale() * (double)this.getAncientModifier()));
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Math.max(4.0, com.github.alexthe666.iceandfire.IceAndFire.CONFIG.seaSerpentAttackStrength * (double)this.getSeaSerpentScale()
         * (double)this.getAncientModifier()));
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(10.0, com.github.alexthe666.iceandfire.IceAndFire.CONFIG.seaSerpentBaseHealth * (double)this.getSeaSerpentScale()
         * (double)this.getAncientModifier()));
      this.heal(30.0F * this.getSeaSerpentScale());
   }

   private float getAncientModifier() {
      return this.isAncient() ? 1.5F : 1.0F;
   }

   public float getSeaSerpentScale() {
      return this.entityData.get(SCALE);
   }

   public int getVariant() {
      return this.entityData.get(VARIANT);
   }

   public boolean isJumpingOutOfWater() {
      return this.entityData.get(JUMPING);
   }

   public boolean isAncient() {
      return this.entityData.get(ANCIENT);
   }

   public boolean isBreathing() {
      if (this.level().isClientSide) {
         boolean breathing = this.entityData.get(BREATHING);
         this.isBreathing = breathing;
         return breathing;
      } else {
         return this.isBreathing;
      }
   }

   public void setVariant(int variant) {
      this.entityData.set(VARIANT, variant);
   }

   public void setJumpingOutOfWater(boolean jump) {
      this.entityData.set(JUMPING, jump);
   }

   public void setAncient(boolean ancient) {
      this.entityData.set(ANCIENT, ancient);
   }

   public void setBreathing(boolean breathing) {
      this.entityData.set(BREATHING, breathing);
      if (!this.level().isClientSide) {
         this.isBreathing = breathing;
      }
   }

   @Override
   public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
      return false;
   }

   private double motionY() {
      return this.getDeltaMovement().y;
   }

   private void addMotion(double x, double y, double z) {
      this.setDeltaMovement(this.getDeltaMovement().add(x, y, z));
   }

   @Override
   public void aiStep() {
      if (!this.level().isClientSide && this.isJumpingOutOfWater() && this.swimBehavior == SwimBehavior.WANDER && this.shouldStopJumping()) {
         this.addMotion(0.0, -0.25, 0.0);
         if (this.isInWater()) {
            this.setJumpingOutOfWater(false);
            this.ticksSinceJump = 0;
         }
      }

      super.aiStep();
      boolean breathing = this.isBreathing() && this.getAnimation() != ANIMATION_BITE && this.getAnimation() != ANIMATION_ROAR;
      boolean jumping = !this.isInWater() && !this.onGround() && this.motionY() >= 0.0;
      boolean wantJumping = false;
      boolean ground = !this.isInWater() && this.onGround();
      boolean prevJumping = this.isJumpingOutOfWater();
      this.ticksSinceRoar++;
      this.ticksSinceJump++;
      if (this.ticksSinceRoar > 300 && this.isAtSurface() && this.getAnimation() != ANIMATION_BITE && this.jumpProgress == 0.0F && !this.isJumpingOutOfWater()) {
         this.setAnimation(ANIMATION_ROAR);
         this.ticksSinceRoar = 0;
      }

      if (this.getAnimation() == ANIMATION_ROAR && this.getAnimationTick() == 1) {
         this.playSound(AirWaterRefs.sound("sea_serpent_roar"), this.getSoundVolume() + 1.0F, 1.0F);
      }

      if (this.getAnimation() == ANIMATION_BITE && this.getAnimationTick() == 5) {
         this.playSound(AirWaterRefs.sound("sea_serpent_bite"), this.getSoundVolume(), 1.0F);
      }

      if (this.isJumpingOutOfWater()) {
         this.ticksJumping++;
      } else {
         this.ticksJumping = 0;
      }

      if (this.isJumpingOutOfWater() && isWaterBlock(this.level(), AirWaterUtils.pos(this).above(2))) {
         this.setJumpingOutOfWater(false);
      }

      if (!this.isJumpingOutOfWater() && !isWaterBlock(this.level(), AirWaterUtils.pos(this).above()) && (this.ticksSinceJump > 170 || this.getTarget() != null)) {
         this.ticksSinceJump = 0;
         this.setJumpingOutOfWater(true);
      }

      if (this.swimCycle < 38) {
         this.swimCycle += 2;
      } else {
         this.swimCycle = 0;
      }

      if (breathing && this.breathProgress < 20.0F) {
         this.breathProgress += 0.5F;
      } else if (!breathing && this.breathProgress > 0.0F) {
         this.breathProgress -= 0.5F;
      }

      if (jumping && this.jumpProgress < 10.0F) {
         this.jumpProgress += 0.5F;
      } else if (!jumping && this.jumpProgress > 0.0F) {
         this.jumpProgress -= 0.5F;
      }

      if (wantJumping && this.wantJumpProgress < 10.0F) {
         this.wantJumpProgress += 2.0F;
      } else if (!wantJumping && this.wantJumpProgress > 0.0F) {
         this.wantJumpProgress -= 2.0F;
      }

      if (this.isJumpingOutOfWater() && this.jumpRot < 1.0F) {
         this.jumpRot += 0.1F;
      } else if (!this.isJumpingOutOfWater() && this.jumpRot > 0.0F) {
         this.jumpRot -= 0.1F;
      }

      if (prevJumping != this.isJumpingOutOfWater() && !this.isJumpingOutOfWater()) {
         this.playSound(AirWaterRefs.sound("sea_serpent_splash"), 5.0F, 0.75F);
         this.spawnSlamParticles(ParticleTypes.FIREWORK);
         this.spawnSlamParticles(ParticleTypes.BUBBLE);
         this.spawnSlamParticles(ParticleTypes.BUBBLE);
         this.spawnSlamParticles(ParticleTypes.BUBBLE);
         this.doSplashDamage();
      }

      if (!ground && this.isLandNavigator) {
         this.switchNavigator(false);
      }

      if (ground && !this.isLandNavigator) {
         this.switchNavigator(true);
      }

      this.yBodyRot = this.getYRot();
      this.setXRot((float)this.motionY() * 20.0F);
      if (this.level().isClientSide) {
         AirWaterEntities.waveBuffer(this.pitch_buffer, 90.0F, 10, 10.0F, 0.5F, this);
         if (!jumping) {
            AirWaterEntities.swingBuffer(this.tail_buffer, 70.0F, 20, 5.0F, this);
            AirWaterEntities.swingBuffer(this.head_buffer, 70.0F, 20, 5.0F, this);
         }
      }

      if (this.changedSwimBehavior) {
         this.changedSwimBehavior = false;
      }

      if (!this.level().isClientSide) {
         if (this.attackDecision) {
            this.setBreathing(false);
         }

         if (this.getTarget() == null || this.getAnimation() == ANIMATION_ROAR) {
            this.setBreathing(false);
         } else if (this.attackDecision) {
            if (this.distanceToSqr(this.getTarget()) > (double)(500.0F * this.getSeaSerpentScale())) {
               this.attackDecision = false;
            }
         } else {
            if (!this.getTarget().isInWater() || !this.isDirectPathBetweenPoints(AirWaterUtils.pos(this.getTarget()))
               || this.distanceToSqr(this.getTarget()) < (double)(60.0F * this.getSeaSerpentScale())) {
               this.attackDecision = true;
            }
            if (!this.attackDecision) {
               this.shoot(this.getTarget());
            }
         }

         if (this.swimBehavior == SwimBehavior.CIRCLE) {
            this.ticksCircling++;
         } else {
            this.ticksCircling = 0;
         }

         if (this.getTarget() != null) {
            if (this.isInWater()) {
               if (this.attackDecision) {
                  if (this.isPreyAtSurface() && this.distanceToSqr(this.getTarget()) < (double)(200.0F * this.getSeaSerpentScale())) {
                     this.swimBehavior = SwimBehavior.JUMP;
                  } else {
                     this.swimBehavior = SwimBehavior.ATTACK;
                  }
               } else {
                  this.swimBehavior = SwimBehavior.ATTACK;
               }
            } else if (this.onGround()) {
               this.swimBehavior = SwimBehavior.ATTACK;
            } else {
               this.swimBehavior = SwimBehavior.WANDER;
            }
         } else if (this.swimBehavior == SwimBehavior.JUMP && this.getTarget() != null
            && this.distanceToSqr(this.getTarget()) > (double)(200.0F * this.getSeaSerpentScale())) {
            this.swimBehavior = SwimBehavior.WANDER;
            this.ticksSinceJump = 0;
            this.setJumpingOutOfWater(false);
         }

         if (this.swimBehavior != SwimBehavior.JUMP && this.swimBehavior != SwimBehavior.ATTACK && this.ticksSinceJump > 170) {
            this.swimBehavior = SwimBehavior.JUMP;
         }

         if (this.swimBehavior != SwimBehavior.ATTACK) {
            this.arcingYAdditive = 0.0F;
         }

         if (this.swimBehavior == SwimBehavior.JUMP && this.motionY() < 0.0 && !this.isInWater()) {
            this.swimBehavior = SwimBehavior.WANDER;
            this.ticksSinceJump = 0;
            this.setJumpingOutOfWater(false);
         }

         if (this.swimBehavior == SwimBehavior.ATTACK && this.getTarget() != null && !this.getTarget().isInWater()) {
            this.swimBehavior = SwimBehavior.WANDER;
            this.lookAt(this.getTarget(), 360.0F, 360.0F);
         }

         if (this.swimBehavior == SwimBehavior.ATTACK && (this.getTarget() == null || !this.isDirectPathBetweenPoints(AirWaterUtils.pos(this.getTarget())))) {
            this.swimBehavior = SwimBehavior.WANDER;
         }
      }

      if (this.getAnimation() == ANIMATION_BITE && this.getTarget() != null
         && (this.isTouchingMob(this.getTarget()) || this.distanceToSqr(this.getTarget()) < 50.0)) {
         this.hurtMob(this.getTarget());
      }

      this.breakBlock();
   }

   private void doSplashDamage() {
      double width = 2.0 * (double)this.getSeaSerpentScale();
      for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(width, width * 0.5, width), NOT_SEA_SERPENT)) {
         if (entity instanceof LivingEntity) {
            entity.hurt(this.damageSources().mobAttack(this), (float)(int)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
            this.destroyBoat(entity);
            double xRatio = this.getX() - entity.getX();
            double zRatio = this.getZ() - entity.getZ();
            float f = Mth.sqrt((float)(xRatio * xRatio + zRatio * zRatio));
            float strength = 0.3F * this.getSeaSerpentScale();
            Vec3 m = entity.getDeltaMovement();
            entity.setDeltaMovement(m.x / 2.0 - xRatio / (double)f * (double)strength, m.y + (double)strength, m.z / 2.0 - zRatio / (double)f * (double)strength);
            entity.hurtMarked = true;
            if (this.motionY() > 0.4F) {
               Vec3 s = this.getDeltaMovement();
               this.setDeltaMovement(s.x, 0.4F, s.z);
            }
         }
      }
   }

   public void destroyBoat(Entity sailor) {
      if (sailor.getVehicle() instanceof Boat boat && !this.level().isClientSide) {
         boat.discard();
         if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            for (int i = 0; i < 3; i++) {
               boat.spawnAtLocation(new ItemStack(boat.getVariant().getPlanks()), 0.0F);
            }
            for (int j = 0; j < 2; j++) {
               boat.spawnAtLocation(new ItemStack(Items.STICK), 0.0F);
            }
         }
      }
   }

   private boolean isPreyAtSurface() {
      if (this.getTarget() != null) {
         BlockPos pos = AirWaterUtils.pos(this.getTarget());
         return !isWaterBlock(this.level(), pos.above((int)Math.ceil((double)this.getTarget().getBbHeight())));
      } else {
         return false;
      }
   }

   private void hurtMob(LivingEntity entity) {
      if (this.getAnimation() == ANIMATION_BITE && entity != null && this.getAnimationTick() > 6) {
         this.getTarget().hurt(this.damageSources().mobAttack(this), (float)(int)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
         this.attackDecision = this.getRandom().nextBoolean();
      }
   }

   public void moveJumping() {
      float velocity = 0.5F;
      double x = (double)(-Mth.sin(this.getYRot() * (float)(Math.PI / 180.0)) * Mth.cos(this.getXRot() * (float)(Math.PI / 180.0)));
      double z = (double)(Mth.cos(this.getYRot() * (float)(Math.PI / 180.0)) * Mth.cos(this.getXRot() * (float)(Math.PI / 180.0)));
      float f = (float)Math.sqrt(x * x + z * z);
      x /= (double)f;
      z /= (double)f;
      this.setDeltaMovement(x * velocity, this.motionY(), z * velocity);
   }

   private boolean isTouchingMob(Entity entity) {
      float s = this.getSeaSerpentScale();
      if (this.getBoundingBox().expandTowards(s * 3.0F, s * 3.0F, s * 3.0F).intersects(entity.getBoundingBox())) {
         return true;
      } else {
         for (Entity segment : this.segments) {
            if (segment.getBoundingBox().expandTowards(s * 2.0F, s * 2.0F, s * 2.0F).intersects(entity.getBoundingBox())) {
               return true;
            }
         }
         return false;
      }
   }

   @Override
   public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
      return false;
   }

   @Override
   public boolean isInWater() {
      return super.isInWater() || this.isEyeInFluid(FluidTags.WATER);
   }

   /** breaks Material.PLANTS blocks around it (flowers, grass, crops; not leaves) */
   public void breakBlock() {
      if (com.github.alexthe666.iceandfire.IceAndFire.CONFIG.seaSerpentGriefing) {
         AABB bb = this.getBoundingBox();
         for (int a = (int)Math.round(bb.minX) - 2; a <= (int)Math.round(bb.maxX) + 2; a++) {
            for (int b = (int)Math.round(bb.minY); b <= (int)Math.round(bb.maxY) + 2 && b <= 127; b++) {
               for (int c = (int)Math.round(bb.minZ) - 2; c <= (int)Math.round(bb.maxZ) + 2; c++) {
                  BlockPos pos = new BlockPos(a, b, c);
                  BlockState state = this.level().getBlockState(pos);
                  if (!state.isAir() && !(state.getBlock() instanceof LiquidBlock) && isPlantMaterial(state) && !this.level().isClientSide) {
                     this.level().destroyBlock(pos, true);
                  }
               }
            }
         }
      }
   }

   /** 1.12 Material.PLANTS: flowers, saplings, crops, mushrooms, lily pads, cocoa, nether wart, small 1.21 water plants */
   private static boolean isPlantMaterial(BlockState state) {
      return state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS) || state.is(BlockTags.CROPS) || state.is(net.minecraft.world.level.block.Blocks.BROWN_MUSHROOM)
         || state.is(net.minecraft.world.level.block.Blocks.RED_MUSHROOM) || state.is(net.minecraft.world.level.block.Blocks.LILY_PAD)
         || state.is(net.minecraft.world.level.block.Blocks.COCOA) || state.is(net.minecraft.world.level.block.Blocks.NETHER_WART);
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
      data = super.finalizeSpawn(level, difficulty, spawnType, data);
      this.setVariant(this.getRandom().nextInt(7));
      boolean ancient = this.getRandom().nextInt(16) == 1;
      if (ancient) {
         this.setAncient(true);
         this.setSeaSerpentScale(6.0F + this.getRandom().nextFloat() * 3.0F);
      } else {
         this.setSeaSerpentScale(1.5F + this.getRandom().nextFloat() * 4.0F);
      }
      return data;
   }

   /** StructureGenerator: sea serpents placed at world generation (6 colours, 1 in 15 ancient) */
   public void onWorldSpawn(RandomSource random) {
      this.setVariant(random.nextInt(6));
      boolean ancient = random.nextInt(15) == 1;
      if (ancient) {
         this.setAncient(true);
         this.setSeaSerpentScale(6.0F + random.nextFloat() * 3.0F);
      } else {
         this.setSeaSerpentScale(1.5F + random.nextFloat() * 4.0F);
      }
   }

   @Override
   public boolean isFood(ItemStack stack) {
      return stack.is(Items.WHEAT);
   }

   @Nullable
   @Override
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
      return null;
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

   /** 1.7.1 left NO_ANIMATION out of this list */
   @Override
   public Animation[] getAnimations() {
      return new Animation[]{ANIMATION_BITE, ANIMATION_ROAR, ANIMATION_SPEAK};
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return AirWaterRefs.sound("sea_serpent_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return AirWaterRefs.sound("sea_serpent_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return AirWaterRefs.sound("sea_serpent_die");
   }

   @Override
   public void playAmbientSound() {
      if (this.getAnimation() == NO_ANIMATION) {
         this.setAnimation(ANIMATION_SPEAK);
      }
      super.playAmbientSound();
   }

   @Override
   protected void playHurtSound(DamageSource source) {
      if (this.getAnimation() == NO_ANIMATION) {
         this.setAnimation(ANIMATION_SPEAK);
      }
      super.playHurtSound(source);
   }

   @Override
   public boolean shouldAnimalsFear(Entity entity) {
      return true;
   }

   public boolean isBlinking() {
      return this.tickCount % 50 > 43;
   }

   private boolean canMove() {
      return true;
   }

   public boolean wantsToJump() {
      return this.ticksSinceJump > 170 && this.swimBehavior == SwimBehavior.JUMP;
   }

   private boolean shouldStopJumping() {
      return this.ticksJumping > 30 || !isWaterBlock(this.level(), this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, AirWaterUtils.pos(this)).below(1));
   }

   public static BlockPos getPositionRelativeToSeafloor(EntitySeaSerpent entity, Level world, double x, double z, RandomSource rand) {
      BlockPos topY = BlockPos.containing(x, entity.getY(), z);
      BlockPos bottomY = BlockPos.containing(x, entity.getY(), z);
      while (isWaterBlock(world, topY) && topY.getY() < world.getMaxBuildHeight()) {
         topY = topY.above();
      }
      while (isWaterBlock(world, bottomY) && bottomY.getY() > world.getMinBuildHeight()) {
         bottomY = bottomY.below();
      }
      if (entity.ticksSinceJump > 170) {
         return topY.above((int)Math.ceil((double)(3.0F * entity.getSeaSerpentScale())));
      } else if (entity.ticksSinceRoar <= 300 && entity.getAnimation() != ANIMATION_ROAR) {
         for (int tries = 0; tries < 5; tries++) {
            BlockPos pos = BlockPos.containing(x, (double)(bottomY.getY() + 1 + rand.nextInt(Math.max(1, topY.getY() - bottomY.getY() - 2))), z);
            if (isWaterBlock(world, pos)) {
               return pos;
            }
         }
         return AirWaterUtils.pos(entity);
      } else {
         return topY.below();
      }
   }

   public static BlockPos getPositionInOrbit(EntitySeaSerpent entity, Level world, BlockPos orbit, RandomSource rand) {
      float possibleOrbitRadius = entity.orbitRadius + 10.0F;
      float radius = 5.0F * entity.getSeaSerpentScale();
      float angle = (float)(Math.PI / 180.0) * possibleOrbitRadius;
      double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
      double extraZ = (double)(radius * Mth.cos(angle));
      BlockPos radialPos = BlockPos.containing((double)orbit.getX() + extraX, (double)orbit.getY(), (double)orbit.getZ() + extraZ);
      entity.orbitRadius = possibleOrbitRadius;
      return radialPos;
   }

   public static BlockPos getPositionPreyArc(EntitySeaSerpent entity, BlockPos target, Level world) {
      float radius = 10.0F * entity.getSeaSerpentScale();
      entity.yBodyRot = entity.getYRot();
      float angle = (float)(Math.PI / 180.0) * entity.yBodyRot;
      double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
      double extraZ = (double)(radius * Mth.cos(angle));
      double signum = Math.signum((double)((float)target.getY() + 0.5F) - entity.getY());
      BlockPos pos = BlockPos.containing((double)target.getX() + extraX, (double)target.getY() + (double)entity.random.nextInt(5) * signum,
         (double)target.getZ() + extraZ);
      entity.isArcing = true;
      return clampBlockPosToWater(entity, world, pos);
   }

   private static BlockPos clampBlockPosToWater(Entity entity, Level world, BlockPos pos) {
      BlockPos topY = BlockPos.containing(pos.getX(), entity.getY(), pos.getZ());
      BlockPos bottomY = BlockPos.containing(pos.getX(), entity.getY(), pos.getZ());
      while (isWaterBlock(world, topY) && topY.getY() < world.getMaxBuildHeight()) {
         topY = topY.above();
      }
      while (isWaterBlock(world, bottomY) && bottomY.getY() > world.getMinBuildHeight()) {
         bottomY = bottomY.below();
      }
      return new BlockPos(pos.getX(), Mth.clamp(pos.getY(), bottomY.getY() + 1, topY.getY() - 1), pos.getZ());
   }

   public static boolean isWaterBlock(Level world, BlockPos pos) {
      return com.github.alexthe666.iceandfire.entity.ai.legacy112.Legacy112.isWaterMaterial(world.getBlockState(pos));
   }

   public boolean isAtSurface() {
      BlockPos pos = AirWaterUtils.pos(this);
      return isWaterBlock(this.level(), pos.below()) && !isWaterBlock(this.level(), pos.above());
   }

   private void shoot(LivingEntity entity) {
      if (!this.attackDecision) {
         if (!this.isInWater()) {
            this.setBreathing(false);
            this.attackDecision = true;
         }
         if (this.isBreathing()) {
            if (this.tickCount % 40 == 0) {
               this.playSound(AirWaterRefs.sound("sea_serpent_breath"), 4.0F, 1.0F);
            }
            if (this.tickCount % 3 == 0) {
               this.setYRot(this.yBodyRot);
               float headPosX = (float)(this.segments[0].getX() + (double)(0.3F * this.getSeaSerpentScale()) * Math.cos((double)(this.getYRot() + 90.0F) * Math.PI / 180.0));
               float headPosZ = (float)(this.segments[0].getZ() + (double)(0.3F * this.getSeaSerpentScale()) * Math.sin((double)(this.getYRot() + 90.0F) * Math.PI / 180.0));
               float headPosY = (float)(this.segments[0].getY() + (double)(0.2F * this.getSeaSerpentScale()));
               double d2 = entity.getX() - (double)headPosX;
               double d3 = entity.getY() - (double)headPosY;
               double d4 = entity.getZ() - (double)headPosZ;
               EntitySeaSerpentBubbles bubbles = new EntitySeaSerpentBubbles(this.level(), this, d2, d3, d4);
               bubbles.setPos(headPosX, headPosY, headPosZ);
               if (!this.level().isClientSide && entity.isAlive()) {
                  this.level().addFreshEntity(bubbles);
               }
               if (!entity.isAlive()) {
                  this.setBreathing(false);
                  this.attackDecision = this.getRandom().nextBoolean();
               }
            }
         } else {
            this.setBreathing(true);
         }
      }
      this.lookAt(entity, 360.0F, 360.0F);
   }

   public String getVariantName() {
      return VARIANTS[Mth.clamp(this.getVariant(), 0, VARIANTS.length - 1)];
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return LOOT;
   }

   /**
    * 1.12 CustomizeToSeaSerpent.apply (loot table sea_serpent, function iceandfire:customize_to_sea_serpent): the scales become this
    * serpent's colour, 1 + rand(1 + ceil(scale * 3 * ancient)); fangs 1 + rand(1 + ceil(scale * 2 * ancient)), ancient = 2 if ancient.
    */
   public static ItemStack customizeLoot(ItemStack stack, net.minecraft.world.entity.Entity looted) {
      if (!stack.isEmpty() && looted instanceof EntitySeaSerpent seaSerpent) {
         java.util.Random random = new java.util.Random();
         int ancientModifier = seaSerpent.isAncient() ? 2 : 1;
         net.minecraft.resources.ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
         if (id.getPath().startsWith("sea_serpent_scales_")) {
            stack.setCount(1 + random.nextInt(1 + (int)Math.ceil((double)(seaSerpent.getSeaSerpentScale() * 3.0F * (float)ancientModifier))));
            return new ItemStack(AirWaterRefs.item("sea_serpent_scales_" + seaSerpent.getVariantName()), stack.getCount());
         }
         if (id.getPath().equals("sea_serpent_fang")) {
            stack.setCount(1 + random.nextInt(1 + (int)Math.ceil((double)(seaSerpent.getSeaSerpentScale() * 2.0F * (float)ancientModifier))));
            return stack;
         }
      }
      return stack;
   }

   @Override
   public void travel(Vec3 move) {
      if (this.isEffectiveAi()) {
         if (this.isInWater()) {
            this.moveRelative(0.1F, move);
            float f4 = 0.6F;
            float d0 = (float)this.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY) * 3.0F;
            if (d0 > 3.0F) {
               d0 = 3.0F;
            }
            if (!this.onGround()) {
               d0 *= 0.5F;
            }
            if (d0 > 0.0F) {
               f4 += (0.54600006F - f4) * d0 / 3.0F;
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x * f4 * 0.900000011920929, m.y * 0.900000011920929 * f4, m.z * 0.900000011920929 * f4);
            this.calculateEntityAnimation(true);
         } else {
            super.travel(move);
         }
      }
   }

   @Override
   public void awardKillScore(Entity killed, int score, DamageSource source) {
      super.awardKillScore(killed, score, source);
      this.attackDecision = this.getRandom().nextBoolean();
   }

   /** a body part (llibrary PartEntity) that can be resized in place */
   public static class SerpentPart extends PartEntity {
      SerpentPart(EntitySeaSerpent parent) {
         super(parent, 0.0F, 0.0F, 0.0F, 0.5F, 0.5F, 1.0F);
      }

      void reset(float radius, float sizeX, float sizeY) {
         this.radius = radius;
         this.setSize(sizeX, sizeY);
      }
   }

   class AISwimBite extends EntityAIBase {
      BlockPos target;
      boolean secondPhase = false;
      boolean isOver = false;
      public int max_distance = 1000;

      AISwimBite() {
         this.setMutexBits(1);
      }

      @Override
      public void resetTask() {
         this.target = null;
         this.secondPhase = false;
         this.isOver = false;
         EntitySeaSerpent.this.isArcing = false;
         EntitySeaSerpent.this.arcingYAdditive = 0.0F;
      }

      @Override
      public boolean shouldExecute() {
         EntitySeaSerpent s = EntitySeaSerpent.this;
         if (!s.attackDecision && s.getTarget() != null && s.distanceToSqr(s.getTarget()) < 300.0) {
            return false;
         } else if ((s.swimBehavior == SwimBehavior.ATTACK || s.swimBehavior == SwimBehavior.JUMP) && s.canMove() && s.getTarget() != null) {
            if (s.isInWater() && s.getTarget() != null) {
               this.target = AirWaterUtils.pos(s.getTarget());
               s.orbitPos = null;
               this.secondPhase = false;
               s.isArcing = false;
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      @Override
      public boolean shouldContinueExecuting() {
         EntitySeaSerpent s = EntitySeaSerpent.this;
         if (this.isOver) {
            return false;
         } else if (!s.attackDecision && s.getTarget() != null && s.distanceToSqr(s.getTarget()) < 300.0) {
            s.move().setWait();
            this.resetTask();
            return false;
         } else if (this.secondPhase) {
            if (s.getTarget() != null && this.target != null && !(this.distTo(this.target) > (double)this.max_distance) && !this.isOver) {
               return true;
            } else {
               s.swimBehavior = SwimBehavior.WANDER;
               this.resetTask();
               return false;
            }
         } else {
            return s.getTarget() != null;
         }
      }

      private double distTo(BlockPos p) {
         // 1.12 getDistanceSq(BlockPos) measured to the block corner
         return EntitySeaSerpent.this.distanceToSqr(p.getX(), p.getY(), p.getZ());
      }

      @Override
      public void updateTask() {
         EntitySeaSerpent s = EntitySeaSerpent.this;
         if (s.swimBehavior == SwimBehavior.JUMP) {
            if (s.getTarget() != null && s.isInWater()) {
               this.target = AirWaterUtils.pos(s.getTarget()).above((int)Math.ceil((double)(3.0F * s.getSeaSerpentScale())));
            }
         } else {
            if (s.getTarget() != null && s.getTarget().isAlive()) {
               double d0 = s.getTarget().getX() - s.getX();
               double d1 = s.getTarget().getZ() - s.getZ();
               double d2 = Math.sqrt(d0 * d0 + d1 * d1);
               s.arcingYAdditive = (float)(this.secondPhase ? 1 : -1) * (float)d2;
            } else {
               this.secondPhase = true;
            }

            if (!this.secondPhase) {
               if (s.getTarget() != null) {
                  this.target = AirWaterUtils.pos(s.getTarget());
               }
               if (!s.attackDecision) {
                  if (s.getTarget() != null) {
                     if (this.distTo(this.target) > (double)(10.0F * s.getSeaSerpentScale())) {
                        s.setBreathing(true);
                     } else {
                        s.attackDecision = true;
                     }
                  }
               } else {
                  if (s.getTarget() != null && (this.distTo(this.target) < (double)(30.0F * s.getSeaSerpentScale()) || s.isTouchingMob(s.getTarget()))) {
                     s.setAnimation(ANIMATION_BITE);
                  }
                  if (s.getTarget() == null || this.distTo(this.target) < (double)(30.0F * s.getSeaSerpentScale())) {
                     this.target = null;
                     this.secondPhase = true;
                  }
               }
            }

            if (this.secondPhase && s.getTarget() != null) {
               if (!(s.distanceToSqr(s.getTarget()) > (double)this.max_distance) && (this.target == null || !(this.distTo(this.target) < 5.0))) {
                  this.target = getPositionPreyArc(s, AirWaterUtils.pos(s.getTarget()), s.level());
               } else {
                  this.resetTask();
                  this.isOver = true;
               }
            }
         }

         if (this.target != null && (isWaterBlock(s.level(), this.target) || s.swimBehavior == SwimBehavior.JUMP)) {
            s.moveControl.setWantedPosition(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 0.5);
            if (s.getTarget() == null) {
               s.getLookControl().setLookAt(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 180.0F, 20.0F);
            }
         }
      }
   }

   class AISwimCircle extends EntityAIBase {
      BlockPos target;

      AISwimCircle() {
         this.setMutexBits(1);
      }

      @Override
      public boolean shouldExecute() {
         EntitySeaSerpent s = EntitySeaSerpent.this;
         if (s.swimBehavior != SwimBehavior.CIRCLE || !s.canMove()) {
            return false;
         } else if (!s.move().isUpdating()) {
            s.swimBehavior = SwimBehavior.WANDER;
            return false;
         } else {
            if (s.isInWater() && !s.isJumpingOutOfWater()) {
               BlockPos gen = this.generateTarget();
               if (gen != null) {
                  s.orbitPos = gen;
                  this.target = getPositionInOrbit(s, s.level(), s.orbitPos, s.random);
                  return s.isDirectPathBetweenPoints(this.target);
               }
               s.swimBehavior = SwimBehavior.WANDER;
            }
            return false;
         }
      }

      protected BlockPos generateTarget() {
         EntitySeaSerpent s = EntitySeaSerpent.this;
         for (int i = 0; i < 5; i++) {
            BlockPos pos = getPositionRelativeToSeafloor(s, s.level(), s.getX() + (double)s.random.nextInt(30) - 15.0, s.getZ() + (double)s.random.nextInt(30) - 15.0,
               s.random);
            if (isWaterBlock(s.level(), pos) && s.isDirectPathBetweenPoints(pos)) {
               return pos;
            }
         }
         return null;
      }

      @Override
      public boolean shouldContinueExecuting() {
         EntitySeaSerpent s = EntitySeaSerpent.this;
         return this.target != null && !s.isDirectPathBetweenPoints(this.target) ? false : s.getTarget() == null && s.swimBehavior == SwimBehavior.CIRCLE;
      }

      @Override
      public void updateTask() {
         EntitySeaSerpent s = EntitySeaSerpent.this;
         if (Math.sqrt(s.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ())) < 5.0) {
            this.target = getPositionInOrbit(s, s.level(), s.orbitPos, s.random);
         }
         if (isWaterBlock(s.level(), this.target) && s.isDirectPathBetweenPoints(this.target)) {
            s.moveControl.setWantedPosition(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 0.25);
            if (s.getTarget() == null) {
               s.getLookControl().setLookAt(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 180.0F, 20.0F);
            }
         }
      }
   }

   public class AISwimWander extends EntityAIBase {
      BlockPos target;

      public AISwimWander() {
         this.setMutexBits(1);
      }

      @Override
      public boolean shouldExecute() {
         EntitySeaSerpent s = EntitySeaSerpent.this;
         if ((s.swimBehavior == SwimBehavior.WANDER || s.swimBehavior == SwimBehavior.JUMP) && s.canMove() && s.getTarget() == null) {
            if (s.isInWater()) {
               BlockPos gen = this.generateTarget();
               if (gen != null) {
                  this.target = gen;
                  s.orbitPos = null;
                  return s.swimBehavior == SwimBehavior.JUMP || !s.move().isUpdating();
               }
            }
            return false;
         } else {
            return false;
         }
      }

      protected BlockPos generateTarget() {
         EntitySeaSerpent s = EntitySeaSerpent.this;
         if (s.swimBehavior == SwimBehavior.JUMP) {
            BlockPos pos = getPositionRelativeToSeafloor(s, s.level(), s.getX(), s.getZ(), s.random);
            return pos.above(3 * (int)Math.ceil((double)s.getSeaSerpentScale()));
         } else {
            for (int i = 0; i < 5; i++) {
               BlockPos pos = getPositionRelativeToSeafloor(s, s.level(), s.getX() + (double)s.random.nextInt(30) - 15.0,
                  s.getZ() + (double)s.random.nextInt(30) - 15.0, s.random);
               if (isWaterBlock(s.level(), pos) && s.isDirectPathBetweenPoints(pos) || s.swimBehavior == SwimBehavior.JUMP) {
                  return pos;
               }
            }
            return null;
         }
      }

      @Override
      public boolean shouldContinueExecuting() {
         return false;
      }

      @Override
      public void startExecuting() {
         EntitySeaSerpent s = EntitySeaSerpent.this;
         if (this.target == null) {
            this.target = this.generateTarget();
         }
         if (this.target != null && (isWaterBlock(s.level(), this.target) || s.swimBehavior == SwimBehavior.JUMP) && s.isDirectPathBetweenPoints(this.target)) {
            s.moveControl.setWantedPosition(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 0.25);
            if (s.getTarget() == null) {
               s.getLookControl().setLookAt(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 180.0F, 20.0F);
            }
         }
      }
   }

   enum SwimBehavior {
      CIRCLE,
      WANDER,
      ATTACK,
      JUMP,
      NONE
   }

   public class SwimmingMoveHelper extends LegacyMoveControl {
      public SwimmingMoveHelper() {
         super(EntitySeaSerpent.this);
      }

      @Override
      public void tick() {
         EntitySeaSerpent s = EntitySeaSerpent.this;
         if (this.isMoveTo()) {
            double d0 = this.wantedX - s.getX();
            double d1 = this.wantedY - s.getY();
            double d2 = this.wantedZ - s.getZ();
            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            if (d3 < 6.0 && s.getTarget() == null) {
               if (!s.changedSwimBehavior && s.swimBehavior == SwimBehavior.WANDER && s.random.nextInt(20) == 0) {
                  s.swimBehavior = SwimBehavior.CIRCLE;
                  s.changedSwimBehavior = true;
               }
               if (!s.changedSwimBehavior && s.swimBehavior == SwimBehavior.CIRCLE && s.random.nextInt(5) == 0 && s.ticksCircling > 150) {
                  s.swimBehavior = SwimBehavior.WANDER;
                  s.changedSwimBehavior = true;
               }
            }
            if (s.swimBehavior == SwimBehavior.JUMP && !s.isInWater() && !s.onGround()) {
               s.ticksSinceJump = 0;
            }
            int dist = s.swimBehavior == SwimBehavior.JUMP ? 10 : 3;
            Vec3 m = s.getDeltaMovement();
            if (d3 < (double)dist && s.getTarget() == null || s.swimBehavior == SwimBehavior.JUMP && s.shouldStopJumping() && s.getTarget() == null) {
               this.setWait();
               s.setDeltaMovement(m.x * 0.5, m.y, m.z * 0.5);
               if (s.swimBehavior == SwimBehavior.JUMP) {
                  s.swimBehavior = SwimBehavior.WANDER;
                  s.ticksSinceJump = 0;
                  s.setJumpingOutOfWater(false);
               }
            } else {
               s.setDeltaMovement(m.x + d0 / d3 * 0.5 * this.speedModifier, m.y + d1 / d3 * 0.5 * this.speedModifier, m.z + d2 / d3 * 0.5 * this.speedModifier);
               float f1 = (float)(-(Mth.atan2(d1, d3) * (180.0 / Math.PI)));
               s.setXRot(f1);
               if (!s.isArcing) {
                  Vec3 n = s.getDeltaMovement();
                  if (s.getTarget() == null) {
                     s.setYRot(-((float)Mth.atan2(n.x, n.z)) * (180.0F / (float)Math.PI));
                     s.yBodyRot = s.getYRot();
                  } else if (s.swimBehavior != SwimBehavior.JUMP) {
                     double d4 = s.getTarget().getX() - s.getX();
                     double d5 = s.getTarget().getZ() - s.getZ();
                     s.setYRot(-((float)Mth.atan2(d4, d5)) * (180.0F / (float)Math.PI));
                     s.yBodyRot = s.getYRot();
                  }
               }
            }
         }
      }
   }
}
