package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

/** 1.12 GuiHoldButton: fires only after the left mouse button was held on it for the healing item's apply time; scaled text. */
public class GuiHoldButton extends Button {
   public final EnumPlayerPart part;
   public final boolean isRightSide;
   private final Consumer<GuiHoldButton> action;
   private int holdTime;
   private float textScaleFactor;
   private long pressStart = -1L;

   public GuiHoldButton(EnumPlayerPart part, int x, int y, int width, int height, Component text, boolean isRightSide, Consumer<GuiHoldButton> action) {
      super(x, y, width, height, text, b -> {
      }, DEFAULT_NARRATION);
      this.part = part;
      this.isRightSide = isRightSide;
      this.action = action;
   }

   public void setup(int holdTime, float textScaleFactor) {
      this.holdTime = holdTime;
      this.textScaleFactor = Mth.clamp(textScaleFactor > 0.95F ? 1.0F : textScaleFactor, 0.8F, 1.0F);
   }

   @Override
   protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      long window = Minecraft.getInstance().getWindow().getWindow();
      boolean pressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS && this.active && this.isMouseOver(mouseX, mouseY);
      if (!pressed) {
         this.pressStart = -1L;
      } else if (this.pressStart == -1L) {
         this.pressStart = GuiHealthScreen.now();
      }
      Component msg = this.getMessage();
      this.setMessage(Component.empty());
      super.renderWidget(g, mouseX, mouseY, partialTicks);
      this.setMessage(msg);
      int color = !this.active ? 10526880 : this.isHoveredOrFocused() ? 16777120 : 14737632;
      var font = Minecraft.getInstance().font;
      g.pose().pushPose();
      g.pose().scale(this.textScaleFactor, this.textScaleFactor, 1.0F);
      g.drawCenteredString(font, msg, Math.round((float)(this.getX() + this.width / 2) / this.textScaleFactor),
         Math.round((float)(this.getY() + (this.height - 8) / 2) / this.textScaleFactor), color);
      g.pose().popPose();
   }

   /** 1.12 mousePressed: true once the press has lasted holdTime ms over the button */
   public void checkHeld(int mouseX, int mouseY) {
      if (this.pressStart != -1L && this.pressStart + (long)this.holdTime <= GuiHealthScreen.now() && this.active && this.isMouseOver(mouseX, mouseY)) {
         this.pressStart = -1L;
         this.playDownSound(Minecraft.getInstance().getSoundManager());
         this.action.accept(this);
      }
   }

   public int getTimeLeft() {
      return this.pressStart == -1L ? -1 : (int)Math.max(0L, (long)this.holdTime - (GuiHealthScreen.now() - this.pressStart));
   }
}
