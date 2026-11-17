package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;

public class ModelGeonach extends ModelTemplateElemental {
   public ModelGeonach() {
      this(1.0F);
   }

   public ModelGeonach(float shadowSize) {
      this.initModel("geonach", LycanitesMobs.modInfo, "entity/geonach");
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }
}
