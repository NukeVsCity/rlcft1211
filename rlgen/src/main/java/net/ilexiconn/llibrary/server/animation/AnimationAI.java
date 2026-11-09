package net.ilexiconn.llibrary.server.animation;

import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

/** LLibrary 1.7.20 AnimationAI (EntityAIBase -> Goal; mutex bits 7 = MOVE | LOOK | JUMP; ticks every tick like 1.12). */
public abstract class AnimationAI<T extends Entity & IAnimatedEntity> extends Goal {
   protected T entity;

   public AnimationAI(T entity) {
      this.entity = entity;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
   }

   public abstract Animation getAnimation();

   public boolean isAutomatic() {
      return false;
   }

   public boolean shouldAnimate() {
      return false;
   }

   public boolean shouldExecute() {
      return this.isAutomatic() ? this.entity.getAnimation() == this.getAnimation() : this.shouldAnimate();
   }

   public void startExecuting() {
      if (!this.isAutomatic()) {
         AnimationHandler.INSTANCE.sendAnimationMessage(this.entity, this.getAnimation());
      }
      this.entity.setAnimationTick(0);
   }

   public boolean shouldContinueExecuting() {
      return this.entity.getAnimationTick() < this.getAnimation().getDuration();
   }

   public void resetTask() {
      AnimationHandler.INSTANCE.sendAnimationMessage(this.entity, IAnimatedEntity.NO_ANIMATION);
   }

   public void updateTask() {
   }

   @Override
   public boolean canUse() {
      return this.shouldExecute();
   }

   @Override
   public void start() {
      this.startExecuting();
   }

   @Override
   public boolean canContinueToUse() {
      return this.shouldContinueExecuting();
   }

   @Override
   public void stop() {
      this.resetTask();
   }

   @Override
   public void tick() {
      this.updateTask();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }
}
