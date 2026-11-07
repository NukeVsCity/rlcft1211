package com.nukevscity.rlgen.vanilla.monument;

import net.minecraft.core.Vec3i;

/** 1.12.2 StructureBoundingBox (the parts the monument uses). */
public class StructureBoundingBox {
   public int minX;
   public int minY;
   public int minZ;
   public int maxX;
   public int maxY;
   public int maxZ;

   public StructureBoundingBox() {
   }

   public static StructureBoundingBox getNewBoundingBox() {
      return new StructureBoundingBox(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
   }

   public static StructureBoundingBox createProper(int i, int j, int k, int l, int i1, int j1) {
      return new StructureBoundingBox(Math.min(i, l), Math.min(j, i1), Math.min(k, j1), Math.max(i, l), Math.max(j, i1), Math.max(k, j1));
   }

   public StructureBoundingBox(int i, int j, int k, int l, int i1, int j1) {
      this.minX = i;
      this.minY = j;
      this.minZ = k;
      this.maxX = l;
      this.maxY = i1;
      this.maxZ = j1;
   }

   public StructureBoundingBox(int i, int j, int k, int l) {
      this.minX = i;
      this.minZ = j;
      this.maxX = k;
      this.maxZ = l;
      this.minY = 1;
      this.maxY = 512;
   }

   public boolean intersectsWith(StructureBoundingBox structureboundingbox1) {
      return this.maxX >= structureboundingbox1.minX
         && this.minX <= structureboundingbox1.maxX
         && this.maxZ >= structureboundingbox1.minZ
         && this.minZ <= structureboundingbox1.maxZ
         && this.maxY >= structureboundingbox1.minY
         && this.minY <= structureboundingbox1.maxY;
   }

   public boolean intersectsWith(int i, int j, int k, int l) {
      return this.maxX >= i && this.minX <= k && this.maxZ >= j && this.minZ <= l;
   }

   public void expandTo(StructureBoundingBox structureboundingbox1) {
      this.minX = Math.min(this.minX, structureboundingbox1.minX);
      this.minY = Math.min(this.minY, structureboundingbox1.minY);
      this.minZ = Math.min(this.minZ, structureboundingbox1.minZ);
      this.maxX = Math.max(this.maxX, structureboundingbox1.maxX);
      this.maxY = Math.max(this.maxY, structureboundingbox1.maxY);
      this.maxZ = Math.max(this.maxZ, structureboundingbox1.maxZ);
   }

   public void offset(int i, int j, int k) {
      this.minX += i;
      this.minY += j;
      this.minZ += k;
      this.maxX += i;
      this.maxY += j;
      this.maxZ += k;
   }

   public boolean isVecInside(Vec3i vec3i) {
      return vec3i.getX() >= this.minX
         && vec3i.getX() <= this.maxX
         && vec3i.getZ() >= this.minZ
         && vec3i.getZ() <= this.maxZ
         && vec3i.getY() >= this.minY
         && vec3i.getY() <= this.maxY;
   }
}
