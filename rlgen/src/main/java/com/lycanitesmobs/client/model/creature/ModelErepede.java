package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelErepede extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelErepede() {
      this(1.0F);
   }

   public ModelErepede(float shadowSize) {
      this.initModel("Erepede", LycanitesMobs.modInfo, "entity/erepede");
      this.setPartCenter("head", 0.0F, 1.2F, 0.6F);
      this.setPartCenter("topmouth", 0.0F, 1.75F, 1.25F);
      this.setPartCenter("leftmouth", 0.14F, 1.6F, 1.25F);
      this.setPartCenter("rightmouth", -0.14F, 1.6F, 1.25F);
      this.setPartCenter("bottommouth", 0.0F, 1.4F, 1.25F);
      this.setPartCenter("body", 0.0F, 1.2F, 0.6F);
      this.setPartCenter("frontleftleg", 0.4F, 1.1F, 0.25F);
      this.setPartCenter("middleleftleg", 0.4F, 1.1F, -0.15F);
      this.setPartCenter("backleftleg", 0.4F, 1.1F, -0.5F);
      this.setPartCenter("frontrightleg", -0.4F, 1.1F, 0.25F);
      this.setPartCenter("middlerightleg", -0.4F, 1.1F, -0.15F);
      this.setPartCenter("backrightleg", -0.4F, 1.1F, -0.5F);
      this.lockHeadY = false;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, -0.2F, -0.3F};
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
      if (partName.equals("topmouth") || partName.equals("leftmouth") || partName.equals("rightmouth") || partName.equals("bottommouth")) {
         this.centerPartToPart(partName, "head");
         if (!this.lockHeadX) {
            this.rotate((float)Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))), 0.0F, 0.0F);
         }

         if (!this.lockHeadY) {
            this.rotate(0.0F, (float)Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))), 0.0F);
         }

         this.uncenterPartToPart(partName, "head");
      }

      if (partName.equals("topmouth")) {
         rotX += (float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F));
      }

      if (partName.equals("leftmouth")) {
         rotY -= (float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F));
      }

      if (partName.equals("rightmouth")) {
         rotY += (float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F));
      }

      if (partName.equals("bottommouth")) {
         rotX -= (float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F));
      }

      float walkSwing = 0.3F;
      if (partName.equals("frontrightleg") || partName.equals("middleleftleg") || partName.equals("backrightleg")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
         rotY = (float)((double)rotY + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
      }

      if (partName.equals("frontleftleg") || partName.equals("middlerightleg") || partName.equals("backleftleg")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
         rotY = (float)((double)rotY + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("topmouth")) {
            rotX -= 20.0F;
         }

         if (partName.equals("leftmouth")) {
            rotY += 20.0F;
         }

         if (partName.equals("rightmouth")) {
            rotY -= 20.0F;
         }

         if (partName.equals("bottommouth")) {
            rotX += 20.0F;
         }
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }

   @Override
   public void childScale(String partName) {
      super.childScale(partName);
      if (partName.equals("head")
         || partName.equals("topmouth")
         || partName.equals("leftmouth")
         || partName.equals("rightmouth")
         || partName.equals("bottommouth")) {
         this.scale(2.0F, 2.0F, 2.0F);
         this.translate(0.0F, -(this.getPartCenter(partName)[1] / 2.0F), 0.0F);
      }
   }
}
