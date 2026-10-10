package jaredbgreat.dldungeons.pieces;

import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.rooms.Room;
import jaredbgreat.dldungeons.themes.ThemeFlags;
import jaredbgreat.dldungeons.themes.ThemeType;

public final class Rectangle {
   private final float xdim;
   private final float zdim;
   private final float xcoord;
   private final float zcoord;
   public static final Rectangle simple = new Rectangle(1.0F, 1.0F, 0.0F, 0.0F);
   public static final Rectangle lback000 = new Rectangle(0.5F, 1.0F, -0.25F, 0.0F);
   public static final Rectangle lbottom000 = new Rectangle(0.5F, 0.5F, 0.25F, -0.25F);
   public static final Rectangle lback090 = new Rectangle(1.0F, 0.5F, 0.0F, -0.25F);
   public static final Rectangle lbottom090 = new Rectangle(0.5F, 0.5F, 0.25F, 0.25F);
   public static final Rectangle lback270 = new Rectangle(0.5F, 1.0F, 0.25F, 0.0F);
   public static final Rectangle lbottom270 = new Rectangle(0.5F, 0.5F, -0.25F, 0.25F);
   public static final Rectangle lback180 = new Rectangle(1.0F, 0.5F, 0.0F, -0.25F);
   public static final Rectangle lbottom180 = new Rectangle(0.5F, 0.5F, 0.25F, 0.25F);
   public static final Rectangle oleft = new Rectangle(0.33333334F, 1.0F, -0.33333334F, 0.0F);
   public static final Rectangle oright = new Rectangle(0.33333334F, 1.0F, 0.33333334F, 0.0F);
   public static final Rectangle obottom = new Rectangle(0.33333334F, 0.33333334F, 0.0F, -0.33333334F);
   public static final Rectangle otop = new Rectangle(0.33333334F, 0.33333334F, 0.0F, 0.33333334F);
   public static final Rectangle ttop000 = new Rectangle(1.0F, 0.33333334F, 0.0F, 0.33333334F);
   public static final Rectangle tbottom000 = new Rectangle(0.33333334F, 0.6666667F, 0.0F, -0.16666667F);
   public static final Rectangle ttop090 = new Rectangle(0.33333334F, 1.0F, -0.33333334F, 0.0F);
   public static final Rectangle tbottom090 = new Rectangle(0.6666667F, 0.33333334F, 0.16666667F, 0.0F);
   public static final Rectangle ttop180 = new Rectangle(1.0F, 0.33333334F, 0.0F, -0.33333334F);
   public static final Rectangle tbottom180 = new Rectangle(0.33333334F, 0.6666667F, 0.0F, 0.16666667F);
   public static final Rectangle ttop270 = new Rectangle(0.33333334F, 1.0F, 0.33333334F, 0.0F);
   public static final Rectangle tbottom270 = new Rectangle(0.6666667F, 0.33333334F, -0.16666667F, 0.0F);
   public static final Rectangle eback000 = new Rectangle(0.33333334F, 1.0F, -0.33333334F, 0.0F);
   public static final Rectangle etop000 = new Rectangle(0.6666667F, 0.2F, 0.16666667F, 0.4F);
   public static final Rectangle emiddle000 = new Rectangle(0.6666667F, 0.2F, 0.16666667F, 0.0F);
   public static final Rectangle ebottom000 = new Rectangle(0.6666667F, 0.2F, 0.16666667F, -0.4F);
   public static final Rectangle eback090 = new Rectangle(1.0F, 0.33333334F, 0.0F, -0.33333334F);
   public static final Rectangle etop090 = new Rectangle(0.2F, 0.6666667F, -0.4F, 0.16666667F);
   public static final Rectangle emiddle090 = new Rectangle(0.2F, 0.6666667F, 0.0F, 0.16666667F);
   public static final Rectangle ebottom090 = new Rectangle(0.2F, 0.6666667F, 0.4F, 0.16666667F);
   public static final Rectangle eback180 = new Rectangle(0.33333334F, 1.0F, 0.33333334F, 0.0F);
   public static final Rectangle etop180 = new Rectangle(0.6666667F, 0.2F, -0.16666667F, 0.4F);
   public static final Rectangle emiddle180 = new Rectangle(0.6666667F, 0.2F, -0.16666667F, 0.0F);
   public static final Rectangle ebottom180 = new Rectangle(0.6666667F, 0.2F, -0.16666667F, -0.4F);
   public static final Rectangle eback270 = new Rectangle(1.0F, 0.33333334F, 0.0F, 0.33333334F);
   public static final Rectangle etop270 = new Rectangle(0.2F, 0.6666667F, 0.4F, -0.16666667F);
   public static final Rectangle emiddle270 = new Rectangle(0.2F, 0.6666667F, 0.0F, -0.16666667F);
   public static final Rectangle ebottom270 = new Rectangle(0.2F, 0.6666667F, -0.4F, -0.16666667F);
   public static final Rectangle itop = new Rectangle(1.0F, 0.33333334F, 0.0F, 0.33333334F);
   public static final Rectangle imiddle = new Rectangle(0.33333334F, 0.33333334F, 0.0F, 0.0F);
   public static final Rectangle ibottom = new Rectangle(1.0F, 0.33333334F, 0.0F, -0.33333334F);
   public static final Rectangle iright = new Rectangle(0.33333334F, 1.0F, 0.33333334F, 0.0F);
   public static final Rectangle ileft = new Rectangle(0.33333334F, 1.0F, -0.33333334F, 0.0F);
   public static final Rectangle crosstop = new Rectangle(0.33333334F, 0.33333334F, 0.0F, 0.33333334F);
   public static final Rectangle crossmiddle = new Rectangle(1.0F, 0.33333334F, 0.0F, 0.0F);
   public static final Rectangle crossbottom = new Rectangle(0.33333334F, 0.33333334F, 0.0F, -0.33333334F);
   public static final Rectangle uleft000 = new Rectangle(0.33333334F, 0.6666667F, -0.33333334F, 0.16666667F);
   public static final Rectangle uright000 = new Rectangle(0.33333334F, 0.6666667F, 0.33333334F, 0.16666667F);
   public static final Rectangle ubottom000 = new Rectangle(1.0F, 0.33333334F, 0.0F, -0.33333334F);
   public static final Rectangle uleft090 = new Rectangle(0.6666667F, 0.33333334F, 0.16666667F, -0.33333334F);
   public static final Rectangle uright090 = new Rectangle(0.6666667F, 0.33333334F, 0.16666667F, 0.33333334F);
   public static final Rectangle ubottom090 = new Rectangle(0.33333334F, 1.0F, -0.33333334F, 0.0F);
   public static final Rectangle uright180 = new Rectangle(0.33333334F, 0.6666667F, 0.33333334F, -0.16666667F);
   public static final Rectangle uleft180 = new Rectangle(0.33333334F, 0.6666667F, -0.33333334F, -0.16666667F);
   public static final Rectangle ubottom180 = new Rectangle(1.0F, 0.33333334F, 0.0F, 0.33333334F);
   public static final Rectangle uright270 = new Rectangle(0.6666667F, 0.33333334F, -0.16666667F, -0.33333334F);
   public static final Rectangle uleft270 = new Rectangle(0.6666667F, 0.33333334F, -0.16666667F, 0.33333334F);
   public static final Rectangle ubottom270 = new Rectangle(0.33333334F, 1.0F, 0.33333334F, 0.0F);
   public static final Rectangle stop000 = new Rectangle(1.0F, 0.2F, 0.0F, -0.4F);
   public static final Rectangle smiddle000 = new Rectangle(1.0F, 0.2F, 0.0F, 0.0F);
   public static final Rectangle sbottom000 = new Rectangle(1.0F, 0.2F, 0.0F, 0.4F);
   public static final Rectangle sleft000 = new Rectangle(0.2F, 0.2F, -0.4F, 0.2F);
   public static final Rectangle sright000 = new Rectangle(0.2F, 0.2F, 0.4F, -0.2F);
   public static final Rectangle stop090 = new Rectangle(0.2F, 1.0F, -0.4F, 0.0F);
   public static final Rectangle smiddle090 = new Rectangle(0.2F, 1.0F, 0.0F, 0.0F);
   public static final Rectangle sbottom090 = new Rectangle(0.2F, 1.0F, 0.4F, 0.0F);
   public static final Rectangle sleft090 = new Rectangle(0.2F, 0.2F, -0.2F, 0.4F);
   public static final Rectangle sright090 = new Rectangle(0.2F, 0.2F, 0.2F, -0.4F);

   public Rectangle(float xdim, float zdim, float xcoord, float zcoord) {
      this.xdim = xdim;
      this.zdim = zdim;
      this.xcoord = xcoord;
      this.zcoord = zcoord;
   }

   public void drawLiquid(Dungeon dungeon, Room room, float sx, float sz, float sdimx, float sdimz, boolean invertX, boolean invertZ) {
      int drop;
      if (dungeon.theme.flags.contains(ThemeFlags.SWAMPY)) {
         drop = 1;
      } else {
         drop = 2;
      }

      byte xbegin;
      byte xend;
      if (invertX) {
         xbegin = (byte)((int)(sx - sdimx * this.xdim / 2.0F - this.xcoord * sdimx));
         xend = (byte)((int)(sx + sdimx * this.xdim / 2.0F - this.xcoord * sdimx));
      } else {
         xbegin = (byte)((int)(sx - sdimx * this.xdim / 2.0F + this.xcoord * sdimx));
         xend = (byte)((int)(sx + sdimx * this.xdim / 2.0F + this.xcoord * sdimx));
      }

      byte zbegin;
      byte zend;
      if (invertZ) {
         zbegin = (byte)((int)(sz - sdimz * this.zdim / 2.0F - this.zcoord * sdimz));
         zend = (byte)((int)(sz + sdimz * this.zdim / 2.0F - this.zcoord * sdimz));
      } else {
         zbegin = (byte)((int)(sz - sdimz * this.zdim / 2.0F + this.zcoord * sdimz));
         zend = (byte)((int)(sz + sdimz * this.zdim / 2.0F + this.zcoord * sdimz));
      }

      for (byte k = zbegin; k < zend; k++) {
         for (byte j = xbegin; j < xend; j++) {
            if (j >= 0 && j < dungeon.size.width && k >= 0 && k < dungeon.size.width && dungeon.map.room[j][k] == room.id) {
               if (dungeon.map.floorY[j][k] > room.floorY) {
                  dungeon.map.floorY[j][k] = (byte)room.floorY;
               }

               dungeon.map.floorY[j][k] = (byte)(room.floorY - drop);
               dungeon.map.hasLiquid[j][k] = true;
               byte nFloorY = dungeon.map.nFloorY[j][k] < dungeon.map.floorY[j][k] ? dungeon.map.nFloorY[j][k] : dungeon.map.floorY[j][k];
               dungeon.map.nFloorY[j][k] = nFloorY;
            }
         }
      }
   }

   public void drawWalkway(Dungeon dungeon, Room room, float sx, float sz, byte sdimx, byte sdimz, boolean invertX, boolean invertZ) {
      int drop;
      if (dungeon.theme.type.contains(ThemeType.SWAMP)) {
         drop = 1;
      } else {
         drop = 2;
      }

      byte xbegin;
      byte xend;
      if (invertX) {
         xbegin = (byte)((int)(sx - (float)sdimx * this.xdim / 2.0F - this.xcoord * (float)sdimx));
         xend = (byte)((int)(sx + (float)sdimx * this.xdim / 2.0F - this.xcoord * (float)sdimx));
      } else {
         xbegin = (byte)((int)(sx - (float)sdimx * this.xdim / 2.0F + this.xcoord * (float)sdimx));
         xend = (byte)((int)(sx + (float)sdimx * this.xdim / 2.0F + this.xcoord * (float)sdimx));
      }

      byte zbegin;
      byte zend;
      if (invertZ) {
         zbegin = (byte)((int)(sz - (float)sdimz * this.zdim / 2.0F - this.zcoord * (float)sdimz));
         zend = (byte)((int)(sz + (float)sdimz * this.zdim / 2.0F - this.zcoord * (float)sdimz));
      } else {
         zbegin = (byte)((int)(sz - (float)sdimz * this.zdim / 2.0F + this.zcoord * (float)sdimz));
         zend = (byte)((int)(sz + (float)sdimz * this.zdim / 2.0F + this.zcoord * (float)sdimz));
      }

      for (byte k = zbegin; k < zend; k++) {
         for (byte j = xbegin; j < xend; j++) {
            if (j >= 0 && j < dungeon.size.width && k >= 0 && k < dungeon.size.width) {
               byte nFloorY = dungeon.map.nFloorY[j][k] < dungeon.map.floorY[j][k] ? dungeon.map.nFloorY[j][k] : dungeon.map.floorY[j][k];
               dungeon.map.floorY[j][k] = (byte)(dungeon.map.floorY[j][k] + drop);
               dungeon.map.hasLiquid[j][k] = false;
               dungeon.map.nFloorY[j][k] = nFloorY;
            }
         }
      }
   }

   public void drawCutout(Dungeon dungeon, Room room, float sx, float sz, float sdimx, float sdimz, boolean invertX, boolean invertZ) {
      byte xbegin;
      byte xend;
      if (invertX) {
         xbegin = (byte)((int)(sx - sdimx * this.xdim / 2.0F - this.xcoord * sdimx));
         xend = (byte)((int)(sx + sdimx * this.xdim / 2.0F - this.xcoord * sdimx));
      } else {
         xbegin = (byte)((int)(sx - sdimx * this.xdim / 2.0F + this.xcoord * sdimx));
         xend = (byte)((int)(sx + sdimx * this.xdim / 2.0F + this.xcoord * sdimx));
      }

      byte zbegin;
      byte zend;
      if (invertZ) {
         zbegin = (byte)((int)(sz - sdimz * this.zdim / 2.0F - this.zcoord * sdimz));
         zend = (byte)((int)(sz + sdimz * this.zdim / 2.0F - this.zcoord * sdimz));
      } else {
         zbegin = (byte)((int)(sz - sdimz * this.zdim / 2.0F + this.zcoord * sdimz));
         zend = (byte)((int)(sz + sdimz * this.zdim / 2.0F + this.zcoord * sdimz));
      }

      for (byte k = zbegin; k < zend; k++) {
         for (byte j = xbegin; j < xend; j++) {
            if (j >= 0 && j < dungeon.size.width && k >= 0 && k < dungeon.size.width && dungeon.map.room[j][k] == room.id) {
               dungeon.map.isWall[j][k] = true;
            }
         }
      }
   }

   public void drawCutin(Dungeon dungeon, Room room, float sx, float sz, byte sdimx, byte sdimz, boolean invertX, boolean invertZ) {
      byte xbegin;
      byte xend;
      if (invertX) {
         xbegin = (byte)((int)(sx - (float)sdimx * this.xdim / 2.0F - this.xcoord * (float)sdimx));
         xend = (byte)((int)(sx + (float)sdimx * this.xdim / 2.0F - this.xcoord * (float)sdimx));
      } else {
         xbegin = (byte)((int)(sx - (float)sdimx * this.xdim / 2.0F + this.xcoord * (float)sdimx));
         xend = (byte)((int)(sx + (float)sdimx * this.xdim / 2.0F + this.xcoord * (float)sdimx));
      }

      byte zbegin;
      byte zend;
      if (invertZ) {
         zbegin = (byte)((int)(sz - (float)sdimz * this.zdim / 2.0F - this.zcoord * (float)sdimz));
         zend = (byte)((int)(sz + (float)sdimz * this.zdim / 2.0F - this.zcoord * (float)sdimz));
      } else {
         zbegin = (byte)((int)(sz - (float)sdimz * this.zdim / 2.0F + this.zcoord * (float)sdimz));
         zend = (byte)((int)(sz + (float)sdimz * this.zdim / 2.0F + this.zcoord * (float)sdimz));
      }

      for (byte k = zbegin; k < zend; k++) {
         for (byte j = xbegin; j < xend; j++) {
            if (j >= 0 && j < dungeon.size.width && k >= 0 && k < dungeon.size.width) {
               dungeon.map.isWall[j][k] = false;
            }
         }
      }
   }

   public void drawPlatform(Dungeon dungeon, Room room, byte floorY, float sx, float sz, float sdimx, float sdimz, boolean invertX, boolean invertZ) {
      byte xbegin;
      byte xend;
      if (invertX) {
         xbegin = (byte)((int)(sx - sdimx * this.xdim / 2.0F - this.xcoord * sdimx));
         xend = (byte)((int)(sx + sdimx * this.xdim / 2.0F - this.xcoord * sdimx));
      } else {
         xbegin = (byte)((int)(sx - sdimx * this.xdim / 2.0F + this.xcoord * sdimx));
         xend = (byte)((int)(sx + sdimx * this.xdim / 2.0F + this.xcoord * sdimx));
      }

      byte zbegin;
      byte zend;
      if (invertZ) {
         zbegin = (byte)((int)(sz - sdimz * this.zdim / 2.0F - this.zcoord * sdimz));
         zend = (byte)((int)(sz + sdimz * this.zdim / 2.0F - this.zcoord * sdimz));
      } else {
         zbegin = (byte)((int)(sz - sdimz * this.zdim / 2.0F + this.zcoord * sdimz));
         zend = (byte)((int)(sz + sdimz * this.zdim / 2.0F + this.zcoord * sdimz));
      }

      for (byte k = zbegin; k < zend; k++) {
         for (byte j = xbegin; j < xend; j++) {
            if (j >= 0 && j < dungeon.size.width && k >= 0 && k < dungeon.size.width && dungeon.map.room[j][k] == room.id) {
               dungeon.map.floorY[j][k] = floorY;
               dungeon.map.hasLiquid[j][k] = false;
            }
         }
      }
   }

   public float getxdim() {
      return this.xdim;
   }

   public float getzdim() {
      return this.zdim;
   }

   public float getxcoord() {
      return this.xcoord;
   }

   public float getzcoord() {
      return this.zcoord;
   }
}
