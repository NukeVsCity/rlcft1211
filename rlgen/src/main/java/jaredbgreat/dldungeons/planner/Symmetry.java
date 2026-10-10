package jaredbgreat.dldungeons.planner;

import jaredbgreat.dldungeons.pieces.Shapes;

public enum Symmetry {
   NONE(0, 0, false, false, false),
   X(1, 1, true, false, false),
   Z(2, 1, false, true, false),
   XZ(3, 2, true, true, false),
   TR1(4, 1, false, false, true),
   TR2(5, 1, false, false, true),
   R(6, 1, false, false, true),
   SW(7, 2, true, true, false);

   public final int ID;
   public final int level;
   public final boolean halfX;
   public final boolean halfZ;
   public final boolean doubler;
   public static final Shapes[] allshapes = new Shapes[]{Shapes.X, Shapes.L, Shapes.O, Shapes.T, Shapes.F, Shapes.E, Shapes.I, Shapes.C, Shapes.U, Shapes.S};
   public static final Shapes[] xsymmetics = new Shapes[]{Shapes.X, Shapes.O, Shapes.E, Shapes.I, Shapes.C};
   public static final Shapes[] ysymmetics = new Shapes[]{Shapes.X, Shapes.O, Shapes.T, Shapes.I, Shapes.C, Shapes.U};
   public static final Shapes[] xysymmetrics = new Shapes[]{Shapes.X, Shapes.O, Shapes.I, Shapes.C};
   public static final Shapes[] transshapes = new Shapes[]{Shapes.X, Shapes.L, Shapes.O, Shapes.C, Shapes.U};
   public static final Shapes[] rotateds = new Shapes[]{Shapes.X, Shapes.O, Shapes.C, Shapes.S};
   public static final Shapes[] allPart = new Shapes[]{Shapes.L, Shapes.O, Shapes.T, Shapes.F, Shapes.E, Shapes.I, Shapes.C, Shapes.U, Shapes.S};
   public static final Shapes[] xsymmetricPart = new Shapes[]{Shapes.O, Shapes.E, Shapes.I, Shapes.C};
   public static final Shapes[] ysymmetricPart = new Shapes[]{Shapes.O, Shapes.T, Shapes.I, Shapes.C, Shapes.U};
   public static final Shapes[] xysymmetricPart = new Shapes[]{Shapes.O, Shapes.I, Shapes.C};
   public static final Shapes[] transPart = new Shapes[]{Shapes.L, Shapes.O, Shapes.C, Shapes.U};
   public static final Shapes[] rotatedPart = new Shapes[]{Shapes.X, Shapes.O, Shapes.C, Shapes.S};
   public static final Shapes[] allSolids = new Shapes[]{Shapes.X, Shapes.L, Shapes.T, Shapes.F, Shapes.E, Shapes.I, Shapes.C, Shapes.U, Shapes.S};
   public static final Shapes[] xsymmetricSolid = new Shapes[]{Shapes.X, Shapes.E, Shapes.I, Shapes.C};
   public static final Shapes[] ysymmetricSolid = new Shapes[]{Shapes.X, Shapes.T, Shapes.I, Shapes.C, Shapes.U};
   public static final Shapes[] xysymmetricSolid = new Shapes[]{Shapes.X, Shapes.I, Shapes.C};
   public static final Shapes[] transSolid = new Shapes[]{Shapes.X, Shapes.L, Shapes.C, Shapes.U};
   public static final Shapes[] rotatedSolid = new Shapes[]{Shapes.X, Shapes.C, Shapes.S};

   private Symmetry(int ID, int level, boolean halfX, boolean halfZ, boolean doubler) {
      this.ID = ID;
      this.level = level;
      this.halfX = halfX;
      this.halfZ = halfZ;
      this.doubler = doubler;
   }

   public static Symmetry getSymmetry(Dungeon dungeon) {
      int num = 0;
      if (dungeon.symmetry.use(dungeon.random)) {
         num++;
      }

      if (dungeon.symmetry.use(dungeon.random)) {
         num++;
      }

      if (num == 0) {
         return NONE;
      } else {
         int which = dungeon.random.nextInt(4 / num);
         if (num == 1) {
            switch (which) {
               case 0:
                  return X;
               case 1:
                  return Z;
               case 2:
                  if (dungeon.random.nextBoolean()) {
                     return TR1;
                  }

                  return TR2;
               case 3:
                  return R;
               default:
                  return NONE;
            }
         } else {
            switch (which) {
               case 0:
                  return XZ;
               case 1:
                  return SW;
               default:
                  return NONE;
            }
         }
      }
   }
}
