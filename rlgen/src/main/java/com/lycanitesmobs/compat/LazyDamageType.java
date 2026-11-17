package com.lycanitesmobs.compat;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

/**
 * A damage type holder for 1.12 DamageSources made at mod loading (ObjectManager.addDamageSource), before the damage type
 * registry exists: it reads the running server's registry when used.
 */
public final class LazyDamageType implements Holder<DamageType> {
   private final ResourceKey<DamageType> key;

   public LazyDamageType(ResourceKey<DamageType> key) {
      this.key = key;
   }

   private Holder<DamageType> real() {
      return LycDamage.registries().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(this.key);
   }

   @Override
   public Holder<DamageType> getDelegate() {
      return this.real();
   }

   @Override
   public DamageType value() {
      return this.real().value();
   }

   @Override
   public boolean isBound() {
      return LycDamage.registries() != null;
   }

   @Override
   public boolean is(ResourceLocation location) {
      return this.key.location().equals(location);
   }

   @Override
   public boolean is(ResourceKey<DamageType> resourceKey) {
      return this.key == resourceKey;
   }

   @Override
   public boolean is(Predicate<ResourceKey<DamageType>> predicate) {
      return predicate.test(this.key);
   }

   @Override
   public boolean is(TagKey<DamageType> tag) {
      return this.real().is(tag);
   }

   @Override
   public boolean is(Holder<DamageType> holder) {
      return holder.is(this.key);
   }

   @Override
   public Stream<TagKey<DamageType>> tags() {
      return this.real().tags();
   }

   @Override
   public Either<ResourceKey<DamageType>, DamageType> unwrap() {
      return Either.left(this.key);
   }

   @Override
   public Optional<ResourceKey<DamageType>> unwrapKey() {
      return Optional.of(this.key);
   }

   @Override
   public Kind kind() {
      return Kind.REFERENCE;
   }

   @Override
   public boolean canSerializeIn(HolderOwner<DamageType> owner) {
      return this.real().canSerializeIn(owner);
   }
}
