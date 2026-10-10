package greymerk.roguelike.dungeon.towers;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.normal.ColoredBlock;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import com.github.fnar.roguelike.worldgen.generatables.SpiralStaircase;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.util.DyeColor;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;

public class WitchTower extends Tower {
   public WitchTower(WorldEditor worldEditor, Theme theme) {
      super(worldEditor, theme);
   }

   @Override
   public void generate(Coord origin) {
      BlockBrush glass = ColoredBlock.stainedGlass().setColor(DyeColor.BLACK);
      Coord main = TowerType.getBaseCoord(this.editor, origin);
      Coord start = main.copy();
      start.north(3);
      start.west(3);
      start.down();
      Coord end = main.copy();
      end.south(3);
      end.east(3);
      end.up(3);
      RectHollow.newRect(start, end).fill(this.editor, this.getPrimaryWall());

      for (Direction dir : Direction.CARDINAL) {
         Direction[] orthogonal = dir.orthogonals();
         start = main.copy();
         start.translate(dir, 3);
         start.translate(orthogonal[0], 3);
         start.down();
         end = start.copy();
         end.up(3);
         this.getPrimaryPillar().fill(this.editor, RectSolid.newRect(start, end));

         for (Direction o : orthogonal) {
            start = main.copy();
            start.translate(dir, 4);
            start.translate(o, 2);
            start.down();
            end = start.copy();
            end.up(3);
            this.getPrimaryPillar().fill(this.editor, RectSolid.newRect(start, end));
            Coord cursor = end.copy();
            cursor.translate(dir);
            this.getPrimaryStair().setUpsideDown(true).setFacing(dir).stroke(this.editor, cursor);

            for (Direction d : orthogonal) {
               cursor = end.copy();
               cursor.translate(d);
               this.getPrimaryStair().setUpsideDown(true).setFacing(d).stroke(this.editor, cursor);
            }
         }
      }

      Coord secondFloor = main.copy();
      secondFloor.up(4);
      start = secondFloor.copy();
      start.north(4);
      start.west(4);
      start.down();
      end = secondFloor.copy();
      end.south(4);
      end.east(4);
      end.up(6);
      RectHollow.newRect(start, end).fill(this.editor, this.getPrimaryWall());

      for (Direction dir : Direction.CARDINAL) {
         Direction[] orthogonal = dir.orthogonals();
         start = secondFloor.copy();
         start.translate(dir, 4);
         start.up();
         end = start.copy();
         start.translate(orthogonal[0]);
         end.translate(orthogonal[1]);
         end.up();
         glass.fill(this.editor, RectSolid.newRect(start, end));
         start = secondFloor.copy();
         start.translate(dir, 4);
         start.down();
         start.translate(orthogonal[0], 4);
         end = start.copy();
         end.up(4);
         SingleBlockBrush.AIR.fill(this.editor, RectSolid.newRect(start, end));

         for (Direction o : orthogonal) {
            start = secondFloor.copy();
            start.down();
            start.translate(dir, 4);
            start.translate(o, 3);
            end = start.copy();
            end.up(3);
            this.getPrimaryPillar().fill(this.editor, RectSolid.newRect(start, end));
            start = secondFloor.copy();
            start.down();
            start.translate(dir, 5);
            start.translate(o, 2);
            end = start.copy();
            end.up(4);
            this.getPrimaryPillar().fill(this.editor, RectSolid.newRect(start, end));
            Coord cursor = end.copy();
            cursor.translate(dir);
            this.getPrimaryStair().setUpsideDown(true).setFacing(dir).stroke(this.editor, cursor);

            for (Direction d : orthogonal) {
               cursor = end.copy();
               cursor.translate(d);
               this.getPrimaryStair().setUpsideDown(true).setFacing(d).stroke(this.editor, cursor);
            }
         }
      }

      Coord thirdFloor = secondFloor.copy();
      thirdFloor.up(7);
      start = thirdFloor.copy();
      start.north(3);
      start.west(3);
      start.down();
      end = thirdFloor.copy();
      end.south(3);
      end.east(3);
      end.up(4);
      RectHollow.newRect(start, end).fill(this.editor, this.getPrimaryWall());

      for (Direction dir : Direction.CARDINAL) {
         Direction[] orthogonal = dir.orthogonals();
         Coord cursor = thirdFloor.copy();
         cursor.translate(dir, 3);
         cursor.up();
         this.window(this.editor, dir, cursor);
         start = thirdFloor.copy();
         start.translate(dir, 2);
         end = start.copy();
         end.translate(dir, 4);
         end.down();
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         start = thirdFloor.copy();
         start.translate(dir, 5);
         start.down(2);
         end = start.copy();
         start.translate(orthogonal[0]);
         end.translate(orthogonal[1]);
         end.down();
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         start = thirdFloor.copy();
         start.translate(dir, 3);
         start.translate(orthogonal[0], 3);
         start.down(2);
         end = start.copy();
         end.up(5);
         this.getPrimaryPillar().fill(this.editor, RectSolid.newRect(start, end));
         cursor = end.copy();
         cursor.translate(dir);
         this.getPrimaryStair().setUpsideDown(true).setFacing(dir).stroke(this.editor, cursor);
         cursor = end.copy();
         cursor.translate(orthogonal[0]);
         this.getPrimaryStair().setUpsideDown(true).setFacing(orthogonal[0]).stroke(this.editor, cursor);

         for (Direction o : orthogonal) {
            start = thirdFloor.copy();
            start.translate(dir, 4);
            start.down();
            start.translate(o, 3);
            end = start.copy();
            end.translate(o);
            end.down();
            SingleBlockBrush.AIR.fill(this.editor, RectSolid.newRect(start, end));

            for (int i = 0; i < 4; i++) {
               start = thirdFloor.copy();
               start.translate(dir, 4);
               start.translate(o, i + 1);
               start.down(i);
               end = start.copy();
               end.translate(dir, 2);
               this.getPrimaryStair().setUpsideDown(false).setFacing(o);
               this.getPrimaryStair().fill(this.editor, RectSolid.newRect(start, end));
               if (i < 3) {
                  start = thirdFloor.copy();
                  start.translate(dir, 4);
                  start.translate(o, i + 1);
                  start.down(i + 1);
                  end = start.copy();
                  end.translate(dir, 2);
                  this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
               }

               start = thirdFloor.copy();
               start.translate(dir, 4);
               start.translate(o, 2);
               start.down(3);
               end = start.copy();
               end.translate(dir, 2);
               this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
               cursor = thirdFloor.copy();
               cursor.translate(dir, 6);
               cursor.translate(o);
               cursor.down(2);
               this.getPrimaryStair().setUpsideDown(true).setFacing(o.reverse()).stroke(this.editor, cursor);
            }
         }
      }

      for (int i = thirdFloor.getY() - 1; i >= 50; i--) {
         Coord at = new Coord(origin.getX(), i, origin.getZ());
         SpiralStaircase.newStaircase(this.editor).withHeight(1).withStairs(this.getPrimaryStair()).withPillar(this.getPrimaryPillar()).generate(at);
      }

      Coord attic = thirdFloor.copy();
      attic.up(5);
      start = attic.copy();
      start.north(2);
      start.west(2);
      start.down();
      end = attic.copy();
      end.south(2);
      end.east(2);
      end.up(3);
      RectHollow.newRect(start, end).fill(this.editor, this.getPrimaryWall());
      start = attic.copy();
      start.up(4);
      end = start.copy();
      start.north();
      start.west();
      end.south();
      end.east();
      RectHollow.newRect(start, end).fill(this.editor, this.getPrimaryWall());
      start = attic.copy();
      start.up(5);
      end = start.copy();
      end.up(2);
      RectHollow.newRect(start, end).fill(this.editor, this.getPrimaryWall());

      for (Direction dir : Direction.CARDINAL) {
         Direction[] orthogonal = dir.orthogonals();
         Coord cursor = attic.copy();
         cursor.translate(dir, 2);
         cursor.up();
         this.window(this.editor, dir, cursor);
         this.getPrimaryStair().setUpsideDown(false).setFacing(dir);
         start = attic.copy();
         start.translate(dir, 3);
         end = start.copy();
         start.translate(orthogonal[0], 3);
         end.translate(orthogonal[1], 3);
         this.getPrimaryStair().fill(this.editor, RectSolid.newRect(start, end));
         start = attic.copy();
         start.translate(dir, 4);
         start.down();
         end = start.copy();
         start.translate(orthogonal[0], 4);
         end.translate(orthogonal[1], 4);
         this.getPrimaryStair().fill(this.editor, RectSolid.newRect(start, end));
         start = attic.copy();
         start.translate(dir, 3);
         start.up(3);
         end = start.copy();
         start.translate(orthogonal[0], 3);
         end.translate(orthogonal[1], 3);
         this.getPrimaryStair().fill(this.editor, RectSolid.newRect(start, end));
         start = attic.copy();
         start.translate(dir, 2);
         start.up(4);
         end = start.copy();
         start.translate(orthogonal[0], 2);
         end.translate(orthogonal[1], 2);
         this.getPrimaryStair().fill(this.editor, RectSolid.newRect(start, end));
      }

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = main.copy();
         cursor.translate(dir, 4);
         if (this.editor.isAirBlock(cursor)) {
            cursor = main.copy();
            cursor.translate(dir, 3);
            this.getPrimaryDoor().setFacing(dir).stroke(this.editor, cursor);
            cursor.translate(dir);
            start = cursor.copy();
            end = start.copy();
            end.up();
            end.translate(dir, 3);
            SingleBlockBrush.AIR.fill(this.editor, RectSolid.newRect(start, end));
            cursor = main.copy();
            cursor.translate(dir, 4);
            cursor.down();
            this.step(this.editor, dir, cursor);
            break;
         }
      }
   }

   private void window(WorldEditor editor, Direction dir, Coord origin) {
      StairsBlock stair = this.getPrimaryStair();
      Coord cursor = origin.copy();
      SingleBlockBrush.AIR.stroke(editor, cursor);
      cursor.up();
      SingleBlockBrush.AIR.stroke(editor, cursor);

      for (Direction o : dir.orthogonals()) {
         cursor = origin.copy();
         cursor.translate(o);
         stair.setUpsideDown(false).setFacing(o.reverse()).stroke(editor, cursor);
         cursor.up();
         stair.setUpsideDown(true).setFacing(o.reverse()).stroke(editor, cursor);
      }
   }

   private void step(WorldEditor editor, Direction dir, Coord origin) {
      if (!editor.isOpaqueCubeBlock(origin)) {
         StairsBlock stair = this.getPrimaryStair();
         BlockBrush blocks = this.getPrimaryWall();
         Direction[] orthogonal = dir.orthogonals();
         Coord start = origin.copy();
         Coord end = origin.copy();
         start.translate(orthogonal[0]);
         end.translate(orthogonal[1]);
         end = new Coord(end.getX(), 60, end.getZ());
         RectSolid.newRect(start, end).fill(editor, blocks);
         start = origin.copy();
         end = origin.copy();
         start.translate(orthogonal[0]);
         end.translate(orthogonal[1]);
         stair.setUpsideDown(false).setFacing(dir);
         RectSolid.newRect(start, end).fill(editor, stair);
         origin.down();
         origin.translate(dir);
         this.step(editor, dir, origin);
      }
   }
}
