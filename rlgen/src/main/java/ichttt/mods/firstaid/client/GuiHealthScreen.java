package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 1.12 GuiHealthScreen: the 256x137 show_wounds window with the eight part buttons (hold for the item's apply time to use a
 * bandage/plaster on that part), each part's hearts, the player, the morphine time and the sleep-heal bed.
 */
public class GuiHealthScreen extends Screen {
   public static final int X_SIZE = 256;
   public static final int Y_SIZE = 137;
   public static final ItemStack BED_ITEMSTACK = new ItemStack(Items.WHITE_BED);
   private static final DecimalFormat FORMAT = new DecimalFormat("##.#");
   public static boolean isOpen = false;
   private final PlayerDamageModel damageModel;
   private final List<GuiHoldButton> holdButtons = new ArrayList<>();
   private final boolean disableButtons;
   private final float bedScaleFactor = 1.25F;
   public int guiLeft;
   public int guiTop;
   @Nullable
   private InteractionHand activeHand;

   public GuiHealthScreen(PlayerDamageModel damageModel) {
      super(Component.empty());
      this.damageModel = damageModel;
      this.disableButtons = true;
   }

   public GuiHealthScreen(PlayerDamageModel damageModel, InteractionHand activeHand) {
      super(Component.empty());
      this.damageModel = damageModel;
      this.activeHand = activeHand;
      this.disableButtons = false;
   }

   @Override
   protected void init() {
      isOpen = true;
      this.guiLeft = (this.width - 256) / 2;
      this.guiTop = (this.height - 137) / 2;
      this.holdButtons.clear();
      this.hold(EnumPlayerPart.HEAD, 4, 8, "gui.head", false);
      this.hold(EnumPlayerPart.LEFT_ARM, 4, 33, "gui.left_arm", false);
      this.hold(EnumPlayerPart.LEFT_LEG, 4, 58, "gui.left_leg", false);
      this.hold(EnumPlayerPart.LEFT_FOOT, 4, 83, "gui.left_foot", false);
      this.hold(EnumPlayerPart.BODY, 199, 8, "gui.body", true);
      this.hold(EnumPlayerPart.RIGHT_ARM, 199, 33, "gui.right_arm", true);
      this.hold(EnumPlayerPart.RIGHT_LEG, 199, 58, "gui.right_leg", true);
      this.hold(EnumPlayerPart.RIGHT_FOOT, 199, 83, "gui.right_foot", true);
      this.addRenderableWidget(Button.builder(Component.translatable(this.disableButtons ? "gui.done" : "gui.cancel"), b -> this.onClose())
         .bounds(this.width / 2 - 100, this.height - 50, 200, 20).build());
      if (this.minecraft.getDebugOverlay().showDebugScreen()) {
         this.addRenderableWidget(Button.builder(Component.literal("resync"), b -> {
            FirstAidNetwork.sendClientRequest(1);
            this.minecraft.player.displayClientMessage(Component.literal("Re-downloading health data from server..."), true);
            this.onClose();
         }).bounds(this.guiLeft + 218, this.guiTop + 115, 36, 20).build());
      }
   }

   private void hold(EnumPlayerPart part, int x, int y, String key, boolean right) {
      Integer holdTime = this.activeHand == null ? null : FirstAidRegistryImpl.INSTANCE.getPartHealingTime(this.minecraft.player.getItemInHand(this.activeHand));
      GuiHoldButton button = new GuiHoldButton(part, this.guiLeft + x, this.guiTop + y, 52, 20, Component.translatable(key), right, this::apply);
      button.setup(holdTime == null ? Integer.MAX_VALUE : holdTime, 52.0F / (float)HUDHandler.INSTANCE.getMaxLength());
      if (this.disableButtons) {
         button.active = false;
      }
      this.holdButtons.add(button);
      this.addRenderableWidget(button);
   }

   /** actionPerformed for a part button: the item goes on that part */
   private void apply(GuiHoldButton button) {
      FirstAidNetwork.sendApplyHealingItem(button.part, this.activeHand);
      this.damageModel.getFromEnum(button.part).activeHealer = FirstAidRegistryImpl.INSTANCE.getPartHealer(this.minecraft.player.getItemInHand(this.activeHand));
      this.onClose();
   }

   /** 1.12 drawDefaultBackground (dark gradient over the world) + the window and the player, under the buttons */
   @Override
   public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      g.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
      g.fill(this.guiLeft, this.guiTop, this.guiLeft + 256, this.guiTop + 137, 0xFF000000);
      g.blit(HealthRenderUtils.GUI_LOCATION, this.guiLeft, this.guiTop, 0, 0, 256, 137);
      float lookX = (float)(this.guiLeft + 128 - mouseX);
      float lookY = (float)(this.guiTop + 20 - mouseY);
      // 1.12 GuiInventory.drawEntityOnScreen(width / 2, height / 2 + 30, 45, lookX, lookY, player): feet at height / 2 + 30
      int feetY = this.height / 2 + 30;
      InventoryScreen.renderEntityInInventoryFollowsAngle(g, this.width / 2 - 100, feetY - 100, this.width / 2 + 100, feetY + 100, 45,
         -this.minecraft.player.getBbHeight() / 2.0F, (float)Math.atan(lookX / 40.0F), (float)Math.atan(lookY / 40.0F), this.minecraft.player);
   }

   @Override
   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      for (GuiHoldButton b : this.holdButtons) {
         if (!this.disableButtons) {
            b.active = this.damageModel.getFromEnum(b.part).activeHealer == null;
         }
      }
      super.render(g, mouseX, mouseY, partialTicks);
      int morphineTicks = this.damageModel.getMorphineTicks();
      if (morphineTicks > 0) {
         g.drawCenteredString(this.font, I18n.get("gui.morphine_left", StringUtil.formatTickDuration(morphineTicks, 20.0F)), this.guiLeft + 128,
            this.guiTop + 137 - (this.activeHand == null ? 21 : 29), 16777215);
      }
      if (this.activeHand != null) {
         g.drawCenteredString(this.font, I18n.get("gui.apply_hint"), this.guiLeft + 128, this.guiTop + 137 - (morphineTicks == 0 ? 21 : 11), 16777215);
      }
      if (FirstAid.isSynced) {
         this.drawHealth(g, this.damageModel.HEAD, false, 14);
         this.drawHealth(g, this.damageModel.LEFT_ARM, false, 39);
         this.drawHealth(g, this.damageModel.LEFT_LEG, false, 64);
         this.drawHealth(g, this.damageModel.LEFT_FOOT, false, 89);
         this.drawHealth(g, this.damageModel.BODY, true, 14);
         this.drawHealth(g, this.damageModel.RIGHT_ARM, true, 39);
         this.drawHealth(g, this.damageModel.RIGHT_LEG, true, 64);
         this.drawHealth(g, this.damageModel.RIGHT_FOOT, true, 89);
      }
      for (GuiHoldButton b : this.holdButtons) {
         DamageablePart part = this.damageModel.getFromEnum(b.part);
         if (part.activeHealer != null && b.isHovered()) {
            g.renderComponentTooltip(this.font, List.of(
               Component.literal(I18n.get("gui.active_item") + ": ").append(part.activeHealer.stack.getHoverName()),
               Component.translatable("gui.next_heal", Math.round((float)(part.activeHealer.ticksPerHeal - part.activeHealer.getTicksPassed()) / 20.0F))),
               mouseX, mouseY);
         }
      }
      double sleepHealing = FirstAidConfig.externalHealing.sleepHealPercentage;
      int renderBedX = Math.round((float)this.guiLeft / this.bedScaleFactor) + 2;
      int renderBedY = Math.round((float)(this.guiTop + 137) / this.bedScaleFactor) - 18;
      int bedX = (int)((float)renderBedX * this.bedScaleFactor);
      int bedY = (int)((float)renderBedY * this.bedScaleFactor);
      g.pose().pushPose();
      g.pose().scale(this.bedScaleFactor, this.bedScaleFactor, this.bedScaleFactor);
      g.renderItem(BED_ITEMSTACK, renderBedX, renderBedY);
      g.pose().popPose();
      if (mouseX >= bedX && mouseY >= bedY && (float)mouseX < (float)bedX + 16.0F * this.bedScaleFactor
         && (float)mouseY < (float)bedY + 16.0F * this.bedScaleFactor) {
         String s = sleepHealing == 0.0 ? I18n.get("gui.no_sleep_heal") : I18n.get("gui.sleep_heal_amount", FORMAT.format(sleepHealing * 100.0));
         g.renderTooltip(this.font, Component.literal(s), mouseX, mouseY);
      }
      for (GuiHoldButton button : this.holdButtons) {
         int timeLeft = button.getTimeLeft();
         if (timeLeft != -1) {
            float timeInSecs = Math.max(0.0F, (float)timeLeft / 1000.0F);
            g.blit(HealthRenderUtils.GUI_LOCATION, button.getX() + (button.isRightSide ? 56 : -25), button.getY() - 2, button.isRightSide ? 2 : 0, 169, 22, 24);
            g.drawString(this.font, HealthRenderUtils.TEXT_FORMAT.format((double)timeInSecs), button.getX() + (button.isRightSide ? 60 : -20), button.getY() + 6,
               16777215, false);
         }
         button.checkHeld(mouseX, mouseY);
      }
   }

   public void drawHealth(GuiGraphics g, DamageablePart part, boolean right, int yOffset) {
      int xTranslation = this.guiLeft + (right ? getRightOffset(part) : 57);
      HealthRenderUtils.drawHealth(g, part, (float)xTranslation, (float)(this.guiTop + yOffset), true);
   }

   private static int getRightOffset(DamageablePart part) {
      return HealthRenderUtils.drawAsString(part, true) ? 160
         : 200 - Math.min(40, HealthRenderUtils.getMaxHearts((float)part.getMaxHealth()) * 9 + HealthRenderUtils.getMaxHearts(part.getAbsorption()) * 9 + 2);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (FirstAidClient.SHOW_WOUNDS.matches(keyCode, scanCode)) {
         this.onClose();
         return true;
      }
      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void removed() {
      isOpen = false;
      super.removed();
   }

   static long now() {
      return Util.getMillis();
   }
}
