package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelEttin extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelEttin() {
      this(1.0F);
   }

   public ModelEttin(float shadowSize) {
      this.initModel("Ettin", LycanitesMobs.modInfo, "entity/ettin");
      this.setPartCenter("lefthead", 0.35F, 3.1F, 0.0F);
      this.setPartCenter("righthead", -0.35F, 3.1F, 0.0F);
      this.setPartCenter("body", 0.0F, 3.1F, 0.0F);
      this.setPartCenter("leftarm", 0.8F, 2.75F, 0.0F);
      this.setPartCenter("rightarm", -0.8F, 2.75F, 0.0F);
      this.setPartCenter("leftleg", 0.35F, 1.25F, 0.0F);
      this.setPartCenter("rightleg", -0.35F, 1.25F, 0.0F);
      this.trophyScale = 1.0F;
      this.trophyOffset = new float[]{0.0F, -0.2F, -0.2F};
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

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("leftarm")) {
            if (((BaseCreatureEntity)entity).getAttackPhase() == 0) {
               rotX += 30.0F;
            } else {
               rotX -= 30.0F;
            }
         }

         if (partName.equals("rightarm")) {
            if (((BaseCreatureEntity)entity).getAttackPhase() == 0) {
               rotX -= 30.0F;
            } else {
               rotX += 30.0F;
            }
         }
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
