package com.nukevscity.rlgen.legacy.noise;

import java.util.Random;

public class NoiseGeneratorImproved extends NoiseGenerator {
   private final int[] permutations = new int[512];
   public double xCoord;
   public double yCoord;
   public double zCoord;
   private static final double[] GRAD_X = new double[]{1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0, 0.0};
   private static final double[] GRAD_Y = new double[]{1.0, 1.0, -1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0};
   private static final double[] GRAD_Z = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 0.0, 1.0, 0.0, -1.0};
   private static final double[] GRAD_2X = new double[]{1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0, 0.0};
   private static final double[] GRAD_2Z = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 0.0, 1.0, 0.0, -1.0};

   public NoiseGeneratorImproved() {
      this(new Random());
   }

   public NoiseGeneratorImproved(Random random) {
      this.xCoord = random.nextDouble() * 256.0;
      this.yCoord = random.nextDouble() * 256.0;
      this.zCoord = random.nextDouble() * 256.0;
      int i = 0;

      while (i < 256) {
         this.permutations[i] = i++;
      }

      for (int l = 0; l < 256; l++) {
         int j = random.nextInt(256 - l) + l;
         int k = this.permutations[l];
         this.permutations[l] = this.permutations[j];
         this.permutations[j] = k;
         this.permutations[l + 256] = this.permutations[l];
      }
   }

   public final double lerp(double d0, double d1, double d2) {
      return d1 + d0 * (d2 - d1);
   }

   public final double grad2(int i, double d0, double d1) {
      int j = i & 15;
      return GRAD_2X[j] * d0 + GRAD_2Z[j] * d1;
   }

   public final double grad(int i, double d0, double d1, double d2) {
      int j = i & 15;
      return GRAD_X[j] * d0 + GRAD_Y[j] * d1 + GRAD_Z[j] * d2;
   }

   public void populateNoiseArray(double[] adouble, double d0, double d1, double d2, int i, int j, int k, double d3, double d4, double d5, double d6) {
      if (j == 1) {
         int l5 = 0;
         int i6 = 0;
         int i1 = 0;
         int j6 = 0;
         double d21 = 0.0;
         double d22 = 0.0;
         int k6 = 0;
         double d23 = 1.0 / d6;

         for (int i3 = 0; i3 < i; i3++) {
            double d24 = d0 + (double)i3 * d3 + this.xCoord;
            int l6 = (int)d24;
            if (d24 < (double)l6) {
               l6--;
            }

            int j3 = l6 & 0xFF;
            d24 -= (double)l6;
            double d25 = d24 * d24 * d24 * (d24 * (d24 * 6.0 - 15.0) + 10.0);

            for (int i7 = 0; i7 < k; i7++) {
               double d26 = d2 + (double)i7 * d5 + this.zCoord;
               int j7 = (int)d26;
               if (d26 < (double)j7) {
                  j7--;
               }

               int k7 = j7 & 0xFF;
               d26 -= (double)j7;
               double d27 = d26 * d26 * d26 * (d26 * (d26 * 6.0 - 15.0) + 10.0);
               l5 = this.permutations[j3] + 0;
               i6 = this.permutations[l5] + k7;
               i1 = this.permutations[j3 + 1] + 0;
               j6 = this.permutations[i1] + k7;
               d21 = this.lerp(d25, this.grad2(this.permutations[i6], d24, d26), this.grad(this.permutations[j6], d24 - 1.0, 0.0, d26));
               d22 = this.lerp(d25, this.grad(this.permutations[i6 + 1], d24, 0.0, d26 - 1.0), this.grad(this.permutations[j6 + 1], d24 - 1.0, 0.0, d26 - 1.0));
               double d28 = this.lerp(d27, d21, d22);
               adouble[k6++] += d28 * d23;
            }
         }
      } else {
         int l = 0;
         double d7 = 1.0 / d6;
         int j1 = -1;
         int k1 = 0;
         int l1 = 0;
         int i2 = 0;
         int j2 = 0;
         int k2 = 0;
         int l2 = 0;
         double d8 = 0.0;
         double d9 = 0.0;
         double d10 = 0.0;
         double d11 = 0.0;

         for (int k3 = 0; k3 < i; k3++) {
            double d12 = d0 + (double)k3 * d3 + this.xCoord;
            int l3 = (int)d12;
            if (d12 < (double)l3) {
               l3--;
            }

            int i4 = l3 & 0xFF;
            d12 -= (double)l3;
            double d13 = d12 * d12 * d12 * (d12 * (d12 * 6.0 - 15.0) + 10.0);

            for (int j4 = 0; j4 < k; j4++) {
               double d14 = d2 + (double)j4 * d5 + this.zCoord;
               int k4 = (int)d14;
               if (d14 < (double)k4) {
                  k4--;
               }

               int l4 = k4 & 0xFF;
               d14 -= (double)k4;
               double d15 = d14 * d14 * d14 * (d14 * (d14 * 6.0 - 15.0) + 10.0);

               for (int i5 = 0; i5 < j; i5++) {
                  double d16 = d1 + (double)i5 * d4 + this.yCoord;
                  int j5 = (int)d16;
                  if (d16 < (double)j5) {
                     j5--;
                  }

                  int k5 = j5 & 0xFF;
                  d16 -= (double)j5;
                  double d17 = d16 * d16 * d16 * (d16 * (d16 * 6.0 - 15.0) + 10.0);
                  if (i5 == 0 || k5 != j1) {
                     j1 = k5;
                     k1 = this.permutations[i4] + k5;
                     l1 = this.permutations[k1] + l4;
                     i2 = this.permutations[k1 + 1] + l4;
                     j2 = this.permutations[i4 + 1] + k5;
                     k2 = this.permutations[j2] + l4;
                     l2 = this.permutations[j2 + 1] + l4;
                     d8 = this.lerp(d13, this.grad(this.permutations[l1], d12, d16, d14), this.grad(this.permutations[k2], d12 - 1.0, d16, d14));
                     d9 = this.lerp(d13, this.grad(this.permutations[i2], d12, d16 - 1.0, d14), this.grad(this.permutations[l2], d12 - 1.0, d16 - 1.0, d14));
                     d10 = this.lerp(
                        d13, this.grad(this.permutations[l1 + 1], d12, d16, d14 - 1.0), this.grad(this.permutations[k2 + 1], d12 - 1.0, d16, d14 - 1.0)
                     );
                     d11 = this.lerp(
                        d13,
                        this.grad(this.permutations[i2 + 1], d12, d16 - 1.0, d14 - 1.0),
                        this.grad(this.permutations[l2 + 1], d12 - 1.0, d16 - 1.0, d14 - 1.0)
                     );
                  }

                  double d18 = this.lerp(d17, d8, d9);
                  double d19 = this.lerp(d17, d10, d11);
                  double d20 = this.lerp(d15, d18, d19);
                  adouble[l++] += d20 * d7;
               }
            }
         }
      }
   }
}
