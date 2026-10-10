package com.github.fnar.roguelike.worldgen.generatables;

import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;

public class Staircase3x3 extends BaseGeneratable {
   protected Staircase3x3(WorldEditor worldEditor) {
      super(worldEditor);
   }

   public static Staircase3x3 newStaircase(WorldEditor worldEditor) {
      return new Staircase3x3(worldEditor);
   }

   @Override
   public BaseGeneratable generate(Coord at) {
      Coord stairsCursor = at.copy().translate(this.facing);

      for (int x = 0; x < 3; x++) {
         for (int y = 0; y < x; y++) {
            Coord wallsCursor = stairsCursor.copy().up(y);
            this.walls
               .fill(this.worldEditor, RectSolid.newRect(wallsCursor.copy().translate(this.facing.left()), wallsCursor.copy().translate(this.facing.right())));
         }

         this.rowOfSteps(stairsCursor.copy().up(x), this.facing);
         stairsCursor.translate(this.facing.reverse());
      }

      return null;
   }

   private void rowOfSteps(Coord at, Direction facing) {
      this.stairs
         .setUpsideDown(false)
         .setFacing(facing)
         .fill(this.worldEditor, RectSolid.newRect(at.copy().translate(facing.left()), at.copy().translate(facing.right())), true, false);
   }
}
