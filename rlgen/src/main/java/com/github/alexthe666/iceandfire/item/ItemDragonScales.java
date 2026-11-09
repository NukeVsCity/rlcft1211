package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** 1.12 ItemDragonScales: tooltip shows the dragon colour. */
public class ItemDragonScales extends Item {
   EnumDragonEgg type;

   public ItemDragonScales(EnumDragonEgg type) {
      super(new Properties());
      this.type = type;
      IafNames.item(this, "iceandfire.dragonscales");
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("dragon." + this.type.toString().toLowerCase()).withStyle(this.type.color));
   }
}
