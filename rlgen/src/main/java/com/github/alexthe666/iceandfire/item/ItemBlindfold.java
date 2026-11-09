package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 1.12 ItemBlindfold: a helmet that keeps blindness III (20 ticks, ambient, no particles) on the wearer (protects from gorgons). */
public class ItemBlindfold extends ArmorItem {
   public ItemBlindfold(Holder<ArmorMaterial> material) {
      super(material, Type.HELMET, new Properties().durability(Type.HELMET.getDurability(5)));
      IafNames.item(this, "iceandfire.blindfold");
   }

   @Override
   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      if (entity instanceof Player player && player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD) == stack) {
         player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 2, true, false));
      }
   }
}
