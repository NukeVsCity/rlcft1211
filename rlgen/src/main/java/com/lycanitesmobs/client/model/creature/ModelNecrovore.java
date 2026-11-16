package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelNecrovore extends ModelTemplateQuadruped {
   public ModelNecrovore() {
      this(1.0F);
   }

   public ModelNecrovore(float shadowSize) {
      this.initModel("necrovore", LycanitesMobs.modInfo, "entity/necrovore");
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, -0.2F, 0.0F};
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.contains("tail")) {
         this.rotate(0.0F, (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F + 0.1F))), 0.0F);
      }

      if (partName.equals("lefleftfront") || partName.equals("lefrightfront")) {
         this.rotate(-75.0F * this.getAttackProgress(), 0.0F, 0.0F);
      }
   }
}
