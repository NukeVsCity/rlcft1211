package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.normal.StairsBlock;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.ArrayList;
import java.util.List;

public class StorageRoom extends BaseRoom {
   public StorageRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 7;
      this.ceilingHeight = 4;
   }

   private void pillarTop(Coord cursor) {
      StairsBlock stair = this.secondaryStairBrush();

      for (Direction dir : Direction.CARDINAL) {
         cursor.translate(dir, 1);
         stair.setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor, true, false);
         cursor.translate(dir.reverse(), 1);
      }
   }

   private void pillar(Coord base, int height) {
      Coord top = base.copy();
      top.up(height);
      this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(base, top));
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      super.generate(at, entrances);
      List<Coord> chestSpaces = new ArrayList<>();

      for (Direction dir : Direction.CARDINAL) {
         for (Direction orthogonals : dir.orthogonals()) {
            Coord cursor = at.copy().up(3).translate(dir, 2).translate(orthogonals, 2);
            this.pillarTop(cursor);
            cursor.translate(dir, 3).translate(orthogonals, 3);
            this.pillarTop(cursor);
            Coord start = cursor.copy();
            cursor.down().translate(dir, 1);
            this.pillarTop(cursor);
            Coord end = cursor.copy().down(3).translate(dir, 1).translate(orthogonals, 1);
            this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            cursor = at.copy().translate(dir, 2).translate(orthogonals, 2);
            this.pillar(cursor, 4);
            cursor.translate(dir, 4);
            this.pillar(cursor, 3);
            cursor.up(2);
            this.pillarTop(cursor);
            cursor.up(1);
            cursor.translate(dir.reverse(), 1);
            this.pillarTop(cursor);
            cursor.translate(dir.reverse(), 3);
            this.pillarTop(cursor);
            this.generateWall(at, dir, orthogonals);
            cursor = at.copy();
            cursor.translate(dir, 6);
            cursor.translate(orthogonals, 3);
            StairsBlock stair = this.secondaryStairBrush();
            stair.setUpsideDown(true).setFacing(dir.reverse());
            stair.stroke(this.worldEditor, cursor);
            cursor.translate(orthogonals, 1);
            stair.stroke(this.worldEditor, cursor);
            cursor.up(1);
            chestSpaces.add(cursor.copy());
            cursor.translate(orthogonals.reverse(), 1);
            chestSpaces.add(cursor.copy());
            start = at.copy();
            start.down();
            start.translate(dir, 3);
            start.translate(orthogonals, 3);
            end = start.copy();
            end.translate(dir, 3);
            end.translate(orthogonals, 1);
            this.secondaryFloorBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            cursor = at.copy();
            cursor.translate(dir, 5);
            cursor.translate(orthogonals, 5);
            this.pillar(cursor, 4);
         }
      }

      Coord.randomFrom(chestSpaces, 2, this.random())
         .forEach(
            coord -> new TreasureChest(coord, this.worldEditor)
                  .withChestType(this.getChestTypeOrUse(ChestType.chooseRandomAmong(this.random(), ChestType.SUPPLIES_TREASURES)))
                  .withFacing(coord.dirTo(at).reverse())
                  .withTrap(false)
                  .stroke(this.worldEditor, coord)
         );
      this.generateDoorways(at, entrances);
      return this;
   }

   private void generateWall(Coord origin, Direction dir, Direction orthogonals) {
      Coord start = origin.copy().translate(dir, 6).up(3);
      Coord end = start.copy().translate(orthogonals, 5);
      this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      start.translate(dir, 1);
      end.translate(dir, 1).down(3);
      RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), false, true);
   }
}
