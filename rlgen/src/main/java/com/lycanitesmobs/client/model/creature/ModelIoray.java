package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateAquatic;

public class ModelIoray extends ModelTemplateAquatic {
   public ModelIoray() {
      this(1.0F);
   }

   public ModelIoray(float shadowSize) {
      this.initModel("ioray", LycanitesMobs.modInfo, "entity/ioray");
      this.lookHeadScaleX = 0.0F;
      this.lookHeadScaleY = 0.0F;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }
}
