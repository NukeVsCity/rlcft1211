package com.alcatrazescapee.alcatrazcore.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * alcatrazcore 1.12 GuiContainerCore: 176x166 background texture, title centered at y 6, "Inventory" at (8, ySize - 94), both
 * 0x404040 without shadow, tooltips.
 */
public class GuiContainerCore<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
   private final ResourceLocation background;

   public GuiContainerCore(T container, Inventory playerInv, Component title, ResourceLocation background) {
      super(container, playerInv, title);
      this.background = background;
   }

   @Override
   protected void init() {
      super.init();
      this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
      this.titleLabelY = 6;
      this.inventoryLabelX = 8;
      this.inventoryLabelY = this.imageHeight - 94;
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      super.render(graphics, mouseX, mouseY, partialTicks);
      this.renderTooltip(graphics, mouseX, mouseY);
   }

   @Override
   protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      int x = (this.width - this.imageWidth) / 2;
      int y = (this.height - this.imageHeight) / 2;
      graphics.blit(this.background, x, y, 0, 0, this.imageWidth, this.imageHeight);
   }
}
