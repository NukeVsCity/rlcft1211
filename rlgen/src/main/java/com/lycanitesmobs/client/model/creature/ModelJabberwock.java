package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;

public class ModelJabberwock extends ModelTemplateBiped {
   public ModelJabberwock() {
      this(1.0F);
   }

   public ModelJabberwock(float shadowSize) {
      this.initModel("jabberwock", LycanitesMobs.modInfo, "entity/jabberwock");
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }
}
