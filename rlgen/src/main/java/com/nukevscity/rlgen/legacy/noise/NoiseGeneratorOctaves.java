package com.nukevscity.rlgen.legacy.noise;

import java.util.Random;
import com.nukevscity.rlgen.legacy.MathHelper;

public class NoiseGeneratorOctaves extends NoiseGenerator {
   private final NoiseGeneratorImproved[] generatorCollection;
   private final int octaves;

   public NoiseGeneratorOctaves(Random random, int i) {
      this.octaves = i;
      this.generatorCollection = new NoiseGeneratorImproved[i];

      for (int j = 0; j < i; j++) {
         this.generatorCollection[j] = new NoiseGeneratorImproved(random);
      }
   }

   public double[] generateNoiseOctaves(double[] adouble, int i, int j, int k, int l, int i1, int j1, double d0, double d1, double d2) {
      if (adouble == null) {
         adouble = new double[l * i1 * j1];
      } else {
         for (int k1 = 0; k1 < adouble.length; k1++) {
            adouble[k1] = 0.0;
         }
      }

      double d6 = 1.0;

      for (int l1 = 0; l1 < this.octaves; l1++) {
         double d3 = (double)i * d6 * d0;
         double d4 = (double)j * d6 * d1;
         double d5 = (double)k * d6 * d2;
         long i2 = MathHelper.lfloor(d3);
         long j2 = MathHelper.lfloor(d5);
         d3 -= (double)i2;
         d5 -= (double)j2;
         i2 %= 16777216L;
         j2 %= 16777216L;
         d3 += (double)i2;
         d5 += (double)j2;
         this.generatorCollection[l1].populateNoiseArray(adouble, d3, d4, d5, l, i1, j1, d0 * d6, d1 * d6, d2 * d6, d6);
         d6 /= 2.0;
      }

      return adouble;
   }

   public double[] generateNoiseOctaves(double[] adouble, int i, int j, int k, int l, double d0, double d1, double d2) {
      return this.generateNoiseOctaves(adouble, i, 10, j, k, 1, l, d0, 1.0, d1);
   }
}
