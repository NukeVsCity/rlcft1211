package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateArachnid;

public class ModelSutiramu extends ModelTemplateArachnid {
   public ModelSutiramu() {
      this(1.0F);
   }

   public ModelSutiramu(float shadowSize) {
      this.initModel("sutiramu", LycanitesMobs.modInfo, "entity/sutiramu");
      this.lookHeadScaleX = 0.0F;
      this.lookHeadScaleY = 0.0F;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
      this.bodyIsTrophy = true;
   }
}
