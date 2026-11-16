package com.lycanitesmobs.core.network;

/** 1.12 NetworkRegistry.TargetPoint (dimension id as in LycDim). */
public class TargetPoint {
   public final int dimension;
   public final double x;
   public final double y;
   public final double z;
   public final double range;

   public TargetPoint(int dimension, double x, double y, double z, double range) {
      this.dimension = dimension;
      this.x = x;
      this.y = y;
      this.z = z;
      this.range = range;
   }
}
