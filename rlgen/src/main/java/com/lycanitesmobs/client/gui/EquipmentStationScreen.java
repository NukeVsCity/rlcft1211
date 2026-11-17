package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.container.BaseContainer;
import com.lycanitesmobs.core.container.EquipmentStationContainer;
import com.lycanitesmobs.core.container.EquipmentStationRepairSlot;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.network.MessageTileEntityButton;
import com.lycanitesmobs.core.tileentity.EquipmentStationTileEntity;
import java.util.List;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;

public class EquipmentStationScreen extends BaseContainerScreen<EquipmentStationContainer> {
   public EquipmentStationTileEntity equipmentStation;
   public Inventory playerInventory;

   public EquipmentStationScreen(EquipmentStationContainer container, Inventory playerInventory) {
      super(container, playerInventory);
      EquipmentStationTileEntity tileEntity = container.equipmentStation;
      this.equipmentStation = tileEntity;
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
         if (forgeSlot instanceof EquipmentStationRepairSlot) {
            slotV = slotVBase + slotHeight * 9;
         }

         this.drawTexturedModalRect(slotX, slotY, slotU, slotV, slotWidth, slotHeight);
      }
   }

   protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
      this.drawString(this.equipmentStation.getName(), 8, 6, 4210752);
      this.drawString(LanguageManager.translate(this.playerInventory.getName().getString()), 8, this.imageHeight - 96 + 2, 4210752);
      int backX = (this.width - this.imageWidth) / 2;
      int backY = (this.height - this.imageHeight) / 2;
      this.drawBars(0, 0);
   }

   protected void drawBars(int backX, int backY) {
      int barWidth = 100;
      int barHeight = 11;
      int barX = barWidth / 2 - 10;
      int barY = backY + 48;
      int manaBarY = barY + 12;
      int barCenter = barX + barWidth / 2;
      this.drawTexture(AssetManager.getTexture("GUIPetBarEmpty"), (float)barX, (float)barY, 0.0F, 1.0F, 1.0F, (float)barWidth, (float)barHeight);
      this.drawTexture(AssetManager.getTexture("GUIPetBarEmpty"), (float)barX, (float)barY, 0.0F, 1.0F, 1.0F, (float)barWidth, (float)barHeight);
      this.drawTexture(AssetManager.getTexture("GUIPetBarEmpty"), (float)barX, (float)manaBarY, 0.0F, 1.0F, 1.0F, (float)barWidth, (float)barHeight);
      this.bindTexture(AssetManager.getTexture("GUIEquipmentForge"));
      this.drawTexturedModalRect(barX - 14, barY, 225, 158, 13, 10);
      this.drawTexturedModalRect(barX - 14, manaBarY, 225, 170, 13, 10);
      ItemStack partStack = this.equipmentStation.getStackInSlot(1);
      if (partStack.getItem() instanceof ItemEquipment) {
         ItemEquipment equipmentItem = (ItemEquipment)partStack.getItem();
         int sharpness = equipmentItem.getSharpness(partStack);
         int sharpnessMax = ItemEquipment.SHARPNESS_MAX;
         float sharpnessNormal = (float)sharpness / (float)sharpnessMax;
         this.drawTexture(AssetManager.getTexture("GUIPetBarEmpty"), (float)barX, (float)barY, 0.0F, 1.0F, 1.0F, (float)barWidth, (float)barHeight);
         this.drawTexture(
            AssetManager.getTexture("GUIPetBarHealth"),
            (float)barX,
            (float)barY,
            0.0F,
            sharpnessNormal,
            1.0F,
            (float)barWidth * sharpnessNormal,
            (float)barHeight
         );
         String sharpnessText = sharpness + "/" + sharpnessMax;
         this.drawString(sharpnessText, (float)(barCenter - this.font.width(sharpnessText) / 2), (float)(barY + 2), 16777215, true);
         int mana = equipmentItem.getMana(partStack);
         int manaMax = ItemEquipment.SHARPNESS_MAX;
         float manaNormal = (float)mana / (float)manaMax;
         this.drawTexture(AssetManager.getTexture("GUIPetBarEmpty"), (float)barX, (float)manaBarY, 0.0F, 1.0F, 1.0F, (float)barWidth, (float)barHeight);
         this.drawTexture(
            AssetManager.getTexture("GUIPetBarRespawn"), (float)barX, (float)manaBarY, 0.0F, manaNormal, 1.0F, (float)barWidth * manaNormal, (float)barHeight
         );
         String manaText = mana + "/" + manaMax;
         this.drawString(manaText, (float)(barCenter - this.font.width(manaText) / 2), (float)(manaBarY + 2), 16777215, true);
      }
   }

   public void actionPerformed(ButtonBase guiButton) {
      if (guiButton != null) {
         MessageTileEntityButton message = new MessageTileEntityButton((byte)guiButton.id, this.equipmentStation.getBlockPos());
         LycanitesMobs.packetHandler.sendToServer(message);
      }

      super.actionPerformed(guiButton);
   }
}
