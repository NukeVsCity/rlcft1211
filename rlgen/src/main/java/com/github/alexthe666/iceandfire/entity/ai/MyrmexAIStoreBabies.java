package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexWorker;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.structures.WorldGenMyrmexHive;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** 1.12 MyrmexAIStoreBabies: carry a larva/pupa/egg to a nursery. */
public class MyrmexAIStoreBabies extends EntityAIBase {
   private final EntityMyrmexWorker myrmex;
   private final double movementSpeed;
   private BlockPos nextRoom = BlockPos.ZERO;

   public MyrmexAIStoreBabies(EntityMyrmexWorker entityIn, double movementSpeedIn) {
      this.myrmex = entityIn;
      this.movementSpeed = movementSpeedIn;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      if (this.myrmex.canMove() && this.myrmex.holdingBaby() && (this.myrmex.shouldEnterHive() || this.myrmex.getNavigation().isDone()) && !this.myrmex.canSeeSky()) {
         MyrmexHive village = this.myrmex.getHive();
         if (village == null) {
            return false;
         } else {
            this.nextRoom = MyrmexHive.getGroundedPos(
                  this.myrmex.level(), village.getRandomRoom(WorldGenMyrmexHive.RoomType.NURSERY, this.myrmex.getRandom(), this.myrmex.blockPosition())
               )
               .above();
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return this.myrmex.holdingBaby()
         && !this.myrmex.getNavigation().isDone()
         && this.myrmex.distanceToSqr(Vec3.atLowerCornerOf(this.nextRoom)) > 3.0
         && this.myrmex.shouldEnterHive();
   }

   @Override
   public void updateTask() {
      this.myrmex.getNavigation().moveTo((double)this.nextRoom.getX(), (double)this.nextRoom.getY(), (double)this.nextRoom.getZ(), this.movementSpeed);
      if (this.nextRoom != null && this.myrmex.distanceToSqr(Vec3.atLowerCornerOf(this.nextRoom)) < 4.0 && this.myrmex.holdingBaby() && !this.myrmex.getPassengers().isEmpty()) {
         for (Entity entity : new ArrayList<>(this.myrmex.getPassengers())) {
            entity.stopRiding();
            this.resetTask();
            entity.copyPosition(this.myrmex);
         }
      }
   }

   @Override
   public void resetTask() {
      this.nextRoom = BlockPos.ZERO;
      this.myrmex.getNavigation().moveTo((Path)null, this.movementSpeed);
   }
}
