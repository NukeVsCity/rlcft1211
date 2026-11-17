package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelAstaroth extends ModelTemplateQuadruped {
   float maxLeg = 0.0F;

   public ModelAstaroth() {
      this(1.0F);
   }

   public ModelAstaroth(float shadowSize) {
      this.initModel("astaroth", LycanitesMobs.modInfo, "entity/astaroth");
      this.lookHeadScaleX = 0.0F;
      this.lookHeadScaleY = 0.0F;
      this.lookNeckScaleX = 0.0F;
      this.lookNeckScaleY = 0.0F;
      this.trophyScale = 0.6F;
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.equals("body")) {
         float bob = Mth.cos(time * 0.6662F + (float)Math.PI) * 0.3F * distance;
         if (bob < 0.0F) {
            bob += -bob * 2.0F;
         }

         this.translate(0.0F, bob, 0.0F);
      }

      if (partName.equals("tail01") || partName.equals("tail03")) {
         float rotX = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.05F)));
         float rotY = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)));
         this.rotate(rotX, rotY, 0.0F);
      } else if (partName.equals("tail02")) {
         float rotX = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.05F)));
         float rotY = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)));
         this.rotate(-rotX, rotY, 0.0F);
      } else if (partName.equals("fingerleft")) {
         this.rotate(0.0F, (float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F - 0.2F)), 0.0F);
      } else if (partName.equals("fingerright")) {
         this.rotate(0.0F, (float)Math.toDegrees((double)(Mth.cos((loop + 20.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F);
      } else if (partName.equals("fingertop")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.2F) * 0.2F - 0.2F)),
            (float)Math.toDegrees((double)(Mth.cos((loop + 40.0F) * 0.2F) * 0.2F - 0.2F)),
            0.0F
         );
      }
   }
}
