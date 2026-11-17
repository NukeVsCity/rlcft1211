package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.client.renderer.LycGL;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import org.joml.Vector4f;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelNymph extends ModelTemplateElemental {
   float maxLeg = 0.0F;

   public ModelNymph() {
      this(1.0F);
   }

   public ModelNymph(float shadowSize) {
      this.initModel("nymph", LycanitesMobs.modInfo, "entity/nymph");
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "overlay"));
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.equals("wingleftbottom") || partName.equals("winglefttop")) {
         this.rotate(0.0F, 10.0F + (float)Math.toDegrees((double)(Mth.sin(loop * 0.2F) * 0.4F)), 0.0F);
      } else if (partName.equals("wingrightbottom") || partName.equals("wingrighttop")) {
         this.rotate(0.0F, -10.0F + (float)Math.toDegrees((double)(Mth.sin(loop * 0.2F + (float)Math.PI) * 0.4F)), 0.0F);
      } else if (partName.equals("hairleft")) {
         this.rotate(
            -((float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F))),
            0.0F,
            -((float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F)))
         );
      } else if (partName.equals("hairright")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)),
            0.0F,
            (float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F))
         );
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
