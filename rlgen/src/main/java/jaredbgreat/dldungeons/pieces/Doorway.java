package jaredbgreat.dldungeons.pieces;

import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.planner.mapping.Tile;

public class Doorway extends Tile implements Comparable<Doorway> {
   public boolean xOriented;
   public int priority;
   public int otherside;

   public Doorway(int x, int z, boolean xOriented) {
      super(x, z);
      this.xOriented = xOriented;
      this.priority = 0;
   }

   public Doorway(Doorway door) {
      super(door.x, door.z);
      this.xOriented = door.xOriented;
      this.priority = door.priority;
      this.otherside = door.otherside;
   }

   public Doorway(Doorway door, int otherside) {
      super(door.x, door.z);
      this.xOriented = door.xOriented;
      this.priority = door.priority;
      this.otherside = otherside;
   }

   public void prioritize(Dungeon dungeon, int start) {
      if (this.xOriented) {
         if (dungeon.map.hasLiquid[this.x + 1][this.z]) {
            this.priority++;
         }

         if (dungeon.map.hasLiquid[this.x - 1][this.z]) {
            this.priority++;
         }

         if (dungeon.map.room[this.x + 1][this.z] == start) {
            this.otherside = dungeon.map.room[this.x - 1][this.z];
         } else {
            this.otherside = dungeon.map.room[this.x + 1][this.z];
         }
      } else {
         if (dungeon.map.hasLiquid[this.x][this.z + 1]) {
            this.priority++;
         }

         if (dungeon.map.hasLiquid[this.x][this.z - 1]) {
            this.priority++;
         }

         if (dungeon.map.room[this.x][this.z + 1] == start) {
            this.otherside = dungeon.map.room[this.x][this.z - 1];
         } else {
            this.otherside = dungeon.map.room[this.x][this.z + 1];
         }
      }
   }

   public int compareTo(Doorway o) {
      return this.priority - o.priority;
   }

   public Tile getTile() {
      return new Tile(this.x, this.z);
   }
}
