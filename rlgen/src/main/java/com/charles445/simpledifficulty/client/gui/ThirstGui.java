package com.charles445.simpledifficulty.client.gui;

import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.SDPotions;
import com.charles445.simpledifficulty.capability.ThirstCapability;
import com.charles445.simpledifficulty.config.ModConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.12 ThirstGui: ten 9x9 drops above the food bar (right side, at GuiIngameForge.right_height, which then grows by 10), drawn where
 * 1.12 drew it (the AIR overlay pre event: after vehicle health, before air). Jitters when thirst saturation is 0.
 */
public class ThirstGui implements LayeredDraw.Layer {
   public static final ResourceLocation ICONS = ResourceLocation.parse("simpledifficulty:textures/gui/icons.png");
   public static final ResourceLocation THIRSTHUD = ResourceLocation.parse("simpledifficulty:textures/gui/thirsthud.png");
   private final Minecraft mc = Minecraft.getInstance();
   private final Random rand = new Random();
   private int updateCounter = 0;

   public void onClientTick() {
      if (!this.mc.isPaused()) {
         this.updateCounter++;
      }
   }

   @Override
   public void render(GuiGraphics g, DeltaTracker delta) {
      if (this.mc.options.hideGui || this.mc.gameMode == null || !this.mc.gameMode.canHurtPlayer() || !ModConfig.isThirstEnabled()) {
         return;
      }
      LocalPlayer player = this.mc.player;
      if (player == null) {
         return;
      }
      this.rand.setSeed((long)(this.updateCounter * 445));
      ResourceLocation tex = ModConfig.client.classicHUDThirst ? ICONS : THIRSTHUD;
      ThirstCapability capability = SDCapabilities.getThirstData(player);
      this.renderThirst(g, tex, g.guiWidth(), g.guiHeight(), capability.getThirstLevel(), capability.getThirstSaturation(), player);
      this.mc.gui.rightHeight += 10;
   }

   private void renderThirst(GuiGraphics g, ResourceLocation tex, int width, int height, int thirst, float thirstSaturation, LocalPlayer player) {
      RenderSystem.enableBlend();
      int left = width / 2 + 82;
      int top = height - this.mc.gui.rightHeight;
      for (int i = 0; i < 10; i++) {
         int halfIcon = i * 2 + 1;
         int x = left - i * 8;
         int y = top;
         int bgXOffset = 0;
         int xOffset = 0;
         if (player.hasEffect(SDPotions.thirsty)) {
            xOffset += 36;
            bgXOffset = 117;
         }
         if (thirstSaturation <= 0.0F && this.updateCounter % (thirst * 3 + 1) == 0) {
            y = top + (this.rand.nextInt(3) - 1);
         }
         g.blit(tex, x, y, bgXOffset, 0, 9, 9);
         if (halfIcon < thirst) {
            g.blit(tex, x, y, xOffset + 36, 0, 9, 9);
         } else if (halfIcon == thirst) {
            g.blit(tex, x, y, xOffset + 45, 0, 9, 9);
         }
      }
      int thirstSaturationInt = (int)thirstSaturation;
      if (thirstSaturationInt > 0 && ModConfig.client.drawThirstSaturation) {
         for (int i = 0; i < 10; i++) {
            int halfIcon = i * 2 + 1;
            int x = left - i * 8;
            if (halfIcon < thirstSaturationInt) {
               g.blit(tex, x, top, 126, 0, 9, 9);
            } else if (halfIcon == thirstSaturationInt) {
               g.blit(tex, x, top, 135, 0, 9, 9);
            }
         }
      }
      RenderSystem.disableBlend();
   }
}
