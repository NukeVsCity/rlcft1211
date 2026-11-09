package com.github.alexthe666.iceandfire.entity.ai.legacy;

/**
 * 1.12.2 net.minecraft.entity.ai.EntityAIBase. The 1.12 Ice and Fire mobs run on the 1.12 task scheduler
 * ({@link EntityAITasks}) so goal priorities, mutex bits, the 3-tick evaluation rate and every random call stay as in 1.12.
 * Mutex bits: 1 = move, 2 = look, 4 = jump.
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
