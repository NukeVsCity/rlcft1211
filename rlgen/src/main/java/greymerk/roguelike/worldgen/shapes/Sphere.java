package greymerk.roguelike.worldgen.shapes;

import com.google.common.collect.Sets;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public class Sphere implements IShape {
   private final Coord start;
   private final Coord end;

   public Sphere(Coord start, Coord end) {
      this.start = start.copy();
      this.end = end.copy();
   }

   @Nonnull
   @Override
   public Iterator<Coord> iterator() {
      return new Sphere.SphereIterator(this.start, this.end);
   }

   @Override
   public List<Coord> get() {
      List<Coord> copy = new ArrayList<>();

      for (Coord pos : this) {
         copy.add(pos);
      }

      return copy;
   }

   @Override
   public Set<Coord> getAnchors() {
      return Sets.newHashSet(new Coord[]{this.start, this.end});
   }

   private static class SphereIterator implements Iterator<Coord> {
      private final Coord centre;
      private final int radius;
      private int layer;
      private int row;
      private int col;
      private Direction dir;
      private boolean top;

      public SphereIterator(Coord centre, Coord end) {
         this.centre = centre.copy();
         Coord s = centre.copy();
         Coord e = end.copy();
         Coord.correct(s, e);
         Coord diff = e.translate(-s.getX(), -s.getY(), -s.getZ());
         int radius = diff.getX();
         radius = Math.max(radius, diff.getY());
         radius = Math.max(radius, diff.getZ());
         this.radius = radius;
         this.layer = 0;
         this.row = 0;
         this.col = 0;
         this.top = true;
         this.dir = Direction.NORTH;
      }

      @Override
      public boolean hasNext() {
         return this.layer < this.radius;
      }

      public Coord next() {
         Coord toReturn = this.centre.copy();
         toReturn.translate(this.top ? Direction.UP : Direction.DOWN, this.layer);
         toReturn.translate(this.dir, this.row);
         toReturn.translate(this.dir.antiClockwise(), this.col);
         if (this.dir == Direction.NORTH && !this.top) {
            this.col++;
            if (this.inRange(this.col, this.layer, this.row)) {
               this.dir = this.dir.antiClockwise();
               this.top = true;
               return toReturn;
            } else {
               this.col = 0;
               this.row++;
               if (this.inRange(this.col, this.layer, this.row)) {
                  this.dir = this.dir.antiClockwise();
                  this.top = true;
                  return toReturn;
               } else {
                  this.row = 0;
                  this.layer++;
                  this.dir = this.dir.antiClockwise();
                  this.top = true;
                  return toReturn;
               }
            }
         } else {
            if (this.dir == Direction.NORTH) {
               this.top = false;
            }

            this.dir = this.dir.antiClockwise();
            return toReturn;
         }
      }

      private boolean inRange(int x, int y, int z) {
         return x * x + y * y + z * z < this.radius * this.radius;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }
}
