package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.decorative.PumpkinBlock;
import com.github.fnar.minecraft.block.normal.ColoredBlock;
import com.github.fnar.minecraft.block.normal.SlabBlock;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.util.DyeColor;
import greymerk.roguelike.util.mst.MinimumSpanningTree;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class TreethoRoom extends BaseRoom {
   public TreethoRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 11;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      Direction dir = this.getEntrance(entrances);
      Coord start = at.copy();
      Coord end = at.copy();
      start.translate(new Coord(-11, -1, -11));
      end.translate(new Coord(11, 8, 11));
      RectHollow.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      BlockBrush birchSlab = SlabBlock.birch().setTop(true).setFullBlock(false).setSeamless(false);
      BlockBrush pumpkin = PumpkinBlock.jackOLantern();
      start = at.copy();
      end = at.copy();
      start.translate(new Coord(-9, 8, -9));
      end.translate(new Coord(9, 8, 9));
      birchSlab.fill(this.worldEditor, RectSolid.newRect(start, end));
      start.up();
      end.up();
      pumpkin.fill(this.worldEditor, RectSolid.newRect(start, end));
      Coord cursor = at.copy();
      cursor.translate(new Coord(0, 8, 0));
      this.ceiling(cursor);
      cursor = at.copy();
      this.treeFarm(cursor, dir);

      for (Direction o : dir.orthogonals()) {
         cursor = at.copy();
         cursor.translate(o, 5);
         this.treeFarm(cursor, dir);
      }

      return this;
   }

   private void treeFarm(Coord origin, Direction dir) {
      Coord start = origin.copy();
      Coord end = origin.copy();
      start.translate(dir.antiClockwise());
      end.translate(dir.clockwise());
      start.translate(dir.reverse(), 7);
      end.translate(dir, 7);
      SlabBlock.sandstone().fill(this.worldEditor, RectSolid.newRect(start, end));
      Coord cursor = origin.copy();
      cursor.translate(dir.reverse(), 6);

      for (int i = 0; i <= 12; i++) {
         if (i % 2 == 0) {
            Coord p = cursor.copy();
            if (i % 4 == 0) {
               BlockType.BIRCH_SAPLING.getBrush().stroke(this.worldEditor, p);
               p.down();
               BlockType.DIRT.getBrush().stroke(this.worldEditor, p);
            } else {
               ColoredBlock.stainedGlass().setColor(DyeColor.YELLOW).stroke(this.worldEditor, p);
               p.down();
               PumpkinBlock.jackOLantern().stroke(this.worldEditor, p);
            }
         }

         cursor.translate(dir);
      }
   }

   private void ceiling(Coord origin) {
      BlockBrush fill = BlockType.SPRUCE_PLANK.getBrush();
      MinimumSpanningTree tree = new MinimumSpanningTree(this.worldEditor.getRandom(), 7, 3);
      tree.generate(this.worldEditor, fill, origin);

      for (Direction dir : Direction.CARDINAL) {
         Coord start = origin.copy();
         start.translate(dir, 9);
         Coord end = start.copy();
         start.translate(dir.antiClockwise(), 9);
         end.translate(dir.clockwise(), 9);
         fill.fill(this.worldEditor, RectSolid.newRect(start, end));
         Coord cursor = origin.copy();
         cursor.down();
         cursor.translate(dir, 10);
         cursor.translate(dir.antiClockwise(), 10);

         for (int i = 0; i < 5; i++) {
            this.pillar(cursor);
            cursor.translate(dir.clockwise(), 4);
         }
      }
   }

   private void pillar(Coord origin) {
      Coord cursor = origin.copy();
      this.worldEditor.fillDown(cursor, this.primaryPillarBrush());

      for (Direction dir : Direction.CARDINAL) {
         cursor = origin.copy();
         cursor.translate(dir);
         if (this.worldEditor.isAirBlock(cursor)) {
            this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor);
         }
      }
   }
}
