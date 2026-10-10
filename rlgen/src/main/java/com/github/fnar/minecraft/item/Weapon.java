package com.github.fnar.minecraft.item;

import greymerk.roguelike.treasure.loot.Quality;
import java.util.Objects;

public class Weapon extends RldBaseItem {
   private WeaponType weaponType;
   private Quality quality = Quality.WOOD;

   public Weapon(WeaponType weaponType) {
      this.weaponType = weaponType;
   }

   public WeaponType getWeaponType() {
      return this.weaponType;
   }

   public Weapon withWeaponType(WeaponType weaponType) {
      this.weaponType = weaponType;
      return this;
   }

   public Quality getQuality() {
      return this.quality;
   }

   public Weapon withQuality(Quality quality) {
      this.quality = quality;
      return this;
   }

   public Weapon wooden() {
      this.withQuality(Quality.WOOD);
      return this;
   }

   public Weapon stone() {
      this.withQuality(Quality.STONE);
      return this;
   }

   public Weapon iron() {
      this.withQuality(Quality.IRON);
      return this;
   }

   public Weapon golden() {
      this.withQuality(Quality.GOLD);
      return this;
   }

   public Weapon diamond() {
      this.withQuality(Quality.DIAMOND);
      return this;
   }

   @Override
   public ItemType getItemType() {
      return ItemType.WEAPON;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Weapon weapon = (Weapon)o;
         return this.weaponType == weapon.weaponType && this.quality == weapon.quality;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.weaponType, this.quality);
   }

   @Override
   public String toString() {
      return "Weapon(super=" + super.toString() + ", weaponType=" + this.getWeaponType() + ", quality=" + this.getQuality() + ")";
   }
}
