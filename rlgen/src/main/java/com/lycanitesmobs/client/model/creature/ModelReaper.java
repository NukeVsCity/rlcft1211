package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureScrolling;
import org.joml.Vector2f;
import org.joml.Vector4f;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelReaper extends ModelTemplateBiped {
   public ModelReaper() {
      this(1.0F);
   }

   public ModelReaper(float shadowSize) {
      this.initModel("reaper", LycanitesMobs.modInfo, "entity/reaper");
      this.flightBobScale = 0.15F;
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, 0.0F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureScrolling(renderer, "cape", false, LayerCreatureEffect.BLEND.SUB.id, true, new Vector2f(0.0F, -1.0F)));
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.equals("cloak")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F)), 0.0F, 0.0F);
      } else if (partName.equals("cape")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.05F)), 0.0F, 0.0F);
      } else if (partName.equals("clawleft01") || partName.equals("clawright01")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("clawleft02") || partName.equals("clawright02")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 20.0F) * 0.2F) * 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("clawleft03") || partName.equals("clawright03")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 40.0F) * 0.2F) * 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("skull01")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("skull02")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 20.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("skull03")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 40.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("skull04")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 60.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("skull05")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 80.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      }
   }

   @Override
   public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
      return !"cape".equals(partName) ? layer == null : layer != null && "cape".equals(layer.name);
   }

   @Override
   public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
      return super.getPartColor(partName, entity, layer, trophy, loop);
   }
}
