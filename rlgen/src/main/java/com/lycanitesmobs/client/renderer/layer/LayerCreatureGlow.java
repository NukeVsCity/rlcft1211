package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.AssetManager;
import com.lycanitesmobs.client.renderer.LycGL;
import com.lycanitesmobs.client.renderer.RenderCreature;
import net.minecraft.world.entity.Entity;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import org.joml.Vector4f;
import net.minecraft.resources.ResourceLocation;

public class LayerCreatureGlow extends LayerCreatureBase {
   public LayerCreatureGlow(RenderCreature renderer) {
      super(renderer);
   }

   @Override
   public Vector4f getPartColor(String partName, BaseCreatureEntity entity, boolean trophy) {
      return new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @Override
   public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
      String textureName = entity.getTextureName();
      if (entity.getVariant() != null) {
         textureName = textureName + "_" + entity.getVariant().color;
      }

      textureName = textureName + "_glow";
      if (AssetManager.getTexture(textureName) == null) {
         AssetManager.addTexture(textureName, entity.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
      }

      return AssetManager.getTexture(textureName);
   }

   @Override
   public void onRenderStart(Entity entity, boolean trophy) {
      LycGL.setLightmap(15728880);
   }

   @Override
   public void onRenderFinish(Entity entity, boolean trophy) {
      LycGL.setLightmap(LycGL.entityLight());
   }

}
