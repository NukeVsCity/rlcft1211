package com.lycanitesmobs.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * 1.12 DrawHelper. The 1.12 immediate mode quads (POSITION_TEX, blend on, depth test and writes off) are drawn the same
 * way with the 1.21 position_tex shader, transformed by the pose of the {@link GuiGraphics} set with {@link #setGraphics}
 * for the current frame (the 1.12 modelview matrix).
 */
public class DrawHelper {
   protected Minecraft minecraft;
   protected Font fontRenderer;
   /** The GuiGraphics of the frame being drawn. */
   public static GuiGraphics graphics;

   public DrawHelper(Minecraft minecraft, Font fontRenderer) {
      this.minecraft = minecraft;
      this.fontRenderer = fontRenderer;
   }

   public static void setGraphics(GuiGraphics guiGraphics) {
      graphics = guiGraphics;
   }

   public Minecraft getMinecraft() {
      return this.minecraft;
   }

   public Font getFontRenderer() {
      if (this.fontRenderer == null) {
         this.fontRenderer = this.minecraft.font;
      }

      return this.fontRenderer;
   }

   public int getStringWidth(String text) {
      return this.getFontRenderer().width(text);
   }

   public int getWordWrappedHeight(String text, int wrapWidth) {
      return this.getFontRenderer().wordWrapHeight(text, wrapWidth);
   }

   /** 1.12 FontRenderer.drawString (no shadow). */
   public void drawString(String text, int x, int y, int color) {
      graphics.drawString(this.getFontRenderer(), text, x, y, color, false);
   }

   /** 1.12 FontRenderer.drawSplitString (no shadow). */
   public void drawSplitString(String text, int x, int y, int wrapWidth, int color) {
      graphics.drawWordWrap(this.getFontRenderer(), FormattedText.of(text), x, y, wrapWidth, color);
   }

   public void drawTexture(ResourceLocation texture, float x, float y, float z, float u, float v, float width, float height) {
      this.preDraw();
      quad(texture, x, y, z, width, height, 0.0F, 0.0F, u, v);
      this.postDraw();
   }

   public void drawTextureTiled(ResourceLocation texture, float x, float y, float z, float u, float v, float width, float height, float resolution) {
      this.preDraw();
      float scaleX = 0.00390625F * resolution;
      quad(texture, x, y, z, width, height, u * scaleX, v * scaleX, (u + width) * scaleX, (v + height) * scaleX);
      this.postDraw();
   }

   public void drawBar(ResourceLocation texture, int x, int y, float z, float width, float height, int segments, int segmentLimit) {
      boolean reverse = segmentLimit < 0;
      if (reverse) {
         segmentLimit = -segmentLimit;
      }

      for (int i = 0; i < segments; i++) {
         int currentSegment = i;
         if (reverse) {
            currentSegment = segmentLimit - i - 1;
         }

         this.drawTexture(texture, (float)x + width * (float)currentSegment, (float)y, z, 1.0F, 1.0F, width, height);
      }
   }

   public void preDraw() {
      RenderSystem.enableBlend();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void postDraw() {
      RenderSystem.disableBlend();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(true);
   }

   /** Draws with the texture bound by the caller (1.12 bindTexture before drawTexturedModalRect). */
   public void drawTexturedModalRect(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
      this.drawTexturedModalRect(texture, x, y, u, v, width, height, 1);
   }

   public void drawTexturedModalRect(ResourceLocation texture, int x, int y, int u, int v, int width, int height, int resolution) {
      this.preDraw();
      float scaleX = 0.00390625F * (float)resolution;
      quad(texture, x, y, 0.0F, width, height, u * scaleX, v * scaleX, (u + width) * scaleX, (v + height) * scaleX);
      this.postDraw();
   }

   /** One POSITION_TEX quad like the 1.12 Tessellator calls: corners (x, y + h), (x + w, y + h), (x + w, y), (x, y). */
   public static void quad(ResourceLocation texture, float x, float y, float z, float width, float height, float u0, float v0, float u1, float v1) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, texture);
      Matrix4f matrix = graphics.pose().last().pose();
      BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      buffer.addVertex(matrix, x, y + height, z).setUv(u0, v1);
      buffer.addVertex(matrix, x + width, y + height, z).setUv(u1, v1);
      buffer.addVertex(matrix, x + width, y, z).setUv(u1, v0);
      buffer.addVertex(matrix, x, y, z).setUv(u0, v0);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
   }

   /**
    * 1.12 GuiInventory.drawEntityOnScreen(x, y, scale, lookX, lookY, entity): the entity's feet at (x, y), turned towards
    * the look offsets (body yaw atan(lookX / 40) * 20, yaw * 40, pitch -atan(lookY / 40) * 20), drawn with the 1.21
    * inventory entity renderer (the same rotations as vanilla 1.12).
    */
   public static void drawEntityOnScreen(int x, int y, int scale, float lookX, float lookY, net.minecraft.world.entity.LivingEntity entity) {
      float angleX = (float)Math.atan((double)(lookX / 40.0F));
      float angleY = (float)Math.atan((double)(lookY / 40.0F));
      org.joml.Quaternionf rotation = new org.joml.Quaternionf().rotateZ((float)Math.PI);
      org.joml.Quaternionf pitch = new org.joml.Quaternionf().rotateX(angleY * 20.0F * (float)(Math.PI / 180.0));
      rotation.mul(pitch);
      float f = entity.yBodyRot;
      float f1 = entity.getYRot();
      float f2 = entity.getXRot();
      float f3 = entity.yHeadRotO;
      float f4 = entity.yHeadRot;
      entity.yBodyRot = angleX * 20.0F;
      entity.setYRot(angleX * 40.0F);
      entity.setXRot(-angleY * 20.0F);
      entity.yHeadRot = entity.getYRot();
      entity.yHeadRotO = entity.getYRot();
      net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventory(graphics, (float)x, (float)y, (float)scale,
         new org.joml.Vector3f(), rotation, pitch, entity);
      entity.yBodyRot = f;
      entity.setYRot(f1);
      entity.setXRot(f2);
      entity.yHeadRotO = f3;
      entity.yHeadRot = f4;
   }
}
