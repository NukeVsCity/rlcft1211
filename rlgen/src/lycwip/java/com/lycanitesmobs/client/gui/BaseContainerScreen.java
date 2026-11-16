package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.client.renderer.LycGL;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Container;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseContainerScreen extends GuiContainer {
   public BaseContainerScreen(Container container) {
      super(container);
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      super.drawScreen(mouseX, mouseY, partialTicks);
      this.renderHoveredToolTip(mouseX, mouseY);
   }

   public void drawTexture(ResourceLocation texture, float x, float y, float z, float u, float v, float width, float height) {
      LycGL.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      LegacyRender.color(1.0F, 1.0F, 1.0F, 1.0F);
      
      this.mc.getTextureManager().bindTexture(texture);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
      buffer.pos((double)x, (double)(y + height), (double)z).tex(0.0, (double)v).endVertex();
      buffer.pos((double)(x + width), (double)(y + height), (double)z).tex((double)u, (double)v).endVertex();
      buffer.pos((double)(x + width), (double)y, (double)z).tex((double)u, 0.0).endVertex();
      buffer.pos((double)x, (double)y, (double)z).tex(0.0, 0.0).endVertex();
      tessellator.draw();
      LycGL.disableBlend();
      GlStateManager.enableDepth();
      GlStateManager.depthMask(true);
      
   }

   public void drawTexturedModalRect(int x, int y, int u, int v, int width, int height) {
      this.drawTexturedModalRect(x, y, u, v, width, height, 1);
   }

   public void drawTexturedModalRect(int x, int y, int u, int v, int width, int height, int resolution) {
      float scaleX = 0.00390625F * (float)resolution;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder vertexbuffer = tessellator.getBuffer();
      vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
      vertexbuffer.pos((double)(x + 0), (double)(y + height), (double)this.zLevel)
         .tex((double)((float)(u + 0) * scaleX), (double)((float)(v + height) * scaleX))
         .endVertex();
      vertexbuffer.pos((double)(x + width), (double)(y + height), (double)this.zLevel)
         .tex((double)((float)(u + width) * scaleX), (double)((float)(v + height) * scaleX))
         .endVertex();
      vertexbuffer.pos((double)(x + width), (double)(y + 0), (double)this.zLevel)
         .tex((double)((float)(u + width) * scaleX), (double)((float)(v + 0) * scaleX))
         .endVertex();
      vertexbuffer.pos((double)(x + 0), (double)(y + 0), (double)this.zLevel)
         .tex((double)((float)(u + 0) * scaleX), (double)((float)(v + 0) * scaleX))
         .endVertex();
      tessellator.draw();
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
}
