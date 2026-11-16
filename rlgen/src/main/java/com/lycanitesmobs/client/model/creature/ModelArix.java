package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;

public class ModelArix extends ModelTemplateBiped {
   public ModelArix() {
      this(1.0F);
   }

   public ModelArix(float shadowSize) {
      this.initModel("arix", LycanitesMobs.modInfo, "entity/arix");
      this.trophyScale = 1.8F;
      this.trophyOffset = new float[]{0.0F, -0.05F, -0.1F};
   }
}
