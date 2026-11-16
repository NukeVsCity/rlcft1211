package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;

public class ModelAfrit extends ModelTemplateBiped {
   public ModelAfrit() {
      this(1.0F);
   }

   public ModelAfrit(float shadowSize) {
      this.initModel("afrit", LycanitesMobs.modInfo, "entity/afrit");
      this.trophyScale = 1.8F;
   }
}
