package com.nukevscity.rlgen.legacy.noise;

import java.util.Random;

public class NoiseGeneratorSimplex {
   private static final int[][] grad3 = new int[][]{
      {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}
   };
   public static final double SQRT_3 = Math.sqrt(3.0);
   private final int[] p = new int[512];
   public double xo;
   public double yo;
   public double zo;
   private static final double F2 = 0.5 * (SQRT_3 - 1.0);
   private static final double G2 = (3.0 - SQRT_3) / 6.0;

   public NoiseGeneratorSimplex() {
      this(new Random());
   }

   public NoiseGeneratorSimplex(Random random) {
      this.xo = random.nextDouble() * 256.0;
      this.yo = random.nextDouble() * 256.0;
      this.zo = random.nextDouble() * 256.0;
      int i = 0;

      while (i < 256) {
         this.p[i] = i++;
      }

      for (int l = 0; l < 256; l++) {
         int j = random.nextInt(256 - l) + l;
         int k = this.p[l];
         this.p[l] = this.p[j];
         this.p[j] = k;
         this.p[l + 256] = this.p[l];
      }
   }

   private static int fastFloor(double d0) {
      return d0 > 0.0 ? (int)d0 : (int)d0 - 1;
   }

   private static double dot(int[] aint, double d0, double d1) {
      return (double)aint[0] * d0 + (double)aint[1] * d1;
   }

   public double getValue(double d0, double d1) {
      double d5 = 0.5 * (SQRT_3 - 1.0);
      double d6 = (d0 + d1) * d5;
      int i = fastFloor(d0 + d6);
      int j = fastFloor(d1 + d6);
      double d7 = (3.0 - SQRT_3) / 6.0;
      double d8 = (double)(i + j) * d7;
      double d9 = (double)i - d8;
      double d10 = (double)j - d8;
      double d11 = d0 - d9;
      double d12 = d1 - d10;
      int k;
      int l;
      if (d11 > d12) {
         k = 1;
         l = 0;
      } else {
         k = 0;
         l = 1;
      }

      double d13 = d11 - (double)k + d7;
      double d14 = d12 - (double)l + d7;
      double d15 = d11 - 1.0 + 2.0 * d7;
      double d16 = d12 - 1.0 + 2.0 * d7;
      int i1 = i & 0xFF;
      int j1 = j & 0xFF;
      int k1 = this.p[i1 + this.p[j1]] % 12;
      int l1 = this.p[i1 + k + this.p[j1 + l]] % 12;
      int i2 = this.p[i1 + 1 + this.p[j1 + 1]] % 12;
      double d17 = 0.5 - d11 * d11 - d12 * d12;
      double d2;
      if (d17 < 0.0) {
         d2 = 0.0;
      } else {
         d17 *= d17;
         d2 = d17 * d17 * dot(grad3[k1], d11, d12);
      }

      double d18 = 0.5 - d13 * d13 - d14 * d14;
      double d3;
      if (d18 < 0.0) {
         d3 = 0.0;
      } else {
         d18 *= d18;
         d3 = d18 * d18 * dot(grad3[l1], d13, d14);
      }

      double d19 = 0.5 - d15 * d15 - d16 * d16;
      double d4;
      if (d19 < 0.0) {
         d4 = 0.0;
      } else {
         d19 *= d19;
         d4 = d19 * d19 * dot(grad3[i2], d15, d16);
      }

      return 70.0 * (d2 + d3 + d4);
   }

   public void add(double[] adouble, double d0, double d1, int i, int j, double d2, double d3, double d4) {
      int k = 0;

      for (int l = 0; l < j; l++) {
         double d5 = (d1 + (double)l) * d3 + this.yo;

         for (int i1 = 0; i1 < i; i1++) {
            double d6 = (d0 + (double)i1) * d2 + this.xo;
            double d10 = (d6 + d5) * F2;
            int j1 = fastFloor(d6 + d10);
            int k1 = fastFloor(d5 + d10);
            double d11 = (double)(j1 + k1) * G2;
            double d12 = (double)j1 - d11;
            double d13 = (double)k1 - d11;
            double d14 = d6 - d12;
            double d15 = d5 - d13;
            int l1;
            int i2;
            if (d14 > d15) {
               l1 = 1;
               i2 = 0;
            } else {
               l1 = 0;
               i2 = 1;
            }

            double d16 = d14 - (double)l1 + G2;
            double d17 = d15 - (double)i2 + G2;
            double d18 = d14 - 1.0 + 2.0 * G2;
            double d19 = d15 - 1.0 + 2.0 * G2;
            int j2 = j1 & 0xFF;
            int k2 = k1 & 0xFF;
            int l2 = this.p[j2 + this.p[k2]] % 12;
            int i3 = this.p[j2 + l1 + this.p[k2 + i2]] % 12;
            int j3 = this.p[j2 + 1 + this.p[k2 + 1]] % 12;
            double d20 = 0.5 - d14 * d14 - d15 * d15;
            double d7;
            if (d20 < 0.0) {
               d7 = 0.0;
            } else {
               d20 *= d20;
               d7 = d20 * d20 * dot(grad3[l2], d14, d15);
            }

            double d21 = 0.5 - d16 * d16 - d17 * d17;
            double d8;
            if (d21 < 0.0) {
               d8 = 0.0;
            } else {
               d21 *= d21;
               d8 = d21 * d21 * dot(grad3[i3], d16, d17);
            }

            double d22 = 0.5 - d18 * d18 - d19 * d19;
            double d9;
            if (d22 < 0.0) {
               d9 = 0.0;
            } else {
               d22 *= d22;
               d9 = d22 * d22 * dot(grad3[j3], d18, d19);
            }

            adouble[k++] += 70.0 * (d7 + d8 + d9) * d4;
         }
      }
   }
}
