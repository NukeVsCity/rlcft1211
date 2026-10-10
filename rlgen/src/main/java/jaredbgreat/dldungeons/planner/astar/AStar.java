package jaredbgreat.dldungeons.planner.astar;

import jaredbgreat.dldungeons.pieces.Doorway;
import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.planner.mapping.Tile;
import jaredbgreat.dldungeons.rooms.Room;
import java.util.PriorityQueue;

public class AStar {
   int room;
   int x1;
   int x2;
   int z1;
   int z2;
   PriorityQueue<Step> edges;
   Step[][] spt;
   Dungeon dungeon;
   Step root;
   Tile end;

   protected AStar() {
   }

   public AStar(Room room, Dungeon dungeon, Doorway start, Doorway finish) {
      this.room = room.id;
      this.dungeon = dungeon;
      this.x1 = room.beginX;
      this.x2 = room.endX;
      this.z1 = room.beginZ;
      this.z2 = room.endZ;
      this.end = finish;
      if (this.end.x < this.x1) {
         this.x1 = this.end.x;
      }

      if (this.end.x > this.x2) {
         this.x2 = this.end.x;
      }

      if (this.end.z < this.z1) {
         this.z1 = this.end.z;
      }

      if (this.end.z > this.z2) {
         this.z2 = this.end.z;
      }

      this.root = Step.firstFromDoorway(start, finish);
      if (this.root.x < this.x1) {
         this.x1 = this.root.x;
      }

      if (this.root.x > this.x2) {
         this.x2 = this.root.x;
      }

      if (this.root.z < this.z1) {
         this.z1 = this.root.z;
      }

      if (this.root.z > this.z2) {
         this.z2 = this.root.z;
      }

      this.spt = dungeon.map.nodedge;

      for (int i = this.x1; i <= this.x2; i++) {
         for (int j = this.z1; j <= this.z2; j++) {
            this.spt[i][j] = null;
         }
      }

      this.spt[this.root.x][this.root.z] = this.root;
      this.edges = new PriorityQueue<>();
      this.edges.add(this.root);
   }

   public void seek() {
      Step current;
      do {
         current = this.edges.poll();
         this.getEdgeStep(current);
      } while (!current.equals(this.end));

      this.makeRoute(current);
   }

   public void makeRoute(Step end) {
      Step child = end;
      Step parent = end.parent;
      if (parent != null) {
         this.dungeon.map.astared[end.x][end.z] = true;
         if (this.dungeon.map.isWall[end.x][end.z] || this.dungeon.map.isFence[end.x][end.z]) {
            this.dungeon.map.isDoor[end.x][end.z] = true;
         }

         if (this.dungeon.map.hasLiquid[end.x][end.z]) {
            this.dungeon.map.hasLiquid[end.x][end.z] = false;
            this.dungeon.map.floorY[end.x][end.z] = (byte)this.dungeon.rooms.get(this.room).floorY;
         }

         do {
            this.dungeon.map.astared[child.x][child.z] = true;
            if (this.dungeon.map.isWall[child.x][child.z] || this.dungeon.map.isFence[child.x][child.z]) {
               this.addDoor(parent, child);
            }

            if (this.dungeon.map.hasLiquid[child.x][child.z]) {
               this.fixLiquid(parent, child, (byte)this.dungeon.rooms.get(this.room).floorY);
            }

            this.fixHeights(parent, child);
            child = parent;
            parent = parent.parent;
         } while (parent != null);

         this.dungeon.map.astared[child.x][child.z] = true;
         if (this.dungeon.map.isWall[child.x][child.z] || this.dungeon.map.isFence[child.x][child.z]) {
            this.dungeon.map.isDoor[child.x][child.z] = true;
         }

         if (this.dungeon.map.hasLiquid[child.x][child.z]) {
            this.dungeon.map.hasLiquid[child.x][child.z] = false;
            this.dungeon.map.floorY[child.x][child.z] = (byte)this.dungeon.rooms.get(this.room).floorY;
         }
      }
   }

   protected void addDoor(Step from, Step to) {
      this.dungeon.map.isDoor[to.x][to.z] = true;
      this.dungeon.map.isDoor[from.x][from.z] = true;
   }

   protected void fixLiquid(Step from, Step to, byte floorY) {
      this.dungeon.map.hasLiquid[to.x][to.z] = false;
      this.dungeon.map.floorY[to.x][to.z] = this.dungeon.map.floorY[from.x][from.z];
      if (this.dungeon.map.floorY[to.x][to.z] < floorY) {
         this.dungeon.map.floorY[to.x][to.z] = floorY;
      }
   }

   protected void fixHeights(Step from, Step to) {
      int diff = this.dungeon.map.floorY[to.x][to.z] - this.dungeon.map.floorY[from.x][from.z];
      if (diff > 2) {
         this.dungeon.map.floorY[to.x][to.z] = (byte)(this.dungeon.map.floorY[from.x][from.z] - 1);
      }

      if (diff < -2) {
         this.dungeon.map.floorY[to.x][to.z] = (byte)(this.dungeon.map.floorY[from.x][from.z] + 1);
      }
   }

   protected void getEdgeStep(Step src) {
      int x = src.x - 1;
      int z = src.z;
      if (x >= this.x1) {
         Step nextEdge = new Step(x, z, src, this.end, this.dungeon);
         if (this.spt[x][z] == null) {
            this.spt[x][z] = nextEdge;
            this.edges.add(nextEdge);
         }
      }

      x = src.x + 1;
      z = src.z;
      if (x >= this.x1 && x <= this.x2) {
         Step nextEdge = new Step(x, z, src, this.end, this.dungeon);
         if (this.spt[x][z] == null) {
            this.spt[x][z] = nextEdge;
            this.edges.add(nextEdge);
         }
      }

      x = src.x;
      z = src.z - 1;
      if (z >= this.z1 && z <= this.z2) {
         Step nextEdge = new Step(x, z, src, this.end, this.dungeon);
         if (this.spt[x][z] == null) {
            this.spt[x][z] = nextEdge;
            this.edges.add(nextEdge);
         }
      }

      x = src.x;
      z = src.z + 1;
      if (z >= this.z1 && z <= this.z2) {
         Step nextEdge = new Step(x, z, src, this.end, this.dungeon);
         if (this.spt[x][z] == null) {
            this.spt[x][z] = nextEdge;
            this.edges.add(nextEdge);
         }
      }
   }
}
