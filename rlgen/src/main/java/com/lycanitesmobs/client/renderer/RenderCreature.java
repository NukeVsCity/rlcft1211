package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.client.AssetManager;
import com.lycanitesmobs.client.model.ModelCustom;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.12 RenderCreature (a RenderLiving). The 1.21 LivingEntityRenderer does what 1.12 RenderLivingBase.doRender did
 * (yaw/death rotation, scale(-1, -1, 1), translate(0, -1.501, 0), model.render with scale 0.0625, then the layers), so
 * this only swaps the model per entity (subspecies models) and gives the 1.12 GL states as RenderTypes.
 */
public class RenderCreature extends MobRenderer<BaseCreatureEntity, EntityModel<BaseCreatureEntity>> {
   public boolean multipass = true;
   protected EntityModel<BaseCreatureEntity> defaultModel;
   public static final float[][] colorTable = new float[][]{
      {1.0F, 1.0F, 1.0F},
      {0.85F, 0.5F, 0.2F},
      {0.7F, 0.3F, 0.85F},
      {0.4F, 0.6F, 0.85F},
      {0.9F, 0.9F, 0.2F},
      {0.5F, 0.8F, 0.1F},
      {0.95F, 0.5F, 0.65F},
      {0.3F, 0.3F, 0.3F},
      {0.6F, 0.6F, 0.6F},
      {0.3F, 0.5F, 0.6F},
      {0.5F, 0.25F, 0.7F},
      {0.2F, 0.3F, 0.7F},
      {0.4F, 0.3F, 0.2F},
      {0.4F, 0.5F, 0.2F},
      {0.6F, 0.2F, 0.2F},
      {0.1F, 0.1F, 0.1F}
   };

   @SuppressWarnings({"unchecked", "rawtypes"})
   public RenderCreature(String entityID, EntityRendererProvider.Context context, float shadowSize) {
      super(context, (EntityModel)AssetManager.getCreatureModel(CreatureManager.getInstance().getCreature(entityID)), shadowSize);
      this.defaultModel = this.model;
      if ((Object)this.model instanceof ModelCustom modelCustom) {
         modelCustom.addCustomLayers(this);
      }
   }

   /** 1.12 getMainModel (a ModelBase there). */
   public Object getMainModel() {
      return this.model;
   }

   public boolean isMultipass() {
      return false;
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   @Override
   public void render(BaseCreatureEntity entity, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      ModelBase model = AssetManager.getCreatureModel(entity);
      this.model = model != null ? (EntityModel)model : this.defaultModel;
      if (this.model != null) {
         LycGL.begin(buffers, this.getTextureLocation(entity), packedLight, null);
         try {
            super.render(entity, entityYaw, partialTicks, pose, buffers, packedLight);
         } finally {
            LycGL.end();
         }
      }
   }

   @Override
   protected RenderType getRenderType(BaseCreatureEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
      ResourceLocation texture = this.getTextureLocation(entity);
      if (texture == null) {
         return null;
      }
      RenderType type = translucent || bodyVisible ? LycRenderTypes.creature(texture) : (glowing ? RenderType.outline(texture) : null);
      LycGL.baseType(type);
      return type;
   }

   @Override
   public ResourceLocation getTextureLocation(BaseCreatureEntity entity) {
      return entity.getTexture();
   }

   public ResourceLocation getEquipmentTexture(BaseCreatureEntity entity, String equipmentName) {
      return entity.getEquipmentTexture(equipmentName);
   }

   /** 1.12 canRenderName. */
   @Override
   protected boolean shouldShowName(BaseCreatureEntity entity) {
      Minecraft mc = Minecraft.getInstance();
      if (!Minecraft.renderNames()) {
         return false;
      } else if (entity == mc.getCameraEntity()) {
         return false;
      } else if (mc.player != null && entity.isInvisibleTo(mc.player)) {
         return false;
      } else if (entity.getControllingPassenger() != null) {
         return false;
      } else if (entity.isCustomNameVisible()) {
         return entity.isTamed() ? entity == this.entityRenderDispatcher.crosshairPickEntity : true;
      } else {
         return entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity;
      }
   }

   public float interpolateRotation(float par1, float par2, float par3) {
      float f3 = par2 - par1;

      while (f3 < -180.0F) {
         f3 += 360.0F;
      }

      while (f3 >= 180.0F) {
         f3 -= 360.0F;
      }

      return par1 + par3 * f3;
   }
}
