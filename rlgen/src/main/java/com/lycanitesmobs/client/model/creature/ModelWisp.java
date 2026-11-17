package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.client.renderer.LycGL;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureScrolling;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import org.joml.Vector2f;
import org.joml.Vector4f;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelWisp extends ModelTemplateElemental {
   float maxLeg = 0.0F;

   public ModelWisp() {
      this(1.0F);
   }

   public ModelWisp(float shadowSize) {
      this.initModel("wisp", LycanitesMobs.modInfo, "entity/wisp");
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "ball", true, LayerCreatureEffect.BLEND.NORMAL.id, true));
      renderer.addLayer(new LayerCreatureEffect(renderer, "ball_glow", "ball", true, LayerCreatureEffect.BLEND.ADD.id, true));
      renderer.addLayer(new LayerCreatureScrolling(renderer, "hair", true, LayerCreatureEffect.BLEND.NORMAL.id, true, new Vector2f(0.0F, 4.0F)));
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.equals("haircenter")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.1F)), 0.0F, 0.0F);
      } else if (partName.equals("hairleft")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.cos(loop * 0.05F) * 0.1F)),
            (float)Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.2F - 0.2F)),
            0.0F
         );
      } else if (partName.equals("hairright")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.cos(loop * 0.05F) * 0.1F)),
            -((float)Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.2F - 0.2F))),
            0.0F
         );
      } else if (partName.contains("fringeleft")) {
         this.rotate(
            -((float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F))),
            0.0F,
            -((float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F)))
         );
      } else if (partName.contains("fringeright")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)),
            0.0F,
            (float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F))
         );
      } else if (partName.equals("armleft")) {
         this.rotate(0.0F, 0.0F, (float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F)));
      } else if (partName.equals("armright")) {
         this.rotate(0.0F, 0.0F, -((float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F))));
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("armleft")) {
            this.rotate(0.0F, 25.0F, 25.0F);
         }

         if (partName.equals("armright")) {
            this.rotate(0.0F, -25.0F, -25.0F);
         }
      }
   }

   @Override
   public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
      if (partName.contains("ball") && entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         return false;
      } else if (partName.equals("ball01")) {
         return layer != null && "ball".equals(layer.name);
      } else if (!partName.equals("ball02") && !partName.equals("ball03")) {
         if (partName.contains("fringe")) {
            return layer != null && "hair".equals(layer.name);
         } else {
            return !partName.contains("hair") ? layer == null : layer != null && "hair".equals(layer.name);
         }
      } else {
         return layer != null && "ball_glow".equals(layer.name);
      }
   }

   @Override
   public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
      if (layer == null || !"ball".equals(layer.name) && !"ball_glow".equals(layer.name)) {
         return super.getPartColor(partName, entity, layer, trophy, loop);
      } else {
         float glowSpeed = 40.0F;
         float glow = loop * glowSpeed % 360.0F;
         float color = (float)Math.cos(Math.toRadians((double)glow)) * 0.1F + 0.9F;
         return new Vector4f(color, color, color, 1.0F);
      }
   }

   @Override
   public void onRenderStart(LayerCreatureBase layer, Entity entity, boolean renderAsTrophy) {
      super.onRenderStart(layer, entity, renderAsTrophy);
      LycGL.disableLighting();
   }

   @Override
   public void onRenderFinish(LayerCreatureBase layer, Entity entity, boolean renderAsTrophy) {
      super.onRenderFinish(layer, entity, renderAsTrophy);
      LycGL.enableLighting();
   }
}
