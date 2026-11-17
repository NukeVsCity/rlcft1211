package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelRemobra extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelRemobra() {
      this(1.0F);
   }

   public ModelRemobra(float shadowSize) {
      this.initModel("Remobra", LycanitesMobs.modInfo, "entity/remobra");
      this.setPartCenter("body", 0.0F, 0.8F, 0.0F);
      this.setPartCenter("tail", 0.0F, 0.6F, -0.1F);
      this.setPartCenter("leftwing", 0.1F, 0.8F, -0.1F);
      this.setPartCenter("rightwing", -0.1F, 0.8F, -0.1F);
      this.trophyScale = 1.0F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.1F};
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
      if (partName.equals("leftwing")) {
         rotX = 20.0F;
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.4F) * 0.6F)));
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.sin(loop * 0.4F) * 0.6F)));
      }

      if (partName.equals("rightwing")) {
         rotX = 20.0F;
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.4F) * 0.6F)));
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.sin(loop * 0.4F + (float)Math.PI) * 0.6F)));
      }

      if (partName.equals("tail")) {
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.1F) * 0.6F)));
      }

      float bob = -Mth.sin(loop * 0.2F) * 0.3F;
      if (bob < 0.0F) {
         bob = -bob;
      }

      posY += bob;
      this.translate(posX, posY, posZ);
      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
   }
}
