package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.ModelCreatureObj;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelTemplateQuadruped extends ModelCreatureObj {
   protected float legAnimationScale = 1.0F;

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
      if (partName.equals("mouth")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.1F - 0.1F))), 0.0F, 0.0F);
      }

      if (partName.contains("neck")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F))), 0.0F, 0.0F);
      }

      if (partName.contains("armleft")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.contains("armright")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.contains("tail")) {
         rotX = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.05F)));
         rotY = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)));
      }

      if (partName.equals("earleft")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.18F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.equals("earright")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.18F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (this.legAnimationScale != 0.0F) {
         float walkSwing = 0.6F * this.legAnimationScale;
         if (partName.contains("armleft") || partName.equals("wingright")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing) * 1.0F * distance * 0.5F)));
            rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(time * walkSwing) * 0.5F * distance * 0.5F)));
         }

         if (partName.contains("armright") || partName.equals("wingleft")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 1.0F * distance * 0.5F)));
            rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 0.5F * distance * 0.5F)));
         }

         if (partName.contains("legrightfront") || partName.contains("legleftback") || partName.contains("wingright")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
         }

         if (partName.contains("legleftfront") || partName.contains("legrightback") || partName.contains("wingleft")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
         }
      }

      if (entity != null && !entity.onGround() && !entity.isInWater()) {
         if (partName.equals("wingleft")) {
            rotX = 20.0F;
            rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.4F) * 0.6F)));
            rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.sin(loop * 0.4F) * 0.6F)));
         }

         if (partName.equals("wingright")) {
            rotX = 20.0F;
            rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.4F) * 0.6F)));
            rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.sin(loop * 0.4F + (float)Math.PI) * 0.6F)));
         }

         if (partName.contains("legleftback") || partName.contains("legrightback")) {
            rotX += 25.0F;
         }

         if (partName.contains("legleftfront") || partName.contains("legrightfront")) {
            rotX -= 25.0F;
         }
      }

      if (partName.equals("mouth")) {
         rotX -= 15.0F * this.getAttackProgress();
      }

      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
