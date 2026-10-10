package com.github.fnar.minecraft.item;

import java.util.Objects;

public class Food extends RldBaseItem {
   private final Food.Type type;

   public Food(Food.Type type) {
      this.type = type;
   }

   public Food.Type getFoodType() {
      return this.type;
   }

   @Override
   public ItemType getItemType() {
      return ItemType.FOOD;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Food food = (Food)o;
         return this.type == food.type;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type);
   }

   public static enum Type {
      APPLE,
      BAKED_POTATO,
      BREAD,
      CARROT,
      COOKED_BEEF,
      COOKED_CHICKEN,
      COOKED_FISH,
      COOKED_MUTTON,
      COOKED_PORKCHOP,
      COOKIE,
      GOLDEN_APPLE,
      GOLDEN_CARROT,
      MILK_BUCKET,
      MUSHROOM_STEW,
      POTATO,
      RAW_COD,
      ROTTEN_FLESH;

      public Food asItem() {
         return new Food(this);
      }
   }
}
