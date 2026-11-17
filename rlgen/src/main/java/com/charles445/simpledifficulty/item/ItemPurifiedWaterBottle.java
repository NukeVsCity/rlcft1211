package com.charles445.simpledifficulty.item;

import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import net.minecraft.world.item.ItemStack;

/** 1.12 ItemPurifiedWaterBottle */
public class ItemPurifiedWaterBottle extends ItemDrinkBase {
   public ItemPurifiedWaterBottle(Properties props) {
      super(props);
   }

   @Override
   public int getThirstLevel(ItemStack stack) {
      return ThirstEnum.PURIFIED.getThirst();
   }

   @Override
   public float getSaturationLevel(ItemStack stack) {
      return ThirstEnum.PURIFIED.getSaturation();
   }

   @Override
   public float getDirtyChance(ItemStack stack) {
      return ThirstEnum.PURIFIED.getThirstyChance();
   }
}
