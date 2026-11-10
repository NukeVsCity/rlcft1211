package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.client.model.ModelCockatrice;
import com.github.alexthe666.iceandfire.entity.EntityCockatrice;
import com.github.alexthe666.iceandfire.entity.EntityGorgon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** 1.12 RenderCockatrice: half size chicks, and the stare beam (beam.png, like the guardian's) while both look at each other. */
public class RenderCockatrice extends RenderLandMob<EntityCockatrice> {
   public static final ResourceLocation TEXTURE_ROOSTER = ResourceLocation.parse("iceandfire:textures/models/cockatrice/cockatrice_0.png");
   public static final ResourceLocation TEXTURE_HEN = ResourceLocation.parse("iceandfire:textures/models/cockatrice/cockatrice_1.png");
   private static final ResourceLocation TEXTURE_BEAM = ResourceLocation.parse("iceandfire:textures/models/cockatrice/beam.png");
   public static final ResourceLocation TEXTURE_ROOSTER_CHICK = ResourceLocation.parse("iceandfire:textures/models/cockatrice/cockatrice_0_chick.png");
   public static final ResourceLocation TEXTURE_HEN_CHICK = ResourceLocation.parse("iceandfire:textures/models/cockatrice/cockatrice_1_chick.png");

   public RenderCockatrice(EntityRendererProvider.Context context) {
      super(context, new ModelCockatrice(), 0.6F, c -> c.isBaby() ? (c.isHen() ? TEXTURE_HEN_CHICK : TEXTURE_ROOSTER_CHICK) : (c.isHen() ? TEXTURE_HEN : TEXTURE_ROOSTER),
         (c, pose, pt) -> {
            if (c.isBaby()) {
               pose.scale(0.5F, 0.5F, 0.5F);
            }
         });
   }

   private static Vec3 getPosition(LivingEntity e, double yOffset, float pt) {
      return new Vec3(e.xOld + (e.getX() - e.xOld) * pt, yOffset + e.yOld + (e.getY() - e.yOld) * pt, e.zOld + (e.getZ() - e.zOld) * pt);
   }

   @Override
   public boolean shouldRender(EntityCockatrice livingEntity, Frustum camera, double camX, double camY, double camZ) {
      if (super.shouldRender(livingEntity, camera, camX, camY, camZ)) {
         return true;
      } else {
         if (livingEntity.hasTargetedEntity()) {
            LivingEntity target = livingEntity.getTargetedEntity();
            if (target != null) {
               Vec3 vec3d = getPosition(target, (double)target.getBbHeight() * 0.5, 1.0F);
               Vec3 vec3d1 = getPosition(livingEntity, (double)livingEntity.getEyeHeight(), 1.0F);
               return camera.isVisible(new AABB(vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x, vec3d.y, vec3d.z));
            }
         }
         return false;
      }
   }

   @Override
   public void render(EntityCockatrice entity, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
      super.render(entity, entityYaw, partialTicks, pose, buffers, light);
      LivingEntity target = entity.getTargetedEntity();
      boolean blindness = entity.hasEffect(MobEffects.BLINDNESS) || target != null && target.hasEffect(MobEffects.BLINDNESS);
      if (!blindness && target != null && EntityGorgon.isEntityLookingAt(entity, target, 0.6F) && EntityGorgon.isEntityLookingAt(target, entity, 0.6F)) {
         float f2 = (float)entity.level().getGameTime() + partialTicks;
         float f3 = f2 * 0.15F % 1.0F;
         float f4 = entity.getEyeHeight();
         pose.pushPose();
         pose.translate(0.0F, f4, 0.0F);
         Vec3 vec3d = getPosition(target, (double)target.getBbHeight() * 0.5, partialTicks);
         Vec3 vec3d1 = getPosition(entity, (double)f4, partialTicks);
         Vec3 vec3d2 = vec3d.subtract(vec3d1);
         double d0 = vec3d2.length() + 1.0;
         vec3d2 = vec3d2.normalize();
         float f5 = (float)Math.acos(vec3d2.y);
         float f6 = (float)Math.atan2(vec3d2.z, vec3d2.x);
         pose.mulPose(Axis.YP.rotationDegrees(((float)(Math.PI / 2) + -f6) * (180.0F / (float)Math.PI)));
         pose.mulPose(Axis.XP.rotationDegrees(f5 * (180.0F / (float)Math.PI)));
         double d1 = (double)f2 * 0.05 * -1.5;
         int c = 225;
         double d4 = Math.cos(d1 + (Math.PI * 3.0 / 4.0)) * 0.282;
         double d5 = Math.sin(d1 + (Math.PI * 3.0 / 4.0)) * 0.282;
         double d6 = Math.cos(d1 + (Math.PI / 4)) * 0.282;
         double d7 = Math.sin(d1 + (Math.PI / 4)) * 0.282;
         double d8 = Math.cos(d1 + (Math.PI * 5.0 / 4.0)) * 0.282;
         double d9 = Math.sin(d1 + (Math.PI * 5.0 / 4.0)) * 0.282;
         double d10 = Math.cos(d1 + (Math.PI * 7.0 / 4.0)) * 0.282;
         double d11 = Math.sin(d1 + (Math.PI * 7.0 / 4.0)) * 0.282;
         double d12 = Math.cos(d1 + Math.PI) * 0.2;
         double d13 = Math.sin(d1 + Math.PI) * 0.2;
         double d14 = Math.cos(d1 + 0.0) * 0.2;
         double d15 = Math.sin(d1 + 0.0) * 0.2;
         double d16 = Math.cos(d1 + (Math.PI / 2)) * 0.2;
         double d17 = Math.sin(d1 + (Math.PI / 2)) * 0.2;
         double d18 = Math.cos(d1 + (Math.PI * 3.0 / 2.0)) * 0.2;
         double d19 = Math.sin(d1 + (Math.PI * 3.0 / 2.0)) * 0.2;
         double d22 = (double)(-1.0F + f3);
         double d23 = d0 * 2.5 + d22;
         // 1.12: additive blending, no cull, full bright
         VertexConsumer b = buffers.getBuffer(RenderType.beaconBeam(TEXTURE_BEAM, true));
         Matrix4f m = pose.last().pose();
         v(b, m, d12, d0, d13, 0.4999, d23, c);
         v(b, m, d12, 0.0, d13, 0.4999, d22, c);
         v(b, m, d14, 0.0, d15, 0.0, d22, c);
         v(b, m, d14, d0, d15, 0.0, d23, c);
         v(b, m, d16, d0, d17, 0.4999, d23, c);
         v(b, m, d16, 0.0, d17, 0.4999, d22, c);
         v(b, m, d18, 0.0, d19, 0.0, d22, c);
         v(b, m, d18, d0, d19, 0.0, d23, c);
         double d24 = entity.tickCount % 2 == 0 ? 0.5 : 0.0;
         v(b, m, d4, d0, d5, 0.5, d24 + 0.5, c);
         v(b, m, d6, d0, d7, 1.0, d24 + 0.5, c);
         v(b, m, d10, d0, d11, 1.0, d24, c);
         v(b, m, d8, d0, d9, 0.5, d24, c);
         pose.popPose();
      }
   }

   private static void v(VertexConsumer b, Matrix4f m, double x, double y, double z, double u, double v, int c) {
      b.addVertex(m, (float)x, (float)y, (float)z).setColor(c, c, c, 255).setUv((float)u, (float)v).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
   }
}
