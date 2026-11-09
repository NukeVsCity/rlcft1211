package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.entity.FrozenEntityProperties;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

/**
 * 1.12 ItemAlchemySword (dragonbone_sword_fire / _ice, damage 3 + 5.5, always enchanted glint). Fire: +13.5 in-fire damage to ice
 * dragons, sets the target on fire for 5 s, knockback 1. Ice: +13.5 drown damage to fire dragons, freezes for 200 ticks, slowness and
 * mining fatigue III for 100 ticks, knockback 1.
 */
public class ItemAlchemySword extends SwordItem {
   public ItemAlchemySword(Tier toolmaterial, String name) {
      super(toolmaterial, new Properties().attributes(IafToolEffects.attributes(3.0F + toolmaterial.getAttackDamageBonus(), -2.4F)));
      IafNames.item(this, name);
   }

   @Override
   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (this == ModItems.dragonbone_sword_fire) {
         if (ModEntityHooks.is(target, "icedragon")) {
            target.hurt(target.level().damageSources().inFire(), 13.5F);
         }
         target.igniteForSeconds(5.0F);
         target.knockback(1.0, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
      }
      if (this == ModItems.dragonbone_sword_ice) {
         if (ModEntityHooks.is(target, "firedragon")) {
            target.hurt(target.level().damageSources().drown(), 13.5F);
         }
         FrozenEntityProperties frozenProps = FrozenEntityProperties.get(target);
         if (frozenProps != null) {
            frozenProps.setFrozenFor(200);
         }
         target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
         target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 2));
         target.knockback(1.0, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
      }
      return super.hurtEnemy(stack, target, attacker);
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      if (this == ModItems.dragonbone_sword_fire) {
         tooltip.add(Component.translatable("dragon_sword_fire.hurt1").withStyle(ChatFormatting.GREEN));
         tooltip.add(Component.translatable("dragon_sword_fire.hurt2").withStyle(ChatFormatting.DARK_RED));
      }
      if (this == ModItems.dragonbone_sword_ice) {
         tooltip.add(Component.translatable("dragon_sword_ice.hurt1").withStyle(ChatFormatting.GREEN));
         tooltip.add(Component.translatable("dragon_sword_ice.hurt2").withStyle(ChatFormatting.AQUA));
      }
   }

   @Override
   public boolean isFoil(ItemStack stack) {
      return true;
   }
}
