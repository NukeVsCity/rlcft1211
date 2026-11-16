package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelCreatureObj;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelRaiko extends ModelCreatureObj {
   float maxLeg = 0.0F;

   public ModelRaiko() {
      this(1.0F);
   }

   public ModelRaiko(float shadowSize) {
      this.initModel("raiko", LycanitesMobs.modInfo, "entity/raiko");
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
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F))), 0.0F, 0.0F);
      }

      if (entity != null && !entity.onGround()) {
         if (partName.equals("body")) {
            float bob = -Mth.sin(loop * 0.2F) * 0.1F;
            if (bob < 0.0F) {
               bob = -bob;
            }

            posY += bob;
         }
      } else {
         if (partName.equals("body")) {
            posY -= 0.6F;
         }

         float walkSwing = 0.6F;
         float wingOffset = 1.0F;
         if (entity.isInWater()) {
            wingOffset = 0.0F;
         }

         if (partName.equals("wingright")) {
            rotX = (float)((double)rotX + (Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)) - (double)(45.0F * wingOffset)));
            this.rotate(1.0F, 20.0F * wingOffset, 0.0F * wingOffset);
            rotZ = (float)((double)rotZ - (Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)) + (double)(90.0F * wingOffset)));
            this.scale(-1.0F, 1.0F, 1.0F);
         }

         if (partName.equals("wingleft")) {
            rotX = (float)((double)rotX + (Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)) - (double)(45.0F * wingOffset)));
            this.rotate(1.0F, -20.0F * wingOffset, 0.0F * wingOffset);
            rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)) + (double)(90.0F * wingOffset));
            this.scale(-1.0F, 1.0F, 1.0F);
         }

         if (partName.equals("legleft")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 0.8F * distance)));
         }

         if (partName.equals("legright")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing) * 0.8F * distance)));
         }

         if (partName.contains("clawback")) {
            rotX += 90.0F;
         } else if (partName.contains("claw")) {
            rotX -= 25.0F;
         }
      }

      if (!entity.isInWater() && !entity.onGround()) {
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

         if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).hasPickupEntity()) {
            if (partName.contains("clawback")) {
               rotX -= 25.0F;
            } else if (partName.contains("claw")) {
               rotX += 25.0F;
            }
         } else {
            if (partName.equals("legleft")) {
               rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.1F + (float)Math.PI) * 0.2F)));
            }

            if (partName.equals("legright")) {
               rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.1F) * 0.2F)));
            }

            if (partName.contains("clawback")) {
               rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.1F) * 0.4F)));
            } else if (partName.contains("claw")) {
               rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.1F) * 0.4F)));
            }
         }
      }

      this.translate(posX, posY, posZ);
      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
   }
}
