package com.lycanitesmobs.client.gui.widgets;

import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.client.gui.SummoningPedestalScreen;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.lycanitesmobs.client.gui.GuiScrollingList;

public class SummoningPedestalList extends GuiScrollingList {
   SummoningPedestalScreen parentGUI;
   Map<Integer, String> minionList;

   public SummoningPedestalList(SummoningPedestalScreen parentGUI, ExtendedPlayer playerExt, int width, int height, int top, int bottom, int left) {
      super(Minecraft.getInstance(), width, height, top, bottom, left, 28, width, height);
      this.parentGUI = parentGUI;
      this.minionList = playerExt.getBeastiary().getSummonableList();
   }

   protected int getSize() {
      return this.minionList.size();
   }

   protected void elementClicked(int index, boolean doubleClick) {
      this.parentGUI.selectMinion(this.minionList.get(index));
   }

   protected boolean isSelected(int index) {
      return this.parentGUI.getSelectedMinion() != null && this.parentGUI.getSelectedMinion().equals(this.minionList.get(index));
   }

   protected void drawBackground() {
   }

   protected void drawSlot(int index, int boxRight, int boxTop, int boxBottom, GuiGraphics graphics) {
      String mobName = this.minionList.get(index);
      CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(mobName);
      int levelBarWidth = 9;
      int levelBarHeight = 9;
      int levelBarX = this.left + 20;
      int levelBarY = boxTop + boxBottom - levelBarHeight - 4;
      int level = creatureInfo.summonCost;
      if (level <= 10) {
         this.parentGUI.drawBar(AssetManager.getTexture("GUIPetLevel"), levelBarX, levelBarY, 0.0F, (float)levelBarWidth, (float)levelBarHeight, level, 10);
      }

      this.parentGUI.drawString(creatureInfo.getTitle(), this.left + 20, boxTop + 4, 16777215);
      this.parentGUI.bindTexture(creatureInfo.getIcon());
      this.parentGUI.drawTexturedModalRect(this.left + 2, boxTop + 4, 0, 0, 16, 16, 16);
   }
}
