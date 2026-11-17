package com.alcatrazescapee.notreepunching.common.container;

import com.alcatrazescapee.alcatrazcore.inventory.container.ContainerItemStack;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.common.items.ItemSmallVessel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * 1.12 ContainerSmallVessel: the 4 slots of the held ceramic small vessel at (71, 23) (89, 23) (71, 41) (89, 41). The held vessel
 * cannot be clicked, thrown or swapped with its own hotbar key. 1.12 kept the items in the stack's ItemHandlerItem capability; here
 * they live in the stack's container component (ItemSmallVessel.getHandler), written back on every change.
 */
public class ContainerSmallVessel extends ContainerItemStack {
   private int itemDragIndex;

   public ContainerSmallVessel(int windowId, Inventory playerInv, ItemStack stack) {
      super(NoTreePunching.SMALL_VESSEL_MENU.get(), windowId, playerInv, stack, 4);
      this.itemDragIndex = playerInv.selected;
   }

   @Override
   protected void addContainerSlots() {
      ItemStackHandler cap = ItemSmallVessel.getHandler(this.stack);
      this.addSlot(new SlotItemHandler(cap, 0, 71, 23));
      this.addSlot(new SlotItemHandler(cap, 1, 89, 23));
      this.addSlot(new SlotItemHandler(cap, 2, 71, 41));
      this.addSlot(new SlotItemHandler(cap, 3, 89, 41));
   }

   @Override
   public void clicked(int slotID, int dragType, ClickType clickType, Player player) {
      if (slotID == this.itemIndex
         && (clickType == ClickType.QUICK_MOVE || clickType == ClickType.PICKUP || clickType == ClickType.THROW || clickType == ClickType.SWAP)) {
         return;
      }
      if (dragType == this.itemDragIndex && clickType == ClickType.SWAP) {
         return;
      }
      super.clicked(slotID, dragType, clickType, player);
   }

   /** 1.21: the held stack the GUI was opened for (main hand if it is a small vessel, else off hand, like ModGuiHandler) */
   public static ItemStack heldVessel(Player player) {
      ItemStack stack = player.getMainHandItem();
      if (!(stack.getItem() instanceof ItemSmallVessel)) {
         stack = player.getOffhandItem();
      }
      return stack;
   }
}
