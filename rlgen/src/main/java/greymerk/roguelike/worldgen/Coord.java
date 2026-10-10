package greymerk.roguelike.worldgen;

import com.google.common.collect.Lists;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Coord {
   private int x;
   private int y;
   private int z;

   public Coord(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Coord(Coord toClone) {
      this(toClone.x, toClone.y, toClone.z);
   }

   public static Coord origin() {
      return new Coord(0, 0, 0);
   }

   public static void correct(Coord one, Coord two) {
      if (two.x < one.x) {
         int temp = two.x;
         two.x = one.x;
         one.x = temp;
      }

      if (two.y < one.y) {
         int temp = two.y;
         two.y = one.y;
         one.y = temp;
      }

      if (two.z < one.z) {
         int temp = two.z;
         two.z = one.z;
         one.z = temp;
      }
   }

   public static List<Coord> randomFrom(List<Coord> coords, int limit, Random random) {
      ArrayList<Coord> coordsShuffled = Lists.newArrayList(coords);
      Collections.shuffle(coordsShuffled, random);
      return coordsShuffled.stream().limit((long)limit).collect(Collectors.toList());
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getZ() {
      return this.z;
   }

   public Coord setX(int x) {
      this.x = x;
      return this;
   }

   public Coord setY(int y) {
      this.y = y;
      return this;
   }

   public Coord setZ(int z) {
      this.z = z;
      return this;
   }

   public Coord copy() {
      return new Coord(this);
   }

   public Coord add(Direction dir) {
      return this.copy().translate(dir, 1);
   }

   public Coord translate(Direction dir, int amount) {
      return dir.translate(this, amount);
   }

   public Coord translate(int x, int y, int z) {
      this.x += x;
      this.y += y;
      this.z += z;
      return this;
   }

   public Coord translate(Coord other) {
      return this.translate(other.x, other.y, other.z);
   }

   public Coord translate(Direction dir) {
      this.translate(dir, 1);
      return this;
   }

   public Coord up() {
      return this.up(1);
   }

   public Coord up(int amount) {
      this.y += amount;
      return this;
   }

   public Coord down() {
      return this.down(1);
   }

   public Coord down(int amount) {
      this.y -= amount;
      return this;
   }

   public Coord north() {
      return this.north(1);
   }

   public Coord north(int amount) {
      this.z -= amount;
      return this;
   }

   public Coord east() {
      return this.east(1);
   }

   public Coord east(int amount) {
      this.x += amount;
      return this;
   }

   public Coord south() {
      return this.south(1);
   }

   public Coord south(int amount) {
      this.z += amount;
      return this;
   }

   public Coord west() {
      return this.west(1);
   }

   public Coord west(int amount) {
      this.x -= amount;
      return this;
   }

   public Coord add(Coord other) {
      return this.copy().translate(other);
   }

   public Coord add(int x, int y, int z) {
      return this.copy().translate(x, y, z);
   }

   public double distance(Coord other) {
      double side1 = (double)Math.abs(this.getX() - other.getX());
      double side2 = (double)Math.abs(this.getZ() - other.getZ());
      return Math.sqrt(side1 * side1 + side2 * side2);
   }

   public Direction dirTo(Coord other) {
      int xdiff = other.x - this.x;
      int ydiff = other.y - this.y;
      int zdiff = other.z - this.z;
      if (Math.abs(ydiff) > Math.abs(xdiff) && Math.abs(ydiff) > Math.abs(zdiff)) {
         return Direction.UP;
      } else if (Math.abs(xdiff) < Math.abs(zdiff)) {
         return zdiff < 0 ? Direction.NORTH : Direction.SOUTH;
      } else {
         return xdiff < 0 ? Direction.WEST : Direction.EAST;
      }
   }

   @Override
   public String toString() {
      return String.format("{\"x\": %d, \"y\": %d, \"z\": %d}", this.x, this.y, this.z);
   }

   public RectSolid newRect(int radius) {
      int newRadius = Math.max(0, radius - 1);
      Coord corner0 = this.copy().north(newRadius).west(newRadius);
      Coord corner1 = this.copy().south(newRadius).east(newRadius);
      return RectSolid.newRect(corner0, corner1);
   }

   public RectHollow newHollowRect(int radius) {
      Coord corner0 = this.copy().north(radius).east(radius);
      Coord corner1 = this.copy().south(radius).west(radius);
      return RectHollow.newRect(corner0, corner1);
   }

   public int distanceAsInt(Coord other) {
      return (int)other.distance(this);
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Coord)) {
         return false;
      } else {
         Coord other = (Coord)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (this.getX() != other.getX()) {
            return false;
         } else {
            return this.getY() != other.getY() ? false : this.getZ() == other.getZ();
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof Coord;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getX();
      result = result * 59 + this.getY();
      return result * 59 + this.getZ();
   }
}
