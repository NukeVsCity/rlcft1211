package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.client.gui.GuiLectern;
import com.github.alexthe666.iceandfire.client.gui.GuiMyrmexCocoon;
import com.github.alexthe666.iceandfire.client.gui.GuiPodium;
import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client side of iaf/core (1.12 ClientProxy item/block parts): menu screens, the "iceandfire:meta" item model property (1.12
 * ModelLoader.setCustomModelResourceLocation per metadata becomes model overrides on it), bow/horn pull properties, block entity
 * renderers, armor models and the bestiary.
 */
public final class IafCoreClient {
   private IafCoreClient() {
   }

   public static final ResourceLocation META = ResourceLocation.fromNamespaceAndPath("iceandfire", "meta");

   public static void init(IEventBus modBus) {
      modBus.addListener(IafCoreClient::screens);
      modBus.addListener(IafCoreClient::setup);
      IafCoreClientExtras.init(modBus);
   }

   private static void screens(RegisterMenuScreensEvent event) {
      event.register(ModBlocks.LECTERN_MENU.get(), GuiLectern::new);
      event.register(ModBlocks.PODIUM_MENU.get(), GuiPodium::new);
      event.register(ModBlocks.MYRMEX_COCOON_MENU.get(), GuiMyrmexCocoon::new);
   }

   private static void setup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         ItemProperties.registerGeneric(META, (stack, level, entity, seed) -> (float)IafMeta.getMetadata(stack));
         for (Item item : new Item[]{ModItems.dragonbone_bow, ModItems.dragon_horn_fire, ModItems.dragon_horn_ice}) {
            ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pull"), (stack, level, entity, seed) -> {
               if (entity == null) {
                  return 0.0F;
               }
               ItemStack itemstack = entity.getUseItem();
               return !itemstack.isEmpty() && itemstack.getItem() == stack.getItem()
                  ? (float)(stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F : 0.0F;
            });
            ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pulling"),
               (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
         }
      });
   }
}
