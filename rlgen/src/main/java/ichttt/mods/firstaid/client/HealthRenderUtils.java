package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.Objects;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;

/** 1.12 HealthRenderUtils + FlashStateManager: part hearts drawn from the 1.12 icons.png (critical parts use the hardcore row). */
public class HealthRenderUtils {
   public static final ResourceLocation GUI_LOCATION = ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "textures/gui/show_wounds.png");
   /** 1.12 Gui.ICONS */
   public static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "textures/gui/icons.png");
   public static final DecimalFormat TEXT_FORMAT = new DecimalFormat("0.0");
   private static final Object2IntOpenHashMap<EnumPlayerPart> prevHealth = new Object2IntOpenHashMap<>();
   private static final EnumMap<EnumPlayerPart, FlashStateManager> flashStates = new EnumMap<>(EnumPlayerPart.class);

   static {
      for (EnumPlayerPart part : EnumPlayerPart.VALUES) {
         flashStates.put(part, new FlashStateManager());
      }
   }

   public static void drawHealthString(GuiGraphics g, DamageablePart damageablePart, float xTranslation, float yTranslation, boolean allowSecondLine) {
      float absorption = damageablePart.getAbsorption();
      String text = TEXT_FORMAT.format((double)damageablePart.currentHealth) + "/" + damageablePart.getMaxHealth();
      var font = Minecraft.getInstance().font;
      if (absorption > 0.0F) {
         String line2 = "+ " + TEXT_FORMAT.format((double)absorption);
         if (allowSecondLine) {
            g.drawString(font, line2, (int)xTranslation, (int)(yTranslation + 5.0F), 16777215, true);
            yTranslation -= 5.0F;
         } else {
            text = text + " " + line2;
         }
      }
      g.drawString(font, text, (int)xTranslation, (int)yTranslation, 16777215, true);
   }

   private static void updatePrev(EnumPlayerPart part, int current, boolean playerDead) {
      if (!playerDead) {
         prevHealth.put(part, current);
      } else {
         prevHealth.clear();
      }
   }

   public static boolean healthChanged(DamageablePart damageablePart, boolean playerDead) {
      int current = (int)Math.ceil((double)damageablePart.currentHealth);
      FlashStateManager activeFlashState = Objects.requireNonNull(flashStates.get(damageablePart.part));
      if (prevHealth.containsKey(damageablePart.part)) {
         int prev = prevHealth.getInt(damageablePart.part);
         updatePrev(damageablePart.part, current, playerDead);
         if (prev != current) {
            activeFlashState.setActive(Util.getMillis());
            return true;
         }
         return false;
      }
      activeFlashState.setActive(Util.getMillis());
      updatePrev(damageablePart.part, current, playerDead);
      return true;
   }

   public static boolean drawAsString(DamageablePart damageablePart, boolean allowSecondLine) {
      int maxHealth = getMaxHearts((float)damageablePart.getMaxHealth());
      int maxExtraHealth = getMaxHearts(damageablePart.getAbsorption());
      return maxHealth + maxExtraHealth > 8 && allowSecondLine || maxHealth + maxExtraHealth > 12;
   }

   public static void drawHealth(GuiGraphics g, DamageablePart damageablePart, float xTranslation, float yTranslation, boolean allowSecondLine) {
      int maxHealth = getMaxHearts((float)damageablePart.getMaxHealth());
      int maxExtraHealth = getMaxHearts(damageablePart.getAbsorption());
      int current = (int)Math.ceil((double)damageablePart.currentHealth);
      FlashStateManager activeFlashState = Objects.requireNonNull(flashStates.get(damageablePart.part));
      if (drawAsString(damageablePart, allowSecondLine)) {
         drawHealthString(g, damageablePart, xTranslation, yTranslation, allowSecondLine);
         return;
      }
      int yTexture = damageablePart.canCauseDeath ? 45 : 0;
      int absorption = (int)Math.ceil((double)damageablePart.getAbsorption());
      boolean highlight = activeFlashState.update(Util.getMillis());
      Minecraft mc = Minecraft.getInstance();
      int regen = -1;
      if (FirstAidConfig.externalHealing.allowOtherHealingItems && mc.player.hasEffect(MobEffects.REGENERATION)) {
         regen = mc.gui.getGuiTicks() / 2 % 15;
      }
      boolean low = (float)(current + absorption) < 1.25F;
      g.pose().pushPose();
      g.pose().translate(xTranslation, yTranslation, 0.0F);
      boolean drawSecondLine = allowSecondLine && maxHealth + maxExtraHealth > 4;
      if (drawSecondLine) {
         int maxHealth2 = 0;
         if (maxHealth > 4) {
            maxHealth2 = maxHealth - 4;
            maxHealth = 4;
         }
         int maxExtraHealth2 = Math.max(0, maxExtraHealth - (4 - maxHealth));
         maxExtraHealth -= maxExtraHealth2;
         int current2 = 0;
         if (current > 8) {
            current2 = current - 8;
            current = 8;
         }
         int absorption2 = absorption - maxExtraHealth * 2;
         absorption -= absorption2;
         g.pose().translate(0.0F, 5.0F, 0.0F);
         g.pose().pushPose();
         renderLine(g, regen, low, yTexture, maxHealth2, maxExtraHealth2, current2, absorption2, highlight);
         regen -= maxHealth2 + maxExtraHealth;
         g.pose().popPose();
         g.pose().translate(0.0F, -10.0F, 0.0F);
      }
      renderLine(g, regen, low, yTexture, maxHealth, maxExtraHealth, current, absorption, highlight);
      g.pose().popPose();
   }

   private static void renderLine(GuiGraphics g, int regen, boolean low, int yTexture, int maxHealth, int maxExtraHearts, int current, int absorption,
                                  boolean highlight) {
      g.pose().pushPose();
      Int2IntMap map = new Int2IntArrayMap();
      if (low) {
         for (int i = 0; i < maxHealth + maxExtraHearts; i++) {
            map.put(i, FirstAid.RAND.nextInt(2));
         }
      }
      renderMax(g, regen, map, maxHealth, yTexture, highlight);
      if (maxExtraHearts > 0) {
         if (maxHealth != 0) {
            g.pose().translate((float)(2 + 9 * maxHealth), 0.0F, 0.0F);
         }
         renderMax(g, regen - maxHealth, map, maxExtraHearts, yTexture, false);
      }
      g.pose().popPose();
      g.pose().translate(0.0F, 0.0F, 1.0F);
      renderCurrentHealth(g, regen, map, current, yTexture);
      if (absorption > 0) {
         int offset = maxHealth * 9 + (maxHealth == 0 ? 0 : 2);
         g.pose().translate((float)offset, 0.0F, 0.0F);
         renderAbsorption(g, regen - maxHealth, map, absorption, yTexture);
      }
   }

   public static int getMaxHearts(float value) {
      int maxCurrentHearts = (int)Math.ceil((double)value);
      if (maxCurrentHearts % 2 != 0) {
         maxCurrentHearts++;
      }
      return maxCurrentHearts >> 1;
   }

   private static void renderMax(GuiGraphics g, int regen, Int2IntMap function, int max, int yTexture, boolean highlight) {
      int background = highlight ? 25 : 16;
      renderTexturedModalRects(g, regen, function, max, false, background, background, yTexture);
   }

   private static void renderCurrentHealth(GuiGraphics g, int regen, Int2IntMap function, int current, int yTexture) {
      boolean renderLastHalf = false;
      int render = current >> 1;
      if (current % 2 != 0) {
         renderLastHalf = true;
         render++;
      }
      renderTexturedModalRects(g, regen, function, render, renderLastHalf, 61, 52, yTexture);
   }

   private static void renderAbsorption(GuiGraphics g, int regen, Int2IntMap function, int absorption, int yTexture) {
      boolean renderLastHalf = false;
      int render = absorption >> 1;
      if (absorption % 2 != 0) {
         renderLastHalf = true;
         render++;
      }
      if (render > 0) {
         renderTexturedModalRects(g, regen, function, render, renderLastHalf, 169, 160, yTexture);
      }
   }

   private static void renderTexturedModalRects(GuiGraphics g, int regen, Int2IntMap function, int toDraw, boolean lastOneHalf, int halfTextureX, int textureX,
                                                int textureY) {
      if (toDraw < 0) {
         throw new IllegalArgumentException("Cannot draw negative amount of icons " + toDraw);
      }
      for (int i = 0; i < toDraw; i++) {
         boolean renderHalf = lastOneHalf && i + 1 == toDraw;
         g.blit(ICONS, 9 * i, (i == regen ? -2 : 0) - function.get(i), renderHalf ? halfTextureX : textureX, textureY, 9, 9);
      }
   }

   /** 1.12 FlashStateManager: 8 states of 150 ms, highlighted on the even ones. */
   public static class FlashStateManager {
      private long startTime;
      private int currentState = 0;

      public void setActive(long startTime) {
         this.startTime = startTime;
         this.currentState = 1;
      }

      public boolean update(long worldTime) {
         if (this.isPaused()) {
            return false;
         }
         this.currentState = (int)((worldTime - this.startTime) / 150L) + 1;
         if (this.currentState >= 8) {
            this.currentState = 0;
         }
         return !this.isPaused() && this.currentState % 2 == 0;
      }

      public boolean isPaused() {
         return this.currentState == 0;
      }
   }
}
