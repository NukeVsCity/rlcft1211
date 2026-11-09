package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.entity.AirWaterEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** ClientProxy.render(): renderers and screens of the air and water mobs. */
public final class AirWaterRenderers {
   private AirWaterRenderers() {
   }

   public static void register(IEventBus modBus) {
      modBus.addListener(AirWaterRenderers::renderers);
      modBus.addListener(AirWaterRenderers::screens);
   }

   private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(AirWaterEntities.HIPPOGRYPH.get(), NoopRenderer::new);
      event.registerEntityRenderer(AirWaterEntities.HIPPOGRYPH_EGG.get(), ThrownItemRenderer::new);
   }

   private static void screens(RegisterMenuScreensEvent event) {
   }
}
