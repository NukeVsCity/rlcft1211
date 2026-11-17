package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.client.gui.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.12 ButtonBase (a GuiButton with a numeric id). A press calls {@link Listener#actionPerformed} on the screen that owns
 * it (1.12 GuiScreen.actionPerformed(ButtonBase)).
 */
public class ButtonBase extends Button {
   public int id;

   public interface Listener {
      void actionPerformed(ButtonBase button);
   }

   public ButtonBase(int buttonID, int x, int y, int width, int height, String text) {
      super(x, y, width, height, Component.literal(text), b -> {
         if (Minecraft.getInstance().screen instanceof Listener listener) {
            listener.actionPerformed((ButtonBase)b);
         }
      }, DEFAULT_NARRATION);
      this.id = buttonID;
   }

   /** 1.12 displayString. */
   public String getDisplayString() {
      return this.getMessage().getString();
   }

   public void setDisplayString(String text) {
      this.setMessage(Component.literal(text));
   }

   /** 1.12 GuiButton.getHoverState: 0 disabled, 1 normal, 2 hovered. */
   public int getHoverState(boolean mouseOver) {
      int i = 1;
      if (!this.active) {
         i = 0;
      } else if (mouseOver) {
         i = 2;
      }

      return i;
   }

   public void drawTexturedModalRect(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
      this.drawTexturedModalRect(texture, x, y, u, v, width, height, 1);
   }

   public void drawTexturedModalRect(ResourceLocation texture, int x, int y, int u, int v, int width, int height, int resolution) {
      float scaleX = 0.00390625F * (float)resolution;
      DrawHelper.quad(texture, x, y, 0.0F, width, height, u * scaleX, v * scaleX, (u + width) * scaleX, (v + height) * scaleX);
   }
   /** The texture 1.12 had bound (TextureManager.bindTexture) for drawTexturedModalRect. */
   protected ResourceLocation boundTexture;

   public void bindTexture(ResourceLocation texture) {
      this.boundTexture = texture;
   }

   public void drawTexturedModalRect(int x, int y, int u, int v, int width, int height) {
      this.drawTexturedModalRect(this.boundTexture, x, y, u, v, width, height, 1);
   }

   public void drawTexturedModalRect(int x, int y, int u, int v, int width, int height, int resolution) {
      this.drawTexturedModalRect(this.boundTexture, x, y, u, v, width, height, resolution);
   }
}
