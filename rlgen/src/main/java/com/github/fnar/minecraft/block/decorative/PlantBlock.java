package com.github.fnar.minecraft.block.decorative;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;

public class PlantBlock extends SingleBlockBrush {
   private Plant plant;

   public PlantBlock() {
      super(BlockType.PLANT);
   }

   public PlantBlock setPlant(Plant plant) {
      this.plant = plant;
      return this;
   }

   public Plant getPlant() {
      return this.plant;
   }

   public static PlantBlock plant() {
      return new PlantBlock();
   }

   public PlantBlock copy() {
      PlantBlock copy = new PlantBlock();
      copy.setFacing(this.getFacing());
      copy.setPlant(this.plant);
      return copy;
   }
}
