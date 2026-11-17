package com.lycanitesmobs.compat;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;

/**
 * 1.12 net.minecraft.util.EntityDamageSource / EntityDamageSourceIndirect with the 1.12 mutable flags
 * (setDamageBypassesArmor, setDamageIsAbsolute, setMagicDamage, setFireDamage, setProjectile): 1.21 keeps those as damage type tags,
 * so {@link #is(TagKey)} answers the flags on top of the damage type's own tags. getMsgId() is the 1.12 damageType string.
 */
public class EntityDamageSource extends DamageSource {
   private final String damageType;
   private boolean bypassesArmor;
   private boolean absolute;
   private boolean magic;
   private boolean fire;
   private boolean projectile;

   public EntityDamageSource(String damageType, @Nullable Entity source) {
      this(damageType, source, source);
   }

   public EntityDamageSource(String damageType, @Nullable Entity direct, @Nullable Entity source) {
      super(holder(source != null ? source : direct, typeKey(damageType)), direct, source);
      this.damageType = damageType;
   }

   protected EntityDamageSource(DamageSource copy) {
      super(copy.typeHolder(), copy.getDirectEntity(), copy.getEntity(), copy.sourcePositionRaw());
      this.damageType = copy.getMsgId();
   }

   public static ResourceKey<DamageType> typeKey(String damageType) {
      switch (damageType) {
         case "mob": return DamageTypes.MOB_ATTACK;
         case "player": return DamageTypes.PLAYER_ATTACK;
         case "thrown": return DamageTypes.THROWN;
         case "arrow": return DamageTypes.ARROW;
         case "indirectMagic": return DamageTypes.INDIRECT_MAGIC;
         case "magic": return DamageTypes.MAGIC;
         case "inFire": return DamageTypes.IN_FIRE;
         case "onFire": return DamageTypes.ON_FIRE;
         case "lava": return DamageTypes.LAVA;
         case "wither": return DamageTypes.WITHER;
         case "fall": return DamageTypes.FALL;
         case "drown": return DamageTypes.DROWN;
         case "outOfWorld": return DamageTypes.FELL_OUT_OF_WORLD;
         case "explosion": case "explosion.player": return DamageTypes.EXPLOSION;
         default: return DamageTypes.GENERIC;
      }
   }

   static Holder<DamageType> holder(@Nullable Entity entity, ResourceKey<DamageType> key) {
      RegistryAccess access = entity != null ? entity.level().registryAccess() : LycDamage.registries();
      if (access == null) {
         // made during mod loading (1.12 static damage sources): resolved when used
         return new LazyDamageType(key);
      }
      return access.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
   }

   public static EntityDamageSource named(String damageType) {
      return new EntityDamageSource(damageType, null);
   }

   public static EntityDamageSource causeMobDamage(Entity mob) {
      return new EntityDamageSource("mob", mob);
   }

   public static EntityDamageSource causePlayerDamage(Entity player) {
      return new EntityDamageSource("player", player);
   }

   public static EntityDamageSource causeThrownDamage(Entity projectile, @Nullable Entity thrower) {
      return new EntityDamageSource("thrown", projectile, thrower).setProjectile();
   }

   public static EntityDamageSource causeIndirectDamage(Entity direct, @Nullable Entity source) {
      return new EntityDamageSource("mob", direct, source);
   }

   public static EntityDamageSource causeIndirectMagicDamage(Entity direct, @Nullable Entity source) {
      return new EntityDamageSource("indirectMagic", direct, source).setDamageBypassesArmor().setMagicDamage();
   }

   public static EntityDamageSource copyOf(DamageSource source) {
      return source instanceof EntityDamageSource e ? e : new EntityDamageSource(source);
   }

   public EntityDamageSource setDamageBypassesArmor() {
      this.bypassesArmor = true;
      return this;
   }

   public EntityDamageSource setDamageIsAbsolute() {
      this.absolute = true;
      return this;
   }

   public EntityDamageSource setMagicDamage() {
      this.magic = true;
      return this;
   }

   public EntityDamageSource setFireDamage() {
      this.fire = true;
      return this;
   }

   public EntityDamageSource setProjectile() {
      this.projectile = true;
      return this;
   }

   public boolean isDamageAbsolute() {
      return this.absolute || this.is(DamageTypeTags.BYPASSES_RESISTANCE);
   }

   @Override
   public boolean is(TagKey<DamageType> tag) {
      if (this.bypassesArmor && tag == DamageTypeTags.BYPASSES_ARMOR) return true;
      if (this.absolute && (tag == DamageTypeTags.BYPASSES_EFFECTS || tag == DamageTypeTags.BYPASSES_RESISTANCE || tag == DamageTypeTags.BYPASSES_ENCHANTMENTS)) return true;
      if (this.magic && tag == DamageTypeTags.WITCH_RESISTANT_TO) return true;
      if (this.fire && tag == DamageTypeTags.IS_FIRE) return true;
      if (this.projectile && tag == DamageTypeTags.IS_PROJECTILE) return true;
      return super.is(tag);
   }

   @Override
   public String getMsgId() {
      return this.damageType;
   }
}
