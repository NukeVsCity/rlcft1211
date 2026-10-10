package greymerk.roguelike.dungeon.towers;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.roguelike.worldgen.generatables.SpiralStaircase;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;

public class VillagerHouseTower extends Tower {
   public VillagerHouseTower(WorldEditor worldEditor, Theme theme) {
      super(worldEditor, theme);
   }

   @Override
   public void generate(Coord origin) {
      Direction facing = Direction.randomCardinal(this.editor.getRandom());
      Coord base = TowerType.getBaseCoord(this.editor, origin);
      this.clearTowerArea(this.editor, base);
      this.createFoundation(origin, base, facing);
      this.createWalls(base, facing);
      this.createRoof(base);
      this.createSpiralStaircase(origin, base);
   }

   private void clearTowerArea(WorldEditor editor, Coord origin) {
      RectSolid rect = origin.newRect(3);

      for (int i = 0; i < 6; i++) {
         rect.fill(editor, BlockType.AIR.getBrush());
         rect.translate(Direction.UP, 1);
      }
   }

   private void createFoundation(Coord origin, Coord base, Direction facing) {
      Coord cursor = new Coord(base);

      while (origin.getY() + 10 <= cursor.getY()) {
         cursor.newRect(3).fill(this.editor, this.theme.getPrimary().getFloor());
         cursor.down();
      }

      this.createOutsideStaircase(base, facing);
   }

   private void createOutsideStaircase(Coord base, Direction facing) {
      BlockBrush stairBlock = this.theme.getPrimary().getStair().setUpsideDown(false).setFacing(facing);
      Coord stairCoord = base.copy().translate(facing, 3);

      do {
         stairBlock.stroke(this.editor, stairCoord);
         this.fillBeneathStep(stairCoord);
         stairCoord.translate(facing).down();
      } while (!this.editor.isValidGroundBlock(stairCoord));
   }

   private void fillBeneathStep(Coord entrance) {
      Coord belowTop = entrance.copy().down();
      Coord belowBottom = belowTop.copy();

      while (!this.editor.isValidGroundBlock(belowBottom)) {
         belowBottom.down();
      }

      this.getPrimaryWall().fill(this.editor, RectSolid.newRect(belowTop, belowBottom));
   }

   private void createWalls(Coord origin, Direction facing) {
      for (Direction cardinal : Direction.CARDINAL) {
         Coord wallCenter = origin.copy().translate(cardinal, 2).up();
         Coord left = wallCenter.copy().translate(cardinal.antiClockwise());
         Coord right = wallCenter.copy().translate(cardinal.clockwise());
         this.getPrimaryWall().fill(this.editor, RectSolid.newRect(left, right.copy().up(3)));
         right.translate(cardinal.clockwise());
         this.getPrimaryPillar().fill(this.editor, RectSolid.newRect(right, right.copy().up(3)));
         if (cardinal == facing) {
            this.createDoorway(origin, facing);
         } else {
            Coord windowCoord = origin.copy().translate(cardinal, 2).up(2);
            BlockType.GLASS_PANE.getBrush().stroke(this.editor, windowCoord);
         }
      }
   }

   private void createDoorway(Coord origin, Direction facing) {
      Coord doorway = origin.copy().translate(facing, 2).up(1);
      this.getPrimaryDoor().setFacing(facing).stroke(this.editor, doorway);
      Direction inward = facing.reverse();
      Coord torchCoord = doorway.copy().translate(inward).up(2);
      BlockType.TORCH.getBrush().setFacing(inward).stroke(this.editor, torchCoord);
   }

   public void createRoof(Coord origin) {
      Coord center = origin.copy().up(4);
      this.getSecondaryPillar().fill(this.editor, center.newRect(3));
      this.getSecondaryFloor().fill(this.editor, center.newRect(2));
      center.up();
      BlockType.OAK_FENCE.getBrush().fill(this.editor, center.newRect(3));
      BlockType.AIR.getBrush().fill(this.editor, center.newRect(2));
   }

   public void createSpiralStaircase(Coord origin, Coord base) {
      int height = base.getY() - origin.getY() + 1;
      SpiralStaircase.newStaircase(this.editor).withHeight(height).withStairs(this.getPrimaryStair()).withPillar(this.getPrimaryPillar()).generate(origin);
   }
}
