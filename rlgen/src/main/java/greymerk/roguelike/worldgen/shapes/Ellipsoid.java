package greymerk.roguelike.worldgen.shapes;

import com.google.common.collect.Sets;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public class Ellipsoid implements IShape {
   private Coord start;
   private Coord end;

   public Ellipsoid(Coord start, Coord end) {
      this.start = start.copy();
      this.end = end.copy();
   }

   @Nonnull
   @Override
   public Iterator<Coord> iterator() {
      return new Ellipsoid.EllipsoidIterator(this.start, this.end);
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

   private class EllipsoidIterator implements Iterator<Coord> {
      private Coord centre;
      private Coord diff;
      private Coord cursor;
      private Direction dir;
      private boolean top;

      public EllipsoidIterator(Coord centre, Coord end) {
         this.centre = centre.copy();
         Coord s = centre.copy();
         Coord e = end.copy();
         this.diff = e.translate(-s.getX(), -s.getY(), -s.getZ());
         this.diff = new Coord(Math.abs(this.diff.getX()), Math.abs(this.diff.getY()), Math.abs(this.diff.getZ()));
         this.cursor = new Coord(0, 0, 0);
         this.top = true;
         this.dir = Direction.NORTH;
      }

      @Override
      public boolean hasNext() {
         return this.cursor.getY() < this.diff.getY();
      }

      public Coord next() {
         Coord toReturn = this.centre.copy();
         toReturn.translate(this.top ? Direction.UP : Direction.DOWN, this.cursor.getY());
         if (this.dir != Direction.NORTH && this.dir != Direction.SOUTH) {
            toReturn.translate(this.dir, this.cursor.getX());
            toReturn.translate(this.dir.antiClockwise(), this.cursor.getZ());
         } else {
            toReturn.translate(this.dir.antiClockwise(), this.cursor.getX());
            toReturn.translate(this.dir, this.cursor.getZ());
         }

         if (this.dir == Direction.NORTH && !this.top) {
            this.cursor.south();
            if (this.inRange(this.cursor)) {
               this.dir = this.dir.antiClockwise();
               this.top = true;
               return toReturn;
            } else {
               this.cursor = new Coord(this.cursor.getX(), this.cursor.getY(), 0);
               this.cursor.east();
               if (this.inRange(this.cursor)) {
                  this.dir = this.dir.antiClockwise();
                  this.top = true;
                  return toReturn;
               } else {
                  this.cursor = new Coord(0, this.cursor.getY(), this.cursor.getZ());
                  this.cursor.up();
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

      private boolean inRange(Coord pos) {
         double x = (double)pos.getX();
         double y = (double)pos.getY();
         double z = (double)pos.getZ();
         double rx = this.diff.getX() == 0 ? 1.0 : (double)this.diff.getX();
         double ry = this.diff.getY() == 0 ? 1.0 : (double)this.diff.getY();
         double rz = this.diff.getZ() == 0 ? 1.0 : (double)this.diff.getZ();
         return x / rx * (x / rx) + y / ry * (y / ry) + z / rz * (z / rz) <= 1.0;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }
}
