package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.core.entity.creature.EntitySpectre;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelSpectre extends ModelTemplateElemental {
   public ModelSpectre() {
      this(1.0F);
   }

   public ModelSpectre(float shadowSize) {
      this.initModel("spectre", LycanitesMobs.modInfo, "entity/spectre");
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "glow", true, LayerCreatureEffect.BLEND.ADD.id, true));
      renderer.addLayer(new LayerCreatureEffect(renderer, "", false, LayerCreatureEffect.BLEND.SUB.id, true));
   }

   @Override
   public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
      if ("effect01".equals(partName)) {
         return layer != null && "".equals(layer.name);
      } else if (!"effect02".equals(partName) && !"effect03".equals(partName)) {
         return layer == null || "glow".equals(layer.name);
      } else {
         return entity instanceof EntitySpectre && layer != null && "".equals(layer.name) ? ((EntitySpectre)entity).canPull() : false;
      }
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      boolean isPulling = false;
      if (entity instanceof EntitySpectre) {
         isPulling = ((EntitySpectre)entity).canPull();
      }

      float loopOffset = 0.0F;
      if (partName.contains("upper")) {
         loopOffset += 10.0F;
      } else if (partName.contains("middle")) {
         loopOffset += 20.0F;
      } else if (partName.contains("lower")) {
         loopOffset += 30.0F;
      }

      if ("effect01".equals(partName)) {
         this.rotate(25.0F, 0.0F, -loop * 10.0F);
         float effectScale = 1.0F + (float)Math.cos((double)(loop / 10.0F)) * 0.1F;
         if (isPulling) {
            effectScale *= 2.0F;
         }

         this.scale(effectScale, effectScale, effectScale);
      } else if ("effect02".equals(partName)) {
         this.rotate(0.0F, 0.0F, -loop * 10.0F);
         float effectScale = 2.0F + (float)Math.cos((double)(loop / 10.0F));
         this.scale(effectScale, effectScale, effectScale);
      } else if ("effect03".equals(partName)) {
         this.rotate(0.0F, 0.0F, loop * 10.0F);
         float effectScale = 2.0F + (float)Math.cos((double)(loop / 10.0F));
         this.scale(effectScale, effectScale, effectScale);
      } else if (partName.contains("armleft")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.034F) * 0.05F)),
            (float)Math.toDegrees((double)(Mth.sin((loop + loopOffset / 2.0F) * 0.1F) * 0.25F)) - 10.0F,
            (float)(-Math.toDegrees((double)(Mth.cos((loop + loopOffset / 2.0F) * 0.09F) * 0.1F)))
         );
      } else if (partName.contains("armright")) {
         this.rotate(
            (float)Math.toDegrees((double)(Mth.sin(loop * 0.034F) * 0.05F)),
            (float)(-Math.toDegrees((double)(Mth.sin((loop + loopOffset / 2.0F) * 0.1F) * 0.25F))) + 10.0F,
            (float)Math.toDegrees((double)(Mth.cos((loop + loopOffset / 2.0F) * 0.09F) * 0.1F))
         );
      } else if (partName.contains("mouthleft")) {
         this.rotate((float)Math.cos((double)(loop / 10.0F)) * 4.0F, (float)Math.cos((double)(loop / 10.0F)) * 4.0F, 0.0F);
      } else if (partName.contains("mouthright")) {
         this.rotate((float)Math.cos((double)(loop / 10.0F)) * 4.0F, -((float)Math.cos((double)(loop / 10.0F))) * 4.0F, 0.0F);
      } else if (partName.contains("mawleft")) {
         this.rotate(0.0F, (float)Math.cos((double)(loop / 10.0F)) * 10.0F + (float)(isPulling ? 90 : 0), 0.0F);
      } else if (partName.contains("mawright")) {
         this.rotate(0.0F, -((float)Math.cos((double)(loop / 10.0F))) * 10.0F - (float)(isPulling ? 90 : 0), 0.0F);
      }
   }

   @Override
   public void onRenderStart(LayerCreatureBase layer, Entity entity, boolean renderAsTrophy) {
      super.onRenderStart(layer, entity, renderAsTrophy);
   }

   @Override
   public void onRenderFinish(LayerCreatureBase layer, Entity entity, boolean renderAsTrophy) {
      super.onRenderFinish(layer, entity, renderAsTrophy);
   }
}
