package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;

public class ModelDawon extends ModelTemplateQuadruped {
   public ModelDawon() {
      this(1.0F);
   }

   public ModelDawon(float shadowSize) {
      this.initModel("dawon", LycanitesMobs.modInfo, "entity/dawon");
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }
}
