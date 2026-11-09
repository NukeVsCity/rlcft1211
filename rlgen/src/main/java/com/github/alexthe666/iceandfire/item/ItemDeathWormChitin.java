package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafMeta;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** 1.12 ItemDeathWormChitin: meta 0 yellow, 1 white, 2 red. */
public class ItemDeathWormChitin extends Item {
   public ItemDeathWormChitin() {
      super(new Properties());
   }

   @Override
   public String getDescriptionId(ItemStack stack) {
      int m = IafMeta.getMetadata(stack);
      return (m == 2 ? "item.iceandfire.deathworm_chitin_red" : (m == 1 ? "item.iceandfire.deathworm_chitin_white" : "item.iceandfire.deathworm_chitin_yellow"))
         + ".name";
   }
}
