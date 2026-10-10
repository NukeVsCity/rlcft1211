package jaredbgreat.dldungeons.planner.astar;

import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.planner.mapping.Tile;

public class Step extends Tile implements Comparable<Step> {
   int changes;
   int distance;
   int heuristic;
   int value;
   Step parent;

   Step(int x, int z, int traversed, int changes, Tile destination) {
      super(x, z);
      this.distance = traversed;
      this.changes = changes;
      this.heuristic = Math.abs(x - destination.x) + Math.abs(z - destination.z);
      this.value = changes * 16 + this.distance + this.heuristic;
      this.parent = null;
   }

   public Step(int x, int z, Step previous, Tile destination, Dungeon dungeon) {
      super(x, z);
      this.distance = previous.distance + 1;
      this.heuristic = Math.abs(x - destination.x) + Math.abs(z - destination.z);
      this.changes = previous.changes;
      if (dungeon.map.isWall[x][z]) {
         this.changes++;
      }

      if (dungeon.map.isFence[x][z]) {
         this.changes++;
      }

      if (dungeon.map.hasLiquid[x][z]) {
         this.changes++;
      }

      if (Math.abs(dungeon.map.floorY[x][z] - dungeon.map.floorY[previous.x][previous.z]) > 1) {
         this.changes++;
      }

      this.value = this.changes * 16 + this.distance + this.heuristic;
      this.parent = previous;
   }

   public static Step firstFromDoorway(Tile door, Tile destination) {
      return new Step(door.x, door.z, 0, 0, destination);
   }

   public Tile getTile() {
      return new Tile(this.x, this.z);
   }

   public int compareTo(Step o) {
      return this.value - o.value;
   }
}
