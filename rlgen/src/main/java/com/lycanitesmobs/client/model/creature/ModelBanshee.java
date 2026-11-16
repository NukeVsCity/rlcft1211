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

public class ModelBanshee extends ModelTemplateElemental {
   public ModelBanshee() {
      this(1.0F);
   }

   public ModelBanshee(float shadowSize) {
      this.initModel("banshee", LycanitesMobs.modInfo, "entity/banshee");
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "eye", true, LayerCreatureEffect.BLEND.NORMAL.id, true));
      renderer.addLayer(new LayerCreatureScrolling(renderer, "hair", false, LayerCreatureEffect.BLEND.NORMAL.id, true, new Vector2f(0.0F, -16.0F)));
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      float bob = -Mth.sin(loop * 0.2F) * 0.15F;
      if (partName.equals("handleft")) {
         this.translate(bob / 4.0F, bob, this.getAttackProgress());
      } else if (partName.equals("handright")) {
         this.translate(-bob / 4.0F, -bob, this.getAttackProgress());
      }

      if (partName.toLowerCase().equals("eyeleft")) {
         this.rotate(
            (float)Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))),
            (float)Math.toDegrees((double)((lookY - 15.0F) / (180.0F / (float)Math.PI))),
            0.0F
         );
      } else if (partName.toLowerCase().equals("eyeright")) {
         this.rotate(
            (float)Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))),
            (float)Math.toDegrees((double)((lookY + 15.0F) / (180.0F / (float)Math.PI))),
            0.0F
         );
      } else if (partName.equals("hair01")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("hair02")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("fingerleft01") || partName.equals("fingerright01")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos(loop * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("fingerleft02") || partName.equals("fingerright02")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 20.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("fingerleft03") || partName.equals("fingerright03")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 40.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("fingerleft04") || partName.equals("fingerright04")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 60.0F) * 0.2F) * 0.2F - 0.2F)), 0.0F, 0.0F);
      } else if (partName.equals("fingerleft05") || partName.equals("fingerright05")) {
         this.rotate((float)Math.toDegrees((double)(Mth.cos((loop + 60.0F) * 0.2F) * 0.2F + 0.2F)), 0.0F, 0.0F);
      }
   }

   @Override
   public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
      if (partName.contains("eye")) {
         return layer != null && "eye".equals(layer.name);
      } else {
         return !partName.contains("hair") ? layer == null : layer != null && "hair".equals(layer.name);
      }
   }

   @Override
   public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
      return super.getPartColor(partName, entity, layer, trophy, loop);
   }
}
