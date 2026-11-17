package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelTarantula extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelTarantula() {
      this(1.0F);
   }

   public ModelTarantula(float shadowSize) {
      this.initModel("Tarantula", LycanitesMobs.modInfo, "entity/tarantula");
      this.setPartCenter("head", 0.0F, 0.3F, 0.2F);
      this.setPartCenter("innerleftmouth", 0.07F, 0.3F, 0.35F);
      this.setPartCenter("innerrightmouth", -0.07F, 0.3F, 0.35F);
      this.setPartCenter("outerleftmouth", 0.29F, 0.3F, 0.25F);
      this.setPartCenter("outerrightmouth", -0.29F, 0.3F, 0.25F);
      this.setPartCenter("body", 0.0F, 0.3F, 0.2F);
      this.setPartCenter("frontleftleg", 0.25F, 0.3F, 0.17F);
      this.setPartCenter("middlefrontleftleg", 0.25F, 0.3F, 0.05F);
      this.setPartCenter("middlebackleftleg", 0.25F, 0.3F, -0.07F);
      this.setPartCenter("backleftleg", 0.25F, 0.3F, -0.19F);
      this.setPartCenter("frontrightleg", -0.25F, 0.3F, 0.17F);
      this.setPartCenter("middlefrontrightleg", -0.25F, 0.3F, 0.05F);
      this.setPartCenter("middlebackrightleg", -0.25F, 0.3F, -0.07F);
      this.setPartCenter("backrightleg", -0.25F, 0.3F, -0.19F);
      this.lockHeadX = true;
      this.lockHeadY = true;
      this.trophyScale = 1.0F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.35F};
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
      if (partName.equals("innerleftmouth")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F + (float)Math.PI) * 0.05F - 0.05F))), 0.0F, 0.0F);
      }

      if (partName.equals("innerrightmouth")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F))), 0.0F, 0.0F);
      }

      if (partName.equals("outerleftmouth")) {
         rotY += (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F - 0.05F)));
      }

      if (partName.equals("outerrightmouth")) {
         rotY -= (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F - 0.05F)));
      }

      float walkSwing = 0.6F;
      if (partName.equals("frontrightleg") || partName.equals("middlebackrightleg") || partName.equals("middlefrontleftleg") || partName.equals("backleftleg")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.3331F + (float)Math.PI) * walkSwing * distance)));
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
      }

      if (partName.equals("frontleftleg") || partName.equals("middlebackleftleg") || partName.equals("middlefrontrightleg") || partName.equals("backrightleg")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.3331F) * walkSwing * distance)));
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("innerleftmouth") || partName.equals("innerrightmouth")) {
            rotX += 20.0F;
         }

         if (partName.equals("outerleftmouth")) {
            rotY -= 15.0F;
         }

         if (partName.equals("outerrightmouth")) {
            rotY += 15.0F;
         }
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
