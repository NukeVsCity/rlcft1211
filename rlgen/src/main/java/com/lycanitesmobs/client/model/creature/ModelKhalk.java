package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelKhalk extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelKhalk() {
      this(1.0F);
   }

   public ModelKhalk(float shadowSize) {
      this.initModel("Khalk", LycanitesMobs.modInfo, "entity/khalk");
      this.setPartCenter("head", 0.0F, 1.4F, 2.7F);
      this.setPartCenter("body", 0.0F, 2.0F, 0.0F);
      this.setPartCenter("legleftfront", 1.8F, 1.3F, 1.0F);
      this.setPartCenter("legleftback", 1.8F, 1.3F, -1.4F);
      this.setPartCenter("legrightfront", -1.8F, 1.3F, 1.0F);
      this.setPartCenter("legrightback", -1.8F, 1.3F, -1.4F);
      this.setPartCenter("tail", 0.0F, 1.2F, -3.5F);
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, -0.2F, 0.0F};
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
      if (partName.equals("tail")) {
         rotX = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.05F)));
         rotY = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)));
      }

      float walkSwing = 0.3F;
      if (partName.equals("legrightfront") || partName.equals("legleftback")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
      }

      if (partName.equals("legleftfront") || partName.equals("legrightback")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
