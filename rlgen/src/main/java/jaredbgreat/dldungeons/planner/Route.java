package jaredbgreat.dldungeons.planner;

import jaredbgreat.dldungeons.rooms.Room;
import java.util.ArrayList;
import java.util.Random;

public class Route {
   private final Node start;
   private final Node finish;
   private Room current1;
   private Room current2;
   private Room temp;
   private float realXDist;
   private float realZDist;
   private int bXDist;
   private int bZDist;
   private int dir1;
   private int dir2;
   private boolean xMatch;
   private boolean zMatch;
   private boolean finishTurn;
   private boolean complete;
   private boolean comp1;
   private boolean comp2;
   private ArrayList<Room> side1;
   private ArrayList<Room> side2;

   protected Route(Node start, Node finish) {
      this.start = start;
      this.finish = finish;
      this.current1 = start.hubRoom;
      this.current2 = finish.hubRoom;
      this.side1 = new ArrayList<>();
      this.side1.add(this.current1);
      this.side2 = new ArrayList<>();
      this.side2.add(this.current2);
      this.finishTurn = false;
      this.complete = false;
      if (this.realXDist > 3.0F) {
         this.bXDist = this.current2.endX - this.current1.beginX;
         this.xMatch = false;
      } else if (this.realXDist < -3.0F) {
         this.bXDist = this.current2.beginX - this.current1.endX;
         this.xMatch = false;
      } else {
         this.bXDist = (int)this.realXDist;
         this.xMatch = true;
      }

      if (this.realZDist > 3.0F) {
         this.bZDist = this.current2.endZ - this.current1.beginZ;
         this.zMatch = false;
      } else if (this.realZDist < -3.0F) {
         this.bZDist = this.current2.beginZ - this.current1.endZ;
         this.zMatch = false;
      } else {
         this.bZDist = (int)this.realZDist;
         this.zMatch = true;
      }
   }

   protected void drawConnections(Dungeon dungeon) {
      int limit = dungeon.size.maxRooms;

      while (!this.complete && limit > 0) {
         limit--;
         if (dungeon.rooms.size() >= dungeon.size.maxRooms) {
            return;
         }

         this.drawConnection(dungeon);
         if (this.complete || limit < 0) {
            return;
         }
      }
   }

   public void drawConnection(Dungeon dungeon) {
      this.getGrowthDir(dungeon.random);
      int height = dungeon.baseHeight;
      int x = dungeon.random.nextInt(dungeon.size.width);
      int z = dungeon.random.nextInt(dungeon.size.width);
      int xdim = dungeon.random.nextInt(dungeon.size.maxRoomSize - 5) + 6;
      int zdim = dungeon.random.nextInt(dungeon.size.maxRoomSize - 5) + 6;
      if (this.close(dungeon.size.maxRoomSize - 1)) {
         zdim = dungeon.size.maxRoomSize;
         xdim = dungeon.size.maxRoomSize;
      }

      int ymod = xdim <= zdim ? (int)Math.sqrt((double)xdim) : (int)Math.sqrt((double)zdim);
      int roomHeight = dungeon.random.nextInt(dungeon.verticle.value / 2 + ymod + 1) + 2;
      if (this.finishTurn) {
         this.dir1 = (this.dir1 + 2) % 4;
         this.dir2 = (this.dir2 + 2) % 4;
         this.temp = this.current2.connector(dungeon, this.dir1, xdim, zdim, roomHeight, this);
         if (this.temp == null || this.side2.contains(this.temp)) {
            this.temp = this.current2.connector(dungeon, this.dir2, xdim, zdim, roomHeight, this);
         }

         if (this.temp == null || this.side2.contains(this.temp)) {
            this.comp2 = true;
         } else if (this.side1.contains(this.temp)) {
            this.complete = true;
         } else {
            this.side2.add(this.temp);
            this.current2 = this.temp;
         }
      } else {
         this.temp = this.current1.connector(dungeon, this.dir1, xdim, zdim, roomHeight, this);
         if (this.temp == null || this.side1.contains(this.temp)) {
            this.temp = this.current1.connector(dungeon, this.dir2, xdim, zdim, roomHeight, this);
         }

         if (this.temp == null || this.side1.contains(this.temp)) {
            this.comp1 = true;
         } else if (this.side2.contains(this.temp)) {
            this.complete = true;
         } else {
            this.side1.add(this.temp);
            this.current1 = this.temp;
         }
      }

      if (!this.complete) {
         this.complete = this.comp1 && this.comp2;
      }

      if (this.comp1) {
         this.finishTurn = true;
      } else if (this.comp2) {
         this.finishTurn = false;
      } else {
         this.finishTurn = !this.finishTurn;
      }
   }

   private boolean xOverlap() {
      return this.current1.endX > this.current2.beginX && this.current2.endX > this.current1.beginX;
   }

   private boolean zOverlap() {
      return this.current1.endZ > this.current2.beginZ && this.current2.endZ > this.current1.beginZ;
   }

   private boolean touchesOnX() {
      return !this.zOverlap() ? false : this.current1.beginX == this.current2.endX || this.current1.endX == this.current2.beginX;
   }

   private boolean touchesOnZ() {
      return !this.xOverlap() ? false : this.current1.beginZ == this.current2.endZ || this.current1.endZ == this.current2.beginZ;
   }

   private boolean touching() {
      return this.touchesOnX() || this.touchesOnZ();
   }

   private int touchDir() {
      if (this.zOverlap()) {
         if (this.current1.endX == this.current2.beginX) {
            return 0;
         }

         if (this.current1.beginX == this.current2.endX) {
            return 2;
         }
      } else if (this.xOverlap()) {
         if (this.current1.endZ == this.current2.beginZ) {
            return 1;
         }

         if (this.current1.beginZ == this.current2.endZ) {
            return 3;
         }
      }

      return -1;
   }

   private boolean close(int range) {
      if (Math.abs(this.current1.beginX - this.current2.endX) > range) {
         return false;
      } else if (Math.abs(this.current2.beginX - this.current1.endX) > range) {
         return false;
      } else {
         return Math.abs(this.current1.beginZ - this.current2.endZ) > range ? false : Math.abs(this.current2.beginZ - this.current1.endZ) <= range;
      }
   }

   private void getGrowthDir(Random random) {
      boolean posX = this.current1.realX < this.current2.realX;
      boolean posZ = this.current1.realZ < this.current2.realZ;
      if (this.xOverlap()) {
         if (posZ) {
            this.dir1 = 1;
            this.dir2 = random.nextInt(2) * 2;
         } else {
            this.dir1 = 3;
            this.dir2 = random.nextInt(2) * 2;
         }
      } else if (this.zOverlap()) {
         if (posX) {
            this.dir1 = 0;
            this.dir2 = 1 + random.nextInt(2) * 2;
         } else {
            this.dir1 = 2;
            this.dir2 = 1 + random.nextInt(2) * 2;
         }
      } else if (random.nextInt((int)(Math.abs(this.realXDist) + Math.abs(this.realZDist) + 1.0F)) > (int)Math.abs(this.realXDist)) {
         if (posX) {
            this.dir1 = 0;
            if (posZ) {
               this.dir2 = 1;
            } else {
               this.dir2 = 3;
            }
         } else {
            this.dir1 = 2;
            if (posZ) {
               this.dir2 = 1;
            } else {
               this.dir2 = 3;
            }
         }
      } else if (posZ) {
         this.dir1 = 1;
         if (posX) {
            this.dir2 = 0;
         } else {
            this.dir2 = 2;
         }
      } else {
         this.dir1 = 3;
         if (posX) {
            this.dir2 = 0;
         } else {
            this.dir2 = 2;
         }
      }
   }
}
