package com.github.fnar.minecraft.block.redstone;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;

public class LeverBlock extends SingleBlockBrush {
   private boolean isActive;

   public LeverBlock() {
      super(BlockType.LEVER);
   }

   public LeverBlock setActive(boolean isActive) {
      this.isActive = isActive;
      return this;
   }

   public static LeverBlock lever() {
      return new LeverBlock();
   }

   public boolean isActive() {
      return this.isActive;
   }

   public LeverBlock copy() {
      LeverBlock copy = new LeverBlock();
      copy.setFacing(this.getFacing());
      copy.setActive(this.isActive);
      return copy;
   }
}
