package com.lycanitesmobs.core.config;

/** 1.12 net.minecraftforge.common.config.Property (the parts Lycanites uses). */
public class Property {
   public enum Type {
      STRING('S'), INTEGER('I'), BOOLEAN('B'), DOUBLE('D');

      public final char id;

      Type(char id) {
         this.id = id;
      }

      public static Type of(char c) {
         for (Type t : values()) {
            if (t.id == c) {
               return t;
            }
         }
         return STRING;
      }
   }

   private final String name;
   Type type;
   private String value;
   private String[] values;
   String comment;

   public Property(String name, String value, Type type) {
      this.name = name;
      this.value = value;
      this.type = type;
   }

   public Property(String name, String[] values, Type type) {
      this.name = name;
      this.values = values;
      this.type = type;
   }

   public String getName() {
      return this.name;
   }

   public boolean isList() {
      return this.values != null;
   }

   void setList(String[] values) {
      this.values = values;
      this.value = null;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public String getString() {
      return this.value == null ? "" : this.value;
   }

   public String[] getStringList() {
      return this.values == null ? new String[0] : this.values;
   }

   public boolean getBoolean(boolean def) {
      if ("true".equalsIgnoreCase(this.value)) {
         return true;
      }
      if ("false".equalsIgnoreCase(this.value)) {
         return false;
      }
      return def;
   }

   public boolean getBoolean() {
      return this.getBoolean(false);
   }

   public int getInt(int def) {
      try {
         return Integer.parseInt(this.value.trim());
      } catch (Exception e) {
         return def;
      }
   }

   public int getInt() {
      return this.getInt(0);
   }

   public double getDouble(double def) {
      try {
         return Double.parseDouble(this.value.trim());
      } catch (Exception e) {
         return def;
      }
   }

   public double getDouble() {
      return this.getDouble(0.0);
   }

   public void set(String v) {
      this.value = v;
      this.values = null;
   }

   public void set(boolean v) {
      this.set(Boolean.toString(v));
   }

   public void set(int v) {
      this.set(Integer.toString(v));
   }

   public void set(double v) {
      this.set(Double.toString(v));
   }

   public void set(String[] v) {
      this.values = v;
      this.value = null;
   }
}
