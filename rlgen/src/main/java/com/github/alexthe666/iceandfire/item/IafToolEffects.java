package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import com.github.alexthe666.iceandfire.core.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * The shared hitEntity/addInformation code of 1.12 ItemModSword/Axe/Pickaxe/Shovel/Hoe: silver tools deal 2 extra magic damage to
 * undead, myrmex chitin tools 4 extra generic damage to non-arthropods (and again to death worms), venom swords poison (200 ticks,
 * amplifier 2). Plus the 1.12 attribute values (1.12 tools set absolute damage/speed, not "tier bonus + x").
 */
public final class IafToolEffects {
   private IafToolEffects() {
   }

   public static void hitEntity(Item item, Tier tier, LivingEntity target, LivingEntity attacker) {
      boolean silver = item == ModItems.silver_sword || item == ModItems.silver_axe || item == ModItems.silver_pickaxe || item == ModItems.silver_shovel
         || item == ModItems.silver_hoe;
      if (silver && target.getType().is(EntityTypeTags.UNDEAD)) {
         target.hurt(target.level().damageSources().magic(), 2.0F);
      }
      if (tier == ModItems.myrmexChitin) {
         if (!target.getType().is(EntityTypeTags.ARTHROPOD)) {
            target.hurt(target.level().damageSources().generic(), 4.0F);
         }
         if (ModEntityHooks.is(target, "deathworm")) {
            target.hurt(target.level().damageSources().generic(), 4.0F);
         }
      }
      if (item == ModItems.myrmex_desert_sword_venom || item == ModItems.myrmex_jungle_sword_venom) {
         target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
      }
   }

   public static void addInformation(Item item, List<Component> tooltip) {
      if (item == ModItems.silver_sword || item == ModItems.silver_axe || item == ModItems.silver_pickaxe || item == ModItems.silver_shovel
         || item == ModItems.silver_hoe) {
         tooltip.add(Component.translatable("silvertools.hurt").withStyle(ChatFormatting.GREEN));
      }
      if (item == ModItems.myrmex_desert_sword_venom || item == ModItems.myrmex_jungle_sword_venom) {
         tooltip.add(Component.translatable("myrmextools.poison").withStyle(ChatFormatting.DARK_GREEN));
         tooltip.add(Component.translatable("myrmextools.hurt").withStyle(ChatFormatting.GREEN));
      }
      if (item == ModItems.myrmex_desert_sword || item == ModItems.myrmex_jungle_sword || item == ModItems.myrmex_desert_axe || item == ModItems.myrmex_jungle_axe
         || item == ModItems.myrmex_desert_pickaxe || item == ModItems.myrmex_jungle_pickaxe || item == ModItems.myrmex_desert_shovel
         || item == ModItems.myrmex_jungle_shovel || item == ModItems.myrmex_desert_hoe || item == ModItems.myrmex_jungle_hoe) {
         tooltip.add(Component.translatable("myrmextools.hurt").withStyle(ChatFormatting.GREEN));
      }
   }

   /** Main hand attack damage (added to the player's base 1) and attack speed (added to the base 4), like 1.12 tool modifiers. */
   public static ItemAttributeModifiers attributes(double damage, double speed) {
      return ItemAttributeModifiers.builder()
         .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
         .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
         .build();
   }
}
