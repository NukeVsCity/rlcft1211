package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelClink extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelClink() {
      this(1.0F);
   }

   public ModelClink(float shadowSize) {
      this.initModel("Clink", LycanitesMobs.modInfo, "entity/clink");
      this.setPartCenters(0.0F, 1.5F, 0.2F, new String[]{"head", "mouth"});
      this.setPartSubCenter("mouth", 0.0F, 1.475F, 0.45F);
      this.setPartCenter("body", 0.0F, 1.5F, 0.2F);
      this.setPartCenter("leftarm", 0.55F, 1.5F, 0.15F);
      this.setPartCenter("rightarm", -0.55F, 1.5F, 0.15F);
      this.setPartCenter("leftleg", 0.3F, 0.75F, -0.25F);
      this.setPartCenter("rightleg", -0.3F, 0.75F, -0.25F);
      this.setPartCenters(0.0F, 0.9F, -0.4F, new String[]{"tail01", "tail02", "tail03", "tail04", "tail05"});
      this.setPartSubCenter("tail02", 0.0F, 1.1F, -0.8F);
      this.setPartSubCenter("tail03", 0.0F, 1.55F, -0.9F);
      this.setPartSubCenter("tail04", 0.0F, 0.8F, -0.65F);
      this.setPartSubCenter("tail05", 0.0F, 2.0F, -0.8F);
      this.trophyScale = 1.2F;
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
      if (partName.equals("mouth")) {
         if (!this.lockHeadX) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))));
         }

         if (!this.lockHeadY) {
            rotY = (float)((double)rotY + Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))));
         }
      }

      if (partName.equals("mouth")) {
         this.subCenterPart("mouth");
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F))), 0.0F, 0.0F);
         this.unsubCenterPart("mouth");
      }

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

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("mouth")) {
            this.subCenterPart("mouth");
            this.rotate(30.0F, 0.0F, 0.0F);
            this.unsubCenterPart("mouth");
         }

         if (partName.equals("rightarm")) {
            angleX = -0.2F;
            angleZ = 0.0F;
            if (((BaseCreatureEntity)entity).getAttackPhase() == 2) {
               angleY = 0.2F;
               rotation = 85.0F;
            } else if (((BaseCreatureEntity)entity).getAttackPhase() == 3) {
               angleY = 0.7F;
               angleZ = -0.1F;
               rotation = 40.0F;
            } else {
               angleY = 0.5F;
               rotation = 85.0F;
            }
         }
      }

      if (partName.equals("tail01") || partName.equals("tail02") || partName.equals("tail03") || partName.equals("tail04") || partName.equals("tail05")) {
         rotX = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.05F)));
         rotY = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)));
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
