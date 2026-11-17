package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.client.AssetManager;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * 1.12 RenderProjectileModel: the projectile's OBJ model (AssetManager.getProjectileModel) at half size, turned by the
 * entity yaw, rendered with render(entity, 0, 0, partialTicks, 0, 0, 1) (1.12 passes partialTicks as the loop time and,
 * a projectile not being an EntityLiving, ModelCreatureObj never animates its parts).
 */
public class RenderProjectileModel extends EntityRenderer<Entity> {
   public ModelBase mainModel;

   public RenderProjectileModel(EntityRendererProvider.Context context) {
      super(context);
   }

   public RenderProjectileModel(EntityRendererProvider.Context context, String projectileName) {
      super(context);
   }

   @Override
   public void render(Entity entity, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      if (entity instanceof CustomProjectileEntity custom) {
         ProjectileInfo projectileInfo = custom.projectileInfo;
         if (projectileInfo != null) {
            this.mainModel = AssetManager.getProjectileModel(projectileInfo);
            ResourceLocation texture = this.getTextureLocation(entity);
            if (this.mainModel != null && texture != null) {
               pose.pushPose();
               pose.translate(0.0F, -0.25F, 0.0F);
               pose.scale(0.5F, 0.5F, 0.5F);
               pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(entity.getYRot()));
               RenderType type = LycRenderTypes.creature(texture);
               LycGL.begin(buffers, texture, packedLight, type);
               LegacyRender.push(pose, buffers.getBuffer(type), packedLight, OverlayTexture.NO_OVERLAY, -1);
               try {
                  this.mainModel.render(entity, 0.0F, 0.0F, partialTicks, 0.0F, 0.0F, 1.0F);
               } finally {
                  LegacyRender.pop();
                  LycGL.end();
                  pose.popPose();
               }
            }
         }
      }
   }

   @Override
   public ResourceLocation getTextureLocation(Entity entity) {
      return entity instanceof BaseProjectileEntity projectile ? projectile.getTexture() : null;
   }
}
