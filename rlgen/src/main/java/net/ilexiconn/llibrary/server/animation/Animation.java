package net.ilexiconn.llibrary.server.animation;

// STUB(iaf/llibrary): minimal copy of LLibrary 1.7.20 Animation until the llibrary branch is merged.
public class Animation {
   private final int duration;
   private boolean looping;

   protected Animation(int duration) {
      this.duration = duration;
   }

   public static Animation create(int duration) {
      return new Animation(duration);
   }

   public int getDuration() {
      return this.duration;
   }

   public Animation setLooping(boolean loops) {
      this.looping = loops;
      return this;
   }

   public boolean doesLoop() {
      return this.looping;
   }
}
