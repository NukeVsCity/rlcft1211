package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelVentoraptor extends ModelTemplateBiped {
   public ModelVentoraptor() {
      this(1.0F);
   }

   public ModelVentoraptor(float shadowSize) {
      this.initModel("Ventoraptor", LycanitesMobs.modInfo, "entity/ventoraptor");
      this.trophyScale = 1.0F;
      this.trophyOffset = new float[]{0.0F, -0.1F, -0.3F};
      this.tailScaleX = 1.5F;
      this.tailScaleY = 0.1F;
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.contains("claw")) {
         float clawX = (float)Math.toDegrees((double)(Mth.cos((loop + time) * 0.1F) * 0.2F));
         this.rotate(clawX, 0.0F, 0.0F);
      }
   }
}
