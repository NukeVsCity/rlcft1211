package com.github.alexthe666.iceandfire.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 1.12 PathNavigateMyrmex: always navigates; waypoint radius 0.49 and the 1.12 shortcut to the furthest node on the same level. */
public class PathNavigateMyrmex extends GroundPathNavigation {
   public PathNavigateMyrmex(Mob myrmex, Level worldIn) {
      super(myrmex, worldIn);
   }

   @Override
   protected boolean canUpdatePath() {
      return true;
   }

   @Override
   protected void followThePath() {
      Vec3 vec3d = this.getTempMobPos();
      int i = this.path.getNodeCount();

      for (int j = this.path.getNextNodeIndex(); j < this.path.getNodeCount(); j++) {
         if ((double)this.path.getNode(j).y != Math.floor(vec3d.y)) {
            i = j;
            break;
         }
      }

      this.maxDistanceToWaypoint = 0.49F;
      Vec3 vec3d1 = Vec3.atLowerCornerOf(this.path.getNextNodePos());
      if (Mth.abs((float)(this.mob.getX() - (vec3d1.x + 0.5))) < this.maxDistanceToWaypoint
         && Mth.abs((float)(this.mob.getZ() - (vec3d1.z + 0.5))) < this.maxDistanceToWaypoint
         && Math.abs(this.mob.getY() - vec3d1.y) < 1.0) {
         this.path.setNextNodeIndex(this.path.getNextNodeIndex() + 1);
      }

      for (int j1 = i - 1; j1 >= this.path.getNextNodeIndex(); j1--) {
         if (this.canMoveDirectly(vec3d, this.path.getEntityPosAtNode(this.mob, j1))) {
            this.path.setNextNodeIndex(j1);
            break;
         }
      }

      this.doStuckDetection(vec3d);
   }
}
