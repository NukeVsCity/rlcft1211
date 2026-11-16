package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateDragon;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelIgnibus extends ModelTemplateDragon {
   public ModelIgnibus() {
      this(1.0F);
   }

   public ModelIgnibus(float shadowSize) {
      this.initModel("ignibus", LycanitesMobs.modInfo, "entity/ignibus");
      this.lookHeadScaleX = 0.5F;
      this.lookHeadScaleY = 0.5F;
      this.lookNeckScaleX = 0.5F;
      this.lookNeckScaleY = 0.5F;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (entity != null && !entity.onGround()) {
         RideableCreatureEntity rideableCreatureEntity = (RideableCreatureEntity)entity;
         if (rideableCreatureEntity.hasRiderTarget()) {
            if (partName.equals("body")) {
               this.rotate(0.0F, 0.0F, 0.0F);
            }

            if (partName.equals("neck")) {
               this.rotate(70.0F, 0.0F, 0.0F);
            }
         } else {
            if (partName.equals("body")) {
               this.rotate(-45.0F * (1.0F - distance), 0.0F, 0.0F);
            }

            if (partName.equals("neck")) {
               this.rotate(50.0F, 0.0F, 0.0F);
            }

            if (partName.equals("head")) {
               this.rotate(20.0F * (1.0F - distance), 0.0F, 0.0F);
            }

            if (partName.equals("legleftback") || partName.equals("legrightback")) {
               this.rotate(20.0F * (1.0F - distance), 0.0F, 0.0F);
            }

            if (partName.equals("legleftfront")) {
               this.rotate(
                  (float)((double)(40.0F * (1.0F - distance)) + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F))),
                  120.0F * (1.0F - distance),
                  (float)((double)(-40.0F * (1.0F - distance)) + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)))
               );
            }

            if (partName.equals("legrightfront")) {
               this.rotate(
                  (float)((double)(40.0F * (1.0F - distance)) + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F))),
                  -120.0F * (1.0F - distance),
                  (float)((double)(40.0F * (1.0F - distance)) + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)))
               );
            }

            if (partName.contains("foot")) {
               this.rotate(90.0F * (1.0F - distance), 0.0F, 0.0F);
            }

            if (partName.contains("tail")) {
               this.rotate(20.0F * (1.0F - distance), 0.0F, 0.0F);
            }
         }
      }
   }
}
