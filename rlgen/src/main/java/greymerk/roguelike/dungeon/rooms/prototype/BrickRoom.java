package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.spawner.MobType;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class BrickRoom extends BaseRoom {
   public BrickRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 4;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      int x = at.getX();
      int y = at.getY();
      int z = at.getZ();
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 3, y, z - 3), new Coord(x + 3, y + 3, z + 3)));
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 1, y + 4, z - 1), new Coord(x + 1, y + 4, z + 1)));
      RectHollow.newRect(new Coord(x - 4, y - 1, z - 4), new Coord(x + 4, y + 4, z + 4)).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      RectSolid.newRect(new Coord(x - 4, y - 1, z - 4), new Coord(x + 4, y - 1, z + 4)).fill(this.worldEditor, this.primaryFloorBrush(), false, true);
      Coord cursor = new Coord(x, y, z);
      cursor.translate(Direction.UP, 5);
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
      cursor.translate(Direction.UP, 1);
      this.primaryWallBrush().stroke(this.worldEditor, cursor);

      for (Direction dir : Direction.CARDINAL) {
         cursor = new Coord(x, y, z);
         cursor.translate(dir, 1);
         cursor.translate(Direction.UP, 5);
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor, false, true);
         cursor.translate(dir.antiClockwise(), 1);
         this.primaryWallBrush().stroke(this.worldEditor, cursor, false, true);
         cursor = new Coord(x, y, z);
         cursor.translate(dir, 2);
         cursor.translate(Direction.UP, 4);
         SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
         cursor.translate(Direction.UP, 1);
         this.primaryWallBrush().stroke(this.worldEditor, cursor, false, true);
         cursor = new Coord(x, y, z);
         cursor.translate(dir, 3);
         cursor.translate(dir.antiClockwise(), 3);
         Coord start = cursor.copy();
         cursor.translate(Direction.UP, 2);
         Coord end = cursor.copy();
         this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         cursor.translate(Direction.UP, 1);
         this.primaryWallBrush().stroke(this.worldEditor, cursor);

         for (Direction orthogonals : dir.orthogonals()) {
            cursor = new Coord(x, y, z);
            cursor.translate(dir, 3);
            cursor.translate(orthogonals, 2);
            cursor.translate(Direction.UP, 3);
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonals.reverse()).stroke(this.worldEditor, cursor);
         }

         cursor = new Coord(x, y, z);
         cursor.translate(dir, 2);
         cursor.translate(dir.antiClockwise(), 2);
         cursor.translate(Direction.UP, 4);
         this.primaryWallBrush().stroke(this.worldEditor, cursor, false, true);

         for (Direction orthogonals : dir.orthogonals()) {
            cursor = new Coord(x, y, z);
            cursor.translate(Direction.UP, 4);
            cursor.translate(dir, 2);
            cursor.translate(orthogonals, 1);
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonals.reverse()).stroke(this.worldEditor, cursor, false, true);
         }

         cursor = new Coord(x, y, z);
         cursor.translate(dir, 1);
         cursor.translate(dir.antiClockwise(), 1);
         cursor.translate(Direction.UP, 5);
         this.primaryWallBrush().stroke(this.worldEditor, cursor, false, true);
      }

      Direction entrance = this.getEntrance(entrances);
      Coord spawnerCoord = at.copy().translate(entrance, this.random().nextInt(3) - 1).translate(entrance.right(), this.random().nextInt(3) - 1);
      this.generateSpawner(spawnerCoord, new MobType[0]);
      this.generateChest(at, entrance.reverse());
      this.generateDoorways(at, entrances);
      return this;
   }

   private void generateChest(Coord origin, Direction facing) {
      Coord chestLocation = this.generateChestLocation(origin);
      new TreasureChest(chestLocation, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.chooseRandomAmong(this.random(), ChestType.COMMON_TREASURES)))
         .withFacing(facing)
         .withTrap(TreasureChest.shouldBeTrapped(this.random(), this.levelSettings.getLevel()))
         .stroke(this.worldEditor, chestLocation);
   }
}
