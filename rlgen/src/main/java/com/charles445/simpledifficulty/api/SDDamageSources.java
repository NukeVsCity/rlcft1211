package com.charles445.simpledifficulty.api;

import com.charles445.simpledifficulty.SimpleDifficulty;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 1.12 SDDamageSources and SDEnchantments. The damage types are data (data/simpledifficulty/damage_type); 1.12 flags become tags:
 * setDamageBypassesArmor = #bypasses_armor, setDamageIsAbsolute = #bypasses_effects/#bypasses_enchantments, setMagicDamage = #witch_resistant_to.
 */
public class SDDamageSources {
   public static final ResourceKey<DamageType> DEHYDRATION = key("dehydration");
   public static final ResourceKey<DamageType> HYPERTHERMIA = key("hyperthermia");
   public static final ResourceKey<DamageType> HYPOTHERMIA = key("hypothermia");
   public static final ResourceKey<DamageType> PARASITES = key("parasites");

   public static final ResourceKey<Enchantment> CHILLING = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(SimpleDifficulty.MODID, "chilling"));
   public static final ResourceKey<Enchantment> HEATING = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(SimpleDifficulty.MODID, "heating"));

   private static ResourceKey<DamageType> key(String name) {
      return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(SimpleDifficulty.MODID, name));
   }

   public static DamageSource source(Entity entity, ResourceKey<DamageType> type) {
      return new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type));
   }
}
