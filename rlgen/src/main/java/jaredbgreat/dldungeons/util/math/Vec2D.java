package jaredbgreat.dldungeons.util.math;

import java.util.Random;

public class Vec2D {
   private static final double P2 = Math.PI * 2;
   double x;
   double y;

   public Vec2D(double x, double y) {
      this.x = x;
      this.y = y;
   }

   public Vec2D(Random rand) {
      this.x = rand.nextDouble() * 2.0 - 1.0;
      this.y = rand.nextDouble() * 2.0 - 1.0;
   }

   public Vec2D(SpatialHash random, int px, int py, int pz) {
      this.x = random.doubleFor(px, py, pz) * 2.0 - 1.0;
      this.y = random.doubleFor(px, py, pz + 1) * 2.0 - 1.0;
   }

   public static double dot(Vec2D a, Vec2D b) {
      return a.x * b.x + a.y * b.y;
   }
}
