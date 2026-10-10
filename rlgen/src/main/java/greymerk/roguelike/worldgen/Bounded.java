package greymerk.roguelike.worldgen;

import greymerk.roguelike.worldgen.shapes.IShape;
import greymerk.roguelike.worldgen.shapes.Shape;

public interface Bounded {
   default boolean collide(Bounded other) {
      if (this.getEnd().getX() < other.getStart().getX() || other.getEnd().getX() < this.getStart().getX()) {
         return false;
      } else {
         return this.getEnd().getY() >= other.getStart().getY() && other.getEnd().getY() >= this.getStart().getY()
            ? this.getEnd().getZ() >= other.getStart().getZ() && other.getEnd().getZ() >= this.getStart().getZ()
            : false;
      }
   }

   IShape getShape(Shape var1);

   Coord getStart();

   Coord getEnd();
}
