package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelAstarothVoid extends ModelAstaroth {
   boolean tentacleFlip = false;

   public ModelAstarothVoid() {
      this(1.0F);
   }

   public ModelAstarothVoid(float shadowSize) {
      this.initModel("astaroth_void", LycanitesMobs.modInfo, "entity/astaroth_void");
      this.lookHeadScaleX = 0.0F;
      this.lookHeadScaleY = 0.0F;
      this.lookNeckScaleX = 0.0F;
      this.lookNeckScaleY = 0.0F;
      this.trophyScale = 0.6F;
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.contains("tentacle")) {
         float loopOffset = 0.0F;
         if (partName.contains("front")) {
            loopOffset += 10.0F;
         } else if (partName.contains("middle")) {
            loopOffset += 20.0F;
         } else if (partName.contains("back")) {
            loopOffset += 30.0F;
         }

         if (partName.contains("left")) {
            this.rotate(
               (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)),
               (float)Math.toDegrees((double)(Mth.sin((loop + loopOffset / 2.0F) * 0.2F) * 0.25F)) - 10.0F,
               (float)(-Math.toDegrees((double)(Mth.cos((loop + loopOffset / 2.0F) * 0.09F) * 0.1F)))
            );
         } else {
            this.rotate(
               (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)),
               (float)(-Math.toDegrees((double)(Mth.sin((loop + loopOffset / 2.0F) * 0.2F) * 0.25F))) - 10.0F,
               (float)(-Math.toDegrees((double)(Mth.cos((loop + loopOffset / 2.0F) * 0.09F) * 0.1F)))
            );
         }
      }
   }
}
