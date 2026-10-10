package com.github.fnar.minecraft.item;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;

public abstract class RldBaseItem implements RldItem {
   private final List<Enchantment> enchantments = Lists.newArrayList();
   private int plzEnchantLevel = 0;

   public void addEnchantment(Enchantment enchantment) {
      this.enchantments.add(enchantment);
   }

   public RldBaseItem withEnchantment(Enchantment enchantment) {
      this.addEnchantment(enchantment);
      return this;
   }

   public RldBaseItem withEnchantment(Enchantment.Effect effect) {
      return this.withEnchantment(effect.asEnchantment());
   }

   public List<Enchantment> getEnchantments() {
      return this.enchantments;
   }

   public boolean isPlzEnchant() {
      return this.plzEnchantLevel != 0;
   }

   public int getPlzEnchantLevel() {
      return this.plzEnchantLevel;
   }

   public RldBaseItem plzEnchantAtLevel(int level) {
      this.plzEnchantLevel = level;
      return this;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         RldBaseItem that = (RldBaseItem)o;
         return this.plzEnchantLevel == that.plzEnchantLevel && this.enchantments.equals(that.enchantments);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.enchantments, this.plzEnchantLevel);
   }

   @Override
   public String toString() {
      return "RldBaseItem(enchantments=" + this.getEnchantments() + ", plzEnchantLevel=" + this.getPlzEnchantLevel() + ")";
   }
}
