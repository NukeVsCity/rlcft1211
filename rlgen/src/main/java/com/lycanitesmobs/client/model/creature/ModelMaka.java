package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;

public class ModelMaka extends ModelTemplateQuadruped {
   public ModelMaka() {
      this(1.0F);
   }

   public ModelMaka(float shadowSize) {
      this.initModel("maka", LycanitesMobs.modInfo, "entity/maka");
      this.trophyScale = 0.6F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }
}
