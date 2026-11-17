package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelCreatureObj;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelGorger extends ModelCreatureObj {
   public ModelGorger() {
      this(1.0F);
   }

   public ModelGorger(float shadowSize) {
      this.initModel("gorger", LycanitesMobs.modInfo, "entity/gorger");
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      float posX = 0.0F;
      float posY = 0.0F;
      float posZ = 0.0F;
      float angleX = 0.0F;
      float angleY = 0.0F;
      float angleZ = 0.0F;
      float rotation = 0.0F;
      float rotX = 0.0F;
      float rotY = 0.0F;
      float rotZ = 0.0F;
      float scaleX = 1.0F;
      float scaleY = 1.0F;
      float scaleZ = 1.0F;
      if (!(scale < 0.0F)) {
         if (partName.equals("mouthtop")) {
            this.rotate(-((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)))), 0.0F, 0.0F);
         }

         if (partName.equals("mouthleft")) {
            rotY += (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F - 0.05F)));
         }

         if (partName.equals("mouthright")) {
            rotY -= (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F - 0.05F)));
         }

         if (partName.equals("mouthbottom")) {
            this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F))), 0.0F, 0.0F);
         }

         if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
            if (partName.equals("mouthtop")) {
               rotX += -20.0F;
            }

            if (partName.equals("mouthleft")) {
               rotY -= 15.0F;
            }

            if (partName.equals("mouthright")) {
               rotY += 15.0F;
            }

            if (partName.equals("mouthbottom")) {
               rotX += 20.0F;
            }
         }

         float walkSwing = 0.8F;
         if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).getStealth() > 0.0F && ((BaseCreatureEntity)entity).getStealth() < 1.0F) {
            time = loop;
         }

         time /= 2.0F;
         if (partName.equals("head")) {
            posX += Mth.sin((time - walkSwing) * walkSwing) * walkSwing;
         }

         if (partName.equals("body")) {
            posX += Mth.sin(time * walkSwing) * walkSwing;
            float parentX = Mth.sin((time - walkSwing) * walkSwing) * walkSwing;
            rotY = (float)((double)rotY + this.rotateToPoint(0.0, (double)posX, -0.6F, (double)parentX));
         }

         if (partName.equals("body01")) {
            posX += Mth.sin((time + walkSwing) * walkSwing) * walkSwing;
            float parentX = Mth.sin(time * walkSwing) * walkSwing;
            rotY = (float)((double)rotY + this.rotateToPoint(0.0, (double)posX, -0.6F, (double)parentX));
         }

         if (partName.equals("body02")) {
            posX += Mth.sin((time + walkSwing * 2.0F) * walkSwing) * walkSwing;
            float parentX = Mth.sin((time + walkSwing) * walkSwing) * walkSwing;
            rotY = (float)((double)rotY + this.rotateToPoint(0.0, (double)posX, -0.6F, (double)parentX));
         }

         if (partName.equals("body03")) {
            posX += Mth.sin((time + walkSwing * 3.0F) * walkSwing) * walkSwing;
            float parentX = Mth.sin((time + walkSwing * 2.0F) * walkSwing) * walkSwing;
            rotY = (float)((double)rotY + this.rotateToPoint(0.0, (double)posX, -0.6F, (double)parentX));
         }

         if (partName.equals("body04")) {
            posX += Mth.sin((time + walkSwing * 4.0F) * walkSwing) * walkSwing;
            float parentX = Mth.sin((time + walkSwing * 3.0F) * walkSwing) * walkSwing;
            rotY = (float)((double)rotY + this.rotateToPoint(0.0, (double)posX, -0.6F, (double)parentX));
         }

         if (partName.equals("body05")) {
            posX += Mth.sin((time + walkSwing * 5.0F) * walkSwing) * walkSwing;
            float parentX = Mth.sin((time + walkSwing * 4.0F) * walkSwing) * walkSwing;
            rotY = (float)((double)rotY + this.rotateToPoint(0.0, (double)posX, -0.6F, (double)parentX));
         }

         if (partName.equals("body06")) {
            posX += Mth.sin((time + walkSwing * 6.0F) * walkSwing) * walkSwing;
            float parentX = Mth.sin((time + walkSwing * 5.0F) * walkSwing) * walkSwing;
            rotY = (float)((double)rotY + this.rotateToPoint(0.0, (double)posX, -0.6F, (double)parentX));
         }

         if (partName.equals("body07")) {
            posX += Mth.sin((time + walkSwing * 7.0F) * walkSwing) * walkSwing;
            float parentX = Mth.sin((time + walkSwing * 6.0F) * walkSwing) * walkSwing;
            rotY = (float)((double)rotY + this.rotateToPoint(0.0, (double)posX, -0.6F, (double)parentX));
         }

         if (partName.equals("body08")) {
            posX += Mth.sin((time + walkSwing * 8.0F) * walkSwing) * walkSwing;
            float parentX = Mth.sin((time + walkSwing * 7.0F) * walkSwing) * walkSwing;
            rotY = (float)((double)rotY + this.rotateToPoint(0.0, (double)posX, -0.6F, (double)parentX));
         }

         if (partName.equals("body09")) {
            posX += Mth.sin((time + walkSwing * 9.0F) * walkSwing) * walkSwing;
            float parentX = Mth.sin((time + walkSwing * 8.0F) * walkSwing) * walkSwing;
            rotY = (float)((double)rotY + this.rotateToPoint(0.0, (double)posX, -0.6F, (double)parentX));
         }

         if (entity instanceof BaseCreatureEntity) {
            posY -= 2.0F * ((BaseCreatureEntity)entity).getStealth();
         }

         this.translate(posX, posY, posZ);
         this.angle(rotation, angleX, angleY, angleZ);
         this.rotate(rotX, rotY, rotZ);
         this.scale(scaleX, scaleY, scaleZ);
      }
   }
}
