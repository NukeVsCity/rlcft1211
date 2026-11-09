package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import net.minecraft.world.item.ItemStack;

/** 1.12 ItemDeathwormEgg (registry name "iceandfire.deathworm_egg", quirk kept): meta 1 = giant; thrown, it is an EntityDeathWormEgg. */
public class ItemDeathwormEgg extends ItemIafThrowable {
   public ItemDeathwormEgg() {
      super(new Properties().stacksTo(1), () -> ModEntityHooks.throwDeathwormEgg);
   }

   @Override
   public String getDescriptionId(ItemStack stack) {
      return IafMeta.getMetadata(stack) == 1 ? "item.iceandfire.deathworm_egg_giant.name" : "item.iceandfire.deathworm_egg.name";
   }
}
