package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import com.github.fnar.minecraft.block.spawner.MobType;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.HashSet;
import java.util.List;

public class ObsidianRoom extends BaseRoom {
   public ObsidianRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 11;
      this.ceilingHeight = 6;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      super.generate(at, entrances);
      this.generateHollowSpace(at);
      this.generateRoof(at);
      this.generateFoundation(at);
      HashSet<Coord> spawnerLocations = this.generateSpawnerHolesInCeiling(at);
      this.generateCeilingTrimAndOuterWalls(at);
      this.generateOuterPillars(at);
      this.generateUpperMidFloor(at);
      this.generateMidOuterFloors(at);
      this.generateInnerPillars(at);

      for (Coord space : spawnerLocations) {
         this.generateSpawner(space, new MobType[0]);
      }

      this.generateDoorways(at, entrances);
      this.generateChests(at);
      return this;
   }

   private void generateHollowSpace(Coord origin) {
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(origin.copy().translate(-10, -3, -10), origin.copy().translate(10, 3, 10)));
   }

   private void generateRoof(Coord origin) {
      this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(origin.copy().translate(-7, 6, -7), origin.copy().translate(7, 6, 7)));
      this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(origin.copy().translate(-8, 5, -8), origin.copy().translate(8, 5, 8)));
      this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(origin.copy().translate(-9, 4, -9), origin.copy().translate(9, 4, 9)));
      SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(origin.copy().translate(-1, 3, -1), origin.copy().translate(1, 5, 1)));
      this.secondaryWallBrush().stroke(this.worldEditor, origin.copy().translate(0, 5, 0));
   }

   private void generateFoundation(Coord origin) {
      this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(origin.copy().translate(-10, -4, -10), origin.copy().translate(10, -4, 10)));
   }

   private HashSet<Coord> generateSpawnerHolesInCeiling(Coord origin) {
      HashSet<Coord> spawnerLocations = new HashSet<>();
      spawnerLocations.add(origin.copy().translate(0, 4, 0));

      for (Direction dir : Direction.CARDINAL) {
         for (Direction orthogonal : dir.orthogonals()) {
            Coord start = origin.copy();
            start.up(3);
            start.translate(dir, 3);
            start.translate(orthogonal, 3);
            Coord end = start.copy();
            end.up(2);
            end.translate(dir, 2);
            end.translate(orthogonal, 2);
            SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));
            start = origin.copy();
            start.translate(dir, 3);
            start.up(3);
            end = start.copy();
            end.translate(dir, 2);
            start.translate(orthogonal, 1);
            end.up(2);
            SingleBlockBrush.AIR.fill(this.worldEditor, RectSolid.newRect(start, end));
            Coord cursor = origin.copy();
            cursor.up(4);
            cursor.translate(dir, 4);
            spawnerLocations.add(cursor.copy());
            cursor.translate(orthogonal, 4);
            spawnerLocations.add(cursor.copy());
            cursor = origin.copy();
            cursor.up(5);
            cursor.translate(dir, 4);
            this.secondaryWallBrush().stroke(this.worldEditor, cursor);
            cursor.translate(orthogonal, 4);
            this.secondaryWallBrush().stroke(this.worldEditor, cursor);
         }
      }

      return spawnerLocations;
   }

   private void generateCeilingTrimAndOuterWalls(Coord origin) {
      for (Direction dir : Direction.CARDINAL) {
         Coord start = origin.copy();
         start.translate(dir, 10);
         Coord end = start.copy();
         start.translate(dir.antiClockwise(), 9);
         end.translate(dir.clockwise(), 9);
         start.down(4);
         end.down();
         this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         start.up(7);
         end.up(4);
         this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         start = origin.copy();
         start.translate(dir, 6);
         start.up(3);
         end = start.copy();
         start.translate(dir.antiClockwise(), 9);
         end.translate(dir.clockwise(), 9);
         this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         start = origin.copy();
         start.translate(dir, 2);
         start.up(3);
         end = start.copy();
         start.translate(dir.antiClockwise(), 9);
         end.translate(dir.clockwise(), 9);
         this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         start = origin.copy();
         start.translate(dir, 11);
         end = start.copy();
         start.down(3);
         end.up(3);
         start.translate(dir.antiClockwise(), 11);
         end.translate(dir.clockwise(), 11);
         RectSolid.newRect(start, end).fill(this.worldEditor, this.secondaryWallBrush(), false, true);
      }
   }

   private void generateOuterPillars(Coord origin) {
      for (Direction dir : Direction.CARDINAL) {
         for (Direction orthogonal : dir.orthogonals()) {
            Coord pillarLocation = origin.copy();
            pillarLocation.translate(dir, 10);
            pillarLocation.translate(orthogonal, 2);
            this.generateOuterPillar(pillarLocation, dir, this.primaryPillarBrush());
            pillarLocation.translate(orthogonal, 3);
            this.generateOuterPillar(pillarLocation, dir, this.secondaryPillarBrush());
            pillarLocation.translate(orthogonal, 3);
            this.generateOuterPillar(pillarLocation, dir, this.secondaryPillarBrush());
            pillarLocation.translate(orthogonal, 2);
            this.generateOuterPillar(pillarLocation, dir, this.primaryPillarBrush());
         }
      }
   }

   private void generateOuterPillar(Coord pillarLocation, Direction dir, BlockBrush blockBrush) {
      blockBrush.fill(this.worldEditor, RectSolid.newRect(pillarLocation.copy().down(2), pillarLocation.copy().up(3)));
      Coord blockLocation = pillarLocation.copy().up(3).translate(dir, 1);
      blockBrush.stroke(this.worldEditor, blockLocation);

      for (int i = 0; i < 3; i++) {
         blockLocation.up(1).translate(dir.reverse(), 1);
         blockBrush.stroke(this.worldEditor, blockLocation);
      }
   }

   private void generateUpperMidFloor(Coord origin) {
      BlockBrush primaryWall = this.primaryWallBrush();

      for (Direction dir : Direction.CARDINAL) {
         Coord start = origin.copy();
         start.down();
         Coord end = start.copy();
         end.down(3);
         start.translate(dir, 9);
         start.translate(dir.antiClockwise(), 1);
         end.translate(dir.clockwise(), 1);
         primaryWall.fill(this.worldEditor, RectSolid.newRect(start, end));
      }
   }

   private void generateMidOuterFloors(Coord origin) {
      BlockBrush primaryWall = this.primaryWallBrush();

      for (Direction dir : Direction.CARDINAL) {
         for (Direction orthogonal : dir.orthogonals()) {
            Coord start = origin.copy();
            Coord end = start.copy();
            start.translate(dir, 9);
            start.translate(orthogonal, 2);
            start.down(3);
            end.translate(dir, 8);
            end.translate(orthogonal, 9);
            end.down(2);
            primaryWall.fill(this.worldEditor, RectSolid.newRect(start, end));
            StairsBlock stair = this.primaryStairBrush();
            Coord stepCoord = origin.copy();
            stepCoord.translate(dir, 8);
            stepCoord.down();
            stepCoord.translate(orthogonal, 2);
            stair.setUpsideDown(false).setFacing(orthogonal);
            stair.stroke(this.worldEditor, stepCoord);
            stepCoord.translate(dir, 1);
            stair.stroke(this.worldEditor, stepCoord);
            stair.setUpsideDown(false).setFacing(dir.reverse());
            stepCoord = origin.copy();
            stepCoord.down(2);
            stepCoord.translate(dir, 7);
            stepCoord.translate(orthogonal, 3);
            stair.stroke(this.worldEditor, stepCoord);
            stepCoord.translate(orthogonal, 1);
            stair.stroke(this.worldEditor, stepCoord);
            stepCoord.down();
            stepCoord.translate(dir.reverse(), 1);
            stair.stroke(this.worldEditor, stepCoord);
            stepCoord.translate(orthogonal.reverse(), 1);
            stair.stroke(this.worldEditor, stepCoord);
            stepCoord.translate(dir, 1);
            primaryWall.stroke(this.worldEditor, stepCoord);
            stepCoord.translate(orthogonal, 1);
            primaryWall.stroke(this.worldEditor, stepCoord);
            Coord corner = origin.copy();
            corner.translate(dir, 7);
            corner.translate(orthogonal, 7);
            corner.down(2);
            primaryWall.stroke(this.worldEditor, corner);
            corner.down();
            primaryWall.stroke(this.worldEditor, corner);
         }
      }
   }

   private void generateInnerPillars(Coord origin) {
      for (Direction dir : Direction.CARDINAL) {
         for (Direction orthogonal : dir.orthogonals()) {
            Coord pillar = origin.copy().translate(dir, 2).translate(orthogonal, 2);
            this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(pillar.copy().down(4), pillar.copy().up(4)));
            pillar.translate(dir, 4);
            this.secondaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(pillar.copy().down(4), pillar.copy().up(4)));
            pillar.translate(orthogonal, 3);
            this.secondaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(pillar.copy().down(4), pillar.copy().up(4)));
            Coord start = origin.copy();
            start.down();
            start.translate(orthogonal, 2);
            start.translate(dir, 2);
            Coord end = start.copy();
            end.translate(dir, 5);
            this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
            start = origin.copy();
            start.down();
            start.translate(dir, 7);
            start.translate(orthogonal, 5);
            this.secondaryPillarBrush().stroke(this.worldEditor, start);
            start.down();
            end = start.copy();
            end.translate(dir.reverse(), 1);
            end.translate(orthogonal, 1);
            end.down();
            this.secondaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
         }
      }
   }

   private void generateChests(Coord origin) {
      for (Direction dir : Direction.CARDINAL) {
         for (Direction orthogonal : dir.orthogonals()) {
            Coord cursor = origin.copy();
            cursor.down(2);
            cursor.translate(dir, 3);
            this.liquidWindow(this.worldEditor, cursor.copy(), orthogonal);
            cursor.translate(dir, 2);
            this.liquidWindow(this.worldEditor, cursor.copy(), orthogonal);
            Coord chestPos = origin.copy();
            chestPos.translate(dir, 4);
            chestPos.translate(orthogonal, 2);
            chestPos.down(3);
            new TreasureChest(chestPos, this.worldEditor)
               .withChestType(this.getChestTypeOrUse(ChestType.chooseRandomAmong(this.random(), ChestType.RARE_TREASURES)))
               .withFacing(orthogonal.reverse())
               .withTrap(TreasureChest.shouldBeTrapped(this.random(), this.levelSettings.getLevel()))
               .stroke(this.worldEditor, chestPos);
         }
      }
   }

   private void liquidWindow(WorldEditor editor, Coord cursor, Direction orthogonal) {
      RectSolid.newRect(cursor, cursor).fill(editor, this.primaryLiquidBrush());
      cursor.down();
      RectSolid.newRect(cursor, cursor).fill(editor, this.primaryLiquidBrush());
      BlockBrush fence = BlockType.FENCE_NETHER_BRICK.getBrush();
      cursor.translate(orthogonal, 1);
      fence.stroke(editor, cursor);
      cursor.up(1);
      fence.stroke(editor, cursor);
   }
}
