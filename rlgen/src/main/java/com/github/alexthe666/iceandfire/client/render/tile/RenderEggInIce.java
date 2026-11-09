package com.github.alexthe666.iceandfire.client.render.tile;

import com.github.alexthe666.iceandfire.entity.tile.TileEntityEggInIce;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** 1.12 RenderEggInIce: the wobbling egg inside the ice (a fire egg type shows as blue, 1.12 quirk). */
public class RenderEggInIce implements BlockEntityRenderer<TileEntityEggInIce> {
   public RenderEggInIce(BlockEntityRendererProvider.Context ctx) {
   }

   @Override
   public void render(TileEntityEggInIce egg, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
      if (egg.type != null && IafRenderHooks.egg != null) {
         pose.pushPose();
         pose.translate(0.5F, -0.75F, 0.5F);
         EnumDragonEgg eggType = egg.type.isFire ? EnumDragonEgg.BLUE : egg.type;
         IafRenderHooks.egg.render(pose, buffers, light, overlay, RenderPodium.getEggTexture(eggType), egg.ticksExisted);
         pose.popPose();
      }
   }
}
