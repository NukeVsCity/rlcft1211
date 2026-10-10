package com.nukevscity.rlgen.legacy;

/** 1.12.2 ChunkPrimer: 16x256x16, index x << 12 | z << 8 | y. Empty means air. */
public final class ChunkPrimer {
   public final byte[] data = new byte[65536];

   private static int getBlockIndex(int x, int y, int z) {
      return x << 12 | z << 8 | y;
   }

   public LBlock getBlockState(int x, int y, int z) {
      return LBlock.byId(this.data[getBlockIndex(x, y, z)]);
   }

   public void setBlockState(int x, int y, int z, LBlock state) {
      this.data[getBlockIndex(x, y, z)] = (byte)state.ordinal();
   }

   /** 1.12 findGroundBlockIdx-style helper: highest non-air y in a column, or -1. */
   public int topNonAir(int x, int z) {
      int base = x << 12 | z << 8;
      for (int y = 255; y >= 0; y--) {
         if (this.data[base | y] != 0) {
            return y;
         }
      }
      return -1;
   }
}
