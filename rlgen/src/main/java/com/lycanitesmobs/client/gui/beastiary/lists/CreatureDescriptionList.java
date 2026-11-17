package com.lycanitesmobs.client.gui.beastiary.lists;

import com.lycanitesmobs.client.gui.beastiary.BeastiaryScreen;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureKnowledge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.lycanitesmobs.client.gui.GuiScrollingList;

public class CreatureDescriptionList extends GuiScrollingList {
   protected BeastiaryScreen parentGui;
   public CreatureKnowledge creatureKnowledge;

   public CreatureDescriptionList(BeastiaryScreen parentGui, int width, int height, int top, int bottom, int x) {
      super(Minecraft.getInstance(), width, height, top, bottom, x, 10800, width, height);
      this.parentGui = parentGui;
   }

   protected int getSize() {
      return 1;
   }

   protected void elementClicked(int index, boolean doubleClick) {
      this.selectedIndex = index;
   }

   protected boolean isSelected(int index) {
      return false;
   }

   protected void drawBackground() {
   }

   protected int getContentHeight() {
      return this.parentGui.getFontRenderer().wordWrapHeight(this.getContent(), this.listWidth) + 10;
   }

   protected void drawSlot(int index, int boxRight, int boxTop, int boxBottom, GuiGraphics graphics) {
      if (index == 0 && this.creatureKnowledge != null) {
         this.parentGui.drawSplitString(this.getContent(), this.left + 6, boxTop, this.listWidth - 20, 16777215, true);
      }
   }

   public String getContent() {
      if (this.creatureKnowledge == null) {
         return "";
      } else {
         CreatureInfo creatureInfo = this.creatureKnowledge.getCreatureInfo();
         if (creatureInfo == null) {
            return "";
         } else {
            String text = "";
            if (creatureInfo.creatureType != null) {
               if (creatureInfo.isTameable() && creatureInfo.creatureType.getTreatItem() != null) {
                  text = text
                     + "§l"
                     + LanguageManager.translate("gui.beastiary.tameable")
                     + ": §r"
                     + LanguageManager.translate(creatureInfo.creatureType.getTreatItem().getDescriptionId())
                     + "\n\n";
                  if (creatureInfo.isMountable()) {
                     text = text + "§l" + LanguageManager.translate("gui.beastiary.mountable") + "§r\n\n";
                  }
               }

               if (creatureInfo.isSummonable()) {
                  text = text + "§l" + LanguageManager.translate("gui.beastiary.summonable") + "§r\n\n";
               }

               if ((creatureInfo.isTameable() || creatureInfo.isSummonable()) && creatureInfo.isPerchable()) {
                  text = text + "§l" + LanguageManager.translate("gui.beastiary.perchable") + "§r\n\n";
               }
            }

            text = text + "§l" + LanguageManager.translate("gui.beastiary.diet") + ": §r" + creatureInfo.getDietNames() + "\n\n";
            text = text + "§l" + LanguageManager.translate("gui.beastiary.summary") + ": §r";
            text = text + "\n" + creatureInfo.getDescription();
            text = text + "\n\n§l" + LanguageManager.translate("creature.stat.base") + ": §r";
            if (this.creatureKnowledge.rank >= 2) {
               String statPrefix = "\n" + LanguageManager.translate("creature.stat.base") + " ";
               text = text + "\n" + LanguageManager.translate("creature.stat.health") + ": " + creatureInfo.health;
               text = text + "\n" + LanguageManager.translate("creature.stat.defense") + ": " + creatureInfo.defense;
               text = text + "\n" + LanguageManager.translate("creature.stat.speed") + ": " + creatureInfo.speed;
               text = text + "\n" + LanguageManager.translate("creature.stat.damage") + ": " + creatureInfo.damage;
               text = text + "\n" + LanguageManager.translate("creature.stat.pierce") + ": " + creatureInfo.pierce;
               String effectText = creatureInfo.effectDuration + "s " + creatureInfo.effectAmplifier + "X";
               if (creatureInfo.effectDuration <= 0.0 || creatureInfo.effectAmplifier < 0.0) {
                  effectText = LanguageManager.translate("common.none");
               }

               text = text + "\n" + LanguageManager.translate("creature.stat.effect") + ": " + effectText;
            } else {
               text = text
                  + "\n"
                  + LanguageManager.translate("gui.beastiary.unlockedat")
                  + " "
                  + LanguageManager.translate("creature.stat.knowledge")
                  + " "
                  + 2;
            }

            text = text + "\n\n§l" + LanguageManager.translate("gui.beastiary.combat") + ": §r";
            if (this.creatureKnowledge.rank >= 2) {
               text = text + "\n" + creatureInfo.getCombatDescription();
            } else {
               text = text
                  + "\n"
                  + LanguageManager.translate("gui.beastiary.unlockedat")
                  + " "
                  + LanguageManager.translate("creature.stat.knowledge")
                  + " "
                  + 2;
            }

            text = text + "\n\n§l" + LanguageManager.translate("gui.beastiary.habitat") + ": §r";
            if (this.creatureKnowledge.rank >= 2) {
               text = text + "\n" + creatureInfo.getHabitatDescription();
            } else {
               text = text
                  + "\n"
                  + LanguageManager.translate("gui.beastiary.unlockedat")
                  + " "
                  + LanguageManager.translate("creature.stat.knowledge")
                  + " "
                  + 2;
            }

            text = text + "\n\n§l" + LanguageManager.translate("gui.beastiary.biomes") + ": §r";
            if (this.creatureKnowledge.rank >= 2) {
               text = text + "\n" + creatureInfo.getBiomeNames();
            } else {
               text = text
                  + "\n"
                  + LanguageManager.translate("gui.beastiary.unlockedat")
                  + " "
                  + LanguageManager.translate("creature.stat.knowledge")
                  + " "
                  + 2;
            }

            text = text + "\n\n§l" + LanguageManager.translate("gui.beastiary.drops") + ": §r";
            if (this.creatureKnowledge.rank >= 2) {
               text = text + "\n" + creatureInfo.getDropNames();
            } else {
               text = text
                  + "\n"
                  + LanguageManager.translate("gui.beastiary.unlockedat")
                  + " "
                  + LanguageManager.translate("creature.stat.knowledge")
                  + " "
                  + 2;
            }

            return text;
         }
      }
   }

   protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2) {
      color1 = 856690704;
      this.graphics.fillGradient(left, top, right, bottom, color1, color1);
   }
}
