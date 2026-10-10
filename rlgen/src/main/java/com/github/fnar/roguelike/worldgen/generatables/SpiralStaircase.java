package com.github.fnar.roguelike.worldgen.generatables;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;

public class SpiralStaircase extends BaseGeneratable {
   private final WorldEditor worldEditor;
   private int height = 1;

   protected SpiralStaircase(WorldEditor worldEditor) {
      super(worldEditor);
      this.worldEditor = worldEditor;
   }

   public static SpiralStaircase newStaircase(WorldEditor worldEditor) {
      return new SpiralStaircase(worldEditor);
   }

   public SpiralStaircase generate(Coord at) {
      Coord layer = at.copy();

      for (int i = 0; i < this.height; i++) {
         SingleBlockBrush.AIR.fill(this.worldEditor, layer.newRect(2));
         this.pillar.stroke(this.worldEditor, layer);
         Direction dir = Direction.CARDINAL.get(layer.getY() % 4);
         Coord step = layer.copy();
         step.translate(dir);
         this.stairs.setUpsideDown(false).setFacing(dir.antiClockwise()).stroke(this.worldEditor, step);
         step.translate(dir.clockwise());
         this.stairs.setUpsideDown(true).setFacing(dir.clockwise()).stroke(this.worldEditor, step);
         step.translate(dir.reverse());
         this.stairs.setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, step);
         layer.up();
      }

      return this;
   }

   public SpiralStaircase withHeight(int height) {
      this.height = height;
      return this;
   }
}
