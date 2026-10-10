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

public class PyramidSpawnerRoom extends BaseRoom {
   public PyramidSpawnerRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 3;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      int x = at.getX();
      int y = at.getY();
      int z = at.getZ();
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 3, y, z - 3), new Coord(x + 3, y + 3, z + 3)));
      RectHollow.newRect(new Coord(x - 4, y - 1, z - 4), new Coord(x + 4, y + 4, z + 4)).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      RectSolid.newRect(new Coord(x - 3, y + 4, z - 3), new Coord(x + 3, y + 6, z + 3)).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 2, y + 4, z - 2), new Coord(x + 2, y + 4, z + 2)));
      RectSolid.newRect(new Coord(x - 4, y - 1, z - 4), new Coord(x + 4, y - 1, z + 4))
         .fill(this.worldEditor, this.theme().getPrimary().getFloor(), false, true);
      Coord cursor = at.copy();
      cursor.up(5);
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
      cursor.up();
      this.primaryWallBrush().stroke(this.worldEditor, cursor);
      cursor = at.copy();
      cursor.up(5);
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
      cursor.up();
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);

      for (Direction dir : Direction.CARDINAL) {
         cursor = at.copy();
         cursor.translate(dir, 3);
         cursor.translate(dir.antiClockwise(), 3);
         Coord start = cursor.copy();
         cursor.up(3);
         Coord end = cursor.copy();
         this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         cursor.up();
         this.primaryWallBrush().stroke(this.worldEditor, cursor);
         cursor = at.copy();
         cursor.up(4);
         cursor.translate(dir, 2);
         this.primaryWallBrush().stroke(this.worldEditor, cursor);
         cursor.translate(dir.antiClockwise(), 2);
         this.primaryWallBrush().stroke(this.worldEditor, cursor);
         cursor = at.copy();
         cursor.up(5);
         cursor.translate(dir.antiClockwise());
         SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
         cursor.up();
         SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);

         for (Direction orthogonals : dir.orthogonals()) {
            cursor = at.copy();
            cursor.up(3);
            cursor.translate(orthogonals);
            cursor.translate(dir, 3);
            this.primaryWallBrush().stroke(this.worldEditor, cursor);
            cursor = at.copy();
            cursor.up(4);
            cursor.translate(dir, 2);
            this.primaryWallBrush().stroke(this.worldEditor, cursor);
         }
      }

      Coord coord = this.generateChestLocation(at);
      new TreasureChest(coord, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.chooseRandomAmong(this.random(), ChestType.COMMON_TREASURES)))
         .withFacing(this.getEntrance(entrances).reverse())
         .withTrap(false)
         .stroke(this.worldEditor, coord);
      Coord cursor1 = at.copy();
      this.generateSpawner(cursor1, MobType.COMMON_MOBS);
      return this;
   }
}
