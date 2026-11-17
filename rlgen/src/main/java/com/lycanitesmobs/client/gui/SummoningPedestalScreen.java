package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.client.gui.buttons.MainTab;
import com.lycanitesmobs.client.gui.widgets.SummoningPedestalList;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.container.SummoningPedestalContainer;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.pets.SummonSet;
import com.lycanitesmobs.core.tileentity.TileEntitySummoningPedestal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import com.lycanitesmobs.client.gui.GuiScrollingList;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;

public class SummoningPedestalScreen extends BaseContainerScreen<SummoningPedestalContainer> {
   public Player player;
   public ExtendedPlayer playerExt;
   public TileEntitySummoningPedestal summoningPedestal;
   public SummonSet summonSet;
   public GuiScrollingList list;
   public int centerX;
   public int centerY;
   public int windowWidth;
   public int windowHeight;
   public int halfX;
   public int halfY;
   public int windowX;
   public int windowY;
   public static int TAB_BUTTON_ID = 55555;
   public int editSet;

   public SummoningPedestalScreen(SummoningPedestalContainer container, net.minecraft.world.entity.player.Inventory inventory) {
      super(container, inventory);
      Player player = inventory.player;
      TileEntitySummoningPedestal summoningPedestal = container.summoningPedestal;
      this.summoningPedestal = summoningPedestal;
      this.player = player;
      this.playerExt = ExtendedPlayer.getForPlayer(player);
      this.summonSet = this.summoningPedestal.summonSet;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   protected void init() {
      super.init();
      this.clearWidgets();
      this.windowWidth = 256;
      this.windowHeight = 166;
      this.halfX = this.windowWidth / 2;
      this.halfY = this.windowHeight / 2;
      this.windowX = this.width / 2 - this.windowWidth / 2;
      this.windowY = this.height / 2 - this.windowHeight / 2;
      this.centerX = this.windowX + this.windowWidth / 2;
      this.centerY = this.windowY + this.windowHeight / 2;
      this.initControls();
      this.initList();
   }

   public void initList() {
      if (this.hasPets() && this.summoningPedestal.summonSet != null) {
         this.selectMinion(this.summoningPedestal.summonSet.summonType);
      }

      int buttonSpacing = 2;
      int listWidth = this.windowWidth / 2 - buttonSpacing * 4;
      int listHeight = this.windowHeight - (39 + buttonSpacing) - 16;
      int listTop = this.windowY + 39 + buttonSpacing;
      int listBottom = listTop + listHeight;
      int listX = this.windowX + buttonSpacing * 2;
      this.list = new SummoningPedestalList(this, this.playerExt, listWidth, listHeight, listTop, listBottom, listX);
      // 1.12 registerScrollButtons(buttonList, 51, 52): Forge's scroll buttons did nothing in GuiScrollingList
   }

   protected void initControls() {
      
      int buttonSpacing = 2;
      int buttonWidth = this.windowWidth / 4 - buttonSpacing * 2;
      int buttonHeight = 20;
      int buttonX = this.windowX + 6;
      int buttonY = this.windowY;
      this.addButton(new MainTab(TAB_BUTTON_ID, buttonX, buttonY - 24));
      buttonX = this.centerX + buttonSpacing;
      int buttonXRight = buttonX + buttonWidth + buttonSpacing;
      buttonY = this.windowY + 39 + buttonSpacing;
      buttonY += buttonHeight + buttonSpacing * 2;
      this.addButton(new ButtonBase(BaseCreatureEntity.GUI_COMMAND.SITTING.id, buttonX, buttonY, buttonWidth * 2, buttonHeight, "..."));
      buttonY += buttonHeight + buttonSpacing * 2;
      this.addButton(new ButtonBase(BaseCreatureEntity.GUI_COMMAND.PASSIVE.id, buttonX, buttonY, buttonWidth, buttonHeight, "..."));
      this.addButton(new ButtonBase(BaseCreatureEntity.GUI_COMMAND.STANCE.id, buttonXRight, buttonY, buttonWidth, buttonHeight, "..."));
      buttonY += buttonHeight + buttonSpacing * 2;
      this.addButton(new ButtonBase(BaseCreatureEntity.GUI_COMMAND.PVP.id, buttonX, buttonY, buttonWidth * 2, buttonHeight, "..."));
   }

   @Override
   public void drawScreen(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(graphics, mouseX, mouseY, partialTicks);
      this.updateControls();
      this.drawGuiContainerForegroundLayer(partialTicks, mouseX, mouseY);
      if (this.hasPets()) {
         this.list.drawScreen(mouseX, mouseY, partialTicks);
      }
   }

   protected void drawGuiContainerForegroundLayer(float partialTicks, int mouseX, int mouseY) {
      this.bindTexture(this.getTexture());
      if (!this.hasPets()) {
         this.drawString(LanguageManager.translate("gui.beastiary.summoning.empty.title"), (float)(this.centerX - 96), (float)(this.windowY + 6), 16777215, true);
         this.drawHelper.drawSplitString(
            LanguageManager.translate("gui.beastiary.summoning.empty.info"), this.windowX + 16, this.windowY + 30, this.windowWidth - 32, 16777215
         );
      } else {
         this.drawString(this.getTitleText(), this.centerX - 48, this.windowY + 6, 16777215);
         this.drawString(this.getEnergyTitle(), this.windowX + 16, this.windowY + 20, 16777215);
      }
   }

   public String getTitleText() {
      return LanguageManager.translate("gui.summoningpedestal.name");
   }

   public String getEnergyTitle() {
      return LanguageManager.translate("stat.portal.name");
   }

   protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      this.bindTexture(this.getTexture());
      this.drawTexturedModalRect(this.windowX, this.windowY, 0, 0, this.windowWidth, this.windowHeight);
      this.drawTexturedModalRect(this.windowX + 40, this.windowY + this.windowHeight, 40, 224, this.windowWidth - 80, 29);
      if (this.hasPets()) {
         this.drawFuel();
         this.drawCapacityBar();
         this.drawProgressBar();
      }
   }

   public void drawFuel() {
      int fuelX = this.windowX + 132;
      int fuelY = this.windowY + 42;
      this.drawTexturedModalRect(fuelX, fuelY, 47, 170, 18, 18);
      int barWidth = 38;
      int barHeight = 11;
      int barX = fuelX + 22;
      int barY = fuelY + 3;
      int barU = 218;
      int barV = 225;
      this.drawTexturedModalRect(barX, barY, barU, barV + barHeight, barWidth, barHeight);
      barWidth = Math.round((float)barWidth * ((float)this.summoningPedestal.summoningFuel / (float)this.summoningPedestal.summoningFuelMax));
      this.drawTexturedModalRect(barX, barY, barU, barV, barWidth, barHeight);
   }

   public void drawCapacityBar() {
      int energyBarWidth = 9;
      int energyBarHeight = 9;
      int energyBarX = this.windowX + 16;
      int energyBarY = this.windowY + 40 - energyBarHeight;
      int energyBarU = 256 - energyBarWidth;
      int energyBarV = 256 - energyBarHeight;

      for (int energyBarEnergyN = 1; energyBarEnergyN <= 10; energyBarEnergyN++) {
         this.drawTexturedModalRect(energyBarX - energyBarWidth + energyBarWidth * energyBarEnergyN, energyBarY, energyBarU, energyBarV, energyBarWidth, energyBarHeight);
         if (this.summoningPedestal.capacity >= energyBarEnergyN * this.summoningPedestal.capacityCharge) {
            this.drawTexturedModalRect(
               energyBarX - energyBarWidth + energyBarWidth * energyBarEnergyN,
               energyBarY,
               energyBarU - energyBarWidth * 2,
               energyBarV,
               energyBarWidth,
               energyBarHeight
            );
         } else if (this.summoningPedestal.capacity + this.summoningPedestal.capacityCharge > energyBarEnergyN * this.summoningPedestal.capacityCharge) {
            float spiritChargeScale = (float)(this.summoningPedestal.capacity % this.summoningPedestal.capacityCharge)
               / (float)this.summoningPedestal.capacityCharge;
            this.drawTexturedModalRect(
               energyBarX - energyBarWidth + energyBarWidth * energyBarEnergyN,
               energyBarY,
               energyBarU - energyBarWidth * 2,
               energyBarV,
               Math.round((float)energyBarWidth * spiritChargeScale),
               energyBarHeight
            );
         }
      }
   }

   public void drawProgressBar() {
      
      this.bindTexture(AssetManager.getTexture("GUIInventoryCreature"));
      int barWidth = 80;
      int barHeight = 11;
      int barX = this.centerX + 2;
      int barY = this.windowY + 26;
      int barU = 144;
      int barV = 256 - barHeight * 2;
      this.drawTexturedModalRect(barX, barY, barU, barV, barWidth, barHeight);
      barWidth = Math.round((float)barWidth * ((float)this.summoningPedestal.summonProgress / (float)this.summoningPedestal.summonProgressMax));
      barV += barHeight;
      this.drawTexturedModalRect(barX, barY, barU, barV, barWidth, barHeight);
   }

   public void updateControls() {
      for (Object buttonObj : this.children()) {
         if (buttonObj instanceof ButtonBase) {
            ButtonBase button = (ButtonBase)buttonObj;
            if (button instanceof MainTab) {
               button.active = true;
               button.visible = true;
            } else if (!this.hasSelectedPet()) {
               button.active = false;
               button.visible = false;
            } else {
               this.updateButtons(button);
            }
         }
      }
   }

   public void updateButtons(ButtonBase button) {
      if (button.id == BaseCreatureEntity.GUI_COMMAND.SITTING.id) {
         button.setDisplayString(LanguageManager.translate("gui.pet.sit")
            + ": "
            + (this.summonSet.getSitting() ? LanguageManager.translate("common.yes") : LanguageManager.translate("common.no")));
      }

      if (button.id == BaseCreatureEntity.GUI_COMMAND.PASSIVE.id) {
         button.setDisplayString(LanguageManager.translate("gui.pet.passive")
            + ": "
            + (this.summonSet.getPassive() ? LanguageManager.translate("common.yes") : LanguageManager.translate("common.no")));
      }

      if (button.id == BaseCreatureEntity.GUI_COMMAND.STANCE.id) {
         button.setDisplayString(this.summonSet.getAggressive()
            ? LanguageManager.translate("gui.pet.aggressive")
            : LanguageManager.translate("gui.pet.defensive"));
      }

      if (button.id == BaseCreatureEntity.GUI_COMMAND.PVP.id) {
         button.setDisplayString(LanguageManager.translate("gui.pet.pvp")
            + ": "
            + (this.summonSet.getPVP() ? LanguageManager.translate("common.yes") : LanguageManager.translate("common.no")));
      }
   }

   public void sendCommandsToServer() {
      this.summoningPedestal.sendSummonSetToServer(this.summonSet);
   }

   public void actionPerformed(ButtonBase guiButton) {
      if (!this.hasSelectedPet()) {
         super.actionPerformed(guiButton);
      } else {
         if (guiButton.id == BaseCreatureEntity.GUI_COMMAND.SITTING.id) {
            this.summonSet.sitting = !this.summonSet.sitting;
         }

         if (guiButton.id == BaseCreatureEntity.GUI_COMMAND.FOLLOWING.id) {
            this.summonSet.following = !this.summonSet.following;
         }

         if (guiButton.id == BaseCreatureEntity.GUI_COMMAND.PASSIVE.id) {
            this.summonSet.passive = !this.summonSet.passive;
         }

         if (guiButton.id == BaseCreatureEntity.GUI_COMMAND.STANCE.id) {
            this.summonSet.aggressive = !this.summonSet.aggressive;
         }

         if (guiButton.id == BaseCreatureEntity.GUI_COMMAND.PVP.id) {
            this.summonSet.pvp = !this.summonSet.pvp;
         }

         if (guiButton.id < 100) {
            this.sendCommandsToServer();
         }

         super.actionPerformed(guiButton);
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE || this.mc.options.keyInventory.matches(keyCode, scanCode)) {
         this.onClose();
         return true;
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   public void selectMinion(String minionName) {
      if (this.summonSet == null) {
         if (this.summoningPedestal.summonSet == null) {
            this.summoningPedestal.summonSet = new SummonSet(this.playerExt);
            this.summoningPedestal.sendSummonSetToServer(this.summoningPedestal.summonSet);
         }

         this.summonSet = this.summoningPedestal.summonSet;
      }

      this.summonSet.setSummonType(minionName);
      this.sendCommandsToServer();
   }

   public String getSelectedMinion() {
      return this.summonSet == null ? null : this.summonSet.summonType;
   }

   public boolean hasPets() {
      return this.playerExt.getBeastiary().getSummonableList().size() > 0;
   }

   public boolean hasSelectedPet() {
      return this.hasPets() && this.summonSet != null && !this.summonSet.summonType.equals("");
   }

   protected ResourceLocation getTexture() {
      return AssetManager.getTexture("GUIMinionLg");
   }
}
