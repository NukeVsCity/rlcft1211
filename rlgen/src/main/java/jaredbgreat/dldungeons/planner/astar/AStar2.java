package jaredbgreat.dldungeons.planner.astar;

import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.planner.mapping.Tile;
import jaredbgreat.dldungeons.rooms.Room;
import java.util.PriorityQueue;

public class AStar2 extends AStar {
   PriorityQueue<Step2> edges;
   Step2 root;

   public AStar2(Dungeon dungeon, Room start, Room finish) {
      this.dungeon = dungeon;
      this.room = 0;
      this.x1 = 2;
      this.x2 = dungeon.size.width - 3;
      this.z1 = 1;
      this.z2 = dungeon.size.width - 2;
      this.end = new Tile((int)finish.realX, (int)finish.realZ);
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

      this.root = Step2.firstFromDoorway(new Tile((int)start.realX, (int)start.realZ), this.end);
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

   @Override
   public void makeRoute(Step end) {
      int roomid = this.dungeon.map.room[end.x][end.z];
      byte floory = this.dungeon.map.floorY[end.x][end.z];
      byte ceily = (byte)(this.dungeon.baseHeight + 2);
      int size = this.dungeon.random.nextInt(2) + 1;
      Step child = end;
      Step parent = end.parent;
      if (parent != null) {
         do {
            if (this.dungeon.map.room[child.x][child.z] != 0) {
               roomid = this.dungeon.map.room[child.x][child.z];
               floory = this.dungeon.map.floorY[child.x][child.z];
               ceily = (byte)(floory + 2);
            } else {
               for (int i = -size; i <= size; i++) {
                  for (int j = -size; j <= size; j++) {
                     this.dungeon.map.floorY[child.x + i][child.z + j] = floory;
                     this.dungeon.map.ceilY[child.x + i][child.z + j] = ceily;
                     this.dungeon.map.hasLiquid[child.x + i][child.z + j] = false;
                     if (this.dungeon.map.room[child.x + i][child.z + j] < 1) {
                        this.dungeon.map.room[child.x + i][child.z + j] = roomid;
                        this.dungeon.map.isWall[child.x + i][child.z + j] = true;
                        this.dungeon.map.floor[child.x + i][child.z + j] = this.dungeon.floorBlock;
                        this.dungeon.map.ceiling[child.x + i][child.z + j] = this.dungeon.cielingBlock;
                        this.dungeon.map.wall[child.x + i][child.z + j] = this.dungeon.wallBlock1;
                     }

                     if (this.dungeon.map.astared[child.x + i][child.z + j] || Math.abs(i) < size && Math.abs(j) < size) {
                        this.dungeon.map.isDoor[child.x + i][child.z + j] = true;
                        this.dungeon.map.isWall[child.x + i][child.z + j] = false;
                     }
                  }
               }
            }

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

   @Override
   public void seek() {
      Step2 current;
      do {
         current = this.edges.poll();
         this.getEdgeStep(current);
      } while (!current.equals(this.end));

      this.makeRoute(current);
   }

   @Override
   protected void getEdgeStep(Step src) {
      int x = src.x - 1;
      int z = src.z;
      if (x >= this.x1 && x <= this.x2) {
         Step2 nextEdge = new Step2(x, z, src, this.end, this.dungeon);
         if (this.spt[x][z] == null) {
            this.spt[x][z] = nextEdge;
            this.edges.add(nextEdge);
         }
      }

      x = src.x + 1;
      z = src.z;
      if (x >= this.x1 && x <= this.x2) {
         Step2 nextEdge = new Step2(x, z, src, this.end, this.dungeon);
         if (this.spt[x][z] == null) {
            this.spt[x][z] = nextEdge;
            this.edges.add(nextEdge);
         }
      }

      x = src.x;
      z = src.z - 1;
      if (z >= this.z1 && z <= this.z2) {
         Step2 nextEdge = new Step2(x, z, src, this.end, this.dungeon);
         if (this.spt[x][z] == null) {
            this.spt[x][z] = nextEdge;
            this.edges.add(nextEdge);
         }
      }

      x = src.x;
      z = src.z + 1;
      if (z >= this.z1 && z <= this.z2) {
         Step2 nextEdge = new Step2(x, z, src, this.end, this.dungeon);
         if (this.spt[x][z] == null) {
            this.spt[x][z] = nextEdge;
            this.edges.add(nextEdge);
         }
      }
   }
}
