package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelVorach extends ModelTemplateBiped {
   public ModelVorach() {
      this(1.0F);
   }

   public ModelVorach(float shadowSize) {
      this.initModel("vorach", LycanitesMobs.modInfo, "entity/vorach");
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if ((entity == null || entity.onGround() || entity.isInWater()) && partName.equals("legback")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(time * 0.4F + (float)Math.PI) * 1.4F * distance)), 0.0F, 0.0F);
      }
   }
}
