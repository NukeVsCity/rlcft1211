package com.github.fnar.minecraft.item;

import java.util.Objects;

public class Material extends RldBaseItem {
   private final Material.Type type;

   public Material(Material.Type type) {
      this.type = type;
   }

   public static Material clayBall() {
      return new Material(Material.Type.CLAY_BALL);
   }

   @Override
   public ItemType getItemType() {
      return ItemType.MATERIAL;
   }

   public Material.Type getMaterialType() {
      return this.type;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Material material = (Material)o;
         return this.type == material.type;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type);
   }

   public static enum Type {
      BONE,
      BOOK,
      CLAY_BALL,
      COAL,
      DIAMOND,
      EMERALD,
      FEATHER,
      FLINT,
      GOLD_INGOT,
      GOLD_NUGGET,
      IRON_INGOT,
      IRON_NUGGET,
      LEATHER,
      PAPER,
      SLIME_BALL,
      SNOWBALL,
      STICK,
      STRING,
      WHEAT;

      public Material asItem() {
         return new Material(this);
      }

      public RldItemStack asItemStack() {
         return this.asItem().asStack();
      }
   }
}
