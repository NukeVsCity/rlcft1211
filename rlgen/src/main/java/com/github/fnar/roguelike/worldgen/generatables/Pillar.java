package com.github.fnar.roguelike.worldgen.generatables;

import com.github.fnar.minecraft.block.normal.StairsBlock;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;

public class Pillar extends BaseGeneratable {
   private int height = 3;
   private boolean withSupports = true;

   private Pillar(WorldEditor worldEditor) {
      super(worldEditor);
   }

   public Pillar generate(Coord at) {
      Coord top = at.copy().up(this.getHeight() - 1);
      this.worldEditor.fillDown(top, this.pillar);
      if (this.withSupports) {
         for (Direction cardinal : Direction.CARDINAL) {
            Coord support = top.copy().translate(cardinal);
            this.stairs.setUpsideDown(true).setFacing(cardinal).stroke(this.worldEditor, support, true, false);
         }
      }

      return this;
   }

   public static Pillar newPillar(WorldEditor worldEditor) {
      return new Pillar(worldEditor);
   }

   public int getHeight() {
      return Math.max(1, this.height);
   }

   public Pillar withHeight(int height) {
      this.height = Math.max(1, height);
      return this;
   }

   public Pillar withSupports(boolean withSupports) {
      this.withSupports = withSupports;
      return this;
   }

   public Pillar withPillar(BlockBrush pillarBrush) {
      super.withPillar(pillarBrush);
      return this;
   }

   public Pillar withStairs(StairsBlock stairBrush) {
      super.withStairs(stairBrush);
      return this;
   }

   public Pillar withLevelSettings(LevelSettings levelSettings) {
      super.withLevelSettings(levelSettings);
      this.withTheme(levelSettings.getTheme());
      return this;
   }
}
