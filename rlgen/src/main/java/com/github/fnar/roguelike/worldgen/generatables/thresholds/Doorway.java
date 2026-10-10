package com.github.fnar.roguelike.worldgen.generatables.thresholds;

import com.github.fnar.roguelike.worldgen.BlockPattern;
import com.github.fnar.roguelike.worldgen.generatables.BaseGeneratable;
import com.google.common.collect.Maps;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.Map;

public class Doorway extends BaseGeneratable {
   public Doorway(WorldEditor worldEditor) {
      super(worldEditor);
   }

   public Doorway generate(Coord at) {
      Map<Character, BlockBrush> blockBrushMap = Maps.newHashMap();
      blockBrushMap.put('#', this.levelSettings.getTheme().getPrimary().getWall());
      blockBrushMap.put('B', this.levelSettings.getTheme().getPrimary().getDoor().setFacing(this.facing));
      blockBrushMap.put('T', this.levelSettings.getTheme().getPrimary().getDoor().setTop().setFacing(this.facing));
      String pattern = "# # B # # \n\n# # T # # \n\n# # # # # \n";
      new BlockPattern(this.worldEditor, pattern, blockBrushMap).stroke(at.copy().translate(this.facing.left(), 2), this.facing);
      return this;
   }
}
