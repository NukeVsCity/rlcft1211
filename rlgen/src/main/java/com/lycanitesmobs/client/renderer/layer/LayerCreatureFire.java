package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.resources.ResourceLocation;

public class LayerCreatureFire extends LayerCreatureBase {
   public LayerCreatureFire(RenderCreature renderer) {
      super(renderer);
   }

   @Override
   public boolean canRenderLayer(BaseCreatureEntity entity, float scale) {
      return !super.canRenderLayer(entity, scale) ? false : entity.isAttackOnCooldown();
   }

   @Override
   public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
      return entity.getSubTexture("fire");
   }
}
