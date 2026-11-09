package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

/** Ice and Fire 1.7.1 HippogryphAIAttackMelee (EntityAIAttackMelee without the attack cooldown check, reach width*4). */
public class HippogryphAIAttackMelee extends EntityAIBase {
   Level world;
   protected PathfinderMob attacker;
   protected int attackTick;
   double speedTowardsTarget;
   boolean longMemory;
   Path path;
   private int delayCounter;
   private double targetX;
   private double targetY;
   private double targetZ;
   protected final int attackInterval = 20;
   private int failedPathFindingPenalty = 0;
   private boolean canPenalize = false;

   public HippogryphAIAttackMelee(PathfinderMob creature, double speedIn, boolean useLongMemory) {
      this.attacker = creature;
      this.world = creature.level();
      this.speedTowardsTarget = speedIn;
      this.longMemory = useLongMemory;
      this.setMutexBits(3);
   }

   @Override
   public boolean shouldExecute() {
      LivingEntity target = this.attacker.getTarget();
      if (target == null) {
         return false;
      } else if (!target.isAlive()) {
         return false;
      } else if (this.canPenalize) {
         if (--this.delayCounter <= 0) {
            this.path = this.attacker.getNavigation().createPath(target, 0);
            this.delayCounter = 4 + this.attacker.getRandom().nextInt(7);
            return this.path != null;
         } else {
            return true;
         }
      } else {
         this.path = this.attacker.getNavigation().createPath(target, 0);
         return this.path != null
            ? true
            : this.getAttackReachSqr(target) >= this.attacker.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      LivingEntity target = this.attacker.getTarget();
      if (target == null) {
         return false;
      } else if (!target.isAlive()) {
         return false;
      } else if (!this.longMemory) {
         return !this.attacker.getNavigation().isDone();
      } else {
         return !this.attacker.isWithinRestriction(target.blockPosition())
            ? false
            : !(target instanceof Player p) || !p.isSpectator() && !p.isCreative();
      }
   }

   @Override
   public void startExecuting() {
      this.attacker.getNavigation().moveTo(this.path, this.speedTowardsTarget);
      this.delayCounter = 0;
   }

   @Override
   public void resetTask() {
      LivingEntity target = this.attacker.getTarget();
      if (target instanceof Player p && (p.isSpectator() || p.isCreative())) {
         this.attacker.setTarget(null);
      }
      this.attacker.getNavigation().stop();
   }

   @Override
   public void updateTask() {
      LivingEntity target = this.attacker.getTarget();
      if (target != null) {
         this.attacker.getLookControl().setLookAt(target, 30.0F, 30.0F);
         double d0 = this.attacker.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
         this.delayCounter--;
         if ((this.longMemory || this.attacker.getSensing().hasLineOfSight(target)) && this.delayCounter <= 0
            && (this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0 || target.distanceToSqr(this.targetX, this.targetY, this.targetZ) >= 1.0
               || this.attacker.getRandom().nextFloat() < 0.05F)) {
            this.targetX = target.getX();
            this.targetY = target.getBoundingBox().minY;
            this.targetZ = target.getZ();
            this.delayCounter = 4 + this.attacker.getRandom().nextInt(7);
            if (this.canPenalize) {
               this.delayCounter = this.delayCounter + this.failedPathFindingPenalty;
               if (this.attacker.getNavigation().getPath() != null) {
                  Node finalPathPoint = this.attacker.getNavigation().getPath().getEndNode();
                  if (finalPathPoint != null && target.distanceToSqr(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1.0) {
                     this.failedPathFindingPenalty = 0;
                  } else {
                     this.failedPathFindingPenalty += 10;
                  }
               } else {
                  this.failedPathFindingPenalty += 10;
               }
            }
            if (d0 > 1024.0) {
               this.delayCounter += 10;
            } else if (d0 > 256.0) {
               this.delayCounter += 5;
            }
            if (!this.attacker.getNavigation().moveTo(target, this.speedTowardsTarget)) {
               this.delayCounter += 15;
            }
         }
         this.attackTick = Math.max(this.attackTick - 1, 0);
         this.checkAndPerformAttack(target, d0);
      }
   }

   protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
      double d0 = this.getAttackReachSqr(enemy);
      if (distToEnemySqr <= d0) {
         this.attackTick = 20;
         this.attacker.swing(InteractionHand.MAIN_HAND);
         this.attacker.doHurtTarget(enemy);
      }
   }

   protected double getAttackReachSqr(LivingEntity attackTarget) {
      return (double)(this.attacker.getBbWidth() * 4.0F * this.attacker.getBbWidth() * 4.0F + attackTarget.getBbWidth());
   }
}
