package com.github.alexthe666.iceandfire.entity.ai.legacy;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/** 1.12.2 net.minecraft.entity.ai.EntityAITasks: tasks are (re)considered every 3rd tick, running ones are checked every tick. */
public class EntityAITasks {
   private final Set<EntityAITaskEntry> taskEntries = new LinkedHashSet<>();
   private final Set<EntityAITaskEntry> executingTaskEntries = new LinkedHashSet<>();
   private int tickCount;
   private final int tickRate = 3;
   private int disabledControlFlags;

   public void addTask(int priority, EntityAIBase task) {
      this.taskEntries.add(new EntityAITaskEntry(priority, task));
   }

   public void removeTask(EntityAIBase task) {
      Iterator<EntityAITaskEntry> iterator = this.taskEntries.iterator();
      while (iterator.hasNext()) {
         EntityAITaskEntry entry = iterator.next();
         if (entry.action == task) {
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
         for (EntityAITaskEntry entry : this.taskEntries) {
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
         Iterator<EntityAITaskEntry> iterator = this.executingTaskEntries.iterator();
         while (iterator.hasNext()) {
            EntityAITaskEntry entry = iterator.next();
            if (!this.canContinue(entry)) {
               entry.using = false;
               entry.action.resetTask();
               iterator.remove();
            }
         }
      }

      if (!this.executingTaskEntries.isEmpty()) {
         for (EntityAITaskEntry entry : this.executingTaskEntries) {
            entry.action.updateTask();
         }
      }
   }

   private boolean canContinue(EntityAITaskEntry entry) {
      return entry.action.shouldContinueExecuting();
   }

   private boolean canUse(EntityAITaskEntry entry) {
      if (this.executingTaskEntries.isEmpty()) {
         return true;
      } else if (this.isControlFlagDisabled(entry.action.getMutexBits())) {
         return false;
      } else {
         for (EntityAITaskEntry other : this.executingTaskEntries) {
            if (other != entry) {
               if (entry.priority >= other.priority) {
                  if (!areTasksCompatible(entry, other)) {
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

   private static boolean areTasksCompatible(EntityAITaskEntry a, EntityAITaskEntry b) {
      return (a.action.getMutexBits() & b.action.getMutexBits()) == 0;
   }

   public boolean isControlFlagDisabled(int flags) {
      return (this.disabledControlFlags & flags) > 0;
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

   public boolean isExecuting(EntityAIBase task) {
      for (EntityAITaskEntry entry : this.executingTaskEntries) {
         if (entry.action == task) {
            return true;
         }
      }
      return false;
   }

   static final class EntityAITaskEntry {
      final EntityAIBase action;
      final int priority;
      boolean using;

      EntityAITaskEntry(int priority, EntityAIBase action) {
         this.priority = priority;
         this.action = action;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }
         return o != null && this.getClass() == o.getClass() && this.action.equals(((EntityAITaskEntry)o).action);
      }

      @Override
      public int hashCode() {
         return this.action.hashCode();
      }
   }
}
