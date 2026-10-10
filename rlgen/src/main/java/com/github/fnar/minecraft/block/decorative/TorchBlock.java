package com.github.fnar.minecraft.block.decorative;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;

public class TorchBlock extends SingleBlockBrush {
   private boolean isLit = true;

   public TorchBlock(BlockType blockType) {
      super(blockType);
   }

   public TorchBlock extinguish() {
      this.isLit = false;
      return this;
   }

   public boolean isLit() {
      return this.isLit;
   }

   public TorchBlock setLit(boolean isLit) {
      this.isLit = isLit;
      return this;
   }

   public static TorchBlock redstone() {
      return new TorchBlock(BlockType.REDSTONE_TORCH);
   }

   public static TorchBlock torch() {
      return new TorchBlock(BlockType.TORCH);
   }

   public TorchBlock copy() {
      TorchBlock copy = new TorchBlock(this.getBlockType());
      copy.setFacing(this.getFacing());
      copy.setLit(this.isLit);
      return copy;
   }
}
