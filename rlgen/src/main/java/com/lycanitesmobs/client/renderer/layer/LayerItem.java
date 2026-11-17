package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.renderer.LycGL;
import com.lycanitesmobs.client.renderer.IItemModelRenderer;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import org.joml.Vector2f;
import org.joml.Vector4f;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class LayerItem {
   public IItemModelRenderer renderer;
   public String name;
   public String textureSuffix;
   public boolean glow = false;
   public int blending = 0;
   public Vector2f scrollSpeed;
   public Vector4f colorFadeSpeed;

   public LayerItem(IItemModelRenderer renderer, String name) {
      this.renderer = renderer;
      this.name = name;
   }

   public Vector4f getPartColor(String partName, ItemStack itemStack, float loop) {
      if (this.colorFadeSpeed != null) {
         float red = 1.0F;
         if (this.colorFadeSpeed.x != 0.0F) {
            red = loop % this.colorFadeSpeed.x / this.colorFadeSpeed.x * 2.0F;
         }

         if (red > 1.0F) {
            red = -(red - 1.0F);
         }

         float green = 1.0F;
         if (this.colorFadeSpeed.y != 0.0F) {
            green = loop % this.colorFadeSpeed.y / this.colorFadeSpeed.y * 2.0F;
         }

         if (green > 1.0F) {
            green = -(green - 1.0F);
         }

         float blue = 1.0F;
         if (this.colorFadeSpeed.z != 0.0F) {
            blue = loop % this.colorFadeSpeed.z / this.colorFadeSpeed.z * 2.0F;
         }

         if (blue > 1.0F) {
            blue = -(blue - 1.0F);
         }

         float alpha = 1.0F;
         if (this.colorFadeSpeed.w != 0.0F) {
            alpha = loop % this.colorFadeSpeed.w / this.colorFadeSpeed.w * 2.0F;
         }

         if (alpha > 1.0F) {
            alpha = -(alpha - 1.0F);
         }

         return new Vector4f(red, green, blue, alpha);
      } else {
         return new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
      return entity.getTexture(this.textureSuffix);
   }

   public void onRenderStart(ItemStack itemStack) {
      if (this.glow) {
         LycGL.disableLighting();
         LycGL.setLightmap(15728880);
      }

      if (this.blending == LayerItem.BLEND.ADD.id) {
         LycGL.blendFunc(com.lycanitesmobs.client.renderer.LycRenderTypes.BLEND_ADD);
      } else if (this.blending == LayerItem.BLEND.SUB.id) {
         LycGL.blendFunc(com.lycanitesmobs.client.renderer.LycRenderTypes.BLEND_SUB);
      }
   }

   public void onRenderFinish(ItemStack itemStack) {
      if (this.glow) {
         LycGL.enableLighting();
      }
   }

   public Vector2f getTextureOffset(String partName, ItemStack itemStack, float loop) {
      if (this.scrollSpeed == null) {
         this.scrollSpeed = new Vector2f(0.0F, 0.0F);
      }

      return new Vector2f(loop * this.scrollSpeed.x, loop * this.scrollSpeed.y);
   }

   public static enum BLEND {
      NORMAL(0),
      ADD(1),
      SUB(2);

      public final int id;

      private BLEND(int value) {
         this.id = value;
      }

      public int getValue() {
         return this.id;
      }
   }
}
