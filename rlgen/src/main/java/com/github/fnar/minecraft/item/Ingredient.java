package com.github.fnar.minecraft.item;

public class Ingredient extends RldBaseItem {
   private final Ingredient.Type type;

   public Ingredient(Ingredient.Type type) {
      this.type = type;
   }

   public Ingredient.Type getType() {
      return this.type;
   }

   @Override
   public ItemType getItemType() {
      return ItemType.INGREDIENT;
   }

   public static enum Type {
      BLAZE_POWDER,
      BROWN_MUSHROOM,
      FERMENTED_SPIDER_EYE,
      FISH,
      GHAST_TEAR,
      GLASS_BOTTLE,
      GLOWSTONE_DUST,
      GUNPOWDER,
      MAGMA_CREAM,
      NETHER_WART,
      RABBIT_FOOT,
      REDSTONE,
      RED_MUSHROOM,
      SPECKLED_MELON,
      SPIDER_EYE,
      SUGAR;

      public Ingredient asItem() {
         return new Ingredient(this);
      }

      public RldItemStack asItemStack() {
         return this.asItem().asStack();
      }
   }
}
