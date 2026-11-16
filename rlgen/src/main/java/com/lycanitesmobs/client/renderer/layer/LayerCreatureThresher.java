package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.creature.EntityThresher;
import org.joml.Vector4f;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class LayerCreatureThresher extends LayerCreatureBase {
   public LayerCreatureThresher(RenderCreature renderer) {
      super(renderer);
   }

   @Override
   public boolean canRenderLayer(BaseCreatureEntity entity, float scale) {
      if (entity instanceof EntityThresher) {
         EntityThresher entityThresher = (EntityThresher)entity;
         return entityThresher.canWhirlpool();
      } else {
         return false;
      }
   }

   @Override
   public boolean canRenderPart(String partName, BaseCreatureEntity entity, boolean trophy) {
      return "effect".equals(partName);
   }

   @Override
   public Vector4f getPartColor(String partName, BaseCreatureEntity entity, boolean trophy) {
      return new Vector4f(1.0F, 1.0F, 1.0F, 0.5F);
   }

   @Override
   public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
      return entity.getSubTexture("effect");
   }

   @Override
   public void onRenderStart(Entity entity, boolean trophy) {
   }

   @Override
   public void onRenderFinish(Entity entity, boolean trophy) {
   }
}
