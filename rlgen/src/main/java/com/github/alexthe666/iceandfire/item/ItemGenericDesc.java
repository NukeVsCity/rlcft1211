package com.github.alexthe666.iceandfire.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** 1.12 ItemGenericDesc: adds the "<key>.desc" line (key = 1.12 "item.iceandfire.x"). */
public class ItemGenericDesc extends ItemGeneric {
   private final String descKey;

   public ItemGenericDesc(String name) {
      super(name);
      this.descKey = "item." + name + ".desc";
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable(this.descKey));
   }
}
