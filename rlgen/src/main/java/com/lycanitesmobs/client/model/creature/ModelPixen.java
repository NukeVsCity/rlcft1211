package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;

public class ModelPixen extends ModelTemplateBiped {
   public ModelPixen() {
      this(1.0F);
   }

   public ModelPixen(float shadowSize) {
      this.initModel("pixen", LycanitesMobs.modInfo, "entity/pixen");
      this.trophyScale = 1.8F;
      this.trophyOffset = new float[]{0.0F, -0.05F, -0.1F};
      this.wingScale = 4.0F;
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "glow", true, LayerCreatureEffect.BLEND.ADD.id, true));
   }
}
