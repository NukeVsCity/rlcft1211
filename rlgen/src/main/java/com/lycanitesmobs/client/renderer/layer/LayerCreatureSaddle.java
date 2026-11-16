package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import net.minecraft.resources.ResourceLocation;

public class LayerCreatureSaddle extends LayerCreatureBase {
   public LayerCreatureSaddle(RenderCreature renderer) {
      super(renderer);
   }

   @Override
   public boolean canRenderLayer(BaseCreatureEntity entity, float scale) {
      return super.canRenderLayer(entity, scale) && entity instanceof RideableCreatureEntity ? ((RideableCreatureEntity)entity).hasSaddle() : false;
   }

   @Override
   public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
      return entity.getEquipmentTexture("saddle");
   }
}
