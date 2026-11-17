package com.lycanitesmobs.client.gui.overlays;

import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.KeyHandler;
import com.lycanitesmobs.client.gui.DrawHelper;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.CreatureRelationshipEntry;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.item.temp.ItemStaffSummoning;
import com.lycanitesmobs.core.mobevent.MobEventPlayerClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * 1.12 BaseOverlay: after the experience bar (1.12 RenderGameOverlayEvent.Post EXPERIENCE) it draws the mob event intro
 * images, the summoning focus bar (with a summoning staff), the mount stamina bar (from the 1.12 icons.png, bundled as
 * textures/guis/icons112.png since 1.21 has no such sheet) and the taming reputation bar of the creature looked at.
 */
public class BaseOverlay {
   private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath("lycanitesmobs", "textures/guis/icons112.png");
   public DrawHelper drawHelper;
   public Minecraft minecraft;
   private int mountMessageTimeMax = 200;
   private int mountMessageTime = 0;

   public BaseOverlay(Minecraft minecraft) {
      this.minecraft = minecraft;
      this.drawHelper = new DrawHelper(minecraft, minecraft.font);
   }

   /** 1.12 BaseGui.drawTexturedModalRect (current GL state: blend on, the texture bound before). */
   public void drawTexturedModalRect(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
      float scaleX = 0.00390625F;
      DrawHelper.quad(texture, x, y, 0.0F, width, height, u * scaleX, v * scaleX, (u + width) * scaleX, (v + height) * scaleX);
   }

   /** Called by MobEventPlayerClient.onGUIUpdate (through ClientProxy.drawMobEventIntro). */
   public void drawMobEventIntro(ResourceLocation texture, int x, int y, int width, int height, float alpha) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      // 1.12: drawTexturedModalRect(x, y, width, height, width, height), u/v = 256 (the texture repeats)
      this.drawTexturedModalRect(texture, x, y, width, height, width, height);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public void onRenderExperienceBar(RenderGuiLayerEvent.Post event) {
      if (!VanillaGuiLayers.EXPERIENCE_BAR.equals(event.getName())) {
         return;
      }
      Player player = LycanitesMobs.proxy.getClientPlayer();
      if (player == null) {
         return;
      }
      GuiGraphics graphics = event.getGuiGraphics();
      DrawHelper.setGraphics(graphics);
      graphics.pose().pushPose();
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
         com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
         com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      int sWidth = graphics.guiWidth();
      int sHeight = graphics.guiHeight();
      ExtendedWorld worldExt = ExtendedWorld.getForWorld(player.level());
      if (worldExt != null) {
         for (MobEventPlayerClient mobEventPlayerClient : worldExt.clientMobEventPlayers.values()) {
            graphics.pose().pushPose();
            mobEventPlayerClient.onGUIUpdate(this, sWidth, sHeight);
            graphics.pose().popPose();
         }

         if (worldExt.clientWorldEventPlayer != null) {
            graphics.pose().pushPose();
            worldExt.clientWorldEventPlayer.onGUIUpdate(this, sWidth, sHeight);
            graphics.pose().popPose();
         }
      }

      ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
      if (playerExt != null && !this.minecraft.player.getAbilities().instabuild
         && (this.minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemStaffSummoning
            || this.minecraft.player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ItemStaffSummoning)) {
         ResourceLocation texture = AssetManager.getTexture("GUIInventoryCreature");
         int barYSpace = 10;
         int barXSpace = -1;
         int summonBarWidth = 9;
         int summonBarHeight = 9;
         int summonBarX = sWidth / 2 + 10;
         int summonBarY = sHeight - 30 - summonBarHeight;
         int summonBarU = 256 - summonBarWidth;
         int summonBarV = 256 - summonBarHeight;
         summonBarY -= barYSpace;
         // 1.12 isInsideOfMaterial(WATER): the eyes in water
         if (this.minecraft.player.isEyeInFluid(FluidTags.WATER)) {
            summonBarY -= barYSpace;
         }

         for (int summonBarEnergyN = 0; summonBarEnergyN < 10; summonBarEnergyN++) {
            this.drawTexturedModalRect(texture, summonBarX + (summonBarWidth + barXSpace) * summonBarEnergyN, summonBarY, summonBarU, summonBarV, summonBarWidth, summonBarHeight);
            if (playerExt.summonFocus >= playerExt.summonFocusMax - summonBarEnergyN * playerExt.summonFocusCharge) {
               this.drawTexturedModalRect(texture, summonBarX + (summonBarWidth + barXSpace) * summonBarEnergyN, summonBarY, summonBarU - summonBarWidth, summonBarV,
                  summonBarWidth, summonBarHeight);
            } else if (playerExt.summonFocus + playerExt.summonFocusCharge > playerExt.summonFocusMax - summonBarEnergyN * playerExt.summonFocusCharge) {
               float summonChargeScale = (float)(playerExt.summonFocus % playerExt.summonFocusCharge) / (float)playerExt.summonFocusCharge;
               this.drawTexturedModalRect(texture,
                  summonBarX + (summonBarWidth + barXSpace) * summonBarEnergyN + (summonBarWidth - Math.round((float)summonBarWidth * summonChargeScale)),
                  summonBarY, summonBarU - Math.round((float)summonBarWidth * summonChargeScale), summonBarV,
                  Math.round((float)summonBarWidth * summonChargeScale), summonBarHeight);
            }
         }
      }

      if (this.minecraft.player.getVehicle() instanceof RideableCreatureEntity mount) {
         float mountStamina = mount.getStaminaPercent();
         if (this.mountMessageTime > 0) {
            String mountMessage = LanguageManager.translate("gui.mount.controls");
            mountMessage = mountMessage.replace("%controlA%", KeyHandler.instance.mountAbility.getTranslatedKeyMessage().getString());
            mountMessage = mountMessage.replace("%controlB%", KeyHandler.instance.dismount.getTranslatedKeyMessage().getString());
            this.minecraft.gui.setOverlayMessage(Component.literal(mountMessage), false);
         }

         int staminaBarWidth = 182;
         int staminaBarHeight = 5;
         int staminaEnergyWidth = (int)((float)(staminaBarWidth + 1) * mountStamina);
         int staminaBarX = sWidth / 2 - staminaBarWidth / 2;
         int staminaBarY = sHeight - 32 + 3;
         int staminaTextureY = 84;
         if ("toggle".equals(mount.getStaminaType())) {
            staminaTextureY -= staminaBarHeight * 2;
         }

         int staminaEnergyY = staminaTextureY + staminaBarHeight;
         this.drawTexturedModalRect(ICONS, staminaBarX, staminaBarY, 0, staminaTextureY, staminaBarWidth, staminaBarHeight);
         if (staminaEnergyWidth > 0) {
            this.drawTexturedModalRect(ICONS, staminaBarX, staminaBarY, 0, staminaEnergyY, staminaEnergyWidth, staminaBarHeight);
         }

         if (this.mountMessageTime > 0) {
            this.mountMessageTime--;
         }
      } else {
         this.mountMessageTime = this.mountMessageTimeMax;
      }

      HitResult mouseOver = this.minecraft.hitResult;
      if (mouseOver instanceof EntityHitResult entityHit) {
         Entity mouseOverEntity = entityHit.getEntity();
         if (mouseOverEntity instanceof BaseCreatureEntity creatureEntity) {
            CreatureInfo creatureInfo = creatureEntity.creatureInfo;
            CreatureRelationshipEntry relationshipEntry = creatureEntity.relationships.getEntry(player);
            if (relationshipEntry != null && relationshipEntry.getReputation() > 0 && !creatureEntity.isTamed()) {
               float barWidth = 100.0F;
               float barHeight = 11.0F;
               float barX = (float)sWidth / 2.0F - barWidth / 2.0F;
               float barY = (float)sHeight * 0.75F;
               float barCenter = barX + barWidth / 2.0F;
               this.drawHelper.drawTexture(AssetManager.getTexture("GUIPetBarEmpty"), barX, barY, 0.0F, 1.0F, 1.0F, barWidth, barHeight);
               float reputationNormal = Math.min(1.0F, (float)relationshipEntry.getReputation() / (float)creatureInfo.getTamingReputation());
               String barFillTexture = "GUIPetBarRespawn";
               if (relationshipEntry.getReputation() >= creatureInfo.getFriendlyReputation()) {
                  barFillTexture = "GUIPetBarHealth";
               }

               this.drawHelper.drawTexture(AssetManager.getTexture(barFillTexture), barX, barY, 0.0F, reputationNormal, 1.0F, barWidth * reputationNormal, barHeight);
               String reputationText = LanguageManager.translate("entity.reputation") + ": " + relationshipEntry.getReputation() + "/"
                  + creatureInfo.getTamingReputation();
               this.drawHelper.drawString(reputationText, Math.round(barCenter - (float)this.drawHelper.getStringWidth(reputationText) / 2.0F),
                  Math.round(barY + 2.0F), 16777215);
            }
         }
      }

      RenderSystem.disableBlend();
      graphics.pose().popPose();
   }

   /** 1.12 RenderGameOverlayEvent.Text with the "Debug Overlay" config. */
   @SubscribeEvent
   public void onGameOverlay(CustomizeGuiOverlayEvent.DebugText event) {
      if (LycanitesMobs.config.getBool("Debug", "Overlay", false)) {
         if (this.minecraft.hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof BaseCreatureEntity mouseOverCreature) {
            java.util.List<String> left = event.getLeft();
            left.add("");
            left.add("Target Creature: " + mouseOverCreature.getName().getString());
            left.add("Distance To player: " + mouseOverCreature.distanceTo(this.minecraft.player));
            left.add("Subspecies: " + mouseOverCreature.getSubspeciesIndex());
            left.add("Variant: " + mouseOverCreature.getVariantIndex());
            left.add("Level: " + mouseOverCreature.getLevel());
            left.add("Experience: " + mouseOverCreature.getExperience() + "/" + mouseOverCreature.creatureStats.getExperienceForNextLevel());
            left.add("Size: " + mouseOverCreature.sizeScale);
            left.add("");
            left.add("Health: " + mouseOverCreature.getHealth() + "/" + mouseOverCreature.getMaxHealth() + " Fresh: " + mouseOverCreature.creatureStats.getHealth());
            left.add("Speed: " + mouseOverCreature.getAttributeValue(Attributes.MOVEMENT_SPEED) + "/" + mouseOverCreature.creatureStats.getSpeed());
            left.add("");
            left.add("Defense: " + mouseOverCreature.creatureStats.getDefense());
            left.add("Armor: " + mouseOverCreature.getArmorValue());
            left.add("");
            left.add("Damage: " + mouseOverCreature.creatureStats.getDamage());
            left.add("Melee Speed: " + mouseOverCreature.creatureStats.getAttackSpeed());
            left.add("Ranged Speed: " + mouseOverCreature.creatureStats.getRangedSpeed());
            left.add("Pierce: " + mouseOverCreature.creatureStats.getPierce());
            left.add("");
            left.add("Has Attack Target: " + mouseOverCreature.hasAttackTarget());
            left.add("Has Master Target: " + mouseOverCreature.hasMaster());
            left.add("Has Parent Target: " + mouseOverCreature.hasParent());
            CreatureRelationshipEntry relationshipEntry = mouseOverCreature.relationships.getEntry(this.minecraft.player);
            left.add("Reputation with Player: " + (relationshipEntry != null ? relationshipEntry.getReputation() : 0) + "/"
               + mouseOverCreature.creatureInfo.getTamingReputation());
            if (mouseOverCreature instanceof TameableCreatureEntity mouseOverTameable) {
               left.add("");
               left.add("Owner ID: " + (mouseOverTameable.getOwnerUUID() != null ? mouseOverTameable.getOwnerUUID().toString() : "None"));
               left.add("Owner Name: " + mouseOverTameable.getOwnerName());
            }
         }
      }
   }
}
