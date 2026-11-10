package com.github.alexthe666.iceandfire.entity.ai.legacy;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;

/** 1.12.2 EntityMoveHelper as 1.21 MoveControl, with the 1.12 code's direct access to the action field. */
public class LegacyMoveControl extends MoveControl {
   public LegacyMoveControl(Mob mob) {
      super(mob);
   }

   /** moveHelper.action = Action.WAIT */
   public void setWait() {
      this.operation = Operation.WAIT;
   }

   public boolean isMoveTo() {
      return this.operation == Operation.MOVE_TO;
   }

   public boolean isJumping() {
      return this.operation == Operation.JUMPING;
   }

   /** EntityMoveHelper.isUpdating */
   public boolean isUpdating() {
      return this.operation == Operation.MOVE_TO;
   }

   public void setSpeed(double speed) {
      this.speedModifier = speed;
   }

   public double getSpeed() {
      return this.speedModifier;
   }

   /** EntityMoveHelper.limitAngle */
   public static float limitAngle(float sourceAngle, float targetAngle, float maximumChange) {
      float f = net.minecraft.util.Mth.wrapDegrees(targetAngle - sourceAngle);
      if (f > maximumChange) {
         f = maximumChange;
      }
      if (f < -maximumChange) {
         f = -maximumChange;
      }
      float f1 = sourceAngle + f;
      if (f1 < 0.0F) {
         f1 += 360.0F;
      } else if (f1 > 360.0F) {
         f1 -= 360.0F;
      }
      return f1;
   }
}
