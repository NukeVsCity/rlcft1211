package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class PyramidCornerRoom extends BaseRoom {
   public PyramidCornerRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 3;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      int x = at.getX();
      int y = at.getY();
      int z = at.getZ();
      RectHollow.newRect(new Coord(x - 3, y - 1, z - 3), new Coord(x + 3, y + 3, z + 3)).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      RectSolid.newRect(new Coord(x - 2, y + 3, z - 2), new Coord(x + 2, y + 5, z + 2)).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(new Coord(x - 1, y + 3, z - 1), new Coord(x + 1, y + 3, z + 1)));
      RectSolid.newRect(new Coord(x - 3, y - 1, z - 3), new Coord(x + 3, y - 1, z + 3))
         .fill(this.worldEditor, this.theme().getPrimary().getFloor(), false, true);
      Coord cursor = at.copy();
      cursor.up(4);
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
      cursor.up(1);
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);

      for (Direction dir : Direction.CARDINAL) {
         cursor = at.copy();
         cursor.up(4);
         cursor.translate(dir);
         SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
         cursor = at.copy();
         cursor.translate(dir, 2);
         cursor.translate(dir.orthogonals()[0], 2);
         Coord start = cursor.copy();
         cursor.up(2);
         Coord end = cursor.copy();
         this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      }

      return this;
   }
}
