package com.github.fnar.minecraft.item;

public class Miscellaneous extends RldBaseItem {
   private final Miscellaneous.Type type;

   public Miscellaneous(Miscellaneous.Type type) {
      this.type = type;
   }

   public Miscellaneous.Type getType() {
      return this.type;
   }

   @Override
   public ItemType getItemType() {
      return ItemType.MISCELLANEOUS;
   }

   public static enum Type {
      BOOK,
      ENCHANTED_BOOK,
      ENDER_EYE,
      ENDER_PEARL,
      EXPERIENCE_BOTTLE,
      GLASS_BOTTLE,
      LEAD,
      SADDLE;

      public Miscellaneous asItem() {
         return new Miscellaneous(this);
      }
   }
}
