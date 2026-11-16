package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.model.ModelCustom;
import com.lycanitesmobs.client.renderer.LycGL;
import com.lycanitesmobs.client.renderer.RenderCreature;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * 1.12 LayerCreatureBase (a LayerRenderer). doRenderLayer binds the layer texture (or keeps the texture bound last) and
 * renders the whole model again with this layer, which picks the parts, colours and texture offsets and changes the GL
 * state in onRenderStart/onRenderFinish (emulated by {@link LycGL}).
 */
public class LayerCreatureBase extends RenderLayer<BaseCreatureEntity, EntityModel<BaseCreatureEntity>> {
   public RenderCreature renderer;
   public String name;

   public LayerCreatureBase(RenderCreature renderer) {
      super(renderer);
      this.renderer = renderer;
      this.name = "Layer";
   }

   @Override
   public void render(PoseStack pose, MultiBufferSource buffers, int packedLight, BaseCreatureEntity entity, float limbSwing, float limbSwingAmount,
                      float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
      this.doRenderLayer(pose, buffers, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, 0.0625F);
   }

   public void doRenderLayer(PoseStack pose, MultiBufferSource buffers, int packedLight, BaseCreatureEntity entity, float limbSwing, float limbSwingAmount,
                             float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      if (this.canRenderLayer(entity, scale)) {
         if (this.renderer.getMainModel() instanceof ModelCustom model) {
            ResourceLocation layerTexture = this.getLayerTexture(entity);
            if (layerTexture != null) {
               LycGL.bindTexture(layerTexture);
            }

            // 1.12 RenderLivingBase.setBrightness: the red hurt tint for layers that combine textures
            int overlay = this.shouldCombineTextures() ? LivingEntityRenderer.getOverlayCoords(entity, 0.0F) : OverlayTexture.NO_OVERLAY;
            LycGL.unlock();
            LegacyRender.push(pose, LycGL.buffer(), LycGL.light(), overlay, -1);
            try {
               model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, this, true);
            } finally {
               LegacyRender.pop();
            }
         }
      }
   }

   public boolean canRenderLayer(BaseCreatureEntity entity, float scale) {
      return entity == null ? false : !entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player);
   }

   public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
      return null;
   }

   public boolean canRenderPart(String partName, BaseCreatureEntity entity, boolean trophy) {
      if (this.renderer.getMainModel() instanceof ModelCustom) {
         ((ModelCustom)this.renderer.getMainModel()).canBaseRenderPart(partName, entity, trophy);
      }

      return true;
   }

   public Vector4f getPartColor(String partName, BaseCreatureEntity entity, boolean trophy) {
      return new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public Vector2f getTextureOffset(String partName, BaseCreatureEntity entity, boolean trophy, float loop) {
      return new Vector2f(0.0F, 0.0F);
   }

   public void onRenderStart(Entity entity, boolean trophy) {
   }

   public void onRenderFinish(Entity entity, boolean trophy) {
   }

   public boolean shouldCombineTextures() {
      return true;
   }
}
