package com.github.fnar.minecraft.block.redstone;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.Material;
import com.github.fnar.minecraft.block.SingleBlockBrush;

public class TrapdoorBlock extends SingleBlockBrush {
   private boolean isFlushWithTop;
   private boolean isOpen;

   public TrapdoorBlock(Material material) {
      super(BlockType.TRAPDOOR, material);
   }

   public static TrapdoorBlock wood() {
      return new TrapdoorBlock(Material.WOOD);
   }

   public static TrapdoorBlock iron() {
      return new TrapdoorBlock(Material.METAL);
   }

   public TrapdoorBlock setFlushWithTop(boolean isFlushWithTop) {
      this.isFlushWithTop = isFlushWithTop;
      return this;
   }

   public TrapdoorBlock setFlushWithTop() {
      this.isFlushWithTop = true;
      return this;
   }

   public boolean isFlushWithTop() {
      return this.isFlushWithTop;
   }

   public TrapdoorBlock setOpen(boolean isOpen) {
      this.isOpen = isOpen;
      return this;
   }

   public TrapdoorBlock setOpen() {
      this.isOpen = true;
      return this;
   }

   public boolean isOpen() {
      return this.isOpen;
   }

   public TrapdoorBlock copy() {
      TrapdoorBlock copy = new TrapdoorBlock(this.getMaterial());
      copy.setFacing(this.getFacing());
      copy.setFlushWithTop(this.isFlushWithTop);
      copy.setOpen(this.isOpen);
      return copy;
   }
}
