package com.github.alexthe666.iceandfire.entity.ai.legacy;

/**
 * 1.12.2 net.minecraft.entity.ai.EntityAIBase. The 1.12 Ice and Fire mobs keep their 1.12 task lists (priorities, mutex bits, the
 * 1.12 scheduling of every 3 ticks) through {@link EntityAITasks}; mutex bits are 1 = move, 2 = look, 4 = jump/swim.
 */
public abstract class EntityAIBase {
   private int mutexBits;

   public abstract boolean shouldExecute();

   public boolean shouldContinueExecuting() {
      return this.shouldExecute();
   }

   public boolean isInterruptible() {
      return true;
   }

   public void startExecuting() {
   }

   public void resetTask() {
   }

   public void updateTask() {
   }

   public void setMutexBits(int mutexBits) {
      this.mutexBits = mutexBits;
   }

   public int getMutexBits() {
      return this.mutexBits;
   }
}
