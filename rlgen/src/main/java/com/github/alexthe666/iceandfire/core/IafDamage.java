package com.github.alexthe666.iceandfire.core;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 1.12 IceAndFire.dragon / dragonFire / dragonIce / gorgon damage sources. The types are data (data/iceandfire/damage_type; dragon_fire
 * is in #minecraft:is_fire like setFireDamage, gorgon in #minecraft:bypasses_armor like setDamageBypassesArmor). The death message is
 * the 1.12 one: "<name> " + death.attack.<type>.player_<0|1>.
 */
public final class IafDamage {
   private IafDamage() {
   }

   public static final ResourceKey<DamageType> DRAGON = key("dragon");
   public static final ResourceKey<DamageType> DRAGON_FIRE = key("dragon_fire");
   public static final ResourceKey<DamageType> DRAGON_ICE = key("dragon_ice");
   public static final ResourceKey<DamageType> GORGON = key("gorgon");

   private static ResourceKey<DamageType> key(String name) {
      return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("iceandfire", name));
   }

   /** 1.12 IceAndFire.dragon (no attacker, like 1.12). */
   public static DamageSource dragon(Level level) {
      return source(level, DRAGON, null);
   }

   public static DamageSource dragonFire(Level level) {
      return source(level, DRAGON_FIRE, null);
   }

   public static DamageSource dragonIce(Level level) {
      return source(level, DRAGON_ICE, null);
   }

   public static DamageSource gorgon(Level level) {
      return source(level, GORGON, null);
   }

   public static DamageSource source(Level level, ResourceKey<DamageType> key, @Nullable Entity attacker) {
      Holder<DamageType> type = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
      return new IafDamageSource(type, attacker);
   }

   public static class IafDamageSource extends DamageSource {
      public IafDamageSource(Holder<DamageType> type, @Nullable Entity attacker) {
         super(type, attacker);
      }

      @Override
      public Component getLocalizedDeathMessage(LivingEntity entityLivingBaseIn) {
         String s = "death.attack." + this.getMsgId();
         String s1 = s + ".player_" + new Random().nextInt(2);
         return Component.empty().append(entityLivingBaseIn.getDisplayName()).append(" ")
            .append(Component.translatable(s1, entityLivingBaseIn.getDisplayName()));
      }
   }
}
