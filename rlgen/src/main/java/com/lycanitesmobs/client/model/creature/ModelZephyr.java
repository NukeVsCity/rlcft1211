package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import org.joml.Vector4f;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class ModelZephyr extends ModelTemplateElemental {
   public ModelZephyr() {
      this(1.0F);
   }

   public ModelZephyr(float shadowSize) {
      this.initModel("zephyr", LycanitesMobs.modInfo, "entity/zephyr");
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, -0.2F, 0.2F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "glow", true, LayerCreatureEffect.BLEND.ADD.id, true));
      renderer.addLayer(new LayerCreatureEffect(renderer, "pulse01", true, LayerCreatureEffect.BLEND.ADD.id, true));
      renderer.addLayer(new LayerCreatureEffect(renderer, "pulse02", true, LayerCreatureEffect.BLEND.ADD.id, true));
      renderer.addLayer(new LayerCreatureEffect(renderer, "pulse03", true, LayerCreatureEffect.BLEND.ADD.id, true));
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      if ("effectouter".equals(partName) || "effectinner".equals(partName)) {
         this.rotate(-15.0F, 0.0F, 0.0F);
      }

      if (partName.equals("armeffectleft") || partName.equals("armeffectright")) {
         float angleX = 35.0F;
         float angleY = -45.0F;
         float angleZ = 140.0F;
         if (partName.equals("armeffectright")) {
            angleX = -angleX;
         }

         this.angle(loop * 50.0F, angleX, angleY, angleZ);
      }

      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
   }

   @Override
   public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
      if (layer == null) {
         return super.getPartColor(partName, entity, layer, trophy, loop);
      } else {
         float alphaTime = 15.0F;
         if ("pulse02".equals(layer.name)) {
            alphaTime = 20.0F;
         }

         if ("pulse03".equals(layer.name)) {
            alphaTime = 25.0F;
         }

         float alpha = loop % alphaTime / alphaTime * 2.0F;
         if (alpha > 1.0F) {
            alpha = -(alpha - 1.0F);
         }

         return new Vector4f(1.0F, 1.0F, 1.0F, alpha);
      }
   }
}
