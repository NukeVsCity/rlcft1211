package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.client.KeyHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

public class MountTab extends Tab {
   public MountTab(int id) {
      super(id, Tab.startX, Tab.startY, ResourceLocation.withDefaultNamespace("textures/items/saddle.png"));
   }

   @Override
   public void onTabClicked() {
      KeyMapping.set(KeyHandler.instance.mountInventory.getKey(), true);
   }

   @Override
   public boolean shouldAddToList() {
      return false;
   }
}
