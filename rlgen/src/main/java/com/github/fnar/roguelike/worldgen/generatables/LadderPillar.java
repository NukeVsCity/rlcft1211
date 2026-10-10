package com.github.fnar.roguelike.worldgen.generatables;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;

public class LadderPillar extends BaseGeneratable {
   private int height = 1;

   protected LadderPillar(WorldEditor worldEditor) {
      super(worldEditor);
      this.withFacing(Direction.randomCardinal(worldEditor.getRandom()));
   }

   public static LadderPillar newLadderPillar(WorldEditor worldEditor) {
      return new LadderPillar(worldEditor);
   }

   @Override
   public BaseGeneratable generate(Coord at) {
      Coord layer = at.copy();

      for (int i = 0; i < this.height; i++) {
         BlockBrush layerFill = (BlockBrush)(i == this.height - 1 ? this.pillar : SingleBlockBrush.AIR);
         layerFill.fill(this.worldEditor, layer.newRect(2));
         this.pillar.stroke(this.worldEditor, layer);
         Coord ladder = layer.copy().translate(this.facing);
         BlockType.LADDER.getBrush().setFacing(this.facing.reverse()).stroke(this.worldEditor, ladder);
         layer.up();
      }

      return this;
   }

   public LadderPillar withHeight(int height) {
      this.height = height;
      return this;
   }
}
