package com.lycanitesmobs.client;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

/** The event registrations of 1.12 ClientProxy.registerEvents / registerTextures. */
@EventBusSubscriber(modid = LycanitesMobs.modid, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientEvents {
   private ClientEvents() {
   }

   public static void register() {
      new KeyHandler(Minecraft.getInstance());
      NeoForge.EVENT_BUS.register(new com.lycanitesmobs.client.gui.overlays.BaseOverlay(Minecraft.getInstance()));
      NeoForge.EVENT_BUS.register(new ClientEventListener());
   }

   /** 1.12 TabManager.registerTab(new MainTab(0)). */
   public static void registerTabs() {
      com.lycanitesmobs.client.gui.buttons.TabManager.registerTab(new com.lycanitesmobs.client.gui.buttons.MainTab(0));
   }

   @SubscribeEvent
   public static void registerKeys(RegisterKeyMappingsEvent event) {
      if (KeyHandler.instance != null) {
         KeyHandler.instance.registerKeys(event);
      }
   }

   /** 1.12 ClientProxy.addItemRender: ForgeHooksClient.registerTESRItemStack for equipment parts and equipment. */
   @SubscribeEvent
   public static void registerItemRenderers(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
      java.util.function.Supplier<com.lycanitesmobs.client.renderer.EquipmentPartRenderer> partRenderer =
         com.google.common.base.Suppliers.memoize(com.lycanitesmobs.client.renderer.EquipmentPartRenderer::new);
      java.util.function.Supplier<com.lycanitesmobs.client.renderer.EquipmentRenderer> equipmentRenderer =
         com.google.common.base.Suppliers.memoize(com.lycanitesmobs.client.renderer.EquipmentRenderer::new);
      for (net.minecraft.world.item.Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
         if (item instanceof com.lycanitesmobs.core.item.equipment.ItemEquipmentPart) {
            event.registerItem(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
               @Override
               public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                  return partRenderer.get();
               }
            }, item);
         } else if (item instanceof com.lycanitesmobs.core.item.equipment.ItemEquipment) {
            event.registerItem(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
               @Override
               public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                  return equipmentRenderer.get();
               }
            }, item);
         }
      }
   }
}
