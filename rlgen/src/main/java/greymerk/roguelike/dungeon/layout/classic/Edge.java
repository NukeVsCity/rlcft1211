package greymerk.roguelike.dungeon.layout.classic;

import greymerk.roguelike.dungeon.layout.DungeonTunnel;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import java.util.Random;

public class Edge {
   private final LayoutGeneratorClassic layoutGeneratorClassic;
   private final Coord start;
   private final Coord end;
   private final Direction dir;
   private final int scatter;
   private boolean done = false;
   private int extensionsRemaining;

   public Edge(LayoutGeneratorClassic layoutGeneratorClassic, Coord start, Direction dir) {
      this.layoutGeneratorClassic = layoutGeneratorClassic;
      this.start = start.copy();
      this.end = start.copy();
      this.dir = dir;
      this.scatter = layoutGeneratorClassic.getScatter();
      this.extensionsRemaining = 2;
   }

   public void update(Random random) {
      if (!this.isDone()) {
         if (this.layoutGeneratorClassic.hasNodeNearby(this.end)) {
            this.end.translate(this.getDir(), this.scatter);
         } else if (random.nextInt(1 + this.extensionsRemaining) > 0) {
            this.end.translate(this.getDir(), this.scatter);
            this.extensionsRemaining--;
         } else {
            this.layoutGeneratorClassic.createNode(this);
            this.done = true;
         }
      }
   }

   public boolean isDone() {
      return this.done;
   }

   public Direction getDirection() {
      return this.getDir();
   }

   public Coord getEnd() {
      return this.end.copy();
   }

   public DungeonTunnel asDungeonTunnel() {
      return new DungeonTunnel(this.start.copy(), this.end.copy());
   }

   public Direction getDir() {
      return this.dir;
   }
}
