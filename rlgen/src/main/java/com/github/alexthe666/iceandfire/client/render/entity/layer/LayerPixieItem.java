package com.github.alexthe666.iceandfire.client.render.entity.layer;

import com.github.alexthe666.iceandfire.entity.EntityPixie;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Ice and Fire 1.7.1 LayerPixieItem: the stolen item, carried in front of the pixie (the 1.12 transforms, GROUND item transform). */
public class LayerPixieItem extends RenderLayer<EntityPixie, EntityModel<EntityPixie>> {
   private final ItemInHandRenderer items;

   public LayerPixieItem(RenderLayerParent<EntityPixie, EntityModel<EntityPixie>> parent, ItemInHandRenderer items) {
      super(parent);
      this.items = items;
   }

   @Override
   public void render(PoseStack pose, MultiBufferSource buffers, int light, EntityPixie entity, float limbSwing, float limbSwingAmount, float partialTick,
         float ageInTicks, float netHeadYaw, float headPitch) {
      ItemStack itemstack = entity.getItemInHand(InteractionHand.MAIN_HAND);
      if (!itemstack.isEmpty()) {
         pose.pushPose();
         if (this.getParentModel().young) {
            pose.translate(0.0F, 0.625F, 0.0F);
            pose.mulPose(Axis.of(new org.joml.Vector3f(-1.0F, 0.0F, 0.0F)).rotationDegrees(-20.0F));
            pose.scale(0.5F, 0.5F, 0.5F);
         }
         pose.translate(-0.0625F, 0.53125F, 0.21875F);
         pose.translate(-0.075F, 0.0F, -0.05F);
         pose.mulPose(Axis.YP.rotationDegrees(-10.0F));
         pose.translate(0.05F, 0.55F, -0.4F);
         pose.mulPose(Axis.XP.rotationDegrees(180.0F));
         pose.mulPose(Axis.ZP.rotationDegrees(140.0F));
         pose.mulPose(Axis.XP.rotationDegrees(12.0F));
         pose.mulPose(Axis.YP.rotationDegrees(180.0F));
         pose.mulPose(Axis.XP.rotationDegrees(220.0F));
         pose.mulPose(Axis.XP.rotationDegrees(-15.0F));
         pose.mulPose(Axis.ZP.rotationDegrees(40.0F));
         this.items.renderItem(entity, itemstack, ItemDisplayContext.GROUND, false, pose, buffers, light);
         pose.popPose();
      }
   }
}
