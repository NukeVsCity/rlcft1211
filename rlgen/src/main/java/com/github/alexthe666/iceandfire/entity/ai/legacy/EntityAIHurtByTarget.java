package com.github.alexthe666.iceandfire.entity.ai.legacy;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.AABB;

/** 1.12.2 EntityAIHurtByTarget */
public class EntityAIHurtByTarget extends EntityAITarget {
   private final boolean entityCallsForHelp;
   private int revengeTimerOld;
   private final Class<?>[] excludedReinforcementTypes;

   public EntityAIHurtByTarget(PathfinderMob creature, boolean entityCallsForHelp, Class<?>... excludedReinforcementTypes) {
      super(creature, true);
      this.entityCallsForHelp = entityCallsForHelp;
      this.excludedReinforcementTypes = excludedReinforcementTypes;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      int i = this.taskOwner.getLastHurtByMobTimestamp();
      LivingEntity revenge = this.taskOwner.getLastHurtByMob();
      return i != this.revengeTimerOld && revenge != null && this.isSuitableTarget(revenge, false);
   }

   @Override
   public void startExecuting() {
      this.taskOwner.setTarget(this.taskOwner.getLastHurtByMob());
      this.target = this.taskOwner.getTarget();
      this.revengeTimerOld = this.taskOwner.getLastHurtByMobTimestamp();
      this.unseenMemoryTicks = 300;
      if (this.entityCallsForHelp) {
         this.alertOthers();
      }
      super.startExecuting();
   }

   protected void alertOthers() {
      double d0 = this.getTargetDistance();
      AABB box = new AABB(this.taskOwner.getX(), this.taskOwner.getY(), this.taskOwner.getZ(), this.taskOwner.getX() + 1.0,
         this.taskOwner.getY() + 1.0, this.taskOwner.getZ() + 1.0).inflate(d0, 10.0, d0);
      for (PathfinderMob other : this.taskOwner.level().getEntitiesOfClass(this.taskOwner.getClass(), box)) {
         if (this.taskOwner != other
            && other.getTarget() == null
            && (!(this.taskOwner instanceof TamableAnimal t) || t.getOwner() == ((TamableAnimal)other).getOwner())
            && !other.isAlliedTo(this.taskOwner.getLastHurtByMob())) {
            boolean excluded = false;
            for (Class<?> c : this.excludedReinforcementTypes) {
               if (other.getClass() == c) {
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

   protected void setEntityAttackTarget(PathfinderMob creature, LivingEntity target) {
      creature.setTarget(target);
   }
}
