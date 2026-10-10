package greymerk.roguelike.worldgen.shapes;

import com.google.common.collect.Sets;
import greymerk.roguelike.worldgen.Coord;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public class Line implements IShape {
   Coord start;
   Coord end;

   public Line(Coord start, Coord end) {
      this.start = start.copy();
      this.end = end.copy();
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

   @Nonnull
   @Override
   public Iterator<Coord> iterator() {
      return new Line.LineIterator();
   }

   private class LineIterator implements Iterator<Coord> {
      int x;
      int y;
      int z;
      int dx;
      int dy;
      int dz;
      int absDx;
      int absDy;
      int absDz;
      int x_inc;
      int y_inc;
      int z_inc;
      int err_1;
      int err_2;
      int dx2;
      int dy2;
      int dz2;
      int i;

      public LineIterator() {
         Coord point = Line.this.start.copy();
         this.x = point.getX();
         this.y = point.getY();
         this.z = point.getZ();
         this.dx = Line.this.end.getX() - Line.this.start.getX();
         this.dy = Line.this.end.getY() - Line.this.start.getY();
         this.dz = Line.this.end.getZ() - Line.this.start.getZ();
         this.x_inc = this.dx < 0 ? -1 : 1;
         this.absDx = Math.abs(this.dx);
         this.y_inc = this.dy < 0 ? -1 : 1;
         this.absDy = Math.abs(this.dy);
         this.z_inc = this.dz < 0 ? -1 : 1;
         this.absDz = Math.abs(this.dz);
         this.dx2 = this.absDx << 1;
         this.dy2 = this.absDy << 1;
         this.dz2 = this.absDz << 1;
         this.i = 0;
         if (this.absDx >= this.absDy && this.absDx >= this.absDz) {
            this.err_1 = this.dy2 - this.absDx;
            this.err_2 = this.dz2 - this.absDx;
         } else if (this.absDy >= this.absDx && this.absDy >= this.absDz) {
            this.err_1 = this.dx2 - this.absDy;
            this.err_2 = this.dz2 - this.absDy;
         } else {
            this.err_1 = this.dy2 - this.absDz;
            this.err_2 = this.dz2 - this.absDz;
         }
      }

      @Override
      public boolean hasNext() {
         if (this.absDx >= this.absDy && this.absDx >= this.absDz) {
            return this.i < this.absDx;
         } else {
            return this.absDy >= this.absDx && this.absDy >= this.absDz ? this.i < this.absDy : this.i < this.absDz;
         }
      }

      public Coord next() {
         if (this.absDx >= this.absDy && this.absDx >= this.absDz) {
            if (this.err_1 > 0) {
               this.y = this.y + this.y_inc;
               this.err_1 = this.err_1 - this.dx2;
            }

            if (this.err_2 > 0) {
               this.z = this.z + this.z_inc;
               this.err_2 = this.err_2 - this.dx2;
            }

            this.err_1 = this.err_1 + this.dy2;
            this.err_2 = this.err_2 + this.dz2;
            this.x = this.x + this.x_inc;
         } else if (this.absDy >= this.absDx && this.absDy >= this.absDz) {
            if (this.err_1 > 0) {
               this.x = this.x + this.x_inc;
               this.err_1 = this.err_1 - this.dy2;
            }

            if (this.err_2 > 0) {
               this.z = this.z + this.z_inc;
               this.err_2 = this.err_2 - this.dy2;
            }

            this.err_1 = this.err_1 + this.dx2;
            this.err_2 = this.err_2 + this.dz2;
            this.y = this.y + this.y_inc;
         } else {
            if (this.err_1 > 0) {
               this.y = this.y + this.y_inc;
               this.err_1 = this.err_1 - this.dz2;
            }

            if (this.err_2 > 0) {
               this.x = this.x + this.x_inc;
               this.err_2 = this.err_2 - this.dz2;
            }

            this.err_1 = this.err_1 + this.dy2;
            this.err_2 = this.err_2 + this.dx2;
            this.z = this.z + this.z_inc;
         }

         this.i++;
         return new Coord(this.x, this.y, this.z);
      }
   }
}
