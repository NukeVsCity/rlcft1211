package com.github.fnar.minecraft.tag;

public class IntTag implements Tag {
   private final int value;

   public IntTag(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   @Override
   public TagType getType() {
      return TagType.INT;
   }

   @Override
   public String toString() {
      return "IntTag(value=" + this.getValue() + ")";
   }
}
