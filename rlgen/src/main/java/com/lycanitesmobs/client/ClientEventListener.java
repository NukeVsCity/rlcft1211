package com.lycanitesmobs.client;

import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.item.special.ItemSoulgazer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** 1.12 ClientEventListener (registered on the NeoForge bus by ClientProxy). */
public class ClientEventListener {
   public static int ITEM_RENDER_MODE = 0;

   @SubscribeEvent
   public void onRenderHand(RenderHandEvent event) {
      ITEM_RENDER_MODE = 1;
   }

   /** 1.12 RenderGameOverlayEvent (EXPERIENCE): items drawn from here on are GUI items. */
   @SubscribeEvent
   public void onRenderGui(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre event) {
      if (net.neoforged.neoforge.client.gui.VanillaGuiLayers.EXPERIENCE_BAR.equals(event.getName())) {
         ITEM_RENDER_MODE = 2;
      }
   }

   /** 1.12 RenderWorldLastEvent. */
   @SubscribeEvent
   public void onRenderWorldLast(RenderLevelStageEvent event) {
      if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
         ITEM_RENDER_MODE = 1;
      }
   }

   /**
    * 1.12 FogDensity in lava: density 0.5 (exponential fog) when the entity is not burning or has fire resistance.
    * 1.21 only has linear fog: an exponential density of 0.5 hides things at about 6 blocks, so the fog ends there.
    */
   @SubscribeEvent
   public void onFogDensity(ViewportEvent.RenderFog event) {
      if (event.getCamera().getEntity() instanceof LivingEntity entityLiving && event.getType() == FogType.LAVA) {
         if (!entityLiving.isOnFire() || entityLiving.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            event.setNearPlaneDistance(0.0F);
            event.setFarPlaneDistance(6.0F);
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public void onBlockOverlay(RenderBlockScreenEffectEvent event) {
      if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE
         && (!event.getPlayer().isOnFire() || event.getPlayer().hasEffect(MobEffects.FIRE_RESISTANCE))) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onItemTooltip(ItemTooltipEvent event) {
      int sharpness = ItemManager.getInstance().getEquipmentSharpnessRepair(event.getItemStack());
      int mana = ItemManager.getInstance().getEquipmentManaRepair(event.getItemStack());
      if (sharpness > 0 || mana > 0) {
         event.getToolTip().add(Component.literal(LanguageManager.translate("equipment.item.repair")));
         if (sharpness > 0) {
            event.getToolTip().add(Component.literal(LanguageManager.translate("equipment.sharpness") + " " + sharpness));
         }

         if (mana > 0) {
            event.getToolTip().add(Component.literal(LanguageManager.translate("equipment.mana") + " " + mana));
         }
      }

      if (event.getItemStack().getItem() instanceof ItemSoulgazer && event.getEntity() != null) {
         ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(event.getEntity());
         if (extendedPlayer != null && extendedPlayer.creatureStudyCooldown > 0) {
            event.getToolTip()
               .add(Component.literal(LanguageManager.translate("message.beastiary.study.cooldown")
                  + " " + String.format("%.0f", (float)extendedPlayer.creatureStudyCooldown / 20.0F) + "s"));
         }
      }
   }
}
