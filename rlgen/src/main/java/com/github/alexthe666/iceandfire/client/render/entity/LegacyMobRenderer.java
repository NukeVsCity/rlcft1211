package com.github.alexthe666.iceandfire.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * 1.12 RenderLiving for the Ice and Fire 1.7.1 java models: the texture, the preRenderCallback scale, and the 1.12 layers that drew the
 * main model again with another texture (saddles, chests, armour, glow, overlays).
 */
public class LegacyMobRenderer<T extends Mob> extends MobRenderer<T, EntityModel<T>> {
   private final Function<T, ResourceLocation> texture;
   private final float scale;

   @SuppressWarnings({"unchecked", "rawtypes"})
   public LegacyMobRenderer(EntityRendererProvider.Context ctx, ModelBase model, float shadow, float scale, Function<T, ResourceLocation> texture) {
      super(ctx, (EntityModel)model, shadow);
      this.texture = texture;
      this.scale = scale;
   }

   @Override
   public ResourceLocation getTextureLocation(T entity) {
      return this.texture.apply(entity);
   }

   @Override
   protected void scale(T entity, PoseStack pose, float partialTick) {
      if (this.scale != 1.0F) {
         pose.scale(this.scale, this.scale, this.scale);
      }
      this.extraTransform(entity, pose, partialTick);
   }

   protected void extraTransform(T entity, PoseStack pose, float partialTick) {
   }

   public LegacyMobRenderer<T> layer(Predicate<T> when, Function<T, ResourceLocation> texture) {
      return this.layer(when, texture, e -> -1, RenderType::entityCutoutNoCull);
   }

   public LegacyMobRenderer<T> layer(Predicate<T> when, Function<T, ResourceLocation> texture, ToIntFunction<T> color,
         Function<ResourceLocation, RenderType> type) {
      this.addLayer(new ModelLayer<>(this, when, texture, color, type));
      return this;
   }

   /** a 1.12 LayerRenderer that bound a texture and called getMainModel().render(...) */
   public static class ModelLayer<T extends Mob> extends RenderLayer<T, EntityModel<T>> {
      private final Predicate<T> when;
      private final Function<T, ResourceLocation> texture;
      private final ToIntFunction<T> color;
      private final Function<ResourceLocation, RenderType> type;

      ModelLayer(RenderLayerParent<T, EntityModel<T>> parent, Predicate<T> when, Function<T, ResourceLocation> texture, ToIntFunction<T> color,
            Function<ResourceLocation, RenderType> type) {
         super(parent);
         this.when = when;
         this.texture = texture;
         this.color = color;
         this.type = type;
      }

      @Override
      public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing, float limbSwingAmount, float partialTick,
            float ageInTicks, float netHeadYaw, float headPitch) {
         if (this.when.test(entity)) {
            @Nullable ResourceLocation tex = this.texture.apply(entity);
            if (tex != null) {
               this.getParentModel().renderToBuffer(pose, buffers.getBuffer(this.type.apply(tex)), light, LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                  this.color.applyAsInt(entity));
            }
         }
      }
   }
}
