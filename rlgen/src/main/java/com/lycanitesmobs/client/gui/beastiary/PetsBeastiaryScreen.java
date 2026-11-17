package com.lycanitesmobs.client.gui.beastiary;

import net.minecraft.client.Minecraft;
import com.lycanitesmobs.GuiHandler;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.client.gui.beastiary.lists.CreatureFilterList;
import com.lycanitesmobs.client.gui.beastiary.lists.CreatureList;
import com.lycanitesmobs.client.gui.beastiary.lists.PetTypeList;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.pets.PetEntry;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

public class PetsBeastiaryScreen extends BeastiaryScreen {
   public CreatureFilterList petTypeList;
   public CreatureList petList;
   private int petCommandIdStart = 200;
   private int releaseConfirmId = 300;
   private int releaseCancelId = 301;

   public static void openToPlayer(Player player) {
      if (player != null) {
         Minecraft.getInstance().setScreen(new PetsBeastiaryScreen(player));
      }
   }

   public PetsBeastiaryScreen(Player player) {
      super(player);
      this.playerExt.selectedSubspecies = 0;
      this.playerExt.selectedVariant = 0;
   }

   @Override
   public String getTitleText() {
      if (this.playerExt.selectedPet != null) {
         String title = this.playerExt.selectedPet.getDisplayName();
         title = title + " " + LanguageManager.translate("creature.stat.level") + " " + this.playerExt.selectedPet.getLevel();
         if (this.playerExt.selectedPet.releaseEntity) {
            title = LanguageManager.translate("gui.pet.release") + " " + title;
         }

         return title;
      } else {
         return this.playerExt.petManager.entries.isEmpty()
            ? LanguageManager.translate("gui.beastiary.pets.empty.title")
            : LanguageManager.translate("gui.beastiary.pets");
      }
   }

   @Override
   public void initControls() {
      super.initControls();
      int petTypeListHeight = Math.round((float)this.colLeftHeight * 0.225F);
      int petTypeListY = this.colLeftY;
      this.petTypeList = new PetTypeList(this, this.colLeftWidth, petTypeListHeight, petTypeListY, petTypeListY + petTypeListHeight, this.colLeftX);
      int petListHeight = Math.round((float)this.colLeftHeight * 0.7F);
      int petListY = petTypeListY + petTypeListHeight + Math.round((float)this.colLeftHeight * 0.025F);
      this.petList = new CreatureList(
         CreatureList.Type.PET, this, this.petTypeList, this.colLeftWidth, petListHeight, petListY, petListY + petListHeight, this.colLeftX
      );
      int buttonWidth = 80;
      int buttonHeight = 20;
      int buttonSpacing = 2;
      int buttonMarginX = 10
         + Math.max(
            Math.max(
               this.getFontRenderer().width(LanguageManager.translate("gui.pet.actions")),
               this.getFontRenderer().width(LanguageManager.translate("gui.pet.stance"))
            ),
            this.getFontRenderer().width(LanguageManager.translate("gui.pet.movement"))
         );
      int buttonX = this.colRightX + buttonMarginX;
      int buttonY = this.colRightY + this.colRightHeight - (buttonHeight + buttonSpacing) * 3;
      ButtonBase button = new ButtonBase(
         BaseCreatureEntity.PET_COMMAND_ID.ACTIVE.id + this.petCommandIdStart,
         buttonX,
         buttonY,
         buttonWidth,
         buttonHeight,
         LanguageManager.translate("gui.pet.active")
      );
      this.addButton(button);
      buttonX += buttonWidth + buttonSpacing;
      button = new ButtonBase(
         BaseCreatureEntity.PET_COMMAND_ID.TELEPORT.id + this.petCommandIdStart,
         buttonX,
         buttonY,
         buttonWidth,
         buttonHeight,
         LanguageManager.translate("gui.pet.teleport")
      );
      this.addButton(button);
      buttonX += buttonWidth + buttonSpacing;
      button = new ButtonBase(
         BaseCreatureEntity.PET_COMMAND_ID.PVP.id + this.petCommandIdStart,
         buttonX,
         buttonY,
         buttonWidth,
         buttonHeight,
         LanguageManager.translate("gui.pet.pvp")
      );
      this.addButton(button);
      buttonX += buttonWidth + buttonSpacing;
      button = new ButtonBase(
         BaseCreatureEntity.PET_COMMAND_ID.RELEASE.id + this.petCommandIdStart,
         buttonX,
         buttonY,
         buttonWidth,
         buttonHeight,
         LanguageManager.translate("gui.pet.release")
      );
      this.addButton(button);
      buttonX = this.colRightX + buttonMarginX;
      buttonY += buttonHeight + 2;
      button = new ButtonBase(
         BaseCreatureEntity.PET_COMMAND_ID.PASSIVE.id + this.petCommandIdStart,
         buttonX,
         buttonY,
         buttonWidth,
         buttonHeight,
         LanguageManager.translate("gui.pet.passive")
      );
      this.addButton(button);
      buttonX += buttonWidth + buttonSpacing;
      button = new ButtonBase(
         BaseCreatureEntity.PET_COMMAND_ID.DEFENSIVE.id + this.petCommandIdStart,
         buttonX,
         buttonY,
         buttonWidth,
         buttonHeight,
         LanguageManager.translate("gui.pet.defensive")
      );
      this.addButton(button);
      buttonX += buttonWidth + buttonSpacing;
      button = new ButtonBase(
         BaseCreatureEntity.PET_COMMAND_ID.ASSIST.id + this.petCommandIdStart,
         buttonX,
         buttonY,
         buttonWidth,
         buttonHeight,
         LanguageManager.translate("gui.pet.assist")
      );
      this.addButton(button);
      buttonX += buttonWidth + buttonSpacing;
      button = new ButtonBase(
         BaseCreatureEntity.PET_COMMAND_ID.AGGRESSIVE.id + this.petCommandIdStart,
         buttonX,
         buttonY,
         buttonWidth,
         buttonHeight,
         LanguageManager.translate("gui.pet.aggressive")
      );
      this.addButton(button);
      buttonX = this.colRightX + buttonMarginX;
      buttonY += buttonHeight + 2;
      button = new ButtonBase(
         BaseCreatureEntity.PET_COMMAND_ID.FOLLOW.id + this.petCommandIdStart,
         buttonX,
         buttonY,
         buttonWidth,
         buttonHeight,
         LanguageManager.translate("gui.pet.follow")
      );
      this.addButton(button);
      buttonX += buttonWidth + buttonSpacing;
      button = new ButtonBase(
         BaseCreatureEntity.PET_COMMAND_ID.WANDER.id + this.petCommandIdStart,
         buttonX,
         buttonY,
         buttonWidth,
         buttonHeight,
         LanguageManager.translate("gui.pet.wander")
      );
      this.addButton(button);
      buttonX += buttonWidth + buttonSpacing;
      button = new ButtonBase(
         BaseCreatureEntity.PET_COMMAND_ID.SIT.id + this.petCommandIdStart,
         buttonX,
         buttonY,
         buttonWidth,
         buttonHeight,
         LanguageManager.translate("gui.pet.sit")
      );
      this.addButton(button);
      buttonX = this.colRightX + Math.round((float)this.colRightWidth / 2.0F) - (buttonWidth + buttonSpacing);
      buttonY = this.colRightY + Math.round((float)this.colRightHeight / 2.0F) - Math.round((float)buttonHeight / 2.0F);
      button = new ButtonBase(this.releaseConfirmId, buttonX, buttonY, buttonWidth, buttonHeight, LanguageManager.translate("common.yes"));
      this.addButton(button);
      buttonX += buttonSpacing;
      button = new ButtonBase(this.releaseCancelId, buttonX + buttonWidth, buttonY, buttonWidth, buttonHeight, LanguageManager.translate("common.no"));
      this.addButton(button);
   }

   @Override
   public void drawBackground(int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(mouseX, mouseY, partialTicks);
   }

   @Override
   protected void updateControls(int mouseX, int mouseY, float partialTicks) {
      super.updateControls(mouseX, mouseY, partialTicks);
      boolean empty = this.playerExt.petManager.entries.isEmpty();
      if (!empty) {
         this.petTypeList.drawScreen(mouseX, mouseY, partialTicks);
         this.petList.drawScreen(mouseX, mouseY, partialTicks);
      }

      for (ButtonBase button : this.buttons()) {
         if (button.id >= this.petCommandIdStart && button.id < this.releaseConfirmId) {
            if (this.playerExt.selectedPet != null && !this.playerExt.selectedPet.releaseEntity) {
               button.visible = !empty;
               if (button.id == BaseCreatureEntity.PET_COMMAND_ID.ACTIVE.id + this.petCommandIdStart) {
                  if (!this.playerExt.selectedPet.spawningActive) {
                     button.setDisplayString(LanguageManager.translate("gui.pet.summon"));
                  } else {
                     button.setDisplayString(LanguageManager.translate("gui.pet.dismiss"));
                  }
               } else if (button.id == BaseCreatureEntity.PET_COMMAND_ID.PVP.id + this.petCommandIdStart) {
                  if (this.playerExt.selectedPet.summonSet.getPVP()) {
                     button.setDisplayString(LanguageManager.translate("gui.pet.pvp") + ": " + LanguageManager.translate("common.yes"));
                  } else {
                     button.setDisplayString(LanguageManager.translate("gui.pet.pvp") + ": " + LanguageManager.translate("common.no"));
                  }
               } else if (button.id == BaseCreatureEntity.PET_COMMAND_ID.PASSIVE.id + this.petCommandIdStart) {
                  button.active = !this.playerExt.selectedPet.summonSet.passive;
               } else if (button.id == BaseCreatureEntity.PET_COMMAND_ID.DEFENSIVE.id + this.petCommandIdStart) {
                  button.active = this.playerExt.selectedPet.summonSet.getPassive()
                     || this.playerExt.selectedPet.summonSet.getAssist()
                     || this.playerExt.selectedPet.summonSet.getAggressive();
               } else if (button.id == BaseCreatureEntity.PET_COMMAND_ID.ASSIST.id + this.petCommandIdStart) {
                  button.active = this.playerExt.selectedPet.summonSet.getPassive()
                     || !this.playerExt.selectedPet.summonSet.getAssist()
                     || this.playerExt.selectedPet.summonSet.getAggressive();
               } else if (button.id == BaseCreatureEntity.PET_COMMAND_ID.AGGRESSIVE.id + this.petCommandIdStart) {
                  button.active = this.playerExt.selectedPet.summonSet.getPassive() || !this.playerExt.selectedPet.summonSet.getAggressive();
               } else if (button.id == BaseCreatureEntity.PET_COMMAND_ID.FOLLOW.id + this.petCommandIdStart) {
                  button.active = this.playerExt.selectedPet.summonSet.getSitting() || !this.playerExt.selectedPet.summonSet.getFollowing();
               } else if (button.id != BaseCreatureEntity.PET_COMMAND_ID.WANDER.id + this.petCommandIdStart) {
                  if (button.id == BaseCreatureEntity.PET_COMMAND_ID.SIT.id + this.petCommandIdStart) {
                     button.active = !this.playerExt.selectedPet.summonSet.getSitting();
                  }
               } else {
                  button.active = this.playerExt.selectedPet.summonSet.getSitting() || this.playerExt.selectedPet.summonSet.getFollowing();
               }
            } else {
               button.visible = false;
            }
         } else if (button.id == this.releaseConfirmId || button.id == this.releaseCancelId) {
            button.visible = this.playerExt.selectedPet != null && this.playerExt.selectedPet.releaseEntity && !empty;
         }
      }
   }

   @Override
   public void drawForeground(int mouseX, int mouseY, float partialTicks) {
      super.drawForeground(mouseX, mouseY, partialTicks);
      int marginX = 0;
      int nextX = this.colRightX + marginX;
      int nextY = this.colRightY + 20;
      int width = this.colRightWidth - marginX;
      if (this.playerExt.petManager.entries.isEmpty()) {
         String text = LanguageManager.translate("gui.beastiary.pets.empty.info");
         this.drawSplitString(text, nextX, nextY, width, 16777215, true);
      } else {
         if (this.playerExt.selectedPet != null) {
            this.renderCreature(
               this.playerExt.selectedPet.getCreatureInfo(),
               this.colRightX + marginX / 2 + this.colRightWidth / 2,
               this.colRightY + Math.round((float)this.colRightHeight / 2.0F),
               mouseX,
               mouseY,
               partialTicks
            );
         }

         String text = "§l" + LanguageManager.translate("gui.beastiary.player.spirit") + ": ";
         this.drawString(text, (float)nextX, (float)nextY, 16777215, true);
         int barX = nextX + this.getFontRenderer().width(text);
         int spiritMax = Math.round((float)this.playerExt.spiritMax / (float)this.playerExt.spiritCharge);
         int spiritReserved = (int)Math.floor((double)this.playerExt.spiritReserved / (double)this.playerExt.spiritCharge);
         int spiritAvailable = (int)Math.floor((double)this.playerExt.spirit / (double)this.playerExt.spiritCharge);
         float spiritFilling = (float)this.playerExt.spirit / (float)this.playerExt.spiritCharge - (float)spiritAvailable;
         this.drawBar(AssetManager.getTexture("GUIPetSpiritEmpty"), barX, nextY, 0.0F, 9.0F, 9.0F, spiritMax, 10);
         this.drawBar(AssetManager.getTexture("GUIPetSpirit"), barX, nextY, 0.0F, 9.0F, 9.0F, spiritAvailable, 10);
         if (spiritFilling > 0.0F) {
            this.drawTexture(
               AssetManager.getTexture("GUIPetSpiritFilling"),
               (float)(barX + 9 * spiritAvailable),
               (float)nextY,
               0.0F,
               spiritFilling,
               1.0F,
               spiritFilling * 9.0F,
               9.0F
            );
         }

         this.drawBar(AssetManager.getTexture("GUIPetSpiritUsed"), barX, nextY, 0.0F, 9.0F, 9.0F, spiritReserved, -10);
         if (this.playerExt.selectedPet != null) {
            nextY += 4 + this.getFontRenderer().wordWrapHeight(text, this.colRightWidth);
            text = "§l" + LanguageManager.translate("creature.stat.spirit") + ": ";
            this.drawString(text, (float)nextX, (float)nextY, 16777215, true);
            this.drawLevel(
               this.playerExt.selectedPet.getCreatureInfo(), AssetManager.getTexture("GUIPetLevel"), nextX + this.getFontRenderer().width(text), nextY
            );
            nextY += 4 + this.getFontRenderer().wordWrapHeight(text, this.colRightWidth);
            if (this.playerExt.selectedPet.respawnTime <= 0) {
               text = "§l" + LanguageManager.translate("creature.stat.health") + ": ";
            } else {
               text = "§l" + LanguageManager.translate("creature.stat.respawning") + ": ";
            }

            this.drawString(text, (float)nextX, (float)nextY, 16777215, true);
            int barY = nextY - 1;
            int barWidth = 80;
            int barHeight = 10;
            barX = nextX + this.getFontRenderer().width(text);
            int barCenter = barX + barWidth / 2;
            this.drawTexture(AssetManager.getTexture("GUIPetBarEmpty"), (float)barX, (float)barY, 0.0F, 1.0F, 1.0F, (float)barWidth, (float)barHeight);
            if (this.playerExt.selectedPet.respawnTime <= 0) {
               float healthNormal = Math.min(this.playerExt.selectedPet.getHealth() / this.playerExt.selectedPet.getMaxHealth(), 1.0F);
               this.drawTexture(
                  AssetManager.getTexture("GUIPetBarHealth"),
                  (float)barX,
                  (float)barY,
                  0.0F,
                  healthNormal,
                  1.0F,
                  (float)barWidth * healthNormal,
                  (float)barHeight
               );
               String healthText = String.format("%.0f", this.playerExt.selectedPet.getHealth())
                  + "/"
                  + String.format("%.0f", this.playerExt.selectedPet.getMaxHealth());
               this.drawString(healthText, barCenter - this.getFontRenderer().width(healthText) / 2, barY + 1, 16777215);
            } else {
               float respawnNormal = 1.0F - (float)this.playerExt.selectedPet.respawnTime / (float)this.playerExt.selectedPet.respawnTimeMax;
               this.drawTexture(
                  AssetManager.getTexture("GUIPetBarRespawn"),
                  (float)barX,
                  (float)barY,
                  0.0F,
                  respawnNormal,
                  1.0F,
                  (float)barWidth * respawnNormal,
                  (float)barHeight
               );
               this.drawString("" + this.playerExt.selectedPet.respawnTime / 20 + "s", barX + barWidth + 10, nextY, 16777215);
            }

            nextY += 4 + this.getFontRenderer().wordWrapHeight(text, this.colRightWidth);
            text = "§l" + LanguageManager.translate("creature.stat.experience") + ": ";
            this.drawString(text, (float)nextX, (float)nextY, 16777215, true);
            barY = nextY - 1;
            int var36 = 80;
            int var38 = 10;
            barX = nextX + this.getFontRenderer().width(text);
            barCenter = barX + var36 / 2;
            this.drawTexture(AssetManager.getTexture("GUIPetBarEmpty"), (float)barX, (float)barY, 0.0F, 1.0F, 1.0F, (float)var36, (float)var38);
            float experienceNormal = (float)this.playerExt.selectedPet.getExperience() / (float)this.playerExt.selectedPet.getMaxExperience();
            this.drawTexture(
               AssetManager.getTexture("GUIBarExperience"),
               (float)barX,
               (float)barY,
               0.0F,
               experienceNormal,
               1.0F,
               (float)var36 * experienceNormal,
               (float)var38
            );
            String experienceText = this.playerExt.selectedPet.getExperience() + "/" + this.playerExt.selectedPet.getMaxExperience();
            this.drawString(experienceText, barCenter - this.getFontRenderer().width(experienceText) / 2, barY + 1, 16777215);
         } else {
            nextY += 4 + this.getFontRenderer().wordWrapHeight(text, this.colRightWidth);
            text = LanguageManager.translate("gui.beastiary.pets.select");
            this.drawSplitString(text, nextX, nextY, width, 16777215, true);
         }

         int buttonHeight = 20;
         int buttonSpacing = 2;
         int buttonY = this.colRightY + this.colRightHeight - (buttonHeight + buttonSpacing) * 3;
         if (this.playerExt.selectedPet != null && !this.playerExt.selectedPet.releaseEntity) {
            this.drawString("§l" + LanguageManager.translate("gui.pet.actions"), (float)this.colRightX, (float)(buttonY + 6), 16777215, true);
            buttonY += buttonHeight + buttonSpacing;
            this.drawString("§l" + LanguageManager.translate("gui.pet.stance"), (float)this.colRightX, (float)(buttonY + 6), 16777215, true);
            buttonY += buttonHeight + buttonSpacing;
            this.drawString("§l" + LanguageManager.translate("gui.pet.movement"), (float)this.colRightX, (float)(buttonY + 6), 16777215, true);
         }

         if (this.playerExt.selectedPet != null && this.playerExt.selectedPet.releaseEntity) {
            text = LanguageManager.translate("gui.pet.release.confirm");
            nextX = this.colRightX;
            nextY = this.colRightY
               + Math.round((float)this.colRightHeight / 2.0F)
               - Math.round((float)buttonHeight / 2.0F)
               - this.getFontRenderer().wordWrapHeight(text, this.colRightWidth)
               - 2;
            this.drawSplitString(text, nextX, nextY, this.colRightWidth, 16777215, true);
         }
      }
   }

   @Override
   public void actionPerformed(ButtonBase button) {
      if (this.playerExt.selectedPet != null && this.playerExt.selectedPet.summonSet != null) {
         PetEntry petEntry = this.playerExt.selectedPet;
         if (button.id >= this.petCommandIdStart && button.id < this.releaseConfirmId) {
            int petCommandId = button.id - this.petCommandIdStart;
            if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.ACTIVE.id) {
               petEntry.spawningActive = !petEntry.spawningActive;
            } else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.TELEPORT.id) {
               petEntry.teleportEntity = true;
            } else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.PVP.id) {
               petEntry.summonSet.pvp = !petEntry.summonSet.pvp;
            } else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.RELEASE.id) {
               petEntry.releaseEntity = true;
            } else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.PASSIVE.id) {
               petEntry.summonSet.passive = true;
               petEntry.summonSet.assist = false;
               petEntry.summonSet.aggressive = false;
            } else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.DEFENSIVE.id) {
               petEntry.summonSet.passive = false;
               petEntry.summonSet.assist = false;
               petEntry.summonSet.aggressive = false;
            } else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.ASSIST.id) {
               petEntry.summonSet.passive = false;
               petEntry.summonSet.assist = true;
               petEntry.summonSet.aggressive = false;
            } else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.AGGRESSIVE.id) {
               petEntry.summonSet.passive = false;
               petEntry.summonSet.assist = true;
               petEntry.summonSet.aggressive = true;
            } else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.FOLLOW.id) {
               petEntry.summonSet.following = true;
               petEntry.summonSet.sitting = false;
            } else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.WANDER.id) {
               petEntry.summonSet.following = false;
               petEntry.summonSet.sitting = false;
            } else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.SIT.id) {
               petEntry.summonSet.following = false;
               petEntry.summonSet.sitting = true;
            }

            this.playerExt.sendPetEntryToServer(petEntry);
            if (this.playerExt.selectedPet == null) {
               openToPlayer(this.player);
            }

            return;
         }

         if (button.id == this.releaseCancelId) {
            petEntry.releaseEntity = false;
            return;
         }

         if (button.id == this.releaseConfirmId) {
            this.playerExt.selectedPet = null;
            this.playerExt.sendPetEntryRemoveRequest(petEntry);
            return;
         }
      }

      super.actionPerformed(button);
   }

   @Override
   public int getDisplaySubspecies(CreatureInfo creatureInfo) {
      return this.playerExt.selectedPet.subspeciesIndex;
   }

   @Override
   public int getDisplayVariant(CreatureInfo creatureInfo) {
      return this.playerExt.selectedPet.variantIndex;
   }

   @Override
   public void playCreatureSelectSound(CreatureInfo creatureInfo) {
      String soundSuffix = "";
      if (this.playerExt.selectedPet != null && creatureInfo.getSubspecies(this.playerExt.selectedPet.subspeciesIndex).name != null) {
         soundSuffix = soundSuffix + "." + creatureInfo.getSubspecies(this.playerExt.selectedPet.subspeciesIndex).name;
      }

      SoundEvent soundEvent = AssetManager.getSound(creatureInfo.getName() + soundSuffix + "_say");
      this.player
         .level()
         .playSound(
            this.player, this.player.getX(), this.player.getY(), this.player.getZ(), soundEvent, SoundSource.NEUTRAL, 1.0F, 1.0F
         );
   }
}
