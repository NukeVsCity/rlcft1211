package com.lycanitesmobs.client.model.template;

import com.lycanitesmobs.client.model.ModelCreatureObj;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelTemplateInsect extends ModelCreatureObj {
   public double mouthScaleX = 0.0;
   public double mouthScaleY = 1.0;

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
      if (partName.equals("mouthleft") || partName.equals("antennaleft") || partName.equals("antennaleftfront") || partName.equals("antennarightback")) {
         rotX = (float)((double)rotX + this.mouthScaleX * -Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F - 0.05F)));
         rotY = (float)((double)rotY + this.mouthScaleY * -Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F - 0.05F)));
      }

      if (partName.equals("mouthright") || partName.equals("antennaright") || partName.equals("antennarightfront") || partName.equals("antennaleftback")) {
         rotX = (float)((double)rotX + this.mouthScaleX * -Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F - 0.05F)));
         rotY = (float)((double)rotY - this.mouthScaleY * -Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F - 0.05F)));
      }

      if (partName.equals("tail")) {
         rotX = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.05F)));
         rotY = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)));
      }

      if (partName.equals("wingleft") || partName.equals("winglefttop") || partName.equals("wingleftbottom") || partName.equals("wingrightmiddle")) {
         rotX = 20.0F;
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 3.2F) * 0.6F)));
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.sin(loop * 3.2F) * 0.6F)));
      }

      if (partName.equals("wingright") || partName.equals("wingrighttop") || partName.equals("wingrightbottom") || partName.equals("wingleftmiddle")) {
         rotX = 20.0F;
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 3.2F) * 0.6F)));
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.sin(loop * 3.2F + (float)Math.PI) * 0.6F)));
      }

      float walkSwing = 0.3F;
      if (partName.equals("legrightfront") || partName.equals("legleftmiddle") || partName.equals("legrightback")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.3331F + (float)Math.PI) * walkSwing * distance)));
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
      }

      if (partName.equals("legleftfront") || partName.equals("legrightmiddle") || partName.equals("legleftback")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.3331F) * walkSwing * distance)));
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      if (entity != null && !entity.onGround() && !entity.isInWater() && entity instanceof BaseCreatureEntity) {
         BaseCreatureEntity entityCreature = (BaseCreatureEntity)entity;
         if (entityCreature.isFlying() && partName.equals("body")) {
            float bob = -Mth.sin(loop * 0.2F) * 0.3F;
            if (bob < 0.0F) {
               bob = -bob;
            }

            posY += bob;
         }
      }

      if (partName.equals("leftmouth")) {
         rotX = (float)((double)rotX + this.mouthScaleX * 15.0 * (double)this.getAttackProgress());
         rotY = (float)((double)rotY - this.mouthScaleY * 15.0 * (double)this.getAttackProgress());
      }

      if (partName.equals("rightmouth")) {
         rotX = (float)((double)rotX + this.mouthScaleX * 15.0 * (double)this.getAttackProgress());
         rotY = (float)((double)rotY + this.mouthScaleY * 15.0 * (double)this.getAttackProgress());
      }

      if (partName.equals("tail")) {
         rotX += 45.0F * this.getAttackProgress();
      }

      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
