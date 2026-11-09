package com.github.alexthe666.iceandfire.entity.ai.legacy;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;

/**
 * 1.12.2 net.minecraft.entity.ai.EntityAITasks (tickRate 3, LinkedHashSet order). It is driven by one flagless 1.21 goal
 * per selector ({@link #install}), so 1.21's Mob.serverAiStep calls the target tasks, then the tasks, every tick before the
 * navigation, move, look and jump controls, like 1.12 EntityLiving.updateEntityActionState.
 */
public class EntityAITasks {
   private final Set<EntityAITasks.EntityAITaskEntry> taskEntries = Sets.newLinkedHashSet();
   private final Set<EntityAITasks.EntityAITaskEntry> executingTaskEntries = Sets.newLinkedHashSet();
   private int tickCount;
   private int tickRate = 3;
   private int disabledControlFlags;

   public void addTask(int priority, EntityAIBase task) {
      this.taskEntries.add(new EntityAITasks.EntityAITaskEntry(priority, task));
   }

   public void removeTask(EntityAIBase task) {
      Iterator<EntityAITasks.EntityAITaskEntry> iterator = this.taskEntries.iterator();

      while (iterator.hasNext()) {
         EntityAITasks.EntityAITaskEntry entry = iterator.next();
         EntityAIBase action = entry.action;
         if (action == task) {
            if (entry.using) {
               entry.using = false;
               entry.action.resetTask();
               this.executingTaskEntries.remove(entry);
            }

            iterator.remove();
            return;
         }
      }
   }

   public void onUpdateTasks() {
      if (this.tickCount++ % this.tickRate == 0) {
         for (EntityAITasks.EntityAITaskEntry entry : this.taskEntries) {
            if (entry.using) {
               if (!this.canUse(entry) || !this.canContinue(entry)) {
                  entry.using = false;
                  entry.action.resetTask();
                  this.executingTaskEntries.remove(entry);
               }
            } else if (this.canUse(entry) && entry.action.shouldExecute()) {
               entry.using = true;
               entry.action.startExecuting();
               this.executingTaskEntries.add(entry);
            }
         }
      } else {
         Iterator<EntityAITasks.EntityAITaskEntry> iterator = this.executingTaskEntries.iterator();

         while (iterator.hasNext()) {
            EntityAITasks.EntityAITaskEntry entry = iterator.next();
            if (!this.canContinue(entry)) {
               entry.using = false;
               entry.action.resetTask();
               iterator.remove();
            }
         }
      }

      if (!this.executingTaskEntries.isEmpty()) {
         for (EntityAITasks.EntityAITaskEntry entry : this.executingTaskEntries) {
            entry.action.updateTask();
         }
      }
   }

   public boolean isExecuting(EntityAIBase task) {
      for (EntityAITasks.EntityAITaskEntry entry : this.executingTaskEntries) {
         if (entry.action == task) {
            return true;
         }
      }
      return false;
   }

   private boolean canContinue(EntityAITasks.EntityAITaskEntry entry) {
      return entry.action.shouldContinueExecuting();
   }

   private boolean canUse(EntityAITasks.EntityAITaskEntry entry) {
      if (this.executingTaskEntries.isEmpty()) {
         return true;
      } else if (this.isControlFlagDisabled(entry.action.getMutexBits())) {
         return false;
      } else {
         for (EntityAITasks.EntityAITaskEntry other : this.executingTaskEntries) {
            if (other != entry) {
               if (entry.priority >= other.priority) {
                  if (!this.areTasksCompatible(entry, other)) {
                     return false;
                  }
               } else if (!other.action.isInterruptible()) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   private boolean areTasksCompatible(EntityAITasks.EntityAITaskEntry a, EntityAITasks.EntityAITaskEntry b) {
      return (a.action.getMutexBits() & b.action.getMutexBits()) == 0;
   }

   public boolean isControlFlagDisabled(int flag) {
      return (this.disabledControlFlags & flag) > 0;
   }

   public void disableControlFlag(int flag) {
      this.disabledControlFlags |= flag;
   }

   public void enableControlFlag(int flag) {
      this.disabledControlFlags &= ~flag;
   }

   public void setControlFlag(int flag, boolean enabled) {
      if (enabled) {
         this.enableControlFlag(flag);
      } else {
         this.disableControlFlag(flag);
      }
   }

   /** Runs {@code targetTasks} from the 1.21 target selector and {@code tasks} from the goal selector of {@code mob}. */
   public static void install(Mob mob, GoalSelector goalSelector, EntityAITasks tasks, GoalSelector targetSelector, EntityAITasks targetTasks) {
      targetSelector.addGoal(0, new Driver(mob, targetTasks));
      goalSelector.addGoal(0, new Driver(mob, tasks));
   }

   class EntityAITaskEntry {
      public final EntityAIBase action;
      public final int priority;
      public boolean using;

      public EntityAITaskEntry(int priority, EntityAIBase task) {
         this.priority = priority;
         this.action = task;
      }

      @Override
      public boolean equals(Object other) {
         if (this == other) {
            return true;
         } else {
            return other != null && this.getClass() == other.getClass() && this.action.equals(((EntityAITasks.EntityAITaskEntry)other).action);
         }
      }

      @Override
      public int hashCode() {
         return this.action.hashCode();
      }
   }

   /** Flagless 1.21 goal that ticks a 1.12 task list once per server tick while the mob's AI runs. */
   private static final class Driver extends Goal {
      private final Mob mob;
      private final EntityAITasks tasks;

      Driver(Mob mob, EntityAITasks tasks) {
         this.mob = mob;
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
         this.tasks.onUpdateTasks();
      }
   }
}
