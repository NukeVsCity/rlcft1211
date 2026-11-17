package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.container.BaseContainer;
import com.lycanitesmobs.core.container.EquipmentInfuserChargeSlot;
import com.lycanitesmobs.core.container.EquipmentInfuserContainer;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.network.MessageTileEntityButton;
import com.lycanitesmobs.core.tileentity.EquipmentInfuserTileEntity;
import java.util.List;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;

public class EquipmentInfuserScreen extends BaseContainerScreen<EquipmentInfuserContainer> {
   public EquipmentInfuserTileEntity equipmentInfuser;
   public Inventory playerInventory;

   public EquipmentInfuserScreen(EquipmentInfuserContainer container, Inventory playerInventory) {
      super(container, playerInventory);
      EquipmentInfuserTileEntity equipmentInfuser = container.equipmentInfuser;
      this.equipmentInfuser = equipmentInfuser;
      this.playerInventory = playerInventory;
   }

   @Override
   protected void init() {
      super.init();
      this.imageWidth = 176;
      this.imageHeight = 166;
   }

   protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      
      this.bindTexture(AssetManager.getTexture("GUIEquipmentForge"));
      this.imageWidth = 176;
      this.imageHeight = 166;
      int backX = (this.width - this.imageWidth) / 2;
      int backY = (this.height - this.imageHeight) / 2;
      this.drawTexturedModalRect(backX, backY, 0, 0, this.imageWidth, this.imageHeight);
      this.drawSlots(backX, backY);
   }

   protected void drawSlots(int backX, int backY) {
      
      this.bindTexture(AssetManager.getTexture("GUIEquipmentForge"));
      BaseContainer container = this.menu;
      List<Slot> forgeSlots = container.inventorySlots.subList(container.inventoryStart, container.inventoryFinish);
      int slotWidth = 18;
      int slotHeight = 18;
      int slotU = 238;
      int slotVBase = 0;

      for (Slot forgeSlot : forgeSlots) {
         int slotX = backX + forgeSlot.x - 1;
         int slotY = backY + forgeSlot.y - 1;
         int slotV = slotVBase;
         if (forgeSlot instanceof EquipmentInfuserChargeSlot) {
            slotV = slotVBase + slotHeight * 9;
         }

         this.drawTexturedModalRect(slotX, slotY, slotU, slotV, slotWidth, slotHeight);
      }
   }

   protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
      this.drawString(this.equipmentInfuser.getName(), 8, 6, 4210752);
      this.drawString(LanguageManager.translate(this.playerInventory.getName().getString()), 8, this.imageHeight - 96 + 2, 4210752);
      int backX = (this.width - this.imageWidth) / 2;
      int backY = (this.height - this.imageHeight) / 2;
      this.drawBars(0, 0);
   }

   protected void drawBars(int backX, int backY) {
      int barWidth = 100;
      int barHeight = 11;
      int barX = barWidth / 2 - 10;
      int barY = backY + 58;
      int barCenter = barX + barWidth / 2;
      this.drawTexture(AssetManager.getTexture("GUIPetBarEmpty"), (float)barX, (float)barY, 0.0F, 1.0F, 1.0F, (float)barWidth, (float)barHeight);
      ItemStack partStack = this.equipmentInfuser.getStackInSlot(1);
      if (partStack.getItem() instanceof ItemEquipmentPart) {
         ItemEquipmentPart partItem = (ItemEquipmentPart)partStack.getItem();
         if (partItem.getLevel(partStack) < partItem.levelMax) {
            int experience = partItem.getExperience(partStack);
            int experienceMax = partItem.getExperienceForNextLevel(partStack);
            float experienceNormal = (float)experience / (float)experienceMax;
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
            String experienceText = LanguageManager.translate("entity.experience") + ": " + experience + "/" + experienceMax;
            this.drawString(experienceText, barCenter - this.font.width(experienceText) / 2, barY + 2, 16777215);
         }
      }
   }

   public void actionPerformed(ButtonBase guiButton) {
      if (guiButton != null) {
         MessageTileEntityButton message = new MessageTileEntityButton((byte)guiButton.id, this.equipmentInfuser.getBlockPos());
         LycanitesMobs.packetHandler.sendToServer(message);
      }

      super.actionPerformed(guiButton);
   }
}
