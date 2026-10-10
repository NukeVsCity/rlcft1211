package greymerk.roguelike.dungeon.towers;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import com.github.fnar.roguelike.worldgen.generatables.SpiralStaircase;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;

public class EthoTower extends Tower {
   public EthoTower(WorldEditor worldEditor, Theme theme) {
      super(worldEditor, theme);
   }

   @Override
   public void generate(Coord dungeon) {
      Coord floor = TowerType.getBaseCoord(this.editor, dungeon);
      Coord start = floor.copy();
      Coord end = start.copy();
      start.north(3);
      start.west(3);
      end.south(3);
      end.east(3);
      end.up(4);
      start.north();
      start.west();
      start.down();
      end.south();
      end.east();
      end.up();
      RectHollow.newRect(start, end).fill(this.editor, this.getPrimaryWall());

      for (Direction dir : Direction.CARDINAL) {
         Direction[] orthogonals = dir.orthogonals();
         start = floor.copy();
         start.translate(dir, 3);
         start.translate(orthogonals[0], 3);
         end = start.copy();
         end.up(6);
         this.getSecondaryPillar().fill(this.editor, RectSolid.newRect(start, end));

         for (Direction o : orthogonals) {
            start = floor.copy();
            start.translate(dir, 5);
            start.translate(o, 4);
            end = start.copy();
            end.up(4);
            start.down(10);
            this.getSecondaryPillar().fill(this.editor, RectSolid.newRect(start, end));
            end.up();
            this.getSecondaryStair().setUpsideDown(false).setFacing(dir).stroke(this.editor, end);
            end.translate(dir.reverse());
            end.translate(o.reverse());
            this.getSecondaryStair().setUpsideDown(false).setFacing(o.reverse()).stroke(this.editor, end);
            end.translate(o.reverse());
            start = end.copy();
            start.translate(o.reverse(), 2);
            this.getSecondaryStair().setUpsideDown(false).setFacing(dir).fill(this.editor, RectSolid.newRect(start, end));
            end.translate(dir.reverse());
            end.up();
            start.translate(dir.reverse());
            start.up();
            this.getSecondaryStair().setUpsideDown(false).setFacing(dir).fill(this.editor, RectSolid.newRect(start, end));
            this.getSecondaryStair().setUpsideDown(false).setFacing(o.reverse()).stroke(this.editor, end);
            start = floor.copy();
            start.translate(dir, 3);
            start.up(4);
            end = start.copy();
            end.translate(o, 2);
            this.getSecondaryStair().setUpsideDown(true).setFacing(dir.reverse()).fill(this.editor, RectSolid.newRect(start, end));
            start.translate(dir.reverse());
            start.up();
            end = start.copy();
            end.translate(o, 2);
            this.getSecondaryStair().setUpsideDown(true).setFacing(dir.reverse()).fill(this.editor, RectSolid.newRect(start, end));
            start.up();
            end.up();
            this.getSecondaryPillar().fill(this.editor, RectSolid.newRect(start, end));
            Coord cursor = end.copy();
            start = end.copy();
            start.up(3);
            this.getSecondaryPillar().fill(this.editor, RectSolid.newRect(start, end));
            cursor.translate(o.reverse());
            cursor.up();
            this.getSecondaryStair().setUpsideDown(false).setFacing(o.reverse()).stroke(this.editor, cursor);
            cursor.up(2);
            this.getSecondaryStair().setUpsideDown(true).setFacing(o.reverse()).stroke(this.editor, cursor);
            start.up();
            end = start.copy();
            end.translate(o.reverse(), 2);
            this.getSecondaryStair().setUpsideDown(false).setFacing(dir).fill(this.editor, RectSolid.newRect(start, end));
            cursor = end.copy();
            cursor.translate(dir.reverse());
            this.getSecondaryStair().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.editor, cursor);
            cursor.translate(o);
            this.getSecondaryStair().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.editor, cursor);
            cursor.up();
            this.getSecondaryStair().setUpsideDown(false).setFacing(dir).stroke(this.editor, cursor);
            cursor.translate(o.reverse());
            this.getSecondaryStair().setUpsideDown(false).setFacing(dir).stroke(this.editor, cursor);
         }
      }

      Direction front = Direction.NORTH;

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = floor.copy();
         cursor.translate(dir, 6);
         if (this.editor.isAirBlock(cursor)) {
            front = dir;
            break;
         }
      }

      for (Direction dirx : Direction.CARDINAL) {
         if (dirx == front) {
            for (Direction o : dirx.orthogonals()) {
               Coord cursor = floor.copy();
               cursor.translate(dirx, 5);
               cursor.translate(o, 2);
               this.getPrimaryWall().stroke(this.editor, cursor);
               cursor.translate(o);
               this.getSecondaryStair().setUpsideDown(false).setFacing(o).stroke(this.editor, cursor);
               cursor.translate(dirx);
               this.getSecondaryStair().setUpsideDown(false).setFacing(o).stroke(this.editor, cursor);
               cursor.translate(o.reverse());
               this.getSecondaryStair().setUpsideDown(false).setFacing(dirx).stroke(this.editor, cursor);
               cursor.translate(dirx.reverse());
               cursor.up();
               this.getSecondaryStair().setUpsideDown(false).setFacing(o.reverse()).stroke(this.editor, cursor);
               cursor.up();
               this.getSecondaryStair().setUpsideDown(false).setFacing(dirx).stroke(this.editor, cursor);
               cursor.translate(o);
               this.getSecondaryStair().setUpsideDown(false).setFacing(o).stroke(this.editor, cursor);
               cursor.translate(o.reverse());
               cursor.up();
               this.getSecondaryStair().setUpsideDown(false).setFacing(o.reverse()).stroke(this.editor, cursor);
               cursor.translate(o.reverse());
               this.getSecondaryStair().setUpsideDown(true).setFacing(o.reverse()).stroke(this.editor, cursor);
               cursor.translate(o.reverse());
               cursor.up();
               this.getSecondaryStair().setUpsideDown(false).setFacing(dirx).stroke(this.editor, cursor);
               cursor.translate(o);
               this.getSecondaryStair().setUpsideDown(false).setFacing(dirx).stroke(this.editor, cursor);
               cursor.translate(o);
               this.getSecondaryStair().setUpsideDown(false).setFacing(o).stroke(this.editor, cursor);
            }

            Direction[] orthogonals = dirx.orthogonals();
            Coord cursor = floor.copy();
            cursor.translate(dirx, 4);
            start = cursor.copy();
            end = start.copy();
            start.translate(orthogonals[0]);
            end.up(2);
            end.translate(orthogonals[1]);
            SingleBlockBrush.AIR.fill(this.editor, RectSolid.newRect(start, end));
            cursor = floor.copy();
            cursor.translate(dirx, 6);
            cursor.down();
            this.step(dirx, cursor);
         } else {
            for (Direction o : dirx.orthogonals()) {
               start = floor.copy();
               start.up(4);
               start.translate(dirx, 5);
               end = start.copy();
               start.translate(o, 2);
               this.getSecondaryStair().setUpsideDown(false).setFacing(dirx).fill(this.editor, RectSolid.newRect(start, end));
               start.translate(o);
               this.getSecondaryStair().setUpsideDown(false).setFacing(o.reverse()).stroke(this.editor, start);
               start.down();
               this.getSecondaryStair().setUpsideDown(true).setFacing(o.reverse()).stroke(this.editor, start);
            }
         }
      }

      SpiralStaircase.newStaircase(this.editor)
         .withHeight(floor.getY() - dungeon.getY())
         .withStairs(this.getPrimaryStair())
         .withPillar(this.getPrimaryWall())
         .generate(dungeon);
   }

   private void step(Direction dir, Coord origin) {
      if (!this.editor.isOpaqueCubeBlock(origin)) {
         Coord start = origin.copy();
         Coord end = origin.copy();
         start.translate(dir.left());
         end.translate(dir.right());
         end = new Coord(end.getX(), 60, end.getZ());
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         start = origin.copy();
         end = origin.copy();
         start.translate(dir.left());
         end.translate(dir.right());
         StairsBlock stair = this.getPrimaryStair();
         stair.setUpsideDown(false).setFacing(dir);
         stair.fill(this.editor, RectSolid.newRect(start, end));
         origin.down();
         origin.translate(dir);
         this.step(dir, origin);
      }
   }
}
