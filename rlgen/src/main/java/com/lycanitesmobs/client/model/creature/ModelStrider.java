package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelCreatureObj;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelStrider extends ModelCreatureObj {
   float maxLeg = 0.0F;

   public ModelStrider() {
      this(1.0F);
   }

   public ModelStrider(float shadowSize) {
      this.initModel("strider", LycanitesMobs.modInfo, "entity/strider");
      this.lookHeadScaleX = 0.0F;
      this.lookHeadScaleY = 0.0F;
      this.trophyScale = 0.4F;
      this.trophyOffset = new float[]{0.0F, -0.2F, 0.0F};
      this.bodyIsTrophy = true;
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
      if (partName.equals("armleft")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.equals("armright")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      float walkSwing = 0.15F;
      if (partName.equals("legleft")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * (distance / 2.0F))));
      }

      if (partName.equals("legright")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * (distance / 2.0F))));
      }

      if (partName.equals("legback")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("armleft")) {
            this.rotate(-25.0F, 0.0F, 0.0F);
         }

         if (partName.equals("armright")) {
            this.rotate(-25.0F, 0.0F, 0.0F);
         }
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).hasPickupEntity() && (partName.equals("armleft") || partName.equals("armright"))
         )
       {
         rotX = (float)((double)rotX + 20.0);
      }

      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
