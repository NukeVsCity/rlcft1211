package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelConcapedeSegment extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelConcapedeSegment() {
      this(1.0F);
   }

   public ModelConcapedeSegment(float shadowSize) {
      this.initModel("concapedesegment", LycanitesMobs.modInfo, "entity/concapede");
      this.setPartCenter("body", 0.0F, 0.5F, 0.6F);
      this.setPartCenter("frontleftleg", 0.6F, 0.6F, 0.15F);
      this.setPartCenter("backleftleg", 0.6F, 0.6F, -0.15F);
      this.setPartCenter("frontrightleg", -0.6F, 0.6F, 0.15F);
      this.setPartCenter("backrightleg", -0.6F, 0.6F, -0.15F);
      this.lockHeadX = true;
      this.lockHeadY = true;
      this.trophyScale = 0.7F;
      this.trophyOffset = new float[]{0.0F, -0.2F, 0.6F};
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

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
