package atomicstryker.ruins.common;

public class RuinData implements Comparable<RuinData> {
   public final int xMin;
   public final int xMax;
   public final int yMin;
   public final int yMax;
   public final int zMin;
   public final int zMax;
   private final int xMid;
   private final int yMid;
   private final int zMid;
   public final String name;

   public RuinData(int xmin, int xmax, int ymin, int ymax, int zmin, int zmax, String n) {
      this.xMin = xmin;
      this.xMax = xmax;
      this.yMin = ymin;
      this.yMax = ymax;
      this.zMin = zmin;
      this.zMax = zmax;
      this.name = n;
      this.xMid = (this.xMin + this.xMax) / 2;
      this.yMid = (this.yMin + this.yMax) / 2;
      this.zMid = (this.zMin + this.zMax) / 2;
   }

   public RuinData(String string) {
      String[] split = string.split(" ");
      int min = Integer.valueOf(split[0]);
      int max = Integer.valueOf(split[3]);
      if (min > max) {
         int tmp = min;
         min = max;
         max = tmp;
      }

      this.xMin = min;
      this.xMax = max;
      min = Integer.valueOf(split[1]);
      max = Integer.valueOf(split[4]);
      if (min > max) {
         int tmp = min;
         min = max;
         max = tmp;
      }

      this.yMin = min;
      this.yMax = max;
      min = Integer.valueOf(split[2]);
      max = Integer.valueOf(split[5]);
      if (min > max) {
         int tmp = min;
         min = max;
         max = tmp;
      }

      this.zMin = min;
      this.zMax = max;
      this.name = split[6];
      this.xMid = (this.xMin + this.xMax) / 2;
      this.yMid = (this.yMin + this.yMax) / 2;
      this.zMid = (this.zMin + this.zMax) / 2;
   }

   public boolean intersectsWith(RuinData check) {
      return check.xMin <= this.xMax
         && check.xMax >= this.xMin
         && check.zMin <= this.zMax
         && check.zMax >= this.zMin
         && check.yMin <= this.yMax
         && check.yMax >= this.yMin;
   }

   @Override
   public String toString() {
      return this.xMin + " " + this.yMin + " " + this.zMin + " " + this.xMax + " " + this.yMax + " " + this.zMax + " " + this.name;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof RuinData)) {
         return false;
      } else {
         RuinData r = (RuinData)o;
         return r.xMin == this.xMin && r.yMin == this.yMin && r.zMin == this.zMin && r.name.equals(this.name);
      }
   }

   @Override
   public int hashCode() {
      return this.xMid + this.zMid << 8 + this.yMid << 16;
   }

   public int compareTo(RuinData r) {
      if (r.xMin == this.xMin && r.zMin == this.zMin && r.yMin == this.yMin) {
         return 0;
      } else {
         return r.xMin < this.xMin ? 1 : -1;
      }
   }
}
