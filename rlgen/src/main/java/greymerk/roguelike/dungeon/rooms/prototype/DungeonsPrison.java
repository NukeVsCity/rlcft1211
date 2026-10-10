package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.spawner.MobType;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.ArrayList;
import java.util.List;

public class DungeonsPrison extends BaseRoom {
   public DungeonsPrison(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 11;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      this.largeRoom(at);

      for (Direction dir : entrances) {
         Coord cursor = at.copy();
         cursor.translate(dir, 6);
         this.sideRoom(cursor, dir);
      }

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = at.copy();
         cursor.translate(dir, 3);
         cursor.translate(dir.antiClockwise(), 3);
         this.pillar(cursor, 4);
      }

      for (Direction dir : Direction.CARDINAL) {
         List<Direction> doors = new ArrayList<>();
         if (entrances.contains(dir)) {
            doors.add(dir.clockwise());
         }

         if (entrances.contains(dir.antiClockwise())) {
            doors.add(dir.reverse());
         }

         if (!doors.isEmpty()) {
            Coord cursor = at.copy();
            cursor.translate(dir, 6);
            cursor.translate(dir.antiClockwise(), 6);
            this.cell(cursor, doors, this.random().nextBoolean());
         }
      }

      return this;
   }

   public void largeRoom(Coord origin) {
      Coord start = origin.copy();
      start.up(6);
      Coord end = start.copy();
      start.north(3);
      start.east(3);
      end.south(3);
      end.west(3);
      RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      BlockBrush floor = this.theme().getPrimary().getFloor();
      start = origin.copy();
      start.down();
      end = start.copy();
      start.north(3);
      start.east(3);
      end.south(3);
      end.west(3);
      RectSolid.newRect(start, end).fill(this.worldEditor, floor, false, true);
      start = origin.copy();
      end = origin.copy();
      start.north(3);
      start.west(3);
      end.south(3);
      end.east(3);
      end.up(4);
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));
      start = origin.copy();
      end = origin.copy();
      start.north(4);
      start.west(4);
      end.south(4);
      end.east(4);
      end.up(5);
      this.theme().getPrimary().getWall().fill(this.worldEditor, new RectHollow(start, end), false, true);

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = origin.copy();
         cursor.translate(dir, 3);
         cursor.translate(dir.antiClockwise(), 3);
         this.pillar(cursor, 4);
      }

      start = origin.copy();
      start.up(5);
      end = start.copy();
      start.north();
      start.east();
      end.south();
      end.west();
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = origin.copy();
         cursor.up(5);
         cursor.translate(dir, 2);
         SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);

         for (Direction o : dir.orthogonals()) {
            Coord c = cursor.copy();
            c.translate(o);
            this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, c);
         }

         cursor = origin.copy();
         cursor.up(6);
         SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
         cursor.translate(dir, 1);
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
      }
   }

   private void sideRoom(Coord origin, Direction dir) {
      int height = 3;
      Coord start = origin.copy();
      start.up(6);
      Coord end = start.copy();
      start.north(3);
      start.east(3);
      end.south(3);
      end.west(3);
      RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      start = origin.copy();
      start.down();
      end = start.copy();
      start.north(3);
      start.east(3);
      end.south(3);
      end.west(3);
      RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryFloorBrush(), false, true);
      start = origin.copy();
      end = origin.copy();
      start.north(3);
      start.west(3);
      end.south(3);
      end.east(3);
      end.up(height);
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));
      start = origin.copy();
      end = origin.copy();
      start.north(4);
      start.west(4);
      end.south(4);
      end.east(4);
      end.up(height + 1);
      this.theme().getPrimary().getWall().fill(this.worldEditor, new RectHollow(start, end), false, true);
      start = origin.copy();
      start.up(4);
      end = start.copy();
      start.translate(dir);
      end.translate(dir.reverse(), 3);
      start.translate(dir.antiClockwise());
      end.translate(dir.clockwise());
      end.up();
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));

      for (Direction d : dir.orthogonals()) {
         Coord cursor = origin.copy();
         cursor.translate(d, 3);
         cursor.translate(dir, 3);
         this.pillar(cursor, height);
      }

      start = origin.copy();
      start.up(4);
      end = start.copy();
      start.translate(dir.antiClockwise());
      end.translate(dir.clockwise());
      start.translate(dir.reverse(), 3);
      end.translate(dir, 2);

      for (Direction d : dir.orthogonals()) {
         Coord cursor = origin.copy();
         cursor.up(4);
         start = cursor.copy();
         start.translate(d, 2);
         end = start.copy();
         start.translate(d.antiClockwise(), 3);
         end.translate(d.clockwise(), 3);
         this.primaryStairBrush().setUpsideDown(true).setFacing(d.reverse()).fill(this.worldEditor, RectSolid.newRect(start, end));
         cursor.up(1);
         start = cursor.copy();
         start.translate(d);
         end = start.copy();
         start.translate(d.antiClockwise(), 3);
         end.translate(d.clockwise(), 3);
         this.primaryStairBrush().setUpsideDown(true).setFacing(d.reverse()).fill(this.worldEditor, RectSolid.newRect(start, end));
      }

      Coord cursor = origin.copy();
      cursor.up(4);
      start = cursor.copy();
      start.translate(dir, 2);
      end = start.copy();
      start.translate(dir.antiClockwise(), 2);
      end.translate(dir.clockwise(), 2);
      this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).fill(this.worldEditor, RectSolid.newRect(start, end));
      cursor.up(1);
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
      start = cursor.copy();
      start.translate(dir, 1);
      end = start.copy();
      start.translate(dir.antiClockwise(), 1);
      end.translate(dir.clockwise(), 1);
      this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).fill(this.worldEditor, RectSolid.newRect(start, end));
   }

   private void pillar(Coord origin, int height) {
      Coord cursor = origin.copy();
      cursor.up(height - 1);
      this.worldEditor.fillDown(cursor.copy(), this.primaryPillarBrush());
      cursor.up();
      this.primaryPillarBrush().stroke(this.worldEditor, cursor);

      for (Direction dir : Direction.CARDINAL) {
         cursor.translate(dir);
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor, true, false);
      }
   }

   private void cell(Coord origin, List<Direction> entrances, boolean occupied) {
      Coord cursor = origin.copy();
      cursor.down();
      if (!this.worldEditor.isAirBlock(cursor)) {
         BlockBrush bar = BlockType.IRON_BAR.getBrush();
         Coord start = origin.copy();
         Coord end = origin.copy();
         start.down();
         start.north(2);
         start.west(2);
         end.south(2);
         end.east(2);
         end.up(4);
         RectHollow.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), false, true);
         start = origin.copy();
         start.down();
         end = start.copy();
         start.north();
         start.east();
         end.south();
         end.west();
         RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryFloorBrush(), false, true);

         for (Direction dir : entrances) {
            cursor = origin.copy();
            cursor.translate(dir, 2);
            start = cursor.copy();
            end = cursor.copy();
            start.translate(dir.antiClockwise());
            end.translate(dir.clockwise());
            end.up(2);
            bar.fill(this.worldEditor, RectSolid.newRect(start, end));
            if (this.random().nextBoolean()) {
               SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
               cursor.up();
               SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
            }
         }

         if (occupied) {
            this.generateSpawner(origin, new MobType[]{MobType.SKELETON});
         }
      }
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof DungeonsPrison)) {
         return false;
      } else {
         DungeonsPrison other = (DungeonsPrison)o;
         return other.canEqual(this);
      }
   }

   @Override
   protected boolean canEqual(Object other) {
      return other instanceof DungeonsPrison;
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 1;
   }
}
