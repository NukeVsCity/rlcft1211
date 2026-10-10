package com.nukevscity.rlgen.legacy;

/** The parts of 1.12.2 net.minecraft.util.math.MathHelper that world generation uses, copied as-is. */
public final class MathHelper {
   private static final float[] SIN_TABLE = new float[65536];

   static {
      for (int i = 0; i < 65536; i++) {
         SIN_TABLE[i] = (float)Math.sin((double)i * Math.PI * 2.0 / 65536.0);
      }
   }

   private MathHelper() {
   }

   public static float sin(float f) {
      return SIN_TABLE[(int)(f * 10430.378F) & 65535];
   }

   public static float cos(float f) {
      return SIN_TABLE[(int)(f * 10430.378F + 16384.0F) & 65535];
   }

   public static float sqrt(float f) {
      return (float)Math.sqrt((double)f);
   }

   public static float sqrt(double d0) {
      return (float)Math.sqrt(d0);
   }

   public static int floor(float f) {
      int i = (int)f;
      return f < (float)i ? i - 1 : i;
   }

   public static int floor(double d0) {
      int i = (int)d0;
      return d0 < (double)i ? i - 1 : i;
   }

   public static long lfloor(double d0) {
      long i = (long)d0;
      return d0 < (double)i ? i - 1L : i;
   }

   public static double clampedLerp(double d0, double d1, double d2) {
      if (d2 < 0.0) {
         return d0;
      } else {
         return d2 > 1.0 ? d1 : d0 + (d1 - d0) * d2;
      }
   }

   public static float clamp(float f, float min, float max) {
      if (f < min) {
         return min;
      } else {
         return f > max ? max : f;
      }
   }
}
