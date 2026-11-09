package com.github.alexthe666.iceandfire.client.gui;

import com.github.alexthe666.iceandfire.inventory.ContainerMyrmexCocoon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** 1.12 GuiMyrmexCocoon: the 2-row generic chest screen. */
public class GuiMyrmexCocoon extends AbstractContainerScreen<ContainerMyrmexCocoon> {
   private static final ResourceLocation CHEST_GUI_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
   private final int inventoryRows = 2;

   public GuiMyrmexCocoon(ContainerMyrmexCocoon menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title);
      this.imageHeight = 114 + this.inventoryRows * 18;
      this.inventoryLabelY = this.imageHeight - 96 + 2;
   }

   @Override
   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      super.render(g, mouseX, mouseY, partialTicks);
      this.renderTooltip(g, mouseX, mouseY);
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      g.blit(CHEST_GUI_TEXTURE, i, j, 0, 0, this.imageWidth, this.inventoryRows * 18 + 17);
      g.blit(CHEST_GUI_TEXTURE, i, j + this.inventoryRows * 18 + 17, 0, 126, this.imageWidth, 96);
   }
}
