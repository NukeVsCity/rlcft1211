package com.nukevscity.rlgen.vanilla.mansion;

/** 1.12 StructureBoundingBox, the parts the mansion uses (mutable, inclusive, min may pass max like 1.12). */
final class Box {
   int minX;
   int minY;
   int minZ;
   int maxX;
   int maxY;
   int maxZ;

   Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      this.minX = minX;
      this.minY = minY;
      this.minZ = minZ;
      this.maxX = maxX;
      this.maxY = maxY;
      this.maxZ = maxZ;
   }

   /** StructureBoundingBox(x0, z0, x1, z1): Y 1..512 */
   static Box columns(int minX, int minZ, int maxX, int maxZ) {
      return new Box(minX, 1, minZ, maxX, 512, maxZ);
   }

   static Box getNewBoundingBox() {
      return new Box(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
   }

   boolean intersectsWith(Box b) {
      return this.maxX >= b.minX && this.minX <= b.maxX && this.maxZ >= b.minZ && this.minZ <= b.maxZ && this.maxY >= b.minY && this.minY <= b.maxY;
   }

   boolean intersectsWith(int minX, int minZ, int maxX, int maxZ) {
      return this.maxX >= minX && this.minX <= maxX && this.maxZ >= minZ && this.minZ <= maxZ;
   }

   void expandTo(Box b) {
      this.minX = Math.min(this.minX, b.minX);
      this.minY = Math.min(this.minY, b.minY);
      this.minZ = Math.min(this.minZ, b.minZ);
      this.maxX = Math.max(this.maxX, b.maxX);
      this.maxY = Math.max(this.maxY, b.maxY);
      this.maxZ = Math.max(this.maxZ, b.maxZ);
   }

   void offset(int x, int y, int z) {
      this.minX += x;
      this.minY += y;
      this.minZ += z;
      this.maxX += x;
      this.maxY += y;
      this.maxZ += z;
   }

   boolean isVecInside(int x, int y, int z) {
      return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ && y >= this.minY && y <= this.maxY;
   }

   boolean isVecInside(net.minecraft.core.Vec3i p) {
      return this.isVecInside(p.getX(), p.getY(), p.getZ());
   }

   net.minecraft.world.level.levelgen.structure.BoundingBox toModern() {
      return new net.minecraft.world.level.levelgen.structure.BoundingBox(Math.min(this.minX, this.maxX), Math.min(this.minY, this.maxY),
         Math.min(this.minZ, this.maxZ), Math.max(this.minX, this.maxX), Math.max(this.minY, this.maxY), Math.max(this.minZ, this.maxZ));
   }
}
