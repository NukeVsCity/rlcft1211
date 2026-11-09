package com.github.alexthe666.iceandfire.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * Client hooks of the iaf/core block renderers into models other branches port: ModelDragonEgg (iaf/dragon-client) for the podium and
 * the frozen egg, ModelPixie (iaf/mobs-air-water) for jars and pixie houses. Each is set once from that branch's client setup; until
 * then the egg/pixie is simply not drawn.
 */
public final class IafRenderHooks {
   private IafRenderHooks() {
   }

   public interface EggRenderer {
      /** ModelDragonEgg.renderPodium() (frozenTicks < 0) or renderFrozen(tile) (frozenTicks = tile.ticksExisted) with the texture
       *  bound, at the current pose (1.12 GL state after the renderer's translate). */
      void render(PoseStack pose, MultiBufferSource buffers, int light, int overlay, ResourceLocation texture, int frozenTicks);
   }

   public interface PixieRenderer {
      /** ModelPixie.animateInJar(...) / animateInHouse(tile) for pixie colour type (texture RenderPixie.TEXTURE_type), at the current pose. */
      void render(PoseStack pose, MultiBufferSource buffers, int light, int overlay, int type, boolean inHouse, Object tile);
   }

   public static volatile EggRenderer egg;
   public static volatile PixieRenderer pixie;

   public static final ResourceLocation EGG_RED = ResourceLocation.parse("iceandfire:textures/models/firedragon/egg_red.png");
   public static final ResourceLocation EGG_GREEN = ResourceLocation.parse("iceandfire:textures/models/firedragon/egg_green.png");
   public static final ResourceLocation EGG_BRONZE = ResourceLocation.parse("iceandfire:textures/models/firedragon/egg_bronze.png");
   public static final ResourceLocation EGG_GREY = ResourceLocation.parse("iceandfire:textures/models/firedragon/egg_gray.png");
   public static final ResourceLocation EGG_BLUE = ResourceLocation.parse("iceandfire:textures/models/icedragon/egg_blue.png");
   public static final ResourceLocation EGG_WHITE = ResourceLocation.parse("iceandfire:textures/models/icedragon/egg_white.png");
   public static final ResourceLocation EGG_SAPPHIRE = ResourceLocation.parse("iceandfire:textures/models/icedragon/egg_sapphire.png");
   public static final ResourceLocation EGG_SILVER = ResourceLocation.parse("iceandfire:textures/models/icedragon/egg_silver.png");
}
