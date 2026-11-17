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
}
