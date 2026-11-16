package com.lycanitesmobs.client.gui.overlays;

import com.lycanitesmobs.GuiHandler;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.BaseScreen;
import com.lycanitesmobs.client.gui.buttons.CreatureButton;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.network.MessageSummonSetSelection;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import net.minecraft.world.entity.player.Player;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;

public class MinionSelectionOverlay extends BaseScreen {
   public Player player;
   public ExtendedPlayer playerExt;
   int centerX;
   int centerY;
   int windowWidth;
   int windowHeight;
   int windowX;
   int windowY;

   public static void openToPlayer(Player player) {
      if (player != null && player.getEntityWorld() != null) {
         player.openGui(LycanitesMobs.instance, GuiHandler.GuiType.PLAYER.id, player.getEntityWorld(), GuiHandler.PlayerGuiType.MINION_SELECTION.id, 0, 0);
      }
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   public MinionSelectionOverlay(Player player) {
      this.player = player;
      this.playerExt = ExtendedPlayer.getForPlayer(player);
   }

   public void initGui() {
      super.initGui();
      this.centerX = this.width / 2;
      this.centerY = this.height / 2;
      this.windowWidth = 256;
      this.windowHeight = 256;
      this.windowX = this.centerX;
      this.windowY = this.centerY;
      this.drawControls();
   }

   public void drawScreen(int x, int y, float f) {
      this.drawGuiContainerBackgroundLayer();
      this.updateControls();
      this.drawGuiContainerForegroundLayer();
      super.drawScreen(x, y, f);
   }

   protected void drawGuiContainerForegroundLayer() {
   }

   protected void drawGuiContainerBackgroundLayer() {
   }

   protected void drawControls() {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      int buttonSpacing = 2;
      int buttonWidth = 32;
      int buttonHeight = 32;
      int buttonX = this.centerX - Math.round((float)(buttonWidth / 2));
      int buttonY = this.centerY - Math.round((float)(buttonHeight / 2));
      int offset = 32;
      CreatureInfo creatureInfo = this.playerExt.getSummonSet(1).getCreatureInfo();
      ButtonBase button = new CreatureButton(1, buttonX, buttonY - Math.round((float)(offset * 2)), buttonWidth, buttonHeight, "1", creatureInfo);
      this.addButton(button);
      creatureInfo = this.playerExt.getSummonSet(2).getCreatureInfo();
      button = new CreatureButton(
         2, buttonX + Math.round((float)(offset * 2)), buttonY - Math.round((float)offset * 0.5F), buttonWidth, buttonHeight, "2", creatureInfo
      );
      this.addButton(button);
      creatureInfo = this.playerExt.getSummonSet(3).getCreatureInfo();
      button = new CreatureButton(
         3, buttonX + Math.round((float)(offset * 1)), buttonY + Math.round((float)offset * 1.75F), buttonWidth, buttonHeight, "3", creatureInfo
      );
      this.addButton(button);
      creatureInfo = this.playerExt.getSummonSet(4).getCreatureInfo();
      button = new CreatureButton(
         4, buttonX - Math.round((float)(offset * 1)), buttonY + Math.round((float)offset * 1.75F), buttonWidth, buttonHeight, "4", creatureInfo
      );
      this.addButton(button);
      creatureInfo = this.playerExt.getSummonSet(5).getCreatureInfo();
      button = new CreatureButton(
         5, buttonX - Math.round((float)(offset * 2)), buttonY - Math.round((float)offset * 0.5F), buttonWidth, buttonHeight, "5", creatureInfo
      );
      this.addButton(button);
   }

   public void updateControls() {
      for (Object buttonObj : this.buttonList) {
         if (buttonObj instanceof GuiButton) {
            ButtonBase button = (GuiButton)buttonObj;
            button.visible = this.playerExt.getSummonSet(button.id).isUseable();
            button.enabled = button.id != this.playerExt.selectedSummonSet;
         }
      }
   }

   public void actionPerformed(ButtonBase guiButton) {
      if (guiButton != null) {
         this.playerExt.setSelectedSummonSet(guiButton.id);
         MessageSummonSetSelection message = new MessageSummonSetSelection(this.playerExt);
         LycanitesMobs.packetHandler.sendToServer(message);
      }

      super.actionPerformed(guiButton);
   }

   protected void keyTyped(char par1, int par2) throws IOException {
      if (par2 == 1 || par2 == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
         this.mc.player.closeScreen();
      }

      super.keyTyped(par1, par2);
   }
}
