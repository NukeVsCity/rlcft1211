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

public class ModelGrue extends ModelTemplateElemental {
   float maxLeg = 0.0F;

   public ModelGrue() {
      this(1.0F);
   }

   public ModelGrue(float shadowSize) {
      this.initModel("grue", LycanitesMobs.modInfo, "entity/grue");
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureScrolling(renderer, "cloak", false, LayerCreatureEffect.BLEND.SUB.id, true, new Vector2f(0.0F, 1.0F)));
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.equals("mouth")) {
         this.rotate(10.0F, 0.0F, 0.0F);
      } else if (partName.equals("fingerleft01") || partName.equals("fingerright01")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("fingerleft02") || partName.equals("fingerright02")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 20.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("fingerleft03") || partName.equals("fingerright03")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 40.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("fingerleft04") || partName.equals("fingerright04")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 60.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      }
   }

   @Override
   public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
      return !"cloak".equals(partName) ? layer == null : layer != null && "cloak".equals(layer.name);
   }

   @Override
   public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
      return super.getPartColor(partName, entity, layer, trophy, loop);
   }
}
