package com.github.fnar.roguelike.dungeon.rooms;

import com.github.fnar.minecraft.block.spawner.MobType;
import com.github.fnar.roguelike.worldgen.generatables.Generatable;
import com.github.fnar.roguelike.worldgen.generatables.Pillar;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.List;

public class SmallLiquidPitRoom extends BaseRoom {
   public SmallLiquidPitRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 3 + this.random().nextInt(4);
      this.ceilingHeight = Math.max(4, this.wallDist);
   }

   @Override
   protected void generateDecorations(Coord at, List<Direction> entrances) {
      this.generateLiquid(at);
      this.generatePillars(at);
      this.generateSpawners(at);
   }

   private void generateLiquid(Coord at) {
      int liquidHeight = 1 + this.random().nextInt(3);
      int radius = Math.max(1, this.getWallDist() - (this.random().nextBoolean() ? 1 : 0) - this.random().nextInt(this.getWallDist() / 2));
      this.primaryLiquidBrush().fill(this.worldEditor, at.newRect(radius).withHeight(liquidHeight).down(liquidHeight));
   }

   private void generatePillars(Coord at) {
      Generatable pillar = Pillar.newPillar(this.worldEditor).withHeight(this.getCeilingHeight()).withTheme(this.theme());
      if (this.getWallDist() >= 2) {
         for (Direction cardinal : Direction.cardinals()) {
            int pillarDist = this.getWallDist() - 1;
            pillar.generate(at.copy().translate(cardinal, pillarDist).translate(cardinal.left(), pillarDist));
         }
      }

      if (this.getWallDist() >= 6 && this.random().nextBoolean()) {
         for (Direction cardinal : Direction.cardinals()) {
            pillar.generate(at.copy().translate(cardinal).translate(cardinal.left()));
         }
      }
   }

   private void generateSpawners(Coord at) {
      for (Direction cardinal : Direction.cardinals()) {
         if (this.random().nextDouble() < 0.125) {
            Coord spawnerCoord = at.copy().translate(cardinal, this.getWallDist()).translate(cardinal.left(), this.getWallDist());
            this.generateSpawner(spawnerCoord, new MobType[0]);
         }
      }
   }
}
