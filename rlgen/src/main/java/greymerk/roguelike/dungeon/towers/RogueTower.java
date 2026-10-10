package greymerk.roguelike.dungeon.towers;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.decorative.TorchBlock;
import com.github.fnar.roguelike.worldgen.generatables.SpiralStaircase;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;

public class RogueTower extends Tower {
   public RogueTower(WorldEditor worldEditor, Theme theme) {
      super(worldEditor, theme);
   }

   @Override
   public void generate(Coord origin) {
      int x = origin.getX();
      int y = origin.getY();
      int z = origin.getZ();
      Coord floor = TowerType.getBaseCoord(this.editor, origin);
      int ground = floor.getY() - 1;
      int main = floor.getY() + 4;
      int roof = floor.getY() + 9;
      SingleBlockBrush.AIR.fill(this.editor, RectSolid.newRect(new Coord(x - 3, ground, z - 3), new Coord(x + 3, floor.getY() + 12, z + 3)));
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(new Coord(x - 2, y + 10, z - 2), new Coord(x + 2, floor.getY() - 1, z + 2)), false, true);
      this.getSecondaryWall().fill(this.editor, RectSolid.newRect(new Coord(x - 3, main, z - 3), new Coord(x + 3, main, z + 3)));
      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(new Coord(x - 3, roof, z - 3), new Coord(x + 3, roof, z + 3)));

      for (Direction dir : Direction.CARDINAL) {
         for (Direction orthogonals : dir.orthogonals()) {
            Coord start = floor.copy();
            start.down();
            start.translate(dir, 2);
            Coord end = start.copy();
            end.translate(dir, 3);
            end.translate(orthogonals, 1);
            this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
            start.translate(orthogonals, 2);
            end.translate(dir.reverse(), 2);
            end.translate(orthogonals, 2);
            this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
            Coord cursor = floor.copy();
            cursor.translate(dir, 5);
            cursor.translate(orthogonals, 1);
            start = cursor.copy();
            end = cursor.copy();
            end.translate(dir.reverse(), 1);
            end.up(2);
            this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
            start = end.copy();
            start.translate(dir, 1);
            start.translate(orthogonals.reverse(), 1);
            this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
            cursor.up(2);
            this.getPrimaryStair().setUpsideDown(false).setFacing(orthogonals).stroke(this.editor, cursor);
            start = floor.copy();
            start.translate(dir, 4);
            end = start.copy();
            end.up(9);
            end.translate(orthogonals, 2);
            this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
            start = floor.copy();
            start.translate(dir, 3);
            start.translate(orthogonals, 3);
            end = start.copy();
            end.up(9);
            this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
            start = floor.copy();
            start.translate(dir, 4);
            end = start.copy();
            end.translate(dir, 1);
            end.up(1);
            SingleBlockBrush.AIR.fill(this.editor, RectSolid.newRect(start, end));
            cursor = floor.copy();
            cursor.translate(dir, 3);
            cursor.translate(orthogonals, 2);
            cursor.up(3);
            this.getPrimaryStair().setUpsideDown(true).setFacing(orthogonals.reverse()).stroke(this.editor, cursor);
            cursor.up(5);
            this.getPrimaryStair().setUpsideDown(true).setFacing(orthogonals.reverse()).stroke(this.editor, cursor);
            start = floor.copy();
            start.translate(dir, 4);
            start.translate(orthogonals, 3);
            start.up(4);
            this.getPrimaryStair().setUpsideDown(true).setFacing(orthogonals).stroke(this.editor, start);
            start.up(1);
            end = start.copy();
            end.up(4);
            this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
            start = floor.copy();
            start.translate(dir, 5);
            start.up(4);
            this.getPrimaryStair().setUpsideDown(true).setFacing(dir).stroke(this.editor, start);
            cursor = start.copy();
            cursor.translate(orthogonals, 1);
            this.getPrimaryStair().setUpsideDown(true).setFacing(orthogonals).stroke(this.editor, cursor);
            start.up(3);
            this.getPrimaryStair().setUpsideDown(true).setFacing(dir).stroke(this.editor, start);
            cursor = start.copy();
            cursor.translate(orthogonals, 1);
            this.getPrimaryStair().setUpsideDown(true).setFacing(orthogonals).stroke(this.editor, cursor);
            start.up(1);
            end = start.copy();
            end.translate(orthogonals, 1);
            end.up(1);
            this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
            cursor = end.copy();
            cursor.translate(orthogonals, 1);
            cursor.down();
            this.getPrimaryStair().setUpsideDown(true).setFacing(orthogonals).stroke(this.editor, cursor);
            cursor.up(1);
            cursor.translate(orthogonals, 1);
            this.getPrimaryStair().setUpsideDown(true).setFacing(orthogonals).stroke(this.editor, cursor);
            cursor.translate(orthogonals.reverse(), 1);
            this.getPrimaryWall().stroke(this.editor, cursor);
            cursor.up(1);
            this.getPrimaryWall().stroke(this.editor, cursor);
            cursor.translate(orthogonals, 1);
            this.getPrimaryWall().stroke(this.editor, cursor);
            cursor.up(1);
            this.addCrenellation(this.editor, cursor, this.getPrimaryWall());
            cursor.down(2);
            cursor.translate(dir.reverse(), 1);
            cursor.translate(orthogonals, 1);
            this.getPrimaryWall().stroke(this.editor, cursor);
            cursor.down();
            this.getPrimaryWall().stroke(this.editor, cursor);
            cursor = floor.copy();
            cursor.translate(dir, 6);
            cursor.up(9);
            this.getPrimaryStair().setUpsideDown(true).setFacing(dir).stroke(this.editor, cursor);
            cursor.translate(orthogonals, 1);
            this.getPrimaryStair().setUpsideDown(true).setFacing(orthogonals).stroke(this.editor, cursor);
            cursor.translate(orthogonals.reverse(), 1);
            cursor.up(1);
            this.getPrimaryWall().stroke(this.editor, cursor);
            cursor.translate(orthogonals, 1);
            this.getPrimaryWall().stroke(this.editor, cursor);
            cursor.up(1);
            this.addCrenellation(this.editor, cursor, this.getPrimaryWall());
            cursor = floor.copy();
            cursor.translate(dir, 4);
            cursor.up(5);
            SingleBlockBrush.AIR.stroke(this.editor, cursor);
            cursor.up(1);
            SingleBlockBrush.AIR.stroke(this.editor, cursor);
            cursor.translate(orthogonals, 2);
            BlockType.IRON_BAR.getBrush().stroke(this.editor, cursor);
         }
      }

      for (Direction dir : Direction.CARDINAL) {
         for (Direction orthogonals : dir.orthogonals()) {
            Coord start = new Coord(x, ground, z);
            start.translate(dir, 4);
            Coord end = new Coord(x, 60, z);
            end.translate(dir, 4);
            start.translate(orthogonals.reverse(), 2);
            end.translate(orthogonals, 2);
            this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
            start = new Coord(x, ground, z);
            start.translate(dir, 3);
            start.translate(orthogonals, 3);
            end = new Coord(x, 60, z);
            end.translate(dir, 3);
            end.translate(orthogonals, 3);
            this.getPrimaryWall().fill(this.editor, RectSolid.newRect(start, end));
         }
      }

      SpiralStaircase.newStaircase(this.editor)
         .withHeight(main - y + 1)
         .withStairs(this.getPrimaryStair())
         .withPillar(this.getPrimaryPillar())
         .generate(origin);
   }

   private void addCrenellation(WorldEditor editor, Coord cursor, BlockBrush blocks) {
      blocks.stroke(editor, cursor);
      if (!editor.isAirBlock(cursor)) {
         cursor.up(1);
         TorchBlock.torch().setFacing(Direction.UP).stroke(editor, cursor);
      }
   }
}
