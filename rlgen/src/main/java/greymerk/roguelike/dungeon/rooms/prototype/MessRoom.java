package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.theme.BlockSet;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessRoom extends BaseRoom {
   public MessRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 9;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      Coord start = at.copy();
      start.translate(-8, -1, -8);
      Coord end = at.copy();
      end.translate(8, 5, 8);
      RectHollow.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      start = at.copy();
      start.translate(-2, 5, -2);
      end = at.copy();
      end.translate(2, 5, 2);
      RectSolid.newRect(start, end).fill(this.worldEditor, this.secondaryWallBrush(), false, true);
      Coord cursor = at.copy();
      cursor.up(4);
      this.primaryLightBrush().stroke(this.worldEditor, cursor);

      for (Direction dir : Direction.CARDINAL) {
         start = at.copy();
         start.translate(dir, 3);
         start.translate(dir.antiClockwise(), 3);
         end = start.copy();
         end.up(3);
         this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));

         for (Direction d : Direction.CARDINAL) {
            cursor = end.copy();
            cursor.translate(d);
            this.primaryStairBrush().setUpsideDown(true).setFacing(d).stroke(this.worldEditor, cursor);
         }

         start = at.copy();
         start.translate(dir, 3);
         start.up(4);
         end = start.copy();
         start.translate(dir.antiClockwise(), 3);
         end.translate(dir.clockwise(), 3);
         this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         Direction[] corners = new Direction[]{dir, dir.antiClockwise()};
         if (entrances.size() == 4 && dir == this.getEntrance(entrances)) {
            this.supplyCorner(corners, at, dir);
         } else {
            this.corner(corners, at);
         }

         this.generateWallDecor(dir, at);
         cursor = at.copy().up(4).translate(dir);
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor);
         cursor.translate(dir);
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
      }

      List<Direction> nonDoors = new ArrayList<>();

      for (Direction dir : Direction.CARDINAL) {
         if (!entrances.contains(dir)) {
            nonDoors.add(dir);
         }
      }

      Collections.shuffle(nonDoors);
      switch (nonDoors.size()) {
         case 3:
            this.sideTable(nonDoors.get(2), at);
         case 2:
            this.fireplace(nonDoors.get(1), at);
         case 1:
            this.supplies(this.getEntrance(nonDoors), at);
         default:
            this.generateDoorways(at, entrances);
            return this;
      }
   }

   private void supplyCorner(Direction[] corners, Coord origin, Direction entranceDir) {
      BlockBrush wall = this.theme().getPrimary().getWall();
      Coord start = origin.copy().translate(corners[0], 7).translate(corners[1], 7);
      Coord end = start.copy().up(4);
      wall.fill(this.worldEditor, RectSolid.newRect(start, end));
      start = origin.copy().translate(corners[0], 4).translate(corners[1], 4).up(4);
      this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      Coord cursor = origin.copy().translate(corners[0], 7).translate(corners[1], 5).up();
      new TreasureChest(cursor, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.FOOD))
         .withFacing(entranceDir.reverse())
         .withTrap(false)
         .stroke(this.worldEditor, cursor);
      cursor = origin.copy().translate(corners[0], 5).translate(corners[1], 7);
      BlockType.FURNACE.getBrush().setFacing(corners[1].reverse()).stroke(this.worldEditor, cursor);
      this.generateEntrywayWithDecor(corners, origin, true);
   }

   private void corner(Direction[] corners, Coord origin) {
      StairsBlock table = this.secondaryStairBrush();
      Coord start = origin.copy().translate(corners[0], 7).translate(corners[1], 7);
      Coord end = start.copy().up(4);
      this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      start = origin.copy().translate(corners[0], 4).translate(corners[1], 4).up(4);
      this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      Coord cursor = origin.copy().translate(corners[0], 4).translate(corners[1], 4);
      table.setUpsideDown(true).setFacing(corners[0].reverse()).stroke(this.worldEditor, cursor);
      cursor.translate(corners[1]);
      table.setUpsideDown(true).setFacing(corners[1]).stroke(this.worldEditor, cursor);
      cursor.translate(corners[0]);
      table.setUpsideDown(true).setFacing(corners[0]).stroke(this.worldEditor, cursor);
      cursor.translate(corners[1].reverse());
      table.setUpsideDown(true).setFacing(corners[1].reverse()).stroke(this.worldEditor, cursor);
      this.generateEntrywayWithDecor(corners, origin, false);
   }

   private void generateEntrywayWithDecor(Direction[] entrances, Coord origin, boolean areSomeStairsUpsideDown) {
      BlockSet primaryBlockSet = this.theme().getPrimary();
      BlockBrush wall = primaryBlockSet.getWall();
      BlockBrush pillar = primaryBlockSet.getPillar();
      StairsBlock stair = primaryBlockSet.getStair();

      for (Direction dir : entrances) {
         Coord cursor = origin.copy();
         cursor.translate(entrances[0], 3);
         cursor.translate(entrances[1], 3);
         cursor.translate(dir, 4);
         Coord start = cursor.copy();
         Coord end = cursor.copy();
         end.up(3);
         pillar.fill(this.worldEditor, RectSolid.newRect(start, end));
         cursor.up(3);
         cursor.translate(dir.reverse());
         wall.stroke(this.worldEditor, cursor);
         cursor.down();
         stair.setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
         cursor.up();
         cursor.translate(dir.reverse());
         stair.setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
         cursor.up();
         cursor.translate(dir.reverse());
         start = cursor.copy();
         end = cursor.copy();
         end.translate(dir, 3);
         wall.fill(this.worldEditor, RectSolid.newRect(start, end));
         cursor = origin.copy();
         cursor.translate(entrances[0], 5);
         cursor.translate(entrances[1], 5);
         cursor.translate(dir, 2);
         start = cursor.copy();
         start.translate(dir.antiClockwise());
         end = cursor.copy();
         end.translate(dir.clockwise());
         stair.setUpsideDown(areSomeStairsUpsideDown).setFacing(dir.reverse()).fill(this.worldEditor, RectSolid.newRect(start, end));
         start.up(2);
         end.up(2);
         stair.setUpsideDown(true).setFacing(dir.clockwise()).stroke(this.worldEditor, start);
         stair.setUpsideDown(true).setFacing(dir.antiClockwise()).stroke(this.worldEditor, end);
         start.up();
         end.up();
         wall.fill(this.worldEditor, RectSolid.newRect(start, end));
         start.translate(dir);
         end.translate(dir);
         end.down(3);
         RectSolid.newRect(start, end).fill(this.worldEditor, this.secondaryWallBrush(), false, true);
      }
   }

   private void generateWallDecor(Direction dir, Coord origin) {
      Coord start = origin.copy().translate(dir, 7).up(3).translate(dir.left(), 2);
      Coord end = origin.copy().translate(dir, 7).up(4).translate(dir.right(), 2);
      this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));

      for (Direction o : dir.orthogonals()) {
         Coord cursor = origin.copy();
         cursor.translate(dir, 7);
         cursor.translate(o, 2);
         cursor.up(2);
         this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(dir.reverse());
         cursor.up(2);
         this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(o.reverse());
         this.primaryStairBrush().setUpsideDown(true).setFacing(o).stroke(this.worldEditor, cursor);
      }

      Coord cursor = origin.copy();
      cursor.translate(dir, 6);
      cursor.up(3);
      this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
      cursor.up();
      this.primaryWallBrush().stroke(this.worldEditor, cursor);
      cursor.translate(dir.reverse());
      this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
      cursor.translate(dir.reverse());
      this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor);
      start = origin.copy();
      start.up(5);
      start.translate(dir, 4);
      end = start.copy();
      end.translate(dir);
      start.translate(dir.antiClockwise(), 2);
      end.translate(dir.clockwise(), 2);
      RectSolid.newRect(start, end).fill(this.worldEditor, this.secondaryWallBrush(), false, true);
   }

   private void fireplace(Direction dir, Coord origin) {
      RectSolid behindMantle = RectSolid.newRect(
         origin.copy().translate(dir, 7).translate(dir.left(), 2), origin.copy().translate(dir, 8).translate(dir.right(), 2).up(2)
      );
      this.primaryWallBrush().fill(this.worldEditor, behindMantle);
      RectSolid fireArea = RectSolid.newRect(origin.copy().translate(dir, 7).translate(dir.left()), origin.copy().translate(dir, 7).translate(dir.right()).up());
      SingleBlockBrush.AIR.fill(this.worldEditor, fireArea);
      Coord cursor = origin.copy();
      cursor.translate(dir, 6);
      BlockType.IRON_BAR.getBrush().stroke(this.worldEditor, cursor);
      cursor.translate(dir);
      cursor.down();
      BlockType.NETHERRACK.getBrush().stroke(this.worldEditor, cursor);
      cursor.up();
      BlockType.FIRE.getBrush().stroke(this.worldEditor, cursor);
      cursor.up();
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
      cursor.up();
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
      cursor.translate(dir.reverse());
      this.primaryStairBrush().setUpsideDown(false).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);

      for (Direction o : dir.orthogonals()) {
         cursor = origin.copy().translate(dir, 6).translate(o);
         BlockType.IRON_BAR.getBrush().stroke(this.worldEditor, cursor);
         cursor.translate(o);
         this.theme().getPrimary().getWall().stroke(this.worldEditor, cursor);
         cursor.up();
         this.primaryStairBrush().setUpsideDown(false).setFacing(o).stroke(this.worldEditor, cursor);
         cursor.translate(o.reverse());
         this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(dir);
         this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
         cursor.up().translate(dir.reverse());
         this.primaryStairBrush().setUpsideDown(false).setFacing(o).stroke(this.worldEditor, cursor);
         cursor = origin.copy().translate(dir, 6).translate(o);
         BlockType.IRON_BAR.getBrush().stroke(this.worldEditor, cursor);
         cursor.translate(dir).down();
         BlockType.NETHERRACK.getBrush().stroke(this.worldEditor, cursor);
         cursor.up();
         BlockType.FIRE.getBrush().stroke(this.worldEditor, cursor);
      }
   }

   private void supplies(Direction dir, Coord origin) {
      Coord cursor = origin.copy();
      cursor.translate(dir, 7);
      this.theme().getPrimary().getStair().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
      cursor.up();
      new TreasureChest(cursor, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.FOOD))
         .withFacing(dir)
         .withTrap(false)
         .stroke(this.worldEditor, cursor);
      cursor.translate(dir.antiClockwise());
      BlockType.FURNACE.getBrush().setFacing(dir).stroke(this.worldEditor, cursor);
      cursor.translate(dir.clockwise(), 2);
      BlockType.CRAFTING_TABLE.getBrush().stroke(this.worldEditor, cursor);

      for (Direction o : dir.orthogonals()) {
         cursor = origin.copy();
         cursor.translate(dir, 7);
         cursor.translate(o);
         this.theme().getPrimary().getStair().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(o);
         this.theme().getPrimary().getStair().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
         cursor.up();
         this.theme().getPrimary().getStair().setUpsideDown(false).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
      }
   }

   private void sideTable(Direction dir, Coord origin) {
      StairsBlock table = this.secondaryStairBrush();
      Coord cursor = origin.copy();
      cursor.translate(dir, 5);
      table.setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
      cursor.translate(dir);
      table.setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor);

      for (Direction o : dir.orthogonals()) {
         cursor = origin.copy();
         cursor.translate(dir, 5);
         cursor.translate(o);
         table.setUpsideDown(true).setFacing(o).stroke(this.worldEditor, cursor);
         cursor.translate(dir);
         table.setUpsideDown(true).setFacing(o).stroke(this.worldEditor, cursor);
      }
   }
}
