package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
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

public class CakeRoom extends BaseRoom {
   private final int width = this.random().nextInt(2) + 2;
   private final int length = this.random().nextInt(2) + 3;

   public CakeRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = Math.max(this.width, this.length);
      this.ceilingHeight = 4;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      super.generate(at, entrances);
      int x = at.getX();
      int y = at.getY();
      int z = at.getZ();
      int height = 3;
      SingleBlockBrush.AIR
         .fill(this.worldEditor, RectSolid.newRect(new Coord(x - this.width, y, z - this.length), new Coord(x + this.width, y + height, z + this.length)));
      RectHollow floorAccentRect = RectHollow.newRect(
         at.copy().west(this.width + 1).north(this.length + 1).down(), at.copy().east(this.width + 1).south(this.length + 1).up(height + 1)
      );
      this.secondaryFloorBrush().fill(this.worldEditor, floorAccentRect, false, true);
      this.secondaryPillarBrush()
         .fill(this.worldEditor, RectSolid.newRect(new Coord(x - this.width, y, z - this.length), new Coord(x - this.width, y + height, z - this.length)));
      this.secondaryPillarBrush()
         .fill(this.worldEditor, RectSolid.newRect(new Coord(x - this.width, y, z + this.length), new Coord(x - this.width, y + height, z + this.length)));
      this.secondaryPillarBrush()
         .fill(this.worldEditor, RectSolid.newRect(new Coord(x + this.width, y, z - this.length), new Coord(x + this.width, y + height, z - this.length)));
      this.secondaryPillarBrush()
         .fill(this.worldEditor, RectSolid.newRect(new Coord(x + this.width, y, z + this.length), new Coord(x + this.width, y + height, z + this.length)));
      this.generateLightAccents(at, this.getEntrance(entrances));
      this.placeCake(at);
      Coord coord = this.generateChestLocation(at);
      new TreasureChest(coord, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.FOOD))
         .withFacing(this.getEntrance(entrances).reverse())
         .withTrap(false)
         .stroke(this.worldEditor, coord);
      return this;
   }

   private void generateLightAccents(Coord at, Direction entrance) {
      for (Direction orthogonal : entrance.orthogonals()) {
         Coord lightCoord = at.copy().translate(orthogonal, this.width - 1).down();
         Coord leftLight = lightCoord.copy().translate(orthogonal.left(), this.length - 1);
         Coord rightLight = lightCoord.copy().translate(orthogonal.right(), this.length - 1);
         this.primaryLightBrush().stroke(this.worldEditor, leftLight);
         this.primaryLightBrush().stroke(this.worldEditor, rightLight);
         this.primaryLightBrush().stroke(this.worldEditor, leftLight.copy().up(1 + this.ceilingHeight));
         this.primaryLightBrush().stroke(this.worldEditor, rightLight.copy().up(1 + this.ceilingHeight));
      }
   }

   private void placeCake(Coord at) {
      this.primaryPillarBrush().stroke(this.worldEditor, at);
      BlockType.CAKE.getBrush().stroke(this.worldEditor, at.copy().up());
   }
}
