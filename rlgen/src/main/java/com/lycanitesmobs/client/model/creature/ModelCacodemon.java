package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import net.minecraft.world.entity.Mob;

public class ModelCacodemon extends ModelTemplateBiped {
   public ModelCacodemon() {
      this(1.0F);
   }

   public ModelCacodemon(float shadowSize) {
      this.initModel("cacodemon", LycanitesMobs.modInfo, "entity/cacodemon");
      this.flightBobScale = 0.1F;
      this.trophyScale = 0.5F;
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "glow", true, LayerCreatureEffect.BLEND.ADD.id, true));
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      float rotX = 0.0F;
      float rotY = 0.0F;
      if (partName.toLowerCase().equals("body")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))) * 0.75);
         rotY = (float)((double)rotY + Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))) * 0.75);
      }

      if (partName.equals("eye")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))) * 0.25);
         rotY = (float)((double)rotY + Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))) * 0.25);
      }

      this.rotate(rotX, rotY, 0.0F);
   }
}
