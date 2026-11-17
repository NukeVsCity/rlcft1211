package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import net.minecraft.world.entity.Mob;

public class ModelAglebemu extends ModelTemplateQuadruped {
   public ModelAglebemu() {
      this(1.0F);
   }

   public ModelAglebemu(float shadowSize) {
      this.initModel("aglebemu", LycanitesMobs.modInfo, "entity/aglebemu");
      this.lookHeadScaleX = 0.5F;
      this.lookHeadScaleY = 0.5F;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
      this.bodyIsTrophy = true;
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.equals("tongue")) {
         this.scale(this.getAttackProgress() * 2.0F, this.getAttackProgress() * 2.0F, this.getAttackProgress() * 2.0F);
      }
   }
}
