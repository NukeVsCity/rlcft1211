package com.alcatrazescapee.notreepunching.common.container;

import com.alcatrazescapee.alcatrazcore.inventory.container.ContainerTileInventory;
import com.alcatrazescapee.alcatrazcore.inventory.slot.SlotOutput;
import com.alcatrazescapee.alcatrazcore.inventory.slot.SlotTileCore;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.common.tile.TileFirePit;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

/** 1.12 ContainerFirePit: fuel (80, 59), input (52, 23), output (108, 23). */
public class ContainerFirePit extends ContainerTileInventory<TileFirePit> {
   public ContainerFirePit(int windowId, Inventory playerInv, TileFirePit tile) {
      super(NoTreePunching.FIRE_PIT_MENU.get(), windowId, playerInv, tile);
   }

   @Override
   protected void addContainerSlots() {
      IItemHandler inventory = this.tile.getInventory();
      this.addSlot(new SlotTileCore(inventory, TileFirePit.FUEL_SLOT_ID, 80, 59, this.tile));
      this.addSlot(new SlotTileCore(inventory, TileFirePit.INPUT_SLOT_ID, 52, 23, this.tile));
      this.addSlot(new SlotOutput(inventory, TileFirePit.OUTPUT_SLOT_ID, 108, 23));
   }
}
