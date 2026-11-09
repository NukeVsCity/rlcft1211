package com.github.alexthe666.iceandfire.client.gui;

import com.github.alexthe666.iceandfire.inventory.ContainerHippocampus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Ice and Fire 1.7.1 GuiHippocampus: the hippogryph GUI texture. */
public class GuiHippocampus extends AbstractContainerScreen<ContainerHippocampus> {
   private float mousePosx;
   private float mousePosY;

   public GuiHippocampus(ContainerHippocampus menu, Inventory inv, Component title) {
      super(menu, inv, title);
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
      GuiHippogryph.drawBackground(g, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, this.menu.hippocampus.isChested(), this.menu.hippocampus,
         this.mousePosx, this.mousePosY);
   }

   @Override
   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      this.mousePosx = mouseX;
      this.mousePosY = mouseY;
      super.render(g, mouseX, mouseY, partialTicks);
      this.renderTooltip(g, mouseX, mouseY);
   }
}
