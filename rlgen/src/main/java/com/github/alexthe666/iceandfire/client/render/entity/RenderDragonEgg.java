package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.client.model.ModelDragonEgg;
import com.github.alexthe666.iceandfire.entity.EntityDragonEgg;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** 1.12 RenderDragonEgg (RenderLiving with ModelDragonEgg, shadow 0.3). */
public class RenderDragonEgg extends MobRenderer<EntityDragonEgg, EntityModel<EntityDragonEgg>> {
   public static final ResourceLocation EGG_RED = ResourceLocation.parse("iceandfire:textures/models/firedragon/egg_red.png");
   public static final ResourceLocation EGG_GREEN = ResourceLocation.parse("iceandfire:textures/models/firedragon/egg_green.png");
   public static final ResourceLocation EGG_BRONZE = ResourceLocation.parse("iceandfire:textures/models/firedragon/egg_bronze.png");
   public static final ResourceLocation EGG_GREY = ResourceLocation.parse("iceandfire:textures/models/firedragon/egg_gray.png");
   public static final ResourceLocation EGG_BLUE = ResourceLocation.parse("iceandfire:textures/models/icedragon/egg_blue.png");
   public static final ResourceLocation EGG_WHITE = ResourceLocation.parse("iceandfire:textures/models/icedragon/egg_white.png");
   public static final ResourceLocation EGG_SAPPHIRE = ResourceLocation.parse("iceandfire:textures/models/icedragon/egg_sapphire.png");
   public static final ResourceLocation EGG_SILVER = ResourceLocation.parse("iceandfire:textures/models/icedragon/egg_silver.png");

   @SuppressWarnings("unchecked")
   public RenderDragonEgg(EntityRendererProvider.Context context) {
      super(context, (EntityModel<EntityDragonEgg>)(EntityModel<?>)new ModelDragonEgg(), 0.3F);
   }

   @Override
   public ResourceLocation getTextureLocation(EntityDragonEgg entity) {
      return getEggTexture(entity.getEggType());
   }

   /** RenderDragonEgg.getEntityTexture of 1.12 (RenderPodium.getEggTexture is the same switch). */
   public static ResourceLocation getEggTexture(EnumDragonEgg type) {
      switch (type) {
         case GREEN:
            return EGG_GREEN;
         case BRONZE:
            return EGG_BRONZE;
         case GRAY:
            return EGG_GREY;
         case BLUE:
            return EGG_BLUE;
         case WHITE:
            return EGG_WHITE;
         case SAPPHIRE:
            return EGG_SAPPHIRE;
         case SILVER:
            return EGG_SILVER;
         default:
            return EGG_RED;
      }
   }
}
