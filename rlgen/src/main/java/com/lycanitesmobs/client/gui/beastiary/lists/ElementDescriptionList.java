package com.lycanitesmobs.client.gui.beastiary.lists;

import com.lycanitesmobs.client.gui.beastiary.BeastiaryScreen;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.info.ElementInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import com.lycanitesmobs.client.gui.GuiScrollingList;

public class ElementDescriptionList extends GuiScrollingList {
   protected BeastiaryScreen parentGui;
   public ElementInfo elementInfo;

   public ElementDescriptionList(BeastiaryScreen parentGui, int width, int height, int top, int bottom, int x) {
      super(Minecraft.getInstance(), width, height, top, bottom, x, 10800, width, height);
      this.parentGui = parentGui;
   }

   protected int getSize() {
      return 1;
   }

   protected void elementClicked(int index, boolean doubleClick) {
      this.selectedIndex = index;
   }

   protected boolean isSelected(int index) {
      return false;
   }

   protected void drawBackground() {
   }

   protected int getContentHeight() {
      return this.parentGui.getFontRenderer().wordWrapHeight(this.getContent(), this.listWidth) + 10;
   }

   protected void drawSlot(int index, int boxRight, int boxTop, int boxBottom, GuiGraphics graphics) {
      if (index == 0 && this.elementInfo != null) {
         this.parentGui.drawSplitString(this.getContent(), this.left + 6, boxTop, this.listWidth - 20, 16777215, true);
      }
   }

   public String getContent() {
      if (this.elementInfo == null) {
         return "";
      } else {
         String text = "§l" + this.elementInfo.getTitle() + ": §r";
         text = text + "\n" + this.elementInfo.getDescription();
         text = text + "\n\n§l" + LanguageManager.translate("gui.beastiary.elements.buffs") + ": §r";

         for (String buff : this.elementInfo.buffs) {
            ResourceLocation effectResource = ResourceLocation.parse(buff);
            text = text + "\n" + LanguageManager.translate("effect." + effectResource.getPath());
            text = text + ": " + LanguageManager.translate("effect." + effectResource.getPath() + ".description");
         }

         text = text + "\n\n§l" + LanguageManager.translate("gui.beastiary.elements.debuffs") + ": §r";

         for (String debuff : this.elementInfo.debuffs) {
            if ("burning".equals(debuff)) {
               text = text + "\n" + LanguageManager.translate("effect.burning");
               text = text + ": " + LanguageManager.translate("effect.burning.description");
            } else {
               ResourceLocation effectResource = ResourceLocation.parse(debuff);
               text = text + "\n" + LanguageManager.translate("effect." + effectResource.getPath());
               text = text + ": " + LanguageManager.translate("effect." + effectResource.getPath() + ".description");
            }
         }

         return text;
      }
   }

   protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2) {
      color1 = 856690704;
      this.graphics.fillGradient(left, top, right, bottom, color1, color1);
   }
}
