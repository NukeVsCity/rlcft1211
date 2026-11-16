package com.lycanitesmobs.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Forge 1.12 net.minecraftforge.fml.client.GuiScrollingList (the base of the Lycanites beastiary lists), on 1.21:
 * slots of slotHeight drawn between top and bottom (scissored), a selection box around the selected slot (grey 0x808080
 * border, black inside), a 6 px scrollbar on the right when the content is taller than the list, mouse wheel scrolling
 * (scrollDistance -= wheel * slotHeight / 2) and click/double click selection (double click within 250 ms).
 * The ported lists call {@link #drawScreen} from the screen's render and forward mouse events.
 */
public abstract class GuiScrollingList {
   protected final Minecraft client;
   protected final int listWidth;
   protected final int listHeight;
   protected final int screenWidth;
   protected final int screenHeight;
   protected final int top;
   protected final int bottom;
   protected final int right;
   protected final int left;
   protected final int slotHeight;
   protected int mouseX;
   protected int mouseY;
   private float initialMouseClickY = -2.0F;
   private float scrollFactor;
   private float scrollDistance;
   protected int selectedIndex = -1;
   private long lastClickTime = 0L;
   private boolean highlightSelected = true;
   private boolean hasHeader;
   private int headerHeight;
   protected boolean captureMouse = true;

   public GuiScrollingList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
      this.client = client;
      this.listWidth = width;
      this.listHeight = height;
      this.top = top;
      this.bottom = bottom;
      this.slotHeight = entryHeight;
      this.left = left;
      this.right = width + this.left;
      this.screenWidth = screenWidth;
      this.screenHeight = screenHeight;
   }

   public void setHeaderInfo(boolean hasHeader, int headerHeight) {
      this.hasHeader = hasHeader;
      this.headerHeight = headerHeight;
      if (!hasHeader) {
         this.headerHeight = 0;
      }
   }

   protected abstract int getSize();

   protected abstract void elementClicked(int index, boolean doubleClick);

   protected abstract boolean isSelected(int index);

   protected int getContentHeight() {
      return this.getSize() * this.slotHeight + this.headerHeight;
   }

   protected abstract void drawBackground();

   /** 1.12 drawSlot(index, right, top, bottom, tessellator): the Tessellator argument is gone. */
   protected abstract void drawSlot(int index, int right, int top, int height, GuiGraphics graphics);

   protected void drawHeader(int entryRight, int relativeY, GuiGraphics graphics) {
   }

   protected void clickHeader(int x, int y) {
   }

   protected void drawScreen(int mouseX, int mouseY) {
   }

   public int func_27256_c(int x, int y) {
      int left = this.left + 1;
      int right = this.left + this.listWidth - 7;
      int relativeY = y - this.top - this.headerHeight + (int)this.scrollDistance - 4;
      int entryIndex = relativeY / this.slotHeight;
      return x >= left && x <= right && entryIndex >= 0 && relativeY >= 0 && entryIndex < this.getSize() ? entryIndex : -1;
   }

   public boolean isHovering(double x, double y) {
      return x >= this.left && x <= this.left + this.listWidth && y >= this.top && y <= this.bottom;
   }

   private void applyScrollLimits() {
      int listHeight = this.getContentHeight() - (this.bottom - this.top - 4);
      if (listHeight < 0) {
         listHeight /= 2;
      }

      if (this.scrollDistance < 0.0F) {
         this.scrollDistance = 0.0F;
      }

      if (this.scrollDistance > (float)listHeight) {
         this.scrollDistance = (float)listHeight;
      }
   }

   /** 1.12 handleMouseInput with a wheel delta. */
   public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
      if (!this.isHovering(mouseX, mouseY) || scroll == 0.0) {
         return false;
      }
      this.scrollDistance += (float)((scroll > 0 ? -1 : 1) * this.slotHeight / 2);
      this.applyScrollLimits();
      return true;
   }

   /** The left button press part of 1.12 drawScreen (it polled Mouse.isButtonDown). */
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button != 0 || !this.isHovering(mouseX, mouseY)) {
         return false;
      }
      int boxLeft = this.left;
      int boxRight = this.left + this.listWidth - 6;
      int scrollBarLeft = boxRight;
      int scrollBarRight = scrollBarLeft + 6;
      if (mouseY >= this.top && mouseY <= this.bottom) {
         int mouseListY = (int)mouseY - this.top - this.headerHeight + (int)this.scrollDistance - 4;
         int slotIndex = mouseListY / this.slotHeight;
         if (mouseX >= boxLeft && mouseX <= boxRight && slotIndex >= 0 && mouseListY >= 0 && slotIndex < this.getSize()) {
            this.elementClicked(slotIndex, slotIndex == this.selectedIndex && System.currentTimeMillis() - this.lastClickTime < 250L);
            this.selectedIndex = slotIndex;
            this.lastClickTime = System.currentTimeMillis();
         } else if (mouseX >= boxLeft && mouseX <= boxRight && mouseListY < 0) {
            this.clickHeader((int)mouseX - boxLeft, (int)mouseY - this.top + (int)this.scrollDistance - 4);
         }

         if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight) {
            this.scrollFactor = -1.0F;
            int scrollHeight = this.getContentHeight() - (this.bottom - this.top) - 4;
            if (scrollHeight < 1) {
               scrollHeight = 1;
            }

            int var13 = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getContentHeight());
            if (var13 < 32) {
               var13 = 32;
            }

            if (var13 > this.bottom - this.top - 8) {
               var13 = this.bottom - this.top - 8;
            }

            this.scrollFactor /= (float)(this.bottom - this.top - var13) / (float)scrollHeight;
         } else {
            this.scrollFactor = 1.0F;
         }

         this.initialMouseClickY = (float)mouseY;
      } else {
         this.initialMouseClickY = -2.0F;
      }
      return true;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button) {
      if (button != 0 || this.initialMouseClickY < 0.0F) {
         return false;
      }
      this.scrollDistance -= ((float)mouseY - this.initialMouseClickY) * this.scrollFactor;
      this.initialMouseClickY = (float)mouseY;
      this.applyScrollLimits();
      return true;
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0) {
         this.initialMouseClickY = -1.0F;
      }
      return false;
   }

   public void drawScreen(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.drawBackground();
      this.applyScrollLimits();

      int listLength = this.getSize();
      int scrollBarWidth = 6;
      int scrollBarRight = this.left + this.listWidth;
      int scrollBarLeft = scrollBarRight - scrollBarWidth;
      int entryLeft = this.left;
      int entryRight = scrollBarLeft - 1;
      int viewHeight = this.bottom - this.top;
      int border = 4;

      graphics.enableScissor(this.left, this.top, this.left + this.listWidth, this.bottom);

      int baseY = this.top + border - (int)this.scrollDistance;
      if (this.hasHeader) {
         this.drawHeader(entryRight, baseY, graphics);
      }

      for (int slotIdx = 0; slotIdx < listLength; slotIdx++) {
         int slotTop = baseY + slotIdx * this.slotHeight + this.headerHeight;
         int slotBuffer = this.slotHeight - border;
         if (slotTop <= this.bottom && slotTop + slotBuffer >= this.top) {
            if (this.highlightSelected && this.isSelected(slotIdx)) {
               int min = entryLeft;
               int max = entryRight;
               graphics.fill(min, slotTop - 2, max, slotTop + slotBuffer + 2, 0xFF808080);
               graphics.fill(min + 1, slotTop - 1, max - 1, slotTop + slotBuffer + 1, 0xFF000000);
            }

            this.drawSlot(slotIdx, entryRight, slotTop, slotBuffer, graphics);
         }
      }

      int extraHeight = this.getContentHeight() + border - viewHeight;
      if (extraHeight > 0) {
         int height = viewHeight * viewHeight / this.getContentHeight();
         if (height < 32) {
            height = 32;
         }

         if (height > viewHeight - border * 2) {
            height = viewHeight - border * 2;
         }

         int barTop = (int)this.scrollDistance * (viewHeight - height) / extraHeight + this.top;
         if (barTop < this.top) {
            barTop = this.top;
         }

         graphics.fill(scrollBarLeft, this.top, scrollBarRight, this.bottom, 0xFF000000);
         graphics.fill(scrollBarLeft, barTop, scrollBarRight, barTop + height, 0xFF808080);
         graphics.fill(scrollBarLeft, barTop, scrollBarRight - 1, barTop + height - 1, 0xFFC0C0C0);
      }

      this.drawScreen(mouseX, mouseY);
      graphics.disableScissor();
   }

   protected int clamp(int value) {
      return Mth.clamp(value, 0, Math.max(0, this.getSize() - 1));
   }
}
