package com.github.fnar.minecraft.block.decorative;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.Material;
import com.github.fnar.minecraft.block.SingleBlockBrush;

public class PumpkinBlock extends SingleBlockBrush {
   private boolean isCarved;
   private boolean isLit;

   public PumpkinBlock() {
      super(BlockType.PUMPKIN, Material.NONE);
   }

   public static PumpkinBlock pumpkin() {
      return new PumpkinBlock();
   }

   public static PumpkinBlock jackOLantern() {
      return new PumpkinBlock().setLit();
   }

   public boolean isCarved() {
      return this.isCarved;
   }

   public PumpkinBlock setCarved() {
      this.isCarved = true;
      return this;
   }

   private PumpkinBlock setCarved(boolean isCarved) {
      this.isCarved = isCarved;
      return this;
   }

   public boolean isLit() {
      return this.isLit;
   }

   public PumpkinBlock setLit() {
      this.isLit = true;
      return this;
   }

   private PumpkinBlock setLit(boolean isLit) {
      this.isLit = isLit;
      return this;
   }

   public PumpkinBlock copy() {
      PumpkinBlock copy = new PumpkinBlock();
      copy.setFacing(this.getFacing());
      copy.setCarved(this.isCarved);
      copy.setLit(this.isLit);
      return copy;
   }
}
