package com.alcatrazescapee.notreepunching.client.gui;

import com.alcatrazescapee.alcatrazcore.client.gui.GuiContainerCore;
import com.alcatrazescapee.notreepunching.common.container.ContainerFirePit;
import com.alcatrazescapee.notreepunching.common.tile.TileFirePit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** 1.12 GuiFirePit: the flame (14x13 from 176, 0) above the fuel slot and the cook arrow (23x16 from 190, 0). */
public class GuiFirePit extends GuiContainerCore<ContainerFirePit> {
   private final ResourceLocation background;

   public GuiFirePit(ContainerFirePit container, Inventory playerInv, Component title, ResourceLocation background) {
      super(container, playerInv, title, background);
      this.background = background;
   }

   @Override
   protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      super.renderBg(graphics, partialTicks, mouseX, mouseY);
      int x = (this.width - this.imageWidth) / 2;
      int y = (this.height - this.imageHeight) / 2;
      TileFirePit tile = this.menu.getTile();
      int burnTime = tile.getScaledBurnTicks();
      int cookTime = tile.getScaledCookTime();
      if (burnTime > 0) {
         graphics.blit(this.background, x + 81, y + 54 - burnTime, 176, 13 - burnTime, 14, burnTime);
      }
      if (cookTime > 0) {
         graphics.blit(this.background, x + 77, y + 23, 190, 0, cookTime, 16);
      }
   }
}
