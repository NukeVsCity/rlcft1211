package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;

public class ModelWildkin extends ModelTemplateBiped {
   public ModelWildkin() {
      this(1.0F);
   }

   public ModelWildkin(float shadowSize) {
      this.initModel("wildkin", LycanitesMobs.modInfo, "entity/wildkin");
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }
}
