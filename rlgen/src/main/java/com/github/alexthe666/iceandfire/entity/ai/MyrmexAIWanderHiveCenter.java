package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import net.minecraft.core.BlockPos;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import net.minecraft.world.level.pathfinder.Path;

/** 1.12 MyrmexAIWanderHiveCenter */
public class MyrmexAIWanderHiveCenter extends EntityAIBase {
   private final EntityMyrmexBase myrmex;
   private final double movementSpeed;
   private Path path;
   private BlockPos target = BlockPos.ZERO;

   public MyrmexAIWanderHiveCenter(EntityMyrmexBase entityIn, double movementSpeedIn) {
      this.myrmex = entityIn;
      this.movementSpeed = movementSpeedIn;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      if (this.myrmex.canMove() && (this.myrmex.shouldEnterHive() || this.myrmex.getNavigation().isDone()) && !this.myrmex.canSeeSky()) {
         MyrmexHive village = MyrmexWorldData.get(this.myrmex.level()).getNearestHive(this.myrmex.blockPosition(), 300);
         if (village == null) {
            village = this.myrmex.getHive();
         }

         if (village == null) {
            return false;
         } else {
            this.target = this.getNearPos(MyrmexHive.getGroundedPos(this.myrmex.level(), village.getCenter()));
            this.path = this.myrmex.getNavigation().createPath(this.target, 0);
            return this.path != null;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.myrmex.getNavigation().isDone() && this.myrmex.distanceToSqr(net.minecraft.world.phys.Vec3.atLowerCornerOf(this.target)) > 3.0 && this.myrmex.shouldEnterHive();
   }

   @Override
   public void startExecuting() {
      this.myrmex.getNavigation().moveTo(this.path, this.movementSpeed);
   }

   @Override
   public void resetTask() {
      this.target = BlockPos.ZERO;
      this.myrmex.getNavigation().moveTo((Path)null, this.movementSpeed);
   }

   public BlockPos getNearPos(BlockPos pos) {
      return pos.offset(this.myrmex.getRandom().nextInt(15) - 7, 0, this.myrmex.getRandom().nextInt(15) - 7);
   }
}
