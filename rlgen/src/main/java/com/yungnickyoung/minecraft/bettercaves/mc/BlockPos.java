package com.yungnickyoung.minecraft.bettercaves.mc;

/** rlgen: the parts of 1.12 BlockPos Better Caves uses. */
public class BlockPos {
   protected int x;
   protected int y;
   protected int z;

   public BlockPos(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public BlockPos(double x, double y, double z) {
      this((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
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

   public BlockPos add(int dx, int dy, int dz) {
      return dx == 0 && dy == 0 && dz == 0 ? this : new BlockPos(this.x + dx, this.y + dy, this.z + dz);
   }

   public BlockPos offset(EnumFacing f, int n) {
      return n == 0 ? this : new BlockPos(this.x + f.dx * n, this.y, this.z + f.dz * n);
   }

   public BlockPos east() {
      return this.offset(EnumFacing.EAST, 1);
   }

   public BlockPos west() {
      return this.offset(EnumFacing.WEST, 1);
   }

   public BlockPos north() {
      return this.offset(EnumFacing.NORTH, 1);
   }

   public BlockPos south() {
      return this.offset(EnumFacing.SOUTH, 1);
   }

   public static class MutableBlockPos extends BlockPos {
      public MutableBlockPos() {
         super(0, 0, 0);
      }

      public MutableBlockPos setPos(BlockPos p) {
         this.x = p.x;
         this.y = p.y;
         this.z = p.z;
         return this;
      }

      public MutableBlockPos setPos(int x, int y, int z) {
         this.x = x;
         this.y = y;
         this.z = z;
         return this;
      }

      public MutableBlockPos move(EnumFacing f, int n) {
         this.x += f.dx * n;
         this.z += f.dz * n;
         return this;
      }
   }
}
