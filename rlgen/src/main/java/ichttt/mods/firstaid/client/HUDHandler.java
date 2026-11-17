package ichttt.mods.firstaid.client;

import com.mojang.blaze3d.systems.RenderSystem;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 1.12 HUDHandler + PlayerModelRenderer: the body overlay (RLCraft: PLAYER_MODEL, bottom right, y offset 26, alpha 50), shown for
 * 200 ticks after a part changed (fading out over the last 30) and flashing; drawn after everything (ALL post event).
 */
public class HUDHandler implements LayeredDraw.Layer {
   public static final HUDHandler INSTANCE = new HUDHandler();
   private static final ResourceLocation HEALTH_RENDER_LOCATION = ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "textures/gui/simple_health.png");
   private static final ResourceLocation HEALTH_RENDER_LOCATION_OLD = ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "textures/gui/simple_health_old.png");
   private final Map<EnumPlayerPart, String> TRANSLATION_MAP = new EnumMap<>(EnumPlayerPart.class);
   private final HealthRenderUtils.FlashStateManager flashStateManager = new HealthRenderUtils.FlashStateManager();
   private int maxLength;
   public int ticker = -1;

   private void buildTranslationTable() {
      this.TRANSLATION_MAP.clear();
      this.maxLength = 0;
      for (EnumPlayerPart part : EnumPlayerPart.VALUES) {
         String translated = I18n.get("gui." + part.toString().toLowerCase(Locale.ENGLISH));
         this.maxLength = Math.max(this.maxLength, Minecraft.getInstance().font.width(translated));
         this.TRANSLATION_MAP.put(part, translated);
      }
   }

   public int getMaxLength() {
      if (this.TRANSLATION_MAP.isEmpty()) {
         this.buildTranslationTable();
      }
      return this.maxLength;
   }

   @Override
   public void render(GuiGraphics g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || !mc.player.isAlive() || mc.options.hideGui || !FirstAid.isSynced) {
         return;
      }
      PlayerDamageModel damageModel = FirstAid.getDamageModel(mc.player);
      if (this.TRANSLATION_MAP.isEmpty()) {
         this.buildTranslationTable();
      }
      int visibleTicks = FirstAidConfig.overlay.displayMode.visibleDurationTicks;
      if (visibleTicks != -1) {
         visibleTicks += 30;
      }
      boolean playerDead = damageModel.isDead(mc.player);
      for (DamageablePart damageablePart : damageModel) {
         if (HealthRenderUtils.healthChanged(damageablePart, playerDead)) {
            if (visibleTicks != -1) {
               this.ticker = Math.max(this.ticker, visibleTicks);
            }
            if (FirstAidConfig.overlay.displayMode.flash) {
               this.flashStateManager.setActive(Util.getMillis());
            }
         }
      }
      FirstAidConfig.Overlay o = FirstAidConfig.overlay;
      if (o.overlayMode == FirstAidConfig.Overlay.OverlayMode.OFF || GuiHealthScreen.isOpen && !o.overlayMode.isPlayerModel()
         || !CommonUtils.isSurvivalOrAdventure(mc.player) || visibleTicks != -1 && this.ticker < 0) {
         return;
      }
      int xOffset = o.xOffset;
      int yOffset = o.yOffset;
      boolean playerModel = o.overlayMode.isPlayerModel();
      int width = g.guiWidth();
      int height = g.guiHeight();
      switch (o.pos) {
         case TOP_LEFT -> {
            if (playerModel) {
               xOffset++;
            }
         }
         case TOP_RIGHT -> xOffset = width - xOffset - (playerModel ? 34 : damageModel.getMaxRenderSize() + this.maxLength);
         case BOTTOM_LEFT -> {
            if (playerModel) {
               xOffset++;
            }
            yOffset = height - yOffset - (playerModel ? 66 : 80);
         }
         case BOTTOM_RIGHT -> {
            xOffset = width - xOffset - (playerModel ? 34 : damageModel.getMaxRenderSize() + this.maxLength);
            yOffset = height - yOffset - (playerModel ? 62 : 80);
         }
      }
      if (mc.screen instanceof ChatScreen && o.pos == FirstAidConfig.Overlay.Position.BOTTOM_LEFT
         || mc.getDebugOverlay().showDebugScreen() && o.pos == FirstAidConfig.Overlay.Position.TOP_LEFT) {
         return;
      }
      boolean enableAlphaBlend = visibleTicks != -1 && this.ticker < 30;
      int alpha = enableAlphaBlend ? Mth.clamp((int)((float)(30 - this.ticker) * 255.0F / 30.0F), o.alpha, 250) : o.alpha;
      g.pose().pushPose();
      g.pose().translate((float)xOffset, (float)yOffset, 0.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      if (playerModel) {
         boolean fourColors = o.overlayMode == FirstAidConfig.Overlay.OverlayMode.PLAYER_MODEL_4_COLORS
            || o.overlayMode == FirstAidConfig.Overlay.OverlayMode.PLAYER_MODEL_OLD_4_COLORS;
         boolean oldModel = o.overlayMode == FirstAidConfig.Overlay.OverlayMode.PLAYER_MODEL_OLD_4_COLORS
            || o.overlayMode == FirstAidConfig.Overlay.OverlayMode.PLAYER_MODEL_OLD_COLORS;
         renderPlayerHealth(g, damageModel, fourColors, oldModel, this.flashStateManager.update(Util.getMillis()), (float)alpha);
      } else {
         int xTranslation = this.maxLength;
         for (DamageablePart part : damageModel) {
            g.drawString(mc.font, this.TRANSLATION_MAP.get(part.part), 0, 0, 16777215 - (alpha << 24 & -16777215), true);
            if (o.overlayMode == FirstAidConfig.Overlay.OverlayMode.NUMBERS) {
               HealthRenderUtils.drawHealthString(g, part, (float)xTranslation, 0.0F, false);
            } else {
               HealthRenderUtils.drawHealth(g, part, (float)xTranslation, 0.0F, false);
            }
            g.pose().translate(0.0F, 10.0F, 0.0F);
         }
      }
      RenderSystem.disableBlend();
      g.pose().popPose();
   }

   /** 1.12 PlayerModelRenderer.renderPlayerHealth (the AF day / halloween wobble is EventCalendar dependent, off on normal days) */
   private static void renderPlayerHealth(GuiGraphics g, PlayerDamageModel damageModel, boolean fourColors, boolean oldModel, boolean flashState, float alpha) {
      int yOffset = flashState ? 64 : 0;
      g.pose().pushPose();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F - alpha / 255.0F);
      ResourceLocation tex = oldModel ? HEALTH_RENDER_LOCATION_OLD : HEALTH_RENDER_LOCATION;
      if (yOffset != 0) {
         g.pose().translate(0.0F, (float)(-yOffset), 0.0F);
      }
      drawPart(g, tex, fourColors, damageModel.HEAD, 8, yOffset, 16, 16);
      drawPart(g, tex, fourColors, damageModel.BODY, 8, yOffset + 16, 16, 24);
      drawPart(g, tex, fourColors, damageModel.LEFT_ARM, 0, yOffset + 16, 8, 24);
      drawPart(g, tex, fourColors, damageModel.RIGHT_ARM, 24, yOffset + 16, 8, 24);
      drawPart(g, tex, fourColors, damageModel.LEFT_LEG, 8, yOffset + 40, 8, 16);
      drawPart(g, tex, fourColors, damageModel.RIGHT_LEG, 16, yOffset + 40, 8, 16);
      drawPart(g, tex, fourColors, damageModel.LEFT_FOOT, 8, yOffset + 56, 8, 8);
      drawPart(g, tex, fourColors, damageModel.RIGHT_FOOT, 16, yOffset + 56, 8, 8);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      g.pose().popPose();
   }

   /** Gui.drawTexturedModalRect on a 128x128 texture (1.12 assumed 256, so the 1.12 call read texels at x/256: kept as 1.12 drew) */
   private static void drawPart(GuiGraphics g, ResourceLocation tex, boolean fourColors, DamageablePart part, int texX, int texY, int sizeX, int sizeY) {
      int u = texX + 32 * getState(part, fourColors);
      g.blit(tex, texX, texY, u, texY, sizeX, sizeY, 256, 256);
   }

   private static int getState(DamageablePart part, boolean fourColors) {
      if (part.currentHealth <= 0.001F) {
         return 5;
      }
      int maxHealth = part.getMaxHealth();
      if (Math.abs(part.currentHealth - (float)maxHealth) < 0.001F) {
         return 0;
      }
      float healthPercentage = part.currentHealth / (float)maxHealth;
      if (healthPercentage >= 1.0F || healthPercentage <= 0.0F) {
         return healthPercentage >= 1.0F ? 0 : 5;
      } else if (!fourColors && healthPercentage > 0.75F) {
         return 1;
      } else if (healthPercentage > 0.5F) {
         return 2;
      } else {
         return !fourColors && healthPercentage > 0.25F ? 3 : 4;
      }
   }

   public void clientTick() {
      if (this.ticker >= 0) {
         this.ticker--;
      }
   }
}
