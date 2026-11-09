package com.github.alexthe666.iceandfire.client.render.tile;

import com.github.alexthe666.iceandfire.client.model.ModelDragonEgg;
import com.github.alexthe666.iceandfire.client.render.entity.RenderDragonEgg;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityEggInIce;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import com.mojang.blaze3d.vertex.PoseStack;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** 1.12 RenderEggInIce: the egg model upside down at (x + 0.5, y - 0.75, z + 0.5); a fire egg type shows the blue egg. */
public class RenderEggInIce implements BlockEntityRenderer<TileEntityEggInIce> {
   public RenderEggInIce(BlockEntityRendererProvider.Context context) {
   }

   @Override
   public void render(TileEntityEggInIce egg, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
      // 1.12 made a new ModelDragonEgg every frame (so renderFrozen's wobble, applied after drawing, never shows)
      ModelDragonEgg model = new ModelDragonEgg();
      if (egg.type != null) {
         poseStack.pushPose();
         poseStack.translate(0.5F, -0.75F, 0.5F);
         EnumDragonEgg eggType = egg.type.isFire ? EnumDragonEgg.BLUE : egg.type;
         LegacyRender.push(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(RenderDragonEgg.getEggTexture(eggType))), light,
            OverlayTexture.NO_OVERLAY, -1);
         try {
            model.renderFrozen(egg);
         } finally {
            LegacyRender.pop();
         }
         poseStack.popPose();
      }
   }
}
