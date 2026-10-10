package greymerk.roguelike.worldgen;

import greymerk.roguelike.worldgen.shapes.IShape;
import java.util.Collection;

public interface BlockBrush {
   boolean FILL_AIR_DEFAULT = true;
   boolean REPLACE_SOLID_DEFAULT = true;

   boolean stroke(WorldEditor var1, Coord var2, boolean var3, boolean var4);

   default boolean stroke(WorldEditor editor, Coord pos) {
      return this.stroke(editor, pos, true, true);
   }

   default IShape fill(WorldEditor editor, IShape shape) {
      return this.fill(editor, shape, true, true);
   }

   default IShape fill(WorldEditor editor, IShape shape, boolean fillAir, boolean replaceSolid) {
      return shape.fill(editor, this, fillAir, replaceSolid);
   }

   default void fill(WorldEditor editor, Collection<Coord> coords, boolean fillAir, boolean replaceSolid) {
      coords.forEach(coord -> this.stroke(editor, coord, fillAir, replaceSolid));
   }

   default void fill(WorldEditor editor, Collection<Coord> coords) {
      this.fill(editor, coords, true, true);
   }

   default BlockBrush setFacing(Direction direction) {
      return this;
   }

   BlockBrush copy();
}
