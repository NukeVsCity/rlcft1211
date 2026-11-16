package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.ModelCreatureObj;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelTemplateArachnid extends ModelCreatureObj {
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
      if (partName.equals("mouthleft")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F + (float)Math.PI) * 0.05F - 0.05F))), 0.0F, 0.0F);
      }

      if (partName.equals("mouthright")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F))), 0.0F, 0.0F);
      }

      if (partName.equals("armleft")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.25F) * 0.1F)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.equals("armright")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.25F) * 0.1F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.equals("tail")) {
         rotX = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.05F)));
         rotY = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)));
      }

      if (partName.equals("wingleft")) {
         rotX = 20.0F;
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 3.2F) * 0.6F)));
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.sin(loop * 3.2F) * 0.6F)));
      }

      if (partName.equals("wingright")) {
         rotX = 20.0F;
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 3.2F) * 0.6F)));
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.sin(loop * 3.2F + (float)Math.PI) * 0.6F)));
      }

      float walkSwing = 0.6F;
      if (partName.equals("legrightfront") || partName.equals("legrightmiddleback") || partName.equals("legleftmiddlefront") || partName.equals("legleftback")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.3331F + (float)Math.PI) * walkSwing * distance)));
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
      }

      if (partName.equals("legleftfront") || partName.equals("legleftmiddleback") || partName.equals("legrightmiddlefront") || partName.equals("legrightback")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.3331F) * walkSwing * distance)));
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      if (entity != null && !entity.onGround() && !entity.isInWater() && entity instanceof BaseCreatureEntity) {
         BaseCreatureEntity entityCreature = (BaseCreatureEntity)entity;
         if (entityCreature.isFlying()) {
            float bob = -Mth.sin(loop * 0.2F) * 0.3F;
            if (bob < 0.0F) {
               bob = -bob;
            }

            posY += bob;
         }
      }

      if (partName.equals("mouth")) {
         rotX += 30.0F * this.getAttackProgress();
      }

      if (partName.equals("armleft")) {
         rotY += -25.0F * this.getAttackProgress();
      }

      if (partName.equals("armright")) {
         rotY += 25.0F * this.getAttackProgress();
      }

      if (partName.equals("tail")) {
         rotX += 45.0F * this.getAttackProgress();
      }

      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
