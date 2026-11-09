package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.client.model.ModelFireSkull;
import com.github.alexthe666.iceandfire.client.model.ModelIceSkull;
import com.github.alexthe666.iceandfire.entity.EntityDragonSkull;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** 1.12 RenderDragonSkull: the fire skull model (which hands ice skulls to the ice skull model), stage-4 skeleton textures. */
public class RenderDragonSkull extends MobRenderer<EntityDragonSkull, EntityModel<EntityDragonSkull>> {
   public static final ResourceLocation SKULL_FIRE = ResourceLocation.parse("iceandfire:textures/models/firedragon/fire_skeleton_4.png");
   public static final ResourceLocation SKULL_ICE = ResourceLocation.parse("iceandfire:textures/models/icedragon/ice_skeleton_4.png");
   public static final ModelFireSkull FIRE_SKULL_MODEL = new ModelFireSkull();
   public static final ModelIceSkull ICE_SKULL_MODEL = new ModelIceSkull();

   @SuppressWarnings("unchecked")
   public RenderDragonSkull(EntityRendererProvider.Context context) {
      super(context, (EntityModel<EntityDragonSkull>)(EntityModel<?>)FIRE_SKULL_MODEL, 0.3F);
   }

   @Override
   protected void scale(EntityDragonSkull skull, PoseStack poseStack, float partialTicks) {
      poseStack.scale(skull.getDragonSize(), 1.0F, skull.getDragonSize());
      poseStack.scale(1.0F, -skull.getDragonSize(), 1.0F);
      poseStack.mulPose(Axis.YP.rotationDegrees(skull.getYaw()));
   }

   @Override
   public ResourceLocation getTextureLocation(EntityDragonSkull entity) {
      return entity.getSkullType() == 0 ? SKULL_FIRE : SKULL_ICE;
   }
}
