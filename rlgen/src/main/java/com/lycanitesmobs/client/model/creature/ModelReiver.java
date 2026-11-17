package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import org.joml.Vector4f;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class ModelReiver extends ModelTemplateElemental {
   float maxLeg = 0.0F;

   public ModelReiver() {
      this(1.0F);
   }

   public ModelReiver(float shadowSize) {
      this.initModel("reiver", LycanitesMobs.modInfo, "entity/reiver");
      this.trophyScale = 1.0F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.2F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "pulse01", false, LayerCreatureEffect.BLEND.NORMAL.id, false));
      renderer.addLayer(new LayerCreatureEffect(renderer, "pulse02", false, LayerCreatureEffect.BLEND.NORMAL.id, false));
      renderer.addLayer(new LayerCreatureEffect(renderer, "pulse03", false, LayerCreatureEffect.BLEND.NORMAL.id, false));
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      if (partName.contains("effect")) {
         this.rotate(25.0F, 0.0F, 0.0F);
      }

      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
   }

   @Override
   public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
      if (layer == null) {
         return super.getPartColor(partName, entity, layer, trophy, loop);
      } else {
         float alphaSpeed = 10.0F;
         if ("pulse02".equals(layer.name)) {
            alphaSpeed = 9.5F;
            loop += 100.0F;
         }

         if ("pulse03".equals(layer.name)) {
            alphaSpeed = 9.0F;
            loop += 200.0F;
         }

         float alpha = loop * alphaSpeed % 360.0F;
         return new Vector4f(1.0F, 1.0F, 1.0F, (float)Math.cos(Math.toRadians((double)alpha)) / 2.0F + 0.5F);
      }
   }
}
