package com.github.fnar.util;

import java.util.Random;

public class Color {
   public static final int BOUND = 256;
   public static final Color PRUSSIAN_BLUE = new Color(0, 51, 102);
   public static final Color LEAD = new Color(32, 32, 32);
   public static final Color DEEP_VIOLET = new Color(51, 0, 102);
   public static final Color HARISSA_RED = new Color(165, 42, 42);
   public static final Color PALE_LIME_GREEN = new Color(178, 255, 102);
   public static final Color CHINESE_RED = new Color(250, 96, 128);
   public static final Color SMOKED_SALMON = new Color(250, 128, 114);
   public static final Color PINK_FLAMINGO = new Color(255, 100, 255);
   public static final Color CLASSIC_ROSE = new Color(255, 204, 229);
   public static final Color POPCORN = new Color(250, 220, 140);
   public static final Color SPLIT_PEA_SOUP = new Color(200, 180, 100);
   public static final Color ICE_FISHING = new Color(222, 238, 244);
   public static final Color ADVERTISING_GREEN = new Color(85, 165, 120);
   public static final Color BLUE = new Color(0, 0, 255);
   public static final Color WHITE = new Color(255, 255, 255);
   public static final Color ENGLISH_BREAKFAST = new Color(62, 14, 16);
   public static final Color SUNNY_MOOD = new Color(245, 200, 71);
   public static final Color COFFEE = new Color(111, 78, 55);
   public static final Color DELAYED_YELLOW = new Color(250, 250, 0);
   private final int red;
   private final int green;
   private final int blue;

   public Color(int red, int green, int blue) {
      this.red = red;
      this.green = green;
      this.blue = blue;
   }

   public static Color random(Random random) {
      return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
   }

   public static Color HSLToColor(float h, float s, float l) {
      float r;
      float g;
      float b;
      if (s == 0.0F) {
         b = l;
         g = l;
         r = l;
      } else {
         float q = l < 0.5F ? l * (1.0F + s) : l + s - l * s;
         float p = 2.0F * l - q;
         r = hueToRgb(p, q, h + 0.33333334F);
         g = hueToRgb(p, q, h);
         b = hueToRgb(p, q, h - 0.33333334F);
      }

      return new Color((int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F));
   }

   private static float hueToRgb(float p, float q, float t) {
      if (t < 0.0F) {
         t++;
      }

      if (t > 1.0F) {
         t--;
      }

      if (t < 0.16666667F) {
         return p + (q - p) * 6.0F * t;
      } else if (t < 0.5F) {
         return q;
      } else {
         return t < 0.6666667F ? p + (q - p) * (0.6666667F - t) * 6.0F : p;
      }
   }

   public int asInt() {
      return this.red << 16 | this.green << 8 | this.blue;
   }
}
