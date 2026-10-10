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
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class DungeonsMusic extends BaseRoom {
   public DungeonsMusic(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 6;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      Coord start = at.copy();
      Coord end = at.copy();
      start.translate(new Coord(-6, -1, -6));
      end.translate(new Coord(6, 5, 6));
      RectHollow.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      start = at.copy();
      end = at.copy();
      start.translate(new Coord(-6, 4, -6));
      end.translate(new Coord(6, 5, 6));
      this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      start = at.copy();
      end = at.copy();
      start.translate(new Coord(-3, 4, -3));
      end.translate(new Coord(3, 4, 3));
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));
      start = at.copy();
      end = at.copy();
      start.translate(new Coord(-3, -1, -3));
      end.translate(new Coord(3, -1, 3));
      this.secondaryFloorBrush().fill(this.worldEditor, RectSolid.newRect(start, end));

      for (int i = 2; i >= 0; i--) {
         start = at.copy();
         end = at.copy();
         start.translate(new Coord(-i - 1, 0, -i - 1));
         end.translate(new Coord(i + 1, 0, i + 1));
         BlockBrush carpet = ColoredBlock.carpet().setColor(DyeColor.chooseRandom(this.random()));
         carpet.fill(this.worldEditor, RectSolid.newRect(start, end));
      }

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = at.copy();
         cursor.translate(dir, 5);
         cursor.up(3);
         this.secondaryWallBrush().stroke(this.worldEditor, cursor);
         cursor.translate(dir.reverse());
         this.secondaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
         cursor = at.copy();
         cursor.translate(dir, 5);
         cursor.translate(dir.antiClockwise(), 5);
         this.pillar(cursor);
         start = at.copy();
         start.up(4);
         start.translate(dir, 3);
         end = start.copy();
         start.translate(dir.antiClockwise(), 3);
         end.translate(dir.clockwise(), 3);
         this.secondaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         cursor = at.copy();
         cursor.up(4);
         cursor.translate(dir);
         this.secondaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor);
         cursor.translate(dir);
         this.secondaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);

         for (Direction o : dir.orthogonals()) {
            cursor = at.copy();
            cursor.translate(dir, 5);
            cursor.translate(o, 2);
            this.pillar(cursor);
            cursor = at.copy();
            cursor.translate(dir, 4);
            cursor.up(3);
            cursor.translate(o);
            this.secondaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
            cursor = at.copy();
            cursor.translate(dir, 5);
            cursor.translate(o, 3);
            this.secondaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
            cursor.translate(o);
            this.secondaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
            cursor.up(2);
            this.secondaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
            cursor.translate(o.reverse());
            this.secondaryStairBrush().setUpsideDown(true).setFacing(o).stroke(this.worldEditor, cursor);
            cursor.up();
            this.secondaryWallBrush().stroke(this.worldEditor, cursor);
            cursor.translate(o);
            this.secondaryWallBrush().stroke(this.worldEditor, cursor);
            cursor.translate(dir.reverse());
            this.secondaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
            cursor.translate(o.reverse());
            this.secondaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
         }
      }

      BlockType.JUKEBOX.getBrush().stroke(this.worldEditor, at);
      Coord cursor = at.copy();
      cursor.up(4);
      this.primaryLightBrush().stroke(this.worldEditor, cursor);
      Coord coord = this.generateChestLocation(at.copy().up());
      new TreasureChest(coord, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.MUSIC))
         .withFacing(this.getEntrance(entrances).reverse())
         .withTrap(false)
         .stroke(this.worldEditor, coord);
      this.generateDoorways(at, entrances);
      return this;
   }

   @Override
   protected Coord generateChestLocation(Coord origin) {
      Direction dir0 = Direction.randomCardinal(this.random());
      Direction dir1 = dir0.orthogonals()[this.random().nextBoolean() ? 0 : 1];
      return origin.copy().translate(dir0, 5).translate(dir1, 3 + (this.random().nextBoolean() ? 1 : 0));
   }

   private void pillar(Coord origin) {
      Coord start = origin.copy();
      Coord end = start.copy();
      end.up(2);
      this.secondaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = end.copy();
         cursor.translate(dir);
         this.secondaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor, true, false);
         cursor.up();
         this.secondaryWallBrush().stroke(this.worldEditor, cursor);
      }
   }
}
