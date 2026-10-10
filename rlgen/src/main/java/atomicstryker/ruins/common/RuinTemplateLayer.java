package atomicstryker.ruins.common;

import java.util.ArrayList;
import java.util.Iterator;

class RuinTemplateLayer {
   private final int[][] layer;

   public RuinTemplateLayer(ArrayList<String> layerdata, int width, int length, int maxrule) throws Exception {
      this.layer = new int[width][length];
      Iterator<String> i = layerdata.iterator();

      for (int x = 0; x < width; x++) {
         String row = i.next();
         String[] rowdata = row.split(",");

         for (int z = 0; z < length; z++) {
            this.layer[x][z] = Integer.parseInt(rowdata[length - z - 1]);
            if (this.layer[x][z] > maxrule) {
               throw new Exception("Template uses undefined Rule, number: " + this.layer[x][z]);
            }
         }
      }
   }

   public int getRuleAt(int x, int z) {
      try {
         return this.layer[x][z];
      } catch (Exception var4) {
         System.err.println("Attempting to get rule at " + x + ", " + z + ".");
         return 0;
      }
   }
}
