package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelTreant extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelTreant() {
      this(1.0F);
   }

   public ModelTreant(float shadowSize) {
      this.initModel("treant", LycanitesMobs.modInfo, "entity/treant");
      this.setPartCenter("head", 0.0F, 6.0F, 1.0F);
      this.setPartCenter("body", 0.0F, 5.0F, 0.8F);
      this.setPartCenter("armlefttop", 0.9F, 7.2F, 0.0F);
      this.setPartCenter("armleftmiddle", 1.2F, 6.3F, 0.0F);
      this.setPartCenter("armleftbottom", 1.2F, 5.4F, 0.0F);
      this.setPartCenter("armrighttop", -0.9F, 7.2F, 0.0F);
      this.setPartCenter("armrightmiddle", -1.2F, 6.3F, 0.0F);
      this.setPartCenter("armrightbottom", -1.2F, 5.4F, 0.0F);
      this.setPartCenter("legleft", 0.4F, 4.5F, 0.0F);
      this.setPartCenter("legright", -0.4F, 4.5F, 0.0F);
      this.lockHeadX = true;
      this.lockHeadY = true;
      this.trophyScale = 0.8F;
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
      if (partName.equals("armlefttop") || partName.equals("armleftbottom") || partName.equals("armrightmiddle")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.equals("armrighttop") || partName.equals("armrightbottom") || partName.equals("armleftmiddle")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      float walkSwing = 0.3F;
      if (partName.equals("armlefttop") || partName.equals("armleftbottom") || partName.equals("armrightmiddle")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing) * 2.0F * distance * 0.5F)));
      }

      if (partName.equals("armrighttop") || partName.equals("armrightbottom") || partName.equals("armleftmiddle")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 2.0F * distance * 0.5F)));
      }

      if (partName.equals("legleft")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 1.4F * distance)));
      }

      if (partName.equals("legright")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing) * 1.4F * distance)));
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("armlefttop") || partName.equals("armleftbottom") || partName.equals("armrightmiddle")) {
            this.rotate(0.0F, -25.0F, 0.0F);
         }

         if (partName.equals("armrighttop") || partName.equals("armrightbottom") || partName.equals("armleftmiddle")) {
            this.rotate(0.0F, 25.0F, 0.0F);
         }
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
