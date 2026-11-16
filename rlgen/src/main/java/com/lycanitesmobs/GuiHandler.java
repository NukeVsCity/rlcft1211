package com.lycanitesmobs;

import com.lycanitesmobs.core.container.CreatureContainer;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.tileentity.TileEntityBase;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 1.12 GuiHandler (IGuiHandler with GuiType ids). Server GUIs are 1.21 menus of one menu type (lycanitesmobs:gui) whose
 * open packet carries the 1.12 (id, x, y, z); both sides build the same container from it (getServerGuiElement).
 * The client screens for these menus and the client-only GUIs (beastiary, minion selection) belong to lyc/client:
 * it registers a screen factory for {@link #MENU} that picks the screen from the container class.
 */
public class GuiHandler {
   public static GuiHandler instance = new GuiHandler();
   private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, LycanitesMobs.modid);
   public static final Supplier<MenuType<AbstractContainerMenu>> MENU = MENUS.register("gui",
      () -> IMenuTypeExtension.create((windowId, inventory, buf) -> GuiHandler.clientContainer(windowId, inventory, buf)));
   /** Window id for the container being constructed (BaseContainer reads it, 1.12 containers had none). */
   public static final ThreadLocal<Integer> WINDOW_ID = ThreadLocal.withInitial(() -> 0);
   /** 1.12 GuiType of the container being constructed. */
   public static final ThreadLocal<int[]> GUI_ARGS = ThreadLocal.withInitial(() -> new int[4]);

   public static void init(IEventBus modBus) {
      MENUS.register(modBus);
   }

   /** 1.12 player.openGui(LycanitesMobs.instance, id, world, x, y, z). Client side: lyc/client opens client-only GUIs. */
   public static void openGui(Player player, int id, Level world, int x, int y, int z) {
      if (!(player instanceof ServerPlayer serverPlayer)) {
         LycanitesMobs.proxy.openClientGui(player, id, world, x, y, z);
         return;
      }
      if (instance.getServerGuiElement(id, player, world, x, y, z, 0) == null) {
         return;
      }
      serverPlayer.openMenu(new SimpleMenuProvider((windowId, inventory, p) -> {
         Object container = instance.getServerGuiElement(id, p, world, x, y, z, windowId);
         return container instanceof AbstractContainerMenu menu ? menu : null;
      }, Component.literal("")), buf -> {
         buf.writeByte(id);
         buf.writeInt(x);
         buf.writeInt(y);
         buf.writeInt(z);
      });
   }

   private static AbstractContainerMenu clientContainer(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
      int id = buf.readByte();
      int x = buf.readInt();
      int y = buf.readInt();
      int z = buf.readInt();
      Object container = instance.getServerGuiElement(id, inventory.player, inventory.player.level(), x, y, z, windowId);
      return container instanceof AbstractContainerMenu menu ? menu : null;
   }

   /** 1.12 getServerGuiElement (the container), built with the given window id. */
   public Object getServerGuiElement(int id, Player player, Level world, int x, int y, int z, int windowId) {
      WINDOW_ID.set(windowId);
      GUI_ARGS.set(new int[]{id, x, y, z});
      try {
         if (id == GuiHandler.GuiType.TILEENTITY.id) {
            BlockEntity tileEntity = world.getBlockEntity(new BlockPos(x, y, z));
            if (tileEntity instanceof TileEntityBase) {
               return ((TileEntityBase)tileEntity).getGUI(player);
            }
         } else if (id == GuiHandler.GuiType.ENTITY.id) {
            Entity entity = world.getEntity(x);
            if (entity instanceof BaseCreatureEntity) {
               return new CreatureContainer((BaseCreatureEntity)entity, player.getInventory());
            }
         }
         return null;
      } finally {
         WINDOW_ID.set(0);
      }
   }

   public static enum Beastiary {
      INDEX((byte)0),
      CREATURES((byte)1),
      PETS((byte)2),
      SUMMONING((byte)3),
      ELEMENTS((byte)4);

      public byte id;

      private Beastiary(byte i) {
         this.id = i;
      }
   }

   public static enum GuiType {
      TILEENTITY((byte)0),
      ENTITY((byte)1),
      ITEM((byte)2),
      BEASTIARY((byte)3),
      PLAYER((byte)4);

      public byte id;

      private GuiType(byte i) {
         this.id = i;
      }
   }

   public static enum PlayerGuiType {
      MINION_SELECTION((byte)0);

      public byte id;

      private PlayerGuiType(byte i) {
         this.id = i;
      }
   }
}
