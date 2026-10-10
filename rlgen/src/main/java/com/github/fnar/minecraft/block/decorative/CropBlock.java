package com.github.fnar.minecraft.block.decorative;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.material.Crop;

public class CropBlock extends SingleBlockBrush {
   private Crop crop;

   public CropBlock() {
      super(BlockType.CROP);
   }

   public CropBlock setCrop(Crop crop) {
      this.crop = crop;
      return this;
   }

   public Crop getCrop() {
      return this.crop;
   }

   public static CropBlock crop() {
      return new CropBlock();
   }

   public CropBlock copy() {
      CropBlock copy = new CropBlock();
      copy.setFacing(this.getFacing());
      copy.setCrop(this.crop);
      return copy;
   }
}
