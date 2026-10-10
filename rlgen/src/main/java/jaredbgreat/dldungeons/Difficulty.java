package jaredbgreat.dldungeons;

import java.util.Random;

public enum Difficulty {
   NONE(0, 0, 0, 0, 0, -1, false, "No spawners."),
   BABY(3, 0, 0, 0, 0, 884, false, "Baby mode."),
   NOOB(4, 1, 1, 1, 1, 590, false, "Not too hard."),
   NORM(5, 2, 1, 1, 2, 427, false, "Normal difficulty."),
   HARD(6, 3, 2, 2, 3, 323, true, "Super violent."),
   NUTS(7, 5, 2, 2, 4, 100, true, "Insane horror!");

   public final int spawners;
   public final int promote;
   public final int maxlev;
   public final int nodelev;
   public final int bosslev;
   public final int blocksPerSpawner;
   public final boolean entrancemobs;
   public final String label;

   private Difficulty(int spawners, int promote, int mobmax, int nodelev, int bosslev, int blocksPerSpawner, boolean entrancemobs, String label) {
      this.spawners = spawners;
      this.promote = promote;
      this.maxlev = mobmax;
      this.nodelev = nodelev;
      this.bosslev = bosslev;
      this.blocksPerSpawner = blocksPerSpawner;
      this.entrancemobs = entrancemobs;
      this.label = label;
   }

   public boolean addmob(Random random) {
      return random.nextInt(10) < this.spawners;
   }

   public boolean multimob(Random random) {
      return random.nextInt(20) < this.spawners + this.promote;
   }

   private boolean promote(Random random) {
      return random.nextInt(10) < this.promote;
   }

   public int moblevel(Random random) {
      int lev = 0;
      boolean pr = true;

      while (pr && lev < this.maxlev) {
         if (random.nextInt(10) < this.promote) {
            lev++;
         } else {
            pr = false;
         }
      }

      return lev;
   }

   public int nodelevel(Random random) {
      int lev = this.nodelev;
      boolean pr = true;

      while (pr && lev < this.bosslev) {
         if (random.nextInt(10) < this.promote) {
            lev++;
         } else {
            pr = false;
         }
      }

      return lev;
   }
}
