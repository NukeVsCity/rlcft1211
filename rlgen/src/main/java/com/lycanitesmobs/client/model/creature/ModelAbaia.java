package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateAquatic;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelAbaia extends ModelTemplateAquatic {
   public ModelAbaia() {
      this(1.0F);
   }

   public ModelAbaia(float shadowSize) {
      this.initModel("abaia", LycanitesMobs.modInfo, "entity/abaia");
      this.lookHeadScaleX = 0.5F;
      this.lookHeadScaleY = 0.5F;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }

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
      if (partName.equals("body") || partName.equals("tail01") || partName.equals("tail02")) {
         rotX += (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.25F) * 0.25F)));
         rotY += (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.5F) * 0.25F)));
      }

      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
