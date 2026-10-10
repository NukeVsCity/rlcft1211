package com.github.fnar.minecraft.tag;

public class StringTag implements Tag {
   private final String value;

   public StringTag(String value) {
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }

   @Override
   public TagType getType() {
      return TagType.STRING;
   }

   @Override
   public String toString() {
      return "StringTag(value=" + this.getValue() + ")";
   }
}
