package com.github.alexthe666.iceandfire.entity.ai.legacy;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.AABB;

/** 1.12.2 EntityAIHurtByTarget. */
public class EntityAIHurtByTarget extends EntityAITarget {
   private final boolean entityCallsForHelp;
   private int revengeTimerOld;
   private final Class<?>[] excludedReinforcementTypes;

   public EntityAIHurtByTarget(PathfinderMob owner, boolean callsForHelp, Class<?>... excluded) {
      super(owner, true);
      this.entityCallsForHelp = callsForHelp;
      this.excludedReinforcementTypes = excluded;
   }

   @Override
   public boolean canUse() {
      int i = this.taskOwner.getLastHurtByMobTimestamp();
      LivingEntity attacker = this.taskOwner.getLastHurtByMob();
      return i != this.revengeTimerOld && attacker != null && this.isSuitableTarget(attacker, false);
   }

   @Override
   public void start() {
      this.taskOwner.setTarget(this.taskOwner.getLastHurtByMob());
      this.target = this.taskOwner.getTarget();
      this.revengeTimerOld = this.taskOwner.getLastHurtByMobTimestamp();
      this.unseenMemoryTicks = 300;
      if (this.entityCallsForHelp) {
         this.alertOthers();
      }

      super.start();
   }

   protected void alertOthers() {
      double d = this.getTargetDistance();

      for (PathfinderMob other : this.taskOwner
         .level()
         .getEntitiesOfClass(
            this.taskOwner.getClass(),
            new AABB(
                  this.taskOwner.getX(), this.taskOwner.getY(), this.taskOwner.getZ(), this.taskOwner.getX() + 1.0, this.taskOwner.getY() + 1.0, this.taskOwner.getZ() + 1.0
               )
               .inflate(d, 10.0, d)
         )) {
         if (this.taskOwner != other
            && other.getTarget() == null
            && (!(this.taskOwner instanceof TamableAnimal) || ((TamableAnimal)this.taskOwner).getOwner() == ((TamableAnimal)other).getOwner())
            && !other.isAlliedTo(this.taskOwner.getLastHurtByMob())) {
            boolean excluded = false;

            for (Class<?> cls : this.excludedReinforcementTypes) {
               if (other.getClass() == cls) {
                  excluded = true;
                  break;
               }
            }

            if (!excluded) {
               this.setEntityAttackTarget(other, this.taskOwner.getLastHurtByMob());
            }
         }
      }
   }

   protected void setEntityAttackTarget(PathfinderMob mob, LivingEntity target) {
      mob.setTarget(target);
   }
}
