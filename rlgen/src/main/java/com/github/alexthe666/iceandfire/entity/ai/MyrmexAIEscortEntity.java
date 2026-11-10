package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexSoldier;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

/** 1.12 MyrmexAIEscortEntity */
public class MyrmexAIEscortEntity extends Goal {
   private final EntityMyrmexSoldier myrmex;
   private final double movementSpeed;

   public MyrmexAIEscortEntity(EntityMyrmexSoldier entityIn, double movementSpeedIn) {
      this.myrmex = entityIn;
      this.movementSpeed = movementSpeedIn;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      return this.myrmex.canMove()
         && this.myrmex.getTarget() == null
         && this.myrmex.guardingEntity != null
         && (this.myrmex.guardingEntity.canSeeSky() || !this.myrmex.canSeeSky())
         && !this.myrmex.isEnteringHive;
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      if (this.myrmex.guardingEntity != null && (this.myrmex.distanceTo(this.myrmex.guardingEntity) > 30.0F || this.myrmex.getNavigation().isDone())) {
         this.myrmex.getNavigation().moveTo(this.myrmex.guardingEntity, this.movementSpeed);
      }
   }

   @Override
   public boolean canContinueToUse() {
      return this.myrmex.canMove()
         && this.myrmex.getTarget() == null
         && this.myrmex.guardingEntity != null
         && this.myrmex.guardingEntity.isAlive()
         && (this.myrmex.distanceTo(this.myrmex.guardingEntity) < 15.0F || !this.myrmex.getNavigation().isDone())
         && this.myrmex.canSeeSky() == this.myrmex.guardingEntity.canSeeSky()
         && !this.myrmex.guardingEntity.canSeeSky();
   }
}
