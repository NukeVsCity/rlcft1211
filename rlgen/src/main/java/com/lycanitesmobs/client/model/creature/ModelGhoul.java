package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelGhoul extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelGhoul() {
      this(1.0F);
   }

   public ModelGhoul(float shadowSize) {
      this.initModel("GhoulZombie", LycanitesMobs.modInfo, "entity/ghoul");
      this.setPartCenter("head", 0.0F, 1.2F, 0.4F);
      this.setPartCenter("body", 0.0F, 1.2F, 0.1F);
      this.setPartCenter("leftarm", 0.3F, 1.2F, 0.15F);
      this.setPartCenter("rightarm", -0.3F, 1.2F, 0.15F);
      this.setPartCenter("leftleg", 0.1F, 0.6F, 0.0F);
      this.setPartCenter("rightleg", -0.1F, 0.6F, 0.0F);
      this.trophyScale = 1.2F;
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
      if (partName.equals("leftarm")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.equals("rightarm")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      float walkSwing = 0.6F;
      if (partName.equals("leftarm")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing) * 2.0F * distance * 0.5F)));
      }

      if (partName.equals("rightarm")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 2.0F * distance * 0.5F)));
      }

      if (partName.equals("leftleg")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 1.4F * distance)));
      }

      if (partName.equals("rightleg")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing) * 1.4F * distance)));
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
