package net.ilexiconn.llibrary.server.animation;

import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.server.event.AnimationEvent;
import net.ilexiconn.llibrary.server.network.AnimationMessage;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.ArrayUtils;

/** LLibrary 1.7.20 AnimationHandler: call updateAnimations(entity) every tick on both sides (IaF does it in onLivingUpdate). */
public enum AnimationHandler {
   INSTANCE;

   /** Server only: sets the animation and sends it (index into getAnimations(), -1 = NO_ANIMATION) to every player tracking the entity. */
   public <T extends Entity & IAnimatedEntity> void sendAnimationMessage(T entity, Animation animation) {
      if (!entity.level().isClientSide) {
         entity.setAnimation(animation);
         LLibrary.NETWORK_WRAPPER.sendToAllTracking(new AnimationMessage(entity.getId(), ArrayUtils.indexOf(entity.getAnimations(), animation)), entity);
      }
   }

   public <T extends Entity & IAnimatedEntity> void updateAnimations(T entity) {
      if (entity.getAnimation() == null) {
         entity.setAnimation(IAnimatedEntity.NO_ANIMATION);
      } else if (entity.getAnimation() != IAnimatedEntity.NO_ANIMATION) {
         if (entity.getAnimationTick() == 0) {
            AnimationEvent.Start<T> event = new AnimationEvent.Start<>(entity, entity.getAnimation());
            if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {
               this.sendAnimationMessage(entity, event.getAnimation());
            }
         }

         if (entity.getAnimationTick() < entity.getAnimation().getDuration()) {
            entity.setAnimationTick(entity.getAnimationTick() + 1);
            NeoForge.EVENT_BUS.post(new AnimationEvent.Tick<>(entity, entity.getAnimation(), entity.getAnimationTick()));
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
