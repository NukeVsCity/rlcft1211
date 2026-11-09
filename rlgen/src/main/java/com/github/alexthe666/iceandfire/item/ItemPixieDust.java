package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 1.12 ItemPixieDust: food 1 / 0.3, levitation II and glowing II for 100 ticks. */
public class ItemPixieDust extends Item {
   public ItemPixieDust() {
      super(new Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).build()));
      IafNames.item(this, "iceandfire.pixie_dust");
   }

   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entity) {
      if (entity instanceof Player player) {
         player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 1));
         player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 1));
      }
      return super.finishUsingItem(stack, worldIn, entity);
   }
}
