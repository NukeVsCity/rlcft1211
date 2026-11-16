package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelSerpix extends ModelObjOld {
   public ModelSerpix() {
      this(1.0F);
   }

   public ModelSerpix(float shadowSize) {
      this.initModel("Serpix", LycanitesMobs.modInfo, "entity/serpix");
      this.setPartCenter("head", 0.0F, 0.5F, 4.1F);
      this.setPartCenter("mouthleft", 0.6F, 0.5F, 4.2F);
      this.setPartCenter("mouthright", -0.6F, 0.5F, 4.2F);
      this.setPartCenter("mouthbottom", 0.0F, 0.2F, 4.3F);
      this.setPartCenter("body", 0.0F, 0.5F, 3.2F);
      this.setPartCenter("body01", 0.0F, 0.5F, 2.3F);
      this.setPartCenter("body02", 0.0F, 0.5F, 1.4F);
      this.setPartCenter("body03", 0.0F, 0.5F, 0.5F);
      this.setPartCenter("body04", 0.0F, 0.5F, -0.4F);
      this.setPartCenter("body05", 0.0F, 0.5F, -1.3F);
      this.setPartCenter("body06", 0.0F, 0.5F, -2.2F);
      this.setPartCenter("body07", 0.0F, 0.5F, -3.1F);
      this.setPartCenter("body08", 0.0F, 0.5F, -4.0F);
      this.setPartCenter("body09", 0.0F, 0.5F, -4.9F);
      this.lockHeadX = true;
      this.lockHeadY = true;
      this.trophyScale = 0.6F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      float pi = (float)Math.PI;
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
         if (partName.contains("mouth")) {
            this.centerPartToPart(partName, "head");
            if (!this.lockHeadX) {
               this.rotate((float)Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))), 0.0F, 0.0F);
            }

            if (!this.lockHeadY) {
               this.rotate(0.0F, (float)Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))), 0.0F);
            }

            this.uncenterPartToPart(partName, "head");
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
         if (partName.equals("head") || partName.contains("mouth")) {
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
         this.rotate(rotation, angleX, angleY, angleZ);
         this.rotate(rotX, rotY, rotZ);
         this.scale(scaleX, scaleY, scaleZ);
      }
   }
}
