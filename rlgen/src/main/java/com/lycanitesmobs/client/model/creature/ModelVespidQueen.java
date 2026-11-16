package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateInsect;

public class ModelVespidQueen extends ModelTemplateInsect {
   public ModelVespidQueen() {
      this(1.0F);
   }

   public ModelVespidQueen(float shadowSize) {
      this.initModel("vespidqueen", LycanitesMobs.modInfo, "entity/vespidqueen");
      this.mouthScaleX = 2.0;
      this.mouthScaleY = 2.0;
   }
}
