package jaredbgreat.dldungeons.planner.mapping;

public class Tile {
   public int x;
   public int z;

   public Tile(int x, int z) {
      this.x = x;
      this.z = z;
   }

   public boolean equals(Tile other) {
      return this.x == other.x && this.z == other.z;
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof Tile ? this.equals((Tile)other) : false;
   }

   @Override
   public int hashCode() {
      return this.x + (this.z << 8) + (this.z << 16) + (this.x << 24);
   }
}
