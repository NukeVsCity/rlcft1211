package com.lycanitesmobs.client.gui.beastiary.lists;

import com.lycanitesmobs.client.gui.beastiary.BeastiaryScreen;
import com.lycanitesmobs.core.info.CreatureInfo;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.lycanitesmobs.client.gui.GuiScrollingList;

public class CreatureFilterList extends GuiScrollingList {
   protected List<CreatureList> filteredLists = new ArrayList<>();
   protected BeastiaryScreen parentGui;

   public CreatureFilterList(BeastiaryScreen parentGui, int width, int height, int top, int bottom, int x, int slotHeight) {
      super(Minecraft.getInstance(), width, height, top, bottom, x, slotHeight, width, height);
      this.parentGui = parentGui;
   }

   public void refreshList() {
   }

   protected int getSize() {
      return 0;
   }

   protected void elementClicked(int index, boolean doubleClick) {
      this.selectedIndex = index;

      for (CreatureList creatureList : this.filteredLists) {
         if (creatureList != null) {
            creatureList.refreshList();
         }
      }
   }

   protected boolean isSelected(int index) {
      return false;
   }

   protected void drawBackground() {
   }

   protected int getContentHeight() {
      return this.getSize() * this.slotHeight;
   }

   protected void drawSlot(int index, int boxRight, int boxTop, int boxBottom, GuiGraphics graphics) {
   }

   public void addFilteredList(CreatureList creatureList) {
      if (!this.filteredLists.contains(creatureList)) {
         this.filteredLists.add(creatureList);
      }
   }

   public boolean canListCreature(CreatureInfo creatureInfo, CreatureList.Type listType) {
      return true;
   }
}
