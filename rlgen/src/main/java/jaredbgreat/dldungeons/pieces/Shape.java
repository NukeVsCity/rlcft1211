package jaredbgreat.dldungeons.pieces;

import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.rooms.Room;

public final class Shape {
   private final Rectangle[] rectangles;
   public static final Shape simpleRect = new Shape(new Rectangle[]{Rectangle.simple});
   public static final Shape X = simpleRect;
   public static final Shape[] xgroup = new Shape[]{X, X, X, X};
   public static final Shape L000 = new Shape(new Rectangle[]{Rectangle.lback000, Rectangle.lbottom000});
   public static final Shape L090 = new Shape(new Rectangle[]{Rectangle.lback090, Rectangle.lbottom090});
   public static final Shape L180 = new Shape(new Rectangle[]{Rectangle.lback180, Rectangle.lbottom180});
   public static final Shape L270 = new Shape(new Rectangle[]{Rectangle.lback270, Rectangle.lbottom270});
   public static final Shape[] lgroup = new Shape[]{L000, L090, L180, L270};
   public static final Shape O = new Shape(new Rectangle[]{Rectangle.obottom, Rectangle.oleft, Rectangle.oright, Rectangle.otop});
   public static final Shape[] ogroup = new Shape[]{O, O, O, O};
   public static final Shape T000 = new Shape(new Rectangle[]{Rectangle.ttop000, Rectangle.tbottom000});
   public static final Shape T090 = new Shape(new Rectangle[]{Rectangle.ttop090, Rectangle.tbottom090});
   public static final Shape T180 = new Shape(new Rectangle[]{Rectangle.ttop180, Rectangle.tbottom180});
   public static final Shape T270 = new Shape(new Rectangle[]{Rectangle.ttop270, Rectangle.tbottom270});
   public static final Shape[] tgroup = new Shape[]{T000, T090, T180, T270};
   public static final Shape F000 = new Shape(new Rectangle[]{Rectangle.eback000, Rectangle.etop000, Rectangle.emiddle000});
   public static final Shape F090 = new Shape(new Rectangle[]{Rectangle.eback090, Rectangle.etop090, Rectangle.emiddle090});
   public static final Shape F180 = new Shape(new Rectangle[]{Rectangle.eback180, Rectangle.etop180, Rectangle.emiddle180});
   public static final Shape F270 = new Shape(new Rectangle[]{Rectangle.eback270, Rectangle.etop270, Rectangle.emiddle270});
   public static final Shape[] fgroup = new Shape[]{F000, F090, F180, F270};
   public static final Shape E000 = new Shape(new Rectangle[]{Rectangle.eback000, Rectangle.etop000, Rectangle.emiddle000, Rectangle.ebottom000});
   public static final Shape E090 = new Shape(new Rectangle[]{Rectangle.eback090, Rectangle.etop090, Rectangle.emiddle090, Rectangle.ebottom090});
   public static final Shape E180 = new Shape(new Rectangle[]{Rectangle.eback180, Rectangle.etop180, Rectangle.emiddle180, Rectangle.ebottom180});
   public static final Shape E270 = new Shape(new Rectangle[]{Rectangle.eback270, Rectangle.etop270, Rectangle.emiddle270, Rectangle.ebottom270});
   public static final Shape[] egroup = new Shape[]{E000, E090, E180, E270};
   public static final Shape I000 = new Shape(new Rectangle[]{Rectangle.itop, Rectangle.imiddle, Rectangle.ibottom});
   public static final Shape I090 = new Shape(new Rectangle[]{Rectangle.ileft, Rectangle.imiddle, Rectangle.iright});
   public static final Shape I180 = I000;
   public static final Shape I270 = I090;
   public static final Shape[] igroup = new Shape[]{I000, I090, I180, I270};
   public static final Shape CROSS = new Shape(new Rectangle[]{Rectangle.crosstop, Rectangle.crossmiddle, Rectangle.crossbottom});
   public static final Shape[] cgroup = new Shape[]{CROSS, CROSS, CROSS, CROSS};
   public static final Shape U000 = new Shape(new Rectangle[]{Rectangle.uleft000, Rectangle.uright000, Rectangle.ubottom000});
   public static final Shape U090 = new Shape(new Rectangle[]{Rectangle.uleft090, Rectangle.uright090, Rectangle.ubottom090});
   public static final Shape U180 = new Shape(new Rectangle[]{Rectangle.uleft180, Rectangle.uright180, Rectangle.ubottom180});
   public static final Shape U270 = new Shape(new Rectangle[]{Rectangle.uleft270, Rectangle.uright270, Rectangle.ubottom270});
   public static final Shape[] ugroup = new Shape[]{U000, U090, U180, U270};
   public static final Shape S000 = new Shape(
      new Rectangle[]{Rectangle.stop000, Rectangle.smiddle000, Rectangle.sbottom000, Rectangle.sleft000, Rectangle.sright000}
   );
   public static final Shape S090 = new Shape(
      new Rectangle[]{Rectangle.stop090, Rectangle.smiddle090, Rectangle.sbottom090, Rectangle.sleft090, Rectangle.sright090}
   );
   public static final Shape S180 = S000;
   public static final Shape S270 = S090;
   public static final Shape[] sgroup = new Shape[]{S000, S090, S180, S270};
   public static final Shape[][] allshapes = new Shape[][]{xgroup, lgroup, ogroup, tgroup, fgroup, egroup, igroup, cgroup, ugroup, sgroup};
   public static final Shape[][] allSolids = new Shape[][]{xgroup, lgroup, tgroup, fgroup, egroup, ugroup};

   public Shape(Rectangle[] rectangles) {
      this.rectangles = rectangles;
   }

   public void drawLiquid(Dungeon dungeon, Room room, float sx, float sz, float sdimx, float sdimz, boolean invertX, boolean invertZ) {
      for (Rectangle rect : this.rectangles) {
         rect.drawLiquid(dungeon, room, sx, sz, sdimx, sdimz, invertX, invertZ);
      }
   }

   public void drawWalkway(Dungeon dungeon, Room room, float sx, float sz, byte sdimx, byte sdimz, boolean invertX, boolean invertZ) {
      for (Rectangle rect : this.rectangles) {
         rect.drawWalkway(dungeon, room, sx, sz, sdimx, sdimz, invertX, invertZ);
      }
   }

   public void drawCutout(Dungeon dungeon, Room room, float sx, float sz, float sdimx, float sdimz, boolean invertX, boolean invertZ) {
      for (Rectangle rect : this.rectangles) {
         rect.drawCutout(dungeon, room, sx, sz, sdimx, sdimz, invertX, invertZ);
      }
   }

   public void drawCutin(Dungeon dungeon, Room room, float sx, float sz, byte sdimx, byte sdimz, boolean invertX, boolean invertZ) {
      for (Rectangle rect : this.rectangles) {
         rect.drawCutin(dungeon, room, sx, sz, sdimx, sdimz, invertX, invertZ);
      }
   }

   public void drawPlatform(Dungeon dungeon, Room room, byte floorY, float sx, float sz, float sdimx, float sdimz, boolean invertX, boolean invertZ) {
      for (Rectangle rect : this.rectangles) {
         rect.drawPlatform(dungeon, room, floorY, sx, sz, sdimx, sdimz, invertX, invertZ);
      }
   }
}
