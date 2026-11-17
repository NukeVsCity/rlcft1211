package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;

public class ModelBelph extends ModelTemplateBiped {
   public ModelBelph() {
      this(1.0F);
   }

   public ModelBelph(float shadowSize) {
      this.initModel("belph", LycanitesMobs.modInfo, "entity/belph");
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }
}
