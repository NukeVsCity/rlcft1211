package com.github.fnar.roguelike.worldgen.generatables.thresholds;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.normal.ColoredBlock;
import com.github.fnar.roguelike.worldgen.generatables.BaseGeneratable;
import greymerk.roguelike.util.DyeColor;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;

public class WoolDoorway extends BaseGeneratable {
   public WoolDoorway(WorldEditor worldEditor) {
      super(worldEditor);
   }

   public WoolDoorway generate(Coord at) {
      RectSolid rect = RectSolid.newRect(at.copy().translate(this.facing.left()), at.copy().translate(this.facing.right()).up(2));
      this.getBlockThing(this.facing).fill(this.worldEditor, rect);
      return this;
   }

   private SingleBlockBrush getBlockThing(Direction facing) {
      switch (facing) {
         case NORTH:
            return ColoredBlock.wool().setColor(DyeColor.RED);
         case EAST:
            return ColoredBlock.wool().setColor(DyeColor.BLUE);
         case SOUTH:
            return ColoredBlock.wool().setColor(DyeColor.GREEN);
         case WEST:
            return ColoredBlock.wool().setColor(DyeColor.YELLOW);
         case UP:
            return ColoredBlock.wool().setColor(DyeColor.PURPLE);
         case DOWN:
            return ColoredBlock.wool().setColor(DyeColor.BLACK);
         default:
            return null;
      }
   }
}
