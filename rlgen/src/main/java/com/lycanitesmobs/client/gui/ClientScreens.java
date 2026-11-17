package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.GuiHandler;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.beastiary.CreaturesBeastiaryScreen;
import com.lycanitesmobs.client.gui.beastiary.ElementsBeastiaryScreen;
import com.lycanitesmobs.client.gui.beastiary.IndexBeastiaryScreen;
import com.lycanitesmobs.client.gui.beastiary.PetsBeastiaryScreen;
import com.lycanitesmobs.client.gui.beastiary.SummoningBeastiaryScreen;
import com.lycanitesmobs.client.gui.overlays.MinionSelectionOverlay;
import com.lycanitesmobs.core.container.CreatureContainer;
import com.lycanitesmobs.core.container.EquipmentForgeContainer;
import com.lycanitesmobs.core.container.EquipmentInfuserContainer;
import com.lycanitesmobs.core.container.EquipmentStationContainer;
import com.lycanitesmobs.core.container.SummoningPedestalContainer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * 1.12 GuiHandler.getClientGuiElement. The server GUIs are menus of GuiHandler.MENU: their screen is picked from the
 * container class (1.12 built the screen from the tile entity / creature with the same container inside); the client-only
 * GUIs (beastiary pages, minion selection) are opened directly.
 */
@EventBusSubscriber(modid = LycanitesMobs.modid, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientScreens {
   private ClientScreens() {
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   @SubscribeEvent
   public static void registerScreens(RegisterMenuScreensEvent event) {
      net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor constructor = (menu, inventory, title) -> containerScreen((AbstractContainerMenu)menu, inventory, title);
      event.register((net.minecraft.world.inventory.MenuType)GuiHandler.MENU.get(), constructor);
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private static <M extends AbstractContainerMenu> AbstractContainerScreen containerScreen(AbstractContainerMenu menu, Inventory inventory, Component title) {
      if (menu instanceof CreatureContainer c) {
         return new CreatureInventoryScreen(c, inventory);
      }
      if (menu instanceof EquipmentForgeContainer c) {
         return new EquipmentForgeScreen(c, inventory);
      }
      if (menu instanceof EquipmentInfuserContainer c) {
         return new EquipmentInfuserScreen(c, inventory);
      }
      if (menu instanceof EquipmentStationContainer c) {
         return new EquipmentStationScreen(c, inventory);
      }
      if (menu instanceof SummoningPedestalContainer c) {
         return new SummoningPedestalScreen(c, inventory);
      }
      throw new IllegalStateException("[LycanitesMobs] No screen for the container " + menu);
   }

   public static Screen clientGuiElement(int id, Player player, int x) {
      if (id == GuiHandler.GuiType.BEASTIARY.id) {
         if (x == GuiHandler.Beastiary.INDEX.id) {
            return new IndexBeastiaryScreen(player);
         }
         if (x == GuiHandler.Beastiary.CREATURES.id) {
            return new CreaturesBeastiaryScreen(player);
         }
         if (x == GuiHandler.Beastiary.PETS.id) {
            return new PetsBeastiaryScreen(player);
         }
         if (x == GuiHandler.Beastiary.SUMMONING.id) {
            return new SummoningBeastiaryScreen(player);
         }
         if (x == GuiHandler.Beastiary.ELEMENTS.id) {
            return new ElementsBeastiaryScreen(player);
         }
      } else if (id == GuiHandler.GuiType.PLAYER.id && x == GuiHandler.PlayerGuiType.MINION_SELECTION.id) {
         return new MinionSelectionOverlay(player);
      }
      return null;
   }

   public static void open(String screen, Player player) {
      switch (screen) {
         case "summoning_beastiary" -> SummoningBeastiaryScreen.openToPlayer(player);
         case "creatures_beastiary" -> CreaturesBeastiaryScreen.openToPlayer(player);
         case "pets_beastiary" -> PetsBeastiaryScreen.openToPlayer(player);
         case "index_beastiary" -> IndexBeastiaryScreen.openToPlayer(player);
         case "minion_selection" -> MinionSelectionOverlay.openToPlayer(player);
         default -> LycanitesMobs.logWarning("", "Unknown client screen " + screen);
      }
   }
}
