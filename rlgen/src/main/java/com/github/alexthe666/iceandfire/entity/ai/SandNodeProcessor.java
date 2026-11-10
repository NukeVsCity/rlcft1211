package com.github.alexthe666.iceandfire.entity.ai;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;

/** 1.12 SandNodeProcessor: every node is open, six neighbours (the worm swims through anything while in sand). */
public class SandNodeProcessor extends NodeEvaluator {
   @Override
   public Node getStart() {
      return this.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ));
   }

   @Override
   public Target getTarget(double x, double y, double z) {
      return new Target(this.getNode(Mth.floor(x - 0.4), Mth.floor(y + 0.5), Mth.floor(z - 0.4)));
   }

   @Override
   public int getNeighbors(Node[] pathOptions, Node currentPoint) {
      int i = 0;

      for (Direction enumfacing : Direction.values()) {
         Node pathpoint = this.getWaterNode(currentPoint.x + enumfacing.getStepX(), currentPoint.y + enumfacing.getStepY(), currentPoint.z + enumfacing.getStepZ());
         if (pathpoint != null && !pathpoint.closed) {
            pathOptions[i++] = pathpoint;
         }
      }

      return i;
   }

   @Override
   public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
      return PathType.OPEN;
   }

   @Override
   public PathType getPathType(PathfindingContext context, int x, int y, int z) {
      return PathType.OPEN;
   }

   @Nullable
   private Node getWaterNode(int x, int y, int z) {
      return this.getNode(x, y, z);
   }
}
