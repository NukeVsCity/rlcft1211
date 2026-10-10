package com.github.fnar.roguelike.worldgen.generatables.thresholds;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.roguelike.worldgen.BlockPattern;
import com.github.fnar.roguelike.worldgen.generatables.BaseGeneratable;
import com.google.common.collect.Maps;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.Map;

public class IronBarredEntryway extends BaseGeneratable {
   public IronBarredEntryway(WorldEditor worldEditor) {
      super(worldEditor);
   }

   public IronBarredEntryway generate(Coord at) {
      Map<Character, BlockBrush> blockBrushMap = Maps.newHashMap();
      blockBrushMap.put('W', this.levelSettings.getTheme().getPrimary().getWall());
      blockBrushMap.put('#', BlockType.IRON_BAR.getBrush());
      String pattern = "W # # # W \n\nW # # # W \n\nW # # # W \n";
      new BlockPattern(this.worldEditor, pattern, blockBrushMap).stroke(at.copy().translate(this.facing.left(), 2), this.facing);
      return this;
   }
}
