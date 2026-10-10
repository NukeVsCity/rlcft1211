package com.github.fnar.minecraft.block.decorative;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.WorldEditor;

public class TallPlantBlock extends SingleBlockBrush {
   private TallPlant tallPlant = TallPlant.LARGE_FERN;
   private boolean isTop;

   public TallPlantBlock() {
      super(BlockType.TALL_PLANT);
   }

   public TallPlantBlock setTallPlant(TallPlant tallPlant) {
      this.tallPlant = tallPlant;
      return this;
   }

   public TallPlant getTallPlant() {
      return this.tallPlant;
   }

   public boolean isTop() {
      return this.isTop;
   }

   public TallPlantBlock setTop(boolean top) {
      this.isTop = top;
      return this;
   }

   public TallPlantBlock getTop() {
      return tallPlantBlock().setTallPlant(this.getTallPlant()).setTop(true);
   }

   public TallPlantBlock getBottom() {
      return tallPlantBlock().setTallPlant(this.getTallPlant()).setTop(false);
   }

   public static TallPlantBlock tallPlantBlock() {
      return new TallPlantBlock();
   }

   @Override
   public boolean stroke(WorldEditor editor, Coord pos, boolean fillAir, boolean replaceSolid) {
      boolean isSuccess = super.stroke(editor, pos, fillAir, replaceSolid);
      if (!this.isTop()) {
         isSuccess = isSuccess && this.getTop().stroke(editor, pos.copy().up(), fillAir, replaceSolid);
      }

      return isSuccess;
   }

   public TallPlantBlock copy() {
      TallPlantBlock copy = new TallPlantBlock();
      copy.setFacing(this.getFacing());
      copy.setTallPlant(this.tallPlant);
      copy.setTop(this.isTop);
      return copy;
   }
}
