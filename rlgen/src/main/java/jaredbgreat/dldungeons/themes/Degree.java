package jaredbgreat.dldungeons.themes;

import java.util.Random;

public enum Degree implements Autoselectable {
   NONE(0),
   FEW(1),
   SOME(3),
   PLENTY(5),
   HEAPS(9),
   ALWAYS(10);

   public final int value;
   public static final int scale = 10;

   private Degree(int val) {
      this.value = val;
   }

   public boolean use(Random random) {
      return random.nextInt(10) < this.value;
   }
}
