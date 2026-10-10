package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.spawner.MobType;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;
import java.util.Random;

public class BlazeRoom extends BaseRoom {
   public BlazeRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 9;
   }

   public void genFire(Coord origin) {
      Coord start = origin.copy();
      Coord end = start.copy();
      end.up(2);
      BlockType.LAVA_STILL.getBrush().fill(this.worldEditor, RectSolid.newRect(start, end));

      for (Direction dir : Direction.CARDINAL) {
         start = origin.copy();
         start.translate(dir);
         start.translate(dir.antiClockwise());
         end = start.copy();
         end.up(2);
         RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryPillarBrush(), true, false);
         Coord cursor = origin.copy();
         cursor.translate(dir);
         this.primaryStairBrush().setUpsideDown(false).setFacing(dir).stroke(this.worldEditor, cursor, true, false);
         cursor.up();
         BlockType.IRON_BAR.getBrush().stroke(this.worldEditor, cursor);
         cursor.up();
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor, true, false);
         cursor = origin.copy();
         cursor.up(6);
         cursor.translate(dir, 3);

         for (Direction o : dir.orthogonals()) {
            Coord c = cursor.copy();
            c.translate(o, 2);
            this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, c, true, false);
            c.translate(o);
            this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, c, true, false);
         }

         cursor = origin.copy();
         cursor.up();
         cursor.translate(dir, 2);
         if (this.worldEditor.isAirBlock(cursor)) {
            start = origin.copy();
            start.up(3);
            start.translate(dir, 2);
            end = start.copy();
            start.translate(dir.antiClockwise(), 2);
            end.translate(dir.clockwise(), 2);
            RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryStairBrush().setUpsideDown(true).setFacing(dir), true, false);
         }
      }

      start = origin.copy();
      start.up(3);
      start.north(2);
      start.west(2);
      end = origin.copy();
      end.up(7);
      end.south(2);
      end.east(2);
      RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), true, false);
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      Coord start = at.copy();
      start.north(8);
      start.west(8);
      start.down();
      Coord end = at.copy();
      end.south(8);
      end.east(8);
      end.up(7);
      RectHollow.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      start = at.copy();
      start.down();
      end = start.copy();
      start.north(8);
      start.west(8);
      end.south(8);
      end.east(8);
      RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryFloorBrush(), false, true);

      for (Direction dir : Direction.CARDINAL) {
         for (Direction orthogonal : dir.orthogonals()) {
            start = at.copy();
            start.translate(dir, 7);
            start.translate(orthogonal, 2);
            end = start.copy();
            end.up(6);
            this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            Coord cursor = at.copy();
            cursor.translate(dir, 8);
            cursor.translate(orthogonal);
            cursor.up(2);
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal.reverse()).stroke(this.worldEditor, cursor, true, false);
            cursor.translate(dir.reverse());
            cursor.up();
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal.reverse()).stroke(this.worldEditor, cursor);
            start = cursor.copy();
            start.up();
            end = start.copy();
            end.up(3);
            this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            cursor.translate(dir.reverse());
            cursor.translate(orthogonal);
            this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()).stroke(this.worldEditor, cursor);
            start = cursor.copy();
            start.up();
            end = start.copy();
            end.up(3);
            this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            cursor.translate(dir);
            cursor.translate(orthogonal);
            this.primaryStairBrush().setUpsideDown(true).setFacing(orthogonal).stroke(this.worldEditor, cursor);
            start = cursor.copy();
            start.up();
            end = start.copy();
            end.up(3);
            this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         }

         Coord cursor = at.copy();
         cursor.translate(dir, 6);
         cursor.translate(dir.antiClockwise(), 6);
         this.genFire(cursor);
         cursor = at.copy();
         cursor.up(4);
         cursor.translate(dir);
         start = cursor.copy();
         end = cursor.copy();
         end.translate(dir, 6);
         this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         cursor.translate(dir.antiClockwise());
         this.primaryWallBrush().stroke(this.worldEditor, cursor);
         start = end.copy();
         end.up(2);
         end.translate(dir.reverse());
         this.primaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         cursor = end.copy();
         start = cursor.copy();
         start.translate(dir.antiClockwise(), 3);
         end.translate(dir.clockwise(), 3);
         RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), true, false);
         start = cursor.copy();
         start.down();
         end = start.copy();
         start.translate(dir.antiClockwise(), 3);
         end.translate(dir.clockwise(), 3);
         RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()), true, false);
         start = cursor.copy();
         start.translate(dir.reverse());
         end = start.copy();
         start.translate(dir.antiClockwise(), 3);
         end.translate(dir.clockwise(), 3);
         RectSolid.newRect(start, end).fill(this.worldEditor, this.primaryStairBrush().setUpsideDown(true).setFacing(dir.reverse()), true, false);
      }

      start = at.copy();
      end = at.copy();
      start.north(4);
      start.east(4);
      end.south(4);
      end.west(4);
      end.down(4);
      RectHollow.newRect(start, end).fill(this.worldEditor, this.primaryWallBrush(), false, true);
      this.generateLiquidPit(at);
      Coord cursor = at.copy();
      cursor.up(4);
      start = cursor.copy();
      start.down();
      start.north();
      start.east();
      end = cursor.copy();
      end.up();
      end.south();
      end.west();
      BlockType.OBSIDIAN.getBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      this.generateSpawner(cursor, MobType.NETHER_MOBS);
      this.generateDoorways(at, entrances);
      return this;
   }

   private void generateLiquidPit(Coord origin) {
      Coord topLeft = origin.copy().translate(-3, -3, -3);
      Coord bottomRight = origin.copy().translate(3, -2, 3);
      RectSolid liquidPit = RectSolid.newRect(topLeft, bottomRight);
      this.primaryLiquidBrush().fill(this.worldEditor, liquidPit);
      this.generateLiquidPitChest(origin);
   }

   private void generateLiquidPitChest(Coord origin) {
      Random random = this.worldEditor.getRandom();
      if (random.nextDouble() < 0.1) {
         int x = random.nextInt(5) - 2;
         int y = -3;
         int z = random.nextInt(5) - 2;
         Coord chestCoord = origin.copy().translate(x, y, z);
         Direction chestFacing = Direction.randomCardinal(random);
         new TreasureChest(chestCoord, this.worldEditor)
            .withChestType(this.getChestTypeOrUse(ChestType.chooseRandomAmong(this.random(), ChestType.RARE_TREASURES)))
            .withFacing(chestFacing)
            .withTrap(false)
            .stroke(this.worldEditor, chestCoord);
      }
   }
}
