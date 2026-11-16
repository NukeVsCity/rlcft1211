package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.container.BaseContainer;
import com.lycanitesmobs.core.container.CreatureContainer;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.inventory.InventoryCreature;
import com.lycanitesmobs.core.network.MessageEntityGUICommand;
import java.util.List;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;

public class CreatureInventoryScreen extends BaseContainerScreen {
   public BaseCreatureEntity creature;
   public InventoryCreature creatureInventory;
   public InventoryPlayer playerInventory;

   public CreatureInventoryScreen(BaseCreatureEntity creature, InventoryPlayer playerInventory) {
      super(new CreatureContainer(creature, playerInventory));
      this.creature = creature;
      this.creatureInventory = creature.inventory;
      this.playerInventory = playerInventory;
   }

   public void initGui() {
      super.initGui();
      this.xSize = 176;
      this.ySize = 166;
      int backX = (this.width - this.xSize) / 2;
      int backY = (this.height - this.ySize) / 2;
      this.drawControls(backX, backY);
   }

   protected void drawGuiContainerForegroundLayer(int i, int j) {
      if (this.creature == null || !this.creature.isEntityAlive()) {
         this.mc.player.closeScreen();
      }

      this.fontRenderer.drawString(this.creatureInventory.getName(), 8, 6, 4210752);
      this.fontRenderer.drawString(LanguageManager.translate(this.playerInventory.getName()), 8, this.ySize - 96 + 2, 4210752);
      this.drawBars(0, 0);
   }

   protected void drawBars(int backX, int backY) {
      int barWidth = 100;
      int barHeight = 11;
      int barX = backX - barWidth;
      int barY = backY + 54 + 18;
      int barCenter = barX + barWidth / 2;
      this.drawTexture(AssetManager.getTexture("GUIPetBarEmpty"), (float)barX, (float)barY, 0.0F, 1.0F, 1.0F, (float)barWidth, (float)barHeight);
      float healthNormal = Math.min(1.0F, this.creature.getHealth() / this.creature.getMaxHealth());
      this.drawTexture(
         AssetManager.getTexture("GUIPetBarHealth"), (float)barX, (float)barY, 0.0F, healthNormal, 1.0F, (float)barWidth * healthNormal, (float)barHeight
      );
      String healthText = LanguageManager.translate("entity.health")
         + ": "
         + String.format("%.0f", this.creature.getHealth())
         + "/"
         + String.format("%.0f", this.creature.getMaxHealth());
      this.fontRenderer.drawString(healthText, barCenter - this.fontRenderer.getStringWidth(healthText) / 2, barY + 2, 16777215);
      barY += barHeight + 1;
      this.drawTexture(AssetManager.getTexture("GUIPetBarEmpty"), (float)barX, (float)barY, 0.0F, 1.0F, 1.0F, (float)barWidth, (float)barHeight);
      float experienceNormal = Math.min(1.0F, (float)this.creature.getExperience() / (float)this.creature.creatureStats.getExperienceForNextLevel());
      this.drawTexture(
         AssetManager.getTexture("GUIBarExperience"),
         (float)barX,
         (float)barY,
         0.0F,
         experienceNormal,
         1.0F,
         (float)barWidth * experienceNormal,
         (float)barHeight
      );
      String experienceText = LanguageManager.translate("entity.experience")
         + ": "
         + this.creature.getExperience()
         + "/"
         + this.creature.creatureStats.getExperienceForNextLevel();
      this.fontRenderer.drawString(experienceText, barCenter - this.fontRenderer.getStringWidth(experienceText) / 2, barY + 2, 16777215);
   }

   protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(AssetManager.getTexture("GUIInventoryCreature"));
      this.xSize = 176;
      this.ySize = 166;
      int backX = (this.width - this.xSize) / 2;
      int backY = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(backX, backY, 0, 0, this.xSize, this.ySize);
      this.drawFrames(backX, backY, i, j);
      this.drawSlots(backX, backY);
   }

   protected void drawFrames(int backX, int backY, int mouseX, int mouseY) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(AssetManager.getTexture("GUIInventoryCreature"));
      int statusWidth = 90;
      int statusHeight = 54;
      this.drawTexturedModalRect(backX + 79, backY + 17, 0, 256 - statusHeight, statusWidth, statusHeight);
      int creatureWidth = 54;
      int creatureHeight = 54;
      this.drawTexturedModalRect(backX - creatureWidth + 1, backY + 17, statusWidth, 256 - creatureHeight, creatureWidth, creatureHeight);
      GuiInventory.drawEntityOnScreen(backX + 26 - creatureWidth + 1, backY + 60, 17, (float)backX - (float)mouseX, (float)backY - (float)mouseY, this.creature);
   }

   protected void drawSlots(int backX, int backY) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(AssetManager.getTexture("GUIInventoryCreature"));
      BaseContainer container = (BaseContainer)this.inventorySlots;
      List<Slot> creatureSlots = container.inventorySlots.subList(container.specialStart, container.inventoryFinish + 1);
      int slotWidth = 18;
      int slotHeight = 18;
      int slotU = 238;
      int slotVBase = 0;

      for (Slot creatureSlot : creatureSlots) {
         int slotX = backX + creatureSlot.xPos - 1;
         int slotY = backY + creatureSlot.yPos - 1;
         int slotV = slotVBase;
         String slotType = this.creatureInventory.getTypeFromSlot(creatureSlot.getSlotIndex());
         if (slotType != null) {
            if (slotType.equals("saddle")) {
               slotV = slotVBase + slotHeight;
            } else if (slotType.equals("bag")) {
               slotV = slotVBase + slotHeight * 2;
            } else if (slotType.equals("chest")) {
               slotV = slotVBase + slotHeight * 3;
            }
         }

         this.drawTexturedModalRect(slotX, slotY, slotU, slotV, slotWidth, slotHeight);
      }
   }

   protected void drawControls(int backX, int backY) {
      if (this.creature instanceof TameableCreatureEntity) {
         TameableCreatureEntity pet = (TameableCreatureEntity)this.creature;
         if (pet.petControlsEnabled()) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            int buttonSpacing = 2;
            int buttonWidth = 128;
            int buttonHeight = 20;
            int buttonX = backX + this.xSize;
            String buttonText = LanguageManager.translate("gui.pet.follow");
            int buttonY = backY + buttonSpacing;
            ButtonBase button = new ButtonBase(
               BaseCreatureEntity.PET_COMMAND_ID.FOLLOW.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, buttonText
            );
            if (pet.isFollowing() && !pet.isSitting()) {
               button.enabled = false;
            }

            this.addButton(button);
            buttonText = LanguageManager.translate("gui.pet.wander");
            buttonY += buttonHeight + buttonSpacing * 2;
            button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.WANDER.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, buttonText);
            if (!pet.isFollowing() && !pet.isSitting()) {
               button.enabled = false;
            }

            this.addButton(button);
            buttonText = LanguageManager.translate("gui.pet.sit");
            buttonY += buttonHeight + buttonSpacing * 2;
            button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.SIT.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, buttonText);
            if (!pet.isFollowing() && pet.isSitting()) {
               button.enabled = false;
            }

            this.addButton(button);
            buttonText = LanguageManager.translate("gui.pet.passive");
            buttonY += buttonHeight + buttonSpacing * 2;
            button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.PASSIVE.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, buttonText);
            if (pet.isPassive()) {
               button.enabled = false;
            }

            this.addButton(button);
            buttonText = LanguageManager.translate("gui.pet.defensive");
            buttonY += buttonHeight + buttonSpacing * 2;
            button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.DEFENSIVE.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, buttonText);
            if (!pet.isPassive() && !pet.isAssisting() && !pet.isAggressive()) {
               button.enabled = false;
            }

            this.addButton(button);
            buttonText = LanguageManager.translate("gui.pet.assist");
            buttonY += buttonHeight + buttonSpacing * 2;
            button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.ASSIST.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, buttonText);
            if (!pet.isPassive() && pet.isAssisting() && !pet.isAggressive()) {
               button.enabled = false;
            }

            this.addButton(button);
            buttonText = LanguageManager.translate("gui.pet.aggressive");
            buttonY += buttonHeight + buttonSpacing * 2;
            button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.AGGRESSIVE.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, buttonText);
            if (!pet.isPassive() && pet.isAggressive()) {
               button.enabled = false;
            }

            this.addButton(button);
            buttonText = LanguageManager.translate("gui.pet.pvp")
               + ": "
               + (pet.isPVP() ? LanguageManager.translate("common.yes") : LanguageManager.translate("common.no"));
            buttonY += buttonHeight + buttonSpacing * 2;
            this.buttonList
               .add(new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.PVP.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, buttonText));
         }
      }
   }

   public void actionPerformed(ButtonBase guiButton) {
      if (guiButton != null) {
         MessageEntityGUICommand message = new MessageEntityGUICommand((byte)guiButton.id, this.creature);
         LycanitesMobs.packetHandler.sendToServer(message);
      }

      super.actionPerformed(guiButton);
   }
}
