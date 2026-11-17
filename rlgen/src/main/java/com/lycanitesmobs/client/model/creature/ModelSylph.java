package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.client.renderer.LycGL;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureScrolling;
import org.joml.Vector2f;
import org.joml.Vector4f;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelSylph extends ModelTemplateElemental {
   public ModelSylph() {
      this(1.0F);
   }

   public ModelSylph(float shadowSize) {
      this.initModel("sylph", LycanitesMobs.modInfo, "entity/sylph");
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "hood", false, LayerCreatureEffect.BLEND.NORMAL.id, true));
      renderer.addLayer(new LayerCreatureScrolling(renderer, "wing", true, LayerCreatureEffect.BLEND.ADD.id, true, new Vector2f(0.0F, 1.0F)));
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (this.currentModelState != null) {
         this.currentModelState.attackAnimationSpeed = 0.08F;
      }

      if (partName.contains("wingleft")) {
         float attackAngle = 30.0F;
         if (partName.equals("wingleft02")) {
            attackAngle = 50.0F;
         } else if (partName.equals("wingleft03")) {
            attackAngle = 45.0F;
         }

         this.rotate(
            0.0F,
            5.0F + (float)Math.toDegrees((double)(Mth.sin(loop * 0.2F) * 0.4F)),
            4.0F + (float)Math.toDegrees((double)(Mth.sin(loop * 0.2F) * 0.08F)) + attackAngle * this.getAttackProgress()
         );
      } else if (partName.contains("wingright")) {
         float attackAngle = -30.0F;
         if (partName.equals("wingright02")) {
            attackAngle = -50.0F;
         } else if (partName.equals("wingright03")) {
            attackAngle = -45.0F;
         }

         this.rotate(
            0.0F,
            -5.0F + (float)Math.toDegrees((double)(Mth.sin(loop * 0.2F + (float)Math.PI) * 0.4F)),
            -4.0F + (float)Math.toDegrees((double)(Mth.sin(loop * 0.2F + (float)Math.PI) * 0.08F)) + attackAngle * this.getAttackProgress()
         );
      } else if (partName.contains("finger")) {
         if (partName.contains("thumb")) {
            this.rotate(-((float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F - 0.2F))), 0.0F, 0.0F);
         } else {
            this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
         }
      }
   }

   @Override
   public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
      if (partName.contains("hood")) {
         return layer != null && "hood".equals(layer.name);
      } else {
         return !partName.contains("wing") ? layer == null : layer != null && "wing".equals(layer.name);
      }
   }

   @Override
   public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
      if (layer == null) {
         float glowSpeed = 40.0F;
         float glow = loop * glowSpeed % 360.0F;
         float color = (float)Math.cos(Math.toRadians((double)glow)) * 0.1F + 0.9F;
         return new Vector4f(color, color, color, 1.0F);
      } else {
         return super.getPartColor(partName, entity, layer, trophy, loop);
      }
   }

   @Override
   public void onRenderStart(LayerCreatureBase layer, Entity entity, boolean renderAsTrophy) {
      super.onRenderStart(layer, entity, renderAsTrophy);
      if (layer == null) {
         LycGL.disableLighting();
      }
   }

   @Override
   public void onRenderFinish(LayerCreatureBase layer, Entity entity, boolean renderAsTrophy) {
      super.onRenderFinish(layer, entity, renderAsTrophy);
      if (layer == null) {
         LycGL.enableLighting();
      }
   }
}
