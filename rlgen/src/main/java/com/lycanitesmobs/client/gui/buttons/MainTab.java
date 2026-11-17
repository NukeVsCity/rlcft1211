package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.client.gui.beastiary.IndexBeastiaryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class MainTab extends Tab {
   public MainTab(int id) {
      super(id, Tab.startX, Tab.startY, ResourceLocation.fromNamespaceAndPath("lycanitesmobs", "textures/items/soulgazer.png"));
   }

   public MainTab(int id, int x, int y) {
      super(id, x, y, ResourceLocation.fromNamespaceAndPath("lycanitesmobs", "textures/items/soulgazer.png"));
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
