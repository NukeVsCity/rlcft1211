package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.core.ModSounds;
import com.github.alexthe666.iceandfire.entity.ai.AquaticAITempt;
import com.github.alexthe666.iceandfire.entity.ai.DragonAIAirTarget;
import com.github.alexthe666.iceandfire.entity.ai.DragonAILookIdle;
import com.github.alexthe666.iceandfire.entity.ai.DragonAIMate;
import com.github.alexthe666.iceandfire.entity.ai.DragonAITarget;
import com.github.alexthe666.iceandfire.entity.ai.DragonAITargetItems;
import com.github.alexthe666.iceandfire.entity.ai.DragonAIWander;
import com.github.alexthe666.iceandfire.entity.ai.DragonAIWatchClosest;
import com.github.alexthe666.iceandfire.entity.ai.DragonAIWaterTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIOwnerHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIOwnerHurtTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAISit;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

/**
 * Ice and Fire 1.7.1 EntityIceDragon. 1.12 isSwimming/setSwimming are {@link #isDragonSwimming()}/{@link #setDragonSwimming}
 * (1.21 Entity.isSwimming is the swimming pose and gets reset every tick).
 */
public class EntityIceDragon extends EntityDragonBase {
   private static final EntityDataAccessor<Boolean> SWIMMING = SynchedEntityData.defineId(EntityIceDragon.class, EntityDataSerializers.BOOLEAN);
   public static Animation ANIMATION_FIRECHARGE;
   public static final float[] growth_stage_1 = new float[]{1.0F, 3.0F};
   public static final float[] growth_stage_2 = new float[]{3.0F, 7.0F};
   public static final float[] growth_stage_3 = new float[]{7.0F, 12.5F};
   public static final float[] growth_stage_4 = new float[]{12.5F, 20.0F};
   public static final float[] growth_stage_5 = new float[]{20.0F, 30.0F};
   public boolean isSwimming;
   public float swimProgress;
   public int ticksSwiming;
   public BlockPos waterTarget;
   public static final ResourceKey<LootTable> FEMALE_LOOT = EntityFireDragon.loot("dragon/ice_dragon_female");
   public static final ResourceKey<LootTable> MALE_LOOT = EntityFireDragon.loot("dragon/ice_dragon_male");
   public static final ResourceKey<LootTable> SKELETON_LOOT = EntityFireDragon.loot("dragon/ice_dragon_skeleton");

   public EntityIceDragon(EntityType<? extends EntityIceDragon> type, Level worldIn) {
      super(
         type,
         worldIn,
         1.0,
         (double)(1 + IceAndFire.CONFIG.dragonAttackDamage),
         (double)IceAndFire.CONFIG.dragonHealth * 0.04,
         (double)IceAndFire.CONFIG.dragonHealth,
         0.15F,
         0.4F
      );
      ANIMATION_SPEAK = Animation.create(20);
      ANIMATION_BITE = Animation.create(35);
      ANIMATION_SHAKEPREY = Animation.create(65);
      ANIMATION_TAILWHACK = Animation.create(40);
      ANIMATION_FIRECHARGE = Animation.create(25);
      ANIMATION_WINGBLAST = Animation.create(50);
      ANIMATION_ROAR = Animation.create(40);
      this.growth_stages = new float[][]{growth_stage_1, growth_stage_2, growth_stage_3, growth_stage_4, growth_stage_5};
   }

   @Override
   protected void initEntityAI() {
      this.tasks.addTask(1, this.aiSit = new EntityAISit(this));
      this.tasks.addTask(2, new DragonAIMate(this, 1.0));
      this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.5, false));
      this.tasks.addTask(4, new AquaticAITempt(this, 1.0, ModItems.frost_stew, false));
      this.tasks.addTask(5, new DragonAIAirTarget(this));
      this.tasks.addTask(5, new DragonAIWaterTarget(this));
      this.tasks.addTask(6, new DragonAIWander(this, 1.0));
      this.tasks.addTask(7, new DragonAIWatchClosest(this, LivingEntity.class, 6.0F));
      this.tasks.addTask(7, new DragonAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
      this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
      this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));
      this.targetTasks.addTask(4, new DragonAITarget<>(this, LivingEntity.class, true, entity -> entity instanceof LivingEntity && DragonUtils.isAlive(entity)));
      this.targetTasks.addTask(5, new DragonAITargetItems(this, false));
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(SWIMMING, false);
   }

   @Override
   public String getVariantName(int variant) {
      switch (variant) {
         case 1:
            return "white_";
         case 2:
            return "sapphire_";
         case 3:
            return "silver_";
         default:
            return "blue_";
      }
   }

   /** 1.12 canBreatheUnderwater. */
   @Override
   public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
      return false;
   }

   @Override
   public ItemLike getVariantScale(int variant) {
      switch (variant) {
         case 1:
            return ModItems.dragonscales_white;
         case 2:
            return ModItems.dragonscales_sapphire;
         case 3:
            return ModItems.dragonscales_silver;
         default:
            return ModItems.dragonscales_blue;
      }
   }

   @Override
   public ItemLike getVariantEgg(int variant) {
      switch (variant) {
         case 1:
            return ModItems.dragonegg_white;
         case 2:
            return ModItems.dragonegg_sapphire;
         case 3:
            return ModItems.dragonegg_silver;
         default:
            return ModItems.dragonegg_blue;
      }
   }

   /** 1.12 isPushedByWater. */
   @Override
   public boolean isPushedByFluid() {
      return false;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putBoolean("Swimming", this.isDragonSwimming());
      compound.putInt("SwimmingTicks", this.ticksSwiming);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.setDragonSwimming(compound.getBoolean("Swimming"));
      this.ticksSwiming = compound.getInt("SwimmingTicks");
   }

   @Override
   public boolean attackEntityAsMob(Entity entityIn) {
      return EntityFireDragon.dragonAttack(this, entityIn, false);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.isInLava()
         && !this.isFlying()
         && this.getPassengers().isEmpty()
         && !this.isChild()
         && !this.isHovering()
         && !this.isDragonSleeping()
         && this.canMove()
         && this.onGround()) {
         this.setHovering(true);
         this.setSleeping(false);
         this.setSitting(false);
         this.flyHovering = 0;
         this.flyTicks = 0;
      }

      if (!this.level().isClientSide) {
         if (this.getTarget() != null && !this.isDragonSleeping() && this.getAnimation() != ANIMATION_SHAKEPREY) {
            if ((!this.attackDecision || this.isFlying()) && !this.isTargetBlocked(new Vec3(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ()))) {
               this.shootIceAtMob(this.getTarget());
            } else if (this.getBoundingBox()
               .inflate((double)(this.getRenderSize() * 0.5F), (double)(this.getRenderSize() * 0.5F), (double)(this.getRenderSize() * 0.5F))
               .intersects(this.getTarget().getBoundingBox())) {
               this.attackEntityAsMob(this.getTarget());
            }
         } else {
            this.setBreathingFire(false);
         }
      }

      boolean swimming = this.isDragonSwimming() && !this.isHovering() && !this.isFlying() && this.ridingProgress == 0.0F;
      if (swimming && this.swimProgress < 20.0F) {
         this.swimProgress += 0.5F;
      } else if (!swimming && this.swimProgress > 0.0F) {
         this.swimProgress -= 0.5F;
      }

      if (this.isInsideWaterBlock() && !this.isDragonSwimming()) {
         this.setDragonSwimming(true);
         this.ticksSwiming = 0;
      }

      if (this.isInsideWaterBlock()) {
         this.swimAround();
      }

      if (!this.isInsideWaterBlock() && this.isDragonSwimming()) {
         this.setDragonSwimming(false);
         this.ticksSwiming = 0;
      }

      if (this.isDragonSwimming()) {
         this.ticksSwiming++;
         if (this.isInsideWaterBlock() && this.ticksSwiming > 1000 && !this.isChild() && !this.isHovering() && !this.isFlying()) {
            this.setHovering(true);
         }
      }
   }

   /** 1.12 isInsideOfMaterial(WATER): the eyes are in water. */
   public boolean isInsideWaterBlock() {
      return this.isEyeInFluid(FluidTags.WATER);
   }

   private float headX() {
      return (float)(this.getX() + (double)(1.8F * this.getRenderSize() * 0.3F) * Math.cos((double)(this.getYRot() + 90.0F) * Math.PI / 180.0));
   }

   private float headZ() {
      return (float)(this.getZ() + (double)(1.8F * this.getRenderSize() * 0.3F) * Math.sin((double)(this.getYRot() + 90.0F) * Math.PI / 180.0));
   }

   private float headY() {
      return (float)(this.getY() + 0.5 * (double)this.getRenderSize() * 0.3F);
   }

   @Override
   public void riderShootFire(Entity controller) {
      if (this.getRNG().nextInt(5) == 0 && !this.isChild()) {
         if (this.getAnimation() != ANIMATION_FIRECHARGE) {
            this.setAnimation(ANIMATION_FIRECHARGE);
         } else if (this.getAnimationTick() == 15) {
            this.setYRot(this.yBodyRot);
            float headPosX = this.headX();
            float headPosZ = this.headZ();
            float headPosY = this.headY();
            this.playSound(DragonCompat.snd(ModSounds.ICEDRAGON_BREATH), 4.0F, 1.0F);
            Vec3 look = controller.getViewVector(1.0F);
            double d2 = look.x;
            double d3 = look.y;
            double d4 = look.z;
            float inaccuracy = 1.0F;
            d2 += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
            d3 += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
            d4 += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
            EntityDragonIceCharge entitylargefireball = new EntityDragonIceCharge(this.level(), this, d2, d3, d4);
            float size = this.isChild() ? 0.4F : (this.isAdult() ? 1.3F : 0.8F);
            entitylargefireball.setSizes(size, size);
            entitylargefireball.setPos((double)headPosX, (double)headPosY, (double)headPosZ);
            if (!this.level().isClientSide) {
               this.level().addFreshEntity(entitylargefireball);
            }
         }
      } else if (this.isBreathingFire()) {
         if (this.isActuallyBreathingFire() && this.tickCount % 3 == 0) {
            this.setYRot(this.yBodyRot);
            float headPosX = this.headX();
            float headPosZ = this.headZ();
            float headPosY = this.headY();
            Vec3 look = controller.getViewVector(1.0F);
            double d2 = look.x;
            double d3 = look.y;
            double d4 = look.z;
            float inaccuracy = 1.0F;
            d2 += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
            d3 += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
            d4 += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
            EntityDragonIceProjectile entitylargefireball = new EntityDragonIceProjectile(this.level(), this, d2, d3, d4);
            this.playSound(DragonCompat.snd(ModSounds.ICEDRAGON_BREATH), 4.0F, 1.0F);
            entitylargefireball.setPos((double)headPosX, (double)headPosY, (double)headPosZ);
            if (!this.level().isClientSide) {
               this.level().addFreshEntity(entitylargefireball);
            }
         }
      } else {
         this.setBreathingFire(true);
      }
   }

   public void swimAround() {
      if (this.waterTarget != null) {
         if (!this.isTargetInWater()
            || Math.sqrt(this.distanceToSqr((double)this.waterTarget.getX() + 0.5, (double)this.waterTarget.getY() + 0.5, (double)this.waterTarget.getZ() + 0.5)) < 2.0
            || this.ticksSwiming > 6000) {
            this.waterTarget = null;
         }

         this.swimTowardsTarget();
      }
   }

   @Override
   public ResourceKey<LootTable> getDeadLootTable() {
      if (this.getDeathStage() >= this.getAgeInDays() / 5 / 2) {
         return SKELETON_LOOT;
      } else {
         return this.isMale() ? MALE_LOOT : FEMALE_LOOT;
      }
   }

   public void swimTowardsTarget() {
      if (this.waterTarget != null
         && this.isTargetInWater()
         && this.isInsideWaterBlock()
         && this.getDistanceSquared(new Vec3((double)this.waterTarget.getX(), this.getY(), (double)this.waterTarget.getZ())) > 3.0F) {
         double targetX = (double)this.waterTarget.getX() + 0.5 - this.getX();
         double targetY = (double)this.waterTarget.getY() + 1.0 - this.getY();
         double targetZ = (double)this.waterTarget.getZ() + 0.5 - this.getZ();
         double speed = (double)(3 * (this.getAgeInDays() / 125) + 2);
         double mx = this.motionX() + (Math.signum(targetX) * 0.5 - this.motionX()) * 0.100000000372529 * speed;
         double my = this.motionY() + (Math.signum(targetY) * 0.5 - this.motionY()) * 0.100000000372529 * speed;
         double mz = this.motionZ() + (Math.signum(targetZ) * 0.5 - this.motionZ()) * 0.100000000372529 * speed;
         this.setMotion(mx, my, mz);
         float angle = (float)(Math.atan2(this.motionZ(), this.motionX()) * 180.0 / Math.PI) - 90.0F;
         float rotation = Mth.wrapDegrees(angle - this.getYRot());
         this.zza = 0.5F;
         this.yRotO = this.getYRot();
         this.setYRot(this.getYRot() + rotation);
      } else {
         this.waterTarget = null;
      }
   }

   protected boolean isTargetInWater() {
      return this.waterTarget != null && DragonCompat.material(this.level(), this.waterTarget) == DragonCompat.Material.WATER;
   }

   private void shootIceAtMob(LivingEntity entity) {
      if (!this.attackDecision) {
         if (this.getRNG().nextInt(5) == 0) {
            if (this.getAnimation() != ANIMATION_FIRECHARGE) {
               this.setAnimation(ANIMATION_FIRECHARGE);
            } else if (this.getAnimationTick() == 15) {
               this.setYRot(this.yBodyRot);
               float headPosX = this.headX();
               float headPosZ = this.headZ();
               float headPosY = this.headY();
               double d2 = entity.getX() - (double)headPosX;
               double d3 = entity.getY() - (double)headPosY;
               double d4 = entity.getZ() - (double)headPosZ;
               float inaccuracy = 1.0F;
               d2 += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
               d3 += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
               d4 += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
               this.playSound(DragonCompat.snd(ModSounds.ICEDRAGON_BREATH), 4.0F, 1.0F);
               EntityDragonIceCharge entitylargefireball = new EntityDragonIceCharge(this.level(), this, d2, d3, d4);
               float size = this.isChild() ? 0.4F : (this.isAdult() ? 1.3F : 0.8F);
               entitylargefireball.setSizes(size, size);
               entitylargefireball.setPos((double)headPosX, (double)headPosY, (double)headPosZ);
               if (!this.level().isClientSide) {
                  this.level().addFreshEntity(entitylargefireball);
               }

               if (entity.isRemoved()) {
                  this.setBreathingFire(false);
                  this.attackDecision = true;
               }
            }
         } else if (this.isBreathingFire()) {
            if (this.isActuallyBreathingFire() && this.tickCount % 3 == 0) {
               this.setYRot(this.yBodyRot);
               float headPosXx = this.headX();
               float headPosZx = this.headZ();
               float headPosYx = this.headY();
               double d2x = entity.getX() - (double)headPosXx;
               double d3x = entity.getY() - (double)headPosYx;
               double d4x = entity.getZ() - (double)headPosZx;
               float inaccuracyx = 1.0F;
               d2x += this.random.nextGaussian() * 0.0075F * (double)inaccuracyx;
               d3x += this.random.nextGaussian() * 0.0075F * (double)inaccuracyx;
               d4x += this.random.nextGaussian() * 0.0075F * (double)inaccuracyx;
               this.playSound(DragonCompat.snd(ModSounds.ICEDRAGON_BREATH), 4.0F, 1.0F);
               EntityDragonIceProjectile entitylargefireballx = new EntityDragonIceProjectile(this.level(), this, d2x, d3x, d4x);
               float sizex = this.isChild() ? 0.4F : (this.isAdult() ? 1.3F : 0.8F);
               entitylargefireballx.setPos((double)headPosXx, (double)headPosYx, (double)headPosZx);
               if (!this.level().isClientSide && !entity.isRemoved()) {
                  this.level().addFreshEntity(entitylargefireballx);
               }

               entitylargefireballx.setSizes(sizex, sizex);
               if (entity.isRemoved()) {
                  this.setBreathingFire(false);
                  this.attackDecision = true;
               }
            }
         } else {
            this.setBreathingFire(true);
         }
      }

      this.lookAt(entity, 360.0F, 360.0F);
   }

   public boolean isDragonSwimming() {
      if (this.level().isClientSide) {
         boolean swimming = this.entityData.get(SWIMMING);
         this.isSwimming = swimming;
         return swimming;
      } else {
         return this.isSwimming;
      }
   }

   public void setDragonSwimming(boolean swimming) {
      this.entityData.set(SWIMMING, swimming);
      if (!this.level().isClientSide) {
         this.isSwimming = swimming;
      }
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return DragonCompat.snd(this.isTeen() ? ModSounds.ICEDRAGON_TEEN_IDLE : (this.isAdult() ? ModSounds.ICEDRAGON_ADULT_IDLE : ModSounds.ICEDRAGON_CHILD_IDLE));
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return DragonCompat.snd(this.isTeen() ? ModSounds.ICEDRAGON_TEEN_HURT : (this.isAdult() ? ModSounds.ICEDRAGON_ADULT_HURT : ModSounds.ICEDRAGON_CHILD_HURT));
   }

   @Override
   protected SoundEvent getDeathSound() {
      return DragonCompat.snd(this.isTeen() ? ModSounds.ICEDRAGON_TEEN_DEATH : (this.isAdult() ? ModSounds.ICEDRAGON_ADULT_DEATH : ModSounds.ICEDRAGON_CHILD_DEATH));
   }

   @Override
   public SoundEvent getRoarSound() {
      return DragonCompat.snd(this.isTeen() ? ModSounds.ICEDRAGON_TEEN_ROAR : (this.isAdult() ? ModSounds.ICEDRAGON_ADULT_ROAR : ModSounds.ICEDRAGON_CHILD_ROAR));
   }

   @Override
   public Animation[] getAnimations() {
      return new Animation[]{
         IAnimatedEntity.NO_ANIMATION,
         EntityDragonBase.ANIMATION_EAT,
         EntityDragonBase.ANIMATION_SPEAK,
         EntityDragonBase.ANIMATION_BITE,
         EntityDragonBase.ANIMATION_SHAKEPREY,
         ANIMATION_TAILWHACK,
         ANIMATION_FIRECHARGE,
         ANIMATION_WINGBLAST,
         ANIMATION_ROAR
      };
   }

   /** 1.12 isBreedingItem. */
   @Override
   public boolean isFood(ItemStack stack) {
      return !stack.isEmpty() && stack.is(ModItems.frost_stew);
   }
}
