package com.github.fnar.roguelike.dungeon.rooms;

import com.github.fnar.minecraft.block.spawner.MobType;
import com.github.fnar.roguelike.worldgen.generatables.Generatable;
import com.github.fnar.roguelike.worldgen.generatables.Pillar;
import com.github.fnar.roguelike.worldgen.generatables.Staircase3x3;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.List;

public class Platforms2Room extends BaseRoom {
   public Platforms2Room(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 8;
      this.ceilingHeight = 4;
      this.depth = 3;
   }

   @Override
   protected void generateDecorations(Coord at, List<Direction> entrances) {
      Coord floorLevel = at.copy().down(this.depth - 1);
      this.generate3x3Platforms(floorLevel);

      for (Direction cardinal : Direction.cardinals()) {
         Direction side = this.random().nextBoolean() ? cardinal.left() : cardinal.right();
         this.generate3x3Staircase(floorLevel.copy().down().translate(cardinal, 6).translate(side, 3), side);
      }

      this.generatePillars(at, entrances);
      this.fillWithLiquid(floorLevel.copy());
   }

   private void generate3x3Platforms(Coord floorLevel) {
      if (this.random().nextBoolean()) {
         this.generate3x3Platform(floorLevel.copy());
      }

      Direction direction = Direction.randomCardinal(this.random());
      if (this.random().nextBoolean()) {
         this.generate3x3Platform(floorLevel.copy().translate(direction, 3));
      }

      if (this.random().nextBoolean()) {
         this.generate3x3Platform(floorLevel.copy().translate(direction.reverse(), 3));
      }

      for (Direction cardinal : Direction.cardinals()) {
         this.generate3x3Platform(floorLevel.copy().translate(cardinal, 6));
      }
   }

   private void generate3x3Platform(Coord at) {
      (this.random().nextBoolean() ? this.primaryFloorBrush() : this.secondaryFloorBrush()).fill(this.worldEditor, at.copy().newRect(2).withHeight(2));
      if (this.random().nextDouble() < 0.2) {
         this.generateSpawner(at, new MobType[0]);
      }
   }

   private void generate3x3Staircase(Coord at, Direction facing) {
      Staircase3x3.newStaircase(this.worldEditor).withTheme(this.theme()).withFacing(facing).generate(at);
   }

   private void generatePillars(Coord at, List<Direction> entrances) {
      this.generateCardinalPillars(at, entrances);
      this.generateCornerPillars(at);
   }

   private void generateCardinalPillars(Coord at, List<Direction> entrances) {
      Pillar pillar = this.pillar();

      for (Direction cardinal : Direction.cardinals()) {
         if (!entrances.contains(cardinal)) {
            pillar.generate(at.copy().translate(cardinal, this.getWallDist() - 1));
         }
      }
   }

   private void generateCornerPillars(Coord at) {
      Generatable pillar = this.pillar();

      for (Direction cardinal : Direction.cardinals()) {
         pillar.generate(at.copy().translate(cardinal, this.getWallDist() - 1).translate(cardinal.left(), this.getWallDist() - 1));
      }
   }

   private Pillar pillar() {
      return Pillar.newPillar(this.worldEditor)
         .withHeight(this.getCeilingHeight())
         .withPillar(this.secondaryPillarBrush())
         .withStairs(this.secondaryStairBrush());
   }

   private void fillWithLiquid(Coord at) {
      if (this.random().nextDouble() < 0.5) {
         this.primaryLiquidBrush().fill(this.worldEditor, at.newRect(this.getWallDist()).withHeight(1 + this.random().nextInt(2)), true, false);
      }
   }
}
