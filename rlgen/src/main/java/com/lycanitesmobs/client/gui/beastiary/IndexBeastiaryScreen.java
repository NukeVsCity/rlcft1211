package com.lycanitesmobs.client.gui.beastiary;

import net.minecraft.client.Minecraft;
import com.lycanitesmobs.GuiHandler;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.beastiary.lists.BeastiaryIndexList;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.VersionChecker;
import java.net.URI;
import java.net.URISyntaxException;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import net.minecraft.world.entity.player.Player;

public class IndexBeastiaryScreen extends BeastiaryScreen {
   public BeastiaryIndexList indexList;

   public static void openToPlayer(Player player) {
      if (player != null) {
         Minecraft.getInstance().setScreen(new IndexBeastiaryScreen(player));
      }
   }

   @Override
   public String getTitleText() {
      return LanguageManager.translate("gui.beastiary.index.title");
   }

   public IndexBeastiaryScreen(Player player) {
      super(player);
   }

   @Override
   protected void initControls() {
      super.initControls();
      int menuWidth = this.colRightWidth;
      int buttonCount = 6;
      int buttonPadding = 2;
      int buttonWidth = Math.round((float)(menuWidth / buttonCount)) - buttonPadding;
      int buttonWidthPadded = buttonWidth + buttonPadding;
      int buttonHeight = 20;
      int buttonX = this.colRightX + buttonPadding;
      int buttonY = this.colRightY + this.colRightHeight - buttonHeight;
      ButtonBase button = new ButtonBase(100, buttonX, buttonY, buttonWidth, buttonHeight, "Website");
      this.addButton(button);
      button = new ButtonBase(101, buttonX + buttonWidthPadded, buttonY, buttonWidth, buttonHeight, "Twitter");
      this.addButton(button);
      button = new ButtonBase(102, buttonX + buttonWidthPadded * 2, buttonY, buttonWidth, buttonHeight, "Patreon");
      this.addButton(button);
      button = new ButtonBase(103, buttonX + buttonWidthPadded * 3, buttonY, buttonWidth, buttonHeight, "Guilded");
      this.addButton(button);
      button = new ButtonBase(104, buttonX + buttonWidthPadded * 4, buttonY, buttonWidth, buttonHeight, "Discord");
      this.addButton(button);
      button = new ButtonBase(105, buttonX + buttonWidthPadded * 5, buttonY, buttonWidth, buttonHeight, "Shard");
      this.addButton(button);
      this.indexList = new BeastiaryIndexList(this, this.colRightWidth, this.colRightHeight, this.colRightY + 93, buttonY - buttonPadding, this.colRightX + 2);
   }

   @Override
   public void drawBackground(int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(mouseX, mouseY, partialTicks);
   }

   @Override
   protected void updateControls(int mouseX, int mouseY, float partialTicks) {
      super.updateControls(mouseX, mouseY, partialTicks);
   }

   @Override
   public void drawForeground(int mouseX, int mouseY, float partialTicks) {
      super.drawForeground(mouseX, mouseY, partialTicks);
      int yOffset = this.colRightY + 13;
      String info = LanguageManager.translate("gui.beastiary.index.description");
      this.drawSplitString(info, this.colRightX + 1, yOffset, this.colRightWidth, 16777215, true);
      yOffset += this.getFontRenderer().wordWrapHeight(info, this.colRightWidth);
      VersionChecker.VersionInfo latestVersion = VersionChecker.INSTANCE.getLatestVersion();
      if (latestVersion != null) {
         String version = "\n§l" + LanguageManager.translate("gui.beastiary.index.version") + ": §r";
         if (latestVersion.isNewer) {
            version = version + "§4";
         }

         version = version + "2.0.8.9§r";
         if (latestVersion.isNewer) {
            version = version + " §l" + LanguageManager.translate("gui.beastiary.index.version.newer") + ": §r§2" + latestVersion.versionNumber + "§r";
         }

         this.drawSplitString(version, this.colRightX + 1, yOffset, this.colRightWidth, 16777215, true);
         yOffset += this.getFontRenderer().wordWrapHeight(version, this.colRightWidth);
         this.indexList.versionInfo = latestVersion;
         this.indexList.drawScreen(mouseX, mouseY, partialTicks);
      }
   }

   @Override
   public void actionPerformed(ButtonBase guiButton) {
      if (guiButton != null) {
         if (guiButton.id == 100) {
            try {
               this.openURI(new URI("https://lycanitesmobs.com"));
            } catch (URISyntaxException var8) {
            }
         }

         if (guiButton.id == 101) {
            try {
               this.openURI(new URI("https://twitter.com/Lycanite05"));
            } catch (URISyntaxException var7) {
            }
         }

         if (guiButton.id == 102) {
            try {
               this.openURI(new URI("https://www.patreon.com/lycanite"));
            } catch (URISyntaxException var6) {
            }
         }

         if (guiButton.id == 103) {
            try {
               this.openURI(new URI("https://www.guilded.gg/i/jpLvd6J2"));
            } catch (URISyntaxException var5) {
            }
         }

         if (guiButton.id == 104) {
            try {
               this.openURI(new URI("https://discord.gg/bFpV3z4"));
            } catch (URISyntaxException var4) {
            }
         }

         if (guiButton.id == 105) {
            try {
               this.openURI(new URI("https://shardtcg.com"));
            } catch (URISyntaxException var3) {
            }
         }

         super.actionPerformed(guiButton);
      }

      super.actionPerformed(guiButton);
   }
}
