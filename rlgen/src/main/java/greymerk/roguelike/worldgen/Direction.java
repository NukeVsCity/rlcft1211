package greymerk.roguelike.worldgen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

public enum Direction {
   NORTH(0, 0, -1),
   EAST(1, 0, 0),
   SOUTH(0, 0, 1),
   WEST(-1, 0, 0),
   UP(0, 1, 0),
   DOWN(0, -1, 0);

   private final int xDelta;
   private final int yDelta;
   private final int zDelta;
   public static List<Direction> CARDINAL = Collections.unmodifiableList(Lists.newArrayList(new Direction[]{NORTH, EAST, SOUTH, WEST}));

   private Direction(int xDelta, int yDelta, int zDelta) {
      this.xDelta = xDelta;
      this.yDelta = yDelta;
      this.zDelta = zDelta;
   }

   public static Direction randomCardinal(Random random) {
      return CARDINAL.get(random.nextInt(CARDINAL.size()));
   }

   public static Set<Direction> cardinals() {
      return Sets.newHashSet(CARDINAL);
   }

   public Coord translate(Coord coord) {
      return coord.translate(this.xDelta, this.yDelta, this.zDelta);
   }

   public Coord translate(Coord coord, int magnitude) {
      return coord.translate(this.xDelta * magnitude, this.yDelta * magnitude, this.zDelta * magnitude);
   }

   public Direction reverse() {
      switch (this) {
         case NORTH:
            return SOUTH;
         case EAST:
            return WEST;
         case WEST:
            return EAST;
         case SOUTH:
            return NORTH;
         case UP:
            return DOWN;
         case DOWN:
         default:
            return UP;
      }
   }

   public Direction antiClockwise() {
      switch (this) {
         case NORTH:
            return WEST;
         case EAST:
            return NORTH;
         case WEST:
            return SOUTH;
         case SOUTH:
            return EAST;
         default:
            return this;
      }
   }

   public Direction clockwise() {
      switch (this) {
         case NORTH:
            return EAST;
         case EAST:
            return SOUTH;
         case WEST:
            return NORTH;
         case SOUTH:
            return WEST;
         default:
            return this;
      }
   }

   public Direction back() {
      return this.reverse();
   }

   public Direction left() {
      return this.antiClockwise();
   }

   public Direction right() {
      return this.clockwise();
   }

   public Direction[] orthogonals() {
      return new Direction[]{this.antiClockwise(), this.clockwise()};
   }
}
