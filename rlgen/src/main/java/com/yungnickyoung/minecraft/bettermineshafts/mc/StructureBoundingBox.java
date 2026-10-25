package com.yungnickyoung.minecraft.bettermineshafts.mc;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/** 1.12 StructureBoundingBox (mutable). */
public class StructureBoundingBox {
   public int minX;
   public int minY;
   public int minZ;
   public int maxX;
   public int maxY;
   public int maxZ;

   public StructureBoundingBox() {
   }

   public StructureBoundingBox(int[] aint) {
      if (aint.length == 6) {
         this.minX = aint[0];
         this.minY = aint[1];
         this.minZ = aint[2];
         this.maxX = aint[3];
         this.maxY = aint[4];
         this.maxZ = aint[5];
      }
   }

   public static StructureBoundingBox getNewBoundingBox() {
      return new StructureBoundingBox(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
   }

   public static StructureBoundingBox getComponentToAddBoundingBox(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, Direction enumfacing) {
      switch (enumfacing) {
         case NORTH:
            return new StructureBoundingBox(i + l, j + i1, k - i2 + 1 + j1, i + k1 - 1 + l, j + l1 - 1 + i1, k + j1);
         case SOUTH:
            return new StructureBoundingBox(i + l, j + i1, k + j1, i + k1 - 1 + l, j + l1 - 1 + i1, k + i2 - 1 + j1);
         case WEST:
            return new StructureBoundingBox(i - i2 + 1 + j1, j + i1, k + l, i + j1, j + l1 - 1 + i1, k + k1 - 1 + l);
         case EAST:
            return new StructureBoundingBox(i + j1, j + i1, k + l, i + i2 - 1 + j1, j + l1 - 1 + i1, k + k1 - 1 + l);
         default:
            return new StructureBoundingBox(i + l, j + i1, k + j1, i + k1 - 1 + l, j + l1 - 1 + i1, k + i2 - 1 + j1);
      }
   }

   public static StructureBoundingBox createProper(int i, int j, int k, int l, int i1, int j1) {
      return new StructureBoundingBox(Math.min(i, l), Math.min(j, i1), Math.min(k, j1), Math.max(i, l), Math.max(j, i1), Math.max(k, j1));
   }

   public StructureBoundingBox(StructureBoundingBox b) {
      this(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ);
   }

   public StructureBoundingBox(int i, int j, int k, int l, int i1, int j1) {
      this.minX = i;
      this.minY = j;
      this.minZ = k;
      this.maxX = l;
      this.maxY = i1;
      this.maxZ = j1;
   }

   public StructureBoundingBox(Vec3i a, Vec3i b) {
      this.minX = Math.min(a.getX(), b.getX());
      this.minY = Math.min(a.getY(), b.getY());
      this.minZ = Math.min(a.getZ(), b.getZ());
      this.maxX = Math.max(a.getX(), b.getX());
      this.maxY = Math.max(a.getY(), b.getY());
      this.maxZ = Math.max(a.getZ(), b.getZ());
   }

   public StructureBoundingBox(int i, int j, int k, int l) {
      this.minX = i;
      this.minZ = j;
      this.maxX = k;
      this.maxZ = l;
      this.minY = 1;
      this.maxY = 512;
   }

   public boolean intersectsWith(StructureBoundingBox o) {
      return this.maxX >= o.minX && this.minX <= o.maxX && this.maxZ >= o.minZ && this.minZ <= o.maxZ && this.maxY >= o.minY && this.minY <= o.maxY;
   }

   public boolean intersectsWith(int i, int j, int k, int l) {
      return this.maxX >= i && this.minX <= k && this.maxZ >= j && this.minZ <= l;
   }

   public void expandTo(StructureBoundingBox o) {
      this.minX = Math.min(this.minX, o.minX);
      this.minY = Math.min(this.minY, o.minY);
      this.minZ = Math.min(this.minZ, o.minZ);
      this.maxX = Math.max(this.maxX, o.maxX);
      this.maxY = Math.max(this.maxY, o.maxY);
      this.maxZ = Math.max(this.maxZ, o.maxZ);
   }

   public void offset(int i, int j, int k) {
      this.minX += i;
      this.minY += j;
      this.minZ += k;
      this.maxX += i;
      this.maxY += j;
      this.maxZ += k;
   }

   public boolean isVecInside(Vec3i v) {
      return v.getX() >= this.minX && v.getX() <= this.maxX && v.getZ() >= this.minZ && v.getZ() <= this.maxZ && v.getY() >= this.minY && v.getY() <= this.maxY;
   }

   public Vec3i getLength() {
      return new Vec3i(this.maxX - this.minX, this.maxY - this.minY, this.maxZ - this.minZ);
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
      return "[" + this.minX + "," + this.minY + "," + this.minZ + " -> " + this.maxX + "," + this.maxY + "," + this.maxZ + "]";
   }
}
