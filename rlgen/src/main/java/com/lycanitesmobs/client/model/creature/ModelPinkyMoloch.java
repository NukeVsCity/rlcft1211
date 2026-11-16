package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;

public class ModelPinkyMoloch extends ModelTemplateBiped {
   public ModelPinkyMoloch() {
      this(1.0F);
   }

   public ModelPinkyMoloch(float shadowSize) {
      this.initModel("pinky_moloch", LycanitesMobs.modInfo, "entity/pinky_moloch");
      this.lookHeadScaleX = 0.5F;
      this.lookHeadScaleY = 0.5F;
      this.lookNeckScaleX = 0.5F;
      this.lookNeckScaleY = 0.5F;
      this.bigChildHead = true;
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }
}
