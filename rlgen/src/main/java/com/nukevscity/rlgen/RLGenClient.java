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
      modBus.addListener((net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent e) -> {
         for (com.nukevscity.rlgen.lyc.LycFluids.Kind k : com.nukevscity.rlgen.lyc.LycFluids.ALL) {
            net.minecraft.resources.ResourceLocation still = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(RLGen.MODID, "block/" + k.name + "_still");
            net.minecraft.resources.ResourceLocation flow = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(RLGen.MODID, "block/" + k.name + "_flow");
            e.registerFluidType(new net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions() {
               @Override
               public net.minecraft.resources.ResourceLocation getStillTexture() {
                  return still;
               }

               @Override
               public net.minecraft.resources.ResourceLocation getFlowingTexture() {
                  return flow;
               }
            }, k.type.get());
         }
      });
      modBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e) -> {
         for (com.nukevscity.rlgen.lyc.LycFluids.Kind k : com.nukevscity.rlgen.lyc.LycFluids.ALL) {
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(k.source.get(), net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(k.flowing.get(), net.minecraft.client.renderer.RenderType.translucent());
         }
      });
   }
}
