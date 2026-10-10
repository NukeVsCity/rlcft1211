package jaredbgreat.dldungeons.pieces;

import jaredbgreat.dldungeons.planner.Symmetry;
import java.util.Random;

public enum Shapes {
   X(1, 1, Shape.xgroup),
   L(2, 2, Shape.lgroup),
   O(3, 3, Shape.ogroup),
   T(3, 3, Shape.tgroup),
   F(4, 5, Shape.fgroup),
   E(4, 5, Shape.egroup),
   I(3, 3, Shape.igroup),
   C(3, 3, Shape.cgroup),
   U(3, 3, Shape.ugroup),
   S(5, 5, Shape.sgroup);

   public final int minx;
   public final int miny;
   public final Shape[] family;

   private Shapes(int minx, int miny, Shape[] family) {
      this.minx = minx;
      this.miny = miny;
      this.family = family;
   }

   public static Shapes wholeShape(Symmetry sym, Random random) {
      switch (sym) {
         case NONE:
            return Symmetry.allPart[random.nextInt(Symmetry.allPart.length)];
         case R:
         case SW:
            return Symmetry.rotatedPart[random.nextInt(Symmetry.rotatedPart.length)];
         case TR1:
         case TR2:
            return Symmetry.transPart[random.nextInt(Symmetry.transPart.length)];
         case X:
            return Symmetry.xsymmetricPart[random.nextInt(Symmetry.xsymmetricPart.length)];
         case XZ:
            return Symmetry.xysymmetricPart[random.nextInt(Symmetry.xysymmetricPart.length)];
         case Z:
            return Symmetry.ysymmetricPart[random.nextInt(Symmetry.ysymmetricPart.length)];
         default:
            return Symmetry.allPart[random.nextInt(Symmetry.allPart.length)];
      }
   }
}
