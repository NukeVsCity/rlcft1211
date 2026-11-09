package com.github.alexthe666.iceandfire.client.render.entity.layer;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.enums.EnumDragonTextures;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * 1.12 LayerDragonEyes: the model again with the eyes texture, additive blending (ONE, ONE), full-bright lightmap, depth mask off
 * only when invisible. RenderType.eyes is exactly that (1.12 spider eyes became it). Not drawn on stoned (gorgon) dragons.
 */
public class LayerDragonEyes extends RenderLayer<EntityDragonBase, EntityModel<EntityDragonBase>> {
   public LayerDragonEyes(RenderLayerParent<EntityDragonBase, EntityModel<EntityDragonBase>> renderer) {
      super(renderer);
   }

   @Override
   public void render(PoseStack poseStack, MultiBufferSource buffers, int light, EntityDragonBase dragon, float limbSwing, float limbSwingAmount,
                      float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
      if (!StoneCheck.isStone(dragon) && dragon.shouldRenderEyes()) {
         this.getParentModel().renderToBuffer(poseStack, buffers.getBuffer(RenderType.eyes(EnumDragonTextures.getEyeTextureFromDragon(dragon))),
            0xF000F0, OverlayTexture.NO_OVERLAY, -1);
      }
   }
}
