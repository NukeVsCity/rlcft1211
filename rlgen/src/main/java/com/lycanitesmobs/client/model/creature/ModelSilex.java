package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelSilex extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelSilex() {
      this(1.0F);
   }

   public ModelSilex(float shadowSize) {
      this.initModel("silex", LycanitesMobs.modInfo, "entity/silex");
      this.setPartCenter("body", 0.0F, 0.5F, 0.0F);
      this.lockHeadX = false;
      this.lockHeadY = false;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
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
      if (partName.equals("body")) {
         rotY = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.05F - 0.05F)));
      }

      if ((entity == null || entity.isInWater()) && partName.equals("body")) {
         rotY += (float)(-Math.toDegrees((double)(Mth.cos(time * 0.1F) * 0.2F)));
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }

   @Override
   public void childScale(String partName) {
      if (!partName.equals("head") && !partName.equals("mouth")) {
         super.childScale(partName);
      } else {
         this.translate(-(this.getPartCenter(partName)[0] / 2.0F), -(this.getPartCenter(partName)[1] / 2.0F), -(this.getPartCenter(partName)[2] / 2.0F));
      }
   }
}
