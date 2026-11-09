package net.ilexiconn.llibrary.server.event;

import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** LLibrary 1.7.20 AnimationEvent, posted on NeoForge.EVENT_BUS. */
public class AnimationEvent<T extends Entity & IAnimatedEntity> extends Event {
   protected Animation animation;
   private final T entity;

   AnimationEvent(T entity, Animation animation) {
      this.entity = entity;
      this.animation = animation;
   }

   public T getEntity() {
      return this.entity;
   }

   public Animation getAnimation() {
      return this.animation;
   }

   public static class Start<T extends Entity & IAnimatedEntity> extends AnimationEvent<T> implements ICancellableEvent {
      public Start(T entity, Animation animation) {
         super(entity, animation);
      }

      public void setAnimation(Animation animation) {
         this.animation = animation;
      }
   }

   public static class Tick<T extends Entity & IAnimatedEntity> extends AnimationEvent<T> {
      protected int tick;

      public Tick(T entity, Animation animation, int tick) {
         super(entity, animation);
         this.tick = tick;
      }

      public int getTick() {
         return this.tick;
      }
   }
}
