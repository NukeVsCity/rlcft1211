package com.github.alexthe666.iceandfire.entity.ai.legacy;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.AABB;

/** 1.12.2 net.minecraft.entity.ai.EntityAIHurtByTarget. */
public class EntityAIHurtByTarget extends EntityAITarget {
   private final boolean entityCallsForHelp;
   private int revengeTimerOld;
   private final Class<?>[] excludedReinforcementTypes;

   public EntityAIHurtByTarget(PathfinderMob creature, boolean entityCallsForHelp, Class<?>... excluded) {
      super(creature, true);
      this.entityCallsForHelp = entityCallsForHelp;
      this.excludedReinforcementTypes = excluded;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      int i = this.taskOwner.getLastHurtByMobTimestamp();
      LivingEntity attacker = this.taskOwner.getLastHurtByMob();
      return i != this.revengeTimerOld && attacker != null && this.isSuitableTarget(attacker, false);
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

      for (PathfinderMob other : this.taskOwner
         .level()
         .getEntitiesOfClass(
            this.taskOwner.getClass(),
            new AABB(
                  this.taskOwner.getX(),
                  this.taskOwner.getY(),
                  this.taskOwner.getZ(),
                  this.taskOwner.getX() + 1.0,
                  this.taskOwner.getY() + 1.0,
                  this.taskOwner.getZ() + 1.0
               )
               .inflate(d0, 10.0, d0)
         )) {
         if (this.taskOwner != other
            && other.getTarget() == null
            && (!(this.taskOwner instanceof TamableAnimal) || ((TamableAnimal)this.taskOwner).getOwner() == ((TamableAnimal)other).getOwner())
            && !other.isAlliedTo(this.taskOwner.getLastHurtByMob())) {
            boolean flag = false;

            for (Class<?> oclass : this.excludedReinforcementTypes) {
               if (other.getClass() == oclass) {
                  flag = true;
                  break;
               }
            }

            if (!flag) {
               this.setEntityAttackTarget(other, this.taskOwner.getLastHurtByMob());
            }
         }
      }
   }

   protected void setEntityAttackTarget(PathfinderMob creature, LivingEntity target) {
      creature.setTarget(target);
   }
}
