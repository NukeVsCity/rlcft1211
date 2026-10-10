package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.decorative.AnvilBlock;
import com.github.fnar.minecraft.block.normal.SlabBlock;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;
import java.util.stream.Collectors;

public class DungeonsSmithy extends BaseRoom {
   public DungeonsSmithy(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 8;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      this.generateReversedBecauseEntrancesShouldBeOutwardFromRoomCenter(at, entrances.stream().map(Direction::reverse).collect(Collectors.toList()));
      return this;
   }

   private void generateReversedBecauseEntrancesShouldBeOutwardFromRoomCenter(Coord origin, List<Direction> entrances) {
      Direction entranceDirection = this.getEntrance(entrances);
      this.clearBoxes(entranceDirection, origin);
      Coord cursor = origin.copy();
      cursor.translate(entranceDirection, 6);
      this.sideRoom(entranceDirection, cursor);
      this.anvilRoom(entranceDirection, cursor);
      cursor = origin.copy();
      cursor.translate(entranceDirection.reverse(), 6);
      this.sideRoom(entranceDirection, cursor);
      cursor = origin.copy();
      cursor.translate(entranceDirection.reverse(), 9);
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
      cursor.up();
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);
      this.mainRoom(entranceDirection, origin);
   }

   private void sideRoom(Direction entranceDirection, Coord origin) {
      for (Direction side : entranceDirection.orthogonals()) {
         Coord start = origin.copy();
         start.up(3);
         Coord end = start.copy();
         start.translate(side, 2);
         start.translate(entranceDirection.reverse(), 2);
         end.translate(side, 3);
         end.translate(entranceDirection, 2);
         this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         start.translate(entranceDirection);
         end = start.copy();
         end.translate(entranceDirection, 2);
         this.primaryStairBrush().setUpsideDown(true).setFacing(side.reverse()).fill(this.worldEditor, RectSolid.newRect(start, end));

         for (Direction o : side.orthogonals()) {
            start = origin.copy();
            start.translate(side, 3);
            start.translate(o, 2);
            end = start.copy();
            end.up(2);
            this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            Coord cursor = end.copy();
            cursor.translate(side.reverse());
            this.primaryStairBrush().setUpsideDown(true).setFacing(side.reverse()).stroke(this.worldEditor, cursor);
            cursor.up();
            cursor.translate(side.reverse());
            this.primaryStairBrush().setUpsideDown(true).setFacing(side.reverse()).stroke(this.worldEditor, cursor);
            cursor = end.copy();
            cursor.translate(o.reverse());
            this.primaryStairBrush().setUpsideDown(true).setFacing(o.reverse()).stroke(this.worldEditor, cursor);
         }
      }

      Coord cursor = origin.copy();
      cursor.up(4);
      this.overheadLight(cursor);
   }

   private void clearBoxes(Direction entranceDirection, Coord origin) {
      Coord cursor = origin.copy();
      cursor.translate(entranceDirection, 6);
      Coord start = cursor.copy();
      start.down();
      start.translate(entranceDirection, 3);
      start.translate(entranceDirection.antiClockwise(), 4);
      Coord end = cursor.copy();
      end.up(4);
      end.translate(entranceDirection.reverse(), 3);
      end.translate(entranceDirection.clockwise(), 4);
      RectHollow.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush());
      cursor = origin.copy();
      cursor.translate(entranceDirection.reverse(), 6);
      start = cursor.copy();
      start.down();
      start.translate(entranceDirection, 3);
      start.translate(entranceDirection.antiClockwise(), 4);
      end = cursor.copy();
      end.up(4);
      end.translate(entranceDirection.reverse(), 3);
      end.translate(entranceDirection.clockwise(), 4);
      RectHollow.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush());
      start = origin.copy();
      start.down();
      start.translate(entranceDirection.antiClockwise(), 6);
      start.translate(entranceDirection.reverse(), 4);
      end = origin.copy();
      end.up(6);
      end.translate(entranceDirection.clockwise(), 6);
      end.translate(entranceDirection, 4);
      RectHollow.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), false, true);
   }

   private void mainRoom(Direction entranceDirection, Coord origin) {
      Coord start = origin.copy();
      start.translate(entranceDirection, 3);
      start.up(4);
      Coord end = start.copy();
      start.translate(entranceDirection.antiClockwise(), 5);
      end.translate(entranceDirection.clockwise(), 5);
      end.up();
      this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      start.translate(entranceDirection.reverse(), 6);
      end.translate(entranceDirection.reverse(), 6);
      this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));

      for (Direction side : entranceDirection.orthogonals()) {
         for (Direction o : side.orthogonals()) {
            Coord cursor = origin.copy();
            cursor.translate(side, 2);
            cursor.translate(o, 3);
            this.mainPillar(o, cursor);
            cursor.translate(side, 3);
            this.mainPillar(o, cursor);
         }
      }

      this.smelterSide(entranceDirection.antiClockwise(), origin);
      this.fireplace(entranceDirection.clockwise(), origin);
      Coord cursor = origin.copy();
      cursor.up(6);
      this.overheadLight(cursor);
   }

   private void mainPillar(Direction entranceDirection, Coord origin) {
      Coord start = origin.copy();
      Coord end = origin.copy();
      end.up(3);
      this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      Coord cursor = end.copy();
      cursor.translate(entranceDirection.antiClockwise());
      this.primaryStairBrush().setUpsideDown(true).setFacing(entranceDirection.antiClockwise()).stroke(this.worldEditor, cursor);
      cursor = end.copy();
      cursor.translate(entranceDirection.clockwise());
      this.primaryStairBrush().setUpsideDown(true).setFacing(entranceDirection.clockwise()).stroke(this.worldEditor, cursor);
      cursor = end.copy();
      cursor.translate(entranceDirection.reverse());
      this.primaryStairBrush().setUpsideDown(true).setFacing(entranceDirection.reverse()).stroke(this.worldEditor, cursor);
      cursor.up();
      this.primaryWallBrush().stroke(this.worldEditor, cursor);
      cursor.translate(entranceDirection.reverse());
      this.primaryStairBrush().setUpsideDown(true).setFacing(entranceDirection.reverse()).stroke(this.worldEditor, cursor);
      cursor.translate(entranceDirection.reverse());
      cursor.up();
      start = cursor.copy();
      end = cursor.copy();
      end.translate(entranceDirection, 2);
      this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      cursor = end.copy();
      cursor.translate(entranceDirection.antiClockwise());
      this.primaryStairBrush().setUpsideDown(true).setFacing(entranceDirection.antiClockwise()).stroke(this.worldEditor, cursor);
      cursor = end.copy();
      cursor.translate(entranceDirection.clockwise());
      this.primaryStairBrush().setUpsideDown(true).setFacing(entranceDirection.clockwise()).stroke(this.worldEditor, cursor);
   }

   private void smelterSide(Direction entranceDirection, Coord origin) {
      Coord start = origin.copy();
      start.translate(entranceDirection, 5);
      Coord end = start.copy();
      start.translate(entranceDirection.antiClockwise(), 2);
      end.translate(entranceDirection.clockwise(), 2);
      this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      start.translate(entranceDirection.reverse());
      end.translate(entranceDirection.reverse());
      this.primaryStairBrush().setUpsideDown(false).setFacing(entranceDirection.reverse()).fill(this.worldEditor, RectSolid.newRect(start, end));

      for (Direction o : entranceDirection.orthogonals()) {
         Coord cursor = origin.copy();
         cursor.translate(entranceDirection, 3);
         cursor.translate(o);
         this.smelter(entranceDirection, cursor);
         cursor.translate(o, 2);
         this.primaryWallBrush().stroke(this.worldEditor, cursor);
         cursor.translate(entranceDirection);
         this.primaryWallBrush().stroke(this.worldEditor, cursor);
      }
   }

   private void smelter(Direction entranceDirection, Coord origin) {
      Coord cursor = origin.copy();
      new TreasureChest(cursor, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.EMPTY))
         .withFacing(entranceDirection)
         .withTrap(false)
         .stroke(this.worldEditor, cursor);
      cursor.translate(entranceDirection, 2);
      cursor.up(2);
      new TreasureChest(cursor, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.EMPTY))
         .withFacing(entranceDirection)
         .withTrap(false)
         .stroke(this.worldEditor, cursor);
      cursor.up();
      cursor.translate(entranceDirection.reverse());
      new TreasureChest(cursor, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.EMPTY))
         .withFacing(entranceDirection)
         .withTrap(false)
         .stroke(this.worldEditor, cursor);
      cursor = origin.copy();
      cursor.up();
      cursor.translate(entranceDirection);
      BlockType.FURNACE.getBrush().setFacing(entranceDirection).stroke(this.worldEditor, cursor);
      cursor = origin.copy();
      cursor.translate(entranceDirection);
      BlockType.HOPPER.getBrush().setFacing(entranceDirection).stroke(this.worldEditor, cursor);
      cursor.translate(entranceDirection);
      cursor.up();
      BlockType.HOPPER.getBrush().setFacing(entranceDirection).stroke(this.worldEditor, cursor);
      cursor.translate(entranceDirection.reverse());
      cursor.up();
      BlockType.HOPPER.getBrush().setFacing(Direction.DOWN).stroke(this.worldEditor, cursor);
   }

   private void fireplace(Direction entranceDirection, Coord origin) {
      StairsBlock stair = StairsBlock.brick();
      BlockBrush brick = BlockType.BRICK.getBrush();
      BlockBrush brickSlab = SlabBlock.brick();
      BlockBrush bars = BlockType.IRON_BAR.getBrush();
      Coord start = origin.copy();
      start.translate(entranceDirection, 4);
      Coord end = start.copy();
      start.down();
      start.translate(entranceDirection.antiClockwise());
      end.translate(entranceDirection.clockwise());
      end.translate(entranceDirection, 2);
      end.up(5);
      brick.fill(this.worldEditor, RectSolid.newRect(start, end));
      start = origin.copy();
      start.translate(entranceDirection, 5);
      end = start.copy();
      end.up(5);
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));
      Coord cursor = origin.copy();
      cursor.up();
      cursor.translate(entranceDirection, 4);
      SingleBlockBrush.AIR.stroke(this.worldEditor, cursor);

      for (Direction side : entranceDirection.orthogonals()) {
         cursor = origin.copy();
         cursor.translate(entranceDirection, 4);
         cursor.translate(side);
         stair.setUpsideDown(false).setFacing(side.reverse()).stroke(this.worldEditor, cursor);
         cursor.up();
         stair.setUpsideDown(true).setFacing(side.reverse()).stroke(this.worldEditor, cursor);
         cursor.up();
         stair.setUpsideDown(false).setFacing(side).stroke(this.worldEditor, cursor);
         cursor.up();
         bars.stroke(this.worldEditor, cursor);
         cursor.up();
         bars.stroke(this.worldEditor, cursor);
         cursor.up();
         stair.setUpsideDown(true).setFacing(side).stroke(this.worldEditor, cursor);
         cursor = origin.copy();
         cursor.translate(entranceDirection, 3);
         cursor.translate(side);
         stair.setUpsideDown(false).setFacing(entranceDirection.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(side);
         stair.setUpsideDown(false).setFacing(entranceDirection.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(side);
         brick.stroke(this.worldEditor, cursor);
         cursor.translate(entranceDirection);
         brick.stroke(this.worldEditor, cursor);
         cursor.up();
         stair.setUpsideDown(false).setFacing(side.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(entranceDirection.reverse());
         stair.setUpsideDown(false).setFacing(side.reverse()).stroke(this.worldEditor, cursor);
         cursor = origin.copy();
         cursor.translate(entranceDirection, 4);
         cursor.translate(side, 2);
         brick.stroke(this.worldEditor, cursor);
         cursor.translate(entranceDirection);
         brick.stroke(this.worldEditor, cursor);
         cursor.up();
         brick.stroke(this.worldEditor, cursor);
         cursor.up();
         stair.setUpsideDown(false).setFacing(entranceDirection.reverse()).stroke(this.worldEditor, cursor);
         cursor.down();
         cursor.translate(entranceDirection.reverse());
         stair.setUpsideDown(false).setFacing(entranceDirection.reverse()).stroke(this.worldEditor, cursor);
         cursor = origin.copy();
         cursor.translate(entranceDirection, 3);
         cursor.up(5);
         stair.setUpsideDown(true).setFacing(entranceDirection.reverse()).stroke(this.worldEditor, cursor);
      }

      BlockBrush netherrack = BlockType.NETHERRACK.getBrush();
      BlockBrush fire = BlockType.FIRE.getBrush();
      start = origin.copy();
      start.translate(entranceDirection, 5);
      start.down();
      end = start.copy();
      start.translate(entranceDirection.antiClockwise());
      end.translate(entranceDirection.clockwise());
      netherrack.fill(this.worldEditor, RectSolid.newRect(start, end));
      start.up();
      end.up();
      fire.fill(this.worldEditor, RectSolid.newRect(start, end));
      cursor = origin.copy();
      cursor.translate(entranceDirection, 3);
      brickSlab.stroke(this.worldEditor, cursor);
      cursor.translate(entranceDirection);
      brickSlab.stroke(this.worldEditor, cursor);
   }

   private void anvilRoom(Direction entranceDirection, Coord origin) {
      BlockBrush anvil = AnvilBlock.anvil().setFacing(entranceDirection.antiClockwise());
      Coord cursor = origin.copy();
      cursor.translate(entranceDirection);
      anvil.stroke(this.worldEditor, cursor);
      Coord start = origin.copy();
      start.translate(entranceDirection.clockwise(), 2);
      Coord end = start.copy();
      start.translate(entranceDirection, 2);
      end.translate(entranceDirection.reverse(), 2);
      this.primaryStairBrush().setUpsideDown(false).setFacing(entranceDirection.antiClockwise()).fill(this.worldEditor, RectSolid.newRect(start, end));
      cursor = origin.copy();
      cursor.translate(entranceDirection.clockwise(), 3);
      this.primaryWallBrush().stroke(this.worldEditor, cursor);
      cursor.translate(entranceDirection);
      BlockType.WATER_FLOWING.getBrush().stroke(this.worldEditor, cursor);
      cursor.translate(entranceDirection.reverse(), 2);
      BlockType.LAVA_FLOWING.getBrush().stroke(this.worldEditor, cursor);
      cursor = origin.copy();
      cursor.translate(entranceDirection.antiClockwise(), 3);
      start = cursor.copy();
      end = start.copy();
      start.translate(entranceDirection);
      end.translate(entranceDirection.reverse());
      this.primaryStairBrush().setUpsideDown(true).setFacing(entranceDirection.clockwise()).fill(this.worldEditor, RectSolid.newRect(start, end));
      cursor.up();
      new TreasureChest(cursor, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.SMITH))
         .withFacing(entranceDirection.antiClockwise())
         .withTrap(false)
         .stroke(this.worldEditor, cursor);
   }

   private void overheadLight(Coord origin) {
      SingleBlockBrush.AIR.stroke(this.worldEditor, origin);

      for (Direction dir : Direction.CARDINAL) {
         Coord cursor = origin.copy();
         cursor.translate(dir);
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
         cursor.translate(dir.antiClockwise());
         this.primaryStairBrush().setUpsideDown(true).stroke(this.worldEditor, cursor);
      }

      Coord cursor = origin.copy();
      cursor.up(2);
      BlockType.REDSTONE_BLOCK.getBrush().stroke(this.worldEditor, cursor);
      cursor.down();
      BlockType.REDSTONE_LAMP_LIT.getBrush().stroke(this.worldEditor, cursor);
   }
}
