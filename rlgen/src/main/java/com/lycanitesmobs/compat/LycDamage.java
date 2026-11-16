package com.lycanitesmobs.compat;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;

/**
 * 1.12 static DamageSource constants (DamageSource.IN_FIRE...). 1.21 damage sources need the registries: they come from the
 * running server (set on server start) or the client level.
 */
public final class LycDamage {
   private static RegistryAccess registries;
   private static DamageSources sources;

   private LycDamage() {
   }

   public static void setRegistries(RegistryAccess access) {
      registries = access;
      sources = access == null ? null : new DamageSources(access);
   }

   public static RegistryAccess registries() {
      return registries;
   }

   public static DamageSources sources() {
      return sources;
   }

   public static DamageSource src(ResourceKey<DamageType> key) {
      return sources.source(key);
   }
}
