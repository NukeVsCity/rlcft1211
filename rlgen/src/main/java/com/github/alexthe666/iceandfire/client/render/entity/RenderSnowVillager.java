package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.entity.AirWaterRefs;
import com.github.alexthe666.iceandfire.entity.EntitySnowVillager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Ice and Fire 1.7.1 RenderSnowVillager: the villager model with the profession's snow villager texture (VillagerProfession skins), and
 * the fisherman's fishing spear drawn on his back.
 */
public class RenderSnowVillager extends MobRenderer<EntitySnowVillager, VillagerModel<EntitySnowVillager>> {
   public RenderSnowVillager(EntityRendererProvider.Context ctx) {
      super(ctx, new VillagerModel<>(ctx.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
      this.addLayer(new CustomHeadLayer<>(this, ctx.getModelSet(), ctx.getItemInHandRenderer()));
      this.addLayer(new CrossedArmsItemLayer<>(this, ctx.getItemInHandRenderer()));
      this.addLayer(new SpearLayer(this, ctx));
   }

   @Override
   public ResourceLocation getTextureLocation(EntitySnowVillager entity) {
      String name = switch (entity.getProfessionId()) {
         case 1 -> "craftsman";
         case 2 -> "shaman";
         default -> "fisherman";
      };
      return ResourceLocation.fromNamespaceAndPath("iceandfire", "textures/models/snowvillager/" + name + ".png");
   }

   /** RenderVillager.preRenderCallback: 0.9375, children half size */
   @Override
   protected void scale(EntitySnowVillager entity, PoseStack pose, float partialTick) {
      float f = 0.9375F;
      if (entity.isBaby()) {
         f *= 0.5F;
         this.shadowRadius = 0.25F;
      } else {
         this.shadowRadius = 0.5F;
      }
      pose.scale(f, f, f);
   }

   static class SpearLayer extends RenderLayer<EntitySnowVillager, VillagerModel<EntitySnowVillager>> {
      private final net.minecraft.client.renderer.entity.ItemRenderer items;

      SpearLayer(RenderSnowVillager parent, EntityRendererProvider.Context ctx) {
         super(parent);
         this.items = ctx.getItemRenderer();
      }

      @Override
      public void render(PoseStack pose, MultiBufferSource buffers, int light, EntitySnowVillager entity, float limbSwing, float limbSwingAmount,
            float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
         if (entity.getProfessionId() != 0) {
            return;
         }
         ItemStack spear = new ItemStack(AirWaterRefs.item("fishing_spear"));
         if (spear.isEmpty()) {
            return;
         }
         pose.pushPose();
         pose.translate(0.125F, -1.0F, -0.3F);
         pose.mulPose(Axis.XP.rotationDegrees(-80.0F));
         pose.mulPose(Axis.YP.rotationDegrees(10.0F));
         pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
         this.items.renderStatic(spear, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, light, OverlayTexture.NO_OVERLAY, pose, buffers, entity.level(), entity.getId());
         pose.popPose();
      }
   }
}
