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

/** 1.12 ItemDragonFlesh: food 8 / 0.8 (wolf food); fire flesh sets the eater on fire for 5 s, ice flesh gives slowness III for 100 ticks. */
public class ItemDragonFlesh extends Item {
   boolean isFire;

   public ItemDragonFlesh(boolean isFire) {
      super(new Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build()));
      this.isFire = isFire;
      IafNames.item(this, isFire ? "iceandfire.fire_dragon_flesh" : "iceandfire.ice_dragon_flesh");
   }

   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entity) {
      if (!worldIn.isClientSide && entity instanceof Player player) {
         if (this.isFire) {
            player.igniteForSeconds(5.0F);
         } else {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
         }
      }
      return super.finishUsingItem(stack, worldIn, entity);
   }
}
