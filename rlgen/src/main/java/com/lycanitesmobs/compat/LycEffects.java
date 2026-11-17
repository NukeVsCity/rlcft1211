package com.lycanitesmobs.compat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

/** 1.12 Potion registry lookups (Potion.REGISTRY / getPotionById / getIdFromPotion) on the 1.21 MOB_EFFECT registry. */
public final class LycEffects {
   private LycEffects() {
   }

   public static Holder<MobEffect> get(ResourceLocation id) {
      return BuiltInRegistries.MOB_EFFECT.getHolder(id).map(h -> (Holder<MobEffect>)h).orElse(null);
   }

   public static Holder<MobEffect> get(String id) {
      return get(ResourceLocation.parse(id));
   }

   public static int id(Holder<MobEffect> effect) {
      return BuiltInRegistries.MOB_EFFECT.getId(effect.value());
   }

   public static Holder<MobEffect> byId(int id) {
      return BuiltInRegistries.MOB_EFFECT.getHolder(id).map(h -> (Holder<MobEffect>)h).orElse(null);
   }

   public static ResourceLocation key(Holder<MobEffect> effect) {
      return effect.unwrapKey().map(k -> k.location()).orElse(null);
   }
}
