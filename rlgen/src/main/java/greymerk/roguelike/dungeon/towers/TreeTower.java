package greymerk.roguelike.dungeon.towers;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.material.Wood;
import com.github.fnar.roguelike.worldgen.generatables.SpiralStaircase;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.BlockWeightedRandom;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.Line;
import greymerk.roguelike.worldgen.shapes.MultiShape;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import greymerk.roguelike.worldgen.shapes.Sphere;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeTower extends Tower {
   public static final Wood WOOD_TYPE = Wood.OAK;

   public TreeTower(WorldEditor worldEditor, Theme theme) {
      super(worldEditor, theme);
   }

   @Override
   public void generate(Coord origin) {
      Coord ground = TowerType.getBaseCoord(this.editor, origin);
      Coord upstairs = ground.copy();
      upstairs.up(7);
      BlockBrush log = WOOD_TYPE.getLog();
      Coord start = ground.copy();
      start.down(10);
      TreeTower.Branch tree = new TreeTower.Branch(this.editor.getRandom(), start);
      tree.genWood(this.editor);
      tree.genLeaves(this.editor, this.editor.getRandom());
      start = ground.copy();
      start.translate(new Coord(-3, -3, -3));
      Coord end = ground.copy();
      end.translate(new Coord(3, 3, 3));
      log.fill(this.editor, RectSolid.newRect(start, end));
      this.carveRoom(this.editor, ground);
      this.carveRoom(this.editor, upstairs);
      Direction dir = Direction.randomCardinal(this.editor.getRandom());
      start = ground.copy();
      end = ground.copy();
      end.up();
      end.translate(dir, 8);
      SingleBlockBrush.AIR.fill(this.editor, RectSolid.newRect(start, end));
      start = ground.copy();
      end = ground.copy();
      end.translate(new Coord(8, 8, 8));
      new Sphere(start, end).fill(this.editor, log, false, true);
      start = upstairs.copy();
      start.down();

      for (Coord at : RectSolid.newRect(start, origin)) {
         SpiralStaircase.newStaircase(this.editor).withHeight(1).withStairs(this.getPrimaryStair()).withPillar(this.getPrimaryPillar()).generate(at);
      }
   }

   private void carveRoom(WorldEditor editor, Coord origin) {
      BlockBrush log = WOOD_TYPE.getLog();
      int size = 4;
      Coord start = origin.copy();
      start.translate(new Coord(-(size - 1), 0, -(size - 1)));
      Coord end = origin.copy();
      end.translate(new Coord(size - 1, 2, size - 1));
      SingleBlockBrush.AIR.fill(editor, RectSolid.newRect(start, end));
      start = origin.copy();
      end = start.copy();
      start.up(2);
      end.translate(new Coord(size - 1, size - 1, size - 1));
      new Sphere(start, end).fill(editor, SingleBlockBrush.AIR);

      for (Direction dir : Direction.CARDINAL) {
         start = origin.copy();
         start.translate(dir, size - 1);
         start.translate(dir.antiClockwise(), size - 1);
         end = start.copy();
         end.up(size + 1);
         RectSolid.newRect(start, end).fill(editor, log);
      }

      start = origin.copy();
      end = origin.copy();
      start.translate(new Coord(-(size - 1), -1, -(size - 1)));
      end.translate(new Coord(size - 1, -1, size - 1));
      RectSolid.newRect(start, end).fill(editor, log);
   }

   private static class Branch {
      Coord start;
      Coord end;
      List<TreeTower.Branch> branches;
      double thickness;

      public Branch(Random rand, Coord start) {
         this.start = start.copy();
         this.branches = new ArrayList<>();
         int counter = 7;
         double length = 12.0;
         this.thickness = 7.0;
         int mainBranches = 5;
         int density = 3;
         double noise = 0.15;
         double pitch = 0.0;
         double yaw = Math.PI / 2;
         this.end = this.getEnd(start, 4.0, pitch, yaw);

         for (int i = 0; i < mainBranches; i++) {
            this.branches
               .add(
                  new TreeTower.Branch(
                     rand,
                     this.end.copy(),
                     counter,
                     length,
                     this.thickness,
                     4,
                     noise,
                     pitch + (Math.PI * 2) / (double)density * (double)i,
                     yaw + (rand.nextDouble() - 0.5) * noise
                  )
               );
         }
      }

      public Branch(Random rand, Coord start, int counter, double length, double thickness, int density, double noise, double pitch, double yaw) {
         this.start = start.copy();
         this.thickness = thickness < 1.0 ? 1.0 : thickness;
         this.branches = new ArrayList<>();
         this.end = this.getEnd(start, length, pitch, yaw);
         if (counter > 0) {
            for (int i = 0; i < rand.nextInt(density) + 1; i++) {
               this.branches
                  .add(
                     new TreeTower.Branch(
                        rand,
                        this.end.copy(),
                        counter - (rand.nextInt(2) + 1),
                        length < 1.0 ? 1.0 : length * 0.88,
                        thickness * 0.72,
                        density,
                        noise + thickness / 5.0 * 0.55,
                        pitch + (rand.nextDouble() - 0.5) * noise,
                        yaw + (rand.nextDouble() - 0.5) * noise
                     )
                  );
            }
         }
      }

      public void genWood(WorldEditor editor) {
         BlockBrush log = TreeTower.WOOD_TYPE.getLog().setFacing(this.start.dirTo(this.end));

         for (TreeTower.Branch b : this.branches) {
            b.genWood(editor);
         }

         if (this.thickness == 1.0) {
            new Line(this.start, this.end).fill(editor, log, true, false);
         } else if (this.thickness > 1.0) {
            MultiShape shape = new MultiShape();

            for (Coord pos : new Line(this.start, this.end)) {
               Coord s = pos.copy();
               Coord e = s.copy();
               e.translate(new Coord((int)this.thickness, (int)this.thickness, (int)this.thickness));
               shape.addShape(new Sphere(s, e));
            }

            shape.fill(editor, log);
         }
      }

      public void genLeaves(WorldEditor editor, Random rand) {
         MultiShape leafShape = new MultiShape();
         this.getLeafShape(leafShape, editor.getRandom());
         BlockWeightedRandom leaves = new BlockWeightedRandom();
         leaves.addBlock(TreeTower.WOOD_TYPE.getLeaves(), 5);
         leaves.addBlock(SingleBlockBrush.AIR, 1);
         leafShape.fill(editor, leaves, true, false);
      }

      public void getLeafShape(MultiShape leaves, Random rand) {
         if (this.branches.isEmpty()) {
            Coord s = this.end.copy();
            Coord e = s.copy();
            int size = 2;
            int noise = 2;
            e.translate(new Coord(2 + rand.nextInt(2), 2 + rand.nextInt(2), 2 + rand.nextInt(2)));
            leaves.addShape(new Sphere(s, e));
         } else {
            for (TreeTower.Branch b : this.branches) {
               b.getLeafShape(leaves, rand);
            }
         }
      }

      private Coord getEnd(Coord start, double length, double pitch, double yaw) {
         Coord offset = new Coord(
            (int)(Math.cos(pitch) * Math.cos(yaw) * length), (int)(Math.sin(yaw) * length), (int)(Math.sin(pitch) * Math.cos(yaw) * length)
         );
         Coord end = start.copy();
         end.translate(offset);
         return end;
      }
   }
}
