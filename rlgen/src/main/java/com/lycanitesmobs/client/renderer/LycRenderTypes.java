package com.lycanitesmobs.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * The 1.12 GL states Lycanites rendered its models with ({@link LycGL}), as 1.21 RenderTypes:
 * <ul>
 * <li>blend off, or on with blendFunc NORMAL (SRC_ALPHA, ONE_MINUS_SRC_ALPHA), ADD (SRC_ALPHA, ONE) or
 * SUB (DST_COLOR, ONE_MINUS_SRC_ALPHA); alpha test 0.1; depth writes on;</li>
 * <li>lighting on: the 1.21 entity shader (diffuse light, lightmap, hurt overlay); lighting off: the 1.21 breeze wind
 * shader (lightmap, no diffuse light, no overlay);</li>
 * <li>culling on or off (1.12 RenderLivingBase rendered living entities with culling off).</li>
 * </ul>
 * 1.12 drew the triangles in model order (no sorting), so these buffers are not sorted either.
 */
public final class LycRenderTypes extends RenderType {
   public static final int BLEND_NORMAL = 0;
   public static final int BLEND_ADD = 1;
   public static final int BLEND_SUB = 2;

   private static final TransparencyStateShard[] BLENDS = {
      blend("lyc_blend_normal", GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA),
      blend("lyc_blend_add", GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE),
      blend("lyc_blend_sub", GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
   };

   private static final Map<String, RenderType> CACHE = new HashMap<>();
   private static final java.util.Set<RenderType> TYPES = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());

   private LycRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
      super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
   }

   private static TransparencyStateShard blend(String name, GlStateManager.SourceFactor src, GlStateManager.DestFactor dst) {
      return new TransparencyStateShard(name, () -> {
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(src, dst);
      }, () -> {
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
      });
   }

   /** The state a creature's base model starts with: blended (NORMAL), lit, no culling. */
   public static RenderType creature(ResourceLocation texture) {
      return get(texture, true, BLEND_NORMAL, true, false);
   }

   public static RenderType get(ResourceLocation texture, boolean blend, int blendMode, boolean lighting, boolean cull) {
      String key = texture + "|" + blend + "|" + blendMode + "|" + lighting + "|" + cull;
      synchronized (CACHE) {
         return CACHE.computeIfAbsent(key, k -> {
            CompositeState.CompositeStateBuilder builder = CompositeState.builder()
               .setShaderState(lighting ? (blend ? RENDERTYPE_ENTITY_TRANSLUCENT_SHADER : RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER) : RENDERTYPE_BREEZE_WIND_SHADER)
               .setTextureState(new TextureStateShard(texture, false, false))
               .setTransparencyState(blend ? BLENDS[Math.max(0, Math.min(2, blendMode))] : NO_TRANSPARENCY)
               .setCullState(cull ? CULL : NO_CULL)
               .setLightmapState(LIGHTMAP)
               .setOverlayState(lighting ? OVERLAY : NO_OVERLAY)
               .setWriteMaskState(COLOR_DEPTH_WRITE);
            RenderType type = create("lycanitesmobs_creature", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, false, builder.createCompositeState(true));
            TYPES.add(type);
            return type;
         });
      }
   }

   public static boolean isLycType(RenderType type) {
      synchronized (CACHE) {
         return TYPES.contains(type);
      }
   }
}
