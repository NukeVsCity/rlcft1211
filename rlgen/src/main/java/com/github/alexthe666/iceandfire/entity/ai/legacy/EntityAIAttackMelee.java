package com.github.alexthe666.iceandfire.entity.ai.legacy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

/** 1.12.2 net.minecraft.entity.ai.EntityAIAttackMelee. attackEntityAsMob is the mob's doHurtTarget. */
public class EntityAIAttackMelee extends EntityAIBase {
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

   public EntityAIAttackMelee(PathfinderMob creature, double speedIn, boolean useLongMemory) {
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
         return !this.attacker.isWithinRestriction(BlockPos.containing(target.position()))
            ? false
            : !(target instanceof Player) || !((Player)target).isSpectator() && !((Player)target).isCreative();
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
      if (target instanceof Player && (((Player)target).isSpectator() || ((Player)target).isCreative())) {
         this.attacker.setTarget(null);
      }

      this.attacker.getNavigation().stop();
   }

   @Override
   public void updateTask() {
      LivingEntity target = this.attacker.getTarget();
      if (target == null) {
         return; // 1.12 threw here; a target cleared by onLivingUpdate between the checks is skipped instead
      }
      this.attacker.getLookControl().setLookAt(target, 30.0F, 30.0F);
      double d0 = this.attacker.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
      this.delayCounter--;
      if ((this.longMemory || this.attacker.getSensing().hasLineOfSight(target))
         && this.delayCounter <= 0
         && (
            this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0
               || target.distanceToSqr(this.targetX, this.targetY, this.targetZ) >= 1.0
               || this.attacker.getRandom().nextFloat() < 0.05F
         )) {
         this.targetX = target.getX();
         this.targetY = target.getBoundingBox().minY;
         this.targetZ = target.getZ();
         this.delayCounter = 4 + this.attacker.getRandom().nextInt(7);
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

   protected void checkAndPerformAttack(LivingEntity target, double distSq) {
      double d0 = this.getAttackReachSqr(target);
      if (distSq <= d0 && this.attackTick <= 0) {
         this.attackTick = 20;
         this.attacker.swing(InteractionHand.MAIN_HAND);
         this.attacker.doHurtTarget(target);
      }
   }

   protected double getAttackReachSqr(LivingEntity target) {
      return (double)(this.attacker.getBbWidth() * 2.0F * this.attacker.getBbWidth() * 2.0F + target.getBbWidth());
   }
}
