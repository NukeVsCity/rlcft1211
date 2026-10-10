package com.github.fnar.minecraft.item;

public class Enchantment {
   private Enchantment.Effect effect;
   private int level = 1;

   public Enchantment.Effect getEnchant() {
      return this.effect;
   }

   public Enchantment withEffect(Enchantment.Effect effect) {
      this.effect = effect;
      return this;
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   public Enchantment withLevel(int level) {
      this.setLevel(level);
      return this;
   }

   public static enum Effect {
      AQUA_AFFINITY,
      BANE_OF_ARTHROPODS,
      BLAST_PROTECTION,
      DEPTH_STRIDER,
      EFFICIENCY,
      FEATHER_FALLING,
      FIRE_ASPECT,
      FIRE_PROTECTION,
      FLAME,
      FORTUNE,
      INFINITY,
      KNOCKBACK,
      LOOTING,
      LUCK_OF_THE_SEA,
      LURE,
      MENDING,
      POWER,
      PROJECTILE_PROTECTION,
      PROTECTION,
      PUNCH,
      RESPIRATION,
      SHARPNESS,
      SILK_TOUCH,
      SMITE,
      THORNS,
      UNBREAKING;

      public Enchantment asEnchantment() {
         return new Enchantment().withEffect(this);
      }

      public Enchantment atLevel(int level) {
         return this.asEnchantment().withLevel(level);
      }
   }
}
