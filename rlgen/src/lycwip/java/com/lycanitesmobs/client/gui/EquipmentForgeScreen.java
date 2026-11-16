package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.container.BaseContainer;
import com.lycanitesmobs.core.container.EquipmentForgeContainer;
import com.lycanitesmobs.core.container.EquipmentForgeSlot;
import com.lycanitesmobs.core.network.MessageTileEntityButton;
import com.lycanitesmobs.core.tileentity.TileEntityEquipmentForge;
import java.util.List;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;

public class EquipmentForgeScreen extends BaseContainerScreen {
   public InventoryPlayer playerInventory;
   public TileEntityEquipmentForge equipmentForge;
   public String currentMode = "empty";

   public EquipmentForgeScreen(TileEntityEquipmentForge equipmentForge, InventoryPlayer playerInventory) {
      super(new EquipmentForgeContainer(equipmentForge, playerInventory));
      this.playerInventory = playerInventory;
      this.equipmentForge = equipmentForge;
   }

   public void initGui() {
      super.initGui();
      this.xSize = 176;
      this.ySize = 166;
      int backX = (this.width - this.xSize) / 2;
      int backY = (this.height - this.ySize) / 2;
   }

   protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
      this.fontRenderer.drawString(this.equipmentForge.getName(), 8, 6, 4210752);
      this.fontRenderer.drawString(LanguageManager.translate(this.playerInventory.getName()), 8, this.ySize - 96 + 2, 4210752);
   }

   protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(AssetManager.getTexture("GUIEquipmentForge"));
      this.xSize = 176;
      this.ySize = 166;
      int backX = (this.width - this.xSize) / 2;
      int backY = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(backX, backY, 0, 0, this.xSize, this.ySize);
      this.drawSlots(backX, backY);
   }

   protected void drawSlots(int backX, int backY) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(AssetManager.getTexture("GUIEquipmentForge"));
      BaseContainer container = (BaseContainer)this.inventorySlots;
      List<Slot> forgeSlots = container.inventorySlots.subList(container.inventoryStart, container.inventoryFinish);
      int slotWidth = 18;
      int slotHeight = 18;
      int slotU = 238;
      int slotVBase = 0;

      for (Slot forgeSlot : forgeSlots) {
         int slotX = backX + forgeSlot.xPos - 1;
         int slotY = backY + forgeSlot.yPos - 1;
         int slotV = slotVBase;
         if (forgeSlot instanceof EquipmentForgeSlot) {
            EquipmentForgeSlot slotEquipment = (EquipmentForgeSlot)forgeSlot;
            if ("base".equals(slotEquipment.type)) {
               slotV = slotVBase + slotHeight;
            } else if ("head".equals(slotEquipment.type)) {
               slotV = slotVBase + slotHeight * 2;
            } else if ("blade".equals(slotEquipment.type)) {
               slotV = slotVBase + slotHeight * 3;
            } else if ("axe".equals(slotEquipment.type)) {
               slotV = slotVBase + slotHeight * 4;
            } else if ("pike".equals(slotEquipment.type)) {
               slotV = slotVBase + slotHeight * 5;
            } else if ("pommel".equals(slotEquipment.type)) {
               slotV = slotVBase + slotHeight * 6;
            } else if ("jewel".equals(slotEquipment.type)) {
               slotV = slotVBase + slotHeight * 7;
            } else if ("aura".equals(slotEquipment.type)) {
               slotV = slotVBase + slotHeight * 8;
            }
         }

         this.drawTexturedModalRect(slotX, slotY, slotU, slotV, slotWidth, slotHeight);
      }
   }

   protected void initControls(int backX, int backY) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      int buttonSpacing = 2;
      int buttonWidth = 128;
      int buttonHeight = 20;
      int buttonX = backX + this.xSize;
      String buttonText = "";
      if ("construct".equals(this.currentMode)) {
         buttonText = LanguageManager.translate("gui.equipmentforge.forge");
      } else if ("deconstruct".equals(this.currentMode)) {
         buttonText = LanguageManager.translate("gui.equipmentforge.deconstruct");
      }

      int buttonY = backY + buttonSpacing;
      this.addButton(new ButtonBase(1, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, buttonText));
   }

   public void actionPerformed(ButtonBase guiButton) {
      if (guiButton != null) {
         MessageTileEntityButton message = new MessageTileEntityButton((byte)guiButton.id, this.equipmentForge.getPos());
         LycanitesMobs.packetHandler.sendToServer(message);
      }

      super.actionPerformed(guiButton);
   }
}
