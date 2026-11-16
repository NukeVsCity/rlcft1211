package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelCreatureObj;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelRoc extends ModelCreatureObj {
   public ModelRoc() {
      this(1.0F);
   }

   public ModelRoc(float shadowSize) {
      this.initModel("roc", LycanitesMobs.modInfo, "entity/roc");
      this.trophyScale = 1.0F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
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
      if (partName.equals("mouth")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.05F))), 0.0F, 0.0F);
      }

      if (entity != null && !entity.isInWater()) {
         if (partName.equals("wingleft")) {
            rotX = 20.0F;
            rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.4F) * 0.6F)));
            rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.sin(loop * 0.4F) * 0.6F)));
         }

         if (partName.equals("wingright")) {
            rotX = 20.0F;
            rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.4F) * 0.6F)));
            rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.sin(loop * 0.4F + (float)Math.PI) * 0.6F)));
         }
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).hasPickupEntity()) {
         if (partName.contains("tail0")) {
            rotX -= 5.0F;
            rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.2F) * 0.02F)));
         }

         if (partName.equals("tailclaw")) {
            rotX -= 25.0F;
         }
      } else {
         if (partName.contains("tail0")) {
            rotX += 10.0F;
            rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.1F) * 0.1F)));
         }

         if (partName.equals("tailclaw")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.1F) * 0.4F)));
         }
      }

      if (partName.equals("body")) {
         float bob = -Mth.sin(loop * 0.2F) * 0.1F;
         if (bob < 0.0F) {
            bob = -bob;
         }

         posY += bob;
      }

      this.translate(posX, posY, posZ);
      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
   }
}
