package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import com.lycanitesmobs.core.entity.EntityParticle;
import com.lycanitesmobs.core.entity.EntityProjectileLaser;
import com.lycanitesmobs.core.entity.LaserEndProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.ilexiconn.llibrary.client.model.legacy.ModelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * 1.12 RenderProjectileSprite: a camera facing textured quad (animated by rows of the texture) or a laser beam built from
 * 16 long 1.12 model boxes. 1.12 drew these with lighting on, alpha test and no blending; the entity is already
 * translated to its render position by the 1.21 dispatcher (1.12 translate(x, y, z)).
 */
public class RenderProjectileSprite extends EntityRenderer<Entity> {
   private float scale;
   private int renderTime = 0;
   Class<?> projectileClass;
   protected ModelBase laserModel = new ModelBase() {
   };
   private ModelRenderer laserBox;

   public RenderProjectileSprite(EntityRendererProvider.Context context, Class<?> projectileClass) {
      super(context);
      this.projectileClass = projectileClass;
   }

   @Override
   public void render(Entity entity, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      if (this.renderTime++ > 2147483646) {
         this.renderTime = 0;
      }

      if (entity instanceof CustomProjectileEntity custom && custom.getLaserEnd() != null) {
         this.renderLaser(custom, pose, buffers, packedLight, custom.laserWidth);
      } else if (entity instanceof EntityProjectileLaser laser) {
         this.renderOldLaser(laser, laser.getLaserEnd(), pose, buffers, packedLight, laser.getLaserWidth(), laser.getLength());
      } else {
         this.renderProjectile(entity, pose, buffers, packedLight);
      }
   }

   public void renderProjectile(Entity entity, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      double scale = 1.0;
      double y = 0.0;
      if (!(entity instanceof CustomProjectileEntity) || ((CustomProjectileEntity)entity).projectileInfo != null) {
         if (entity instanceof BaseProjectileEntity baseProjectileEntity) {
            scale *= (double)baseProjectileEntity.getProjectileScale();
            y += (double)baseProjectileEntity.getTextureOffsetY();
         }

         ResourceLocation texture = this.getTextureLocation(entity);
         if (texture != null) {
            pose.pushPose();
            pose.translate(0.0, y, 0.0);
            pose.scale((float)scale, (float)scale, (float)scale);
            this.renderTexture(entity, texture, pose, buffers, packedLight);
            pose.popPose();
         }
      }
   }

   private void renderTexture(Entity entity, ResourceLocation texture, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      float minU = 0.0F;
      float maxU = 1.0F;
      float minV = 0.0F;
      float maxV = 1.0F;
      double textureWidth = 0.5;
      double textureHeight = 0.5;
      double offsetY = 0.0;
      if (entity instanceof BaseProjectileEntity entityProjectile) {
         if (entityProjectile.animationFrameMax > 0) {
            minV = (float)entityProjectile.animationFrame / (float)entityProjectile.animationFrameMax;
            maxV = minV + 1.0F / (float)entityProjectile.animationFrameMax;
            textureWidth *= (double)entityProjectile.textureScale;
            textureHeight *= (double)entityProjectile.textureScale;
            offsetY = (double)entityProjectile.textureOffsetY;
         }
      }

      // 1.12: rotate(-playerViewY, Y), rotate(+-playerViewX, X), rotate(180, Y)
      pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
      pose.mulPose(Axis.YP.rotationDegrees(180.0F));
      pose.translate(-this.scale / 2.0F, -this.scale / 2.0F, -this.scale / 2.0F);
      // no culling: the winding of 1.12's billboard depends on its rotations, the quad always faces the camera anyway
      VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
      PoseStack.Pose last = pose.last();
      vertex(buffer, last, -textureWidth, -textureHeight + textureHeight / 2.0 + offsetY, minU, maxV, packedLight);
      vertex(buffer, last, textureWidth, -textureHeight + textureHeight / 2.0 + offsetY, maxU, maxV, packedLight);
      vertex(buffer, last, textureWidth, textureHeight + textureHeight / 2.0 + offsetY, maxU, minV, packedLight);
      vertex(buffer, last, -textureWidth, textureHeight + textureHeight / 2.0 + offsetY, minU, minV, packedLight);
   }

   private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, double x, double y, float u, float v, int light) {
      buffer.addVertex(pose, (float)x, (float)y, 0.0F)
         .setColor(255, 255, 255, 255)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(light)
         .setNormal(pose, 0.0F, 1.0F, 0.0F);
   }

   private void ensureLaserBox(float scale) {
      if (this.laserBox == null) {
         this.laserBox = new ModelRenderer(this.laserModel, 0, 0);
         this.laserBox.addBox(-(scale / 2.0F), -(scale / 2.0F), 0.0F, (int)scale, (int)scale, 16);
         this.laserBox.rotationPointX = 0.0F;
         this.laserBox.rotationPointY = 0.0F;
         this.laserBox.rotationPointZ = 0.0F;
      }
   }

   public void renderLaser(CustomProjectileEntity entity, PoseStack pose, MultiBufferSource buffers, int packedLight, float scale) {
      this.ensureLaserBox(scale);
      float factor = 0.0625F;
      float lastSegment = 0.0F;
      Vec3 laserEnd = entity.getLaserEnd();
      double length = entity.position().distanceTo(laserEnd);
      if (!(length <= 0.0)) {
         float dx = (float)(laserEnd.x - entity.getX());
         float dy = (float)(laserEnd.y - entity.getY());
         float dz = (float)(laserEnd.z - entity.getZ());
         this.drawLaser(entity, pose, buffers, packedLight, dx, dy, dz, length, factor, lastSegment);
      }
   }

   public void renderOldLaser(BaseProjectileEntity entity, LaserEndProjectileEntity laserEnd, PoseStack pose, MultiBufferSource buffers, int packedLight, float scale, float length) {
      this.ensureLaserBox(scale);
      float factor = 0.0625F;
      if (!(length <= 0.0F)) {
         float dx = 0.0F;
         float dy = 0.0F;
         float dz = 0.0F;
         boolean hasEnd = laserEnd != null;
         if (hasEnd) {
            dx = (float)(laserEnd.getX() - entity.getX());
            dy = (float)(laserEnd.getY() - entity.getY());
            dz = (float)(laserEnd.getZ() - entity.getZ());
         }
         this.drawOldLaser(entity, pose, buffers, packedLight, hasEnd, dx, dy, dz, length, factor);
      }
   }

   private void drawLaser(BaseProjectileEntity entity, PoseStack pose, MultiBufferSource buffers, int packedLight, float dx, float dy, float dz, double length, float factor, float lastSegment) {
      pose.pushPose();
      LegacyRender.push(pose, buffers.getBuffer(RenderType.entityCutout(this.getLaserTexture(entity))), packedLight, OverlayTexture.NO_OVERLAY, -1);
      try {
         float[] angles = laserAngles(dx, dy, dz);
         LegacyRender.rotate(angles[1], 0.0F, 1.0F, 0.0F);
         LegacyRender.rotate(angles[3], 1.0F, 0.0F, 0.0F);

         for (float segment = 0.0F; (double)segment <= length - 1.0; lastSegment = segment++) {
            this.laserBox.render(factor);
            LegacyRender.translate(0.0F, 0.0F, 1.0F);
         }

         lastSegment++;
         LegacyRender.scale(length - (double)lastSegment, 1.0, 1.0);
         this.laserBox.render(factor);
      } finally {
         LegacyRender.pop();
         pose.popPose();
      }
   }

   private void drawOldLaser(BaseProjectileEntity entity, PoseStack pose, MultiBufferSource buffers, int packedLight, boolean hasEnd, float dx, float dy, float dz, float length, float factor) {
      float lastSegment = 0.0F;
      pose.pushPose();
      LegacyRender.push(pose, buffers.getBuffer(RenderType.entityCutout(this.getLaserTexture(entity))), packedLight, OverlayTexture.NO_OVERLAY, -1);
      try {
         float[] angles = hasEnd ? laserAngles(dx, dy, dz) : new float[]{0.0F, 0.0F, 0.0F, 0.0F};
         LegacyRender.rotate(angles[1], 0.0F, 1.0F, 0.0F);
         LegacyRender.rotate(angles[3], 1.0F, 0.0F, 0.0F);

         for (float segment = 0.0F; segment <= length - 1.0F; lastSegment = segment++) {
            this.laserBox.render(factor);
            LegacyRender.translate(0.0F, 0.0F, 1.0F);
         }

         lastSegment++;
         LegacyRender.scale(length - lastSegment, 1.0F, 1.0F);
         this.laserBox.render(factor);
      } finally {
         LegacyRender.pop();
         pose.popPose();
      }
   }

   private static float[] laserAngles(float dx, float dy, float dz) {
      float[] angles = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
      angles[0] = (float)Math.toDegrees(Math.atan2((double)dz, (double)dy)) - 90.0F;
      angles[1] = (float)Math.toDegrees(Math.atan2((double)dx, (double)dz));
      angles[2] = (float)Math.toDegrees(Math.atan2((double)dx, (double)dy)) - 90.0F;
      float dr = (float)Math.sqrt((double)(dx * dx + dz * dz));
      angles[3] = (float)Math.toDegrees(Math.atan2((double)dr, (double)dy)) - 90.0F;
      return angles;
   }

   @Override
   public ResourceLocation getTextureLocation(Entity entity) {
      if (entity instanceof BaseProjectileEntity projectile) {
         return projectile.getTexture();
      } else {
         return entity instanceof EntityParticle particle ? particle.getTexture() : null;
      }
   }

   protected ResourceLocation getLaserTexture(BaseProjectileEntity entity) {
      if (entity instanceof CustomProjectileEntity custom) {
         return custom.getBeamTexture();
      } else {
         return entity instanceof EntityProjectileLaser laser ? laser.getBeamTexture() : this.getTextureLocation(entity);
      }
   }
}
