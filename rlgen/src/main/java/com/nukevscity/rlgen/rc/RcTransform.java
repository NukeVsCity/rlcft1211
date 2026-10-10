package com.nukevscity.rlgen.rc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

/** IvToolkit AxisAlignedTransform2D: mirror the X axis first, then rotate clockwise. */
public record RcTransform(int rotation, boolean mirrorX) {
   public static final RcTransform ORIGINAL = new RcTransform(0, false);
   private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

   public static RcTransform from(int rotation, boolean mirrorX) {
      return new RcTransform((rotation % 4 + 4) % 4, mirrorX);
   }

   public BlockPos apply(int x, int y, int z, int[] size) {
      int px = this.mirrorX ? size[0] - 1 - x : x;
      return switch (this.rotation) {
         case 0 -> new BlockPos(px, y, z);
         case 1 -> new BlockPos(size[2] - 1 - z, y, px);
         case 2 -> new BlockPos(size[0] - 1 - px, y, size[2] - 1 - z);
         default -> new BlockPos(z, y, size[0] - 1 - px);
      };
   }

   public BlockPos apply(BlockPos p, int[] size) {
      return this.apply(p.getX(), p.getY(), p.getZ(), size);
   }

   /** Entity positions (centerCorrection 0). */
   public double[] apply(double[] p, int[] size) {
      double px = this.mirrorX ? size[0] - p[0] : p[0];
      return switch (this.rotation) {
         case 0 -> new double[]{px, p[1], p[2]};
         case 1 -> new double[]{size[2] - p[2], p[1], px};
         case 2 -> new double[]{size[0] - px, p[1], size[2] - p[2]};
         default -> new double[]{p[2], p[1], size[0] - px};
      };
   }

   public int[] applySize(int[] size) {
      return this.rotation % 2 == 1 ? new int[]{size[2], size[1], size[0]} : size.clone();
   }

   public Direction apply(Direction d) {
      if (d.getAxis() == Direction.Axis.Y) {
         return d;
      }
      if (this.mirrorX && d.getAxis() == Direction.Axis.X) {
         d = d.getOpposite();
      }
      return HORIZONTAL[(d.get2DDataValue() + 2 + this.rotation) % 4];
   }

   /** Directions.rotate */
   public static Direction rotate(Direction d, RcTransform t) {
      return t.apply(d);
   }

   public BlockState apply(BlockState state) {
      if (this.mirrorX) {
         state = state.mirror(Mirror.FRONT_BACK);
      }
      return this.rotation == 0 ? state : state.rotate(Rotation.values()[this.rotation]);
   }

   public float applyYaw(float yaw) {
      if (this.mirrorX) {
         yaw = -yaw;
      }
      return yaw + 90.0F * this.rotation;
   }

   /** Directions.getHorizontalClockwiseRotations */
   public static Integer clockwiseRotations(Direction source, Direction dest, boolean mirrorX) {
      if (source == dest) {
         return mirrorX && dest.getAxis() == Direction.Axis.X ? 2 : 0;
      }
      int src = indexOf(source);
      int dst = indexOf(dest);
      if (src < 0 || dst < 0) {
         return null;
      }
      int mirrorRotations = mirrorX && source.getAxis() == Direction.Axis.X ? 2 : 0;
      return (dst - src + mirrorRotations + 4) % 4;
   }

   private static int indexOf(Direction d) {
      for (int i = 0; i < 4; i++) {
         if (HORIZONTAL[i] == d) {
            return i;
         }
      }
      return -1;
   }
}
