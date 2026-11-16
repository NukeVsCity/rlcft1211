package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.renderer.LycRenderTypes;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.client.renderer.LycGL;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;
import org.joml.Vector4f;

/** 1.12 LayerCreatureEffect. */
public class LayerCreatureEffect extends LayerCreatureBase {
   public String textureSuffix;
   public boolean subspecies = true;
   public boolean glow = false;
   public int blending = 0;
   public Vector2f scrollSpeed;

   public LayerCreatureEffect(RenderCreature renderer, String textureSuffix) {
      super(renderer);
      this.name = textureSuffix;
      this.textureSuffix = textureSuffix;
   }

   public LayerCreatureEffect(RenderCreature renderer, String textureSuffix, boolean glow, int blending, boolean subspecies) {
      super(renderer);
      this.name = textureSuffix;
      this.textureSuffix = textureSuffix;
      this.glow = glow;
      this.blending = blending;
      this.subspecies = subspecies;
   }

   public LayerCreatureEffect(RenderCreature renderer, String name, String textureSuffix, boolean glow, int blending, boolean subspecies) {
      super(renderer);
      this.name = name;
      this.textureSuffix = textureSuffix;
      this.glow = glow;
      this.blending = blending;
      this.subspecies = subspecies;
   }

   @Override
   public Vector4f getPartColor(String partName, BaseCreatureEntity entity, boolean trophy) {
      return new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @Override
   public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
      return this.subspecies ? entity.getTexture(this.textureSuffix) : entity.getSubTexture(this.textureSuffix);
   }

   @Override
   public void onRenderStart(Entity entity, boolean trophy) {
      int i = LycGL.entityLight();
      if (this.glow) {
         LycGL.disableLighting();
         i = 15728880;
      }

      LycGL.setLightmap(i);
      if (this.blending == LayerCreatureEffect.BLEND.ADD.id) {
         LycGL.blendFunc(LycRenderTypes.BLEND_ADD);
      } else if (this.blending == LayerCreatureEffect.BLEND.SUB.id) {
         LycGL.blendFunc(LycRenderTypes.BLEND_SUB);
      }
   }

   @Override
   public void onRenderFinish(Entity entity, boolean trophy) {
      if (this.glow) {
         LycGL.enableLighting();
      }
   }

   @Override
   public Vector2f getTextureOffset(String partName, BaseCreatureEntity entity, boolean trophy, float loop) {
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
