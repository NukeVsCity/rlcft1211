package net.ilexiconn.llibrary.client.util;

/** LLibrary 1.7.20 ClientUtils (the player skin helpers are not ported: Ice and Fire does not use them). Pure maths, safe on both sides. */
public class ClientUtils {
   private static long lastUpdate = System.currentTimeMillis();

   public static void updateLast() {
      lastUpdate = System.currentTimeMillis();
   }

   public static float updateValue(float current, float target) {
      return updateValue(current, target, 0.5F);
   }

   public static float updateValue(float current, float target, float factor) {
      float times = (float)(System.currentTimeMillis() - lastUpdate) / 16.666666F;
      float off = target - current;
      off = !(off > 0.01F) && !(off < -0.01F) ? 0.0F : off * (float)Math.pow((double)factor, (double)times);
      return target - off;
   }

   public static float interpolate(float prev, float current, float partialTicks) {
      return prev + partialTicks * (current - prev);
   }

   public static float interpolateRotation(float prev, float current, float partialTicks) {
      float shortest = ((current - prev) % 360.0F + 540.0F) % 360.0F - 180.0F;
      return prev + shortest * partialTicks;
   }
}
