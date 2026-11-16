package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelArgus extends ModelTemplateElemental {
   public ModelArgus() {
      this(1.0F);
   }

   public ModelArgus(float shadowSize) {
      this.initModel("argus", LycanitesMobs.modInfo, "entity/argus");
      this.trophyScale = 1.2F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }

   @Override
   public void addCustomLayers(RenderCreature renderer) {
      super.addCustomLayers(renderer);
      renderer.addLayer(new LayerCreatureEffect(renderer, "flash", true, LayerCreatureEffect.BLEND.ADD.id, true));
   }

   @Override
   public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
      return layer instanceof LayerCreatureEffect && entity instanceof BaseCreatureEntity ? ((BaseCreatureEntity)entity).isAttackOnCooldown() : true;
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.contains("arm")) {
         float rotX = (float)Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)) * 4.0F;
         float rotY = (float)Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.1F));
         if ("arm01".equals(partName) || "arm02".equals(partName) || "arm03".equals(partName)) {
            this.rotate(rotX, rotY, loop * 8.0F);
            this.translate(Mth.cos(loop) * 0.05F - 0.05F, Mth.cos(loop) * 0.05F - 0.05F, 0.0F);
         } else if ("arm04".equals(partName) || "arm05".equals(partName) || "arm06".equals(partName)) {
            this.rotate(rotX, rotY, -loop * 8.0F);
            this.translate(Mth.cos(-loop) * 0.05F - 0.05F, Mth.cos(-loop) * 0.05F - 0.05F, 0.0F);
         }
      }

      if (partName.contains("tail")) {
         float tailSwipeX = 32.0F;
         float tailSwipeY = 32.0F;
         this.rotate(
            Mth.cos(loop * 0.1F) * tailSwipeX - tailSwipeX * 90.0F,
            Mth.sin(loop * 0.2F) * tailSwipeY - tailSwipeY * 90.0F,
            0.0F
         );
      }

      if (partName.equals("mouth")) {
         this.rotate(45.0F * this.getAttackProgress(), 0.0F, 0.0F);
      }
   }
}
