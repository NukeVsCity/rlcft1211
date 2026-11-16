package com.lycanitesmobs.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * 1.12 VBOModel. 1.12 drew the group's VBO (GL_TRIANGLES, POSITION_TEX_NORMAL, v flipped to 1 - v, one face normal per
 * triangle) with GlStateManager.color(color) and a texture matrix translated by (offset.x * 0.01, -offset.y * 0.01).
 * Here the same triangles go into the buffer of the current {@link LegacyRender} context (the 1.21 entity formats are
 * quads, so each triangle is a quad with its last corner doubled), with the same UVs, colour and normals, transformed by
 * the context's PoseStack (the 1.12 modelview matrix).
 */
public class VBOModel extends TessellatorModel {
   public VBOModel(ResourceLocation resourceLocation) {
      super(resourceLocation);
   }

   @Override
   protected void renderGroupImpl(ObjObject obj, Vector4f color, Vector2f textureOffset) {
      if (obj.mesh == null || obj.mesh.indices == null || obj.mesh.indices.length == 0) {
         return;
      }
      com.lycanitesmobs.client.renderer.LycGL.apply();
      LegacyRender ctx = LegacyRender.current();
      VertexConsumer buffer = ctx.buffer;
      PoseStack.Pose pose = ctx.pose.last();
      // the context colour is white for a normal render (1.12 glColor replaced it); 1.21 passes a translucent white for
      // invisible creatures a spectator can see (1.12 RenderLivingBase.renderModel did the same with a 0.15 alpha colour)
      float r = color.x * ctx.red;
      float g = color.y * ctx.green;
      float b = color.z * ctx.blue;
      float a = color.w * ctx.alpha;
      float offU = textureOffset.x * 0.01F;
      float offV = -textureOffset.y * 0.01F;
      int light = ctx.light;
      int overlay = ctx.overlay;
      int[] indices = obj.mesh.indices;
      Vertex[] vertices = obj.mesh.vertices;
      Vector3f[] normals = obj.mesh.getNormals();
      for (int i = 0; i + 2 < indices.length; i += 3) {
         Vector3f n = normals[i];
         for (int iv = 0; iv < 4; iv++) {
            Vertex v = vertices[indices[i + Math.min(iv, 2)]];
            Vector3f p = v.getPos();
            Vector2f t = v.getTexCoords();
            buffer.addVertex(pose, p.x, p.y, p.z)
               .setColor(r, g, b, a)
               .setUv(t.x + offU, 1.0F - t.y + offV)
               .setOverlay(overlay)
               .setLight(light)
               .setNormal(pose, n.x, n.y, n.z);
         }
      }
   }
}
