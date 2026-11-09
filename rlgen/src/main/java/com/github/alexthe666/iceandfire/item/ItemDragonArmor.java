package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 1.12 ItemDragonArmor (dragonarmor_iron/gold/diamond; type 0/1/2; metadata 0 head, 1 neck, 2 body, 3 tail). The dragon inventory and
 * renderer read {@link #type} and the metadata (IafMeta.getMetadata).
 */
public class ItemDragonArmor extends Item {
   public int type;
   public String name;

   public ItemDragonArmor(int type, String name) {
      super(new Properties().stacksTo(1));
      this.type = type;
      this.name = name;
      IafNames.item(this, "iceandfire." + name);
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      String words = switch (IafMeta.getMetadata(stack)) {
         case 1 -> "dragon.armor_neck";
         case 2 -> "dragon.armor_body";
         case 3 -> "dragon.armor_tail";
         default -> "dragon.armor_head";
      };
      tooltip.add(Component.translatable(words));
   }
}
