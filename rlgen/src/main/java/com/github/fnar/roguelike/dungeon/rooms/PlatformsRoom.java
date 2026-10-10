package com.github.fnar.roguelike.dungeon.rooms;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import com.github.fnar.roguelike.worldgen.BlockPattern;
import com.github.fnar.roguelike.worldgen.shape.FnarLine;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlatformsRoom extends BaseRoom {
   public PlatformsRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 7;
      this.ceilingHeight = 6;
      this.depth = this.random().nextBoolean() ? 2 : 3;
   }

   @Override
   public void generateDecorations(Coord origin, List<Direction> entrances) {
      Direction front = this.getEntrance(entrances);
      this.generateIslands(origin, front);
      this.theFloorIsLava(origin);
      this.generateCeilingDecoration(origin);
   }

   private void generateIslands(Coord origin, Direction front) {
      BlockBrush pillar = this.primaryPillarBrush();
      Map<Character, BlockBrush> blockBrushMap = Stream.of(new Object[]{'.', SingleBlockBrush.AIR}, new Object[]{'x', pillar})
         .collect(Collectors.toMap(data -> (Character)data[0], data -> (BlockBrush)data[1]));

      for (int x = 0; x < 2; x++) {
         for (int z = 0; z < 2; z++) {
            int xOffset = (x == 0 ? -1 : 1) * (this.getWallDist() / 2);
            int zOffset = (z == 0 ? -1 : 1) * (this.getWallDist() / 2);
            Coord location = origin.copy().down(2).translate(front.left()).translate(front).translate(front.left(), xOffset).translate(front, zOffset);
            BlockPattern blockPattern = new BlockPattern(this.worldEditor, this.chooseRandomBlockPattern(), blockBrushMap);
            blockPattern.stroke(location, front, true, false);
            blockPattern.stroke(location.up(), front, true, false);
            if (this.random().nextBoolean()) {
               blockPattern.stroke(location.up(), front, true, false);
            }
         }
      }
   }

   private String chooseRandomBlockPattern() {
      String[] patterns = this.blockPatternStrings();
      return patterns[(int)(greymerk.roguelike.util.RldRandom.random() * (double)patterns.length)];
   }

   private String[] blockPatternStrings() {
      String blankPattern = "";
      String plusPatternString = ". x . \nx x x \n. x . \n";
      String squarePatternString = "x x x \nx x x \nx x x \n";
      String diamondPatternString = ". x x \nx x x \nx x . \n";
      return new String[]{blankPattern, plusPatternString, squarePatternString, diamondPatternString};
   }

   private void generateCeilingDecoration(Coord origin) {
      Direction.cardinals()
         .forEach(
            direction -> {
               StairsBlock stair = this.primaryStairBrush();
               Coord beamStart = origin.copy()
                  .translate(direction, this.getWallDist() / 2 - 1)
                  .translate(direction.left(), this.getWallDist() - 1)
                  .up(this.getCeilingHeight() - 2);
               Coord beamEnd = origin.copy()
                  .translate(direction, this.getWallDist() / 2 - 1)
                  .translate(direction.right(), this.getWallDist() - 1)
                  .up(this.getCeilingHeight() - 2);
               this.secondaryPillarBrush().setFacing(direction.right());
               new FnarLine(beamStart, beamEnd).fill(this.worldEditor, this.secondaryPillarBrush());
               stair.setUpsideDown(true).setFacing(direction.right()).stroke(this.worldEditor, beamStart.down(2));
               this.secondaryPillarBrush().stroke(this.worldEditor, beamStart.up());
               stair.stroke(this.worldEditor, beamStart.translate(direction.right()));
               stair.setUpsideDown(true).setFacing(direction.left()).stroke(this.worldEditor, beamEnd.down(2));
               this.secondaryPillarBrush().stroke(this.worldEditor, beamEnd.up());
               stair.stroke(this.worldEditor, beamEnd.translate(direction.left()));
            }
         );
   }
}
