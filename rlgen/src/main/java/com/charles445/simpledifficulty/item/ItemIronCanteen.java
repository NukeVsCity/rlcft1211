package com.charles445.simpledifficulty.item;

import com.charles445.simpledifficulty.config.ModConfig;
import net.minecraft.world.item.ItemStack;

/** 1.12 ItemIronCanteen (model property "contain" is registered client side). */
public class ItemIronCanteen extends ItemCanteen {
   public ItemIronCanteen(Properties props) {
      super(props);
   }

   @Override
   public int getMaxDoses(ItemStack stack) {
      return ModConfig.server.ironCanteenDoses;
   }

   @Override
   protected String keyPrefix() {
      return "item.simpledifficulty:iron_canteen_";
   }
}
