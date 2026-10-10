package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.normal.ColoredBlock;
import com.github.fnar.minecraft.block.normal.Quartz;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import com.github.fnar.minecraft.material.Wood;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.util.DyeColor;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class AvidyaRoom extends BaseRoom {
   public AvidyaRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 9;
   }

   private static void pillarTop(WorldEditor editor, Coord cursor) {
      StairsBlock stair = StairsBlock.quartz();

      for (Direction dir : Direction.CARDINAL) {
         stair.setUpsideDown(true).setFacing(dir);
         cursor.translate(dir, 1);
         stair.stroke(editor, cursor, true, false);
         cursor.translate(dir.reverse(), 1);
      }
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      int x = at.getX();
      int y = at.getY();
      int z = at.getZ();
      BlockBrush redClay = ColoredBlock.stainedHardenedClay().setColor(DyeColor.RED);
      BlockBrush whiteClay = ColoredBlock.stainedHardenedClay().setColor(DyeColor.WHITE);
      BlockBrush pillarQuartz = Quartz.PILLAR.getBrush().setFacing(Direction.UP);
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 8, y, z - 8), new Coord(x + 8, y + 5, z + 8)));
      redClay.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 6, y + 6, z - 6), new Coord(x + 6, y + 6, z + 6)));
      this.primaryLightBrush().fill(this.worldEditor, RectSolid.newRect(new Coord(x - 3, y + 6, z - 3), new Coord(x + 3, y + 6, z + 3)));
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 7, y - 1, z - 7), new Coord(x + 7, y - 1, z + 7)));
      BlockBrush ying = ColoredBlock.stainedHardenedClay().setColor(DyeColor.BLACK);
      BlockBrush yang = ColoredBlock.stainedHardenedClay().setColor(DyeColor.WHITE);
      ying.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 8, y - 2, z - 8), new Coord(x + 8, y - 2, z + 8)));
      BlockBrush quartz = Quartz.SMOOTH.getBrush();
      Coord start = new Coord(x, y, z);
      start.down(2);
      start.west(5);
      Coord end = start.copy();
      start.north(2);
      end.south(2);
      yang.fill(this.worldEditor, RectSolid.newRect(start, end));
      start.east();
      end.east();
      start.north(2);
      end.south(2);
      yang.fill(this.worldEditor, RectSolid.newRect(start, end));
      start.east();
      end.east();
      end.north(3);
      yang.fill(this.worldEditor, RectSolid.newRect(start, end));
      start.east();
      end.east();
      start.north();
      end.north();
      yang.fill(this.worldEditor, RectSolid.newRect(start, end));
      start.east();
      end.east(3);
      end.north();
      yang.fill(this.worldEditor, RectSolid.newRect(start, end));
      start.east(3);
      end.east();
      start.south();
      end.north();
      yang.fill(this.worldEditor, RectSolid.newRect(start, end));
      start.west(3);
      end.west(2);
      end.north();
      yang.fill(this.worldEditor, RectSolid.newRect(start, end));
      start.east();
      end.east();
      start.south(7);
      end.south(7);
      yang.fill(this.worldEditor, RectSolid.newRect(start, end));

      for (Direction dir : Direction.CARDINAL) {
         for (Direction orthogonals : dir.orthogonals()) {
            start = new Coord(x, y, z);
            start.translate(dir, 8);
            start.up(4);
            end = start.copy();
            end.translate(orthogonals, 8);
            whiteClay.fill(this.worldEditor, RectSolid.newRect(start, end));
            start.down(5);
            end.down(5);
            BlockType.STONE_BRICK.getBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            start = new Coord(x, y, z);
            start.translate(dir, 7);
            start.up(5);
            end = start.copy();
            end.translate(orthogonals, 7);
            whiteClay.fill(this.worldEditor, RectSolid.newRect(start, end));
            start = new Coord(x, y, z);
            start.translate(dir, 4);
            start.up(5);
            end = start.copy();
            end.translate(orthogonals, 2);
            quartz.fill(this.worldEditor, RectSolid.newRect(start, end));
            Coord cursor = end.copy();
            cursor.translate(dir, 1);
            quartz.stroke(this.worldEditor, cursor);
            cursor = end.copy();
            cursor.translate(dir.reverse(), 1);
            cursor.translate(orthogonals, 1);
            quartz.stroke(this.worldEditor, cursor);
            pillarTop(this.worldEditor, cursor);
            start = new Coord(x, y, z);
            start.down();
            start.translate(dir, 8);
            start.translate(orthogonals, 2);
            end = start.copy();
            end.up(4);
            pillarQuartz.fill(this.worldEditor, RectSolid.newRect(start, end));
            start.translate(orthogonals, 4);
            end.translate(orthogonals, 4);
            pillarQuartz.fill(this.worldEditor, RectSolid.newRect(start, end));
            cursor = new Coord(x, y, z);
            cursor.translate(dir, 8);
            cursor.translate(orthogonals, 2);
            cursor.up(3);
            Coord cursor2 = cursor.copy();
            pillarTop(this.worldEditor, cursor);
            cursor2.translate(orthogonals, 4);
            pillarTop(this.worldEditor, cursor2);
            cursor2.translate(dir.reverse(), 1);
            cursor2.up(1);
            quartz.stroke(this.worldEditor, cursor2);
            cursor2.translate(dir.reverse(), 1);
            cursor2.up(1);
            whiteClay.stroke(this.worldEditor, cursor2);
            cursor.translate(dir.reverse(), 1);
            cursor.up(1);
            pillarTop(this.worldEditor, cursor);
            cursor.translate(dir.reverse(), 1);
            cursor.up(1);
            pillarTop(this.worldEditor, cursor);
            start = new Coord(x, y, z);
            start.translate(dir, 9);
            end = start.copy();
            end.translate(orthogonals, 9);
            end.up(3);
            RectSolid.newRect(start, end).fill(this.worldEditor, whiteClay, false, true);
            cursor = new Coord(x, y, z);
            cursor.translate(dir, 7);
            cursor.down();
            StairsBlock stair = StairsBlock.stoneBrick();
            stair.setUpsideDown(false).setFacing(dir.reverse());
            stair.stroke(this.worldEditor, cursor);
            cursor.translate(orthogonals, 1);
            stair.stroke(this.worldEditor, cursor);
            cursor.translate(orthogonals, 1);
            stair.stroke(this.worldEditor, cursor);
            stair.setUpsideDown(false).setFacing(orthogonals.reverse());
            cursor.translate(orthogonals, 1);
            stair.stroke(this.worldEditor, cursor);
            cursor.translate(dir.reverse(), 1);
            stair.stroke(this.worldEditor, cursor);
            stair.setUpsideDown(false).setFacing(dir.reverse());
            cursor.translate(orthogonals, 1);
            stair.stroke(this.worldEditor, cursor);
            stair.setUpsideDown(false).setFacing(orthogonals.reverse());
            cursor.translate(orthogonals, 1);
            stair.stroke(this.worldEditor, cursor);
            cursor.translate(dir.reverse(), 1);
            stair.stroke(this.worldEditor, cursor);
            cursor = new Coord(x, y, z);
            cursor.down();
            cursor.translate(dir, 8);
            cursor.translate(orthogonals, 3);
            BlockType.GRASS.getBrush().stroke(this.worldEditor, cursor);
            BlockBrush leaves = Wood.OAK.getLeaves();
            leaves.stroke(this.worldEditor, cursor);
            cursor.translate(orthogonals, 1);
            BlockType.GRASS.getBrush().stroke(this.worldEditor, cursor);
            leaves.stroke(this.worldEditor, cursor);
            cursor.translate(orthogonals, 1);
            BlockType.GRASS.getBrush().stroke(this.worldEditor, cursor);
            leaves.stroke(this.worldEditor, cursor);
            cursor.translate(dir.reverse(), 1);
            BlockType.COBBLESTONE.getBrush().stroke(this.worldEditor, cursor);
            cursor.translate(orthogonals.reverse(), 1);
            this.primaryLightBrush().stroke(this.worldEditor, cursor);
            cursor.translate(orthogonals, 2);
            SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
            cursor.down();
            this.primaryLightBrush().stroke(this.worldEditor, cursor);
            cursor.up(1);
            cursor.translate(dir.reverse(), 1);
            BlockType.COBBLESTONE.getBrush().stroke(this.worldEditor, cursor);
            cursor.translate(dir, 1);
            cursor.translate(orthogonals, 1);
            BlockType.COBBLESTONE.getBrush().stroke(this.worldEditor, cursor);
            cursor.translate(dir, 1);
            BlockType.COBBLESTONE.getBrush().stroke(this.worldEditor, cursor);
            cursor.translate(orthogonals, 1);
            BlockType.COBBLESTONE.getBrush().stroke(this.worldEditor, cursor);
            cursor.up(1);
            BlockType.COBBLESTONE.getBrush().stroke(this.worldEditor, cursor);
            cursor.up(3);
            BlockType.WATER_FLOWING.getBrush().stroke(this.worldEditor, cursor);
         }
      }

      return this;
   }
}
