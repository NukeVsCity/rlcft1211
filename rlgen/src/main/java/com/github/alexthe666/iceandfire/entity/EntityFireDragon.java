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
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIOwnerHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIOwnerHurtTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAISit;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAISwimming;
import java.util.Random;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityFireDragon. */
public class EntityFireDragon extends EntityDragonBase {
   public static Animation ANIMATION_FIRECHARGE;
   public static final float[] growth_stage_1 = new float[]{1.0F, 3.0F};
   public static final float[] growth_stage_2 = new float[]{3.0F, 7.0F};
   public static final float[] growth_stage_3 = new float[]{7.0F, 12.5F};
   public static final float[] growth_stage_4 = new float[]{12.5F, 20.0F};
   public static final float[] growth_stage_5 = new float[]{20.0F, 30.0F};
   public static final ResourceKey<LootTable> FEMALE_LOOT = loot("dragon/fire_dragon_female");
   public static final ResourceKey<LootTable> MALE_LOOT = loot("dragon/fire_dragon_male");
   public static final ResourceKey<LootTable> SKELETON_LOOT = loot("dragon/fire_dragon_skeleton");

   static ResourceKey<LootTable> loot(String path) {
      return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("iceandfire", path));
   }

   public EntityFireDragon(EntityType<? extends EntityFireDragon> type, Level worldIn) {
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
      this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
      this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
      this.setPathfindingMalus(PathType.LAVA, 8.0F);
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
      this.tasks.addTask(2, new EntityAISwimming(this));
      this.tasks.addTask(3, new DragonAIMate(this, 1.0));
      this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.5, false));
      this.tasks.addTask(5, new AquaticAITempt(this, 1.0, ModItems.fire_stew, false));
      this.tasks.addTask(6, new DragonAIAirTarget(this));
      this.tasks.addTask(7, new DragonAIWander(this, 1.0));
      this.tasks.addTask(8, new DragonAIWatchClosest(this, LivingEntity.class, 6.0F));
      this.tasks.addTask(8, new DragonAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
      this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
      this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));
      this.targetTasks.addTask(4, new DragonAITarget<>(this, LivingEntity.class, true, entity -> entity instanceof LivingEntity && DragonUtils.isAlive(entity)));
      this.targetTasks.addTask(5, new DragonAITargetItems(this, false));
   }

   @Override
   public String getVariantName(int variant) {
      switch (variant) {
         case 1:
            return "green_";
         case 2:
            return "bronze_";
         case 3:
            return "gray_";
         default:
            return "red_";
      }
   }

   @Override
   public ItemLike getVariantScale(int variant) {
      switch (variant) {
         case 1:
            return ModItems.dragonscales_green;
         case 2:
            return ModItems.dragonscales_bronze;
         case 3:
            return ModItems.dragonscales_gray;
         default:
            return ModItems.dragonscales_red;
      }
   }

   @Override
   public ItemLike getVariantEgg(int variant) {
      switch (variant) {
         case 1:
            return ModItems.dragonegg_green;
         case 2:
            return ModItems.dragonegg_bronze;
         case 3:
            return ModItems.dragonegg_gray;
         default:
            return ModItems.dragonegg_red;
      }
   }

   /** The bite of 1.12 (damage between animation ticks 15 and 25), shared by both dragons. */
   static boolean bite(EntityDragonBase dragon, Entity entityIn) {
      if (dragon.getAnimation() != ANIMATION_BITE) {
         dragon.setAnimation(ANIMATION_BITE);
         return false;
      }

      if (dragon.getAnimationTick() > 15 && dragon.getAnimationTick() < 25) {
         boolean flag = dragon.hurtWithAttackDamageNoEnchant(entityIn);
         dragon.attackDecision = dragon.getRNG().nextBoolean();
         return flag;
      }
      return false;
   }

   /** 1.12 EntityFireDragon/EntityIceDragon.attackEntityAsMob: bite, grab and shake small prey, tail whack or wing blast. */
   static boolean dragonAttack(EntityDragonBase dragon, Entity entityIn) {
      if (dragon.getAnimation() == ANIMATION_WINGBLAST) {
         return false;
      } else {
         switch (new Random().nextInt(4)) {
            case 0:
               return bite(dragon, entityIn);
            case 1:
               if (new Random().nextInt(2) == 0
                  && dragon.isDirectPathBetweenPoints(dragon, dragon.position(), entityIn.position())
                  && entityIn.getBbWidth() < dragon.getBbWidth() * 0.5F
                  && dragon.getControllingPassenger() == null
                  && dragon.getDragonStage() > 1
                  && !(entityIn instanceof EntityDragonBase)
                  && !DragonUtils.isAnimaniaMob(entityIn)) {
                  if (dragon.getAnimation() != ANIMATION_SHAKEPREY) {
                     dragon.setAnimation(ANIMATION_SHAKEPREY);
                     entityIn.startRiding(dragon);
                     dragon.attackDecision = dragon.getRNG().nextBoolean();
                     return true;
                  }
               } else {
                  return bite(dragon, entityIn);
               }
               break;
            case 2:
               if (dragon.getAnimation() != ANIMATION_TAILWHACK) {
                  dragon.setAnimation(ANIMATION_TAILWHACK);
                  return false;
               }

               if (dragon.getAnimationTick() > 20 && dragon.getAnimationTick() < 25) {
                  boolean flag2 = dragon.hurtWithAttackDamageNoEnchant(entityIn);
                  if (entityIn instanceof LivingEntity) {
                     ((LivingEntity)entityIn).knockback(1.0, 1.0, 1.0);
                  }

                  dragon.attackDecision = dragon.getRNG().nextBoolean();
                  return flag2;
               }
               break;
            case 3:
               if (dragon.onGround() && !dragon.isHovering() && !dragon.isFlying() && dragon.getDragonStage() > 2) {
                  if (dragon.getAnimation() != ANIMATION_WINGBLAST) {
                     dragon.setAnimation(ANIMATION_WINGBLAST);
                     return true;
                  }
               } else {
                  return bite(dragon, entityIn);
               }
               break;
            default:
               return bite(dragon, entityIn);
         }

         return false;
      }
   }

   @Override
   public boolean attackEntityAsMob(Entity entityIn) {
      return dragonAttack(this, entityIn);
   }

   public void moveEntityTowards(Entity entity, double x, double y, double z, float velocity, float inaccuracy) {
      float f = Mth.sqrt((float)(x * x + y * y + z * z));
      x /= (double)f;
      y /= (double)f;
      z /= (double)f;
      x += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
      y += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
      z += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
      x *= (double)velocity;
      y *= (double)velocity;
      z *= (double)velocity;
      entity.setDeltaMovement(x, y, z);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         if (this.getTarget() != null && !this.isDragonSleeping() && this.getAnimation() != ANIMATION_SHAKEPREY) {
            if ((!this.attackDecision || this.isFlying()) && !this.isTargetBlocked(new Vec3(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ()))) {
               this.shootFireAtMob(this.getTarget());
            } else if (this.getBoundingBox()
               .inflate((double)(this.getRenderSize() * 0.5F), (double)(this.getRenderSize() * 0.5F), (double)(this.getRenderSize() * 0.5F))
               .intersects(this.getTarget().getBoundingBox())) {
               this.attackEntityAsMob(this.getTarget());
            }
         } else {
            this.setBreathingFire(false);
         }
      }
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
            this.playSound(DragonCompat.snd(ModSounds.FIREDRAGON_BREATH), 4.0F, 1.0F);
            Vec3 look = controller.getViewVector(1.0F);
            double d2 = look.x;
            double d3 = look.y;
            double d4 = look.z;
            float inaccuracy = 1.0F;
            d2 += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
            d3 += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
            d4 += this.random.nextGaussian() * 0.0075F * (double)inaccuracy;
            EntityDragonFireCharge entitylargefireball = new EntityDragonFireCharge(this.level(), this, d2, d3, d4);
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
            EntityDragonFire entitylargefireball = new EntityDragonFire(this.level(), this, d2, d3, d4);
            this.playSound(DragonCompat.snd(ModSounds.FIREDRAGON_BREATH), 4.0F, 1.0F);
            entitylargefireball.setPos((double)headPosX, (double)headPosY, (double)headPosZ);
            if (!this.level().isClientSide) {
               this.level().addFreshEntity(entitylargefireball);
            }
         }
      } else {
         this.setBreathingFire(true);
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

   private void shootFireAtMob(LivingEntity entity) {
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
               this.playSound(DragonCompat.snd(ModSounds.FIREDRAGON_BREATH), 4.0F, 1.0F);
               EntityDragonFireCharge entitylargefireball = new EntityDragonFireCharge(this.level(), this, d2, d3, d4);
               float size = this.isChild() ? 0.4F : (this.isAdult() ? 1.3F : 0.8F);
               entitylargefireball.setSizes(size, size);
               entitylargefireball.setPos((double)headPosX, (double)headPosY, (double)headPosZ);
               if (!this.level().isClientSide) {
                  this.level().addFreshEntity(entitylargefireball);
               }

               if (entity.isRemoved()) {
                  this.setBreathingFire(false);
                  this.attackDecision = this.getRNG().nextBoolean();
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
               this.playSound(DragonCompat.snd(ModSounds.FIREDRAGON_BREATH), 4.0F, 1.0F);
               EntityDragonFire entitylargefireballx = new EntityDragonFire(this.level(), this, d2x, d3x, d4x);
               float sizex = this.isChild() ? 0.4F : (this.isAdult() ? 1.3F : 0.8F);
               entitylargefireballx.setPos((double)headPosXx, (double)headPosYx, (double)headPosZx);
               if (!this.level().isClientSide && !entity.isRemoved()) {
                  this.level().addFreshEntity(entitylargefireballx);
               }

               entitylargefireballx.setSizes(sizex, sizex);
               if (entity.isRemoved()) {
                  this.setBreathingFire(false);
                  this.attackDecision = this.getRNG().nextBoolean();
               }
            }
         } else {
            this.setBreathingFire(true);
         }
      }

      this.lookAt(entity, 360.0F, 360.0F);
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return DragonCompat.snd(this.isTeen() ? ModSounds.FIREDRAGON_TEEN_IDLE : (this.isAdult() ? ModSounds.FIREDRAGON_ADULT_IDLE : ModSounds.FIREDRAGON_CHILD_IDLE));
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return DragonCompat.snd(this.isTeen() ? ModSounds.FIREDRAGON_TEEN_HURT : (this.isAdult() ? ModSounds.FIREDRAGON_ADULT_HURT : ModSounds.FIREDRAGON_CHILD_HURT));
   }

   @Override
   protected SoundEvent getDeathSound() {
      return DragonCompat.snd(this.isTeen() ? ModSounds.FIREDRAGON_TEEN_DEATH : (this.isAdult() ? ModSounds.FIREDRAGON_ADULT_DEATH : ModSounds.FIREDRAGON_CHILD_DEATH));
   }

   @Override
   public SoundEvent getRoarSound() {
      return DragonCompat.snd(this.isTeen() ? ModSounds.FIREDRAGON_TEEN_ROAR : (this.isAdult() ? ModSounds.FIREDRAGON_ADULT_ROAR : ModSounds.FIREDRAGON_CHILD_ROAR));
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
      return !stack.isEmpty() && stack.is(ModItems.fire_stew);
   }
}
