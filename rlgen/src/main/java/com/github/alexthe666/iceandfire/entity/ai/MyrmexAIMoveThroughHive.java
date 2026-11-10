package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexWorker;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** 1.12 MyrmexAIMoveThroughHive: walk to a random room of the nearest hive while underground. */
public class MyrmexAIMoveThroughHive extends EntityAIBase {
   private final EntityMyrmexBase myrmex;
   private final double movementSpeed;
   private Path path;
   private BlockPos nextRoom = BlockPos.ZERO;

   public MyrmexAIMoveThroughHive(EntityMyrmexBase entityIn, double movementSpeedIn) {
      this.myrmex = entityIn;
      this.movementSpeed = movementSpeedIn;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      if (this.myrmex.canMove()
         && (!(this.myrmex instanceof EntityMyrmexWorker) || !((EntityMyrmexWorker)this.myrmex).holdingBaby())
         && this.myrmex.shouldMoveThroughHive()
         && (this.myrmex.shouldEnterHive() || this.myrmex.getNavigation().isDone())
         && !this.myrmex.canSeeSky()
         && (this.myrmex.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || !(this.myrmex instanceof EntityMyrmexWorker))) {
         MyrmexHive village = MyrmexWorldData.get(this.myrmex.level()).getNearestHive(this.myrmex.blockPosition(), 300);
         if (village == null) {
            village = this.myrmex.getHive();
         }

         if (village == null) {
            return false;
         } else {
            this.nextRoom = MyrmexHive.getGroundedPos(this.myrmex.level(), village.getRandomRoom(this.myrmex.getRandom(), this.myrmex.blockPosition()));
            this.path = this.myrmex.getNavigation().createPath(this.nextRoom, 0);
            return this.path != null;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.myrmex.getNavigation().isDone()
         && this.myrmex.distanceToSqr(Vec3.atLowerCornerOf(this.nextRoom)) > 3.0
         && this.myrmex.shouldEnterHive()
         && (!(this.myrmex instanceof EntityMyrmexWorker) || !((EntityMyrmexWorker)this.myrmex).holdingBaby());
   }

   @Override
   public void startExecuting() {
      this.myrmex.getNavigation().moveTo(this.path, this.movementSpeed);
   }

   @Override
   public void resetTask() {
      this.nextRoom = BlockPos.ZERO;
      this.myrmex.getNavigation().moveTo((Path)null, this.movementSpeed);
   }
}
