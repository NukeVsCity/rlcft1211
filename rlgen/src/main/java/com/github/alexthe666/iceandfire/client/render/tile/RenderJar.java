package com.github.alexthe666.iceandfire.client.render.tile;

import com.github.alexthe666.iceandfire.entity.tile.TileEntityJar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** 1.12 RenderJar: the pixie inside a pixie jar (ModelPixie.animateInJar through IafRenderHooks.pixie); the glass is the block model. */
public class RenderJar implements BlockEntityRenderer<TileEntityJar> {
   public RenderJar(BlockEntityRendererProvider.Context ctx) {
   }

   @Override
   public void render(TileEntityJar entity, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
      if (entity.hasPixie && IafRenderHooks.pixie != null) {
         pose.pushPose();
         pose.translate(0.5F, 1.501F, 0.5F);
         pose.mulPose(Axis.XP.rotationDegrees(180.0F));
         IafRenderHooks.pixie.render(pose, buffers, 0xF000F0, overlay, entity.pixieType, false, entity);
         pose.popPose();
      }
   }
}
