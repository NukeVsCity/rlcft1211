package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelKrake extends ModelTemplateBiped {
   public ModelKrake() {
      this(1.0F);
   }

   public ModelKrake(float shadowSize) {
      this.initModel("krake", LycanitesMobs.modInfo, "entity/krake");
      this.mouthScale = 2.5F;
      this.bigChildHead = false;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, -0.2F, 0.0F};
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance * 0.5F, loop, lookY, lookX, scale);
      if (partName.contains("spike")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.sin(loop * 0.2F) * 0.1F))), 0.0F, 0.0F);
      }
   }
}
