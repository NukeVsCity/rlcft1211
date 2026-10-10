package greymerk.roguelike.util.mst;

import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import java.util.Random;

public class MSTPoint {
   private Coord position;
   private Coord adjusted;
   private int rank = 0;
   private MSTPoint parent;

   public MSTPoint(Coord pos, Random rand) {
      this.position = pos.copy();
      this.adjusted = pos.copy();
      this.adjusted.translate(Direction.randomCardinal(rand));
      this.parent = this;
   }

   public double distance(MSTPoint other) {
      return this.adjusted.distance(other.adjusted);
   }

   public Coord getPosition() {
      return this.position.copy();
   }

   public int getRank() {
      return this.rank;
   }

   public void incRank() {
      this.rank++;
   }

   public MSTPoint getParent() {
      return this.parent;
   }

   public void setParent(MSTPoint p) {
      this.parent = p;
   }

   public void scaleBy(double multiplier) {
      double x = (double)this.position.getX();
      double y = (double)this.position.getY();
      double z = (double)this.position.getZ();
      x *= multiplier;
      z *= multiplier;
      x = Math.floor(x);
      z = Math.floor(z);
      this.position = new Coord((int)x, (int)y, (int)z);
   }
}
