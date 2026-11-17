package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.creature.EntityYale;
import org.joml.Vector4f;
import net.minecraft.resources.ResourceLocation;

public class LayerCreatureDye extends LayerCreatureBase {
   public String textureSuffix;
   public boolean subspecies = true;

   public LayerCreatureDye(RenderCreature renderer, String textureSuffix, boolean subspecies) {
      super(renderer);
      this.name = textureSuffix;
      this.textureSuffix = textureSuffix;
      this.subspecies = subspecies;
   }

   public LayerCreatureDye(RenderCreature renderer, String name, String textureSuffix, boolean subspecies) {
      super(renderer);
      this.name = name;
      this.textureSuffix = textureSuffix;
      this.subspecies = subspecies;
   }

   @Override
   public boolean canRenderLayer(BaseCreatureEntity entity, float scale) {
      if (!super.canRenderLayer(entity, scale)) {
         return false;
      } else {
         return !(entity instanceof EntityYale) ? true : ((EntityYale)entity).hasFur();
      }
   }

   @Override
   public boolean canRenderPart(String partName, BaseCreatureEntity entity, boolean trophy) {
      return this.name.equals(partName);
   }

   @Override
   public Vector4f getPartColor(String partName, BaseCreatureEntity entity, boolean trophy) {
      int colorID = entity.getColor();
      return new Vector4f(RenderCreature.colorTable[colorID][0], RenderCreature.colorTable[colorID][1], RenderCreature.colorTable[colorID][2], 1.0F);
   }

   @Override
   public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
      return this.subspecies ? entity.getTexture(this.textureSuffix) : entity.getSubTexture(this.textureSuffix);
   }
}
