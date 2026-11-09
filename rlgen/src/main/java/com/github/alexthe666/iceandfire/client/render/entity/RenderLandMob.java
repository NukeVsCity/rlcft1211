package com.github.alexthe666.iceandfire.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/** 1.12 RenderLiving with a 1.12 model: texture function and the preRenderCallback scale. */
public class RenderLandMob<T extends Mob> extends MobRenderer<T, EntityModel<T>> {
   private final Function<T, ResourceLocation> texture;
   private final Scale<T> scale;

   public interface Scale<T> {
      void apply(T entity, PoseStack pose, float partialTicks);
   }

   @SuppressWarnings("unchecked")
   public RenderLandMob(EntityRendererProvider.Context context, ModelBase model, float shadow, Function<T, ResourceLocation> texture, Scale<T> scale) {
      super(context, (EntityModel<T>)(EntityModel<?>)model, shadow);
      this.texture = texture;
      this.scale = scale;
   }

   public ModelBase legacyModel() {
      return (ModelBase)(Object)this.getModel();
   }

   @Override
   protected void scale(T entity, PoseStack pose, float partialTicks) {
      if (this.scale != null) {
         this.scale.apply(entity, pose, partialTicks);
      }
   }

   @Override
   public ResourceLocation getTextureLocation(T entity) {
      return this.texture.apply(entity);
   }
}
