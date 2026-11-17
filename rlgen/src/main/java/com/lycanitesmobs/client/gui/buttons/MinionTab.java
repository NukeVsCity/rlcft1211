package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.client.gui.beastiary.SummoningBeastiaryScreen;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class MinionTab extends Tab {
   public MinionTab(int id) {
      super(id, Tab.startX, Tab.startY, ResourceLocation.fromNamespaceAndPath("lycanitesmobs", "textures/items/summoningstaff.png"));
   }

   @Override
   public void onTabClicked() {
      if (ExtendedPlayer.getForPlayer(Minecraft.getInstance().player) != null) {
         SummoningBeastiaryScreen.openToPlayer(Minecraft.getInstance().player);
      }
   }

   @Override
   public boolean shouldAddToList() {
      return true;
   }
}
