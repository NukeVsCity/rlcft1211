package com.lycanitesmobs.client.gui.beastiary.lists;

import com.lycanitesmobs.client.gui.beastiary.BeastiaryScreen;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.info.CreatureInfo;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;

public class PetTypeList extends CreatureFilterList {
   private Map<Integer, String> petTypeList = new HashMap<>();

   public PetTypeList(BeastiaryScreen parentGui, int width, int height, int top, int bottom, int x) {
      super(parentGui, width, height, top, bottom, x, 24);
      this.refreshList();
   }

   @Override
   public void refreshList() {
      this.selectedIndex = this.parentGui.playerExt.selectedPetType;
      this.petTypeList.clear();
      this.petTypeList.put(0, "gui.beastiary.pets");
      this.petTypeList.put(1, "gui.beastiary.mounts");
      this.petTypeList.put(2, "gui.beastiary.familiars");
   }

   @Override
   protected int getSize() {
      return this.petTypeList.size();
   }

   @Override
   protected void elementClicked(int index, boolean doubleClick) {
      super.elementClicked(index, doubleClick);
      this.parentGui.playerExt.selectedPetType = index;

      for (CreatureList creatureList : this.filteredLists) {
         if (creatureList != null) {
            this.updateCreatureListType(creatureList);
         }
      }
   }

   public void updateCreatureListType(CreatureList creatureList) {
      CreatureList.Type listType = null;
      if (this.selectedIndex == 0) {
         listType = CreatureList.Type.PET;
      } else if (this.selectedIndex == 1) {
         listType = CreatureList.Type.MOUNT;
      } else if (this.selectedIndex == 2) {
         listType = CreatureList.Type.FAMILIAR;
      }

      creatureList.changeType(listType);
   }

   @Override
   protected boolean isSelected(int index) {
      return this.selectedIndex == index;
   }

   @Override
   protected void drawBackground() {
   }

   @Override
   protected void drawSlot(int index, int boxRight, int boxTop, int boxBottom, GuiGraphics graphics) {
      String petListType = this.petTypeList.get(index);
      if (petListType != null) {
         this.parentGui.drawString(LanguageManager.translate(petListType), (float)(this.left + 2), (float)(boxTop + 4), 16777215, true);
      }
   }

   @Override
   public void addFilteredList(CreatureList creatureList) {
      super.addFilteredList(creatureList);
      this.updateCreatureListType(creatureList);
   }

   @Override
   public boolean canListCreature(CreatureInfo creatureInfo, CreatureList.Type listType) {
      if (creatureInfo == null || listType == null) {
         return false;
      } else if (this.selectedIndex == 0 && listType == CreatureList.Type.PET) {
         return true;
      } else {
         return this.selectedIndex == 1 && listType == CreatureList.Type.MOUNT ? true : this.selectedIndex == 2 && listType == CreatureList.Type.FAMILIAR;
      }
   }
}
