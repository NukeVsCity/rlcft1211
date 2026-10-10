package jaredbgreat.dldungeons.themes;

import java.util.Random;

public class SizeElement implements Autoselecting {
   private Sizes tiny = Sizes.TINY;
   private Sizes small;
   private Sizes medium;
   private Sizes large;
   private Sizes huge;
   private int prob1;
   private int prob2;
   private int prob3;
   private int prob4;
   private int prob5;
   private int probScale;

   public SizeElement(int prob1, int prob2, int prob3, int prob4, int prob5) {
      this.prob1 = prob1;
      this.small = Sizes.SMALL;
      this.prob2 = prob2;
      this.medium = Sizes.MEDIUM;
      this.prob3 = prob3;
      this.large = Sizes.LARGE;
      this.prob4 = prob4;
      this.huge = Sizes.HUGE;
      this.prob5 = prob5;
      this.probScale = prob1 + prob2 + prob3 + prob4 + prob5;
   }

   public Sizes select(Random random) {
      int roll = random.nextInt(this.probScale);
      if (roll < this.prob1) {
         return Sizes.TINY;
      } else {
         roll -= this.prob1;
         if (roll < this.prob2) {
            return Sizes.SMALL;
         } else {
            roll -= this.prob2;
            if (roll < this.prob3) {
               return Sizes.MEDIUM;
            } else {
               roll -= this.prob3;
               if (roll < this.prob4) {
                  return Sizes.LARGE;
               } else {
                  roll -= this.prob4;
                  return roll < this.prob5 ? Sizes.HUGE : Sizes.MEDIUM;
               }
            }
         }
      }
   }
}
