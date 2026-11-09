package com.github.alexthe666.iceandfire.client.render.tile;

import com.github.alexthe666.iceandfire.block.BlockPixieHouse;
import com.github.alexthe666.iceandfire.client.model.ModelPixieHouse;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityPixieHouse;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** 1.12 RenderPixieHouse: the house model (6 textures by house type), facing, and the resident pixie (IafRenderHooks.pixie). */
public class RenderPixieHouse implements BlockEntityRenderer<TileEntityPixieHouse> {
   private static final ModelPixieHouse MODEL = new ModelPixieHouse();
   private static final ResourceLocation[] TEXTURES = new ResourceLocation[6];

   static {
      for (int i = 0; i < 6; i++) {
         TEXTURES[i] = ResourceLocation.parse("iceandfire:textures/models/pixie/house/pixie_house_" + i + ".png");
      }
   }

   public RenderPixieHouse(BlockEntityRendererProvider.Context ctx) {
   }

   @Override
   public void render(TileEntityPixieHouse entity, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
      int rotation = 0;
      if (entity.getBlockState().getBlock() instanceof BlockPixieHouse) {
         Direction facing = entity.getBlockState().getValue(BlockPixieHouse.FACING);
         if (facing == Direction.NORTH) {
            rotation = 180;
         }
         if (facing == Direction.EAST) {
            rotation = -90;
         }
         if (facing == Direction.WEST) {
            rotation = 90;
         }
      }
      renderHouse(pose, buffers, light, overlay, entity.houseType, rotation, entity);
   }

   public static void renderHouse(PoseStack pose, MultiBufferSource buffers, int light, int overlay, int meta, int rotation, TileEntityPixieHouse entity) {
      pose.pushPose();
      pose.translate(0.5F, 1.501F, 0.5F);
      pose.mulPose(Axis.XP.rotationDegrees(180.0F));
      pose.mulPose(Axis.YP.rotationDegrees((float)rotation));
      if (entity != null && entity.hasPixie && IafRenderHooks.pixie != null) {
         pose.pushPose();
         pose.translate(0.0F, 0.95F, 0.0F);
         pose.scale(0.55F, 0.55F, 0.55F);
         IafRenderHooks.pixie.render(pose, buffers, 0xF000F0, overlay, entity.pixieType, true, entity);
         pose.popPose();
      }
      LegacyRender.push(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURES[Math.max(0, Math.min(5, meta))])), light, overlay, -1);
      try {
         MODEL.render(0.0625F);
      } finally {
         LegacyRender.pop();
      }
      pose.popPose();
   }
}
