package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.AssetManager;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import org.joml.Vector2f;
import org.joml.Vector4f;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class LayerCreatureDjinn extends LayerCreatureBase {
   public LayerCreatureDjinn(RenderCreature renderer) {
      super(renderer);
   }

   @Override
   public boolean canRenderPart(String partName, BaseCreatureEntity entity, boolean trophy) {
      return partName.contains("ribbon");
   }

   @Override
   public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
      String textureName = entity.getTextureName();
      if (entity.getVariant() != null) {
         textureName = textureName + "_" + entity.getVariant().color;
      }

      textureName = textureName + "_ribbon";
      if (AssetManager.getTexture(textureName) == null) {
         AssetManager.addTexture(textureName, entity.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
      }

      return AssetManager.getTexture(textureName);
   }

   @Override
   public Vector2f getTextureOffset(String partName, BaseCreatureEntity entity, boolean trophy, float loop) {
      return new Vector2f(-loop * 25.0F, 0.0F);
   }

   @Override
   public Vector4f getPartColor(String partName, BaseCreatureEntity entity, boolean trophy) {
      return new Vector4f(1.0F, 1.0F, 1.0F, 0.75F);
   }

   @Override
   public void onRenderStart(Entity entity, boolean trophy) {
   }

   @Override
   public void onRenderFinish(Entity entity, boolean trophy) {
   }
}
