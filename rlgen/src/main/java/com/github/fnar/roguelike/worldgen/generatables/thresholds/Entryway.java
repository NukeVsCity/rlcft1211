package com.github.fnar.roguelike.worldgen.generatables.thresholds;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.roguelike.worldgen.generatables.BaseGeneratable;
import com.github.fnar.roguelike.worldgen.generatables.Generatable;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;

public class Entryway extends BaseGeneratable implements Generatable {
   public Entryway(WorldEditor worldEditor) {
      super(worldEditor);
   }

   public Entryway generate(Coord at) {
      RectSolid rect = RectSolid.newRect(at.copy().translate(this.facing.left()), at.copy().translate(this.facing.right()).up(2));
      SingleBlockBrush.AIR.fill(this.worldEditor, rect);
      return this;
   }
}
