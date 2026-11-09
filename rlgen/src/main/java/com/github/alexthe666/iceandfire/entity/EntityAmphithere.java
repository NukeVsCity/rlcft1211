package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.AmphithereAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.AmphithereAIFleePlayer;
import com.github.alexthe666.iceandfire.entity.ai.AmphithereAIFollowOwner;
import com.github.alexthe666.iceandfire.entity.ai.AmphithereAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.EntityAIWatchClosestIgnoreRider;
import com.github.alexthe666.iceandfire.entity.ai.PathNavigateFlyingCreature;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIMate;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIOwnerHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIOwnerHurtTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAISit;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAISwimming;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIWander;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.LegacyMoveControl;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.LegacyTasksGoal;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityAmphithere. */
public class EntityAmphithere extends TamableAnimal implements IAnimatedEntity, IPhasesThroughBlock, IFlapable, IDragonFlute, EntityAISit.Sittable,
   IAirWaterRideable {
   private int animationTick;
   private Animation currentAnimation;
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(EntityAmphithere.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(EntityAmphithere.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> FLAP_TICKS = SynchedEntityData.defineId(EntityAmphithere.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Byte> CONTROL_STATE = SynchedEntityData.defineId(EntityAmphithere.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Integer> COMMAND = SynchedEntityData.defineId(EntityAmphithere.class, EntityDataSerializers.INT);
   public float flapProgress;
   private int flapTicks = 0;
   public float groundProgress = 0.0F;
   public float sitProgress = 0.0F;
   public float diveProgress = 0.0F;
   private int flightCooldown = 0;
   private int ticksFlying = 0;
   private boolean isFlying;
   private boolean isLandNavigator = true;
   /** client only: IFChainBuffer */
   public Object roll_buffer;
   public Object tail_buffer;
   public Object pitch_buffer;
   protected FlightBehavior flightBehavior = FlightBehavior.WANDER;
   private boolean changedFlightBehavior = false;
   @Nullable
   public BlockPos orbitPos = null;
   public float orbitRadius = 0.0F;
   private int ticksStill = 0;
   private int ridingTime = 0;
   public boolean isFallen;
   public static Animation ANIMATION_BITE = Animation.create(15);
   public static Animation ANIMATION_BITE_RIDER = Animation.create(15);
   public static Animation ANIMATION_WING_BLAST = Animation.create(30);
   public static Animation ANIMATION_TAIL_WHIP = Animation.create(30);
   public static Animation ANIMATION_SPEAK = Animation.create(10);
   private boolean isSitting;
   public BlockPos homePos;
   public boolean hasHomePosition = false;
   protected int ticksCircling = 0;
   public static final ResourceKey<LootTable> LOOT = AirWaterEntities.loot("amphithere");
   public final EntityAITasks tasks = new EntityAITasks();
   public final EntityAITasks targetTasks = new EntityAITasks();
   public EntityAISit aiSit;
   public float jumpMovementFactor = 0.02F;
   private final RiddenMotion riddenMotion = new RiddenMotion();

   public EntityAmphithere(EntityType<? extends EntityAmphithere> type, Level level) {
      super(type, level);
      this.initEntityAI();
      this.goalSelector.addGoal(0, new LegacyTasksGoal(this, this.targetTasks, this.tasks));
      if (level.isClientSide) {
         this.roll_buffer = AirWaterEntities.clientChainBuffer();
         this.pitch_buffer = AirWaterEntities.clientChainBuffer();
         this.tail_buffer = AirWaterEntities.clientChainBuffer();
      }
      this.switchNavigator(true);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return TamableAnimal.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.4)
         .add(Attributes.MAX_HEALTH, com.github.alexthe666.iceandfire.IceAndFire.CONFIG.amphithereMaxHealth)
         .add(Attributes.ATTACK_DAMAGE, com.github.alexthe666.iceandfire.IceAndFire.CONFIG.amphithereAttackStrength)
         .add(Attributes.FOLLOW_RANGE, 32.0)
         .add(Attributes.STEP_HEIGHT, 1.0);
   }

   @Override
   protected void registerGoals() {
   }

   @Override
   protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
   }

   @Override
   public float getWalkTargetValue(BlockPos pos, LevelReader level) {
      if (this.isFlying()) {
         return level.isEmptyBlock(pos) ? 10.0F : 0.0F;
      } else {
         return super.getWalkTargetValue(pos, level);
      }
   }

   private InteractionResult done() {
      return InteractionResult.sidedSuccess(this.level().isClientSide);
   }

   /** EntityAmphithere.processInteract (always "handled", as in 1.7.1) */
   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      boolean flag = itemstack.is(Items.NAME_TAG) || itemstack.is(Items.LEAD);
      if (flag) {
         itemstack.interactLivingEntity(player, this, hand);
         return this.done();
      } else if (itemstack.is(Items.COOKIE)) {
         if (this.getAge() == 0 && !this.isInLove()) {
            this.setSitting(false);
            this.setInLove(player);
            this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            if (!player.isCreative()) {
               itemstack.shrink(1);
            }
         }
         return this.done();
      } else if (itemstack.is(Items.COCOA_BEANS)) {
         this.heal(5.0F);
         this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
         if (!player.isCreative()) {
            itemstack.shrink(1);
         }
         return this.done();
      } else {
         if (!super.mobInteract(player, hand).consumesAction()) {
            if (AirWaterRefs.is(itemstack, "dragon_stick") && this.isOwnedBy(player)) {
               if (player.isShiftKeyDown()) {
                  this.homePos = AirWaterUtils.pos(this);
                  this.hasHomePosition = true;
                  player.displayClientMessage(Component.translatable("amphithere.command.new_home", this.homePos.getX(), this.homePos.getY(),
                     this.homePos.getZ()), true);
               }
               return this.done();
            }
            if (player.isShiftKeyDown() && this.isOwnedBy(player)) {
               if (player.getItemInHand(hand).isEmpty()) {
                  this.setCommand(this.getCommand() + 1);
                  if (this.getCommand() > 2) {
                     this.setCommand(0);
                  }
                  player.displayClientMessage(Component.translatable("amphithere.command." + this.getCommand()), true);
                  this.playSound(SoundEvents.ZOMBIE_INFECT, 1.0F, 1.0F);
               }
               return this.done();
            }
            if (!this.isTame() || this.isOwnedBy(player)) {
               if (!this.level().isClientSide) {
                  player.startRiding(this);
               }
               return this.done();
            }
         }
         return this.done();
      }
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return LOOT;
   }

   protected void initEntityAI() {
      this.tasks.addTask(0, this.aiSit = new EntityAISit(this));
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(1, new AmphithereAIAttackMelee(this, 1.0, true));
      this.tasks.addTask(2, new AmphithereAIFollowOwner(this, 1.0, 10.0F, 2.0F));
      this.tasks.addTask(3, new AmphithereAIFleePlayer(this, 32.0F, 0.8, 1.8));
      this.tasks.addTask(3, new AIFlyWander());
      this.tasks.addTask(3, new AIFlyCircle());
      this.tasks.addTask(3, new AILandWander(this, 1.0));
      this.tasks.addTask(4, new EntityAIWatchClosestIgnoreRider(this, LivingEntity.class, 6.0F));
      this.tasks.addTask(4, new EntityAIMate(this, 1.0));
      this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
      this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
      this.targetTasks.addTask(3, new AmphithereAIHurtByTarget(this, false));
   }

   public boolean isStill() {
      Vec3 m = this.riddenMotion.motion(this);
      return Math.abs(m.x) < 0.05 && Math.abs(m.z) < 0.05;
   }

   private void switchNavigator(boolean onLand) {
      if (onLand) {
         this.moveControl = new LegacyMoveControl(this);
         this.navigation = new WallClimberNavigation(this, this.level());
         this.isLandNavigator = true;
      } else {
         this.moveControl = new FlyMoveHelper(this);
         this.navigation = new PathNavigateFlyingCreature(this, this.level());
         this.isLandNavigator = false;
      }
   }

   public boolean onLeaves() {
      return this.level().getBlockState(AirWaterUtils.pos(this).below()).is(BlockTags.LEAVES);
   }

   @Override
   public boolean hurt(DamageSource source, float damage) {
      if (!this.isTame() && this.isFlying() && !this.onGround() && source.is(DamageTypeTags.IS_PROJECTILE) && !this.level().isClientSide) {
         this.isFallen = true;
      }
      return super.hurt(source, damage);
   }

   @Override
   protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
      if (this.hasPassenger(passenger) && this.isTame()) {
         this.setYRot(passenger.getYRot());
      }
      if (!this.level().isClientSide && !this.isTame() && passenger instanceof Player && this.getAnimation() == NO_ANIMATION && this.random.nextInt(15) == 0) {
         this.setAnimation(ANIMATION_BITE_RIDER);
      }
      if (!this.level().isClientSide && this.getAnimation() == ANIMATION_BITE_RIDER && this.getAnimationTick() == 6 && !this.isTame()) {
         passenger.hurt(this.damageSources().mobAttack(this), 1.0F);
      }
      float pitch_forward;
      if (this.getXRot() > 0.0F && this.isFlying()) {
         pitch_forward = this.getXRot() / 45.0F * 0.45F;
      } else {
         pitch_forward = 0.0F;
      }
      float scaled_ground = this.groundProgress * 0.1F;
      float radius = (this.isTame() ? 0.5F : 0.3F) - scaled_ground * 0.5F + pitch_forward;
      float angle = (float)(Math.PI / 180.0) * this.yBodyRot;
      double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
      double extraZ = (double)(radius * Mth.cos(angle));
      moveFunction.accept(passenger, this.getX() + extraX, this.getY() + 0.7F - (double)(scaled_ground * 0.14F) + (double)pitch_forward, this.getZ() + extraZ);
   }

   @Override
   public boolean isFood(ItemStack stack) {
      return stack.is(Items.COOKIE);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.isInLove()) {
         this.setFlying(false);
      }
      if (this.isSitting() && this.getTarget() != null) {
         this.setTarget(null);
      }
      Vec3 motion = this.riddenMotion.motion(this);
      boolean flapping = this.isFlapping();
      boolean flying = this.isFlying() && !this.onGround() || !this.onGround() && !this.onLeaves();
      boolean diving = flying && motion.y <= -0.1F || this.isFallen;
      boolean sitting = this.isSitting() && !this.isFlying();
      boolean notGrounded = flying || this.getAnimation() == ANIMATION_WING_BLAST;
      if (!this.level().isClientSide) {
         if (this.isSitting() && (this.getCommand() != 1 || this.getControllingPassenger() != null)) {
            this.setSitting(false);
         }
         if (!this.isSitting() && this.getCommand() == 1 && this.getControllingPassenger() == null) {
            this.setSitting(true);
         }
         if (this.isSitting()) {
            this.getNavigation().stop();
            ((LegacyMoveControl)this.moveControl).setWait();
         }
         if (flying) {
            this.ticksFlying++;
         } else {
            this.ticksFlying = 0;
         }
      }

      if (sitting && this.sitProgress < 20.0F) {
         this.sitProgress += 0.5F;
      } else if (!sitting && this.sitProgress > 0.0F) {
         this.sitProgress -= 0.5F;
      }

      if (this.flightCooldown > 0) {
         this.flightCooldown--;
      }

      if (!this.level().isClientSide) {
         if (this.flightBehavior == FlightBehavior.CIRCLE) {
            this.ticksCircling++;
         } else {
            this.ticksCircling = 0;
         }
      }

      if (this.getUntamedRider() != null && !this.isTame()) {
         this.ridingTime++;
      }
      if (this.getUntamedRider() == null) {
         this.ridingTime = 0;
      }
      if (!this.isTame() && this.ridingTime > com.github.alexthe666.iceandfire.IceAndFire.CONFIG.amphithereTameTime && this.getUntamedRider() instanceof Player rider) {
         this.level().broadcastEntityEvent(this, (byte)45);
         this.tame(rider);
      }

      if (this.level().isClientSide) {
         AirWaterEntities.updateClientControls(this);
      }

      if (this.isStill()) {
         this.ticksStill++;
      } else {
         this.ticksStill = 0;
      }

      if (!this.isFlying() && !this.isBaby() && this.onGround() && this.random.nextInt(200) == 0 && this.flightCooldown == 0 && this.getPassengers().isEmpty()
         && !this.isNoAi() && this.canMove()) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.5, 0.0));
         this.setFlying(true);
      }

      if (this.getControllingPassenger() != null && this.isFlying() && !this.onGround()) {
         LivingEntity rider = this.getControllingPassenger();
         this.setXRot(rider.getXRot() / 2.0F);
         Vec3 m = this.getDeltaMovement();
         double my = m.y;
         if (rider.getXRot() > 25.0F && my > -1.0) {
            if (my > 0.0) {
               my = 0.0;
            }
            my -= 0.1;
         }
         if (rider.getXRot() < -25.0F && my < 1.0) {
            if (my < 0.0) {
               my = 0.0;
            }
            my += 0.1;
         }
         this.setDeltaMovement(m.x, my, m.z);
      }

      if (notGrounded && this.groundProgress > 0.0F) {
         this.groundProgress -= 2.0F;
      } else if (!notGrounded && this.groundProgress < 20.0F) {
         this.groundProgress += 2.0F;
      }

      if (diving && this.diveProgress < 20.0F) {
         this.diveProgress++;
      } else if (!diving && this.diveProgress > 0.0F) {
         this.diveProgress--;
      }

      if (this.isFlying()) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.08, 0.0));
      }

      if (this.isFallen && this.flightBehavior != FlightBehavior.NONE) {
         this.flightBehavior = FlightBehavior.NONE;
      }

      if (this.flightBehavior == FlightBehavior.NONE && this.getControllingPassenger() == null) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.3F, 0.0));
      }

      if (this.isFlying() && !this.onGround() && this.isFallen && this.getControllingPassenger() == null) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.2F, 0.0));
         this.setXRot(Math.max(this.getXRot() + 5.0F, 75.0F));
      }

      if (this.isFallen && this.onGround()) {
         this.setFlying(false);
         if (this.isTame()) {
            this.flightCooldown = 50;
         } else {
            this.flightCooldown = 12000;
         }
         this.isFallen = false;
      }

      if (flying && this.isLandNavigator) {
         this.switchNavigator(false);
      }
      if (!flying && !this.isLandNavigator) {
         this.switchNavigator(true);
      }

      if ((this.hasHomePosition || this.getCommand() == 2) && this.flightBehavior == FlightBehavior.WANDER) {
         this.flightBehavior = FlightBehavior.CIRCLE;
      }

      if (flapping && this.flapProgress < 10.0F) {
         this.flapProgress++;
      } else if (!flapping && this.flapProgress > 0.0F) {
         this.flapProgress--;
      }

      if (this.flapTicks > 0) {
         this.flapTicks--;
      }

      this.yBodyRot = this.getYRot();
      if (this.level().isClientSide) {
         if (!this.onGround()) {
            AirWaterEntities.flapBuffer(this.roll_buffer, this.isVehicle() ? 55.0F : 90.0F, 3, 10.0F, 0.5F, this);
            AirWaterEntities.waveBuffer(this.pitch_buffer, 90.0F, 10, 10.0F, 0.5F, this);
         }
         AirWaterEntities.swingBuffer(this.tail_buffer, 70.0F, 20, 5.0F, this);
      }

      if (this.changedFlightBehavior) {
         this.changedFlightBehavior = false;
      }

      if (!flapping && (motion.y > 0.15F || motion.y > 0.0 && this.tickCount % 200 == 0) && !this.onGround()) {
         this.flapWings();
      }

      AnimationHandler.INSTANCE.updateAnimations(this);
   }

   @Override
   public boolean isFlapping() {
      return this.flapTicks > 0;
   }

   public void setCommand(int command) {
      this.entityData.set(COMMAND, command);
      this.setSitting(command == 1);
   }

   public int getCommand() {
      return this.entityData.get(COMMAND);
   }

   @Override
   public void flapWings() {
      this.flapTicks = 20;
   }

   public boolean isSitting() {
      if (this.level().isClientSide) {
         boolean sitting = this.isInSittingPose();
         this.isSitting = sitting;
         return sitting;
      } else {
         return this.isSitting;
      }
   }

   @Override
   public void setSitting(boolean sitting) {
      if (!this.level().isClientSide) {
         this.isSitting = sitting;
      }
      this.setInSittingPose(sitting);
   }

   @Nullable
   @Override
   public LivingEntity getControllingPassenger() {
      for (Entity passenger : this.getPassengers()) {
         if (passenger instanceof Player player && this.getTarget() != passenger) {
            if (this.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(player.getUUID())) {
               return player;
            }
         }
      }
      return null;
   }

   @Nullable
   public Entity getUntamedRider() {
      for (Entity passenger : this.getPassengers()) {
         if (passenger instanceof Player) {
            return passenger;
         }
      }
      return null;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VARIANT, 0);
      builder.define(FLYING, false);
      builder.define(FLAP_TICKS, 0);
      builder.define(CONTROL_STATE, (byte)0);
      builder.define(COMMAND, 0);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putBoolean("Sitting", this.isSitting());
      compound.putInt("Variant", this.getVariant());
      compound.putBoolean("Flying", this.isFlying());
      compound.putInt("FlightCooldown", this.flightCooldown);
      compound.putInt("RidingTime", this.ridingTime);
      compound.putBoolean("HasHomePosition", this.hasHomePosition);
      if (this.homePos != null && this.hasHomePosition) {
         compound.putInt("HomeAreaX", this.homePos.getX());
         compound.putInt("HomeAreaY", this.homePos.getY());
         compound.putInt("HomeAreaZ", this.homePos.getZ());
      }
      compound.putInt("Command", this.getCommand());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (this.aiSit != null) {
         this.aiSit.setSitting(compound.getBoolean("Sitting"));
      }
      this.setSitting(compound.getBoolean("Sitting"));
      this.setVariant(compound.getInt("Variant"));
      this.setFlying(compound.getBoolean("Flying"));
      this.flightCooldown = compound.getInt("FlightCooldown");
      this.ridingTime = compound.getInt("RidingTime");
      this.hasHomePosition = compound.getBoolean("HasHomePosition");
      if (this.hasHomePosition && compound.getInt("HomeAreaX") != 0 && compound.getInt("HomeAreaY") != 0 && compound.getInt("HomeAreaZ") != 0) {
         this.homePos = new BlockPos(compound.getInt("HomeAreaX"), compound.getInt("HomeAreaY"), compound.getInt("HomeAreaZ"));
      }
      this.setCommand(compound.getInt("Command"));
   }

   public boolean isRidingPlayer(Player player) {
      return this.getControllingPassenger() != null && this.getControllingPassenger() instanceof Player
         && this.getControllingPassenger().getUUID().equals(player.getUUID()) && this.isTame();
   }

   @Override
   public void tick() {
      this.riddenMotion.update(this);
      super.tick();
      LivingEntity target = this.getTarget();
      int damage = (int)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
      if (this.getAnimation() == ANIMATION_BITE && target != null && this.getAnimationTick() == 7) {
         if (this.distanceToSqr(target) < 10.0) {
            target.hurt(this.damageSources().mobAttack(this), (float)damage);
         }
      }

      if (this.getAnimation() == ANIMATION_WING_BLAST && this.getAnimationTick() == 5) {
         this.playSound(AirWaterRefs.sound("amphithere_gust"), 1.0F, 1.0F);
      }

      if ((this.getAnimation() == ANIMATION_BITE || this.getAnimation() == ANIMATION_BITE_RIDER) && this.getAnimationTick() == 1) {
         this.playSound(AirWaterRefs.sound("amphithere_bite"), 1.0F, 1.0F);
      }

      if (this.getAnimation() == ANIMATION_WING_BLAST && target != null && this.getAnimationTick() > 5 && this.getAnimationTick() < 22) {
         if (this.distanceToSqr(target) < 25.0) {
            target.hurt(this.damageSources().mobAttack(this), (float)(damage / 2));
            target.hasImpulse = true;
            Vec3 m = this.getDeltaMovement();
            float f = (float)Math.sqrt(m.x * m.x * 0.2F + m.y * m.y + m.z * m.z * 0.2F);
            // 1.7.1 divides by the amphithere's own speed: a still amphithere gives an infinite push; capped so 1.21 does not break
            this.knock(target, Math.min(0.5 / (double)f, 10.0));
         }
      }

      if (this.getAnimation() == ANIMATION_TAIL_WHIP && target != null && this.getAnimationTick() == 7) {
         if (this.distanceToSqr(target) < 10.0) {
            target.hurt(this.damageSources().mobAttack(this), (float)damage);
            target.hasImpulse = true;
            float f = Mth.sqrt(0.5F);
            this.knock(target, 0.5 / (double)f * 4.0);
         }
      }

      if (this.level().isClientSide) {
         AirWaterEntities.updateClientControls(this);
      }

      if (this.up() && !this.level().isClientSide && !this.isFlying()) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, 1.0, 0.0));
         this.setFlying(true);
      }

      if (this.onGround() && this.getControllingPassenger() != null) {
         this.setFlying(false);
      }

      if (this.dismount() && this.isFlying() && this.onGround()) {
         this.setFlying(false);
      }

      if (this.getUntamedRider() != null && this.getUntamedRider().isShiftKeyDown()) {
         this.getUntamedRider().stopRiding();
      }

      if (this.attack() && this.getControllingPassenger() instanceof Player rider) {
         LivingEntity looked = AirWaterUtils.riderLookingAtEntity(this, rider, 2.5);
         if (this.getAnimation() != ANIMATION_BITE) {
            this.setAnimation(ANIMATION_BITE);
         }
         if (looked != null) {
            looked.hurt(this.damageSources().mobAttack(this), (float)damage);
         }
      }

      if (this.getTarget() != null && this.isOwnedBy(this.getTarget())) {
         this.setTarget(null);
      }

      if (this.getTarget() != null && this.onGround() && this.isFlying() && this.ticksFlying > 40) {
         this.setFlying(false);
      }
   }

   /** the wing blast / tail whip knockback (motion /= 2, -= k, upward 0.4 cap on ground) */
   private void knock(LivingEntity target, double k) {
      Vec3 t = target.getDeltaMovement();
      double mx = t.x / 2.0 - k;
      double mz = t.z / 2.0 - k;
      double my = t.y;
      if (target.onGround()) {
         my /= 2.0;
         my += 4.0;
         if (my > 0.4F) {
            my = 0.4F;
         }
      }
      target.setDeltaMovement(mx, my, mz);
   }

   @Override
   public boolean doHurtTarget(Entity entityIn) {
      if (this.getAnimation() != ANIMATION_BITE && this.getAnimation() != ANIMATION_TAIL_WHIP && this.getAnimation() != ANIMATION_WING_BLAST
         && this.getControllingPassenger() == null) {
         if (this.random.nextBoolean()) {
            this.setAnimation(ANIMATION_BITE);
         } else {
            this.setAnimation(!this.getRandom().nextBoolean() && !this.isFlying() ? ANIMATION_TAIL_WHIP : ANIMATION_WING_BLAST);
         }
         return true;
      } else {
         return false;
      }
   }

   public boolean isFlying() {
      return this.level().isClientSide ? (this.isFlying = this.entityData.get(FLYING)) : this.isFlying;
   }

   public void setFlying(boolean flying) {
      this.entityData.set(FLYING, flying);
      if (!this.level().isClientSide) {
         this.isFlying = flying;
      }
   }

   public int getVariant() {
      return this.entityData.get(VARIANT);
   }

   public void setVariant(int variant) {
      this.entityData.set(VARIANT, variant);
   }

   public boolean up() {
      return (this.entityData.get(CONTROL_STATE) & 1) == 1;
   }

   public boolean down() {
      return (this.entityData.get(CONTROL_STATE) >> 1 & 1) == 1;
   }

   public boolean attack() {
      return (this.entityData.get(CONTROL_STATE) >> 2 & 1) == 1;
   }

   public boolean dismount() {
      return (this.entityData.get(CONTROL_STATE) >> 3 & 1) == 1;
   }

   public void up(boolean up) {
      this.setStateField(0, up);
   }

   public void down(boolean down) {
      this.setStateField(1, down);
   }

   public void attack(boolean attack) {
      this.setStateField(2, attack);
   }

   public void dismount(boolean dismount) {
      this.setStateField(3, dismount);
   }

   private void setStateField(int i, boolean newState) {
      byte prevState = this.entityData.get(CONTROL_STATE);
      if (newState) {
         this.entityData.set(CONTROL_STATE, (byte)(prevState | 1 << i));
      } else {
         this.entityData.set(CONTROL_STATE, (byte)(prevState & ~(1 << i)));
      }
   }

   @Override
   public byte getControlState() {
      return this.entityData.get(CONTROL_STATE);
   }

   @Override
   public void setControlState(byte state) {
      this.entityData.set(CONTROL_STATE, state);
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return AirWaterRefs.sound("amphithere_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return AirWaterRefs.sound("amphithere_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return AirWaterRefs.sound("amphithere_die");
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
      return new Animation[]{ANIMATION_BITE, ANIMATION_BITE_RIDER, ANIMATION_WING_BLAST, ANIMATION_TAIL_WHIP, ANIMATION_SPEAK};
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

   public boolean isBlinking() {
      return this.tickCount % 50 > 40;
   }

   @Nullable
   @Override
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
      EntityAmphithere amphithere = AirWaterEntities.AMPHITHERE.get().create(level);
      if (amphithere != null) {
         amphithere.setVariant(this.getRandom().nextInt(5));
      }
      return amphithere;
   }

   @Override
   protected int getBaseExperienceReward() {
      return 15 + this.level().random.nextInt(10);
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
      data = super.finalizeSpawn(level, difficulty, spawnType, data);
      this.setVariant(this.getRandom().nextInt(5));
      return data;
   }

   @Override
   public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
      return false;
   }

   public static BlockPos getPositionRelativetoGround(Entity entity, Level world, double x, double z, RandomSource rand) {
      BlockPos pos = BlockPos.containing(x, entity.getY(), z);
      for (int yDown = 0; yDown < 6 + rand.nextInt(6); yDown++) {
         if (!world.isEmptyBlock(pos.below(yDown))) {
            return pos.above(yDown);
         }
      }
      return pos;
   }

   public static BlockPos getPositionInOrbit(EntityAmphithere entity, Level world, BlockPos orbit, RandomSource rand) {
      float possibleOrbitRadius = entity.orbitRadius + 10.0F;
      float radius = 10.0F;
      if (entity.getCommand() == 2) {
         if (entity.getOwner() != null) {
            orbit = AirWaterUtils.pos(entity.getOwner()).above(7);
            radius = 5.0F;
         }
      } else if (entity.hasHomePosition) {
         orbit = entity.homePos.above(30);
         radius = 30.0F;
      }
      float angle = (float)(Math.PI / 180.0) * possibleOrbitRadius;
      double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
      double extraZ = (double)(radius * Mth.cos(angle));
      BlockPos radialPos = BlockPos.containing((double)orbit.getX() + extraX, (double)orbit.getY(), (double)orbit.getZ() + extraZ);
      entity.orbitRadius = possibleOrbitRadius;
      return radialPos;
   }

   @Override
   public boolean canPhaseThroughBlock(Level world, BlockPos pos) {
      return world.getBlockState(pos).is(BlockTags.LEAVES);
   }

   @Override
   public float getFlyingSpeed() {
      return this.jumpMovementFactor;
   }

   /** EntityAmphithere.travel: ridden in the air it always flies forward at full speed */
   @Override
   public void travel(Vec3 move) {
      if (!this.canMove() && !this.isVehicle()) {
         super.travel(new Vec3(0.0, move.y, 0.0));
      } else {
         if (this.isVehicle()) {
            LivingEntity controller = this.getControllingPassenger();
            if (controller != null) {
               float strafe = controller.xxa * 0.5F;
               float forward = controller.zza;
               if (!this.onGround()) {
                  strafe = 0.0F;
                  forward = 1.5F;
               }
               if (forward <= 0.0F) {
                  forward *= 0.25F;
               }
               if (this.isFlying()) {
                  Vec3 m = this.getDeltaMovement();
                  this.setDeltaMovement(m.x * 1.06, m.y, m.z * 1.06);
               }
               this.jumpMovementFactor = 0.05F;
               this.setSpeed(this.onGround() ? (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) : 2.0F);
               super.travel(new Vec3(strafe, 0.0, forward));
               return;
            }
         }
         super.travel(move);
      }
   }

   public boolean canMove() {
      return this.getControllingPassenger() == null && this.sitProgress == 0.0F && !this.isSitting();
   }

   @Override
   public void handleEntityEvent(byte id) {
      if (id == 45) {
         this.playEffect();
      } else {
         super.handleEntityEvent(id);
      }
   }

   protected void playEffect() {
      for (int i = 0; i < 7; i++) {
         double d0 = this.random.nextGaussian() * 0.02;
         double d1 = this.random.nextGaussian() * 0.02;
         double d2 = this.random.nextGaussian() * 0.02;
         this.level().addParticle(ParticleTypes.HEART, this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
            this.getY() + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()),
            this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), d0, d1, d2);
      }
   }

   @Override
   public void onHearFlute(Player player) {
      if (!this.onGround() && this.isTame()) {
         this.isFallen = true;
      }
   }

   class AIFlyCircle extends EntityAIBase {
      BlockPos target;

      AIFlyCircle() {
         this.setMutexBits(1);
      }

      @Override
      public boolean shouldExecute() {
         EntityAmphithere a = EntityAmphithere.this;
         if (a.flightBehavior != FlightBehavior.CIRCLE || !a.canMove()) {
            return false;
         } else if (a.isFlying()) {
            a.orbitPos = getPositionRelativetoGround(a, a.level(), a.getX() + (double)a.random.nextInt(30) - 15.0,
               a.getZ() + (double)a.random.nextInt(30) - 15.0, a.random);
            this.target = getPositionInOrbit(a, a.level(), a.orbitPos, a.random);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean shouldContinueExecuting() {
         return EntityAmphithere.this.getTarget() == null && EntityAmphithere.this.flightBehavior == FlightBehavior.CIRCLE;
      }

      @Override
      public void updateTask() {
         EntityAmphithere a = EntityAmphithere.this;
         if (Math.sqrt(a.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ())) < 5.0) {
            this.target = getPositionInOrbit(a, a.level(), a.orbitPos, a.random);
         }
         if (a.level().isEmptyBlock(this.target)) {
            a.moveControl.setWantedPosition(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 0.25);
            if (a.getTarget() == null) {
               a.getLookControl().setLookAt(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 180.0F, 20.0F);
            }
         }
      }
   }

   class AIFlyWander extends EntityAIBase {
      BlockPos target;

      AIFlyWander() {
         this.setMutexBits(1);
      }

      @Override
      public boolean shouldExecute() {
         EntityAmphithere a = EntityAmphithere.this;
         if (a.flightBehavior != FlightBehavior.WANDER || !a.canMove()) {
            return false;
         } else if (!a.isFlying()) {
            return false;
         } else {
            this.target = getPositionRelativetoGround(a, a.level(), a.getX() + (double)a.random.nextInt(30) - 15.0,
               a.getZ() + (double)a.random.nextInt(30) - 15.0, a.random);
            a.orbitPos = null;
            return !a.getMoveControl().hasWanted() || a.ticksStill >= 50;
         }
      }

      @Override
      public boolean shouldContinueExecuting() {
         return false;
      }

      @Override
      public void updateTask() {
         EntityAmphithere a = EntityAmphithere.this;
         this.target = getPositionRelativetoGround(a, a.level(), a.getX() + (double)a.random.nextInt(30) - 15.0,
            a.getZ() + (double)a.random.nextInt(30) - 15.0, a.random);
         if (a.level().isEmptyBlock(this.target)) {
            a.moveControl.setWantedPosition(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 0.25);
            if (a.getTarget() == null) {
               a.getLookControl().setLookAt(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 180.0F, 20.0F);
            }
         }
      }
   }

   class AILandWander extends EntityAIWander {
      AILandWander(PathfinderMob creature, double speed) {
         super(creature, speed, 10);
      }

      @Override
      public boolean shouldExecute() {
         return this.entity.onGround() && super.shouldExecute() && ((EntityAmphithere)this.entity).canMove();
      }
   }

   public enum FlightBehavior {
      CIRCLE,
      WANDER,
      NONE
   }

   class FlyMoveHelper extends LegacyMoveControl {
      FlyMoveHelper(EntityAmphithere entity) {
         super(entity);
         this.speedModifier = 1.75;
      }

      @Override
      public void tick() {
         EntityAmphithere a = EntityAmphithere.this;
         if (a.canMove() && this.isMoveTo()) {
            double d0 = this.wantedX - a.getX();
            double d1 = this.wantedY - a.getY();
            double d2 = this.wantedZ - a.getZ();
            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            if (d3 < 6.0 && a.getTarget() == null) {
               if (!a.changedFlightBehavior && a.flightBehavior == FlightBehavior.WANDER && a.random.nextInt(30) == 0) {
                  a.flightBehavior = FlightBehavior.CIRCLE;
                  a.changedFlightBehavior = true;
               }
               if (!a.changedFlightBehavior && a.flightBehavior == FlightBehavior.CIRCLE && a.random.nextInt(5) == 0 && a.ticksCircling > 150) {
                  a.flightBehavior = FlightBehavior.WANDER;
                  a.changedFlightBehavior = true;
               }
               if (a.hasHomePosition && a.flightBehavior != FlightBehavior.NONE || a.getCommand() == 2) {
                  a.flightBehavior = FlightBehavior.CIRCLE;
               }
            }
            Vec3 m = a.getDeltaMovement();
            if (d3 < 1.0 && a.getTarget() == null) {
               a.setDeltaMovement(m.scale(0.5));
            } else {
               a.setDeltaMovement(m.x + d0 / d3 * 0.5 * this.speedModifier, m.y + d1 / d3 * 0.5 * this.speedModifier, m.z + d2 / d3 * 0.5 * this.speedModifier);
               float f1 = (float)(-(Mth.atan2(d1, d3) * (180.0 / Math.PI)));
               a.setXRot(f1);
               Vec3 n = a.getDeltaMovement();
               if (a.getTarget() == null) {
                  a.setYRot(-((float)Mth.atan2(n.x, n.z)) * (180.0F / (float)Math.PI));
               } else {
                  double d4 = a.getTarget().getX() - a.getX();
                  double d5 = a.getTarget().getZ() - a.getZ();
                  a.setYRot(-((float)Mth.atan2(d4, d5)) * (180.0F / (float)Math.PI));
               }
               a.yBodyRot = a.getYRot();
            }
         }
      }
   }
}
