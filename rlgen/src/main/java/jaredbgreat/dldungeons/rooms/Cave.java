package jaredbgreat.dldungeons.rooms;

import jaredbgreat.dldungeons.planner.Dungeon;

public class Cave extends Room {
   private int xSize;
   private int zSize;
   private int layers;
   private int[][][] cells;
   private int[][] scratchpad;
   private int sum;

   public Cave(int beginX, int endX, int beginZ, int endZ, int floorY, int ceilY, Dungeon dungeon, Room parent, Room previous) {
      super(beginX, endX, beginZ, endZ, floorY, ceilY, dungeon, previous, previous);
      this.degenerate = true;
   }

   @Override
   public Room plan(Dungeon dungeon, Room parent) {
      this.xSize = this.endX - this.beginX;
      this.zSize = this.endZ - this.beginZ;
      this.layers = dungeon.verticle.ordinal();
      if (this.layers < 1) {
         this.layers = 1;
      }

      this.cells = new int[this.layers][this.xSize][this.zSize];

      for (int i = 0; i < this.xSize; i++) {
         for (int j = 0; j < this.zSize; j++) {
            for (int k = 0; k < this.layers; k++) {
               this.cells[k][i][j] = dungeon.random.nextInt(2);
            }
         }
      }

      this.cells[0] = this.layerConvert1(this.cells[0], 5 + dungeon.random.nextInt(2));

      for (int i = 0; i < this.xSize; i++) {
         for (int j = 0; j < this.zSize; j++) {
            if (this.cells[0][i][j] == 1) {
               dungeon.map.isWall[i + this.beginX][j + this.beginZ] = true;
            }
         }
      }

      int tmpFY = this.floorY;
      int tmpCY = this.ceilY;

      for (int k = 1; k < this.layers; k++) {
         this.cells[k] = this.layerConvert2(this.cells[k], k - 1, 4 + dungeon.random.nextInt(3));
         tmpFY += dungeon.random.nextInt(3) - 1;
         tmpCY += dungeon.random.nextInt(3) - 1;
         if (tmpCY - tmpFY < 3) {
            tmpCY = tmpFY + 3;
         }

         for (int i = 0; i < this.xSize; i++) {
            for (int jx = 0; jx < this.zSize; jx++) {
               if (this.cells[k][i][jx] == 0) {
                  dungeon.map.floorY[i + this.beginX][jx + this.beginZ] = (byte)tmpFY;
                  dungeon.map.nFloorY[i + this.beginX][jx + this.beginZ] = (byte)tmpFY;
                  dungeon.map.ceilY[i + this.beginX][jx + this.beginZ] = (byte)tmpCY;
                  dungeon.map.nCeilY[i + this.beginX][jx + this.beginZ] = (byte)tmpCY;
               }
            }
         }
      }

      if (this.hasEntrance) {
         for (int i = (int)this.realX - 2; i < (int)this.realX + 2; i++) {
            for (int jxx = (int)this.realZ - 2; jxx < (int)this.realZ + 2; jxx++) {
               dungeon.map.floorY[i][jxx] = (byte)this.floorY;
               dungeon.map.hasLiquid[i][jxx] = false;
               dungeon.map.isWall[i][jxx] = false;
            }
         }
      }

      if (parent == null) {
         this.addSpawners(dungeon);
      }

      if (dungeon.naturals.use(dungeon.random) || !dungeon.variability.use(dungeon.random)) {
         for (int i = this.beginX; i < this.endX; i++) {
            for (int jxx = this.beginZ; jxx < this.endZ; jxx++) {
               dungeon.map.wall[i][jxx] = this.caveBlock;
               dungeon.map.floor[i][jxx] = this.caveBlock;
               dungeon.map.ceiling[i][jxx] = this.caveBlock;
            }
         }
      }

      return this;
   }

   private int[][] layerConvert1(int[][] layer, int thresshold) {
      this.makeScratchpad();

      for (int i = this.xSize - 2; i > 0; i--) {
         for (int j = this.zSize - 2; j > 0; j--) {
            this.processCell(layer, i, j, thresshold);
         }
      }

      return this.scratchpad;
   }

   private int[][] layerConvert2(int[][] layer, int down, int thresshold) {
      this.makeScratchpad();

      for (int i = this.xSize - 2; i > 0; i--) {
         for (int j = this.zSize - 2; j > 0; j--) {
            if (this.cells[down][i][j] == 1) {
               layer[i][j] = 1;
            }
         }
      }

      for (int i = this.xSize - 2; i > 0; i--) {
         for (int jx = this.zSize - 2; jx > 0; jx--) {
            this.processCell(layer, i, jx, thresshold);
         }
      }

      return this.scratchpad;
   }

   private void processCell(int[][] layer, int x, int z, int thresshold) {
      this.sum = 0;

      for (int i = x - 1; i <= x + 1; i++) {
         for (int j = z - 1; j <= z + 1; j++) {
            this.sum = this.sum + layer[i][j];
         }
      }

      if (this.sum >= thresshold) {
         this.scratchpad[x][z] = 1;
      } else {
         this.scratchpad[x][z] = 0;
      }
   }

   private void makeScratchpad() {
      this.scratchpad = new int[this.xSize][this.zSize];

      for (int i = 0; i < this.xSize; i++) {
         this.scratchpad[i][0] = 1;
         this.scratchpad[i][this.zSize - 1] = 1;
      }

      for (int i = 0; i < this.zSize; i++) {
         this.scratchpad[0][1] = 1;
         this.scratchpad[this.xSize - 1][i] = 1;
      }
   }
}
