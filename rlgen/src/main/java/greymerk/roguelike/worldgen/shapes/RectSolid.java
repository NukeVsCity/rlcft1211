package greymerk.roguelike.worldgen.shapes;

import com.google.common.collect.Sets;
import greymerk.roguelike.worldgen.Bounded;
import greymerk.roguelike.worldgen.Coord;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public class RectSolid implements IShape, Bounded {
   private final Coord start;
   private final Coord end;

   private RectSolid(Coord start, Coord end) {
      this.start = start.copy();
      this.end = end.copy();
   }

   public static RectSolid newRect(Coord start, Coord end) {
      return new RectSolid(start, end);
   }

   public RectSolid withHeight(int height) {
      Coord lower = this.start.getY() < this.end.getY() ? this.start : this.end;
      this.start.setY(lower.getY());
      this.end.setY(lower.getY() + Math.max(0, height - 1));
      return this;
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
      return new RectSolid.RectSolidIterator(this.start, this.end);
   }

   @Override
   public IShape getShape(Shape type) {
      return this;
   }

   @Override
   public Coord getStart() {
      return this.start;
   }

   @Override
   public Coord getEnd() {
      return this.end;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof RectSolid)) {
         return false;
      } else {
         RectSolid other = (RectSolid)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$start = this.getStart();
            Object other$start = other.getStart();
            if (this$start == null ? other$start == null : this$start.equals(other$start)) {
               Object this$end = this.getEnd();
               Object other$end = other.getEnd();
               return this$end == null ? other$end == null : this$end.equals(other$end);
            } else {
               return false;
            }
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof RectSolid;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $start = this.getStart();
      result = result * 59 + ($start == null ? 43 : $start.hashCode());
      Object $end = this.getEnd();
      return result * 59 + ($end == null ? 43 : $end.hashCode());
   }

   private static class RectSolidIterator implements Iterator<Coord> {
      Coord cursor;
      Coord start;
      Coord end;

      public RectSolidIterator(Coord start, Coord end) {
         this.start = start.copy();
         this.end = end.copy();
         Coord.correct(this.start, this.end);
         this.cursor = this.start.copy();
      }

      @Override
      public boolean hasNext() {
         return this.cursor.getY() <= this.end.getY();
      }

      public Coord next() {
         Coord toReturn = this.cursor.copy();
         if (this.cursor.getZ() == this.end.getZ() && this.cursor.getX() == this.end.getX()) {
            this.cursor = new Coord(this.start.getX(), this.cursor.getY(), this.start.getZ());
            this.cursor.up();
            return toReturn;
         } else if (this.cursor.getX() == this.end.getX()) {
            this.cursor = new Coord(this.start.getX(), this.cursor.getY(), this.cursor.getZ());
            this.cursor.south();
            return toReturn;
         } else {
            this.cursor.east();
            return toReturn;
         }
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }
}
