package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerDragonArmor;
import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerDragonEyes;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.enums.EnumDragonTextures;
import com.mojang.blaze3d.vertex.PoseStack;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.12 RenderDragonBase (RenderLiving, shadow 0.8, eyes layer + 4 armor layers, scaled by getRenderSize() / 3). The model is the
 * 1.12 IceAndFireTabulaModel with the fire or ice animator, drawn through the llibrary legacy ModelBase bridge.
 */
public class RenderDragonBase extends MobRenderer<EntityDragonBase, EntityModel<EntityDragonBase>> {
   @SuppressWarnings("unchecked")
   public RenderDragonBase(EntityRendererProvider.Context context, ModelBase model, boolean fire) {
      super(context, (EntityModel<EntityDragonBase>)(EntityModel<?>)model, 0.8F);
      this.addLayer(new LayerDragonEyes(this));
      this.addLayer(new LayerDragonArmor(this, 0));
      this.addLayer(new LayerDragonArmor(this, 1));
      this.addLayer(new LayerDragonArmor(this, 2));
      this.addLayer(new LayerDragonArmor(this, 3));
   }

   @Override
   protected void scale(EntityDragonBase entity, PoseStack poseStack, float partialTicks) {
      // preRenderCallback: shadowSize = getRenderSize() / 3; GL11.glScalef(shadowSize, shadowSize, shadowSize)
      this.shadowRadius = entity.getRenderSize() / 3.0F;
      poseStack.scale(this.shadowRadius, this.shadowRadius, this.shadowRadius);
   }

   @Override
   protected float getShadowRadius(EntityDragonBase entity) {
      return entity.getRenderSize() / 3.0F;
   }

   @Override
   public ResourceLocation getTextureLocation(EntityDragonBase entity) {
      return EnumDragonTextures.getTextureFromDragon(entity);
   }
}
