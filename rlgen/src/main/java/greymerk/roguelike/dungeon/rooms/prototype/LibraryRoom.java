package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.decorative.FlowerPotBlock;
import com.github.fnar.minecraft.block.decorative.TallPlant;
import com.github.fnar.minecraft.block.decorative.TorchBlock;
import com.github.fnar.minecraft.block.normal.ColoredBlock;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.util.DyeColor;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class LibraryRoom extends BaseRoom {
   public LibraryRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 7;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      int x = at.getX();
      int y = at.getY();
      int z = at.getZ();
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 4, y, z - 4), new Coord(x + 4, y + 3, z + 4)));
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 3, y + 4, z - 3), new Coord(x + 3, y + 6, z + 3)));
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 2, y + 7, z - 2), new Coord(x + 2, y + 7, z + 2)));
      RectHollow.newRect(new Coord(x - 5, y, z - 5), new Coord(x + 5, y + 4, z + 5)).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      RectHollow.newRect(new Coord(x - 4, y + 3, z - 4), new Coord(x + 4, y + 7, z + 4)).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      RectHollow.newRect(new Coord(x - 3, y + 6, z - 3), new Coord(x + 3, y + 8, z + 3)).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      this.primaryFloorBrush().fill(this.worldEditor, RectSolid.newRect(new Coord(x - 5, y - 1, z - 5), new Coord(x + 5, y - 1, z + 5)));
      Coord start = at.copy();
      start.up(5);
      BlockType.REDSTONE_BLOCK.getBrush().stroke(this.worldEditor, start);
      start.down();
      BlockType.REDSTONE_LAMP_LIT.getBrush().stroke(this.worldEditor, start);
      start = at.copy();
      start.up(6);
      Coord end = start.copy();
      end.up();
      this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));

      for (Direction dir : Direction.CARDINAL) {
         if (!entrances.contains(dir)) {
            if (this.random().nextBoolean()) {
               this.desk(this.worldEditor, this.theme(), dir, at);
            } else {
               this.plants(this.worldEditor, this.theme(), dir, at);
            }
         }

         start = at.copy();
         start.translate(dir, 4);
         start.translate(dir.antiClockwise(), 4);
         end = start.copy();
         end.up(4);
         this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         start = at.copy();
         start.translate(dir, 3);
         start.translate(dir.antiClockwise(), 3);
         start.up(3);
         end = start.copy();
         end.up(3);
         this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         Coord cursor = end.copy();
         cursor.translate(dir.reverse());
         cursor.translate(dir.clockwise());
         cursor.up();
         this.primaryWallBrush().stroke(this.worldEditor, cursor);

         for (Direction o : dir.orthogonals()) {
            cursor = at.copy();
            cursor.translate(dir, 4);
            cursor.translate(o, 3);
            cursor.up(2);
            this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
            cursor.up();
            this.primaryWallBrush().stroke(this.worldEditor, cursor);
            cursor.translate(o.reverse());
            this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
            cursor.up(3);
            cursor.translate(dir.reverse());
            this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
         }

         cursor = at.copy();
         cursor.up(4);
         cursor.translate(dir);
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor);
         cursor.translate(dir, 2);
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
         cursor.up();
         start = cursor.copy();
         end = cursor.copy();
         start.translate(dir.reverse(), 2);
         this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         cursor.up();
         this.primaryWallBrush().stroke(this.worldEditor, cursor);
         cursor.up();
         cursor.translate(dir.reverse());
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(dir.reverse());
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor);
      }

      this.generateDoorways(at, entrances);
      return this;
   }

   private void desk(WorldEditor editor, Theme theme, Direction dir, Coord pos) {
      BlockBrush shelf = BlockType.BOOKSHELF.getBrush();
      Coord cursor = pos.copy();
      cursor.translate(dir, 5);
      Coord start = cursor.copy();
      Coord end = cursor.copy();
      start.translate(dir.antiClockwise(), 2);
      end.translate(dir.clockwise(), 2);
      end.up(2);
      SingleBlockBrush.AIR.fill(editor, RectSolid.newRect(start, end));
      start.translate(dir);
      end.translate(dir);
      theme.getPrimary().getWall().fill(editor, RectSolid.newRect(start, end), false, true);

      for (Direction o : dir.orthogonals()) {
         Coord c = cursor.copy();
         c.translate(o, 2);
         c.up(2);
         theme.getPrimary().getStair().setUpsideDown(true).setFacing(o.reverse()).stroke(editor, c);
         c.translate(dir);
         c.down();
         shelf.stroke(editor, c);
         c.down();
         shelf.stroke(editor, c);
      }

      cursor = pos.copy();
      cursor.translate(dir, 4);
      StairsBlock stair = StairsBlock.oak();
      stair.setUpsideDown(false).setFacing(dir).stroke(editor, cursor);
      cursor.translate(dir);
      stair.setUpsideDown(true).setFacing(dir.reverse()).stroke(editor, cursor);
      cursor.translate(dir.antiClockwise());
      stair.setUpsideDown(true).setFacing(dir.clockwise()).stroke(editor, cursor);
      cursor.translate(dir.clockwise(), 2);
      stair.setUpsideDown(true).setFacing(dir.antiClockwise()).stroke(editor, cursor);
      cursor.up();
      FlowerPotBlock.flowerPot().withRandomContent(editor.getRandom()).stroke(editor, cursor);
      cursor.translate(dir.antiClockwise());
      ColoredBlock.carpet().setColor(DyeColor.GREEN).stroke(editor, cursor);
      cursor.translate(dir.antiClockwise());
      TorchBlock.torch().setFacing(Direction.UP).stroke(editor, cursor);
   }

   private void plants(WorldEditor editor, Theme theme, Direction dir, Coord origin) {
      Coord cursor = origin.copy();
      cursor.translate(dir, 5);
      Coord start = cursor.copy();
      Coord end = cursor.copy();
      start.translate(dir.antiClockwise(), 2);
      end.translate(dir.clockwise(), 2);
      end.up(2);
      SingleBlockBrush.AIR.fill(editor, RectSolid.newRect(start, end));
      start.translate(dir);
      end.translate(dir).up();
      theme.getPrimary().getWall().fill(editor, RectSolid.newRect(start, end), false, true);

      for (Direction o : dir.orthogonals()) {
         Coord c = cursor.copy();
         c.translate(o, 2);
         c.up(2);
         theme.getPrimary().getStair().setUpsideDown(true).setFacing(o.reverse()).stroke(editor, c);
      }

      start = cursor.copy().translate(dir.antiClockwise());
      end = cursor.copy().translate(dir.clockwise());

      for (Coord c : RectSolid.newRect(start, end)) {
         TallPlant.placePlant(editor, c, dir.reverse());
      }
   }
}
