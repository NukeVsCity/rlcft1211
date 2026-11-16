package com.lycanitesmobs.client.gui.beastiary;

import com.lycanitesmobs.GuiHandler;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.beastiary.lists.ElementDescriptionList;
import com.lycanitesmobs.client.gui.beastiary.lists.ElementList;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.info.ElementInfo;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import net.minecraft.world.entity.player.Player;

public class ElementsBeastiaryScreen extends BeastiaryScreen {
   public ElementInfo elementInfo;
   protected ElementList elementList;
   protected ElementDescriptionList descriptionList;

   public static void openToPlayer(Player player) {
      if (player != null) {
         Minecraft.getInstance().setScreen(new ElementsBeastiaryScreen(player));
      }
   }

   public ElementsBeastiaryScreen(Player player) {
      super(player);
   }

   @Override
   public String getTitleText() {
      return this.elementInfo != null ? "" : LanguageManager.translate("gui.beastiary.elements");
   }

   @Override
   public void initControls() {
      super.initControls();
      this.elementList = new ElementList(this, this.colLeftWidth, this.colLeftHeight, this.colLeftY, this.colLeftY + this.colLeftHeight, this.colLeftX);
      int descriptionListY = this.colRightY;
      this.descriptionList = new ElementDescriptionList(
         this, this.colRightWidth, this.colRightHeight, descriptionListY, this.colRightY + this.colRightHeight, this.colRightX
      );
   }

   @Override
   public void drawBackground(int x, int y, float partialTicks) {
      super.drawBackground(x, y, partialTicks);
   }

   @Override
   protected void updateControls(int x, int y, float partialTicks) {
      this.elementList.drawScreen(x, y, partialTicks);
      if (this.elementInfo != null) {
         this.descriptionList.elementInfo = this.elementInfo;
         this.descriptionList.drawScreen(x, y, partialTicks);
      }
   }

   @Override
   public void drawForeground(int x, int y, float partialTicks) {
      super.drawForeground(x, y, partialTicks);
      if (this.elementInfo == null) {
         String info = LanguageManager.translate("gui.beastiary.elements.about");
         this.drawSplitString(info, this.colRightX + 1, this.colRightY + 12 + 1, this.colRightWidth, 16777215, true);
      }
   }

   @Override
   public void actionPerformed(ButtonBase guiButton) {
      super.actionPerformed(guiButton);
   }
}
