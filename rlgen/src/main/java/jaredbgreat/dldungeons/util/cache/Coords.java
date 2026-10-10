package jaredbgreat.dldungeons.util.cache;

import jaredbgreat.dldungeons.util.math.SpatialHash;
import java.util.Comparator;
import java.util.List;

public final class Coords implements Comparable<Coords> {
   private static final SpatialHash nh = new SpatialHash(-1378500386L);
   private final int x;
   private final int z;
   private final int d;

   public Coords(int x, int z, int d) {
      this.x = x;
      this.z = z;
      this.d = d;
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof Coords)) {
         return false;
      } else {
         Coords o = (Coords)other;
         return o.x == this.x && o.z == this.z;
      }
   }

   public boolean equals(int x, int z) {
      return this.x == x && this.z == z;
   }

   @Override
   public int hashCode() {
      return nh.intFor(this.x, this.z, this.d);
   }

   public static int hashCoords(int x, int z, int d) {
      return nh.intFor(x, z, d);
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public int getDimension() {
      return this.d;
   }

   @Override
   public String toString() {
      return "(" + this.x + ", " + this.z + " in dimension " + this.d + ")";
   }

   public static final List<Coords> HashSort(List<Coords> l) {
      l.sort(Coords.HashCompare.C);
      return l;
   }

   public int compareTo(Coords o) {
      return this.hashCode() - o.hashCode();
   }

   public static final class HashCompare implements Comparator<Coords> {
      public static final Coords.HashCompare C = new Coords.HashCompare();

      public int compare(Coords o1, Coords o2) {
         return o1.hashCode() - o2.hashCode();
      }
   }
}
