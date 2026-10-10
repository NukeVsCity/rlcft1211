package jaredbgreat.dldungeons.planner;

import jaredbgreat.dldungeons.rooms.AbstractRoom;
import jaredbgreat.dldungeons.rooms.Room;

public class RoomSeed {
   private int w = 0;
   private int lowEdge;
   private int highEdge;
   private boolean canAddX = true;
   private boolean canAddZ = true;
   private boolean canSubX = true;
   private boolean canSubZ = true;
   int x;
   int y;
   int z;
   int height;
   int grownX = 1;
   int grownZ = 1;
   int endX;
   int beginX;
   int endZ;
   int beginZ;

   public RoomSeed(int startX, int startY, int startZ) {
      this.x = this.endX = this.beginX = startX;
      this.y = startY;
      this.z = this.endZ = this.beginZ = startZ;
   }

   public Room growRoom(int xdim, int zdim, int height, Dungeon dungeon, Room parent, Room previous) {
      if (dungeon.rooms.size() >= dungeon.size.maxRooms) {
         return null;
      } else {
         return dungeon.random.nextBoolean()
            ? this.growRoomX(xdim, zdim, height, dungeon, parent, previous)
            : this.growRoomZ(xdim, zdim, height, dungeon, parent, previous);
      }
   }

   public Room growRoomX(int xdim, int zdim, int height, Dungeon dungeon, Room parent, Room previous) {
      int container;
      if (parent == null) {
         container = 0;
         this.w = 0;
      } else {
         container = parent.id;
         this.w = 1;
      }

      if (this.x < dungeon.size.width && this.x >= 0 && this.z < dungeon.size.width && this.z >= 0) {
         if (dungeon.map.room[this.x][this.z] != container) {
            return null;
         } else {
            while ((this.canAddX || this.canSubX) && this.grownX < xdim) {
               if (this.endX + 1 >= dungeon.size.width) {
                  this.canAddX = false;
               }

               if (this.beginX - 1 < 0) {
                  this.canSubX = false;
               }

               if (this.canAddX) {
                  if (dungeon.map.room[this.endX + this.w][this.z] == container) {
                     this.grownX++;
                     this.endX++;
                  } else {
                     this.canAddX = false;
                  }
               }

               if (this.canSubX) {
                  if (dungeon.map.room[this.beginX - this.w][this.z] == container) {
                     this.grownX++;
                     this.beginX--;
                  } else {
                     this.canSubX = false;
                  }
               }
            }

            while ((this.canAddZ || this.canSubZ) && this.grownZ < zdim) {
               this.lowEdge = this.beginX + 1 - this.w;
               this.highEdge = this.endX - 1 + this.w;

               for (int i = this.lowEdge; i <= this.highEdge; i++) {
                  if (this.endZ + 1 >= dungeon.size.width) {
                     this.canAddZ = false;
                  } else if (dungeon.map.room[i][this.endZ + this.w] != container) {
                     this.canAddZ = false;
                  }

                  if (this.beginZ - 1 < 0) {
                     this.canSubZ = false;
                  } else if (dungeon.map.room[i][this.beginZ - this.w] != container) {
                     this.canSubZ = false;
                  }
               }

               if (this.canAddZ) {
                  this.grownZ++;
                  this.endZ++;
               }

               if (this.canSubZ) {
                  this.grownZ++;
                  this.beginZ--;
               }
            }

            if (this.grownX >= 5 && this.grownZ >= 5) {
               dungeon.roomCount++;
               return AbstractRoom.makeRoom(this.beginX, this.endX, this.beginZ, this.endZ, this.y, this.y + height, dungeon, parent, previous);
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   public Room growRoomZ(int xdim, int zdim, int height, Dungeon dungeon, Room parent, Room previous) {
      int container;
      if (parent == null) {
         container = 0;
         this.w = 0;
      } else {
         container = parent.id;
         this.w = 1;
      }

      if (this.x < dungeon.size.width && this.x >= 0 && this.z < dungeon.size.width && this.z >= 0) {
         if (dungeon.map.room[this.x][this.z] != container) {
            return null;
         } else {
            while ((this.canAddZ || this.canSubZ) && this.grownZ < zdim) {
               if (this.endZ + 1 >= dungeon.size.width) {
                  this.canAddZ = false;
               }

               if (this.beginZ - 1 < 0) {
                  this.canSubZ = false;
               }

               if (this.canAddZ) {
                  if (dungeon.map.room[this.x][this.endZ + this.w] == container) {
                     this.grownZ++;
                     this.endZ++;
                  } else {
                     this.canAddZ = false;
                  }
               }

               if (this.canSubZ) {
                  if (dungeon.map.room[this.x][this.beginZ - this.w] == container) {
                     this.grownZ++;
                     this.beginZ--;
                  } else {
                     this.canSubZ = false;
                  }
               }
            }

            while ((this.canAddX || this.canSubX) && this.grownX < xdim) {
               this.lowEdge = this.beginZ + 1 - this.w;
               this.highEdge = this.endZ - 1 + this.w;

               for (int i = this.lowEdge; i <= this.highEdge; i++) {
                  if (this.endX + 1 >= dungeon.size.width) {
                     this.canAddX = false;
                  } else if (dungeon.map.room[this.endX + this.w][i] != container) {
                     this.canAddX = false;
                  }

                  if (this.beginX - 1 < 0) {
                     this.canSubX = false;
                  } else if (dungeon.map.room[this.beginX - this.w][i] != container) {
                     this.canSubX = false;
                  }
               }

               if (this.canAddX) {
                  this.grownX++;
                  this.endX++;
               }

               if (this.canSubX) {
                  this.grownX++;
                  this.beginX--;
               }
            }

            if (this.grownX >= 5 && this.grownZ >= 5) {
               dungeon.roomCount++;
               return AbstractRoom.makeRoom(this.beginX, this.endX, this.beginZ, this.endZ, this.y, this.y + height, dungeon, parent, previous);
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }
}
