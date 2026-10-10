package greymerk.roguelike.dungeon.towers;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.decorative.VineBlock;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import com.github.fnar.minecraft.material.Wood;
import com.github.fnar.roguelike.worldgen.generatables.SpiralStaircase;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JungleTower extends Tower {
   public JungleTower(WorldEditor worldEditor, Theme theme) {
      super(worldEditor, theme);
   }

   @Override
   public void generate(Coord dungeon) {
      Coord origin = TowerType.getBaseCoord(this.editor, dungeon);
      origin.up();
      StairsBlock stair = this.getPrimaryStair();
      BlockBrush grass = BlockType.GRASS.getBrush();

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = origin.copy();
         cursor.up(3);
         cursor.translate(dir, 7);
         this.pillar(this.editor, this.theme, cursor);

         for (Direction o : dir.orthogonals()) {
            Coord c = cursor.copy();
            c.translate(o, 3);
            this.pillar(this.editor, this.theme, c);
            c.translate(dir);
            c.up();
            this.getPrimaryWall().stroke(this.editor, c);
            c.up();
            this.getPrimaryWall().stroke(this.editor, c);
            c.up();
            stair.setUpsideDown(false).setFacing(dir).stroke(this.editor, c);
            c.translate(dir.reverse());
            this.getPrimaryWall().stroke(this.editor, c);
         }

         cursor = origin.copy();
         cursor.up(4);
         cursor.translate(dir, 8);
         this.getPrimaryWall().stroke(this.editor, cursor);
         cursor.up();
         this.getPrimaryWall().stroke(this.editor, cursor);
         cursor.up();
         stair.setUpsideDown(false).setFacing(dir).stroke(this.editor, cursor);
         cursor.translate(dir.reverse());
         this.getPrimaryWall().stroke(this.editor, cursor);
         Coord start = origin.copy();
         start.translate(dir, 2);
         start.translate(dir.antiClockwise(), 2);
         Coord end = start.copy();
         end.up(3);
         this.getPrimaryPillar().fill(this.editor, RectSolid.newRect(start, end));
         cursor = end.copy();

         for (Direction d : new Direction[]{dir.reverse(), dir.clockwise()}) {
            Coord c = cursor.copy();
            c.translate(d);
            stair.setUpsideDown(true).setFacing(d).stroke(this.editor, c);
         }

         cursor = origin.copy();
         cursor.up(6);
         cursor.translate(dir, 6);
         cursor.translate(dir.antiClockwise(), 6);
         this.editor.fillDown(cursor.copy(), this.getPrimaryPillar());

         for (Direction d : new Direction[]{dir, dir.antiClockwise()}) {
            start = cursor.copy();
            start.translate(d);
            stair.setUpsideDown(false).setFacing(d).stroke(this.editor, start);
            start.down();
            end = start.copy();
            end.down(2);
            this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
            end.down();
            stair.setUpsideDown(true).setFacing(d).stroke(this.editor, end);
         }
      }

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = origin.copy();
         cursor.up(4);
         cursor.translate(dir, 7);
         Coord start = cursor.copy();
         Coord end = cursor.copy();
         start.translate(dir.antiClockwise(), 5);
         end.translate(dir.clockwise(), 5);
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         start.up();
         end.up();
         grass.fill(this.editor, RectSolid.newRect(start, end));
         start.translate(dir.reverse());
         end.translate(dir.reverse());
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         start.translate(dir.reverse());
         end.translate(dir.reverse());
         stair.setUpsideDown(true).setFacing(dir.reverse()).fill(this.editor, RectSolid.newRect(start, end));
         cursor = origin.copy();
         cursor.up(4);
         cursor.translate(dir, 6);
         this.pillar(this.editor, this.theme, cursor);

         for (Direction o : dir.orthogonals()) {
            Coord c = cursor.copy();
            c.translate(o, 3);
            this.pillar(this.editor, this.theme, c);
         }

         start = origin.copy();
         start.up(5);
         start.translate(dir, 2);
         end = start.copy();
         end.translate(dir, 3);
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         end.translate(dir.antiClockwise(), 3);
         start = end.copy();
         start.translate(dir.reverse(), 10);
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         start = origin.copy();
         start.up(6);
         start.translate(dir, 3);
         start.translate(dir.antiClockwise(), 2);
         end = start.copy();
         end.translate(dir.clockwise(), 8);
         end.translate(dir, 3);
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         start = origin.copy();
         start.up(4);
         start.translate(dir, 2);
         end = start.copy();
         start.translate(dir.antiClockwise(), 2);
         end.translate(dir.clockwise());
         end.up(2);
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         start = origin.copy();
         start.up(4);
         start.translate(dir, 3);
         end = start.copy();
         start.translate(dir.antiClockwise(), 3);
         end.translate(dir.clockwise(), 2);
         stair.setUpsideDown(true).setFacing(dir).fill(this.editor, RectSolid.newRect(start, end));
      }

      for (Direction dir : Direction.CARDINAL) {
         for (Direction o : dir.orthogonals()) {
            Coord start = origin.copy();
            start.up(6);
            start.translate(dir, 5);
            start.translate(o);
            Coord end = start.copy();
            end.translate(o);
            end.translate(dir);
            grass.fill(this.editor, RectSolid.newRect(start, end));
            start.translate(o, 3);
            end.translate(o, 3);
            grass.fill(this.editor, RectSolid.newRect(start, end));
         }
      }

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = origin.copy();
         cursor.up(9);
         cursor.translate(dir, 5);

         for (Direction o : dir.orthogonals()) {
            Coord c = cursor.copy();
            c.translate(o, 2);
            this.pillar(this.editor, this.theme, c);
            c.translate(dir);
            c.up();
            this.getPrimaryWall().stroke(this.editor, c);
            c.up();
            stair.setUpsideDown(false).setFacing(dir).stroke(this.editor, c);
            c.translate(dir.reverse());
            this.getPrimaryWall().stroke(this.editor, c);
            c.up();
            stair.setUpsideDown(false).setFacing(dir).stroke(this.editor, c);
         }

         cursor.translate(dir.antiClockwise(), 5);
         this.pillar(this.editor, this.theme, cursor);

         for (Direction d : new Direction[]{dir.antiClockwise(), dir}) {
            Coord c = cursor.copy();
            c.translate(d);
            c.up();
            this.getPrimaryWall().stroke(this.editor, c);
            c.up();
            stair.setUpsideDown(false).setFacing(d).stroke(this.editor, c);
         }

         cursor = origin.copy();
         cursor.up(10);
         cursor.translate(dir, 2);
         cursor.translate(dir.antiClockwise(), 2);
         Coord start = cursor.copy();
         Coord end = start.copy();
         end.down(3);
         this.getPrimaryPillar().fill(this.editor, RectSolid.newRect(start, end));

         for (Direction d : new Direction[]{dir.clockwise(), dir.reverse()}) {
            Coord c = cursor.copy();
            c.translate(d);
            stair.setUpsideDown(true).setFacing(d).stroke(this.editor, c);
         }

         cursor.down();

         for (Direction d : new Direction[]{dir.antiClockwise(), dir}) {
            Coord c = cursor.copy();
            c.translate(d);
            stair.setUpsideDown(true).setFacing(d).stroke(this.editor, c);
            c.up();
            this.getPrimaryWall().stroke(this.editor, c);
            c.translate(d);
            this.getPrimaryWall().stroke(this.editor, c);
         }
      }

      for (Direction dir : Direction.CARDINAL) {
         Coord start = origin.copy();
         start.up(10);
         start.translate(dir, 5);
         Coord end = start.copy();
         start.translate(dir.antiClockwise(), 5);
         end.translate(dir.clockwise(), 4);
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         start = origin.copy();
         start.up(11);
         start.translate(dir, 2);
         end = start.copy();
         start.translate(dir.antiClockwise());
         end.translate(dir.clockwise(), 4);
         end.translate(dir, 2);
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         Coord cursor = origin.copy();
         cursor.up(11);
         cursor.translate(dir, 5);
         start = cursor.copy();
         end = start.copy();
         start.translate(dir.antiClockwise(), 4);
         end.translate(dir.clockwise(), 4);
         grass.fill(this.editor, RectSolid.newRect(start, end));
         cursor = origin.copy();
         cursor.up(12);
         cursor.translate(dir, 3);
         start = cursor.copy();
         end = start.copy();
         end.translate(dir);
         start.translate(dir.antiClockwise());
         end.translate(dir.clockwise(), 4);
         grass.fill(this.editor, RectSolid.newRect(start, end));
      }

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = origin.copy();
         cursor.up(12);
         cursor.translate(dir, 2);
         Coord start = cursor.copy();
         Coord end = cursor.copy();
         start.translate(dir.antiClockwise(), 4);
         end.translate(dir.clockwise(), 4);
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         cursor = origin.copy();
         cursor.up(11);
         cursor.translate(dir, 5);
         cursor.translate(dir.antiClockwise(), 5);
         this.getPrimaryWall().stroke(this.editor, cursor);
      }

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = origin.copy();
         cursor.translate(dir, 2);
         cursor.translate(dir.antiClockwise(), 2);
         cursor.up(15);
         this.pillar(this.editor, this.theme, cursor);

         for (Direction d : new Direction[]{dir, dir.antiClockwise()}) {
            Coord c = cursor.copy();
            c.translate(d);
            c.up();
            this.getPrimaryWall().stroke(this.editor, c);
            c.up();
            stair.setUpsideDown(false).setFacing(d).stroke(this.editor, c);
         }

         Coord start = origin.copy();
         start.up(16);
         start.translate(dir, 2);
         Coord end = start.copy();
         start.translate(dir.antiClockwise());
         end.translate(dir.clockwise(), 2);
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         cursor = origin.copy();
         cursor.up(17);
         cursor.translate(dir, 2);
         start = cursor.copy();
         end = cursor.copy();
         start.translate(dir.antiClockwise());
         end.translate(dir.clockwise());
         grass.fill(this.editor, RectSolid.newRect(start, end));
         cursor.translate(dir.antiClockwise(), 2);
         this.getPrimaryWall().stroke(this.editor, cursor);
         start = origin.copy();
         start.up(17);
         end = start.copy();
         start.translate(dir);
         start.translate(dir.antiClockwise());
         end.translate(dir.reverse());
         end.translate(dir.clockwise());
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         start.up();
         end.up();
         grass.fill(this.editor, RectSolid.newRect(start, end));
      }

      Coord start = origin.copy();
      start.north(2);
      start.east(2);
      Coord end = new Coord(origin.getX(), dungeon.getY() + 10, origin.getZ());
      end.south(2);
      end.west(2);
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end), false, true);
      Coord cursor = origin.copy();
      cursor.up(12);
      start = new Coord(cursor.getX(), dungeon.getY(), cursor.getZ());
      end = cursor.copy();

      for (Coord c : RectSolid.newRect(start, end)) {
         SpiralStaircase.newStaircase(this.editor).withStairs(stair).withPillar(this.getPrimaryPillar()).generate(origin);
      }

      this.decorate(this.editor, this.editor.getRandom(), origin);
   }

   private void decorate(WorldEditor editor, Random rand, Coord origin) {
      List<Coord> spots = new ArrayList<>();

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = origin.copy();
         cursor.up(6);
         cursor.translate(dir, 7);

         for (Direction o : dir.orthogonals()) {
            Coord c = cursor.copy();
            c.translate(o);
            spots.add(c.copy());
            c.translate(o);
            spots.add(c.copy());
            c.translate(o, 2);
            spots.add(c.copy());
            c.translate(o);
            spots.add(c.copy());
         }

         cursor = origin.copy();
         cursor.up(12);
         cursor.translate(dir, 5);
         spots.add(cursor.copy());

         for (Direction o : dir.orthogonals()) {
            Coord c = cursor.copy();
            c.translate(o);
            spots.add(c.copy());
            c.translate(o, 2);
            spots.add(c.copy());
            c.translate(o);
            spots.add(c.copy());
         }

         cursor = origin.copy();
         cursor.up(13);
         cursor.translate(dir, 4);
         spots.add(cursor.copy());

         for (Direction o : dir.orthogonals()) {
            Coord c = cursor.copy();
            c.translate(o);
            spots.add(c.copy());
            c.translate(o, 2);
            spots.add(c.copy());
            c.translate(o);
            spots.add(c.copy());
         }

         cursor = origin.copy();
         cursor.up(18);
         cursor.translate(dir, 2);
         spots.add(cursor.copy());

         for (Direction o : dir.orthogonals()) {
            Coord c = cursor.copy();
            c.translate(o);
            spots.add(c.copy());
         }

         cursor = origin.copy();
         cursor.up(19);
         Coord start = cursor.copy();
         Coord end = cursor.copy();
         start.translate(dir);
         end.translate(dir.reverse());
         start.translate(dir.antiClockwise());
         end.translate(dir.clockwise());
         spots.addAll(RectSolid.newRect(start, end).get());
      }

      for (Coord c : spots) {
         if (editor.getRandom().nextBoolean()) {
            this.tree(editor, editor.getRandom(), c);
         }
      }

      Coord start = origin.copy().north(8).east(8);
      Coord end = origin.copy().up(20).south(8).west(8);
      VineBlock.vine().fill(editor, RectSolid.newRect(start, end));
   }

   private void tree(WorldEditor editor, Random rand, Coord origin) {
      Wood jungle = Wood.JUNGLE;
      BlockBrush leaves = jungle.getLeaves();
      BlockBrush log = jungle.getLog();
      Coord cursor = origin.copy();
      log.stroke(editor, cursor);

      for (Direction dir : Direction.CARDINAL) {
         Coord c = cursor.copy();
         c.translate(dir);
         this.leafSpill(editor, editor.getRandom(), c, editor.getRandom().nextInt(6));
      }

      if (editor.getRandom().nextBoolean()) {
         cursor.up();
         log.stroke(editor, cursor);

         for (Direction dir : Direction.CARDINAL) {
            Coord c = cursor.copy();
            c.translate(dir);
            leaves.stroke(editor, c, true, false);
         }
      }

      if (editor.getRandom().nextInt(3) == 0) {
         cursor.up();
         log.stroke(editor, cursor);

         for (Direction dir : Direction.CARDINAL) {
            Coord c = cursor.copy();
            c.translate(dir);
            leaves.stroke(editor, c, true, false);
         }
      }

      cursor.up();
      leaves.stroke(editor, cursor);
   }

   public void leafSpill(WorldEditor editor, Random rand, Coord origin, int count) {
      if (count >= 0) {
         BlockBrush leaves = Wood.JUNGLE.getLeaves();
         leaves.stroke(editor, origin);
         Coord cursor = origin.copy();
         cursor.down();
         if (!editor.isOpaqueBlock(cursor)) {
            leaves.stroke(editor, origin);
            if (editor.getRandom().nextBoolean()) {
               this.leafSpill(editor, editor.getRandom(), cursor, count - 1);
            }
         } else {
            for (Direction dir : Direction.CARDINAL) {
               cursor = origin.copy();
               cursor.translate(dir);
               if (!editor.isOpaqueBlock(cursor)) {
                  leaves.stroke(editor, origin);
                  cursor.down();
                  if (!editor.isOpaqueBlock(cursor)) {
                     this.leafSpill(editor, editor.getRandom(), cursor, count - 1);
                  }
               }
            }
         }
      }
   }

   private void pillar(WorldEditor editor, Theme theme, Coord origin) {
      BlockBrush pillar = theme.getPrimary().getPillar();
      StairsBlock stair = theme.getPrimary().getStair();
      editor.fillDown(origin.copy(), pillar);

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = origin.copy();
         cursor.translate(dir);
         stair.setUpsideDown(true).setFacing(dir).stroke(editor, cursor);
      }
   }
}
