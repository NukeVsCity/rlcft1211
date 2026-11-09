package com.github.alexthe666.iceandfire.api;

import net.minecraft.food.FoodProperties;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Ice and Fire 1.7.1 FoodUtils (1.12 EntityAgeable = AgeableMob, ItemFood = items with food properties). */
public class FoodUtils {
   public static int getFoodPoints(Entity entity) {
      int foodPoints = Math.round(entity.getBbWidth() * entity.getBbHeight() * 10.0F);
      if (entity instanceof AgeableMob) {
         return foodPoints;
      } else {
         return entity instanceof Player ? 15 : 0;
      }
   }

   public static int getFoodPoints(ItemStack item, boolean meatOnly, boolean includeFish) {
      if (item != null && !item.isEmpty()) {
         FoodProperties food = item.getFoodProperties(null);
         if (food != null) {
            int points = food.nutrition() * 10;
            if (!meatOnly) {
               return points;
            }

            // 1.12 ItemFood.isWolfsFavoriteMeat
            if (item.is(ItemTags.MEAT)) {
               return points;
            }

            // 1.12 Items.FISH (raw cod, salmon, clownfish, pufferfish)
            if (includeFish && (item.is(Items.COD) || item.is(Items.SALMON) || item.is(Items.TROPICAL_FISH) || item.is(Items.PUFFERFISH))) {
               return points;
            }
         }
      }

      return 0;
   }
}
