package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelBarghest extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelBarghest() {
      this(1.0F);
   }

   public ModelBarghest(float shadowSize) {
      this.initModel("barghest", LycanitesMobs.modInfo, "entity/barghest");
      this.setPartCenter("head", 0.0F, 0.9F, 1.0F);
      this.setPartCenter("mouth", 0.0F, 0.82F, 1.4F);
      this.setPartCenter("body", 0.0F, 1.0F, 0.0F);
      this.setPartCenter("legleftfront", 0.27F, 0.9F, 0.6F);
      this.setPartCenter("legrightfront", -0.27F, 0.9F, 0.6F);
      this.setPartCenter("legleftback", 0.34661F, 0.9F, -0.5F);
      this.setPartCenter("legrightback", -0.34661F, 0.9F, -0.5F);
      this.setPartCenter("tail", 0.0F, 1.0F, -1.0F);
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
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.1F - 0.1F))), 0.0F, 0.0F);
      }

      if (partName.equals("tail")) {
         rotX = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.05F)));
         rotY = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)));
      }

      float walkSwing = 0.6F;
      if (partName.equals("legrightfront") || partName.equals("legleftback")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
      }

      if (partName.equals("legleftfront") || partName.equals("legrightback")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      if (entity != null && !entity.onGround() && !entity.isInWater()) {
         if (partName.equals("legleftback") || partName.equals("legrightback")) {
            rotX += 25.0F;
         }

         if (partName.equals("legleftfront") || partName.equals("legrightfront")) {
            rotX -= 25.0F;
         }
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown() && partName.equals("mouth")) {
         rotX -= 15.0F;
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
