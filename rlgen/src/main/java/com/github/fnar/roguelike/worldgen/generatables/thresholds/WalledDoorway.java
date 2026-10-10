package com.github.fnar.roguelike.worldgen.generatables.thresholds;

import com.github.fnar.roguelike.worldgen.generatables.BaseGeneratable;
import greymerk.roguelike.theme.BlockSet;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;

public class WalledDoorway extends BaseGeneratable {
   public WalledDoorway(WorldEditor worldEditor) {
      super(worldEditor);
   }

   public WalledDoorway generate(Coord at) {
      RectSolid rect = RectSolid.newRect(at.copy().translate(this.facing.left(), 2), at.copy().translate(this.facing.right(), 2).up(2));
      BlockSet blockSet = this.worldEditor.getRandom().nextBoolean()
         ? this.levelSettings.getTheme().getPrimary()
         : this.levelSettings.getTheme().getSecondary();
      blockSet.getWall().fill(this.worldEditor, rect);
      return this;
   }
}
