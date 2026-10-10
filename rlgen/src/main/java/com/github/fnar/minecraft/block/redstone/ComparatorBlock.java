package com.github.fnar.minecraft.block.redstone;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;

public class ComparatorBlock extends SingleBlockBrush {
   private ComparatorBlock.Mode mode = ComparatorBlock.Mode.COMPARE;
   private boolean isPowered;

   public ComparatorBlock() {
      super(BlockType.COMPARATOR);
   }

   public ComparatorBlock setMode(ComparatorBlock.Mode mode) {
      this.mode = mode;
      return this;
   }

   public ComparatorBlock.Mode getMode() {
      return this.mode;
   }

   public ComparatorBlock setPowered(boolean isPowered) {
      this.isPowered = isPowered;
      return this;
   }

   public boolean isPowered() {
      return this.isPowered;
   }

   public static ComparatorBlock comparator() {
      return new ComparatorBlock();
   }

   public ComparatorBlock copy() {
      ComparatorBlock copy = new ComparatorBlock();
      copy.setFacing(this.getFacing());
      copy.setMode(this.mode);
      copy.setPowered(this.isPowered);
      return copy;
   }

   public static enum Mode {
      COMPARE,
      SUBTRACTION;
   }
}
