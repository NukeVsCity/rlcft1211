package jaredbgreat.dldungeons.themes;

public enum Sizes implements Autoselectable {
   TINY(5, 39, 42, 12, 2, 2),
   SMALL(7, 55, 50, 16, 3, 2),
   MEDIUM(9, 71, 58, 20, 4, 2),
   LARGE(11, 87, 82, 22, 5, 3),
   HUGE(13, 103, 112, 24, 6, 4);

   public final int chunkWidth;
   public final int width;
   public final int chunkRadius;
   public final int radius;
   public final int maxRooms;
   public final int maxRoomSize;
   public final int maxNodes;
   public final int minNodes;

   private Sizes(int d, int r, int mr, int mrs, int maxn, int minn) {
      this.chunkWidth = d;
      this.width = d * 16;
      this.chunkRadius = d / 2;
      this.radius = r;
      this.maxRooms = mr;
      this.maxRoomSize = mrs;
      this.maxNodes = maxn;
      this.minNodes = minn;
   }
}
