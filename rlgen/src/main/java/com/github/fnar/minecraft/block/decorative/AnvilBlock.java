package com.github.fnar.minecraft.block.decorative;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.Material;
import com.github.fnar.minecraft.block.SingleBlockBrush;

public class AnvilBlock extends SingleBlockBrush {
   private AnvilBlock.Damage damage = AnvilBlock.Damage.NEW;

   public AnvilBlock() {
      super(BlockType.ANVIL, Material.METAL);
   }

   public static AnvilBlock anvil() {
      return new AnvilBlock();
   }

   public AnvilBlock setDamage(AnvilBlock.Damage damage) {
      this.damage = damage;
      return this;
   }

   public AnvilBlock.Damage getDamage() {
      return this.damage;
   }

   public AnvilBlock copy() {
      AnvilBlock copy = new AnvilBlock();
      copy.setFacing(this.getFacing());
      copy.setDamage(this.damage);
      return copy;
   }

   public static enum Damage {
      NEW,
      DAMAGED,
      VERY_DAMAGED;
   }
}
