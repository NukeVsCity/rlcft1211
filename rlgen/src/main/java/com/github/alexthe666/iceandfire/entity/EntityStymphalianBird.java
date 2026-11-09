package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.StymphalianBirdAIAirTarget;
import com.github.alexthe666.iceandfire.entity.ai.StymphalianBirdAIFlee;
import com.github.alexthe666.iceandfire.entity.ai.StymphalianBirdAITarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAILookIdle;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAISwimming;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWander;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWatchClosest;
import com.github.alexthe666.iceandfire.entity.ai.legacy.LegacyMoveControl;
import com.github.alexthe666.iceandfire.entity.ai.legacy.LegacyTasksGoal;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityStymphalianBird. */
public class EntityStymphalianBird extends PathfinderMob implements IAnimatedEntity, Enemy, IVillagerFear, IAnimalFear {
   private static final int FLIGHT_CHANCE_PER_TICK = 100;
   private int animationTick;
   private Animation currentAnimation;
   private static final EntityDataAccessor<Optional<UUID>> VICTOR_ENTITY = SynchedEntityData.defineId(EntityStymphalianBird.class,
      EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(EntityStymphalianBird.class, EntityDataSerializers.BOOLEAN);
   private boolean isFlying;
   public float flyProgress;
   public BlockPos airTarget;
   private int flyTicks;
   private int launchTicks;
   public static Animation ANIMATION_PECK = Animation.create(20);
   public static Animation ANIMATION_SHOOT_ARROWS = Animation.create(30);
   public static Animation ANIMATION_SPEAK = Animation.create(10);
   public StymphalianBirdFlock flock;
   private boolean aiFlightLaunch = false;
   public static final ResourceKey<LootTable> LOOT = AirWaterEntities.loot("stymphalian_bird");
   private int airBorneCounter;
   public final EntityAITasks tasks = new EntityAITasks();
   public final EntityAITasks targetTasks = new EntityAITasks();

   public EntityStymphalianBird(EntityType<? extends EntityStymphalianBird> type, Level level) {
      super(type, level);
      this.moveControl = new LegacyMoveControl(this);
      this.initEntityAI();
      this.goalSelector.addGoal(0, new LegacyTasksGoal(this, this.targetTasks, this.tasks));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.MAX_HEALTH, 24.0)
         .add(Attributes.ATTACK_DAMAGE, 4.0)
         .add(Attributes.FOLLOW_RANGE, (double)Math.min(2048, com.github.alexthe666.iceandfire.IceAndFire.CONFIG.stymphalianBirdTargetSearchLength))
         .add(Attributes.ARMOR, 4.0);
   }

   @Override
   protected void registerGoals() {
   }

   @Override
   protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return LOOT;
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, new StymphalianBirdAIFlee(this, 10.0F));
      this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.5, false));
      this.tasks.addTask(5, new EntityAIWander(this, 1.0));
      this.tasks.addTask(6, new StymphalianBirdAIAirTarget(this));
      this.tasks.addTask(7, new EntityAIWatchClosest(this, LivingEntity.class, 6.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
      this.targetTasks.addTask(2, new StymphalianBirdAITarget<>(this, LivingEntity.class, true));
   }

   public LegacyMoveControl getLegacyMoveControl() {
      return (LegacyMoveControl)this.moveControl;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VICTOR_ENTITY, Optional.empty());
      builder.define(FLYING, false);
   }

   @Override
   public boolean removeWhenFarAway(double distance) {
      return false;
   }

   @Override
   protected int getBaseExperienceReward() {
      return 10 + this.level().random.nextInt(5);
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.level().isClientSide && this.level().getDifficulty() == Difficulty.PEACEFUL) {
         this.discard();
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putString("VictorUUID", this.getVictorId() == null ? "" : this.getVictorId().toString());
      tag.putBoolean("Flying", this.isFlying());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      String s = tag.getString("VictorUUID");
      if (!s.isEmpty()) {
         try {
            this.setVictorId(UUID.fromString(s));
         } catch (Throwable ignored) {
         }
      }
      this.setFlying(tag.getBoolean("Flying"));
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

   @Override
   public void die(DamageSource cause) {
      if (cause.getEntity() instanceof LivingEntity killer && !this.level().isClientSide) {
         this.setVictorId(killer.getUUID());
         if (this.flock != null) {
            this.flock.setFearTarget(killer);
         }
      }
      super.die(cause);
   }

   private static final TagKey<Item> BRONZE = TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.parse("c:ingots/bronze"));
   private static final TagKey<Item> COPPER = TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.parse("c:ingots/copper"));

   /** OreDictionary.getOres("ingotBronze") / ("ingotCopper"): modded ingots only, 1.12 had no vanilla copper. */
   private static ItemStack firstOre(TagKey<Item> tag) {
      for (Holder<Item> h : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
         if (h.value() != Items.COPPER_INGOT) {
            return new ItemStack(h.value());
         }
      }
      return ItemStack.EMPTY;
   }

   @Override
   protected void tickDeath() {
      super.tickDeath();
      if (this.deathTime == 20 && !this.level().isClientSide && com.github.alexthe666.iceandfire.IceAndFire.CONFIG.stymphalianBirdsOreDictDrops) {
         ItemStack bronze = firstOre(BRONZE);
         if (!bronze.isEmpty()) {
            bronze.setCount(1 + this.getRandom().nextInt(3));
            this.dropItemAt(bronze, this.getX(), this.getY() + 0.5, this.getZ());
         }
         ItemStack copper = firstOre(COPPER);
         if (!copper.isEmpty()) {
            copper.setCount(1 + this.getRandom().nextInt(3));
            this.dropItemAt(copper, this.getX(), this.getY() + 0.5, this.getZ());
         }
      }
   }

   private void dropItemAt(ItemStack stack, double x, double y, double z) {
      ItemEntity item = new ItemEntity(this.level(), x, y, z, stack);
      item.setDefaultPickUpDelay();
      this.level().addFreshEntity(item);
   }

   @Nullable
   public UUID getVictorId() {
      return this.entityData.get(VICTOR_ENTITY).orElse(null);
   }

   public void setVictorId(@Nullable UUID uuid) {
      this.entityData.set(VICTOR_ENTITY, Optional.ofNullable(uuid));
   }

   /** 1.12 World.getPlayerEntityByUUID: the victor is only ever a player. */
   @Nullable
   public LivingEntity getVictor() {
      UUID uuid = this.getVictorId();
      return uuid == null ? null : this.level().getPlayerByUUID(uuid);
   }

   public boolean isVictor(LivingEntity entityIn) {
      return entityIn == this.getVictor();
   }

   public void setVictor(LivingEntity player) {
      this.setVictorId(player.getUUID());
   }

   public boolean isTargetBlocked(Vec3 target) {
      if (target != null) {
         BlockHitResult rayTrace = AirWaterUtils.rayTraceBlocks(this, Vec3.atLowerCornerOf(this.blockPosition()), target, false);
         if (rayTrace != null) {
            BlockPos pos = BlockPos.containing(rayTrace.getLocation());
            if (!this.level().isEmptyBlock(pos)) {
               return true;
            }
            return false;
         }
      }
      return false;
   }

   @Override
   public boolean doHurtTarget(Entity entityIn) {
      if (this.getAnimation() == NO_ANIMATION) {
         this.setAnimation(ANIMATION_PECK);
      }
      return true;
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.getTarget() != null && (this.getTarget() instanceof Player p && p.isCreative() || this.getVictor() != null && this.isVictor(this.getTarget()))) {
         this.setTarget(null);
      }

      if (this.flock == null) {
         StymphalianBirdFlock otherFlock = StymphalianBirdFlock.getNearbyFlock(this);
         if (otherFlock == null) {
            this.flock = StymphalianBirdFlock.createFlock(this);
         } else {
            this.flock = otherFlock;
            this.flock.addToFlock(this);
         }
      } else {
         if (!this.flock.isLeader(this)) {
            double dist = this.distanceToSqr(this.flock.getLeader());
            if (dist > 360.0) {
               this.setFlying(true);
               this.navigation.stop();
               this.airTarget = StymphalianBirdAIAirTarget.getNearbyAirTarget(this.flock.getLeader());
               this.aiFlightLaunch = false;
            } else if (!this.flock.getLeader().isFlying()) {
               this.setFlying(false);
               this.airTarget = null;
               this.aiFlightLaunch = false;
            }
            if (this.onGround() && dist < 40.0 && this.getAnimation() != ANIMATION_SHOOT_ARROWS) {
               this.setFlying(false);
            }
         }
         this.flock.update();
      }

      if (!this.level().isClientSide && this.getTarget() != null && this.getTarget().isAlive()) {
         double distx = this.distanceToSqr(this.getTarget());
         if (this.getAnimation() == ANIMATION_PECK && this.getAnimationTick() == 7) {
            if (distx < 1.5) {
               this.getTarget().hurt(this.damageSources().mobAttack(this), (float)(int)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
            }
            if (this.onGround()) {
               this.setFlying(false);
            }
         }

         if (this.getAnimation() != ANIMATION_PECK && this.getAnimation() != ANIMATION_SHOOT_ARROWS && distx > 3.0 && distx < 225.0) {
            this.setAnimation(ANIMATION_SHOOT_ARROWS);
         }

         if (this.getAnimation() == ANIMATION_SHOOT_ARROWS) {
            LivingEntity target = this.getTarget();
            this.lookAt(target, 360.0F, 360.0F);
            if (this.isFlying()) {
               this.setYRot(this.yBodyRot);
               if ((this.getAnimationTick() == 7 || this.getAnimationTick() == 14)
                  && this.isDirectPathBetweenPoints(this, this.position(), target.position())) {
                  this.playSound(AirWaterRefs.sound("stymphalian_bird_attack"), 1.0F, 1.0F);
                  for (int i = 0; i < 4; i++) {
                     float wingX = (float)(this.getX() + 0.9F * Math.cos((double)(this.getYRot() + (float)(180 * (i % 2))) * Math.PI / 180.0));
                     float wingZ = (float)(this.getZ() + 0.9F * Math.sin((double)(this.getYRot() + (float)(180 * (i % 2))) * Math.PI / 180.0));
                     float wingY = (float)(this.getY() + 1.0);
                     double d0 = target.getX() - (double)wingX;
                     double d1 = target.getBoundingBox().minY - (double)wingY;
                     double d2 = target.getZ() - (double)wingZ;
                     double d3 = Math.sqrt(d0 * d0 + d2 * d2);
                     EntityStymphalianFeather feather = new EntityStymphalianFeather(this.level(), this);
                     feather.setPos(wingX, wingY, wingZ);
                     feather.shoot(d0, d1 + d3 * 0.10000000298023223, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
                     this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                     this.level().addFreshEntity(feather);
                  }
               }
            } else {
               this.setFlying(true);
            }
         }
      }

      boolean stone = AirWaterUtils.isStone(this);
      boolean flying = this.isFlying() && !this.onGround() || this.airBorneCounter > 10 || this.getAnimation() == ANIMATION_SHOOT_ARROWS;
      if (flying && this.flyProgress < 20.0F) {
         this.flyProgress++;
      } else if (!flying && this.flyProgress > 0.0F) {
         this.flyProgress--;
      }

      if (!this.isFlying() && this.airTarget != null && this.onGround() && !this.level().isClientSide) {
         this.airTarget = null;
      }

      if (this.isFlying() && this.getTarget() == null) {
         this.flyAround();
      } else if (this.getTarget() != null) {
         this.flyTowardsTarget();
      }

      if (!this.level().isClientSide && this.doesWantToLand() && !this.aiFlightLaunch && this.getAnimation() != ANIMATION_SHOOT_ARROWS) {
         this.setFlying(false);
         this.airTarget = null;
      }

      // 1.12 isOffsetPositionInLiquid(0, 0, 0) is true when the box touches no block and no liquid (1.21 isFree)
      if (!this.level().isClientSide && this.isFree(0.0, 0.0, 0.0) && !this.isFlying()) {
         this.setFlying(true);
         this.launchTicks = 0;
         this.flyTicks = 0;
         this.aiFlightLaunch = true;
      }

      if (!this.level().isClientSide && this.onGround() && this.isFlying() && !this.aiFlightLaunch && this.getAnimation() != ANIMATION_SHOOT_ARROWS) {
         this.setFlying(false);
         this.airTarget = null;
      }

      if (!stone && !this.level().isClientSide && (this.flock == null || this.flock.isLeader(this)) && this.getRandom().nextInt(FLIGHT_CHANCE_PER_TICK) == 0
         && !this.isFlying() && this.getPassengers().isEmpty() && !this.isBaby() && this.onGround()) {
         this.setFlying(true);
         this.launchTicks = 0;
         this.flyTicks = 0;
         this.aiFlightLaunch = true;
      }

      if (!this.level().isClientSide) {
         if (this.aiFlightLaunch && this.launchTicks < 40) {
            this.launchTicks++;
         } else {
            this.launchTicks = 0;
            this.aiFlightLaunch = false;
         }
         if (this.isFlying()) {
            this.flyTicks++;
         } else {
            this.flyTicks = 0;
         }
      }

      if (!this.onGround()) {
         this.airBorneCounter++;
      } else {
         this.airBorneCounter = 0;
      }

      if (this.getAnimation() == ANIMATION_SHOOT_ARROWS && !this.isFlying() && !this.level().isClientSide) {
         this.setFlying(true);
         this.aiFlightLaunch = true;
      }

      AnimationHandler.INSTANCE.updateAnimations(this);
      if (this.getY() > (double)com.github.alexthe666.iceandfire.IceAndFire.CONFIG.stymphalianBirdFlightHeight) {
         this.setPos(this.getX(), (double)com.github.alexthe666.iceandfire.IceAndFire.CONFIG.stymphalianBirdFlightHeight, this.getZ());
      }
   }

   public boolean isDirectPathBetweenPoints(Entity entity, Vec3 vec1, Vec3 vec2) {
      return AirWaterUtils.rayTraceBlocks(entity, vec1, new Vec3(vec2.x, vec2.y + (double)entity.getBbHeight() * 0.5, vec2.z), false) == null;
   }

   public void flyAround() {
      if (this.airTarget != null && this.isFlying()) {
         if (!this.isTargetInAir() || this.flyTicks > 6000 || !this.isFlying()) {
            this.airTarget = null;
         }
         this.flyTowardsTarget();
      }
   }

   public void flyTowardsTarget() {
      if (this.airTarget != null && this.isTargetInAir() && this.isFlying()
         && this.getDistanceSquared(new Vec3((double)this.airTarget.getX(), this.getY(), (double)this.airTarget.getZ())) > 3.0F) {
         double targetX = (double)this.airTarget.getX() + 0.5 - this.getX();
         double targetY = (double)Math.min(this.airTarget.getY(), 256) + 1.0 - this.getY();
         double targetZ = (double)this.airTarget.getZ() + 0.5 - this.getZ();
         Vec3 m = this.getDeltaMovement();
         double mx = m.x + (Math.signum(targetX) * 0.5 - m.x) * 0.100000000372529 * (double)this.getFlySpeed(false);
         double my = m.y + (Math.signum(targetY) * 0.5 - m.y) * 0.100000000372529 * (double)this.getFlySpeed(true);
         double mz = m.z + (Math.signum(targetZ) * 0.5 - m.z) * 0.100000000372529 * (double)this.getFlySpeed(false);
         this.setDeltaMovement(mx, my, mz);
         float angle = (float)(Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
         float rotation = Mth.wrapDegrees(angle - this.getYRot());
         this.zza = 0.5F;
         this.yRotO = this.getYRot();
         this.setYRot(this.getYRot() + rotation);
         if (!this.isFlying()) {
            this.setFlying(true);
         }
      } else {
         this.airTarget = null;
      }

      if (this.airTarget != null && this.isTargetInAir() && this.isFlying()
         && this.getDistanceSquared(new Vec3((double)this.airTarget.getX(), this.getY(), (double)this.airTarget.getZ())) < 3.0F && this.doesWantToLand()) {
         this.setFlying(false);
      }
   }

   private float getFlySpeed(boolean y) {
      float speed = 2.0F;
      if (this.flock != null && !this.flock.isLeader(this) && this.distanceToSqr(this.flock.getLeader()) > 10.0) {
         speed = 4.0F;
      }
      if (this.getAnimation() == ANIMATION_SHOOT_ARROWS && !y) {
         speed = (float)((double)speed * 0.05);
      }
      return speed;
   }

   @Override
   public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
      return false;
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

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return AirWaterRefs.sound("stymphalian_bird_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return AirWaterRefs.sound("stymphalian_bird_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return AirWaterRefs.sound("stymphalian_bird_die");
   }

   @Override
   public void setTarget(@Nullable LivingEntity entity) {
      if (entity == null || !this.isVictor(entity)) {
         super.setTarget(entity);
         if (this.flock != null && this.flock.isLeader(this) && entity != null) {
            this.flock.onLeaderAttack(entity);
         }
      }
   }

   public float getDistanceSquared(Vec3 vec3d) {
      float f = (float)(this.getX() - vec3d.x);
      float f1 = (float)(this.getY() - vec3d.y);
      float f2 = (float)(this.getZ() - vec3d.z);
      return f * f + f1 * f1 + f2 * f2;
   }

   protected boolean isTargetInAir() {
      return this.airTarget != null && this.level().getBlockState(this.airTarget).isAir();
   }

   public boolean doesWantToLand() {
      return this.flock != null && !this.flock.isLeader(this) && this.flock.getLeader() != null
         ? this.flock.getLeader().doesWantToLand()
         : this.flyTicks > 500 || this.flyTicks > 40 && this.flyProgress == 0.0F;
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
      return new Animation[]{NO_ANIMATION, ANIMATION_PECK, ANIMATION_SHOOT_ARROWS, ANIMATION_SPEAK};
   }

   @Override
   public boolean shouldAnimalsFear(Entity entity) {
      return com.github.alexthe666.iceandfire.IceAndFire.CONFIG.stympahlianBirdAttackAnimals;
   }
}
