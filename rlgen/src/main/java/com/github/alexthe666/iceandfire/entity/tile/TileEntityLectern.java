package com.github.alexthe666.iceandfire.entity.tile;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.enums.EnumBestiaryPages;
import com.github.alexthe666.iceandfire.inventory.ContainerLectern;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 TileEntityLectern: a furnace that "smelts" a bestiary with a manuscript (300 ticks of fuel) into the bestiary with a new page. */
public class TileEntityLectern extends BlockEntity implements WorldlyContainer, net.minecraft.world.MenuProvider {
   private static final int[] slotsTop = new int[]{0};
   private static final int[] slotsBottom = new int[]{2, 1};
   private static final int[] slotsSides = new int[]{1};
   private static Random rand = new Random();
   public float pageFlip;
   public float pageFlipPrev;
   public float pageHelp1;
   public float pageHelp2;
   private NonNullList<ItemStack> stacks = NonNullList.withSize(3, ItemStack.EMPTY);
   private int furnaceBurnTime;
   private int currentItemBurnTime;
   private int cookTime;
   private int totalCookTime;
   public final ContainerData fields = new ContainerData() {
      @Override
      public int get(int id) {
         return TileEntityLectern.this.getField(id);
      }

      @Override
      public void set(int id, int value) {
         TileEntityLectern.this.setField(id, value);
      }

      @Override
      public int getCount() {
         return 4;
      }
   };

   public TileEntityLectern(BlockPos pos, BlockState state) {
      super(ModBlocks.LECTERN_TILE.get(), pos, state);
   }

   public static boolean isBurning(ContainerData i) {
      return i.get(0) > 0;
   }

   public static int getItemBurnTime(ItemStack i) {
      if (i.isEmpty()) {
         return 0;
      }
      Item item = i.getItem();
      return item == ModItems.manuscript ? 300 : 0;
   }

   public static boolean isItemFuel(ItemStack i) {
      return getItemBurnTime(i) > 0;
   }

   public static void tick(Level level, BlockPos pos, BlockState state, TileEntityLectern te) {
      te.update();
   }

   public void update() {
      boolean flag = this.isBurning();
      boolean flag1 = false;
      if (this.isBurning()) {
         this.furnaceBurnTime--;
      }
      if (!this.level.isClientSide) {
         if (this.isBurning() || !this.stacks.get(1).isEmpty() && !this.stacks.get(0).isEmpty()) {
            if (!this.isBurning() && this.canSmelt()) {
               this.currentItemBurnTime = this.furnaceBurnTime = getItemBurnTime(this.stacks.get(1));
               if (this.isBurning()) {
                  flag1 = true;
                  if (!this.stacks.get(1).isEmpty()) {
                     ItemStack fuel = this.stacks.get(1);
                     ItemStack container = fuel.getCraftingRemainingItem();
                     fuel.shrink(1);
                     if (fuel.isEmpty()) {
                        this.stacks.set(1, container);
                     }
                  }
               }
            }
            if (this.isBurning() && this.canSmelt()) {
               this.cookTime++;
               if (this.cookTime == this.totalCookTime) {
                  this.cookTime = 0;
                  this.totalCookTime = 300;
                  this.smeltItem();
                  flag1 = true;
               }
            } else {
               this.cookTime = 0;
            }
         } else if (!this.isBurning() && this.cookTime > 0) {
            this.cookTime = Mth.clamp(this.cookTime - 2, 0, this.totalCookTime);
         }
         if (flag != this.isBurning()) {
            flag1 = true;
         }
      }
      if (flag1) {
         this.setChanged();
      }
      float f1 = this.pageHelp1;
      do {
         this.pageHelp1 = this.pageHelp1 + (float)(rand.nextInt(4) - rand.nextInt(4));
      } while (f1 == this.pageHelp1);
      this.pageFlipPrev = this.pageFlip;
      float f = (this.pageHelp1 - this.pageFlip) * 0.04F;
      float f3 = 0.02F;
      f = Mth.clamp(f, -f3, f3);
      this.pageHelp2 = this.pageHelp2 + (f - this.pageHelp2) * 0.9F;
      this.pageFlip = this.pageFlip + this.pageHelp2;
   }

   @Override
   public int getContainerSize() {
      return 3;
   }

   @Override
   public ItemStack getItem(int index) {
      return this.stacks.get(index);
   }

   private boolean canSmelt() {
      if (this.stacks.get(0).isEmpty()) {
         return false;
      }
      ItemStack itemstack = this.stacks.get(0).copy();
      if (itemstack.isEmpty() || itemstack.getItem() != ModItems.bestiary) {
         return false;
      }
      List<EnumBestiaryPages> list = EnumBestiaryPages.possiblePages(itemstack);
      if (list == null || list.isEmpty()) {
         return false;
      }
      if (this.stacks.get(2).isEmpty()) {
         return true;
      }
      int result = this.stacks.get(2).getCount() + itemstack.getCount();
      return result <= this.getMaxStackSize() && result <= this.stacks.get(2).getMaxStackSize();
   }

   @Override
   public ItemStack removeItem(int index, int count) {
      if (!this.stacks.get(index).isEmpty()) {
         if (this.stacks.get(index).getCount() <= count) {
            ItemStack itemstack = this.stacks.get(index);
            this.stacks.set(index, ItemStack.EMPTY);
            return itemstack;
         }
         ItemStack itemstack = this.stacks.get(index).split(count);
         if (this.stacks.get(index).getCount() == 0) {
            this.stacks.set(index, ItemStack.EMPTY);
         }
         return itemstack;
      }
      return ItemStack.EMPTY;
   }

   /** 1.12 removeStackFromSlot returned EMPTY (quirk kept). */
   @Override
   public ItemStack removeItemNoUpdate(int index) {
      return ItemStack.EMPTY;
   }

   @Override
   public void setItem(int index, ItemStack stack) {
      boolean flag = !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, this.stacks.get(index));
      this.stacks.set(index, stack);
      if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
         stack.setCount(this.getMaxStackSize());
      }
      if (index == 0 && !flag) {
         this.totalCookTime = 300;
         this.cookTime = 0;
         this.setChanged();
      }
   }

   @Override
   protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
      super.loadAdditional(compound, registries);
      this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      ContainerHelper.loadAllItems(compound, this.stacks, registries);
      this.furnaceBurnTime = compound.getShort("BurnTime");
      this.cookTime = compound.getShort("CookTime");
      this.totalCookTime = compound.getShort("CookTimeTotal");
      this.currentItemBurnTime = getItemBurnTime(this.stacks.get(1));
   }

   @Override
   protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
      super.saveAdditional(compound, registries);
      compound.putShort("BurnTime", (short)this.furnaceBurnTime);
      compound.putShort("CookTime", (short)this.cookTime);
      compound.putShort("CookTimeTotal", (short)this.totalCookTime);
      ContainerHelper.saveAllItems(compound, this.stacks, registries);
   }

   @Override
   public boolean stillValid(Player player) {
      return true;
   }

   @Override
   public void clearContent() {
      this.stacks.clear();
   }

   @Override
   public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
      return false;
   }

   public boolean isBurning() {
      return this.furnaceBurnTime > 0;
   }

   public void smeltItem() {
      if (this.canSmelt()) {
         ItemStack itemstack = this.stacks.get(0).copy();
         if (this.stacks.get(0).getItem() == ModItems.bestiary) {
            EnumBestiaryPages.addRandomPage(itemstack);
         }
         this.stacks.get(0).shrink(1);
         if (this.stacks.get(0).isEmpty()) {
            this.stacks.set(0, ItemStack.EMPTY);
         }
         if (this.stacks.get(2).isEmpty()) {
            this.stacks.set(2, itemstack);
         } else if (this.stacks.get(2).getItem() == itemstack.getItem()) {
            this.stacks.get(2).grow(itemstack.getCount());
         }
      }
   }

   @Override
   public int[] getSlotsForFace(Direction side) {
      return side == Direction.DOWN ? slotsBottom : (side == Direction.UP ? slotsTop : slotsSides);
   }

   @Override
   public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
      return this.canPlaceItem(index, itemStackIn);
   }

   public int getField(int id) {
      return switch (id) {
         case 0 -> this.furnaceBurnTime;
         case 1 -> this.currentItemBurnTime;
         case 2 -> this.cookTime;
         case 3 -> this.totalCookTime;
         default -> 0;
      };
   }

   @Override
   public boolean canPlaceItem(int index, ItemStack stack) {
      return index == 2 ? false : (index != 1 ? true : isItemFuel(stack));
   }

   public void setField(int id, int value) {
      switch (id) {
         case 0 -> this.furnaceBurnTime = value;
         case 1 -> this.currentItemBurnTime = value;
         case 2 -> this.cookTime = value;
         case 3 -> this.totalCookTime = value;
         default -> {
         }
      }
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
      return this.saveWithoutMetadata(registries);
   }

   @Override
   public Component getDisplayName() {
      return Component.translatable("tile.iceandfire.lectern.name");
   }

   @Override
   public boolean isEmpty() {
      for (ItemStack itemstack : this.stacks) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }
      return true;
   }

   @Override
   public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
      return new ContainerLectern(id, inv, this, this.fields);
   }
}
