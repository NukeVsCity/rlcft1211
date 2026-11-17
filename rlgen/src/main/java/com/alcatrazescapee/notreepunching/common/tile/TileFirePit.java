package com.alcatrazescapee.notreepunching.common.tile;

import com.alcatrazescapee.alcatrazcore.tile.ITileFields;
import com.alcatrazescapee.alcatrazcore.tile.TileInventory;
import com.alcatrazescapee.notreepunching.ModConfig;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.common.blocks.BlockFirePit;
import com.alcatrazescapee.notreepunching.common.recipe.FirePitRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 TileFirePit: slot 0 fuel (furnace burn time 1..firePitFuelMaxAmount), 1 input, 2 output. While lit it cooks the input for
 * firePitCookTime ticks (the timer falls back by 4 a tick without a recipe) and burns fuel at burn time * firePitFuelMultiplier; out
 * of fuel it goes out. A new fire pit from the fire starter starts with half a log of fuel.
 */
public class TileFirePit extends TileInventory implements ITileFields {
   private static final byte BURN_FIELD_ID = 0;
   private static final byte MAX_BURN_FIELD_ID = 1;
   private static final byte COOK_FIELD_ID = 2;
   public static final byte FUEL_SLOT_ID = 0;
   public static final byte INPUT_SLOT_ID = 1;
   public static final byte OUTPUT_SLOT_ID = 2;
   private static final byte NUM_FIELDS = 3;
   private int burnTicks;
   private int maxBurnTicks;
   private int cookTimer;
   private boolean hasCachedRecipe = false;
   private ItemStack cachedRecipe = null;

   private static int getItemBurnTime(ItemStack stack) {
      return stack.isEmpty() ? 0 : stack.getBurnTime(RecipeType.SMELTING);
   }

   private static boolean isStackValidFuel(ItemStack stack) {
      return getItemBurnTime(stack) > 0 && getItemBurnTime(stack) <= ModConfig.BALANCE.firePitFuelMaxAmount;
   }

   public TileFirePit(BlockPos pos, BlockState state) {
      super(NoTreePunching.FIRE_PIT_TILE.get(), pos, state, 3);
   }

   public void light(boolean addStartingFuel) {
      this.level.setBlockAndUpdate(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(BlockFirePit.LIT, true));
      if (addStartingFuel) {
         this.burnTicks = (int)(0.5 * (double)getItemBurnTime(new ItemStack(Items.OAK_LOG)) * (double)ModConfig.BALANCE.firePitFuelMultiplier);
         this.maxBurnTicks = this.burnTicks * 2;
      }
      this.setChanged();
   }

   public void extinguish() {
      this.level.setBlockAndUpdate(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(BlockFirePit.LIT, false));
      this.burnTicks = 0;
      this.maxBurnTicks = 0;
      this.cookTimer = 0;
      this.setChanged();
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, TileFirePit te) {
      te.update();
   }

   /** 1.12 ITickable.update (server only) */
   public void update() {
      BlockState state = this.level.getBlockState(this.worldPosition);
      if (!state.hasProperty(BlockFirePit.LIT)) {
         return;
      }
      if (state.getValue(BlockFirePit.LIT)) {
         if (!this.hasCachedRecipe) {
            this.updateRecipe();
         }
         if (this.cachedRecipe != null) {
            this.cookTimer++;
            if (this.cookTimer >= ModConfig.BALANCE.firePitCookTime) {
               this.updateRecipe();
               if (this.cachedRecipe != null) {
                  ItemStack cookStack = this.inventory.getStackInSlot(INPUT_SLOT_ID);
                  ItemStack outStack = this.inventory.getStackInSlot(OUTPUT_SLOT_ID);
                  ItemStack output = this.cachedRecipe;
                  this.inventory.setStackInSlot(INPUT_SLOT_ID, consumeItem(cookStack));
                  this.inventory.setStackInSlot(OUTPUT_SLOT_ID, mergeStacks(outStack, output));
                  this.updateRecipe();
               }
               this.cookTimer = 0;
            }
         } else if (this.cookTimer > 0) {
            this.cookTimer -= 4;
            if (this.cookTimer < 0) {
               this.cookTimer = 0;
            }
         }
         this.burnTicks--;
         if (this.burnTicks <= 0) {
            ItemStack stack = this.inventory.getStackInSlot(FUEL_SLOT_ID);
            if (isStackValidFuel(stack)) {
               this.burnTicks = this.burnTicks + getItemBurnTime(stack) * ModConfig.BALANCE.firePitFuelMultiplier;
               this.maxBurnTicks = this.burnTicks;
               this.inventory.setStackInSlot(FUEL_SLOT_ID, consumeItem(stack));
            } else {
               this.burnTicks = 0;
               this.level.setBlockAndUpdate(this.worldPosition, state.setValue(BlockFirePit.LIT, false));
            }
         }
         this.setChanged();
      } else {
         this.burnTicks = 0;
         this.maxBurnTicks = 0;
      }
   }

   /** CoreHelpers.consumeItem: shrink by one (no container item) */
   private static ItemStack consumeItem(ItemStack stack) {
      return stack.getCount() > 1 ? stack.copyWithCount(stack.getCount() - 1) : ItemStack.EMPTY;
   }

   /** CoreHelpers.canMergeStacks: same item (1.12 item + meta), count and NBT not checked */
   private static boolean canMergeStacks(ItemStack a, ItemStack b) {
      return a.isEmpty() || b.isEmpty() || ItemStack.isSameItem(a, b);
   }

   /** CoreHelpers.mergeStacks: capped at the max stack size, so a full output slot eats what is cooked (1.12 quirk) */
   private static ItemStack mergeStacks(ItemStack a, ItemStack b) {
      if (a.isEmpty()) {
         return b.copy();
      } else if (b.isEmpty()) {
         return a;
      }
      return a.copyWithCount(Math.min(a.getMaxStackSize(), a.getCount() + b.getCount()));
   }

   @Override
   public void setAndUpdateSlots(int slot) {
      if (this.level != null && !this.level.isClientSide) {
         this.updateRecipe();
      }
      super.setAndUpdateSlots(slot);
   }

   @Override
   public boolean isItemValid(int slot, ItemStack stack) {
      return switch (slot) {
         case 0 -> isStackValidFuel(stack);
         case 1 -> true;
         case 2 -> false;
         default -> throw new IllegalArgumentException("Invalid slot id in isItemValid: " + slot);
      };
   }

   public int getScaledCookTime() {
      if (ModConfig.BALANCE.firePitCookTime != 0 && this.cookTimer != 0) {
         float f1 = (float)this.cookTimer / (float)ModConfig.BALANCE.firePitCookTime;
         return Math.round(23.0F * f1);
      }
      return 0;
   }

   public int getScaledBurnTicks() {
      if (this.maxBurnTicks != 0 && this.burnTicks != 0) {
         float f1 = (float)this.burnTicks / (float)this.maxBurnTicks;
         return Math.round(13.0F * f1);
      }
      return 0;
   }

   @Override
   public int getFieldCount() {
      return NUM_FIELDS;
   }

   @Override
   protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
      this.burnTicks = nbt.getInt("burn_ticks");
      this.maxBurnTicks = nbt.getInt("max_burn_ticks");
      this.cookTimer = nbt.getInt("cook_ticks");
      super.loadAdditional(nbt, registries);
   }

   @Override
   protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
      nbt.putInt("burn_ticks", this.burnTicks);
      nbt.putInt("max_burn_ticks", this.maxBurnTicks);
      nbt.putInt("cook_ticks", this.cookTimer);
      super.saveAdditional(nbt, registries);
   }

   private void updateRecipe() {
      this.hasCachedRecipe = true;
      ItemStack cookStack = this.inventory.getStackInSlot(INPUT_SLOT_ID);
      ItemStack output = FirePitRecipes.getOutput(this.level, cookStack);
      if (!output.isEmpty()) {
         ItemStack outStack = this.inventory.getStackInSlot(OUTPUT_SLOT_ID);
         if (canMergeStacks(outStack, output)) {
            this.cachedRecipe = output;
            return;
         }
      }
      this.cachedRecipe = null;
   }

   @Override
   public int getField(int id) {
      switch (id) {
         case BURN_FIELD_ID:
            return this.burnTicks;
         case MAX_BURN_FIELD_ID:
            return this.maxBurnTicks;
         case COOK_FIELD_ID:
            return this.cookTimer;
         default:
            NoTreePunching.LOG.warn("Invalid field ID: {}", id);
            return 0;
      }
   }

   @Override
   public void setField(int id, int value) {
      switch (id) {
         case BURN_FIELD_ID -> this.burnTicks = value;
         case MAX_BURN_FIELD_ID -> this.maxBurnTicks = value;
         case COOK_FIELD_ID -> this.cookTimer = value;
         default -> NoTreePunching.LOG.warn("Invalid Field ID {}", id);
      }
   }
}
