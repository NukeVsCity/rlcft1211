package greymerk.roguelike.worldgen.shapes;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.google.common.collect.Sets;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RectHollow implements IShape {
   private final Coord start;
   private final Coord end;

   public RectHollow(Coord start, Coord end) {
      this.start = start.copy();
      this.end = end.copy();
   }

   public static RectHollow newRect(Coord start, Coord end) {
      return new RectHollow(start, end);
   }

   public RectHollow withHeight(int height) {
      Coord lower = this.start.getY() < this.end.getY() ? this.start : this.end;
      this.start.setY(lower.getY());
      this.end.setY(lower.getY() + Math.max(0, height - 1));
      return this;
   }

   @Override
   public IShape fill(WorldEditor editor, BlockBrush block, boolean fillAir, boolean replaceSolid) {
      IShape.super.fill(editor, block, fillAir, replaceSolid);
      Coord innerStart = this.start.copy();
      Coord innerEnd = this.end.copy();
      Coord.correct(innerStart, innerEnd);
      innerStart.translate(new Coord(1, 1, 1));
      innerEnd.translate(new Coord(-1, -1, -1));
      RectSolid.newRect(innerStart, innerEnd).fill(editor, SingleBlockBrush.AIR);
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

   @Override
   public Iterator<Coord> iterator() {
      return new RectHollow.RectHollowIterator(this.start, this.end);
   }

   private class RectHollowIterator implements Iterator<Coord> {
      Coord cursor;
      Coord c1;
      Coord c2;

      public RectHollowIterator(Coord c1, Coord c2) {
         this.c1 = c1.copy();
         this.c2 = c2.copy();
         Coord.correct(this.c1, this.c2);
         this.cursor = this.c1.copy();
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
         } else if (this.cursor.getX() == this.c2.getX()) {
            this.cursor = new Coord(this.c1.getX(), this.cursor.getY(), this.cursor.getZ());
            this.cursor.south();
            return toReturn;
         } else {
            if (this.cursor.getY() != this.c1.getY()
               && this.cursor.getY() != this.c2.getY()
               && this.cursor.getZ() != this.c1.getZ()
               && this.cursor.getZ() != this.c2.getZ()
               && this.cursor.getX() == this.c1.getX()) {
               this.cursor.east(this.c2.getX() - this.c1.getX());
            } else {
               this.cursor.east();
            }

            return toReturn;
         }
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }
}
