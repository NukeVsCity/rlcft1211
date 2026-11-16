package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.client.KeyHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.resources.ResourceLocation;

public class MountTab extends Tab {
   public MountTab(int id) {
      super(id, Tab.startX, Tab.startY, new ResourceLocation("textures/items/saddle.png"));
   }

   @Override
   public void onTabClicked() {
      KeyBinding.setKeyBindState(KeyHandler.instance.mountInventory.getKeyCode(), true);
   }

   @Override
   public boolean shouldAddToList() {
      return false;
   }
}
