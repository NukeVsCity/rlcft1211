package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelGrell extends ModelTemplateBiped {
   public ModelGrell() {
      this(1.0F);
   }

   public ModelGrell(float shadowSize) {
      this.initModel("grell", LycanitesMobs.modInfo, "entity/grell");
      this.flightBobScale = 0.1F;
      this.trophyScale = 0.5F;
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.contains("tentacle")) {
         float loopOffset = 0.0F;
         if (partName.contains("front")) {
            loopOffset += 10.0F;
         } else if (partName.contains("left")) {
            loopOffset += 20.0F;
         } else if (partName.contains("right")) {
            loopOffset += 30.0F;
         }

         if (partName.contains("left")) {
            this.rotate(
               (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.2F)),
               (float)Math.toDegrees((double)(Mth.sin((loop + loopOffset / 2.0F) * 0.2F) * 0.25F)) - 10.0F,
               (float)(-Math.toDegrees((double)(Mth.cos((loop + loopOffset / 2.0F) * 0.09F) * 0.1F)))
            );
         } else {
            this.rotate(
               (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.2F)),
               (float)(-Math.toDegrees((double)(Mth.sin((loop + loopOffset / 2.0F) * 0.2F) * 0.25F))) - 10.0F,
               (float)(-Math.toDegrees((double)(Mth.cos((loop + loopOffset / 2.0F) * 0.09F) * 0.1F)))
            );
         }
      }
   }
}
