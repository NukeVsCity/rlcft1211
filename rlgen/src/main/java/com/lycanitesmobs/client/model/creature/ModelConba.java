package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelConba extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelConba() {
      this(1.0F);
   }

   public ModelConba(float shadowSize) {
      this.initModel("Conba", LycanitesMobs.modInfo, "entity/conba");
      this.setPartCenter("head", 0.0F, 0.6F, 0.3F);
      this.setPartCenter("mouth", 0.0F, 0.5F, 0.5F);
      this.setPartCenter("body", 0.0F, 0.7F, 0.0F);
      this.setPartCenter("armleft", 0.3F, 0.7F, 0.0F);
      this.setPartCenter("armright", -0.3F, 0.7F, 0.0F);
      this.setPartCenter("legleft", 0.2F, 0.5F, -0.5F);
      this.setPartCenter("legright", -0.2F, 0.5F, -0.5F);
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
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F))), 0.0F, 0.0F);
      }

      float walkSwing = 0.3F;
      if (partName.equals("armright") || partName.equals("legleft")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
      }

      if (partName.equals("armleft") || partName.equals("legright")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("armleft")) {
            this.rotate(-135.0F, 0.0F, 0.0F);
         }

         if (partName.equals("armright")) {
            this.rotate(-135.0F, 0.0F, 0.0F);
         }
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
