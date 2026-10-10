package jaredbgreat.dldungeons.themes;

import java.util.Random;

public final class Element implements Autoselecting {
   private static final Degree NONE = Degree.NONE;
   private static final Degree FEW = Degree.FEW;
   private static final Degree SOME = Degree.SOME;
   private static final Degree PLENTY = Degree.PLENTY;
   private static final Degree HEAPS = Degree.HEAPS;
   private static final Degree ALWAYS = Degree.ALWAYS;
   private final int prob1;
   private final int prob2;
   private final int prob3;
   private final int prob4;
   private final int prob5;
   private final int prob6;
   private final int probScale;
   private final boolean never;

   public Element(int prob1, int prob2, int prob3, int prob4, int prob5, int prob6) {
      this.prob1 = prob1;
      this.prob2 = this.prob1 + prob2;
      this.prob3 = this.prob2 + prob3;
      this.prob4 = this.prob3 + prob4;
      this.prob5 = this.prob4 + prob5;
      this.prob6 = this.prob5 + prob6;
      this.probScale = this.prob6 + 1;
      this.never = this.prob6 == this.prob1;
   }

   public Degree select(Random random) {
      int roll = random.nextInt(this.probScale);
      if (roll < this.prob1) {
         return Degree.NONE;
      } else if (roll < this.prob2) {
         return Degree.FEW;
      } else if (roll < this.prob3) {
         return Degree.SOME;
      } else if (roll < this.prob4) {
         return Degree.PLENTY;
      } else if (roll < this.prob5) {
         return Degree.HEAPS;
      } else {
         return roll < this.prob6 ? Degree.ALWAYS : Degree.NONE;
      }
   }

   public boolean never() {
      return this.never;
   }
}
