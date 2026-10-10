package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.normal.Quartz;
import com.github.fnar.minecraft.block.spawner.MobType;
import greymerk.roguelike.config.RogueConfig;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.BlockCheckers;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.IShape;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class DungeonsEnder extends BaseRoom {
   public DungeonsEnder(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 6;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      BlockBrush black = BlockType.OBSIDIAN.getBrush();
      BlockBrush white = Quartz.SMOOTH.getBrush();
      Coord start = at.copy();
      Coord end = at.copy();
      start.translate(new Coord(-3, 0, -3));
      end.translate(new Coord(3, 2, 3));
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));

      for (Direction dir : Direction.CARDINAL) {
         Direction[] orthogonals = dir.orthogonals();
         start = at.copy();
         start.translate(dir, 4);
         end = start.copy();
         start.translate(orthogonals[0], 4);
         start.down();
         end.translate(orthogonals[1], 4);
         end.up(5);
         RectSolid.newRect(start, end).fill(this.worldEditor, black, false, true);
      }

      start = at.copy();
      end = at.copy();
      start.translate(new Coord(-3, 2, -3));
      end.translate(new Coord(3, 10, 3));
      int top = end.getY() - start.getY() + 1;

      for (Coord cell : RectSolid.newRect(start, end)) {
         boolean dissolve = this.random().nextInt(cell.getY() - start.getY() + 1) < 2;
         SingleBlockBrush.AIR.stroke(this.worldEditor, cell, false, dissolve);
         black.stroke(this.worldEditor, cell, false, this.random().nextInt(top - (cell.getY() - start.getY())) == 0 && !dissolve);
      }

      start = at.copy();
      end = at.copy();
      start.translate(new Coord(-4, -1, -4));
      end.translate(new Coord(4, -1, 4));
      BlockCheckers checkers = new BlockCheckers(black, white);
      checkers.fill(this.worldEditor, RectSolid.newRect(start, end));
      start = at.copy();
      end = at.copy();
      start.translate(new Coord(-4, 0, -4));
      end.translate(new Coord(4, 0, 4));
      if (RogueConfig.GENEROUS.getBoolean()) {
         this.addEnderChest(RectSolid.newRect(start, end));
      }

      this.generateSpawner(at, new MobType[]{MobType.ENDERMAN});
      return this;
   }

   private void addEnderChest(IShape area) {
      for (Coord pos : area) {
         if (this.worldEditor.isAirBlock(pos)) {
            Coord cursor = pos.copy();

            for (Direction dir : Direction.CARDINAL) {
               cursor.translate(dir);
               if (this.worldEditor.isOpaqueCubeBlock(cursor)) {
                  Direction dir1 = dir.reverse();
                  BlockType.ENDER_CHEST
                     .getBrush()
                     .setFacing(Direction.CARDINAL.contains(dir1) ? dir1.reverse() : Direction.SOUTH)
                     .stroke(this.worldEditor, pos);
                  return;
               }

               cursor.translate(dir.reverse());
            }
         }
      }
   }
}
