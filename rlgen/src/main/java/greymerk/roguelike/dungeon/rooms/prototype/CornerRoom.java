package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class CornerRoom extends BaseRoom {
   public CornerRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 2;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      this.createHollowCenter(at);
      this.createShell(at);
      this.fillFloor(at);
      this.createCornerWalls(at);
      this.createCeiling(at);
      this.generateDoorways(at, entrances);
      return this;
   }

   private void createCornerWalls(Coord origin) {
      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = origin.copy().translate(dir, 2).translate(dir.antiClockwise(), 2);
         Coord pillarStart = cursor.copy();
         Coord pillarEnd = cursor.copy().up(2);
         this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(pillarStart, pillarEnd));
         Coord pillarTop = cursor.copy().up();
         this.primaryWallBrush().stroke(this.worldEditor, pillarTop);
      }
   }

   private void createCeiling(Coord origin) {
      SingleBlockBrush.AIR.stroke(this.worldEditor, origin.copy().up(4));
      this.primaryWallBrush().stroke(this.worldEditor, origin.copy().up(5));

      for (Direction dir : Direction.CARDINAL) {
         Coord ceiling = origin.copy().translate(dir, 1).up(4);
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, ceiling);

         for (Direction orthogonal : dir.orthogonals()) {
            Coord decorativeCeiling = origin.copy().translate(dir, 2).translate(orthogonal, 1).up(3);
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal.reverse()).stroke(this.worldEditor, decorativeCeiling);
         }
      }
   }

   private void createHollowCenter(Coord origin) {
      Coord hollowAirCorner0 = origin.add(-2, 0, -2);
      Coord hollowAirCorner1 = origin.add(2, 3, 2);
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(hollowAirCorner0, hollowAirCorner1));
   }

   private void createShell(Coord origin) {
      Coord roomShellCorner0 = origin.add(-3, -1, -3);
      Coord roomShellCorner1 = origin.add(3, 4, 3);
      RectHollow.newRect(roomShellCorner0, roomShellCorner1).fill(this.worldEditor, this.primaryWallBrush(), false, true);
   }

   private void fillFloor(Coord origin) {
      Coord floorCorner0 = origin.add(-3, -1, -3);
      Coord floorCorner1 = origin.add(3, -1, 3);
      RectSolid.newRect(floorCorner0, floorCorner1).fill(this.worldEditor, this.primaryFloorBrush(), false, true);
   }
}
