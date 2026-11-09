package com.github.alexthe666.iceandfire.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/**
 * 1.12 RenderDragonFireCharge: a magma block (fire charge) or a dragon_ice block (ice charge) spinning ticksExisted * 7 degrees
 * around the (1, 1, 1) axis, shifted by (-0.5, 0, 0.5), at the entity's brightness.
 */
public class RenderDragonFireCharge<T extends Entity> extends EntityRenderer<T> {
   private static final ResourceLocation DRAGON_ICE = ResourceLocation.parse("iceandfire:dragon_ice");
   public boolean isFire;

   public RenderDragonFireCharge(EntityRendererProvider.Context context, boolean isFire) {
      super(context);
      this.isFire = isFire;
   }

   @Override
   public ResourceLocation getTextureLocation(T entity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }

   private BlockState state() {
      if (this.isFire) {
         return Blocks.MAGMA_BLOCK.defaultBlockState();
      }
      Block ice = BuiltInRegistries.BLOCK.get(DRAGON_ICE);
      // dragon_ice is registered by the iceandfire block port; plain ice until then
      return (ice == Blocks.AIR ? Blocks.ICE : ice).defaultBlockState();
   }

   @Override
   public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
      poseStack.pushPose();
      poseStack.mulPose(Axis.of(new Vector3f(1.0F, 1.0F, 1.0F).normalize()).rotationDegrees((float)(entity.tickCount * 7)));
      poseStack.translate(-0.5F, 0.0F, 0.5F);
      // 1.12 renderBlockBrightness draws the block model with its block-space origin at the current position
      net.minecraft.client.Minecraft.getInstance().getBlockRenderer()
         .renderSingleBlock(this.state(), poseStack, buffers, light, OverlayTexture.NO_OVERLAY);
      poseStack.popPose();
      super.render(entity, entityYaw, partialTicks, poseStack, buffers, light);
   }
}
