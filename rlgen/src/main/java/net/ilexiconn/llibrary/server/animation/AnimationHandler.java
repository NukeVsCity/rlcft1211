package net.ilexiconn.llibrary.server.animation;

import net.minecraft.world.entity.Entity;

// STUB(iaf/llibrary): no client sync yet (the llibrary branch sends AnimationMessage to tracking players).
public enum AnimationHandler {
   INSTANCE;

   public <T extends Entity & IAnimatedEntity> void sendAnimationMessage(T entity, Animation animation) {
      if (!entity.level().isClientSide) {
         entity.setAnimation(animation);
      }
   }

   public <T extends Entity & IAnimatedEntity> void updateAnimations(T entity) {
      if (entity.getAnimation() == null) {
         entity.setAnimation(IAnimatedEntity.NO_ANIMATION);
      } else if (entity.getAnimation() != IAnimatedEntity.NO_ANIMATION) {
         if (entity.getAnimationTick() == 0) {
            this.sendAnimationMessage(entity, entity.getAnimation());
         }
         if (entity.getAnimationTick() < entity.getAnimation().getDuration()) {
            entity.setAnimationTick(entity.getAnimationTick() + 1);
         }
         if (entity.getAnimationTick() == entity.getAnimation().getDuration()) {
            if (!entity.getAnimation().doesLoop()) {
               entity.setAnimation(IAnimatedEntity.NO_ANIMATION);
            }
            entity.setAnimationTick(0);
         }
      }
   }
}
