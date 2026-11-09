package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.ai.GorgonAIStareAttack;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityGorgon. */
public class EntityGorgon extends Monster implements IAnimatedEntity, IVillagerFear, IAnimalFear {
   public static final Animation ANIMATION_SCARE = Animation.create(30);
   public static final Animation ANIMATION_HIT = Animation.create(10);
   private int animationTick;
   private Animation currentAnimation;
   private GorgonAIStareAttack aiStare;
   private EntityAIAttackMelee aiMelee;
   private boolean dropXpNow;

   public EntityGorgon(EntityType<? extends EntityGorgon> type, Level worldIn) {
      super(type, worldIn);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.25)
         .add(Attributes.ATTACK_DAMAGE, 3.0)
         .add(Attributes.MAX_HEALTH, IceAndFire.CONFIG.gorgonMaxHealth)
         .add(Attributes.ARMOR, 1.0);
   }

   public static boolean isEntityLookingAt(LivingEntity looker, LivingEntity seen, double degree) {
      degree *= 1.0 + (double)looker.distanceTo(seen) * 0.1;
      Vec3 vec3d = looker.getViewVector(1.0F).normalize();
      Vec3 vec3d1 = new Vec3(
         seen.getX() - looker.getX(),
         seen.getBoundingBox().minY + (double)seen.getEyeHeight() - (looker.getY() + (double)looker.getEyeHeight()),
         seen.getZ() - looker.getZ()
      );
      double d0 = vec3d1.length();
      vec3d1 = vec3d1.normalize();
      double d1 = vec3d.dot(vec3d1);
      return d1 > 1.0 - degree / d0 ? looker.hasLineOfSight(seen) && !isStoneMob(seen) : false;
   }

   public static boolean isStoneMob(LivingEntity mob) {
      return mob instanceof Mob && StoneEntityProperties.isStone(mob);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(2, new RestrictSunGoal(this));
      this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0));
      this.goalSelector.addGoal(3, this.aiStare = new GorgonAIStareAttack(this, 1.0, 0, 15.0F));
      this.goalSelector.addGoal(3, this.aiMelee = new EntityAIAttackMelee(this, 1.0, false));
      this.goalSelector.addGoal(4, new GorgonAIStareAttack(this, 1.0, 0, 15.0F));
      WaterAvoidingRandomStrollGoal wander = new WaterAvoidingRandomStrollGoal(this, 1.0);
      wander.setInterval(20);
      this.goalSelector.addGoal(5, wander);
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F, 1.0F) {
         @Override
         public boolean canContinueToUse() {
            return this.lookAt != null && this.lookAt instanceof Player && ((Player)this.lookAt).isCreative() ? false : super.canContinueToUse();
         }
      });
      this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new EntityAIHurtByTarget(this, false));
      this.targetSelector.addGoal(2, new EntityAINearestAttackableTarget<>(this, Player.class, 0, true, false, entity -> true));
      this.targetSelector.addGoal(3, new EntityAINearestAttackableTarget<>(this, Mob.class, 0, true, false, entity -> {
         StoneEntityProperties properties = StoneEntityProperties.get(entity);
         return entity instanceof Mob && LandUtils.isAlive(entity) && (properties == null || !properties.isStone)
            || entity instanceof IBlacklistedFromStatues && ((IBlacklistedFromStatues)entity).canBeTurnedToStone();
      }));
      this.goalSelector.removeGoal(this.aiMelee);
   }

   public void attackEntityWithRangedAttack(LivingEntity entity) {
      if (!(entity instanceof Player) && entity instanceof Mob) {
         this.forcePreyToLook((Mob)entity);
      }
   }

   @Override
   public boolean doHurtTarget(Entity entityIn) {
      boolean blindness = this.hasEffect(MobEffects.BLINDNESS)
         || this.getTarget() != null && this.getTarget().hasEffect(MobEffects.BLINDNESS)
         || this.getTarget() != null && this.getTarget() instanceof IBlacklistedFromStatues && !((IBlacklistedFromStatues)this.getTarget()).canBeTurnedToStone();
      if (blindness && this.deathTime == 0) {
         if (this.getAnimation() != ANIMATION_HIT) {
            this.setAnimation(ANIMATION_HIT);
         }

         if (entityIn instanceof LivingEntity) {
            ((LivingEntity)entityIn).addEffect(new MobEffectInstance(MobEffects.POISON, 100, 2, false, true));
         }
      }

      return super.doHurtTarget(entityIn);
   }

   @Override
   public boolean removeWhenFarAway(double distance) {
      return false;
   }

   @Override
   public void setTarget(@Nullable LivingEntity entitylivingbaseIn) {
      super.setTarget(entitylivingbaseIn);
      if (entitylivingbaseIn != null && !this.level().isClientSide && this.aiStare != null) {
         boolean blindness = this.hasEffect(MobEffects.BLINDNESS)
            || entitylivingbaseIn.hasEffect(MobEffects.BLINDNESS)
            || entitylivingbaseIn instanceof IBlacklistedFromStatues && !((IBlacklistedFromStatues)entitylivingbaseIn).canBeTurnedToStone();
         if (blindness && this.deathTime == 0) {
            this.goalSelector.removeGoal(this.aiStare);
            this.goalSelector.addGoal(3, this.aiMelee);
         } else {
            this.goalSelector.removeGoal(this.aiMelee);
            this.goalSelector.addGoal(3, this.aiStare);
         }
      }
   }

   @Override
   protected int getBaseExperienceReward() {
      return 20 + this.level().random.nextInt(15);
   }

   /** 1.12 onDeathUpdate: 200 ticks of blood, then the experience and the explosion puffs. */
   @Override
   protected void tickDeath() {
      this.deathTime++;
      this.ambientSoundTime = 20;

      for (int k = 0; k < 5; k++) {
         double d2 = 0.4;
         double d0 = 0.1;
         double d1 = 0.1;
         if (this.level().isClientSide) {
            LandParticles.blood(
               this.level(),
               this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
               this.getY(),
               this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
               d2,
               d0,
               d1
            );
         } else {
            // the 1.12 client consumed rand here too, the server proxy did not
            this.random.nextFloat();
            this.random.nextFloat();
         }
      }

      if (this.deathTime >= 200) {
         if (!this.level().isClientSide) {
            this.dropXpNow = true;
            this.dropExperience(this.getLastHurtByMob());
            this.dropXpNow = false;
            this.remove(Entity.RemovalReason.KILLED);
         }

         for (int k = 0; k < 20; k++) {
            double d2 = this.random.nextGaussian() * 0.02;
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            this.level()
               .addParticle(
                  ParticleTypes.POOF,
                  this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                  this.getY() + (double)(this.random.nextFloat() * this.getBbHeight()),
                  this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                  d2,
                  d0,
                  d1
               );
         }
      }
   }

   @Override
   protected void dropExperience(@Nullable Entity killer) {
      if (this.dropXpNow) {
         super.dropExperience(killer);
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.getTarget() != null) {
         boolean blindness = this.hasEffect(MobEffects.BLINDNESS) || this.getTarget().hasEffect(MobEffects.BLINDNESS);
         this.getLookControl()
            .setLookAt(
               this.getTarget().getX(),
               this.getTarget().getY() + (double)this.getTarget().getEyeHeight(),
               this.getTarget().getZ(),
               (float)this.getHeadRotSpeed(),
               (float)this.getMaxHeadXRot()
            );
         if (!blindness && this.deathTime == 0 && this.getTarget() instanceof Mob && !(this.getTarget() instanceof Player)) {
            this.forcePreyToLook((Mob)this.getTarget());
         }
      }

      if (this.getTarget() != null && isEntityLookingAt(this, this.getTarget(), 0.4) && isEntityLookingAt(this.getTarget(), this, 0.4)) {
         boolean blindness = this.hasEffect(MobEffects.BLINDNESS)
            || this.getTarget().hasEffect(MobEffects.BLINDNESS)
            || this.getTarget() instanceof IBlacklistedFromStatues && !((IBlacklistedFromStatues)this.getTarget()).canBeTurnedToStone();
         if (!blindness && this.deathTime == 0) {
            if (this.getAnimation() != ANIMATION_SCARE) {
               this.playSound(IafLand.sound("gorgon_attack"), 1.0F, 1.0F);
               this.setAnimation(ANIMATION_SCARE);
            }

            if (this.getAnimation() == ANIMATION_SCARE && this.getAnimationTick() > 10) {
               LivingEntity target = this.getTarget();
               if (target instanceof Player) {
                  EntityStoneStatue statue = IafLandMobs.STONE_STATUE.get().create(this.level());
                  statue.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
                  statue.smallArms = true;
                  if (!this.level().isClientSide) {
                     this.level().addFreshEntity(statue);
                  }

                  statue.yRotO = target.getYRot();
                  statue.setYRot(target.getYRot());
                  statue.yHeadRot = target.getYRot();
                  statue.yBodyRot = target.getYRot();
                  statue.yBodyRotO = target.getYRot();
                  if (!this.level().isClientSide) {
                     // 1.12 setDead() on the player, then the gorgon damage (2^31) kills it
                     target.hurt(IafLand.gorgon(this.level()), 2.1474836E9F);
                  }
               } else if (target instanceof Mob && !(target instanceof IBlacklistedFromStatues)
                  || target instanceof IBlacklistedFromStatues && ((IBlacklistedFromStatues)target).canBeTurnedToStone()) {
                  StoneEntityProperties properties = StoneEntityProperties.get(target);
                  Mob attackTarget = (Mob)target;
                  if (properties != null || !properties.isStone) {
                     StoneEntityProperties.setStone(attackTarget, true);
                     this.playSound(IafLand.sound("gorgon_turn_stone"), 1.0F, 1.0F);
                     this.setTarget(null);
                  }

                  LandUtils.stopFlying(attackTarget);
               }
            }
         }
      }

      AnimationHandler.INSTANCE.updateAnimations(this);
   }

   @Override
   public int getMaxHeadXRot() {
      return 10;
   }

   @Override
   public int getHeadRotSpeed() {
      return 10;
   }

   public void forcePreyToLook(Mob mob) {
      mob.getLookControl().setLookAt(this.getX(), this.getY() + (double)this.getEyeHeight(), this.getZ(), (float)mob.getHeadRotSpeed(), (float)mob.getMaxHeadXRot());
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
      return new Animation[]{ANIMATION_SCARE, ANIMATION_HIT};
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return IafLand.sound("gorgon_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return IafLand.sound("gorgon_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return IafLand.sound("gorgon_die");
   }

   @Override
   public boolean shouldAnimalsFear(Entity entity) {
      return true;
   }
}
