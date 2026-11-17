package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureScrolling;
import org.joml.Vector2f;
import org.joml.Vector4f;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelXaphan extends ModelTemplateElemental {
   float maxLeg = 0.0F;

   public ModelXaphan() {
      this(1.0F);
   }

   public ModelXaphan(float shadowSize) {
      this.initModel("xaphan", LycanitesMobs.modInfo, "entity/xaphan");
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureScrolling(renderer, "ball", true, LayerCreatureEffect.BLEND.NORMAL.id, true, new Vector2f(0.0F, 4.0F)));
      renderer.addLayer(new LayerCreatureEffect(renderer, "ring", true, LayerCreatureEffect.BLEND.ADD.id, true));
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.equals("fingerleft01") || partName.equals("fingerright01")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("fingerleft02") || partName.equals("fingerright02")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 20.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("fingerleft03") || partName.equals("fingerright03")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 40.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("shoulderleft")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)),
            0.0F,
            (float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F))
         );
      } else if (partName.equals("shoulderright")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)),
            0.0F,
            (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F)))
         );
      } else if (partName.equals("effect01")) {
         this.rotate(0.0F, loop * 16.0F, 0.0F);
      } else if (partName.contains("ball")) {
         this.shiftOrigin(partName, "body");
         this.rotate(0.0F, loop * -8.0F, 0.0F);
         this.shiftOriginBack(partName, "body");
         float ballScale = (float)(1.0 + 0.2 * (double)Mth.cos(loop));
         if (partName.equals("ball02")) {
            ballScale = (float)(1.0 + 0.2 * (double)Mth.cos(loop + 20.0F));
         } else if (partName.equals("ball03")) {
            ballScale = (float)(1.0 + 0.2 * (double)Mth.cos(loop + 40.0F));
         } else if (partName.equals("ball04")) {
            ballScale = (float)(1.0 + 0.2 * (double)Mth.cos(loop + 60.0F));
         }

         this.scale(ballScale, ballScale, ballScale);
      } else if (partName.contains("spine")) {
         float coil = 8.0F;
         this.rotate(Mth.cos(loop * 0.1F) * coil - coil * 90.0F, 0.0F, 0.0F);
      }
   }

   @Override
   public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
      if ("effect01".equals(partName)) {
         return layer != null && "ring".equals(layer.name);
      } else {
         return !partName.contains("ball") ? layer == null : layer != null && "ball".equals(layer.name);
      }
   }

   @Override
   public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
      return super.getPartColor(partName, entity, layer, trophy, loop);
   }
}
