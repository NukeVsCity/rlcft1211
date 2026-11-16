package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.client.gui.beastiary.IndexBeastiaryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class BeastiaryTab extends Tab {
   public BeastiaryTab(int id) {
      super(id, Tab.startX, Tab.startY, new ResourceLocation("lycanitesmobs", "textures/items/soulgazer.png"));
   }

   @Override
   public void onTabClicked() {
      IndexBeastiaryScreen.openToPlayer(Minecraft.getInstance().player);
   }

   @Override
   public boolean shouldAddToList() {
      return true;
   }
}
