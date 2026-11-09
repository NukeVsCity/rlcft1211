package com.github.alexthe666.iceandfire.entity.ai.legacy112;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.scores.Team;

/** 1.12.2 EntityAITarget */
public abstract class EntityAITarget extends EntityAIBase {
   protected final PathfinderMob taskOwner;
   protected boolean shouldCheckSight;
   private final boolean nearbyOnly;
   private int targetSearchStatus;
   private int targetSearchDelay;
   private int targetUnseenTicks;
   protected LivingEntity target;
   protected int unseenMemoryTicks = 60;

   public EntityAITarget(PathfinderMob creature, boolean checkSight) {
      this(creature, checkSight, false);
   }

   public EntityAITarget(PathfinderMob creature, boolean checkSight, boolean onlyNearby) {
      this.taskOwner = creature;
      this.shouldCheckSight = checkSight;
      this.nearbyOnly = onlyNearby;
   }

   @Override
   public boolean shouldContinueExecuting() {
      LivingEntity living = this.taskOwner.getTarget();
      if (living == null) {
         living = this.target;
      }

      if (living == null) {
         return false;
      } else if (!living.isAlive()) {
         return false;
      } else {
         Team team = this.taskOwner.getTeam();
         Team team1 = living.getTeam();
         if (team != null && team1 == team) {
            return false;
         } else {
            double d0 = this.getTargetDistance();
            if (this.taskOwner.distanceToSqr(living) > d0 * d0) {
               return false;
            } else {
               if (this.shouldCheckSight) {
                  if (this.taskOwner.getSensing().hasLineOfSight(living)) {
                     this.targetUnseenTicks = 0;
                  } else if (++this.targetUnseenTicks > this.unseenMemoryTicks) {
                     return false;
                  }
               }

               if (living instanceof Player player && player.getAbilities().invulnerable) {
                  return false;
               } else {
                  this.taskOwner.setTarget(living);
                  return true;
               }
            }
         }
      }
   }

   protected double getTargetDistance() {
      AttributeInstance attr = this.taskOwner.getAttribute(Attributes.FOLLOW_RANGE);
      return attr == null ? 16.0 : attr.getValue();
   }

   @Override
   public void startExecuting() {
      this.targetSearchStatus = 0;
      this.targetSearchDelay = 0;
      this.targetUnseenTicks = 0;
   }

   @Override
   public void resetTask() {
      this.taskOwner.setTarget(null);
      this.target = null;
   }

   protected boolean isSuitableTarget(@Nullable LivingEntity target, boolean includeInvincibles) {
      if (!Legacy112.isSuitableTarget(this.taskOwner, target, includeInvincibles, this.shouldCheckSight)) {
         return false;
      } else if (!this.taskOwner.isWithinRestriction(target.blockPosition())) {
         return false;
      } else {
         if (this.nearbyOnly) {
            if (--this.targetSearchDelay <= 0) {
               this.targetSearchStatus = 0;
            }
            if (this.targetSearchStatus == 0) {
               this.targetSearchStatus = this.canEasilyReach(target) ? 1 : 2;
            }
            if (this.targetSearchStatus == 2) {
               return false;
            }
         }
         return true;
      }
   }

   private boolean canEasilyReach(LivingEntity target) {
      this.targetSearchDelay = 10 + this.taskOwner.getRandom().nextInt(5);
      Path path = this.taskOwner.getNavigation().createPath(target, 0);
      if (path == null) {
         return false;
      } else {
         Node node = path.getEndNode();
         if (node == null) {
            return false;
         } else {
            int i = node.x - Mth.floor(target.getX());
            int j = node.z - Mth.floor(target.getZ());
            return (double)(i * i + j * j) <= 2.25;
         }
      }
   }

   public EntityAITarget setUnseenMemoryTicks(int ticks) {
      this.unseenMemoryTicks = ticks;
      return this;
   }
}
