package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.SingleBlockBrush;
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

public class RewardRoom extends BaseRoom {
   public RewardRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 7;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      int x = at.getX();
      int y = at.getY();
      int z = at.getZ();
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 7, y, z - 7), new Coord(x + 7, y + 5, z + 7)));
      RectHollow.newRect(new Coord(x - 8, y - 1, z - 8), new Coord(x + 8, y + 6, z + 8)).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(new Coord(x - 1, y + 4, z - 1), new Coord(x + 1, y + 5, z + 1)));

      for (Direction dir : Direction.CARDINAL) {
         for (Direction orthogonal : dir.orthogonals()) {
            Coord cursor = at.copy();
            cursor.translate(dir, 7);
            cursor.translate(orthogonal, 2);
            Coord start = cursor.copy();
            Coord end = start.copy();
            end.up(5);
            this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            cursor.translate(dir.reverse());
            this.primaryStairBrush().setUpsideDown(false).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
            cursor.up(2);
            this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
            cursor.up();
            start = cursor.copy();
            end = start.copy();
            end.up(2);
            this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            cursor.translate(dir.reverse());
            this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
            cursor.up();
            start = cursor.copy();
            end = start.copy();
            end.up();
            this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            cursor.up();
            cursor.translate(dir.reverse());
            this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
            start = at.copy();
            start.translate(dir, 7);
            start.up(3);
            end = start.copy();
            end.up(2);
            end.translate(orthogonal);
            this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            start.translate(dir.reverse());
            start.up();
            end.translate(dir.reverse());
            this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            start.translate(dir.reverse());
            start.up();
            end.translate(dir.reverse());
            this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            cursor = at.copy();
            cursor.translate(dir, 8);
            cursor.up(2);
            cursor.translate(orthogonal);
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal.reverse()).stroke(this.worldEditor, cursor, true, false);
            cursor.translate(dir.reverse());
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal.reverse()).stroke(this.worldEditor, cursor);
            cursor.translate(dir.reverse());
            cursor.up();
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal.reverse()).stroke(this.worldEditor, cursor);
            cursor.translate(dir.reverse());
            cursor.up();
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal.reverse()).stroke(this.worldEditor, cursor);
            cursor.translate(dir.reverse());
            cursor.up();
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal.reverse()).stroke(this.worldEditor, cursor);
            cursor.translate(dir.reverse(), 2);
            this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor);
            start = at.copy();
            start.translate(dir, 7);
            start.translate(orthogonal, 3);
            start.up(3);
            end = start.copy();
            end.up(2);
            end.translate(orthogonal, 2);
            this.theme().getPrimary().getPillar().fill(this.worldEditor, RectSolid.newRect(start, end));
            start.translate(dir.reverse());
            start.up();
            end.translate(dir.reverse());
            this.theme().getPrimary().getPillar().fill(this.worldEditor, RectSolid.newRect(start, end));
            cursor = at.copy();
            cursor.translate(dir, 7);
            cursor.translate(orthogonal, 3);
            this.primaryStairBrush().setUpsideDown(false).setFacing(orthogonal).stroke(this.worldEditor, cursor);
            cursor.translate(orthogonal, 2);
            this.primaryStairBrush().setUpsideDown(false).setFacing(orthogonal.reverse()).stroke(this.worldEditor, cursor);
            cursor.up(2);
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal.reverse()).stroke(this.worldEditor, cursor);
            cursor.translate(orthogonal.reverse(), 2);
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal).stroke(this.worldEditor, cursor);
            cursor.translate(dir.reverse());
            cursor.up();
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal).stroke(this.worldEditor, cursor);
            cursor.translate(orthogonal, 2);
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal.reverse()).stroke(this.worldEditor, cursor);
            cursor.translate(dir.reverse());
            cursor.up();
            end = cursor.copy();
            end.translate(orthogonal.reverse(), 2);
            this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).fill(this.worldEditor, RectSolid.newRect(cursor, end));
            cursor.up();
            end.up();
            this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(cursor, end));
            end.translate(dir.reverse());
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal).stroke(this.worldEditor, cursor);
            cursor = at.copy();
            cursor.translate(dir, 7);
            cursor.translate(orthogonal, 4);
            cursor.down();
            this.primaryLightBrush().stroke(this.worldEditor, cursor);
         }

         Direction o = dir.antiClockwise();
         Coord start = at.copy();
         start.translate(dir, 6);
         start.translate(o, 6);
         Coord end = start.copy();
         end.translate(dir);
         end.translate(o);
         end.up(5);
         this.theme().getPrimary().getPillar().fill(this.worldEditor, RectSolid.newRect(start, end));
         Coord cursor = at.copy();
         this.primaryWallBrush().stroke(this.worldEditor, cursor);
         cursor.translate(dir);
         this.primaryStairBrush().setUpsideDown(false).setFacing(dir).stroke(this.worldEditor, cursor);
         cursor.translate(o);
         this.primaryStairBrush().setUpsideDown(false).setFacing(dir).stroke(this.worldEditor, cursor);
         cursor.up(4);
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor);
         cursor.translate(o.reverse());
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor);
      }

      Coord cursor = at.copy();
      cursor.up(4);
      this.primaryLightBrush().stroke(this.worldEditor, cursor);
      cursor = at.copy();
      cursor.up();
      Direction chestFacing = this.getEntrance(entrances).reverse();
      new TreasureChest(cursor, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(this.getChestTypeOrUse(ChestType.REWARD)))
         .withFacing(chestFacing)
         .withTrap(false)
         .stroke(this.worldEditor, cursor);
      this.generateDoorways(at, entrances);
      return this;
   }
}
