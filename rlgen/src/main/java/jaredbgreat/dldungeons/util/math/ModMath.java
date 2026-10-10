package jaredbgreat.dldungeons.util.math;

public class ModMath {
   public static int modRight(int a, int b) {
      return (a & 2147483647) % b;
   }

   public static float asymptote(float n, float start, float rate) {
      return n > start ? start + rate / (n - start + 1.0F) : n;
   }

   public static double asymptote(double n, double start, double rate) {
      return n > start ? start + rate / (n - start + 1.0) : n;
   }
}
