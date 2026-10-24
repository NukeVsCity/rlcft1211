package com.yungnickyoung.minecraft.bettercaves.mc;

/** rlgen: horizontal 1.12 EnumFacing. */
public enum EnumFacing {
   NORTH(0, -1), SOUTH(0, 1), WEST(-1, 0), EAST(1, 0);

   final int dx;
   final int dz;

   EnumFacing(int dx, int dz) {
      this.dx = dx;
      this.dz = dz;
   }

   public EnumFacing rotateY() {
      return switch (this) {
         case NORTH -> EAST;
         case EAST -> SOUTH;
         case SOUTH -> WEST;
         case WEST -> NORTH;
      };
   }

   public EnumFacing rotateYCCW() {
      return switch (this) {
         case NORTH -> WEST;
         case WEST -> SOUTH;
         case SOUTH -> EAST;
         case EAST -> NORTH;
      };
   }

   public enum Plane implements Iterable<EnumFacing> {
      HORIZONTAL;

      /** 1.12 order: NORTH, EAST, SOUTH, WEST */
      private static final EnumFacing[] FACINGS = {NORTH, EAST, SOUTH, WEST};

      @Override
      public java.util.Iterator<EnumFacing> iterator() {
         return java.util.Arrays.asList(FACINGS).iterator();
      }
   }
}
