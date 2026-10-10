package greymerk.roguelike.worldgen.shapes;

import com.google.common.collect.Lists;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.List;
import java.util.Set;

public interface IShape extends Iterable<Coord> {
   default IShape fill(WorldEditor editor, BlockBrush block) {
      return this.fill(editor, block, true, true);
   }

   default IShape fill(WorldEditor editor, BlockBrush block, boolean fillAir, boolean replaceSolid) {
      for (Coord coord : this) {
         block.stroke(editor, coord, fillAir, replaceSolid);
      }

      return this;
   }

   List<Coord> get();

   Set<Coord> getAnchors();

   default IShape translate(Direction direction, int amount) {
      this.getAnchors().forEach(coord -> coord.translate(direction, amount));
      return this;
   }

   default IShape translate(Direction direction) {
      this.translate(direction, 1);
      return this;
   }

   default IShape up(int amount) {
      return this.translate(Direction.UP, amount);
   }

   default IShape up() {
      return this.up(1);
   }

   default IShape down(int amount) {
      return this.translate(Direction.DOWN, amount);
   }

   default IShape down() {
      return this.down(1);
   }

   default IShape north(int amount) {
      return this.translate(Direction.NORTH, amount);
   }

   default IShape north() {
      return this.north(1);
   }

   default IShape east(int amount) {
      return this.translate(Direction.EAST, amount);
   }

   default IShape east() {
      return this.east(1);
   }

   default IShape south(int amount) {
      return this.translate(Direction.SOUTH, amount);
   }

   default IShape south() {
      return this.south(1);
   }

   default IShape west(int amount) {
      return this.translate(Direction.WEST, amount);
   }

   default IShape west() {
      return this.west(1);
   }

   default List<Coord> asList() {
      List<Coord> coords = Lists.newArrayList();

      for (Coord c : this) {
         coords.add(c);
      }

      return coords;
   }
}
