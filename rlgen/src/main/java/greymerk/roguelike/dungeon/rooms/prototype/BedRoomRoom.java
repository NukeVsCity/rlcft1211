package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.decorative.BedBlock;
import com.github.fnar.minecraft.block.decorative.FlowerPotBlock;
import com.github.fnar.minecraft.block.decorative.TorchBlock;
import com.github.fnar.minecraft.item.Material;
import com.github.fnar.minecraft.item.RldItemStack;
import com.github.fnar.roguelike.worldgen.generatables.Pillar;
import com.google.common.collect.Lists;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.util.DyeColor;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class BedRoomRoom extends BaseRoom {
   public BedRoomRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      Direction entranceDirection = this.getEntrance(entrances);
      this.generateWalls(at, entranceDirection);
      this.generateFloorDecorationPattern(at, entranceDirection);
      this.generateShelves(at, entranceDirection);
      this.generateCeiling(at, entranceDirection);
      this.generatePillars(at, entranceDirection);
      this.generateCrossbeams(at, entranceDirection);
      this.generateBed(at, entranceDirection);
      this.generateChestz(at, entranceDirection);
      this.generateCraftingTable(at, entranceDirection);
      this.generateFurnace(at, entranceDirection);
      return this;
   }

   private void generateWalls(Coord origin, Direction entranceDirection) {
      Coord start = origin.copy().translate(entranceDirection.reverse().antiClockwise(), 4).translate(entranceDirection, 4).down();
      Coord end = origin.copy().translate(entranceDirection.reverse().clockwise(), 4).translate(entranceDirection.reverse(), 4).up(4);
      RectHollow walls = RectHollow.newRect(start, end);
      this.primaryWallBrush().fill(this.worldEditor, walls, false, true);
   }

   private void generateFloorDecorationPattern(Coord origin, Direction entranceDirection) {
      Coord start = origin.copy().down().translate(entranceDirection.antiClockwise(), 1).translate(entranceDirection, 2);
      Coord end = origin.copy().down().translate(entranceDirection.clockwise(), 1).translate(entranceDirection.reverse(), 2);
      RectSolid floorPattern = RectSolid.newRect(start, end);
      this.secondaryWallBrush().fill(this.worldEditor, floorPattern);
   }

   private void generateCeiling(Coord origin, Direction entranceDirection) {
      for (Direction outward : entranceDirection.orthogonals()) {
         BlockBrush stair = this.secondaryStairBrush().setUpsideDown(true).setFacing(outward.reverse());
         Coord rowStart = origin.copy().translate(outward, 3).translate(outward.antiClockwise(), 2);
         Coord rowEnd = rowStart.copy().translate(outward.clockwise(), 4);
         RectSolid row = RectSolid.newRect(rowStart, rowEnd);
         row.translate(Direction.UP, 2);
         stair.fill(this.worldEditor, row);
         row.translate(Direction.UP, 1);
         this.primaryWallBrush().fill(this.worldEditor, row);
         row.translate(outward.reverse(), 1);
         stair.fill(this.worldEditor, row);
      }
   }

   private void generateCrossbeams(Coord origin, Direction entranceDirection) {
      Coord beamCenter = origin.copy().up(3).translate(entranceDirection, 3);
      Coord beamStart = beamCenter.copy().translate(entranceDirection.antiClockwise(), 2);
      Coord beamEnd = beamCenter.copy().translate(entranceDirection.clockwise(), 2);
      RectSolid beam = RectSolid.newRect(beamStart, beamEnd);

      for (int i = 0; i < 3; i++) {
         this.secondaryWallBrush().fill(this.worldEditor, beam);
         beam.translate(entranceDirection.reverse(), 3);
      }
   }

   private void generateShelves(Coord origin, Direction entranceDirection) {
      for (Direction outward : entranceDirection.orthogonals()) {
         BlockBrush stair = this.secondaryStairBrush().setUpsideDown(true).setFacing(outward.reverse());
         Coord shelfStart = origin.copy().translate(outward, 3).translate(outward.antiClockwise(), 2);
         Coord shelfEnd = shelfStart.copy().translate(outward.clockwise(), 4);
         RectSolid shelves = RectSolid.newRect(shelfStart, shelfEnd);
         stair.fill(this.worldEditor, shelves);
      }
   }

   private void generatePillars(Coord origin, Direction entranceDirection) {
      List<Coord> pillarCoords = Lists.newArrayList();

      for (Direction orthogonal : entranceDirection.orthogonals()) {
         Coord pillar = origin.copy().translate(orthogonal, 3);
         pillarCoords.add(pillar);

         for (Direction orthogonalOrthogonal : orthogonal.orthogonals()) {
            pillarCoords.add(pillar.copy().translate(orthogonalOrthogonal, 3));
         }
      }

      Pillar.newPillar(this.worldEditor).withPillar(this.primaryPillarBrush()).withStairs(this.secondaryStairBrush()).withHeight(3).generate(pillarCoords);
   }

   private void generateBed(Coord origin, Direction entranceDirection) {
      Direction side = this.random().nextBoolean() ? entranceDirection.antiClockwise() : entranceDirection.clockwise();
      Coord cursor = origin.copy();
      cursor.translate(entranceDirection.reverse(), 3);
      BedBlock.bed().setColor(DyeColor.chooseRandom(this.random())).setFacing(entranceDirection).stroke(this.worldEditor, cursor);
      cursor.translate(side, 2);
      BlockType.BOOKSHELF.getBrush().stroke(this.worldEditor, cursor);
      cursor.up();
      FlowerPotBlock.flowerPot().withRandomContent(this.random()).stroke(this.worldEditor, cursor);
      cursor.translate(side.reverse(), 3);
      cursor.down();
      this.secondaryStairBrush().setUpsideDown(true).setFacing(entranceDirection).stroke(this.worldEditor, cursor);
      cursor.up();
      TorchBlock.torch().setFacing(Direction.UP).stroke(this.worldEditor, cursor);
   }

   private void generateChestz(Coord origin, Direction entranceDirection) {
      Direction side = entranceDirection.reverse().orthogonals()[this.random().nextBoolean() ? 1 : 0];
      Coord cursor = origin.copy().translate(entranceDirection.reverse()).translate(side, 3);
      Coord chestLocation = cursor.add(Direction.UP);
      new TreasureChest(chestLocation, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.STARTER))
         .withFacing(side)
         .withTrap(false)
         .stroke(this.worldEditor, chestLocation);
   }

   private void generateCraftingTable(Coord origin, Direction entranceDirection) {
      Direction side = entranceDirection.reverse().orthogonals()[this.random().nextBoolean() ? 1 : 0];
      Coord cursor = origin.copy().translate(entranceDirection.reverse()).translate(side, 3).translate(side.reverse(), 6);
      if (this.random().nextBoolean()) {
         this.generateTorchAbove(cursor);
         cursor.translate(entranceDirection.reverse());
         BlockType.CRAFTING_TABLE.getBrush().stroke(this.worldEditor, cursor);
      } else {
         BlockType.CRAFTING_TABLE.getBrush().stroke(this.worldEditor, cursor);
         cursor.translate(entranceDirection.reverse());
         this.generateTorchAbove(cursor);
      }
   }

   private void generateTorchAbove(Coord cursor) {
      cursor.up();
      TorchBlock.torch().setFacing(Direction.UP).stroke(this.worldEditor, cursor);
      cursor.down();
   }

   private void generateFurnace(Coord origin, Direction entranceDirection) {
      Direction side = this.random().nextBoolean() ? entranceDirection.antiClockwise() : entranceDirection.clockwise();
      Coord furnace = origin.copy().translate(entranceDirection).translate(side, 3).translate(entranceDirection, this.random().nextBoolean() ? 1 : 0);
      BlockType.FURNACE.getBrush().setFacing(side.reverse()).stroke(this.worldEditor, furnace);
      RldItemStack coal = Material.Type.COAL.asItemStack().withCount(2 + this.random().nextInt(3));
      this.worldEditor.setItem(furnace, 1, coal);
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof BedRoomRoom)) {
         return false;
      } else {
         BedRoomRoom other = (BedRoomRoom)o;
         return other.canEqual(this);
      }
   }

   @Override
   protected boolean canEqual(Object other) {
      return other instanceof BedRoomRoom;
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 1;
   }
}
