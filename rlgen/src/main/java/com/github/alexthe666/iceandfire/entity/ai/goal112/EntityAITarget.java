package com.github.alexthe666.iceandfire.entity.ai.goal112;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.scores.Team;

/** 1.12.2 net.minecraft.entity.ai.EntityAITarget (the 1.21 TargetGoal changed reach/sight rules). */
public abstract class EntityAITarget extends Goal {
   protected final PathfinderMob taskOwner;
   protected boolean shouldCheckSight;
   private final boolean nearbyOnly;
   private int targetSearchStatus;
   private int targetSearchDelay;
   private int targetUnseenTicks;
   protected LivingEntity target;
   protected int unseenMemoryTicks = 60;

   public EntityAITarget(PathfinderMob owner, boolean checkSight) {
      this(owner, checkSight, false);
   }

   public EntityAITarget(PathfinderMob owner, boolean checkSight, boolean onlyNearby) {
      this.taskOwner = owner;
      this.shouldCheckSight = checkSight;
      this.nearbyOnly = onlyNearby;
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
   }

   @Override
   public boolean canContinueToUse() {
      LivingEntity target = this.taskOwner.getTarget();
      if (target == null) {
         target = this.target;
      }

      if (target == null) {
         return false;
      } else if (!target.isAlive()) {
         return false;
      } else {
         Team team = this.taskOwner.getTeam();
         Team team1 = target.getTeam();
         if (team != null && team1 == team) {
            return false;
         } else {
            double d = this.getTargetDistance();
            if (this.taskOwner.distanceToSqr(target) > d * d) {
               return false;
            } else {
               if (this.shouldCheckSight) {
                  if (this.taskOwner.getSensing().hasLineOfSight(target)) {
                     this.targetUnseenTicks = 0;
                  } else if (++this.targetUnseenTicks > this.unseenMemoryTicks) {
                     return false;
                  }
               }

               if (target instanceof Player && ((Player)target).getAbilities().invulnerable) {
                  return false;
               } else {
                  this.taskOwner.setTarget(target);
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
   public void start() {
      this.targetSearchStatus = 0;
      this.targetSearchDelay = 0;
      this.targetUnseenTicks = 0;
   }

   @Override
   public void stop() {
      this.taskOwner.setTarget(null);
      this.target = null;
   }

   public static boolean isSuitableTarget(Mob attacker, @Nullable LivingEntity target, boolean includeInvincibles, boolean checkSight) {
      if (target == null) {
         return false;
      } else if (target == attacker) {
         return false;
      } else if (!target.isAlive()) {
         return false;
      } else if (!attacker.canAttackType(target.getType())) {
         return false;
      } else if (attacker.isAlliedTo(target)) {
         return false;
      } else {
         if (attacker instanceof OwnableEntity && ((OwnableEntity)attacker).getOwnerUUID() != null) {
            if (target instanceof OwnableEntity && ((OwnableEntity)attacker).getOwnerUUID().equals(((OwnableEntity)target).getOwnerUUID())) {
               return false;
            }

            if (target == ((OwnableEntity)attacker).getOwner()) {
               return false;
            }
         } else if (target instanceof Player && !includeInvincibles && ((Player)target).getAbilities().invulnerable) {
            return false;
         }

         return !checkSight || attacker.getSensing().hasLineOfSight(target);
      }
   }

   protected boolean isSuitableTarget(@Nullable LivingEntity target, boolean includeInvincibles) {
      if (!isSuitableTarget(this.taskOwner, target, includeInvincibles, this.shouldCheckSight)) {
         return false;
      } else if (!this.taskOwner.isWithinRestriction(BlockPos.containing(target.getX(), target.getY(), target.getZ()))) {
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
