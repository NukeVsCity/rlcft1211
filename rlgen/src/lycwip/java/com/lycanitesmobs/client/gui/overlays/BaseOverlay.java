package com.lycanitesmobs.client.gui.overlays;

import com.lycanitesmobs.client.renderer.LycGL;
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.client.KeyHandler;
import com.lycanitesmobs.client.gui.BaseGui;
import com.lycanitesmobs.client.gui.DrawHelper;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.CreatureRelationshipEntry;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.item.temp.ItemStaffSummoning;
import com.lycanitesmobs.core.mobevent.MobEventPlayerClient;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Text;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;

public class BaseOverlay extends BaseGui {
   public DrawHelper drawHelper;
   public Minecraft minecraft;
   protected ScaledResolution scaledResolution;
   private int mountMessageTimeMax = 200;
   private int mountMessageTime = 0;

   public BaseOverlay(Minecraft minecraft) {
      this.minecraft = minecraft;
      this.drawHelper = new DrawHelper(minecraft, LycanitesMobs.proxy.getFontRenderer());
      this.scaledResolution = new ScaledResolution(this.minecraft);
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public void onRenderExperienceBar(RenderGameOverlayEvent event) {
      if (LycanitesMobs.proxy.getClientPlayer() != null) {
         Player player = LycanitesMobs.proxy.getClientPlayer();
         if (!event.isCancelable() && event.getType() == ElementType.EXPERIENCE) {
            LegacyRender.pushMatrix();
            LycGL.disableLighting();
            LycGL.enableBlend();
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            LegacyRender.color(1.0F, 1.0F, 1.0F, 1.0F);
            ScaledResolution scaledresolution = new ScaledResolution(this.minecraft);
            int sWidth = scaledresolution.getScaledWidth();
            int sHeight = scaledresolution.getScaledHeight();
            ExtendedWorld worldExt = ExtendedWorld.getForWorld(player.getEntityWorld());
            if (worldExt != null) {
               for (MobEventPlayerClient mobEventPlayerClient : worldExt.clientMobEventPlayers.values()) {
                  LegacyRender.pushMatrix();
                  mobEventPlayerClient.onGUIUpdate(this, sWidth, sHeight);
                  LegacyRender.popMatrix();
               }

               if (worldExt.clientWorldEventPlayer != null) {
                  LegacyRender.pushMatrix();
                  worldExt.clientWorldEventPlayer.onGUIUpdate(this, sWidth, sHeight);
                  LegacyRender.popMatrix();
               }
            }

            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
            if (playerExt != null
               && !this.minecraft.player.capabilities.isCreativeMode
               && (
                  this.minecraft.player.getHeldItem(EnumHand.MAIN_HAND) != null
                        && this.minecraft.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemStaffSummoning
                     || this.minecraft.player.getHeldItem(EnumHand.OFF_HAND) != null
                        && this.minecraft.player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemStaffSummoning
               )) {
               GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
               this.minecraft.getTextureManager().bindTexture(AssetManager.getTexture("GUIInventoryCreature"));
               int barYSpace = 10;
               int barXSpace = -1;
               int summonBarWidth = 9;
               int summonBarHeight = 9;
               int summonBarX = sWidth / 2 + 10;
               int summonBarY = sHeight - 30 - summonBarHeight;
               int summonBarU = 256 - summonBarWidth;
               int summonBarV = 256 - summonBarHeight;
               summonBarY -= barYSpace;
               if (this.minecraft.player.isInsideOfMaterial(Material.WATER)) {
                  summonBarY -= barYSpace;
               }

               for (int summonBarEnergyN = 0; summonBarEnergyN < 10; summonBarEnergyN++) {
                  this.drawTexturedModalRect(
                     summonBarX + (summonBarWidth + barXSpace) * summonBarEnergyN, summonBarY, summonBarU, summonBarV, summonBarWidth, summonBarHeight
                  );
                  if (playerExt.summonFocus >= playerExt.summonFocusMax - summonBarEnergyN * playerExt.summonFocusCharge) {
                     this.drawTexturedModalRect(
                        summonBarX + (summonBarWidth + barXSpace) * summonBarEnergyN,
                        summonBarY,
                        summonBarU - summonBarWidth,
                        summonBarV,
                        summonBarWidth,
                        summonBarHeight
                     );
                  } else if (playerExt.summonFocus + playerExt.summonFocusCharge > playerExt.summonFocusMax - summonBarEnergyN * playerExt.summonFocusCharge) {
                     float summonChargeScale = (float)(playerExt.summonFocus % playerExt.summonFocusCharge) / (float)playerExt.summonFocusCharge;
                     this.drawTexturedModalRect(
                        summonBarX + (summonBarWidth + barXSpace) * summonBarEnergyN + (summonBarWidth - Math.round((float)summonBarWidth * summonChargeScale)),
                        summonBarY,
                        summonBarU - Math.round((float)summonBarWidth * summonChargeScale),
                        summonBarV,
                        Math.round((float)summonBarWidth * summonChargeScale),
                        summonBarHeight
                     );
                  }
               }
            }

            if (this.minecraft.player.getRidingEntity() != null && this.minecraft.player.getRidingEntity() instanceof RideableCreatureEntity) {
               RideableCreatureEntity mount = (RideableCreatureEntity)this.minecraft.player.getRidingEntity();
               float mountStamina = mount.getStaminaPercent();
               if (this.mountMessageTime > 0) {
                  String mountMessage = LanguageManager.translate("gui.mount.controls");
                  mountMessage = mountMessage.replace("%controlA%", GameSettings.getKeyDisplayString(KeyHandler.instance.mountAbility.getKeyCode()));
                  mountMessage = mountMessage.replace("%controlB%", GameSettings.getKeyDisplayString(KeyHandler.instance.dismount.getKeyCode()));
                  this.minecraft.ingameGUI.setOverlayMessage(mountMessage, false);
               }

               GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
               this.minecraft.getTextureManager().bindTexture(ICONS);
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
               this.drawTexturedModalRect(staminaBarX, staminaBarY, 0, staminaTextureY, staminaBarWidth, staminaBarHeight);
               if (staminaEnergyWidth > 0) {
                  this.drawTexturedModalRect(staminaBarX, staminaBarY, 0, staminaEnergyY, staminaEnergyWidth, staminaBarHeight);
               }

               if (this.mountMessageTime > 0) {
                  this.mountMessageTime--;
               }
            } else {
               this.mountMessageTime = this.mountMessageTimeMax;
            }

            RayTraceResult mouseOver = Minecraft.getInstance().objectMouseOver;
            if (mouseOver != null) {
               Entity mouseOverEntity = mouseOver.entityHit;
               if (mouseOverEntity != null && mouseOverEntity instanceof BaseCreatureEntity) {
                  BaseCreatureEntity creatureEntity = (BaseCreatureEntity)mouseOverEntity;
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

                     this.drawHelper
                        .drawTexture(AssetManager.getTexture(barFillTexture), barX, barY, 0.0F, reputationNormal, 1.0F, barWidth * reputationNormal, barHeight);
                     String reputationText = LanguageManager.translate("entity.reputation")
                        + ": "
                        + relationshipEntry.getReputation()
                        + "/"
                        + creatureInfo.getTamingReputation();
                     this.drawHelper
                        .drawString(
                           reputationText,
                           Math.round(barCenter - (float)this.drawHelper.getStringWidth(reputationText) / 2.0F),
                           Math.round(barY + 2.0F),
                           16777215
                        );
                  }
               }
            }

            LegacyRender.popMatrix();
            this.minecraft.getTextureManager().bindTexture(ICONS);
         }
      }
   }

      @SubscribeEvent
   public void onGameOverlay(Text event) {
      if (LycanitesMobs.config.getBool("Debug", "Overlay", false)) {
         RayTraceResult mouseOver = Minecraft.getInstance().objectMouseOver;
         if (mouseOver != null) {
            Entity mouseOverEntity = mouseOver.entityHit;
            if (mouseOverEntity != null && mouseOverEntity instanceof BaseCreatureEntity) {
               BaseCreatureEntity mouseOverCreature = (BaseCreatureEntity)mouseOverEntity;
               event.getLeft().add("");
               event.getLeft().add("Target Creature: " + mouseOverCreature.getName());
               event.getLeft().add("Distance To player: " + mouseOverCreature.getDistance(Minecraft.getInstance().player));
               event.getLeft().add("Elements: " + mouseOverCreature.creatureInfo.getElementNames(mouseOverCreature.getSubspecies()));
               event.getLeft().add("Subspecies: " + mouseOverCreature.getSubspeciesIndex());
               event.getLeft().add("Variant: " + mouseOverCreature.getVariantIndex());
               event.getLeft().add("Level: " + mouseOverCreature.getLevel());
               event.getLeft().add("Experience: " + mouseOverCreature.getExperience() + "/" + mouseOverCreature.creatureStats.getExperienceForNextLevel());
               event.getLeft().add("Size: " + mouseOverCreature.sizeScale);
               event.getLeft().add("");
               event.getLeft()
                  .add(
                     "Health: "
                        + mouseOverCreature.getHealth()
                        + "/"
                        + mouseOverCreature.getMaxHealth()
                        + " Fresh: "
                        + mouseOverCreature.creatureStats.getHealth()
                  );
               event.getLeft()
                  .add(
                     "Speed: "
                        + mouseOverCreature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()
                        + "/"
                        + mouseOverCreature.creatureStats.getSpeed()
                  );
               event.getLeft().add("");
               event.getLeft().add("Defense: " + mouseOverCreature.creatureStats.getDefense());
               event.getLeft().add("Armor: " + mouseOverCreature.getTotalArmorValue());
               event.getLeft().add("");
               event.getLeft().add("Damage: " + mouseOverCreature.creatureStats.getDamage());
               event.getLeft().add("Melee Speed: " + mouseOverCreature.creatureStats.getAttackSpeed());
               event.getLeft().add("Melee Range: " + mouseOverCreature.getPhysicalRange());
               event.getLeft().add("Ranged Speed: " + mouseOverCreature.creatureStats.getRangedSpeed());
               event.getLeft().add("Pierce: " + mouseOverCreature.creatureStats.getPierce());
               event.getLeft().add("");
               event.getLeft().add("Effect Duration: " + mouseOverCreature.creatureStats.getEffect() + " Base Seconds");
               event.getLeft().add("Effect Amplifier: x" + mouseOverCreature.creatureStats.getAmplifier());
               event.getLeft().add("");
               event.getLeft().add("Has Attack Target: " + mouseOverCreature.hasAttackTarget());
               event.getLeft().add("Has Avoid Target: " + mouseOverCreature.hasAvoidTarget());
               event.getLeft().add("Has Master Target: " + mouseOverCreature.hasMaster());
               event.getLeft().add("Has Parent Target: " + mouseOverCreature.hasParent());
               event.getLeft().add("");
               CreatureRelationshipEntry relationshipEntry = mouseOverCreature.relationships.getEntry(this.minecraft.player);
               event.getLeft()
                  .add(
                     "Reputation with Player: "
                        + (relationshipEntry != null ? relationshipEntry.getReputation() : 0)
                        + "/"
                        + mouseOverCreature.creatureInfo.getTamingReputation()
                  );
               if (mouseOverEntity instanceof TameableCreatureEntity) {
                  TameableCreatureEntity mouseOverTameable = (TameableCreatureEntity)mouseOverCreature;
                  event.getLeft().add("");
                  event.getLeft().add("Owner ID: " + (mouseOverTameable.getOwnerId() != null ? mouseOverTameable.getOwnerId().toString() : "None"));
                  event.getLeft().add("Owner Name: " + mouseOverTameable.getOwnerName());
               }
            }
         }
      }
   }
}
