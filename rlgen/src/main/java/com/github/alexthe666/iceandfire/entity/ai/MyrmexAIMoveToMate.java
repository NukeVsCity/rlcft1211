package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexRoyal;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

/** 1.12 MyrmexAIMoveToMate */
public class MyrmexAIMoveToMate extends Goal {
   private final EntityMyrmexRoyal myrmex;
   private final double movementSpeed;

   public MyrmexAIMoveToMate(EntityMyrmexRoyal entityIn, double movementSpeedIn) {
      this.myrmex = entityIn;
      this.movementSpeed = movementSpeedIn;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      return this.myrmex.canMove() && this.myrmex.getTarget() == null && this.myrmex.mate != null && this.myrmex.canSeeSky();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      if (this.myrmex.mate != null && (this.myrmex.distanceTo(this.myrmex.mate) > 30.0F || this.myrmex.getNavigation().isDone())) {
         this.myrmex.getMoveControl().setWantedPosition(this.myrmex.mate.getX(), this.myrmex.getY(), this.myrmex.mate.getZ(), this.movementSpeed);
      }
   }

   @Override
   public boolean canContinueToUse() {
      return this.myrmex.canMove()
         && this.myrmex.getTarget() == null
         && this.myrmex.mate != null
         && this.myrmex.mate.isAlive()
         && (this.myrmex.distanceTo(this.myrmex.mate) < 15.0F || !this.myrmex.getNavigation().isDone());
   }
}
