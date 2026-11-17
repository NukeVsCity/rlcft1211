package com.lycanitesmobs.client.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.12 Tab: a creative-inventory style tab drawn above the player inventory screen. 1.12 read the inventory size with an
 * InventorySnooperScreen (GuiInventory subclass); 1.21 InventoryScreen gives it directly.
 */
public abstract class Tab extends ButtonBase {
   public ResourceLocation texture = ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tabs.png");
   public ResourceLocation icon = null;
   public static int startX = 0;
   public static int startY = 0;
   public static int tabWidth = 28;
   public static int tabHeight = 32;
   public int tabID = 0;

   public Tab(int id, int posX, int posY, ResourceLocation icon) {
      super(550 + id, posX, posY, tabWidth, tabHeight, "");
      this.icon = icon;
      this.tabID = id;
   }

   private int[] tabPos() {
      int ySize = this.active ? tabWidth : tabHeight;
      int tabX = this.getX();
      int tabY = this.getY();
      Minecraft mc = Minecraft.getInstance();
      if (mc.screen instanceof InventoryScreen guiInventory) {
         tabX += guiInventory.width / 2 + guiInventory.getXSize() / 2;
         tabX -= tabWidth * (this.tabID + 1);
         tabX -= 4;
         tabY += guiInventory.height / 2 - guiInventory.getYSize() / 2;
         tabY -= ySize;
      }
      return new int[]{tabX, tabY, ySize};
   }

   /** 1.12 drawButton. */
   @Override
   protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      com.lycanitesmobs.client.gui.DrawHelper.setGraphics(graphics);
      int yTexPos = this.active ? 0 : tabHeight;
      int xOffset = this.id == 2 ? 0 : 1;
      int[] pos = this.tabPos();
      this.drawTexturedModalRect(this.texture, pos[0], pos[1], xOffset * 28, yTexPos, 28, pos[2]);
      if (this.icon != null) {
         this.drawTexturedModalRect(this.icon, pos[0] + 6, pos[1] + 10, 0, 0, 16, 16, 16);
      }
   }

   /** 1.12 mousePressed. */
   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button != 0) {
         return false;
      }
      int[] pos = this.tabPos();
      boolean inWindow = this.active && this.visible && mouseX >= pos[0] && mouseY >= pos[1] && mouseX < pos[0] + this.width && mouseY < pos[1] + this.height;
      if (inWindow) {
         this.onTabClicked();
      }

      return inWindow;
   }

   @Override
   public boolean isMouseOver(double mouseX, double mouseY) {
      int[] pos = this.tabPos();
      return this.visible && mouseX >= pos[0] && mouseY >= pos[1] && mouseX < pos[0] + this.width && mouseY < pos[1] + this.height;
   }

   public abstract void onTabClicked();

   public abstract boolean shouldAddToList();
}
