package greymerk.roguelike.util;

import java.util.Random;

/** rlgen: Roguelike used Math.random() and new Random() in a few places; one seeded random per dungeon keeps rebuilds identical. */
public final class RldRandom {
   private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(() -> new Random(0L));

   private RldRandom() {
   }

   public static void seed(long seed) {
      RANDOM.get().setSeed(seed);
   }

   public static Random get() {
      return RANDOM.get();
   }

   public static double random() {
      return RANDOM.get().nextDouble();
   }
}
