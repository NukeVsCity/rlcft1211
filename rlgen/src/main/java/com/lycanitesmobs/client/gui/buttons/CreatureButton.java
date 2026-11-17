package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.info.CreatureInfo;
import net.minecraft.client.Minecraft;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;

public class CreatureButton extends ButtonBase {
   public CreatureInfo creatureInfo;

   public CreatureButton(int buttonID, int x, int y, String text, CreatureInfo creatureInfo) {
      super(buttonID, x, y, 32, 32, text);
      this.creatureInfo = creatureInfo;
   }

   public CreatureButton(int buttonID, int x, int y, int w, int h, String text, CreatureInfo creatureInfo) {
      super(buttonID, x, y, w, h, text);
      this.creatureInfo = creatureInfo;
   }

   /** 1.12 drawButton. */
   @Override
   protected void renderWidget(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      com.lycanitesmobs.client.gui.DrawHelper.setGraphics(graphics);
      net.minecraft.client.gui.Font fontrenderer = Minecraft.getInstance().font;
      int hoverState = this.getHoverState(this.isHovered());
      int buttonX = this.getX();
      int buttonY = this.getY();
      this.bindTexture(AssetManager.getTexture("GUIInventoryCreature"));
      this.drawTexturedModalRect(buttonX, buttonY, 193, 187 - hoverState * 32, this.width, this.height);
      if (this.creatureInfo != null) {
         this.bindTexture(this.creatureInfo.getIcon());
         this.drawTexturedModalRect(buttonX + 8, buttonY + 8, 0, 0, 16, 16, 16);
      }

      int textColor = 14737632;
      if (!this.active) {
         textColor = -6250336;
      } else if (this.isHovered()) {
         textColor = 16777120;
      }

      graphics.drawCenteredString(fontrenderer, this.getMessage(), buttonX + 5, buttonY + 2, textColor);
   }
}
