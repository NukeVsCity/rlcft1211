package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelBalayang extends ModelTemplateBiped {
   public ModelBalayang() {
      this(1.0F);
   }

   public ModelBalayang(float shadowSize) {
      this.initModel("balayang", LycanitesMobs.modInfo, "entity/balayang");
      this.trophyScale = 1.2F;
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      BaseCreatureEntity creatureEntity = null;
      if (entity instanceof BaseCreatureEntity) {
         creatureEntity = (BaseCreatureEntity)entity;
      }

      if (entity != null && !entity.onGround() && !entity.isInWater() && (creatureEntity == null || !creatureEntity.hasPerchTarget())) {
         if (partName.equals("wingleft01")) {
            float rotX = -40.0F;
            float rotY = 20.5F;
            float rotZ = (float)Math.toDegrees((double)(Mth.sin(loop * 0.4F * this.wingScale) * 0.6F));
            this.rotate(rotX, rotY, rotZ);
            return;
         }

         if (partName.equals("wingright01")) {
            float rotX = -40.0F;
            float rotY = -20.5F;
            float rotZ = (float)Math.toDegrees((double)(Mth.sin(loop * 0.4F * this.wingScale + (float)Math.PI) * 0.6F));
            this.rotate(rotX, rotY, rotZ);
            return;
         }

         if (partName.equals("wingleft02")) {
            float rotZ = (float)Math.toDegrees((double)(Mth.sin(loop * 0.4F * this.wingScale) * 0.15F));
            this.rotate(0.0F, 0.0F, rotZ);
            return;
         }

         if (partName.equals("wingright02")) {
            float rotZ = (float)Math.toDegrees((double)(Mth.sin(loop * 0.4F * this.wingScale + (float)Math.PI) * 0.15F));
            this.rotate(0.0F, 0.0F, rotZ);
            return;
         }
      }

      float rotX = 0.0F;
      float rotY = 0.0F;
      if (partName.toLowerCase().equals("body")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))) * 0.75);
         rotY = (float)((double)rotY + Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))) * 0.75);
      }

      if (partName.equals("eye")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))) * 0.25);
         rotY = (float)((double)rotY + Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))) * 0.25);
      }

      this.rotate(rotX, rotY, 0.0F);
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
   }
}
