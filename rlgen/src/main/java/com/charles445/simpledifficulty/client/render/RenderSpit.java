package com.charles445.simpledifficulty.client.render;

import com.charles445.simpledifficulty.tileentity.TileEntitySpit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** 1.12 RenderSpit: the items in a row along the spit, rotated 45 degrees, rendered as dropped items (GROUND). */
public class RenderSpit implements BlockEntityRenderer<TileEntitySpit> {
   private final ItemRenderer itemRenderer;

   public RenderSpit(BlockEntityRendererProvider.Context ctx) {
      this.itemRenderer = Minecraft.getInstance().getItemRenderer();
   }

   @Override
   public void render(TileEntitySpit te, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
      pose.pushPose();
      pose.mulPose(Axis.YP.rotationDegrees(45.0F));
      int slots = te.items.getSlots();
      double separationAmt = slots > 1 ? 0.8141999999999998 / (double)(slots - 1) : 0.0;
      for (int i = 0; i < slots; i++) {
         ItemStack stack = te.items.getStackInSlot(i);
         if (!stack.isEmpty()) {
            pose.pushPose();
            pose.translate(0.0, 0.0, (double)i * separationAmt + 0.3);
            this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, pose, buffers, te.getLevel(), 0);
            pose.popPose();
         }
      }
      pose.popPose();
   }
}
