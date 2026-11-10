package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** 1.12 MyrmexAIReEnterHive: go to the closest entrance, then down to its bottom. */
public class MyrmexAIReEnterHive extends Goal {
   private final EntityMyrmexBase myrmex;
   private final double movementSpeed;
   private Path path;
   private BlockPos nextEntrance = BlockPos.ZERO;
   private boolean first = true;
   private MyrmexHive hive;

   public MyrmexAIReEnterHive(EntityMyrmexBase entityIn, double movementSpeedIn) {
      this.myrmex = entityIn;
      this.movementSpeed = movementSpeedIn;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (this.myrmex.canMove() && !this.myrmex.shouldLeaveHive() && this.myrmex.canSeeSky() && this.first) {
         MyrmexHive village = this.myrmex.getHive();
         if (village == null) {
            village = MyrmexWorldData.get(this.myrmex.level()).getNearestHive(this.myrmex.blockPosition(), 500);
         }

         if (village == null) {
            return false;
         } else {
            this.hive = village;
            this.nextEntrance = MyrmexHive.getGroundedPos(this.myrmex.level(), this.hive.getClosestEntranceToEntity(this.myrmex, this.myrmex.getRandom(), false));
            this.path = this.myrmex.getNavigation().createPath(this.nextEntrance, 0);
            this.first = true;
            return this.path != null;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      if (this.first) {
         this.hive.setWorld(this.myrmex.level());
         this.nextEntrance = MyrmexHive.getGroundedPos(this.myrmex.level(), this.hive.getClosestEntranceToEntity(this.myrmex, this.myrmex.getRandom(), false));
      }

      this.path = this.myrmex.getNavigation().createPath(this.nextEntrance, 0);
      this.myrmex.getNavigation().moveTo(this.path, this.movementSpeed);
      if (this.myrmex.distanceToSqr(Vec3.atLowerCornerOf(this.nextEntrance)) < 9.0 && this.first && this.hive != null) {
         this.nextEntrance = this.hive.getClosestEntranceBottomToEntity(this.myrmex, this.myrmex.getRandom());
         this.first = false;
         this.myrmex.getNavigation().stop();
         this.path = this.myrmex.getNavigation().createPath(this.nextEntrance, 0);
         this.myrmex.getNavigation().moveTo(this.path, this.movementSpeed);
      }

      if (this.myrmex.distanceToSqr(Vec3.atLowerCornerOf(this.nextEntrance)) < 15.0 && !this.first) {
         this.myrmex.isEnteringHive = false;
      } else {
         this.myrmex.isEnteringHive = true;
      }
   }

   @Override
   public boolean canContinueToUse() {
      return this.myrmex.distanceToSqr(Vec3.atLowerCornerOf(this.nextEntrance)) < 15.0 && !this.first ? false : this.myrmex.shouldEnterHive();
   }

   @Override
   public void stop() {
      this.nextEntrance = BlockPos.ZERO;
      this.myrmex.getNavigation().moveTo((Path)null, this.movementSpeed);
      this.first = true;
   }
}
