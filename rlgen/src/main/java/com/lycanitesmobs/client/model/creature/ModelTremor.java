package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelTremor extends ModelTemplateElemental {
   public ModelTremor() {
      this(1.0F);
   }

   public ModelTremor(float shadowSize) {
      this.initModel("tremor", LycanitesMobs.modInfo, "entity/tremor");
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (!"body".equals(partName) && !"mouth".equals(partName)) {
         float vibration = loop * 2.0F;
         if (partName.contains("right")) {
            vibration = -vibration;
         }

         this.translate(Mth.cos(vibration) * 0.01F, Mth.cos(vibration) * 0.01F, Mth.cos(vibration) * 0.01F);
      }

      if (partName.contains("rib") && !partName.contains("07")) {
         float angleX = 0.0F;
         float angleY = 1.0F;
         float angleZ = 0.0F;
         if (!partName.contains("arm")) {
            if (partName.contains("04")) {
               angleY = 180.0F;
               angleZ = 70.0F;
            } else if (partName.contains("05")) {
               angleY = 180.0F;
               angleZ = 180.0F;
            } else if (partName.contains("06")) {
               angleY = 180.0F;
               angleZ = 60.0F;
            }
         } else {
            if (partName.contains("01")) {
               angleX = -180.0F;
               angleY = 180.0F;
               angleZ = -60.0F;
            } else if (partName.contains("02")) {
               angleX = -60.0F;
               angleY = 180.0F;
               angleZ = -60.0F;
            } else if (partName.contains("03")) {
               angleX = -35.0F;
               angleY = 180.0F;
               angleZ = -60.0F;
            }

            if (partName.contains("armright")) {
               angleX = -angleX;
            }
         }

         this.angle(loop * 50.0F, angleX, angleY, angleZ);
      }
   }
}
