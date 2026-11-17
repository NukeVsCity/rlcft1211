package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.core.container.BaseContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 1.12 BaseContainerScreen (a GuiContainer). The 1.12 hooks are kept: drawGuiContainerBackgroundLayer (1.21 renderBg) and
 * drawGuiContainerForegroundLayer (1.21 renderLabels, already translated to the gui corner like 1.12).
 */
public abstract class BaseContainerScreen<T extends BaseContainer> extends AbstractContainerScreen<T> implements ButtonBase.Listener {
   protected Minecraft mc;
   protected float zLevel;
   protected DrawHelper drawHelper;

   public BaseContainerScreen(T container, Inventory inventory) {
      super(container, inventory, Component.empty());
      this.mc = Minecraft.getInstance();
      this.drawHelper = new DrawHelper(this.mc, this.mc.font);
   }

   public Font getFontRenderer() {
      return this.mc.font;
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      DrawHelper.setGraphics(graphics);
      this.drawScreen(graphics, mouseX, mouseY, partialTicks);
   }

   /** 1.12 drawScreen: drawDefaultBackground, super.drawScreen, renderHoveredToolTip. */
   public void drawScreen(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      super.render(graphics, mouseX, mouseY, partialTicks);
      this.renderTooltip(graphics, mouseX, mouseY);
   }

   @Override
   protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      DrawHelper.setGraphics(graphics);
      this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
   }

   @Override
   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      DrawHelper.setGraphics(graphics);
      this.drawGuiContainerForegroundLayer(mouseX, mouseY);
   }

   protected abstract void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY);

   protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
   }

   @Override
   public void actionPerformed(ButtonBase button) {
   }

   public <B extends ButtonBase> B addButton(B button) {
      return this.addRenderableWidget(button);
   }

   public void drawString(String text, int x, int y, int color) {
      DrawHelper.graphics.drawString(this.getFontRenderer(), text, x, y, color, false);
   }

   public void drawString(String text, float x, float y, int color, boolean shadow) {
      DrawHelper.graphics.drawString(this.getFontRenderer(), text, (int)x, (int)y, color, shadow);
   }

   public void drawTexture(ResourceLocation texture, float x, float y, float z, float u, float v, float width, float height) {
      this.drawHelper.drawTexture(texture, x, y, z, u, v, width, height);
   }

   public void drawTexturedModalRect(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
      this.drawTexturedModalRect(texture, x, y, u, v, width, height, 1);
   }

   public void drawTexturedModalRect(ResourceLocation texture, int x, int y, int u, int v, int width, int height, int resolution) {
      float scaleX = 0.00390625F * (float)resolution;
      DrawHelper.quad(texture, x, y, this.zLevel, width, height, u * scaleX, v * scaleX, (u + width) * scaleX, (v + height) * scaleX);
   }

   public void drawBar(ResourceLocation texture, int x, int y, float z, float width, float height, int segments, int segmentLimit) {
      this.drawHelper.drawBar(texture, x, y, z, width, height, segments, segmentLimit);
   }
   /** The texture 1.12 had bound (TextureManager.bindTexture) for drawTexturedModalRect. */
   protected ResourceLocation boundTexture;

   public void bindTexture(ResourceLocation texture) {
      this.boundTexture = texture;
   }

   public void drawTexturedModalRect(int x, int y, int u, int v, int width, int height) {
      this.drawTexturedModalRect(this.boundTexture, x, y, u, v, width, height, 1);
   }

   public void drawTexturedModalRect(int x, int y, int u, int v, int width, int height, int resolution) {
      this.drawTexturedModalRect(this.boundTexture, x, y, u, v, width, height, resolution);
   }

   /** 1.12 buttonList. */
   public java.util.List<com.lycanitesmobs.client.gui.buttons.ButtonBase> buttons() {
      java.util.List<com.lycanitesmobs.client.gui.buttons.ButtonBase> list = new java.util.ArrayList<>();
      for (Object child : this.children()) {
         if (child instanceof com.lycanitesmobs.client.gui.buttons.ButtonBase button) {
            list.add(button);
         }
      }
      return list;
   }
}
