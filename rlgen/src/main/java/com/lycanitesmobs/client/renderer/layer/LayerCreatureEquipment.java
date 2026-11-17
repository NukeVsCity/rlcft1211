package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.resources.ResourceLocation;

public class LayerCreatureEquipment extends LayerCreatureBase {
   public String equipmentSlot;

   public LayerCreatureEquipment(RenderCreature renderer, String equipmentSlot) {
      super(renderer);
      this.equipmentSlot = equipmentSlot;
   }

   @Override
   public boolean canRenderLayer(BaseCreatureEntity entity, float scale) {
      return super.canRenderLayer(entity, scale) && this.equipmentSlot != null ? entity.getEquipmentName(this.equipmentSlot) != null : false;
   }

   @Override
   public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
      return entity.getEquipmentTexture(entity.getEquipmentName(this.equipmentSlot));
   }
}
