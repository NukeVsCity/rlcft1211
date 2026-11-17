package com.lycanitesmobs.compat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** 1.12 ItemStack.getTagCompound/setTagCompound on the 1.21 custom_data component. getTag returns a copy: write it back with setTag. */
public final class LycNBT {
   private LycNBT() {
   }

   public static boolean hasTag(ItemStack stack) {
      return stack.has(DataComponents.CUSTOM_DATA);
   }

   public static CompoundTag getTag(ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? null : data.copyTag();
   }

   public static void setTag(ItemStack stack, CompoundTag tag) {
      if (tag == null) {
         stack.remove(DataComponents.CUSTOM_DATA);
      } else {
         stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      }
   }
}
