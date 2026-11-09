package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexSentinel;
import com.github.alexthe666.iceandfire.entity.LandUtils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** 1.12 MyrmexAIFindHidingSpot: a sentinel on the surface walks away until no hidden sentinel is near, then hides. */
public class MyrmexAIFindHidingSpot extends Goal {
   private static final int RADIUS = 32;
   private final EntityMyrmexSentinel myrmex;
   private BlockPos targetBlock = BlockPos.ZERO;
   private int wanderRadius = 32;

   public MyrmexAIFindHidingSpot(EntityMyrmexSentinel myrmex) {
      this.myrmex = myrmex;
   }

   @Override
   public boolean canUse() {
      this.targetBlock = this.getTargetPosition(this.wanderRadius);
      return this.myrmex.canMove() && this.myrmex.getTarget() == null && this.myrmex.canSeeSky();
   }

   @Override
   public boolean canContinueToUse() {
      return !this.myrmex.shouldEnterHive() && this.myrmex.getNavigation().isDone();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      if (this.areMyrmexNear(32.0) || this.myrmex.isOnResin()) {
         this.myrmex.getNavigation().moveTo((double)this.targetBlock.getX() + 0.5, (double)this.targetBlock.getY(), (double)this.targetBlock.getZ() + 0.5, 1.0);
         if (this.myrmex.distanceToSqr(Vec3.atCenterOf(this.targetBlock)) < 2.0) {
            this.wanderRadius += 32;
            this.targetBlock = this.getTargetPosition(this.wanderRadius);
         }
      } else if (this.myrmex.getTarget() == null) {
         this.myrmex.setHiding(true);
         this.stop();
      }
   }

   @Override
   public void stop() {
      this.targetBlock = BlockPos.ZERO;
      this.wanderRadius = 32;
   }

   protected AABB getTargetableArea(double targetDistance) {
      return this.myrmex.getBoundingBox().inflate(targetDistance, 14.0, targetDistance);
   }

   public BlockPos getTargetPosition(int radius) {
      int x = (int)this.myrmex.getX() + this.myrmex.getRandom().nextInt(radius * 2) - radius;
      int z = (int)this.myrmex.getZ() + this.myrmex.getRandom().nextInt(radius * 2) - radius;
      return LandUtils.getHeight(this.myrmex.level(), new BlockPos(x, 0, z));
   }

   private boolean areMyrmexNear(double distance) {
      List<Entity> sentinels = this.myrmex.level().getEntities(this.myrmex, this.getTargetableArea(distance), e -> e instanceof EntityMyrmexSentinel);
      for (Entity sentinel : sentinels) {
         if (sentinel instanceof EntityMyrmexSentinel && ((EntityMyrmexSentinel)sentinel).isHiding()) {
            return true;
         }
      }

      return false;
   }
}
