package greymerk.roguelike.worldgen.shapes;

import com.google.common.collect.Sets;
import greymerk.roguelike.worldgen.Coord;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RectWireframe implements IShape {
   private final Coord start;
   private final Coord end;

   public RectWireframe(Coord start, Coord end) {
      this.start = start;
      this.end = end;
   }

   public static RectWireframe newRect(Coord start, Coord end) {
      return new RectWireframe(start, end);
   }

   @Override
   public List<Coord> get() {
      List<Coord> coords = new ArrayList<>();

      for (Coord c : this) {
         coords.add(c);
      }

      return coords;
   }

   @Override
   public Set<Coord> getAnchors() {
      return Sets.newHashSet(new Coord[]{this.start, this.end});
   }

   @Override
   public Iterator<Coord> iterator() {
      return new RectWireframe.RectWireframeIterator(this.start, this.end);
   }

   private class RectWireframeIterator implements Iterator<Coord> {
      Coord cursor;
      Coord c1;
      Coord c2;

      public RectWireframeIterator(Coord c1, Coord c2) {
         this.c1 = c1.copy();
         this.c2 = c2.copy();
         Coord.correct(this.c1, this.c2);
         this.cursor = c1.copy();
      }

      @Override
      public boolean hasNext() {
         return this.cursor.getY() <= this.c2.getY();
      }

      public Coord next() {
         Coord toReturn = this.cursor.copy();
         if (this.cursor.getZ() == this.c2.getZ() && this.cursor.getX() == this.c2.getX()) {
            this.cursor = new Coord(this.c1.getX(), this.cursor.getY(), this.c1.getZ());
            this.cursor.up();
            return toReturn;
         } else {
            if (this.cursor.getY() == this.c1.getY() || this.cursor.getY() == this.c2.getY()) {
               if (this.cursor.getX() == this.c2.getX()) {
                  this.cursor = new Coord(this.c1.getX(), this.cursor.getY(), this.cursor.getZ());
                  this.cursor.south();
                  return toReturn;
               }

               if (this.cursor.getZ() == this.c1.getZ() || this.cursor.getZ() == this.c2.getZ()) {
                  this.cursor.east();
                  return toReturn;
               }

               if (this.cursor.getX() == this.c1.getX()) {
                  this.cursor = new Coord(this.c2.getX(), this.cursor.getY(), this.cursor.getZ());
                  return toReturn;
               }
            }

            if (this.cursor.getX() == this.c1.getX()) {
               this.cursor = new Coord(this.c2.getX(), this.cursor.getY(), this.cursor.getZ());
               return toReturn;
            } else if (this.cursor.getX() == this.c2.getX()) {
               this.cursor = new Coord(this.c1.getX(), this.cursor.getY(), this.c2.getZ());
               return toReturn;
            } else {
               this.cursor = new Coord(this.c2.getX(), this.cursor.getY(), this.cursor.getZ());
               return toReturn;
            }
         }
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }
}
