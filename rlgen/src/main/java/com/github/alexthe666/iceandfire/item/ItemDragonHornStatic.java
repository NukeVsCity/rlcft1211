package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 1.12 ItemDragonHornStatic: the empty dragon horn (right clicking a tamed dragon with it is in the dragon interaction code). */
public class ItemDragonHornStatic extends Item {
   public ItemDragonHornStatic() {
      super(new Properties().stacksTo(1));
      IafNames.item(this, "iceandfire.dragon_horn");
   }

   @Override
   public void inventoryTick(ItemStack stack, Level world, Entity entity, int f, boolean f1) {
      if (IafMeta.getTag(stack) == null) {
         IafMeta.setTag(stack, new CompoundTag());
      }
   }
}
