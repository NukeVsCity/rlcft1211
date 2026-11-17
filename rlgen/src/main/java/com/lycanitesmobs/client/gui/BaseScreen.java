package com.lycanitesmobs.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.12 BaseScreen (a GuiScreen). The drawing helpers are the 1.12 ones (see {@link DrawHelper}); {@link #render} keeps the
 * frame's GuiGraphics for them and calls the 1.12 style {@link #drawScreen}.
 */
public class BaseScreen extends Screen implements com.lycanitesmobs.client.gui.buttons.ButtonBase.Listener {
   protected Minecraft mc;
   protected float zLevel;
   protected DrawHelper drawHelper;
   /** The 1.12 Forge scrolling lists drawn by this screen (they register themselves when first drawn). */
   public final java.util.List<GuiScrollingList> scrollingLists = new java.util.ArrayList<>();

   public BaseScreen() {
      this(Component.empty());
   }

   public BaseScreen(Component title) {
      super(title);
      this.mc = Minecraft.getInstance();
      this.drawHelper = new DrawHelper(this.mc, this.mc.font);
   }

   /** 1.12 actionPerformed(ButtonBase). */
   @Override
   public void actionPerformed(com.lycanitesmobs.client.gui.buttons.ButtonBase button) {
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      for (GuiScrollingList list : java.util.List.copyOf(this.scrollingLists)) {
         if (list.drawnThisFrame && list.mouseClicked(mouseX, mouseY, button)) {
            return true;
         }
      }
      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      for (GuiScrollingList list : this.scrollingLists) {
         if (list.drawnThisFrame && list.mouseDragged(mouseX, mouseY, button)) {
            return true;
         }
      }
      return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      for (GuiScrollingList list : this.scrollingLists) {
         list.mouseReleased(mouseX, mouseY, button);
      }
      return super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      for (GuiScrollingList list : this.scrollingLists) {
         if (list.drawnThisFrame && list.mouseScrolled(mouseX, mouseY, scrollY)) {
            return true;
         }
      }
      return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
   }

   @Override
   protected void clearWidgets() {
      super.clearWidgets();
      this.scrollingLists.clear();
   }

   /** 1.12 buttonList.add. */
   public <T extends com.lycanitesmobs.client.gui.buttons.ButtonBase> T addButton(T button) {
      return this.addRenderableWidget(button);
   }

   public Font getFontRenderer() {
      return this.mc.font;
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      DrawHelper.setGraphics(graphics);
      for (GuiScrollingList list : this.scrollingLists) {
         list.drawnThisFrame = false;
      }
      this.drawScreen(graphics, mouseX, mouseY, partialTicks);
   }

   /** 1.12 drawScreen; super draws the widgets (1.12 buttonList). */
   public void drawScreen(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      super.render(graphics, mouseX, mouseY, partialTicks);
   }

   /** 1.21 draws a blurred/darkened background in super.render; 1.12 Lycanites screens drew their own. */
   @Override
   public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
   }

   /** 1.12 FontRenderer.drawString(text, x, y, color): no shadow. */
   public void drawString(String text, int x, int y, int color) {
      DrawHelper.graphics.drawString(this.getFontRenderer(), text, x, y, color, false);
   }

   public void drawString(String text, float x, float y, int color, boolean shadow) {
      DrawHelper.graphics.drawString(this.getFontRenderer(), text, (int)x, (int)y, color, shadow);
   }

   public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor, boolean shadow) {
      if (shadow) {
         DrawHelper.graphics.pose().translate(0.5F, 0.5F, 0.0F);
         this.drawHelper.drawSplitString(str, x, y, wrapWidth, 4473924);
         DrawHelper.graphics.pose().translate(-0.5F, -0.5F, 0.0F);
      }

      this.drawHelper.drawSplitString(str, x, y, wrapWidth, textColor);
   }

   public void drawTexture(ResourceLocation texture, float x, float y, float z, float u, float v, float width, float height) {
      this.drawHelper.drawTexture(texture, x, y, z, u, v, width, height);
   }

   public void drawTexturedTiled(ResourceLocation texture, float x, float y, float z, float u, float v, float width, float height, float resolution) {
      this.drawHelper.drawTextureTiled(texture, x, y, z, u, v, width, height, resolution);
   }

   public void drawBar(ResourceLocation texture, int x, int y, float z, float width, float height, int segments, int segmentLimit) {
      this.drawHelper.drawBar(texture, x, y, z, width, height, segments, segmentLimit);
   }

   public void drawTexturedModalRect(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
      this.drawHelper.drawTexturedModalRect(texture, x, y, u, v, width, height, 1);
   }

   public void drawTexturedModalRect(ResourceLocation texture, int x, int y, int u, int v, int width, int height, int resolution) {
      this.drawHelper.drawTexturedModalRect(texture, x, y, u, v, width, height, resolution);
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
