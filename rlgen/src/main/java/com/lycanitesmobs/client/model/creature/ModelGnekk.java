package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;

public class ModelGnekk extends ModelTemplateBiped {
   public ModelGnekk() {
      this(1.0F);
   }

   public ModelGnekk(float shadowSize) {
      this.initModel("gnekk", LycanitesMobs.modInfo, "entity/gnekk");
      this.trophyScale = 1.8F;
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "glow", true, LayerCreatureEffect.BLEND.ADD.id, true));
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (entity instanceof BaseCreatureEntity
         && ((BaseCreatureEntity)entity).hasPerchTarget()
         && ("armleft".equalsIgnoreCase(partName) || "armright".equalsIgnoreCase(partName))) {
         this.rotate(45.0F, 0.0F, 0.0F);
      }
   }
}
