package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** 1.12 ItemAmbrosia: food 5 / 0.6, always edible, stack 1, strength/absorption/jump boost/luck III for 3600 ticks, leaves a bowl. */
public class ItemAmbrosia extends Item {
   public ItemAmbrosia() {
      super(new Properties().stacksTo(1).food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).alwaysEdible().build()));
      IafNames.item(this, "iceandfire.ambrosia");
   }

   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entity) {
      if (entity instanceof Player player) {
         player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 2));
         player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 3600, 2));
         player.addEffect(new MobEffectInstance(MobEffects.JUMP, 3600, 2));
         player.addEffect(new MobEffectInstance(MobEffects.LUCK, 3600, 2));
      }
      super.finishUsingItem(stack, worldIn, entity);
      return new ItemStack(Items.BOWL);
   }
}
