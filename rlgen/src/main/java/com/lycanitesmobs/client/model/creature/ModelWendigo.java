package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelWendigo extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelWendigo() {
      this(1.0F);
   }

   public ModelWendigo(float shadowSize) {
      this.initModel("Wendigo", LycanitesMobs.modInfo, "entity/wendigo");
      this.setPartCenter("head", 0.0F, 4.392F, -0.216F);
      this.setPartCenter("mouth", 0.0F, 4.4568F, -1.1376F);
      this.setPartCenter("body", 0.0F, 3.908F, 0.3F);
      this.setPartCenter("armleft", 0.504F, 4.032F, -0.036F);
      this.setPartCenter("armright", -0.504F, 4.032F, -0.036F);
      this.setPartCenter("legleft", 0.216F, 2.592F, 0.0F);
      this.setPartCenter("legright", -0.216F, 2.592F, 0.0F);
      this.lockHeadY = true;
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
      if (partName.equals("mouth")) {
         this.centerPartToPart("mouth", "head");
         if (!this.lockHeadX) {
            this.rotate((float)Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))), 0.0F, 0.0F);
         }

         if (!this.lockHeadY) {
            this.rotate(0.0F, (float)Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))), 0.0F);
         }

         this.uncenterPartToPart("mouth", "head");
      }

      if (partName.equals("mouth")) {
         rotX += Mth.cos(loop * 0.09F) * 0.05F - 0.05F;
      }

      if (partName.equals("armleft")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.equals("armright")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (entity == null || entity.onGround() || entity.isInWater()) {
         float walkSwing = 0.6F;
         if (partName.equals("armleft")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing) * 1.0F * distance * 0.5F)));
            rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(time * walkSwing) * 0.5F * distance * 0.5F)));
         }

         if (partName.equals("armright")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 1.0F * distance * 0.5F)));
            rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 0.5F * distance * 0.5F)));
         }

         if (partName.equals("legleft")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 1.4F * distance)));
         }

         if (partName.equals("legright")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing) * 1.4F * distance)));
         }
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("armleft") || partName.equals("armright")) {
            rotX -= 65.0F;
         }

         if (partName.equals("armleft")) {
            rotY -= 20.0F;
         }

         if (partName.equals("armright")) {
            rotY += 20.0F;
         }
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
