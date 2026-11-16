package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelConcapedeHead extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelConcapedeHead() {
      this(1.0F);
   }

   public ModelConcapedeHead(float shadowSize) {
      this.initModel("Concapede", LycanitesMobs.modInfo, "entity/concapedehead");
      this.setPartCenter("head", 0.0F, 0.5F, 0.6F);
      this.setPartCenter("leftmouth", 0.2F, 0.4F, 1.0F);
      this.setPartCenter("rightmouth", -0.2F, 0.4F, 1.0F);
      this.setPartCenter("body", 0.0F, 0.5F, 0.6F);
      this.setPartCenter("frontleftleg", 0.6F, 0.6F, 0.15F);
      this.setPartCenter("backleftleg", 0.6F, 0.6F, -0.15F);
      this.setPartCenter("frontrightleg", -0.6F, 0.6F, 0.15F);
      this.setPartCenter("backrightleg", -0.6F, 0.6F, -0.15F);
      this.lockHeadX = true;
      this.lockHeadY = true;
      this.trophyScale = 1.0F;
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
      if (partName.equals("leftmouth") || partName.equals("rightmouth")) {
         if (partName.equals("leftmouth")) {
            this.centerPartToPart("leftmouth", "head");
         }

         if (partName.equals("rightmouth")) {
            this.centerPartToPart("rightmouth", "head");
         }

         if (!this.lockHeadX) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))));
         }

         if (!this.lockHeadY) {
            rotY = (float)((double)rotY + Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))));
         }

         if (partName.equals("leftmouth")) {
            this.uncenterPartToPart("leftmouth", "head");
         }

         if (partName.equals("rightmouth")) {
            this.uncenterPartToPart("rightmouth", "head");
         }
      }

      if (partName.equals("leftmouth")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F + (float)Math.PI) * 0.05F - 0.05F))), 0.0F, 0.0F);
      }

      if (partName.equals("rightmouth")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F))), 0.0F, 0.0F);
      }

      if (partName.equals("frontleftleg") || partName.equals("backleftleg") || partName.equals("frontrightleg") || partName.equals("backrightleg")) {
         angleZ = 1.0F;
      }

      if (partName.equals("frontleftleg")) {
         angleY = 0.027777778F;
      }

      if (partName.equals("backleftleg")) {
         angleY = -0.027777778F;
      }

      if (partName.equals("frontrightleg")) {
         angleY = -0.027777778F;
      }

      if (partName.equals("backrightleg")) {
         angleY = 0.027777778F;
      }

      float walkSwing = 0.6F;
      if (partName.equals("frontrightleg") || partName.equals("backleftleg")) {
         rotation = (float)((double)rotation + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
      }

      if (partName.equals("frontleftleg") || partName.equals("backrightleg")) {
         rotation = (float)((double)rotation + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      if (entity instanceof BaseCreatureEntity
         && ((BaseCreatureEntity)entity).isAttackOnCooldown()
         && (partName.equals("leftmouth") || partName.equals("rightmouth"))) {
         rotX += 20.0F;
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
