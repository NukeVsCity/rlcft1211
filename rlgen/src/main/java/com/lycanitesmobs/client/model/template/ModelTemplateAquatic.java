package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.ModelCreatureObj;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelTemplateAquatic extends ModelCreatureObj {
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
      if (partName.equals("body")) {
         this.rotate(0.0F, (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.05F - 0.05F))), 0.0F);
      }

      if (partName.equals("mouth")) {
         this.rotate(5.0F - (float)(-Math.toDegrees((double)(Mth.cos(loop * -0.1F) * 0.1F))), 0.0F, 0.0F);
      }

      if (partName.equals("neck")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F))), 0.0F, 0.0F);
      }

      float speed = 0.5F;
      if (distance > 0.0F) {
         speed = 1.0F;
      }

      if (partName.equals("armleft") || partName.equals("legleftfront") || partName.equals("legrightback")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.25F * speed) * 0.1F)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F * speed) * 0.05F)));
      }

      if (partName.equals("armright") || partName.equals("legrightfront") || partName.equals("legleftback")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.25F * speed) * 0.1F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F * speed) * 0.05F)));
      }

      if (partName.equals("tail")) {
         rotY += (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.25F * speed) * 0.25F)));
      }

      if ((entity == null || entity.isInWater()) && partName.equals("body")) {
         rotY += (float)(-Math.toDegrees((double)(Mth.cos(time * 0.1F) * 0.2F)));
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

         if (partName.equals("legleftfront") || partName.equals("legleftback")) {
            rotZ -= 25.0F;
         }

         if (partName.equals("legrightfront") || partName.equals("legrightback")) {
            rotZ += 25.0F;
         }
      }

      if (partName.equals("mouth")) {
         rotX += 15.0F * this.getAttackProgress();
      }

      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
