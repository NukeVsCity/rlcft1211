package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.client.render.tile.IceAndFireTEISR;
import com.github.alexthe666.iceandfire.client.render.tile.RenderEggInIce;
import com.github.alexthe666.iceandfire.client.render.tile.RenderJar;
import com.github.alexthe666.iceandfire.client.render.tile.RenderLectern;
import com.github.alexthe666.iceandfire.client.render.tile.RenderPixieHouse;
import com.github.alexthe666.iceandfire.client.render.tile.RenderPodium;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.enums.EnumTroll;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Block entity renderers, the builtin/entity item renderer, armor models (1.12 ClientProxy.render). */
public final class IafCoreClientExtras {
   private IafCoreClientExtras() {
   }

   private static IceAndFireTEISR teisr;

   public static void init(IEventBus modBus) {
      IafArmorModels.init(modBus);
      modBus.addListener(IafCoreClientExtras::renderers);
      modBus.addListener(IafCoreClientExtras::extensions);
   }

   private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer(ModBlocks.LECTERN_TILE.get(), RenderLectern::new);
      event.registerBlockEntityRenderer(ModBlocks.PODIUM_TILE.get(), RenderPodium::new);
      event.registerBlockEntityRenderer(ModBlocks.EGG_IN_ICE_TILE.get(), RenderEggInIce::new);
      event.registerBlockEntityRenderer(ModBlocks.JAR_TILE.get(), RenderJar::new);
      event.registerBlockEntityRenderer(ModBlocks.PIXIE_HOUSE_TILE.get(), RenderPixieHouse::new);
   }

   private static void extensions(RegisterClientExtensionsEvent event) {
      IClientItemExtensions ext = new IClientItemExtensions() {
         @Override
         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (teisr == null) {
               teisr = new IceAndFireTEISR();
            }
            return teisr;
         }
      };
      event.registerItem(ext, ModItems.gorgon_head, ModBlocks.pixieHouse.asItem());
      for (EnumTroll.Weapon w : EnumTroll.Weapon.values()) {
         event.registerItem(ext, w.item);
      }
   }
}
