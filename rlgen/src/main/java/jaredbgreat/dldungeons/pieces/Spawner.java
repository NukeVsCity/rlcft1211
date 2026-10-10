package jaredbgreat.dldungeons.pieces;

public final class Spawner {
   private final int x;
   private final int y;
   private final int z;
   private final int room;
   private final int level;
   private final String mob;

   public Spawner(int x, int y, int z, int room, int level, String mob) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.room = room;
      this.level = level;
      this.mob = mob;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getZ() {
      return this.z;
   }

   public int getRoom() {
      return this.room;
   }

   public int getLevel() {
      return this.level;
   }

   public String getMob() {
      return this.mob;
   }
}
