package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateInsect;

public class ModelVespid extends ModelTemplateInsect {
   public ModelVespid() {
      this(1.0F);
   }

   public ModelVespid(float shadowSize) {
      this.initModel("vespid", LycanitesMobs.modInfo, "entity/vespid");
      this.mouthScaleX = 2.0;
      this.mouthScaleY = 2.0;
   }
}
