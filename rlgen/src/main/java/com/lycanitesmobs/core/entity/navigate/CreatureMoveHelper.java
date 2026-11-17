package com.lycanitesmobs.core.entity.navigate;

import com.lycanitesmobs.compat.LycMotion;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.player.Player;

/** Lycanites 1.12 CreatureMoveHelper (EntityMoveHelper: onUpdateMoveHelper = tick, posX/Y/Z = wantedX/Y/Z, action = operation). */
public class CreatureMoveHelper extends MoveControl {
   protected BaseCreatureEntity entityCreature;
   protected int courseChangeCooldown;

   public CreatureMoveHelper(BaseCreatureEntity baseCreatureEntity) {
      super(baseCreatureEntity);
      this.entityCreature = baseCreatureEntity;
   }

   @Override
   public void tick() {
      if (!this.isControlledByRider()) {
         if (this.entityCreature.isStrongSwimmer() && this.entityCreature.isInWater()) {
            this.onUpdateSwimming();
         } else if (this.entityCreature.isFlying() && !this.entityCreature.isInWater()) {
            this.onUpdateFlying();
         } else {
            super.tick();
         }
      }
   }

   public boolean isControlledByRider() {
      return this.entityCreature != null && this.entityCreature.getControllingPassenger() instanceof Player && this.entityCreature.canBeSteered();
   }

   public double getSpeed() {
      return this.speedModifier;
   }

   public void onUpdateSwimming() {
      if (this.operation == MoveControl.Operation.MOVE_TO && !this.entityCreature.getNavigation().isDone()) {
         double x = this.wantedX - this.entityCreature.getX();
         double y = this.wantedY - this.entityCreature.getY();
         double z = this.wantedZ - this.entityCreature.getZ();
         double distance = x * x + y * y + z * z;
         distance = (double)(float)Math.sqrt(distance);
         y /= distance;
         float f = (float)(Mth.atan2(z, x) * (180.0 / Math.PI)) - 90.0F;
         this.entityCreature.setYRot(this.rotlerp(this.entityCreature.getYRot(), f, 90.0F));
         this.entityCreature.yBodyRot = this.entityCreature.getYRot();
         float f1 = (float)(this.speedModifier * this.entityCreature.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
         this.entityCreature.setSpeed(this.entityCreature.getSpeed() + (f1 - this.entityCreature.getSpeed()) * 0.125F);
         double d4 = Math.sin((double)(this.entityCreature.tickCount + this.entityCreature.getId()) * 0.5) * 0.05;
         double d5 = Math.cos((double)(this.entityCreature.getYRot() * (float)(Math.PI / 180.0)));
         double d6 = Math.sin((double)(this.entityCreature.getYRot() * (float)(Math.PI / 180.0)));
         LycMotion.addX(this.entityCreature, d4 * d5);
         LycMotion.addZ(this.entityCreature, d4 * d6);
         d4 = Math.sin((double)(this.entityCreature.tickCount + this.entityCreature.getId()) * 0.75) * 0.05;
         LycMotion.addY(this.entityCreature, d4 * (d6 + d5) * 0.25);
         LycMotion.setY(this.entityCreature, this.entityCreature.getDeltaMovement().y + (double)this.entityCreature.getSpeed() * y * 0.1);
         LookControl entitylookhelper = this.entityCreature.getLookControl();
         double d7 = this.entityCreature.getX() + x / distance * 2.0;
         double d8 = (double)this.entityCreature.getEyeHeight() + this.entityCreature.getY() + y / distance;
         double d9 = this.entityCreature.getZ() + z / distance * 2.0;
         double d10 = entitylookhelper.getWantedX();
         double d11 = entitylookhelper.getWantedY();
         double d12 = entitylookhelper.getWantedZ();
         if (!entitylookhelper.isLookingAtTarget()) {
            d10 = d7;
            d11 = d8;
            d12 = d9;
         }

         this.entityCreature.getLookControl().setLookAt(d10 + (d7 - d10) * 0.125, d11 + (d8 - d11) * 0.125, d12 + (d9 - d12) * 0.125, 10.0F, 40.0F);
      } else {
         this.entityCreature.setSpeed(0.0F);
      }
   }

   public void onUpdateFlying() {
      if (this.operation == MoveControl.Operation.MOVE_TO) {
         double xDistance = this.wantedX - this.entityCreature.getX();
         double yDistance = this.wantedY - this.entityCreature.getY();
         double zDistance = this.wantedZ - this.entityCreature.getZ();
         double distance = xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
         if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown = this.courseChangeCooldown + this.entityCreature.getRandom().nextInt(5) + 2;
            distance = (double)(float)Math.sqrt(distance);
            if (distance >= 1.0) {
               this.entityCreature.setSpeed((float)this.entityCreature.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
               double speed = (double)this.entityCreature.getSpeed() / 2.4 * this.getSpeed();
               LycMotion.addX(this.entityCreature, xDistance / distance * speed);
               LycMotion.addY(this.entityCreature, yDistance / distance * speed);
               LycMotion.addZ(this.entityCreature, zDistance / distance * speed);
            } else {
               this.operation = MoveControl.Operation.WAIT;
            }
         }
      }

      if (this.entityCreature.getTarget() != null) {
         LivingEntity entitylivingbase = this.entityCreature.getTarget();
         double distanceX = entitylivingbase.getX() - this.entityCreature.getX();
         double distanceZ = entitylivingbase.getZ() - this.entityCreature.getZ();
         float yaw = -((float)Mth.atan2(distanceX, distanceZ)) * (180.0F / (float)Math.PI);
         this.entityCreature.setYRot(yaw);
         this.entityCreature.yBodyRot = yaw;
      } else if (this.operation == MoveControl.Operation.MOVE_TO) {
         float yaw = -((float)Mth.atan2(this.entityCreature.getDeltaMovement().x, this.entityCreature.getDeltaMovement().z)) * (180.0F / (float)Math.PI);
         this.entityCreature.setYRot(yaw);
         this.entityCreature.yBodyRot = yaw;
      }
   }
}
