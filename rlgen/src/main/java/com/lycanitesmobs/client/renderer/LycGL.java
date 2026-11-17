package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.core.info.CreatureManager;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * The bits of the 1.12 GL state machine that Lycanites models and layers change while one creature is rendered (bound
 * texture, blending, lighting, culling, lightmap coordinates). State carries over from part to part and from the base
 * model to the layers exactly like the GL state did; every change switches the buffer of the current
 * {@link LegacyRender} context to the matching {@link LycRenderTypes} type. Render thread only.
 */
public final class LycGL {
   private static MultiBufferSource buffers;
   private static boolean locked;
   private static ResourceLocation texture;
   private static boolean blend;
   private static int blendMode;
   private static boolean lighting;
   private static boolean cull;
   private static int light;
   /** 1.12 entity.getBrightnessForRender() of the creature being rendered. */
   private static int entityLight;

   private LycGL() {
   }

   /**
    * 1.12 RenderLivingBase.doRender start: the creature texture is bound, lighting on, culling off, the lightmap is the
    * entity's brightness. {@code baseType} is the type the 1.21 renderer chose for the base model: an outline (glowing
    * effect) or other non-Lycanites type is kept for the base model.
    */
   public static void begin(MultiBufferSource bufferSource, ResourceLocation entityTexture, int packedLight, RenderType baseType) {
      buffers = bufferSource;
      texture = entityTexture;
      blend = !CreatureManager.getInstance().config.disableModelAlpha;
      blendMode = LycRenderTypes.BLEND_NORMAL;
      lighting = true;
      cull = false;
      light = packedLight;
      entityLight = packedLight;
      locked = baseType != null && !LycRenderTypes.isLycType(baseType);
      dirty = false;
      appliedType = locked ? null : baseType;
   }

   /** The RenderType the 1.21 renderer picked for the base model (an outline for glowing creatures is kept). */
   public static void baseType(RenderType baseType) {
      locked = baseType != null && !LycRenderTypes.isLycType(baseType);
      appliedType = locked ? null : baseType;
   }

   public static void end() {
      buffers = null;
      locked = false;
   }

   /** Starts a layer: from now on the state also switches the buffers of layer contexts. */
   public static void unlock() {
      locked = false;
   }

   public static boolean active() {
      return buffers != null;
   }

   public static RenderType currentType() {
      return LycRenderTypes.get(texture, blend, blendMode, lighting, cull);
   }

   public static int light() {
      return light;
   }

   public static int entityLight() {
      return entityLight;
   }

   public static MultiBufferSource buffers() {
      return buffers;
   }

   private static boolean dirty;

   /** A state change only marks the buffer stale; {@link #apply} switches it before the next triangles are written. */
   private static void refresh() {
      dirty = true;
   }

   /** Called before geometry is written (VBOModel): switches the context buffer to the current state's RenderType. */
   public static void apply() {
      if (!dirty || buffers == null || locked || !LegacyRender.active() || texture == null) {
         return;
      }
      dirty = false;
      RenderType type = currentType();
      if (type != appliedType) {
         appliedType = type;
         LegacyRender.setBuffer(buffers.getBuffer(type));
      }
   }

   private static RenderType appliedType;

   /** The buffer for the current state (a layer context starts with it). */
   public static com.mojang.blaze3d.vertex.VertexConsumer buffer() {
      dirty = false;
      appliedType = currentType();
      return buffers.getBuffer(appliedType);
   }

   public static void bindTexture(ResourceLocation location) {
      if (location != null && !location.equals(texture)) {
         texture = location;
         refresh();
      }
   }

   public static void enableBlend() {
      if (!blend) {
         blend = true;
         refresh();
      }
   }

   public static void disableBlend() {
      if (blend) {
         blend = false;
         refresh();
      }
   }

   /** GlStateManager.blendFunc as one of the three Lycanites blend modes (LycRenderTypes.BLEND_*). */
   public static void blendFunc(int mode) {
      if (blendMode != mode) {
         blendMode = mode;
         refresh();
      }
   }

   public static void enableLighting() {
      if (!lighting) {
         lighting = true;
         refresh();
      }
   }

   public static void disableLighting() {
      if (lighting) {
         lighting = false;
         refresh();
      }
   }

   public static void enableCull() {
      if (!cull) {
         cull = true;
         refresh();
      }
   }

   public static void disableCull() {
      if (cull) {
         cull = false;
         refresh();
      }
   }

   /** OpenGlHelper.setLightmapTextureCoords(lightmapTexUnit, i % 65536, i / 65536). */
   public static void setLightmap(int packedLight) {
      light = packedLight;
      if (buffers != null && !locked && LegacyRender.active()) {
         LegacyRender.setLight(packedLight);
      }
   }
}
