package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.normal.ColoredBlock;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.util.DyeColor;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class DungeonsEnchant extends BaseRoom {
   public DungeonsEnchant(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 7;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      Direction entrance = this.getEntrance(entrances);
      this.generateReversedBecauseEntrancesShouldBeOutwardFromRoomCenter(at, entrances, entrance.reverse());
      return this;
   }

   private void generateReversedBecauseEntrancesShouldBeOutwardFromRoomCenter(Coord at, List<Direction> entrances, Direction entrance) {
      Coord start = at.copy();
      start.translate(entrance, 5);
      Coord end = start.copy();
      start.translate(new Coord(-2, 0, -2));
      end.translate(new Coord(2, 3, 2));
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));
      start = at.copy();
      end = at.copy();
      start.translate(entrance.reverse(), 2);
      start.translate(entrance.antiClockwise(), 4);
      end.translate(entrance, 2);
      end.translate(entrance.clockwise(), 4);
      end.up(3);
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));
      start = at.copy();
      start.translate(entrance.reverse(), 2);
      end = start.copy();
      end.translate(entrance.reverse());
      start.translate(entrance.antiClockwise(), 2);
      end.translate(entrance.clockwise(), 2);
      end.up(3);
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));
      start = at.copy();
      start.up(4);
      end = start.copy();
      end.translate(entrance, 3);
      start.translate(entrance.antiClockwise(), 3);
      end.translate(entrance.clockwise(), 3);
      this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      start = at.copy();
      start.down();
      end = start.copy();
      start.translate(entrance.antiClockwise(), 4);
      end.translate(entrance.clockwise(), 4);
      start.translate(entrance.reverse(), 2);
      end.translate(entrance, 2);
      this.theme().getPrimary().getFloor().fill(this.worldEditor, RectSolid.newRect(start, end));
      start = at.copy();
      start.translate(entrance.reverse(), 4);
      end = start.copy();
      end.up(3);
      start.translate(entrance.antiClockwise());
      end.translate(entrance.clockwise());
      RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      Coord cursor = at.copy();
      cursor.translate(entrance, 5);

      for (Direction d : Direction.CARDINAL) {
         start = cursor.copy();
         start.translate(d, 2);
         start.translate(d.antiClockwise(), 2);
         end = start.copy();
         end.up(3);
         this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         if (d != entrance.reverse()) {
            start = cursor.copy();
            start.translate(d, 3);
            end = start.copy();
            start.translate(d.antiClockwise());
            end.translate(d.clockwise());
            end.up(2);
            ColoredBlock.stainedHardenedClay().setColor(DyeColor.PURPLE).fill(this.worldEditor, RectSolid.newRect(start, end));
            start = cursor.copy();
            start.translate(d, 2);
            start.up(3);
            end = start.copy();
            start.translate(d.antiClockwise());
            end.translate(d.clockwise());
            this.primaryStairBrush().setUpsideDown(true).setFacing(d.reverse()).fill(this.worldEditor, RectSolid.newRect(start, end));
            start.translate(d.reverse());
            start.up();
            end.translate(d.reverse());
            end.up();
            this.primaryStairBrush().setUpsideDown(true).setFacing(d.reverse()).fill(this.worldEditor, RectSolid.newRect(start, end));
         }
      }

      cursor = at.copy();
      cursor.translate(entrance, 5);
      cursor.up(4);
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
      cursor.up();
      this.primaryLightBrush().stroke(this.worldEditor, cursor);
      cursor.down();
      cursor.translate(entrance.reverse());
      this.primaryStairBrush().setUpsideDown(true).setFacing(entrance).stroke(this.worldEditor, cursor);
      cursor.translate(entrance.reverse());
      this.primaryWallBrush().stroke(this.worldEditor, cursor);
      cursor.translate(entrance.reverse());
      this.primaryStairBrush().setUpsideDown(true).setFacing(entrance.reverse()).stroke(this.worldEditor, cursor);
      cursor = at.copy();
      cursor.up(5);
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
      cursor.up();
      this.primaryWallBrush().stroke(this.worldEditor, cursor);

      for (Direction dx : Direction.CARDINAL) {
         start = at.copy();
         start.up(4);
         end = start.copy();
         if (dx == entrance) {
            end.translate(dx);
         } else {
            end.translate(dx, 2);
         }

         SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));

         for (Direction o : dx.orthogonals()) {
            Coord s = start.copy();
            s.translate(dx);
            Coord e = end.copy();
            s.translate(o);
            e.translate(o);
            this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).fill(this.worldEditor, RectSolid.newRect(s, e));
         }

         Coord s = start.copy();
         s.translate(dx);
         Coord e = end.copy();
         s.up();
         e.up();
         this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(s, e));
         cursor = at.copy();
         cursor.up(5);
         cursor.translate(dx);
         this.primaryStairBrush().setUpsideDown(true).setFacing(dx.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(dx.antiClockwise());
         this.primaryWallBrush().stroke(this.worldEditor, cursor);
      }

      start = at.copy();
      start.down();
      end = start.copy();
      start.translate(entrance, 3);
      end.translate(entrance, 7);
      start.translate(entrance.antiClockwise(), 2);
      end.translate(entrance.clockwise(), 2);
      ColoredBlock.stainedHardenedClay().setColor(DyeColor.PURPLE).fill(this.worldEditor, RectSolid.newRect(start, end));

      for (Direction o : entrance.orthogonals()) {
         start = at.copy();
         start.translate(entrance.reverse(), 3);
         start.translate(o, 3);
         end = start.copy();
         end.translate(entrance.reverse());
         end.up(3);
         this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         start = at.copy();
         start.translate(entrance, 3);
         start.translate(o, 3);
         end = start.copy();
         end.up(3);
         this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         cursor = end.copy();
         cursor.translate(entrance.reverse());
         this.primaryStairBrush().setUpsideDown(true).setFacing(entrance.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(o.reverse());
         this.primaryStairBrush().setUpsideDown(true).setFacing(entrance.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(o.reverse());
         this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(entrance);
         this.primaryStairBrush().setUpsideDown(true).setFacing(entrance).stroke(this.worldEditor, cursor);
         start = at.copy();
         start.translate(o, 4);
         end = start.copy();
         start.translate(o.antiClockwise());
         end.translate(o.clockwise());
         this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).fill(this.worldEditor, RectSolid.newRect(start, end));
         start.up(3);
         end.up(3);
         this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).fill(this.worldEditor, RectSolid.newRect(start, end));
         start.translate(o.reverse());
         start.up();
         end.translate(o.reverse());
         end.up();
         this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).fill(this.worldEditor, RectSolid.newRect(start, end));

         for (Direction r : o.orthogonals()) {
            start = at.copy();
            start.translate(o, 4);
            start.translate(r, 2);
            end = start.copy();
            end.up(3);
            this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         }

         start = at.copy();
         start.translate(o, 5);
         end = start.copy();
         start.translate(o.antiClockwise());
         end.translate(o.clockwise());
         start.up();
         end.up(2);
         ColoredBlock.stainedHardenedClay().setColor(DyeColor.PURPLE).fill(this.worldEditor, RectSolid.newRect(start, end));
         start = at.copy();
         start.translate(entrance.reverse(), 3);
         start.translate(o, 2);
         end = start.copy();
         end.up(3);
         this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         cursor = end.copy();
         cursor.translate(o.reverse());
         this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(entrance);
         this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(o);
         this.primaryStairBrush().setUpsideDown(true).setFacing(entrance).stroke(this.worldEditor, cursor);
         cursor.translate(o);
         this.primaryStairBrush().setUpsideDown(true).setFacing(entrance).stroke(this.worldEditor, cursor);
         cursor = at.copy();
         cursor.translate(entrance.reverse(), 2);
         cursor.translate(o);
         cursor.up(4);
         this.primaryWallBrush().stroke(this.worldEditor, cursor);
         cursor.translate(entrance);
         this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
      }

      cursor = at.copy();
      cursor.translate(entrance.reverse(), 2);
      cursor.up(4);
      this.primaryStairBrush().setUpsideDown(true).setFacing(entrance).stroke(this.worldEditor, cursor);
      cursor.translate(entrance.reverse());
      this.primaryWallBrush().stroke(this.worldEditor, cursor);
      cursor = at.copy();
      cursor.translate(entrance, 5);
      BlockType.ENCHANTING_TABLE.getBrush().stroke(this.worldEditor, cursor);
      Coord coord = this.generateChestLocation(at.copy().up());
      new TreasureChest(coord, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.ENCHANTING))
         .withFacing(this.getEntrance(entrances))
         .withTrap(false)
         .stroke(this.worldEditor, coord);
   }

   @Override
   protected Coord generateChestLocation(Coord origin) {
      Direction dir0 = Direction.randomCardinal(this.random());
      Direction dir1 = dir0.orthogonals()[this.random().nextBoolean() ? 0 : 1];
      return origin.copy().translate(dir0, 4).translate(dir1, 1);
   }
}
