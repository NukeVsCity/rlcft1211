package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityCockatrice;
import com.github.alexthe666.iceandfire.entity.EntityGorgon;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** 1.12 CockatriceAIStareAttack: walk into the target's view and stare. */
public class CockatriceAIStareAttack extends Goal {
   private final EntityCockatrice entity;
   private final double moveSpeedAmp;
   private final float maxAttackDistance;
   private int attackCooldown;
   private int attackTime = -1;
   private int seeTime;
   private boolean strafingClockwise;
   private boolean strafingBackwards;
   private int strafingTime = -1;
   private BlockPos target = null;
   private int walkingTime = -1;
   private float prevYaw;

   public CockatriceAIStareAttack(EntityCockatrice cockatrice, double speedAmplifier, int delay, float maxDistance) {
      this.entity = cockatrice;
      this.moveSpeedAmp = speedAmplifier;
      this.attackCooldown = delay;
      this.maxAttackDistance = maxDistance * maxDistance;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   public void setAttackCooldown(int cooldown) {
      this.attackCooldown = cooldown;
   }

   @Override
   public boolean canUse() {
      return this.entity.getTarget() != null;
   }

   @Override
   public boolean canContinueToUse() {
      return this.canUse();
   }

   @Override
   public void stop() {
      this.seeTime = 0;
      this.attackTime = -1;
      this.entity.stopUsingItem();
      this.entity.getNavigation().stop();
      this.target = null;
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      LivingEntity entitylivingbase = this.entity.getTarget();
      if (entitylivingbase != null) {
         if (EntityGorgon.isStoneMob(entitylivingbase) || !entitylivingbase.isAlive()) {
            this.entity.setTarget(null);
            this.entity.setTargetedEntity(0);
            this.stop();
            return;
         }

         if (!isEntityLookingAt(entitylivingbase, this.entity, 0.6F)
            || entitylivingbase.xo != this.entity.getX()
            || entitylivingbase.yo != this.entity.getY()
            || entitylivingbase.zo != this.entity.getZ()) {
            this.entity.getNavigation().stop();
            this.prevYaw = entitylivingbase.getYRot();
            BlockPos pos = EntityCockatrice.getBlockInTargetsViewCockatrice(this.entity, entitylivingbase);
            if (this.target == null || pos.distSqr(this.target) > 4.0) {
               this.target = pos;
            }
         }

         this.entity.setTargetedEntity(entitylivingbase.getId());
         double d0 = this.entity.distanceToSqr(entitylivingbase.getX(), entitylivingbase.getBoundingBox().minY, entitylivingbase.getZ());
         boolean flag = this.entity.getSensing().hasLineOfSight(entitylivingbase);
         boolean flag1 = this.seeTime > 0;
         if (flag != flag1) {
            this.seeTime = 0;
         }

         if (flag) {
            this.seeTime++;
         } else {
            this.seeTime--;
         }

         if (this.target != null
            && Math.sqrt(this.entity.distanceToSqr((double)this.target.getX(), (double)this.target.getY(), (double)this.target.getZ())) > 4.0
            && !isEntityLookingAt(entitylivingbase, this.entity, 0.6F)) {
            this.entity.getNavigation().moveTo((double)this.target.getX(), (double)this.target.getY(), (double)this.target.getZ(), this.moveSpeedAmp);
         }

         this.entity
            .getLookControl()
            .setLookAt(
               entitylivingbase.getX(),
               entitylivingbase.getY() + (double)entitylivingbase.getEyeHeight(),
               entitylivingbase.getZ(),
               (float)this.entity.getHeadRotSpeed(),
               (float)this.entity.getMaxHeadXRot()
            );
      }
   }

   public static boolean isEntityLookingAt(LivingEntity looker, LivingEntity seen, double degree) {
      Vec3 vec3d = looker.getViewVector(1.0F).normalize();
      Vec3 vec3d1 = new Vec3(
         seen.getX() - looker.getX(),
         seen.getBoundingBox().minY + (double)seen.getEyeHeight() - (looker.getY() + (double)looker.getEyeHeight()),
         seen.getZ() - looker.getZ()
      );
      double d0 = vec3d1.length();
      vec3d1 = vec3d1.normalize();
      double d1 = vec3d.dot(vec3d1);
      return d1 > 1.0 - degree / d0;
   }
}
