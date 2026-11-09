package com.github.alexthe666.iceandfire.inventory;

import com.github.alexthe666.iceandfire.entity.AirWaterEntities;
import com.github.alexthe666.iceandfire.entity.EntityHippocampus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Ice and Fire 1.7.1 ContainerHippocampus: saddle, chest, armor, 15 chest slots. */
public class ContainerHippocampus extends AbstractContainerMenu {
   private final Container hippocampusInventory;
   public final EntityHippocampus hippocampus;

   public static void open(Player player, EntityHippocampus hippocampus) {
      player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ContainerHippocampus(id, inv, hippocampus), hippocampus.getName()),
         buf -> buf.writeInt(hippocampus.getId()));
   }

   public static ContainerHippocampus fromNetwork(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
      return new ContainerHippocampus(id, inv, (EntityHippocampus)inv.player.level().getEntity(buf.readInt()));
   }

   public ContainerHippocampus(int id, Inventory playerInv, EntityHippocampus hippocampus) {
      super(AirWaterEntities.HIPPOCAMPUS_MENU.get(), id);
      this.hippocampusInventory = hippocampus.hippocampusInventory;
      this.hippocampus = hippocampus;
      this.hippocampusInventory.startOpen(playerInv.player);
      this.addSlot(new Slot(this.hippocampusInventory, 0, 8, 18) {
         @Override
         public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.SADDLE) && !this.hasItem();
         }
      });
      this.addSlot(new Slot(this.hippocampusInventory, 1, 8, 36) {
         @Override
         public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.CHEST) && !this.hasItem();
         }

         @Override
         public void setChanged() {
            super.setChanged();
            ContainerHippocampus.this.hippocampus.refreshInventory();
         }
      });
      this.addSlot(new Slot(this.hippocampusInventory, 2, 8, 52) {
         @Override
         public boolean mayPlace(ItemStack stack) {
            return hippocampus.getIntFromArmor(stack) != 0;
         }

         @Override
         public int getMaxStackSize() {
            return 1;
         }
      });
      for (int k = 0; k < 3; k++) {
         for (int l = 0; l < 5; l++) {
            this.addSlot(new Slot(this.hippocampusInventory, 3 + l + k * 5, 80 + l * 18, 18 + k * 18) {
               @Override
               public boolean isActive() {
                  return ContainerHippocampus.this.hippocampus.isChested();
               }

               @Override
               public boolean mayPlace(ItemStack stack) {
                  return ContainerHippocampus.this.hippocampus.isChested();
               }
            });
         }
      }
      for (int i1 = 0; i1 < 3; i1++) {
         for (int k1 = 0; k1 < 9; k1++) {
            this.addSlot(new Slot(playerInv, k1 + i1 * 9 + 9, 8 + k1 * 18, 102 + i1 * 18 - 18));
         }
      }
      for (int j1 = 0; j1 < 9; j1++) {
         this.addSlot(new Slot(playerInv, j1, 8 + j1 * 18, 142));
      }
   }

   @Override
   public boolean stillValid(Player player) {
      return this.hippocampusInventory.stillValid(player) && this.hippocampus.isAlive() && this.hippocampus.distanceTo(player) < 8.0F;
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         int size = this.hippocampusInventory.getContainerSize();
         if (index < size) {
            if (!this.moveItemStackTo(itemstack1, size, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (this.getSlot(2).mayPlace(itemstack1)) {
            if (!this.moveItemStackTo(itemstack1, 2, 3, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.getSlot(1).mayPlace(itemstack1) && !this.getSlot(1).hasItem()) {
            if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.getSlot(0).mayPlace(itemstack1)) {
            if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (size <= 3 || !this.moveItemStackTo(itemstack1, 3, size, false)) {
            return ItemStack.EMPTY;
         }
         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }
      return itemstack;
   }

   @Override
   public void removed(Player player) {
      super.removed(player);
      this.hippocampusInventory.stopOpen(player);
   }
}
