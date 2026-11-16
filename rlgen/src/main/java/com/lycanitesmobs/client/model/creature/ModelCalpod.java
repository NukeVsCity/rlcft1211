package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateInsect;

public class ModelCalpod extends ModelTemplateInsect {
   public ModelCalpod() {
      this(1.0F);
   }

   public ModelCalpod(float shadowSize) {
      this.initModel("calpod", LycanitesMobs.modInfo, "entity/calpod");
      this.lookHeadScaleX = 0.5F;
      this.lookHeadScaleY = 0.5F;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
      this.bodyIsTrophy = true;
   }
}
