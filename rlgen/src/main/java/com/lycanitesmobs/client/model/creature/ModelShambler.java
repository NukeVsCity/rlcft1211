package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelShambler extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelShambler() {
      this(1.0F);
   }

   public ModelShambler(float shadowSize) {
      this.initModel("shambler", LycanitesMobs.modInfo, "entity/shambler");
      this.setPartCenter("head", 0.0F, 2.2F, 0.0F);
      this.setPartCenter("body", 0.0F, 2.2F, 0.0F);
      this.setPartCenter("leftarm", 0.7F, 1.8F, 0.0F);
      this.setPartCenter("rightarm", -0.7F, 1.8F, 0.0F);
      this.lockHeadX = true;
      this.lockHeadY = false;
      this.trophyScale = 0.6F;
      this.trophyOffset = new float[]{0.0F, -0.4F, 0.0F};
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

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("leftarm")) {
            this.rotate(0.0F, -25.0F, 0.0F);
         }

         if (partName.equals("rightarm")) {
            this.rotate(0.0F, 25.0F, 0.0F);
         }
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
