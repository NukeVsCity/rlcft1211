package com.nukevscity.rlgen;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.GrassColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/** Client-only: the vilespine model is tinted with the grass colour (tinted_cross), like in Defiled Lands. */
final class RLGenClient {
   private RLGenClient() {
   }

   static void init(IEventBus modBus) {
      modBus.addListener((RegisterColorHandlersEvent.Block e) -> e.register(
         (state, level, pos, tint) -> level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : GrassColor.getDefaultColor(),
         RLGen.VILESPINE.get()));
   }
}
