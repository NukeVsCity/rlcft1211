package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexQueen;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexWorker;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** 1.12 MyrmexAILeaveHive: walk out through the closest entrance. */
public class MyrmexAILeaveHive extends Goal {
   private final EntityMyrmexBase myrmex;
   private final double movementSpeed;
   private Path path;
   private BlockPos nextEntrance = BlockPos.ZERO;

   public MyrmexAILeaveHive(EntityMyrmexBase entityIn, double movementSpeedIn) {
      this.myrmex = entityIn;
      this.movementSpeed = movementSpeedIn;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (this.myrmex instanceof EntityMyrmexQueen) {
         return false;
      } else if (this.myrmex.isChild()) {
         return false;
      } else if (this.myrmex.canMove()
         && this.myrmex.shouldLeaveHive()
         && !this.myrmex.canSeeSky()
         && (
            !(this.myrmex instanceof EntityMyrmexWorker)
               || !((EntityMyrmexWorker)this.myrmex).holdingBaby() && this.myrmex.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
         )
         && !this.myrmex.isEnteringHive) {
         MyrmexHive village = MyrmexWorldData.get(this.myrmex.level()).getNearestHive(this.myrmex.blockPosition(), 1000);
         if (village == null) {
            return false;
         } else {
            this.nextEntrance = MyrmexHive.getGroundedPos(this.myrmex.level(), village.getClosestEntranceToEntity(this.myrmex, this.myrmex.getRandom(), true));
            this.path = this.myrmex.getNavigation().createPath(this.nextEntrance, 0);
            return this.path != null;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean canContinueToUse() {
      double d = this.myrmex.distanceToSqr(Vec3.atLowerCornerOf(this.nextEntrance));
      return !(d <= 3.0) && !this.myrmex.shouldEnterHive()
         ? !this.myrmex.getNavigation().isDone() && d > 3.0 && this.myrmex.shouldLeaveHive()
         : false;
   }

   @Override
   public void start() {
      this.myrmex.getNavigation().moveTo(this.path, this.movementSpeed);
   }

   @Override
   public void stop() {
      this.nextEntrance = BlockPos.ZERO;
      this.myrmex.getNavigation().moveTo((Path)null, this.movementSpeed);
   }
}
