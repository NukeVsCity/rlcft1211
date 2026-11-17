package com.lycanitesmobs.client.gui.beastiary.lists;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.client.gui.beastiary.BeastiaryScreen;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.Subspecies;
import com.lycanitesmobs.core.info.Variant;
import com.lycanitesmobs.core.network.MessageSummonSetSelection;
import com.lycanitesmobs.core.pets.PetEntry;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.lycanitesmobs.client.gui.GuiScrollingList;

public class CreatureList extends GuiScrollingList {
   private CreatureList.Type listType;
   private BeastiaryScreen parentGui;
   private CreatureFilterList filterList;
   private Map<Integer, CreatureInfo> creatureList = new HashMap<>();
   private Map<Integer, PetEntry> petList = new HashMap<>();
   private boolean releaseRefresh;

   public CreatureList(CreatureList.Type listType, BeastiaryScreen parentGui, CreatureFilterList filterList, int width, int height, int top, int bottom, int x) {
      super(Minecraft.getInstance(), width, height, top, bottom, x, 24, width, height);
      this.listType = listType;
      this.parentGui = parentGui;
      this.filterList = filterList;
      if (this.filterList != null) {
         this.filterList.addFilteredList(this);
      }

      this.refreshList();
   }

   public void refreshList() {
      this.creatureList.clear();
      this.petList.clear();
      int creatureIndex = 0;
      if (this.listType != CreatureList.Type.KNOWLEDGE && this.listType != CreatureList.Type.SUMMONABLE) {
         if (this.listType == CreatureList.Type.PET || this.listType == CreatureList.Type.MOUNT || this.listType == CreatureList.Type.FAMILIAR) {
            String petType = "pet";
            if (this.listType == CreatureList.Type.MOUNT) {
               petType = "mount";
            } else if (this.listType == CreatureList.Type.FAMILIAR) {
               petType = "familiar";
            }

            List<PetEntry> pets = this.parentGui.playerExt.petManager.createEntryListByType(petType);
            pets.sort(Comparator.comparing(PetEntry::getDisplayName));

            for (PetEntry petEntry : pets) {
               CreatureInfo creatureInfo = petEntry.getCreatureInfo();
               if (creatureInfo != null && (this.filterList == null || this.filterList.canListCreature(creatureInfo, this.listType))) {
                  this.petList.put(creatureIndex++, petEntry);
               }
            }
         }
      } else {
         List<String> creatures = new ArrayList<>(this.parentGui.playerExt.getBeastiary().creatureKnowledgeList.keySet());
         creatures.sort(Collator.getInstance(new Locale("US")));

         for (String creatureName : creatures) {
            CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(creatureName.toLowerCase());
            if ((
                  this.listType != CreatureList.Type.SUMMONABLE
                     || creatureInfo.isSummonable()
                        && this.parentGui.playerExt.getBeastiary().getCreatureKnowledge(creatureName) != null
                        && this.parentGui.playerExt.getBeastiary().getCreatureKnowledge(creatureName).rank >= 2
               )
               && creatureInfo != null
               && (this.filterList == null || this.filterList.canListCreature(creatureInfo, this.listType))) {
               this.creatureList.put(creatureIndex++, creatureInfo);
            }
         }
      }
   }

   protected int getSize() {
      if (this.listType == CreatureList.Type.KNOWLEDGE || this.listType == CreatureList.Type.SUMMONABLE) {
         return this.creatureList.size();
      } else {
         return this.listType != CreatureList.Type.PET && this.listType != CreatureList.Type.MOUNT && this.listType != CreatureList.Type.FAMILIAR
            ? 0
            : this.petList.size();
      }
   }

   protected void elementClicked(int index, boolean doubleClick) {
      this.selectedIndex = index;
      if (this.listType == CreatureList.Type.KNOWLEDGE) {
         this.parentGui.playerExt.selectedCreature = this.creatureList.get(index);
         this.parentGui.playerExt.selectedSubspecies = 0;
         this.parentGui.playerExt.selectedVariant = 0;
      } else if (this.listType == CreatureList.Type.SUMMONABLE) {
         this.parentGui.playerExt.getSelectedSummonSet().setSummonType(this.creatureList.get(index).getName());
         Subspecies subspecies = this.creatureList.get(index).getSubspecies(this.parentGui.playerExt.getSelectedSummonSet().getSubspecies());
         this.parentGui.playerExt.getSelectedSummonSet().setSubspecies(subspecies.index);
         Variant variant = subspecies.getVariant(this.parentGui.playerExt.getSelectedSummonSet().getVariant());
         int variantId = variant != null ? variant.index : 0;
         if (!this.parentGui.playerExt.getBeastiary().hasKnowledgeRank(this.creatureList.get(index).getName(), 2)) {
            variantId = 0;
         }

         this.parentGui.playerExt.getSelectedSummonSet().setVariant(variantId);
         this.parentGui.playerExt.sendSummonSetToServer((byte)this.parentGui.playerExt.selectedSummonSet);
         MessageSummonSetSelection message = new MessageSummonSetSelection(this.parentGui.playerExt);
         LycanitesMobs.packetHandler.sendToServer(message);
      } else if (this.listType == CreatureList.Type.PET || this.listType == CreatureList.Type.MOUNT || this.listType == CreatureList.Type.FAMILIAR) {
         this.parentGui.playerExt.selectedPet = this.petList.get(index);
      }
   }

   protected boolean isSelected(int index) {
      if (this.listType == CreatureList.Type.KNOWLEDGE) {
         return this.parentGui.playerExt.selectedCreature != null && this.parentGui.playerExt.selectedCreature.equals(this.creatureList.get(index));
      } else if (this.listType == CreatureList.Type.SUMMONABLE) {
         return this.parentGui.playerExt.getSelectedSummonSet().getCreatureInfo() != null
            && this.parentGui.playerExt.getSelectedSummonSet().getCreatureInfo().equals(this.creatureList.get(index));
      } else {
         return this.listType != CreatureList.Type.PET && this.listType != CreatureList.Type.MOUNT && this.listType != CreatureList.Type.FAMILIAR
            ? false
            : this.parentGui.playerExt.selectedPet != null && this.parentGui.playerExt.selectedPet.equals(this.petList.get(index));
      }
   }

   protected void drawBackground() {
      if ((this.listType == CreatureList.Type.PET || this.listType == CreatureList.Type.MOUNT || this.listType == CreatureList.Type.FAMILIAR)
         && this.parentGui.playerExt.selectedPet != null
         && this.releaseRefresh != this.parentGui.playerExt.selectedPet.releaseEntity) {
         this.releaseRefresh = this.parentGui.playerExt.selectedPet.releaseEntity;
         this.refreshList();
      }
   }

   protected int getContentHeight() {
      return this.getSize() * this.slotHeight;
   }

   protected void drawSlot(int index, int boxRight, int boxTop, int boxBottom, GuiGraphics graphics) {
      if (this.listType != CreatureList.Type.KNOWLEDGE && this.listType != CreatureList.Type.SUMMONABLE) {
         PetEntry petEntry = this.petList.get(index);
         if (petEntry == null) {
            return;
         }

         int nameY = boxTop + 6;
         if (this.listType == CreatureList.Type.PET || this.listType == CreatureList.Type.MOUNT) {
            nameY = boxTop + 2;
         }

         this.parentGui.drawString(petEntry.getDisplayName(), this.left + 20, nameY, 16777215);
         if (this.listType == CreatureList.Type.PET || this.listType == CreatureList.Type.MOUNT) {
            this.parentGui.drawLevel(petEntry.getCreatureInfo(), AssetManager.getTexture("GUIPetLevel"), this.left + 18, boxTop + 10);
         }

         if (petEntry.getCreatureInfo().getIcon() != null) {
            this.parentGui.drawTexture(petEntry.getCreatureInfo().getIcon(), (float)(this.left + 2), (float)(boxTop + 2), 0.0F, 1.0F, 1.0F, 16.0F, 16.0F);
         }
      } else {
         CreatureInfo creatureInfo = this.creatureList.get(index);
         if (creatureInfo == null) {
            return;
         }

         int nameYx = boxTop + 6;
         if (this.listType == CreatureList.Type.SUMMONABLE) {
            nameYx = boxTop + 2;
         }

         this.parentGui.drawString(creatureInfo.getTitle(), this.left + 20, nameYx, 16777215);
         if (this.listType == CreatureList.Type.SUMMONABLE) {
            this.parentGui.drawLevel(creatureInfo, AssetManager.getTexture("GUIPetLevel"), this.left + 18, boxTop + 10);
         }

         if (creatureInfo.getIcon() != null) {
            this.parentGui.drawTexture(creatureInfo.getIcon(), (float)(this.left + 2), (float)(boxTop + 2), 0.0F, 1.0F, 1.0F, 16.0F, 16.0F);
         }
      }
   }

   public void changeType(CreatureList.Type listType) {
      this.listType = listType;
      this.refreshList();
   }

   public static enum Type {
      KNOWLEDGE((byte)0),
      SUMMONABLE((byte)1),
      PET((byte)2),
      MOUNT((byte)3),
      FAMILIAR((byte)4);

      public byte id;

      private Type(byte i) {
         this.id = i;
      }
   }
}
