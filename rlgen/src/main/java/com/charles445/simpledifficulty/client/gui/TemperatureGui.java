package com.charles445.simpledifficulty.client.gui;

import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.SDItems;
import com.charles445.simpledifficulty.api.temperature.TemperatureEnum;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.util.TemperatureUtil;
import com.charles445.simpledifficulty.util.WorldUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.12 TemperatureGui: the 16x16 temperature orb centred above the hotbar (height - 54), drawn first (ALL overlay pre event), with
 * the AlternateTemperature "feel" ring (32x32, the client-computed target temperature), the change animation, the shake when
 * freezing/burning, and the HUD thermometer above it when a thermometer is in the inventory.
 */
public class TemperatureGui implements LayeredDraw.Layer {
   public static final ResourceLocation ICONS = ResourceLocation.parse("simpledifficulty:textures/gui/icons.png");
   public static final ResourceLocation TEMPHUD = ResourceLocation.parse("simpledifficulty:textures/gui/temphud.png");
   private final Minecraft mc = Minecraft.getInstance();
   private int updateCounter = 0;
   private int oldTemperature = -1;
   private int frameCounterClassic = -1;
   private int frameCounterModern = -1;
   private boolean risingTemperature = false;
   private boolean startAnimation = false;
   private boolean shakeSide = false;
   private int alternateTemperature = 0;
   private int worldThermometerTemperature = 0;
   private boolean hasThermometer = false;

   @Override
   public void render(GuiGraphics g, DeltaTracker delta) {
      if (this.mc.options.hideGui || this.mc.gameMode == null || !this.mc.gameMode.canHurtPlayer() || !ModConfig.isTemperatureEnabled()) {
         return;
      }
      LocalPlayer player = this.mc.player;
      if (player == null) {
         return;
      }
      boolean classic = ModConfig.client.classicHUDTemperature;
      ResourceLocation tex = classic ? ICONS : TEMPHUD;
      int temperature = SDCapabilities.getTemperatureData(player).getTemperatureLevel();
      RenderSystem.enableBlend();
      if (classic) {
         this.renderClassicTemperatureIcon(g, tex, g.guiWidth(), g.guiHeight(), temperature);
      } else {
         this.renderTemperatureIcon(g, tex, g.guiWidth(), g.guiHeight(), temperature);
      }
      RenderSystem.disableBlend();
   }

   private int getTempHudFeelBGX(int temperature) {
      return temperature >= 6 && temperature < 20 ? (temperature / 2 - 2) * 32 : 0;
   }

   private int getTempHudFeelBGY(int temperature) {
      return temperature >= 20 ? 32 : 0;
   }

   private int getTempHudCoreBGX(int temperature) {
      int bgx;
      boolean animated = false;
      if (temperature < 6) {
         bgx = 0;
         animated = true;
      } else if (temperature >= 20) {
         bgx = 192;
         animated = true;
      } else {
         bgx = (temperature / 2 + 2) * 16;
      }
      if (animated) {
         if (temperature < 12) {
            bgx += this.updateCounter % 5 * 16;
         } else {
            bgx += this.updateCounter % 4 * 16;
         }
      }
      return bgx;
   }

   private int shake(TemperatureEnum tempEnum, int temperature) {
      int shakeFrequency = this.getShakeFrequency(tempEnum, temperature);
      if (shakeFrequency > 0) {
         if (this.updateCounter % shakeFrequency == 0) {
            this.shakeSide = this.updateCounter / shakeFrequency % 2 == 0;
         }
         return this.shakeSide ? 1 : -1;
      }
      return 0;
   }

   private void renderTemperatureIcon(GuiGraphics g, ResourceLocation tex, int width, int height, int temperature) {
      TemperatureEnum tempEnum = TemperatureUtil.getTemperatureEnum(temperature);
      int bgXOffset = this.getTempHudCoreBGX(temperature);
      int x = width / 2 - 8;
      int y = height - 54;
      int xOffset = this.shake(tempEnum, temperature);
      if (ModConfig.client.alternateTemp) {
         g.blit(tex, x - 8 + xOffset, y - 8, this.getTempHudFeelBGX(this.alternateTemperature), 16 + this.getTempHudFeelBGY(this.alternateTemperature), 32, 32);
      }
      g.blit(tex, x + xOffset, y, bgXOffset, 0, 16, 16);
      this.renderTemperatureChangeAnimation(g, tex, false, x, y, temperature);
      this.renderThermometer(g, tex, x, y);
   }

   private void renderClassicTemperatureIcon(GuiGraphics g, ResourceLocation tex, int width, int height, int temperature) {
      TemperatureEnum tempEnum = TemperatureUtil.getTemperatureEnum(temperature);
      int bgXOffset = 16 * tempEnum.ordinal();
      int x = width / 2 - 8;
      int y = height - 54;
      int xOffset = this.shake(tempEnum, temperature);
      if (ModConfig.client.alternateTemp) {
         int outsideOffset = 16 * TemperatureUtil.getTemperatureEnum(this.alternateTemperature).ordinal();
         g.blit(tex, x + xOffset, y, outsideOffset, 96, 16, 16);
         g.blit(tex, x + xOffset, y, bgXOffset, 80, 16, 16);
      } else {
         g.blit(tex, x + xOffset, y, bgXOffset, 32, 16, 16);
      }
      this.renderTemperatureChangeAnimation(g, tex, true, x, y, temperature);
      this.renderThermometer(g, tex, x, y);
   }

   private void renderTemperatureChangeAnimation(GuiGraphics g, ResourceLocation tex, boolean classic, int x, int y, int temperature) {
      if (this.oldTemperature == -1) {
         this.oldTemperature = temperature;
      }
      if (this.oldTemperature != temperature) {
         this.risingTemperature = this.oldTemperature < temperature;
         this.oldTemperature = temperature;
         this.startAnimation = true;
      }
      if (classic) {
         if (this.frameCounterClassic >= 0) {
            g.blit(tex, x, y, 16 * this.frameCounterClassic, 32 + (this.risingTemperature ? 1 : 2) * 16, 16, 16);
         }
      } else if (this.frameCounterModern >= 0) {
         g.blit(tex, x, y, (14 - this.frameCounterModern) * 16, 144 + (this.risingTemperature ? 1 : 0) * 16, 16, 16);
      }
   }

   private void renderThermometer(GuiGraphics g, ResourceLocation tex, int x, int y) {
      if (this.hasThermometer && ModConfig.client.thermometer.hudThermometer && ModConfig.client.thermometer.enableThermometer) {
         int thermPosition = this.worldThermometerTemperature - TemperatureEnum.FREEZING.getLowerBound();
         int thermX = thermPosition % 8 * 16;
         int thermY = thermPosition / 8 * 16 + 192;
         g.blit(tex, x + ModConfig.client.thermometer.hudThermometerX, y - 18 + ModConfig.client.thermometer.hudThermometerY, thermX, thermY, 16, 16);
      }
   }

   private int getShakeFrequency(TemperatureEnum tempEnum, int temperature) {
      if (tempEnum == TemperatureEnum.FREEZING) {
         if (temperature == TemperatureEnum.FREEZING.getUpperBound()) {
            return 0;
         }
         return temperature > TemperatureEnum.FREEZING.getMiddle() + 1 ? 2 : 1;
      } else if (tempEnum == TemperatureEnum.BURNING) {
         if (temperature == TemperatureEnum.BURNING.getLowerBound()) {
            return 0;
         }
         return temperature >= TemperatureEnum.BURNING.getMiddle() ? 1 : 2;
      }
      return 0;
   }

   public void onClientTick() {
      if (this.mc.isPaused()) {
         return;
      }
      this.updateCounter++;
      if (this.frameCounterClassic >= 0) {
         this.frameCounterClassic--;
      }
      if (this.frameCounterModern >= 0) {
         this.frameCounterModern--;
      }
      if (this.startAnimation) {
         this.frameCounterClassic = 11;
         this.frameCounterModern = 14;
         this.startAnimation = false;
      }
      if (this.updateCounter % 15 == 12 && ModConfig.isTemperatureEnabled() && this.mc.player != null) {
         LocalPlayer player = this.mc.player;
         if (ModConfig.client.alternateTemp) {
            this.alternateTemperature = TemperatureUtil.clampTemperature(TemperatureUtil.getPlayerTargetTemperature(player));
         }
         if (ModConfig.client.thermometer.hudThermometer && ModConfig.client.thermometer.enableThermometer) {
            this.worldThermometerTemperature = TemperatureUtil.clampTemperature(WorldUtil.calculateClientWorldEntityTemperature(player.level(), player));
            this.hasThermometer = player.getInventory().hasAnyMatching(s -> s.is(SDItems.thermometer.get()));
         }
      }
   }
}
