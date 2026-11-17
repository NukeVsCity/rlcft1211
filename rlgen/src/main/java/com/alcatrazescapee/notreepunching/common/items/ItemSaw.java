package com.alcatrazescapee.notreepunching.common.items;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

/** 1.12 ItemSaw (ItemAxeCore, damage max(material - 1, 1) + material, speed -2.8): stays in the crafting grid with 1 more damage, no repair. */
public class ItemSaw extends DiggerItem {
   public ItemSaw(Tier material) {
      super(material, BlockTags.MINEABLE_WITH_AXE, new Item.Properties().setNoRepair()
         .attributes(DiggerItem.createAttributes(material, Math.max(material.getAttackDamageBonus() - 1.0F, 1.0F), -2.8F)));
   }

   @Override
   public ItemStack getCraftingRemainingItem(ItemStack stack) {
      ItemStack copy = stack.copy();
      copy.setDamageValue(copy.getDamageValue() + 1);
      return copy.getDamageValue() >= copy.getMaxDamage() ? ItemStack.EMPTY : copy;
   }

   @Override
   public boolean hasCraftingRemainingItem(ItemStack stack) {
      return true;
   }
}
