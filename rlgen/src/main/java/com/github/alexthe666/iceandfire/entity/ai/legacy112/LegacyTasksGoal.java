package com.github.alexthe666.iceandfire.entity.ai.legacy112;

import java.util.EnumSet;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.vehicle.Boat;

/**
 * Runs a mob's 1.12 task lists from its 1.21 goal selector, every tick, at the point where 1.12 EntityLiving.updateEntityActionState
 * ran them (targetTasks, then tasks, then the navigator). Also does 1.12 EntityLiving.updateMovementGoalFlags every 5 ticks.
 */
public class LegacyTasksGoal extends Goal {
   private final Mob mob;
   private final EntityAITasks targetTasks;
   private final EntityAITasks tasks;

   public LegacyTasksGoal(Mob mob, EntityAITasks targetTasks, EntityAITasks tasks) {
      this.mob = mob;
      this.targetTasks = targetTasks;
      this.tasks = tasks;
      this.setFlags(EnumSet.noneOf(Goal.Flag.class));
   }

   @Override
   public boolean canUse() {
      return true;
   }

   @Override
   public boolean canContinueToUse() {
      return true;
   }

   @Override
   public boolean isInterruptable() {
      return false;
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      if (this.mob.tickCount % 5 == 0) {
         boolean notMobRidden = !(this.mob.getControllingPassenger() instanceof Mob);
         boolean notInBoat = !(this.mob.getVehicle() instanceof Boat);
         this.tasks.setControlFlag(1, notMobRidden);
         this.tasks.setControlFlag(4, notMobRidden && notInBoat);
         this.tasks.setControlFlag(2, notMobRidden);
      }
      this.targetTasks.onUpdateTasks();
      this.tasks.onUpdateTasks();
   }
}
