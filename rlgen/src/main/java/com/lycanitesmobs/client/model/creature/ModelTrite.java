package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateInsect;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelTrite extends ModelTemplateInsect {
   public ModelTrite() {
      this(1.0F);
   }

   public ModelTrite(float shadowSize) {
      this.initModel("trite", LycanitesMobs.modInfo, "entity/trite");
      this.mouthScaleX = 1.0;
      this.mouthScaleY = 0.1F;
      this.trophyScale = 1.0F;
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
   }
}
