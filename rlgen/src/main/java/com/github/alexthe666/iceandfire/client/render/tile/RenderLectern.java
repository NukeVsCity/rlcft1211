package com.github.alexthe666.iceandfire.client.render.tile;

import com.github.alexthe666.iceandfire.block.BlockLectern;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityLectern;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 1.12 RenderLectern: the enchanting table book on top, flipping its pages. Quirk kept: 1.12 interpolated the page flip with the
 * destroy stage argument (-1) instead of the partial tick.
 */
public class RenderLectern implements BlockEntityRenderer<TileEntityLectern> {
   private static final ResourceLocation bookTex = ResourceLocation.withDefaultNamespace("textures/entity/enchanting_table_book.png");
   private final BookModel book;

   public RenderLectern(BlockEntityRendererProvider.Context ctx) {
      this.book = new BookModel(ctx.bakeLayer(ModelLayers.BOOK));
   }

   @Override
   public void render(TileEntityLectern lectern, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
      float yee = -1.0F;
      pose.pushPose();
      pose.translate(0.5F, 1.07F, 0.5F);
      pose.scale(0.8F, 0.8F, 0.8F);
      pose.mulPose(Axis.YP.rotationDegrees(getRotation(lectern)));
      pose.mulPose(Axis.ZP.rotationDegrees(112.5F));
      pose.mulPose(Axis.XP.rotationDegrees(180.0F));
      float f4 = lectern.pageFlipPrev + (lectern.pageFlip - lectern.pageFlipPrev) * yee + 0.25F;
      float f5 = lectern.pageFlipPrev + (lectern.pageFlip - lectern.pageFlipPrev) * yee + 0.75F;
      f4 = Mth.clamp((f4 - (float)Mth.floor((double)f4)) * 1.6F - 0.3F, 0.0F, 1.0F);
      f5 = Mth.clamp((f5 - (float)Mth.floor((double)f5)) * 1.6F - 0.3F, 0.0F, 1.0F);
      // 1.12 ModelBook.render(null, 0, f4, f5, 1, 0, 1/16): limbSwing 0 = time, f4/f5 = page flips, 1 = fully open
      this.book.setupAnim(0.0F, f4, f5, 1.0F);
      this.book.renderToBuffer(pose, buffers.getBuffer(RenderType.entitySolid(bookTex)), light, overlay);
      pose.popPose();
   }

   /** 1.12 getRotation by block metadata = horizontal index (0 south, 1 west, 2 north, 3 east). */
   private static float getRotation(TileEntityLectern lectern) {
      Direction d = lectern.getBlockState().hasProperty(BlockLectern.FACING) ? lectern.getBlockState().getValue(BlockLectern.FACING) : Direction.SOUTH;
      return switch (d.get2DDataValue()) {
         case 1 -> 0.0F;
         case 2 -> -90.0F;
         case 3 -> 180.0F;
         default -> 90.0F;
      };
   }
}
