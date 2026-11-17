package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelEyewig extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelEyewig() {
      this(1.0F);
   }

   public ModelEyewig(float shadowSize) {
      this.initModel("Eyewig", LycanitesMobs.modInfo, "entity/eyewig");
      this.setPartCenter("head", 0.0F, 0.5F, 1.0F);
      this.setPartCenter("leftmouth", 0.25F, 0.55F, 0.55F);
      this.setPartCenter("rightmouth", -0.25F, 0.55F, 0.55F);
      this.setPartCenter("body", 0.0F, 0.5F, 0.0F);
      this.setPartCenter("frontleftleg", 0.35F, 0.55F, 0.3F);
      this.setPartCenter("middleleftleg", 0.35F, 0.55F, 0.0F);
      this.setPartCenter("backleftleg", 0.35F, 0.55F, -0.3F);
      this.setPartCenter("frontrightleg", -0.35F, 0.55F, 0.3F);
      this.setPartCenter("middlerightleg", -0.35F, 0.55F, 0.0F);
      this.setPartCenter("backrightleg", -0.35F, 0.55F, -0.3F);
      this.trophyScale = 0.8F;
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
      if (partName.equals("leftmouth")) {
         rotY += (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F - 0.05F)));
      }

      if (partName.equals("rightmouth")) {
         rotY -= (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F - 0.05F)));
      }

      if (partName.equals("frontleftleg")
         || partName.equals("middleleftleg")
         || partName.equals("backleftleg")
         || partName.equals("frontrightleg")
         || partName.equals("middlerightleg")
         || partName.equals("backrightleg")) {
         angleZ = 1.0F;
      }

      if (partName.equals("frontleftleg")) {
         angleY = 0.041666668F;
      }

      if (partName.equals("backleftleg")) {
         angleY = -0.041666668F;
      }

      if (partName.equals("frontrightleg")) {
         angleY = -0.041666668F;
      }

      if (partName.equals("backrightleg")) {
         angleY = 0.041666668F;
      }

      float walkSwing = 0.3F;
      if (partName.equals("frontrightleg") || partName.equals("middleleftleg") || partName.equals("backrightleg")) {
         rotation = (float)((double)rotation + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
      }

      if (partName.equals("frontleftleg") || partName.equals("middlerightleg") || partName.equals("backleftleg")) {
         rotation = (float)((double)rotation + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      float bob = Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance;
      if (bob < 0.0F) {
         bob += -bob * 2.0F;
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("leftmouth")) {
            rotY -= 15.0F;
         }

         if (partName.equals("rightmouth")) {
            rotY += 15.0F;
         }
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }

   @Override
   public void childScale(String partName) {
      super.childScale(partName);
      if (partName.equals("head") || partName.equals("mouth")) {
         this.scale(2.0F, 2.0F, 2.0F);
         this.translate(0.0F, -(this.getPartCenter(partName)[1] / 2.0F), 0.0F);
      }
   }
}
