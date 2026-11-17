package ichttt.mods.firstaid.common.util;

import ichttt.mods.firstaid.FirstAidConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.component.DataComponents;

/**
 * 1.12 ArmorUtils: locational armor (the armor of the hit slot only, scaled per slot), 1.12 CombatRules, resistance with RLCraft's
 * 20 % per level, and the 1.12 protection enchantment numbers (LOCAL_ENCHANTMENTS with RLCraft's multipliers). 1.12 had no
 * ISpecialArmor items in vanilla; armor of 1.21 mods is read from its attribute modifiers like 1.12 did for ItemArmor.
 */
public class ArmorUtils {
   private static double getValueFromAttributes(Holder<Attribute> attribute, EquipmentSlot slot, ItemStack stack) {
      double[] sum = {0.0};
      stack.forEachModifier(slot, (attr, mod) -> {
         if (attr.equals(attribute)) {
            sum[0] += mod.amount();
         }
      });
      return sum[0];
   }

   public static double getArmor(ItemStack stack, EquipmentSlot slot) {
      return getValueFromAttributes(Attributes.ARMOR, slot, stack);
   }

   public static double getArmorToughness(ItemStack stack, EquipmentSlot slot) {
      return getValueFromAttributes(Attributes.ARMOR_TOUGHNESS, slot, stack);
   }

   public static double applyArmorModifier(EquipmentSlot slot, double rawArmor) {
      if (rawArmor <= 0.0) {
         return 0.0;
      }
      return rawArmor * getArmorMultiplier(slot) + getArmorOffset(slot);
   }

   public static double applyToughnessModifier(EquipmentSlot slot, double rawToughness) {
      if (rawToughness <= 0.0) {
         return 0.0;
      }
      return rawToughness * getToughnessMultiplier(slot) + getToughnessOffset(slot);
   }

   public static double getArmorMultiplier(EquipmentSlot slot) {
      FirstAidConfig.LocationalArmor.Armor c = FirstAidConfig.locationalArmor.armor;
      return switch (slot) {
         case HEAD -> c.headArmorMultiplier;
         case CHEST -> c.chestArmorMultiplier;
         case LEGS -> c.legsArmorMultiplier;
         case FEET -> c.feetArmorMultiplier;
         default -> throw new IllegalArgumentException("Invalid slot " + slot);
      };
   }

   private static double getArmorOffset(EquipmentSlot slot) {
      FirstAidConfig.LocationalArmor.Armor c = FirstAidConfig.locationalArmor.armor;
      return switch (slot) {
         case HEAD -> c.headArmorOffset;
         case CHEST -> c.chestArmorOffset;
         case LEGS -> c.legsArmorOffset;
         case FEET -> c.feetArmorOffset;
         default -> throw new IllegalArgumentException("Invalid slot " + slot);
      };
   }

   public static double getToughnessMultiplier(EquipmentSlot slot) {
      FirstAidConfig.LocationalArmor.Toughness c = FirstAidConfig.locationalArmor.toughness;
      return switch (slot) {
         case HEAD -> c.headToughnessMultiplier;
         case CHEST -> c.chestToughnessMultiplier;
         case LEGS -> c.legsToughnessMultiplier;
         case FEET -> c.feetToughnessMultiplier;
         default -> throw new IllegalArgumentException("Invalid slot " + slot);
      };
   }

   private static double getToughnessOffset(EquipmentSlot slot) {
      FirstAidConfig.LocationalArmor.Toughness c = FirstAidConfig.locationalArmor.toughness;
      return switch (slot) {
         case HEAD -> c.headToughnessOffset;
         case CHEST -> c.chestToughnessOffset;
         case LEGS -> c.legsToughnessOffset;
         case FEET -> c.feetToughnessOffset;
         default -> throw new IllegalArgumentException("Invalid slot " + slot);
      };
   }

   /** armor/toughness of the player that does not come from the four armor items (potions, other attribute modifiers) */
   private static double getGlobalRestAttribute(Player player, Holder<Attribute> attribute) {
      double sumOfAllAttributes = 0.0;
      for (EquipmentSlot slot : CommonUtils.ARMOR_SLOTS) {
         sumOfAllAttributes += getValueFromAttributes(attribute, slot, player.getItemBySlot(slot));
      }
      double all = player.getAttributeValue(attribute);
      return Math.abs(sumOfAllAttributes - all) > 0.001 ? all - sumOfAllAttributes : 0.0;
   }

   public static float applyArmor(Player entity, ItemStack itemStack, DamageSource source, double damage, EquipmentSlot slot) {
      double totalArmor = 0.0;
      double totalToughness = 0.0;
      if (source.is(DamageTypeTags.BYPASSES_ARMOR)) {
         return (float)damage;
      }
      boolean isArmor = itemStack.getItem() instanceof ArmorItem;
      if (isArmor) {
         totalArmor += getArmor(itemStack, slot);
         totalToughness += getArmorToughness(itemStack, slot);
         totalArmor = applyArmorModifier(slot, totalArmor);
         totalToughness = applyToughnessModifier(slot, totalToughness);
         // ArmorProperties(0, 0.0, MAX): absorb ratio 0, no extra armor damage
      }
      totalArmor += getGlobalRestAttribute(entity, Attributes.ARMOR);
      totalToughness += getGlobalRestAttribute(entity, Attributes.ARMOR_TOUGHNESS);
      if (damage > 0.0 && (totalArmor > 0.0 || totalToughness > 0.0)) {
         double armorDamage = Math.max(1.0, damage);
         if (isArmor) {
            itemStack.hurtAndBreak((int)armorDamage, entity, slot);
         }
         damage = getDamageAfterAbsorb((float)damage, (float)totalArmor, (float)totalToughness);
      }
      return (float)damage;
   }

   /** 1.12 CombatRules.getDamageAfterAbsorb */
   public static float getDamageAfterAbsorb(float damage, float totalArmor, float toughnessAttribute) {
      float f = 2.0F + toughnessAttribute / 4.0F;
      float f1 = Mth.clamp(totalArmor - damage / f, totalArmor * 0.2F, 20.0F);
      return damage * (1.0F - f1 / 25.0F);
   }

   /** 1.12 CombatRules.getDamageAfterMagicAbsorb */
   public static float getDamageAfterMagicAbsorb(float damage, float enchantModifiers) {
      float f = Mth.clamp(enchantModifiers, 0.0F, 20.0F);
      return damage * (1.0F - f / 25.0F);
   }

   /** 1.12 DamageSource.isDamageAbsolute */
   public static boolean isDamageAbsolute(DamageSource source) {
      return source.is(DamageTypeTags.BYPASSES_EFFECTS);
   }

   public static float applyGlobalPotionModifiers(Player player, DamageSource source, float damage) {
      if (isDamageAbsolute(source)) {
         return damage;
      }
      MobEffectInstance resistance = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
      if (resistance != null && !source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
         int i = (resistance.getAmplifier() + 1) * FirstAidConfig.externalHealing.resistanceReductionPercentPerLevel;
         int j = 100 - i;
         damage = damage * (float)j / 100.0F;
      }
      return damage <= 0.0F ? 0.0F : damage;
   }

   /** 1.12 EntityLivingBase.applyPotionDamageCalculations (vanilla 20 % resistance per level, global enchantments) */
   public static float applyPotionDamageCalculations(Player player, DamageSource source, float damage) {
      if (isDamageAbsolute(source)) {
         return damage;
      }
      MobEffectInstance resistance = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
      if (resistance != null && !source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
         int i = (resistance.getAmplifier() + 1) * 5;
         damage = damage * (float)(25 - i) / 25.0F;
      }
      if (damage <= 0.0F) {
         return 0.0F;
      }
      int k = 0;
      for (EquipmentSlot slot : CommonUtils.ARMOR_SLOTS) {
         k += calculateEnchantmentModifier(player.getItemBySlot(slot), source, false);
      }
      if (k > 0) {
         damage = getDamageAfterMagicAbsorb(damage, (float)k);
      }
      return damage;
   }

   public static float applyEnchantmentModifiers(Player player, EquipmentSlot slot, DamageSource source, float damage) {
      if (isDamageAbsolute(source)) {
         return damage;
      }
      int k;
      if (FirstAidConfig.enchantmentHandling.armorEnchantmentMode == FirstAidConfig.EnchantmentHandling.ArmorEnchantmentMode.LOCAL_ENCHANTMENTS) {
         k = calculateEnchantmentModifier(player.getItemBySlot(slot), source, true);
      } else {
         k = 0;
         for (EquipmentSlot s : CommonUtils.ARMOR_SLOTS) {
            k += calculateEnchantmentModifier(player.getItemBySlot(s), source, false);
         }
      }
      if (k > 0) {
         damage = getDamageAfterMagicAbsorb(damage, (float)k);
      }
      return damage;
   }

   /** 1.12 calculateEnchantmentModifierLocal (local = with the FirstAid multipliers) / EnchantmentHelper.getEnchantmentModifierDamage */
   private static int calculateEnchantmentModifier(ItemStack stack, DamageSource source, boolean local) {
      int ret = 0;
      if (stack.isEmpty()) {
         return 0;
      }
      ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
      for (var entry : enchantments.entrySet()) {
         Holder<Enchantment> ench = entry.getKey();
         int lvl = entry.getIntValue();
         String id = ench.unwrapKey().map(ResourceKey::location).map(Object::toString).orElse("");
         int val = calcModifierDamage(id, lvl, source);
         if (!local) {
            ret += val;
            continue;
         }
         String[] resourceLocation = FirstAidConfig.enchantmentHandling.overrideEntries.resourceLocation;
         int[] multiplierOverride = FirstAidConfig.enchantmentHandling.overrideEntries.multiplierOverride;
         int multiplier = FirstAidConfig.enchantmentHandling.enchantmentMultiplier;
         for (int l = 0; l < Math.min(resourceLocation.length, multiplierOverride.length); l++) {
            if (resourceLocation[l].equals(id)) {
               multiplier = multiplierOverride[l];
               break;
            }
         }
         ret += val * multiplier;
      }
      return ret;
   }

   /** 1.12 Enchantment.calcModifierDamage: only the five EnchantmentProtection types count */
   private static int calcModifierDamage(String id, int level, DamageSource source) {
      if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         return 0;
      }
      return switch (id) {
         case "minecraft:protection" -> level;
         case "minecraft:fire_protection" -> source.is(DamageTypeTags.IS_FIRE) ? level * 2 : 0;
         case "minecraft:feather_falling" -> source.is(DamageTypes.FALL) ? level * 3 : 0;
         case "minecraft:blast_protection" -> source.is(DamageTypeTags.IS_EXPLOSION) ? level * 2 : 0;
         case "minecraft:projectile_protection" -> source.is(DamageTypeTags.IS_PROJECTILE) ? level * 2 : 0;
         default -> 0;
      };
   }
}
