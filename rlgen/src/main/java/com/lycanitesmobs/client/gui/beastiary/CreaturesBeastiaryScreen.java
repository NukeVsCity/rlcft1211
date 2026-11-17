package com.lycanitesmobs.client.gui.beastiary;

import net.minecraft.client.Minecraft;
import com.lycanitesmobs.GuiHandler;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.client.gui.beastiary.lists.CreatureDescriptionList;
import com.lycanitesmobs.client.gui.beastiary.lists.CreatureList;
import com.lycanitesmobs.client.gui.beastiary.lists.CreatureTypeList;
import com.lycanitesmobs.client.gui.beastiary.lists.SubspeciesList;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureKnowledge;
import com.lycanitesmobs.core.info.Subspecies;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import net.minecraft.world.entity.player.Player;

public class CreaturesBeastiaryScreen extends BeastiaryScreen {
   public CreatureTypeList creatureTypeList;
   public CreatureList creatureList;
   public SubspeciesList subspeciesList;
   public CreatureDescriptionList descriptionList;

   public static void openToPlayer(Player player) {
      if (player != null) {
         Minecraft.getInstance().setScreen(new CreaturesBeastiaryScreen(player));
      }
   }

   public CreaturesBeastiaryScreen(Player player) {
      super(player);
   }

   @Override
   public String getTitleText() {
      if (this.creatureList != null && this.playerExt.selectedCreature != null) {
         return "";
      } else if (this.creatureTypeList != null && this.playerExt.selectedCreatureType != null) {
         return this.playerExt.selectedCreatureType.getTitle();
      } else {
         if (this.playerExt.getBeastiary().creatureKnowledgeList.isEmpty()) {
            LanguageManager.translate("gui.beastiary.creatures.empty.title");
         }

         return LanguageManager.translate("gui.beastiary.creatures");
      }
   }

   @Override
   public void initControls() {
      super.initControls();
      this.creatureTypeList = new CreatureTypeList(
         this, this.colLeftWidth, this.colLeftHeight, this.colLeftY, this.colLeftY + this.colLeftHeight, this.colLeftX
      );
      int selectionListsWidth = this.getScaledX(0.125F);
      int creatureListY = this.colRightY;
      int creatureListHeight = Math.round((float)this.colRightHeight * 0.6F);
      this.creatureList = new CreatureList(
         CreatureList.Type.KNOWLEDGE,
         this,
         this.creatureTypeList,
         selectionListsWidth,
         creatureListHeight,
         creatureListY,
         creatureListY + creatureListHeight,
         this.colRightX
      );
      int subspeciesListY = creatureListY + 2 + creatureListHeight;
      int subspeciesListHeight = Math.round((float)this.colRightHeight * 0.4F) - 2;
      this.subspeciesList = new SubspeciesList(
         this, false, selectionListsWidth, subspeciesListHeight, subspeciesListY, subspeciesListY + subspeciesListHeight, this.colRightX
      );
      int newLine = this.getFontRenderer().wordWrapHeight("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA", this.colRightWidth - selectionListsWidth) + 2;
      int descriptionListY = this.colRightY + newLine * 3;
      this.descriptionList = new CreatureDescriptionList(
         this,
         this.colRightWidth - selectionListsWidth,
         this.colRightHeight,
         descriptionListY,
         this.colRightY + this.colRightHeight,
         this.colRightX + selectionListsWidth + 2
      );
   }

   @Override
   public void drawBackground(int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(mouseX, mouseY, partialTicks);
   }

   @Override
   protected void updateControls(int mouseX, int mouseY, float partialTicks) {
      super.updateControls(mouseX, mouseY, partialTicks);
      if (!this.playerExt.getBeastiary().creatureKnowledgeList.isEmpty()) {
         this.creatureTypeList.drawScreen(mouseX, mouseY, partialTicks);
         if (this.playerExt.selectedCreatureType != null) {
            this.creatureList.drawScreen(mouseX, mouseY, partialTicks);
            this.subspeciesList.drawScreen(mouseX, mouseY, partialTicks);
         }
      }
   }

   @Override
   public void drawForeground(int mouseX, int mouseY, float partialTicks) {
      super.drawForeground(mouseX, mouseY, partialTicks);
      int marginX = this.getScaledX(0.125F) + 8;
      int nextX = this.colRightX + marginX;
      int nextY = this.colRightY;
      int width = this.colRightWidth - marginX;
      if (this.playerExt.getBeastiary().creatureKnowledgeList.isEmpty()) {
         nextY += 20;
         String text = LanguageManager.translate("gui.beastiary.creatures.empty.info");
         this.drawSplitString(text, this.colRightX, nextY, this.colRightWidth, 16777215, true);
      } else {
         if (this.playerExt.selectedCreature != null) {
            Subspecies subspecies = this.playerExt.selectedCreature.getSubspecies(this.playerExt.selectedSubspecies);
            this.renderCreature(
               this.playerExt.selectedCreature, this.colRightX + marginX / 2 + this.colRightWidth / 2, this.colRightY + 100, mouseX, mouseY, partialTicks
            );
            CreatureInfo creatureInfo = this.playerExt.selectedCreature;
            CreatureKnowledge creatureKnowledge = this.playerExt.beastiary.getCreatureKnowledge(this.playerExt.selectedCreature.getName());
            String text = "§l" + LanguageManager.translate("creature.stat.element") + ": §r";
            text = text + (creatureInfo.elements != null ? creatureInfo.getElementNames(subspecies) : "None");
            this.drawString(text, (float)nextX, (float)nextY, 16777215, true);
            nextY += 2 + this.getFontRenderer().wordWrapHeight(text, width);
            text = "§l" + LanguageManager.translate("creature.stat.cost") + ": §r";
            this.drawString(text, (float)nextX, (float)nextY, 16777215, true);
            this.drawLevel(creatureInfo, AssetManager.getTexture("GUIPetLevel"), nextX + this.getFontRenderer().width(text), nextY);
            nextY += 2 + this.getFontRenderer().wordWrapHeight(text, width);
            text = "§l" + LanguageManager.translate("creature.stat.knowledge") + ": §r";
            int rankX = nextX + this.getFontRenderer().width(text);
            int barX = rankX + 29;
            int barWidth = 80;
            int barHeight = 10;
            int barCenter = barX + barWidth / 2;
            this.drawString(text, (float)nextX, (float)nextY, 16777215, true);
            this.drawBar(AssetManager.getTexture("GUIPetSpiritEmpty"), rankX, nextY, 0.0F, 9.0F, 9.0F, 3, 10);
            this.drawTexture(AssetManager.getTexture("GUIPetBarEmpty"), (float)barX, (float)nextY, 0.0F, 1.0F, 1.0F, (float)barWidth, (float)barHeight);
            if (creatureKnowledge != null) {
               this.drawBar(AssetManager.getTexture("GUIPetSpiritUsed"), rankX, nextY, 0.0F, 9.0F, 9.0F, creatureKnowledge.rank, 10);
               this.drawBar(AssetManager.getTexture("GUIPetSpiritUsed"), rankX, nextY, 0.0F, 9.0F, 9.0F, creatureKnowledge.rank, 10);
               float experienceNormal = 1.0F;
               if (creatureKnowledge.getMaxExperience() > 0) {
                  experienceNormal = (float)creatureKnowledge.experience / (float)creatureKnowledge.getMaxExperience();
               }

               this.drawTexture(
                  AssetManager.getTexture("GUIBarExperience"),
                  (float)barX,
                  (float)nextY,
                  0.0F,
                  experienceNormal,
                  1.0F,
                  (float)barWidth * experienceNormal,
                  (float)barHeight
               );
               String experienceText = "100%";
               if (creatureKnowledge.getMaxExperience() > 0) {
                  experienceText = creatureKnowledge.experience + "/" + creatureKnowledge.getMaxExperience();
               }

               this.drawString(
                     experienceText, Math.round((float)barCenter - (float)this.getFontRenderer().width(experienceText) / 2.0F), nextY + 2, 16777215
                  );
            }

            this.descriptionList.creatureKnowledge = creatureKnowledge;
            this.descriptionList.drawScreen(mouseX, mouseY, partialTicks);
         } else if (this.playerExt.selectedCreatureType != null) {
            nextY += 20;
            String text = LanguageManager.translate("gui.beastiary.creatures.discovered") + ": ";
            text = text + this.playerExt.getBeastiary().getCreaturesDiscovered(this.playerExt.selectedCreatureType);
            text = text + "/" + this.playerExt.selectedCreatureType.creatures.size();
            this.drawString(text, (float)nextX, (float)nextY, 16777215, true);
         } else {
            nextY += 20;
            String text = LanguageManager.translate("gui.beastiary.creatures.select");
            this.drawSplitString(text, this.colRightX, nextY, this.colRightWidth, 16777215, true);
         }
      }
   }

   @Override
   public void actionPerformed(ButtonBase guiButton) {
      super.actionPerformed(guiButton);
   }
}
