package com.nukevscity.rlgen.stronghold;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/** 1.12 StructureBoundingBox (mutable, inclusive). */
public final class SBox {
   public int minX;
   public int minY;
   public int minZ;
   public int maxX;
   public int maxY;
   public int maxZ;

   public SBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      this.minX = minX;
      this.minY = minY;
      this.minZ = minZ;
      this.maxX = maxX;
      this.maxY = maxY;
      this.maxZ = maxZ;
   }

   /** The populate box: Y 1..512. */
   public SBox(int minX, int minZ, int maxX, int maxZ) {
      this(minX, 1, minZ, maxX, 512, maxZ);
   }

   public SBox(SBox b) {
      this(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ);
   }

   public static SBox getNewBoundingBox() {
      return new SBox(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
   }

   public static SBox getComponentToAddBoundingBox(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, Direction facing) {
      switch (facing) {
         case NORTH:
            return new SBox(i + l, j + i1, k - i2 + 1 + j1, i + k1 - 1 + l, j + l1 - 1 + i1, k + j1);
         case SOUTH:
            return new SBox(i + l, j + i1, k + j1, i + k1 - 1 + l, j + l1 - 1 + i1, k + i2 - 1 + j1);
         case WEST:
            return new SBox(i - i2 + 1 + j1, j + i1, k + l, i + j1, j + l1 - 1 + i1, k + k1 - 1 + l);
         case EAST:
            return new SBox(i + j1, j + i1, k + l, i + i2 - 1 + j1, j + l1 - 1 + i1, k + k1 - 1 + l);
         default:
            return new SBox(i + l, j + i1, k + j1, i + k1 - 1 + l, j + l1 - 1 + i1, k + i2 - 1 + j1);
      }
   }

   public boolean intersectsWith(SBox b) {
      return this.maxX >= b.minX && this.minX <= b.maxX && this.maxZ >= b.minZ && this.minZ <= b.maxZ && this.maxY >= b.minY && this.minY <= b.maxY;
   }

   public boolean intersectsWith(int minX, int minZ, int maxX, int maxZ) {
      return this.maxX >= minX && this.minX <= maxX && this.maxZ >= minZ && this.minZ <= maxZ;
   }

   public void expandTo(SBox b) {
      this.minX = Math.min(this.minX, b.minX);
      this.minY = Math.min(this.minY, b.minY);
      this.minZ = Math.min(this.minZ, b.minZ);
      this.maxX = Math.max(this.maxX, b.maxX);
      this.maxY = Math.max(this.maxY, b.maxY);
      this.maxZ = Math.max(this.maxZ, b.maxZ);
   }

   public void offset(int x, int y, int z) {
      this.minX += x;
      this.minY += y;
      this.minZ += z;
      this.maxX += x;
      this.maxY += y;
      this.maxZ += z;
   }

   public boolean isVecInside(Vec3i v) {
      return v.getX() >= this.minX && v.getX() <= this.maxX && v.getZ() >= this.minZ && v.getZ() <= this.maxZ && v.getY() >= this.minY && v.getY() <= this.maxY;
   }

   public int getXSize() {
      return this.maxX - this.minX + 1;
   }

   public int getYSize() {
      return this.maxY - this.minY + 1;
   }

   public int getZSize() {
      return this.maxZ - this.minZ + 1;
   }

   @Override
   public String toString() {
      return "(" + this.minX + "," + this.minY + "," + this.minZ + " .. " + this.maxX + "," + this.maxY + "," + this.maxZ + ")";
   }
}
