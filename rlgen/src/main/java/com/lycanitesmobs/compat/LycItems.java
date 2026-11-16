package com.lycanitesmobs.compat;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** 1.12 ItemStack helpers. */
public final class LycItems {
   private LycItems() {
   }

   /** 1.12 ItemStack.damageItem(amount, entity) (main hand break animation). */
   public static void damageItem(ItemStack stack, int amount, LivingEntity entity) {
      stack.hurtAndBreak(amount, entity, EquipmentSlot.MAINHAND);
   }
}
