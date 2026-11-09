package com.github.alexthe666.iceandfire.client.gui;

import com.github.alexthe666.iceandfire.inventory.ContainerPodium;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** 1.12 GuiPodium (133 px high). */
public class GuiPodium extends AbstractContainerScreen<ContainerPodium> {
   private static final ResourceLocation TEXTURE = ResourceLocation.parse("iceandfire:textures/gui/podium.png");

   public GuiPodium(ContainerPodium menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title);
      this.imageHeight = 133;
      this.inventoryLabelY = this.imageHeight - 96 + 2;
   }

   @Override
   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      g.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
      g.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      g.blit(TEXTURE, k, l, 0, 0, this.imageWidth, this.imageHeight);
   }

   @Override
   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      super.render(g, mouseX, mouseY, partialTicks);
      this.renderTooltip(g, mouseX, mouseY);
   }
}
