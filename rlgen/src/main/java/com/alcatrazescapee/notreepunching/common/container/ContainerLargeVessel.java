package com.alcatrazescapee.notreepunching.common.container;

import com.alcatrazescapee.alcatrazcore.inventory.container.ContainerTileInventory;
import com.alcatrazescapee.alcatrazcore.inventory.slot.SlotTileCore;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.common.tile.TileLargeVessel;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

/** 1.12 ContainerLargeVessel: 3x3 slots at (62 + 18i, 20 + 18j), slot i + 3j. */
public class ContainerLargeVessel extends ContainerTileInventory<TileLargeVessel> {
   public ContainerLargeVessel(int windowId, Inventory playerInv, TileLargeVessel tile) {
      super(NoTreePunching.LARGE_VESSEL_MENU.get(), windowId, playerInv, tile);
   }

   @Override
   protected void addContainerSlots() {
      IItemHandler inventory = this.tile.getInventory();
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            this.addSlot(new SlotTileCore(inventory, i + 3 * j, 62 + i * 18, 20 + j * 18, this.tile));
         }
      }
   }
}
