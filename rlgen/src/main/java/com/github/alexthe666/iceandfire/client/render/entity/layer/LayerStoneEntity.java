package com.github.alexthe666.iceandfire.client.render.entity.layer;

import com.github.alexthe666.iceandfire.client.model.ICustomStatueModel;
import com.github.alexthe666.iceandfire.entity.StoneEntityProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * 1.12 LayerStoneEntity (added to every living renderer by EventClient.initializeStoneLayer) and LayerStoneEntityCrack: a stone
 * mob is invisible and its model is drawn again with the stone texture sized to the model's texture, then the block break stage.
 * 1.12 swapped horses and guardians for their statue models; here they use their own model.
 */
public class LayerStoneEntity<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
   private static final ResourceLocation[] DESTROY_STAGES = new ResourceLocation[10];

   static {
      for (int i = 0; i < 10; i++) {
         DESTROY_STAGES[i] = ResourceLocation.withDefaultNamespace("textures/block/destroy_stage_" + i + ".png");
      }
   }

   private final boolean crackOnly;

   public LayerStoneEntity(RenderLayerParent<T, M> renderer) {
      this(renderer, false);
   }

   public LayerStoneEntity(RenderLayerParent<T, M> renderer, boolean crackOnly) {
      super(renderer);
      this.crackOnly = crackOnly;
   }

   public static ResourceLocation getStoneType(int texWidth, int texHeight) {
      int sizeX = clampToMultipleOfFour(Math.min(128, texWidth));
      int sizeY = clampToMultipleOfFour(Math.min(128, texHeight));
      return sizeX <= 16 && sizeY <= 16
         ? ResourceLocation.withDefaultNamespace("textures/block/stone.png")
         : ResourceLocation.fromNamespaceAndPath("iceandfire", "textures/models/gorgon/stone" + sizeX + "x" + sizeY + ".png");
   }

   public static int clampToMultipleOfFour(int i) {
      if (i % 4 != 0) {
         if (i > 128) {
            return 128;
         } else if (i > 64) {
            return 64;
         } else {
            return i > 32 ? 32 : 16;
         }
      } else {
         return i;
      }
   }

   @Override
   public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing, float limbSwingAmount, float partialTicks,
      float ageInTicks, float netHeadYaw, float headPitch) {
      if (!(entity instanceof Mob)) {
         return;
      }
      StoneEntityProperties properties = StoneEntityProperties.get(entity);
      if (properties == null || !properties.isStone) {
         return;
      }
      M model = this.getParentModel();
      if (!this.crackOnly) {
         ResourceLocation tex = (Object)model instanceof ModelBase legacy ? getStoneType(legacy.textureWidth, legacy.textureHeight) : getStoneType(64, 64);
         draw(model, entity, pose, buffers.getBuffer(RenderType.entityCutout(tex)), light, limbSwing, netHeadYaw, headPitch);
      }
      if (properties.breakLvl > 0) {
         VertexConsumer crack = buffers.getBuffer(RenderType.crumbling(DESTROY_STAGES[Math.min(9, properties.breakLvl - 1)]));
         draw(model, entity, pose, crack, light, limbSwing, netHeadYaw, headPitch);
      }
   }

   static <T extends LivingEntity> void draw(EntityModel<T> model, T entity, PoseStack pose, VertexConsumer vc, int light, float limbSwing, float yaw, float pitch) {
      if ((Object)model instanceof ICustomStatueModel statue && (Object)model instanceof ModelBase) {
         LegacyRender.push(pose, vc, light, OverlayTexture.NO_OVERLAY, -1);
         try {
            statue.renderStatue();
         } finally {
            LegacyRender.pop();
         }
      } else if ((Object)model instanceof ModelBase legacy) {
         legacy.renderLegacy(pose, vc, light, OverlayTexture.NO_OVERLAY, -1, entity, limbSwing, 0.0F, 0.0F, yaw, pitch, 0.0625F);
      } else {
         model.setupAnim(entity, limbSwing, 0.0F, 0.0F, yaw, pitch);
         model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, -1);
      }
   }
}
