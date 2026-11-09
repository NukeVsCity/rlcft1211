package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModItems;
import java.util.function.Predicate;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

/**
 * 1.12 ItemDragonBow: a bow (584 durability, enchantability 1) that only fires dragonbone arrows (EntityDragonArrow). The vanilla bow
 * logic (velocity, crits, power/punch/flame, infinity) is the same as 1.12's copy of ItemBow.
 */
public class ItemDragonBow extends BowItem {
   public ItemDragonBow() {
      super(new Properties().stacksTo(1).durability(584));
      IafNames.item(this, "iceandfire.dragonbone_bow");
   }

   @Override
   public Predicate<ItemStack> getAllSupportedProjectiles() {
      return stack -> !stack.isEmpty() && stack.getItem() == ModItems.dragonbone_arrow;
   }

   @Override
   public ItemStack getDefaultCreativeAmmo(net.minecraft.world.entity.player.Player player, ItemStack projectileWeaponItem) {
      return new ItemStack(ModItems.dragonbone_arrow);
   }

   @Override
   public int getEnchantmentValue() {
      return 1;
   }
}
