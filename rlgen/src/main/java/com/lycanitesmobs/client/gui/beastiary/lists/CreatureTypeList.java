package com.lycanitesmobs.client.gui.beastiary.lists;

import com.lycanitesmobs.client.gui.beastiary.BeastiaryScreen;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.CreatureType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;

public class CreatureTypeList extends CreatureFilterList {
   private Map<Integer, CreatureType> creatureTypeList = new HashMap<>();

   public CreatureTypeList(BeastiaryScreen parentGui, int width, int height, int top, int bottom, int x) {
      super(parentGui, width, height, top, bottom, x, 16);
      this.refreshList();
   }

   @Override
   public void refreshList() {
      this.creatureTypeList.clear();
      int groupIndex = 0;
      List<CreatureType> creatureTypes = new ArrayList<>();
      creatureTypes.addAll(CreatureManager.getInstance().creatureTypes.values());
      creatureTypes.sort(Comparator.comparing(CreatureType::getTitle));

      for (CreatureType creatureType : creatureTypes) {
         if (this.parentGui.playerExt.beastiary.getCreaturesDiscovered(creatureType) > 0) {
            this.creatureTypeList.put(groupIndex++, creatureType);
         }
      }
   }

   @Override
   protected int getSize() {
      return this.creatureTypeList.size();
   }

   @Override
   protected void elementClicked(int index, boolean doubleClick) {
      this.parentGui.playerExt.selectedCreatureType = this.creatureTypeList.get(index);
      super.elementClicked(index, doubleClick);
   }

   @Override
   protected boolean isSelected(int index) {
      return this.parentGui.playerExt.selectedCreatureType != null && this.parentGui.playerExt.selectedCreatureType.equals(this.creatureTypeList.get(index));
   }

   @Override
   protected void drawBackground() {
   }

   @Override
   protected void drawSlot(int index, int boxRight, int boxTop, int boxBottom, GuiGraphics graphics) {
      CreatureType creatureType = this.creatureTypeList.get(index);
      if (creatureType != null) {
         this.parentGui.drawString(creatureType.getTitle(), (float)(this.left + 4), (float)(boxTop + 2), 16777215, true);
      }
   }

   @Override
   public boolean canListCreature(CreatureInfo creatureInfo, CreatureList.Type listType) {
      return this.parentGui.playerExt.selectedCreatureType != null && creatureInfo != null
         ? creatureInfo.creatureType == this.parentGui.playerExt.selectedCreatureType
         : false;
   }
}
