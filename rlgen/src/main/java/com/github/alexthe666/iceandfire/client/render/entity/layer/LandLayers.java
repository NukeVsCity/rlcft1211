package com.github.alexthe666.iceandfire.client.render.entity.layer;

import com.github.alexthe666.iceandfire.client.model.ModelMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityGorgon;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexWorker;
import com.github.alexthe666.iceandfire.entity.EntityTroll;
import com.github.alexthe666.iceandfire.entity.LandUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** The 1.12 layers of the land mobs: gorgon eyes, troll weapon / eyes / stone, myrmex worker's held item. */
public final class LandLayers {
   private LandLayers() {
   }

   private static <T extends net.minecraft.world.entity.LivingEntity> void legacy(EntityModel<T> model, T e, PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers,
      RenderType type, int light, float limbSwing, float limbSwingAmount, float ageInTicks, float yaw, float pitch) {
      ((ModelBase)(Object)model).renderLegacy(pose, buffers.getBuffer(type), light, OverlayTexture.NO_OVERLAY, -1, e, limbSwing, limbSwingAmount, ageInTicks, yaw, pitch, 0.0625F);
   }

   /** LayerGorgonEyes: additive, full bright, while scaring or hitting */
   public static class GorgonEyes extends RenderLayer<EntityGorgon, EntityModel<EntityGorgon>> {
      private static final ResourceLocation TEXTURE = ResourceLocation.parse("iceandfire:textures/models/gorgon/gorgon_eyes.png");

      public GorgonEyes(RenderLayerParent<EntityGorgon, EntityModel<EntityGorgon>> parent) {
         super(parent);
      }

      @Override
      public void render(PoseStack pose, MultiBufferSource buffers, int light, EntityGorgon gorgon, float f, float f1, float pt, float age, float yaw, float pitch) {
         if (!LandUtils.isStone(gorgon) && (gorgon.getAnimation() == EntityGorgon.ANIMATION_SCARE || gorgon.getAnimation() == EntityGorgon.ANIMATION_HIT)) {
            legacy(this.getParentModel(), gorgon, pose, buffers, RenderType.eyes(TEXTURE), 0xF000F0, f, f1, age, yaw, pitch);
         }
      }
   }

   /** LayerTrollEyes */
   public static class TrollEyes extends RenderLayer<EntityTroll, EntityModel<EntityTroll>> {
      public TrollEyes(RenderLayerParent<EntityTroll, EntityModel<EntityTroll>> parent) {
         super(parent);
      }

      @Override
      public void render(PoseStack pose, MultiBufferSource buffers, int light, EntityTroll troll, float f, float f1, float pt, float age, float yaw, float pitch) {
         if (!LandUtils.isStone(troll)) {
            legacy(this.getParentModel(), troll, pose, buffers, RenderType.eyes(troll.getTrollType().TEXTURE_EYES), 0xF000F0, f, f1, age, yaw, pitch);
         }
      }
   }

   /** LayerTrollWeapon: the troll model again with the weapon texture */
   public static class TrollWeapon extends RenderLayer<EntityTroll, EntityModel<EntityTroll>> {
      public TrollWeapon(RenderLayerParent<EntityTroll, EntityModel<EntityTroll>> parent) {
         super(parent);
      }

      @Override
      public void render(PoseStack pose, MultiBufferSource buffers, int light, EntityTroll troll, float f, float f1, float pt, float age, float yaw, float pitch) {
         if (troll.getWeaponType() != null && !LandUtils.isStone(troll)) {
            legacy(this.getParentModel(), troll, pose, buffers, RenderType.entityCutoutNoCull(troll.getWeaponType().TEXTURE), light, f, f1, age, yaw, pitch);
         }
      }
   }

   /** LayerTrollStone (the troll's own stone texture) */
   public static class TrollStone extends RenderLayer<EntityTroll, EntityModel<EntityTroll>> {
      public TrollStone(RenderLayerParent<EntityTroll, EntityModel<EntityTroll>> parent) {
         super(parent);
      }

      @Override
      public void render(PoseStack pose, MultiBufferSource buffers, int light, EntityTroll troll, float f, float f1, float pt, float age, float yaw, float pitch) {
         if (LandUtils.isStone(troll)) {
            legacy(this.getParentModel(), troll, pose, buffers, RenderType.entityCutout(troll.getTrollType().TEXTURE_STONE), light, f, 0.0F, 0.0F, yaw, pitch);
         }
      }
   }

   /** LayerMyrmexItem: a worker's carried item, at the head */
   public static class MyrmexItem extends RenderLayer<EntityMyrmexBase, EntityModel<EntityMyrmexBase>> {
      public MyrmexItem(RenderLayerParent<EntityMyrmexBase, EntityModel<EntityMyrmexBase>> parent) {
         super(parent);
      }

      @Override
      public void render(PoseStack pose, MultiBufferSource buffers, int light, EntityMyrmexBase entity, float f, float f1, float pt, float age, float yaw, float pitch) {
         if (entity instanceof EntityMyrmexWorker) {
            ItemStack stack = entity.getItemInHand(InteractionHand.MAIN_HAND);
            if (!stack.isEmpty()) {
               pose.pushPose();
               if (entity.isShiftKeyDown()) {
                  pose.translate(0.0F, 0.2F, 0.0F);
               }
               LegacyRender.push(pose, null, light, OverlayTexture.NO_OVERLAY, -1);
               try {
                  ((ModelMyrmexBase)(Object)this.getParentModel()).postRenderArm(0.0625F, HumanoidArm.RIGHT);
               } finally {
                  LegacyRender.pop();
               }
               if (Minecraft.getInstance().getItemRenderer().getModel(stack, entity.level(), entity, 0).isGui3d()) {
                  pose.translate(0.0F, 0.25F, -1.65F);
               } else {
                  pose.translate(0.0F, 1.0F, -2.0F);
               }
               pose.mulPose(Axis.XP.rotationDegrees(160.0F));
               pose.mulPose(Axis.YP.rotationDegrees(180.0F));
               Minecraft.getInstance().getItemRenderer().renderStatic(entity, stack, ItemDisplayContext.HEAD, false, pose, buffers, entity.level(), light, OverlayTexture.NO_OVERLAY, entity.getId());
               pose.popPose();
            }
         }
      }
   }
}
