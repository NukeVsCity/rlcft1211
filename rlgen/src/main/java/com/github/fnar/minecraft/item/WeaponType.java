package com.github.fnar.minecraft.item;

import greymerk.roguelike.treasure.loot.Equipment;
import java.util.Random;

public enum WeaponType {
   BOW,
   SWORD;

   public static WeaponType random(Random random) {
      int choice = random.nextInt(values().length);
      return values()[choice];
   }

   public Weapon asItem() {
      return new Weapon(this);
   }

   public Equipment asEquipment() {
      switch (this) {
         case BOW:
            return Equipment.BOW;
         case SWORD:
            return Equipment.SWORD;
         default:
            throw new IllegalArgumentException("Unexpected WeaponType: " + this);
      }
   }
}
