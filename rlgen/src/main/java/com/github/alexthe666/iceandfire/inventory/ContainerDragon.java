package com.github.alexthe666.iceandfire.inventory;

import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.entity.DragonCompat;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafDragonEntities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Ice and Fire 1.7.1 ContainerDragon: the four armor slots (head, neck at the left; body, tail at the right) and the player
 * inventory, at the 1.12 positions. The client opens it with the dragon's entity id (GuiDragon is drawn by the client part).
 */
public class ContainerDragon extends AbstractContainerMenu {
   private Container dragonInv;
   private EntityDragonBase dragon;

   public ContainerDragon(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
      this(id, playerInv, lookup(playerInv.player, buf.readInt()));
   }

   private static EntityDragonBase lookup(Player player, int entityId) {
      Entity entity = player.level().getEntity(entityId);
      return entity instanceof EntityDragonBase dragon ? dragon : null;
   }

   public ContainerDragon(int id, Inventory playerInv, EntityDragonBase dragon) {
      super(IafDragonEntities.DRAGON_MENU.get(), id);
      this.dragon = dragon;
      this.dragonInv = dragon != null ? dragon.dragonInv : new SimpleContainer(4);
      byte b0 = 3;
      this.dragonInv.startOpen(playerInv.player);
      int i = (b0 - 4) * 18;
      this.addSlot(new ArmorSlot(0, 8, 18));
      this.addSlot(new ArmorSlot(1, 8, 36));
      this.addSlot(new ArmorSlot(2, 153, 18));
      this.addSlot(new ArmorSlot(3, 153, 36));

      for (int j = 0; j < 3; j++) {
         for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(playerInv, k + j * 9 + 9, 8 + k * 18, 150 + j * 18 + i));
         }
      }

      for (int var7 = 0; var7 < 9; var7++) {
         this.addSlot(new Slot(playerInv, var7, 8 + var7 * 18, 208 + i));
      }
   }

   public EntityDragonBase getDragon() {
      return this.dragon;
   }

   /** 1.12 ItemDragonArmor with metadata == slot (0 head, 1 neck, 2 body, 3 tail). */
   static boolean isDragonArmor(ItemStack stack, int part) {
      return !stack.isEmpty()
         && (stack.is(ModItems.dragon_armor_iron) || stack.is(ModItems.dragon_armor_gold) || stack.is(ModItems.dragon_armor_diamond))
         && DragonCompat.dragonArmorPart(stack) == part;
   }

   private class ArmorSlot extends Slot {
      private final int part;

      ArmorSlot(int part, int x, int y) {
         super(ContainerDragon.this.dragonInv, part, x, y);
         this.part = part;
      }

      @Override
      public void setChanged() {
         EntityDragonBase drg = ContainerDragon.this.dragon;
         if (drg != null) {
            drg.setArmorInSlot(this.part, drg.getIntFromArmor(ContainerDragon.this.dragonInv.getItem(this.part)));
         }
         this.container.setChanged();
         if (drg != null) {
            drg.updateDragonSlots();
         }
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
         return super.mayPlace(stack) && isDragonArmor(stack, this.part);
      }
   }

   @Override
   public boolean stillValid(Player playerIn) {
      return this.dragon != null && this.dragonInv.stillValid(playerIn) && this.dragon.isAlive() && this.dragon.distanceTo(playerIn) < 8.0F;
   }

   @Override
   public ItemStack quickMoveStack(Player playerIn, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (index < this.dragonInv.getContainerSize()) {
            if (!this.moveItemStackTo(itemstack1, this.dragonInv.getContainerSize(), this.slots.size(), true)) {
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
         } else if (this.dragonInv.getContainerSize() <= 2 || !this.moveItemStackTo(itemstack1, 2, this.dragonInv.getContainerSize(), false)) {
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
   public void removed(Player playerIn) {
      super.removed(playerIn);
      this.dragonInv.stopOpen(playerIn);
   }
}
