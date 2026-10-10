package greymerk.roguelike.dungeon.towers;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.roguelike.worldgen.generatables.SpiralStaircase;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.BlockCheckers;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;

public class BumboTower extends Tower {
   public BumboTower(WorldEditor worldEditor, Theme theme) {
      super(worldEditor, theme);
   }

   @Override
   public void generate(Coord origin) {
      Coord ground = TowerType.getBaseCoord(this.editor, origin);
      Direction dir = Direction.randomCardinal(this.editor.getRandom());
      this.stem(ground, dir);
      Coord pos = ground.copy();
      pos.translate(dir.clockwise(), 5);
      pos.up(9);
      this.arm(pos, dir.clockwise());
      pos = ground.copy();
      pos.translate(dir.antiClockwise(), 5);
      pos.up(10);
      this.arm(pos, dir.antiClockwise());
      pos = ground.copy();
      pos.up(16);
      this.hat(pos);
      pos = ground.copy();
      pos.up(10);
      pos.translate(dir, 4);
      this.face(pos, dir);
      this.rooms(ground);
      Coord topStep = new Coord(ground).up(12);
      SpiralStaircase.newStaircase(this.editor)
         .withHeight(topStep.getY() - origin.getY())
         .withStairs(this.getPrimaryStair())
         .withPillar(this.getPrimaryWall())
         .generate(origin);
   }

   private void stem(Coord origin, Direction dir) {
      Coord start = origin.copy();
      Coord end = origin.copy();
      start.translate(dir, 4);
      start.translate(dir.antiClockwise(), 3);
      end.translate(dir.reverse(), 4);
      end.translate(dir.clockwise(), 3);
      start.down(10);
      end.up(16);
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
      start = origin.copy();
      end = origin.copy();
      start.translate(dir.antiClockwise(), 4);
      start.translate(dir, 3);
      end.translate(dir.antiClockwise(), 4);
      end.translate(dir.reverse(), 3);
      start.down(10);
      end.up(16);
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
      start.translate(dir.clockwise(), 8);
      end.translate(dir.clockwise(), 8);
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
   }

   private void arm(Coord origin, Direction dir) {
      Coord start = origin.copy();
      Coord end = origin.copy();
      start.translate(dir, 5);
      start.down(4);
      start.translate(dir.antiClockwise(), 2);
      end.translate(dir.clockwise(), 2);
      end.down();
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
      start = origin.copy();
      end = origin.copy();
      start.translate(dir, 5);
      end.translate(dir, 3);
      start.translate(dir.antiClockwise(), 2);
      end.translate(dir.clockwise(), 2);
      end.up(2);
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
      start = origin.copy();
      end = origin.copy();
      start.translate(dir.antiClockwise());
      end.translate(dir.clockwise());
      end.translate(dir, 2);
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
      start.translate(dir, 2);
      end.up(2);
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
      start.down(3);
      start.translate(dir, 3);
      end.translate(dir, 3);
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
      start = origin.copy();
      start.translate(dir, 6);
      end = start.copy();
      start.up(2);
      end.down(4);
      start.translate(dir.antiClockwise());
      end.translate(dir.clockwise());
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
      start = origin.copy();
      start.down(5);
      end = start.copy();
      start.translate(dir.antiClockwise());
      end.translate(dir.clockwise());
      end.translate(dir, 5);
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
      start = origin.copy();
      start.translate(dir, 3);
      start.up(3);
      end = start.copy();
      start.translate(dir.antiClockwise());
      end.translate(dir.clockwise());
      end.translate(dir, 2);
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
   }

   private void hat(Coord origin) {
      Coord offset = new Coord(origin.getX(), 0, origin.getZ());
      BlockBrush rim = new BlockCheckers(this.getSecondaryFloor(), this.getSecondaryWall(), offset);
      BlockBrush rim2 = new BlockCheckers(this.getSecondaryWall(), this.getSecondaryFloor(), offset);
      Coord start = origin.copy();
      Coord end = origin.copy();
      start.translate(new Coord(-2, 0, -2));
      end.translate(new Coord(2, 8, 2));
      this.getSecondaryWall().fill(this.editor, RectSolid.newRect(start, end));

      for (Direction dir : Direction.CARDINAL) {
         start = origin.copy();
         start.translate(dir, 3);
         end = start.copy();
         start.translate(dir.antiClockwise(), 2);
         end.translate(dir.clockwise(), 2);
         end.up(5);
         this.getSecondaryWall().fill(this.editor, RectSolid.newRect(start, end));
         start = origin.copy();
         start.translate(dir, 4);
         end = start.copy();
         start.translate(dir.antiClockwise(), 2);
         end.translate(dir.clockwise(), 2);
         end.up(2);
         this.getSecondaryWall().fill(this.editor, RectSolid.newRect(start, end));
         start = origin.copy();
         start.translate(dir, 3);
         start.translate(dir.antiClockwise(), 3);
         end = start.copy();
         end.up(2);
         this.getSecondaryWall().fill(this.editor, RectSolid.newRect(start, end));

         for (Direction o : dir.orthogonals()) {
            start = origin.copy();
            start.translate(dir, 3);
            end = start.copy();
            end.translate(dir, 3);
            end.translate(o, 5);
            this.getSecondaryWall().fill(this.editor, RectSolid.newRect(start, end));
         }

         start = origin.copy();
         start.translate(dir, 7);
         end = start.copy();
         start.translate(dir.antiClockwise(), 4);
         end.translate(dir.clockwise(), 4);
         rim2.fill(this.editor, RectSolid.newRect(start, end));
         start.translate(dir);
         end.translate(dir);
         start.up();
         end.up();
         rim.fill(this.editor, RectSolid.newRect(start, end));

         for (Direction o : dir.orthogonals()) {
            Coord pos = origin.copy();
            pos.translate(dir, 5);
            pos.translate(o, 5);
            rim.stroke(this.editor, pos);
            pos.translate(dir);
            rim.stroke(this.editor, pos);
            pos.up();
            pos.translate(dir);
            rim2.stroke(this.editor, pos);
            pos.translate(dir.reverse());
            pos.translate(o);
            rim.stroke(this.editor, pos);
         }
      }
   }

   private void face(Coord origin, Direction dir) {
      Coord start = origin.copy();
      Coord end = origin.copy();
      start.translate(dir.antiClockwise(), 2);
      end.translate(dir.clockwise(), 2);
      end.translate(dir);
      end.down(3);
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
      start.translate(dir.clockwise());
      start.down();
      end.translate(dir.antiClockwise());
      end.up();
      SingleBlockBrush.AIR.fill(this.editor, RectSolid.newRect(start, end));
      Coord pos = origin.copy();
      pos.translate(dir);
      pos.translate(dir.clockwise(), 2);
      pos.down(3);
      SingleBlockBrush.AIR.stroke(this.editor, pos);
      start = origin.copy();
      end = origin.copy();
      start.translate(dir.reverse());
      end.translate(dir.reverse());
      start.translate(dir.antiClockwise());
      end.translate(dir.clockwise());
      start.down();
      end.down(2);
      this.getSecondaryPillar().fill(this.editor, RectSolid.newRect(start, end));

      for (Direction o : dir.orthogonals()) {
         pos = origin.copy();
         pos.down();
         pos.translate(o);
         this.getPrimaryPillar().stroke(this.editor, pos);
      }

      pos = origin.copy();
      pos.translate(dir, 2);
      this.getPrimaryFloor().stroke(this.editor, pos);

      for (Direction o : dir.orthogonals()) {
         start = origin.copy();
         start.translate(dir, 2);
         end = start.copy();
         end.translate(o, 2);
         start.translate(o);
         start.up();
         this.getPrimaryFloor().fill(this.editor, RectSolid.newRect(start, end));
         start.translate(o, 4);
         start.down(2);
         this.getPrimaryFloor().fill(this.editor, RectSolid.newRect(start, end));
         pos = origin.copy();
         pos.translate(dir, 2);
         pos.translate(o, 4);
         SingleBlockBrush.AIR.stroke(this.editor, pos);
      }

      for (Direction o : dir.orthogonals()) {
         pos = origin.copy();
         pos.translate(o, 2);
         if (o == dir.clockwise()) {
            pos.up();
         }

         pos.up(2);
         SingleBlockBrush.AIR.stroke(this.editor, pos);
         pos.up();
         SingleBlockBrush.AIR.stroke(this.editor, pos);
      }
   }

   private void rooms(Coord origin) {
      Coord start = origin.copy();
      start.up(7);
      Coord end = start.copy();
      start.translate(new Coord(-3, 0, -3));
      end.translate(new Coord(3, 3, 3));
      SingleBlockBrush.AIR.fill(this.editor, RectSolid.newRect(start, end));
      start.up(5);
      end.up(5);
      SingleBlockBrush.AIR.fill(this.editor, RectSolid.newRect(start, end));

      for (Direction d : Direction.CARDINAL) {
         start = origin.copy();
         start.up(5);
         start.translate(d, 3);
         start.translate(d.antiClockwise(), 3);
         end = start.copy();
         end.up(10);
         this.getPrimaryFloor().fill(this.editor, RectSolid.newRect(start, end));
      }
   }
}
