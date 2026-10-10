package greymerk.roguelike.worldgen.shapes;

import com.google.common.collect.Sets;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public class RectPyramid implements IShape {
   private Coord start;
   private Coord end;

   public RectPyramid(Coord start, Coord end) {
      this.start = start.copy();
      this.end = end.copy();
   }

   @Nonnull
   @Override
   public Iterator<Coord> iterator() {
      return new RectPyramid.SquarePyramidIterator(this.start, this.end);
   }

   @Override
   public List<Coord> get() {
      List<Coord> shape = new ArrayList<>();

      for (Coord pos : this) {
         shape.add(pos);
      }

      return shape;
   }

   @Override
   public Set<Coord> getAnchors() {
      return Sets.newHashSet(new Coord[]{this.start, this.end});
   }

   private class SquarePyramidIterator implements Iterator<Coord> {
      Coord start;
      Coord diff;
      Coord cursor;
      Direction dir;
      double thetaX;
      double thetaZ;

      public SquarePyramidIterator(Coord start, Coord end) {
         this.start = start.copy();
         Coord s = start.copy();
         Coord e = end.copy();
         Coord.correct(s, e);
         this.cursor = new Coord(0, 0, 0);
         this.dir = Direction.NORTH;
         this.diff = e.copy();
         this.diff.translate(-s.getX(), -s.getY(), -s.getZ());
         double hx = Math.sqrt(Math.pow((double)this.diff.getX(), 2.0) + Math.pow((double)this.diff.getY(), 2.0));
         this.thetaX = Math.acos((double)this.diff.getY() / hx);
         double hz = Math.sqrt(Math.pow((double)this.diff.getZ(), 2.0) + Math.pow((double)this.diff.getY(), 2.0));
         this.thetaZ = Math.acos((double)this.diff.getY() / hz);
      }

      @Override
      public boolean hasNext() {
         return this.cursor.getY() < this.diff.getY();
      }

      public Coord next() {
         Coord toReturn = this.start.copy();
         toReturn.up(this.cursor.getY());
         if (this.dir != Direction.NORTH && this.dir != Direction.SOUTH) {
            toReturn.translate(this.dir, this.cursor.getX());
            toReturn.translate(this.dir.antiClockwise(), this.cursor.getZ());
         } else {
            toReturn.translate(this.dir.antiClockwise(), this.cursor.getX());
            toReturn.translate(this.dir, this.cursor.getZ());
         }

         if (this.dir != Direction.NORTH) {
            this.dir = this.dir.antiClockwise();
            return toReturn;
         } else {
            this.cursor.south();
            if (this.inRange(this.cursor)) {
               this.dir = this.dir.antiClockwise();
               return toReturn;
            } else {
               this.cursor = new Coord(this.cursor.getX(), this.cursor.getY(), 0);
               this.cursor.east();
               if (this.inRange(this.cursor)) {
                  this.dir = this.dir.antiClockwise();
                  return toReturn;
               } else {
                  this.cursor = new Coord(0, this.cursor.getY(), this.cursor.getZ());
                  this.cursor.up();
                  this.dir = this.dir.antiClockwise();
                  return toReturn;
               }
            }
         }
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }

      private boolean inRange(Coord pos) {
         int y = this.diff.getY() - this.cursor.getY();
         return !((double)this.cursor.getX() < Math.tan(this.thetaX) * (double)y) ? false : (double)this.cursor.getZ() < Math.tan(this.thetaZ) * (double)y;
      }
   }
}
