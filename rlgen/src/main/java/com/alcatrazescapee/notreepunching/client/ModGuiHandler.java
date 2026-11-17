package com.alcatrazescapee.notreepunching.client;

import com.alcatrazescapee.alcatrazcore.client.gui.GuiContainerCore;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.client.gui.GuiFirePit;
import com.alcatrazescapee.notreepunching.common.container.ContainerFirePit;
import com.alcatrazescapee.notreepunching.common.container.ContainerLargeVessel;
import com.alcatrazescapee.notreepunching.common.container.ContainerSmallVessel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** 1.12 ModGuiHandler client side: the large vessel, small vessel and fire pit screens with their 1.12 backgrounds. */
@EventBusSubscriber(modid = NoTreePunching.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ModGuiHandler {
   private static final ResourceLocation LARGE_VESSEL_BACKGROUND = ResourceLocation.fromNamespaceAndPath(NoTreePunching.MOD_ID, "textures/gui/large_vessel.png");
   private static final ResourceLocation SMALL_VESSEL_BACKGROUND = ResourceLocation.fromNamespaceAndPath(NoTreePunching.MOD_ID, "textures/gui/small_vessel.png");
   private static final ResourceLocation FIRE_PIT_BACKGROUND = ResourceLocation.fromNamespaceAndPath(NoTreePunching.MOD_ID, "textures/gui/fire_pit.png");

   private ModGuiHandler() {
   }

   @SubscribeEvent
   public static void registerScreens(RegisterMenuScreensEvent event) {
      event.<ContainerLargeVessel, GuiContainerCore<ContainerLargeVessel>>register(NoTreePunching.LARGE_VESSEL_MENU.get(), (menu, inv, title) -> new GuiContainerCore<>(menu, inv, title, LARGE_VESSEL_BACKGROUND));
      event.<ContainerSmallVessel, GuiContainerCore<ContainerSmallVessel>>register(NoTreePunching.SMALL_VESSEL_MENU.get(), (menu, inv, title) -> new GuiContainerCore<>(menu, inv, title, SMALL_VESSEL_BACKGROUND));
      event.<ContainerFirePit, GuiFirePit>register(NoTreePunching.FIRE_PIT_MENU.get(), (menu, inv, title) -> new GuiFirePit(menu, inv, title, FIRE_PIT_BACKGROUND));
   }
}
