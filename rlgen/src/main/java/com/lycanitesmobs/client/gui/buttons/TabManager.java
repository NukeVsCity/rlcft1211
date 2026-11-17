package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.LycanitesMobs;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 1.12 TabManager. 1.12 put the tabs into the button list of a just opened GuiInventory by reflection (KeyHandler tick);
 * 1.21 adds them as widgets when the inventory screen is initialised (also after a resize, where 1.12 lost them).
 */
public class TabManager {
   public static ArrayList<Tab> tabList = new ArrayList<>();
   private static boolean listening;

   public static void registerTab(Tab tab) {
      tabList.add(tab);
      if (!listening) {
         listening = true;
         NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, event -> {
            if (event.getScreen().getClass() == InventoryScreen.class && showTabs()) {
               for (Tab t : tabList) {
                  if (t.shouldAddToList()) {
                     event.addListener(t);
                  }
               }
            }
         });
      }
   }

   private static boolean showTabs() {
      return LycanitesMobs.config == null || LycanitesMobs.config.getBool("GUI", "Show Inventory Tabs", true, "Set to false to disable the GUI tabs.");
   }

   public static ArrayList<Tab> getTabList() {
      return tabList;
   }

   /** Called by KeyHandler when the inventory opens: the tabs are added by the screen init listener above. */
   public static void addTabsToInventory(Screen gui) {
   }

   public static void openInventoryGui() {
      Minecraft mc = Minecraft.getInstance();
      mc.setScreen(new InventoryScreen(mc.player));
   }

   public static void updateTabValues(int cornerX, int cornerY, Class<?> selectedButton) {
      int count = 2;

      for (int i = 0; i < tabList.size(); i++) {
         Tab t = tabList.get(i);
         if (t.shouldAddToList()) {
            t.id = count;
            t.setX(cornerX + (count - 2) * 28);
            t.setY(cornerY - 28);
            t.active = !t.getClass().equals(selectedButton);
            count++;
         }
      }
   }

   public static void addTabsToList(List<Object> buttonList) {
      for (Tab tab : tabList) {
         if (tab.shouldAddToList()) {
            buttonList.add(tab);
         }
      }
   }
}
