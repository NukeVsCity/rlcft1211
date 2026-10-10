package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.roguelike.worldgen.generatables.Pillar;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class DungeonsSlime extends BaseRoom {
   public DungeonsSlime(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 8;
      this.ceilingHeight = 5;
   }

   public DungeonsSlime generate(Coord at, List<Direction> entrances) {
      super.generate(at, entrances);
      this.generateCorners(at);
      this.generateWaterways(at, entrances);
      this.generateRailings(at, entrances);
      this.generatePipes(at, entrances);
      return this;
   }

   private void generateCorners(Coord origin) {
      for (Direction dir : Direction.CARDINAL) {
         Coord cornerCoord = origin.copy();
         cornerCoord.translate(dir, 5);
         cornerCoord.translate(dir.antiClockwise(), 5);
         this.generateCorner(cornerCoord);
      }

      this.generateBeams(origin);
   }

   private void generateCorner(Coord origin) {
      this.generateCornerPool(origin);
      this.generateCornerPillars(origin);
      this.generateCornerExternalRailings(origin);
   }

   private void generateCornerPool(Coord origin) {
      Coord start = origin.copy().translate(-1, -1, -1);
      Coord end = origin.copy().translate(1, -1, 1);
      RectSolid poolCoords = RectSolid.newRect(start, end);
      this.primaryLiquidBrush().fill(this.worldEditor, poolCoords);
      poolCoords.translate(Direction.DOWN, 1);
      this.primaryWallBrush().fill(this.worldEditor, poolCoords);
   }

   private void generateCornerPillars(Coord origin) {
      for (Direction dir : Direction.CARDINAL) {
         Coord start = origin.copy().translate(dir, 2).translate(dir.antiClockwise(), 2);
         Pillar.newPillar(this.worldEditor).withPillar(this.primaryPillarBrush()).withStairs(this.primaryStairBrush()).withHeight(4).generate(start);
      }
   }

   private void generateCornerExternalRailings(Coord origin) {
      for (Direction dir : Direction.CARDINAL) {
         Coord start = origin.copy().translate(dir, 2).translate(dir.antiClockwise());
         Coord end = origin.copy().translate(dir, 2).translate(dir.clockwise());
         RectSolid railingCoords = RectSolid.newRect(start, end);
         BlockType.IRON_BAR.getBrush().fill(this.worldEditor, railingCoords);
      }
   }

   private void generateBeams(Coord at) {
      for (Direction dir : Direction.CARDINAL) {
         Coord start = at.copy().up(4).translate(dir, 3).translate(dir.antiClockwise(), 8);
         Coord end = at.copy().up(4).translate(dir, 3).translate(dir.clockwise(), 8);
         RectSolid wall = RectSolid.newRect(start, end);
         this.primaryWallBrush().fill(this.worldEditor, wall);
         wall.translate(dir, 4);
         this.primaryWallBrush().fill(this.worldEditor, wall);
      }
   }

   private void generateWaterways(Coord origin, List<Direction> entrances) {
      for (Direction dir : Direction.CARDINAL) {
         if (!entrances.contains(dir)) {
            this.generateWaterway(origin, dir);
         }
      }
   }

   private void generateWaterway(Coord origin, Direction dir) {
      Coord start = origin.copy().translate(dir, 4).translate(dir.antiClockwise(), 3);
      Coord end = origin.copy().translate(dir, 6).translate(dir.clockwise(), 3);
      RectSolid waterway = RectSolid.newRect(start, end);
      SingleBlockBrush.AIR.fill(this.worldEditor, waterway);
      waterway.translate(Direction.DOWN, 1);
      this.primaryLiquidBrush().fill(this.worldEditor, waterway);
      waterway.translate(Direction.DOWN, 1);
      this.primaryWallBrush().fill(this.worldEditor, waterway);
   }

   private void generatePipes(Coord origin, List<Direction> entrances) {
      for (Direction dir : Direction.CARDINAL) {
         if (!entrances.contains(dir)) {
            this.generatePipe(origin, dir);
         }
      }
   }

   private void generatePipe(Coord origin, Direction dir) {
      Coord cursor = origin.copy();
      cursor.translate(dir, 7);
      this.primaryWallBrush().stroke(this.worldEditor, cursor);
      cursor.up(2);
      this.primaryWallBrush().stroke(this.worldEditor, cursor);
      cursor.down();
      cursor.translate(dir);
      this.primaryLiquidBrush().stroke(this.worldEditor, cursor);
      this.generatePipeOpening(origin, dir);
   }

   private void generatePipeOpening(Coord origin, Direction dir) {
      for (Direction o : dir.orthogonals()) {
         Coord cursor = origin.copy();
         cursor.translate(dir, 7);
         cursor.translate(o);
         this.primaryStairBrush().setUpsideDown(true).setFacing(o).stroke(this.worldEditor, cursor);
         cursor.up();
         this.primaryWallBrush().stroke(this.worldEditor, cursor);
         cursor.up();
         this.primaryStairBrush().setUpsideDown(false).setFacing(o).stroke(this.worldEditor, cursor);
      }
   }

   private void generateRailings(Coord origin, List<Direction> entrances) {
      for (Direction dir : Direction.CARDINAL) {
         if (!entrances.contains(dir)) {
            this.generateRailing(origin, dir);
         }
      }
   }

   private void generateRailing(Coord origin, Direction dir) {
      Coord start = origin.copy().translate(dir, 3);
      Coord end = start.copy();
      start.translate(dir.antiClockwise(), 2);
      end.translate(dir.clockwise(), 2);
      BlockType.IRON_BAR.getBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
   }
}
