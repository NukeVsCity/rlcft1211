package com.lycanitesmobs.client.gui;

import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.world.entity.player.Player;

public class InventorySnooperScreen extends GuiInventory {
   public InventorySnooperScreen(Player entityPlayer) {
      super(entityPlayer);
   }

   public String getButtonListFieldName() {
      try {
         for (Field field : GuiScreen.class.getDeclaredFields()) {
            field.setAccessible(true);
            Object object = field.get(this);
            if (object instanceof List && (List)object == this.buttonList) {
               return field.getName();
            }
         }
      } catch (Exception var6) {
      }

      return "";
   }

   public int getGUIXSize() {
      return this.xSize;
   }

   public int getGUIYSize() {
      return this.ySize;
   }
}
