package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.spawner.MobType;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.worldgen.BlockWeightedRandom;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.List;

public class NetherBrickRoom extends BaseRoom {
   public NetherBrickRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 5;
      this.ceilingHeight = 4;
   }

   @Override
   protected void generateCeiling(Coord at, List<Direction> entrances) {
      this.ceilingBrush().fill(this.worldEditor, at.newRect(this.getWallDist()).up(this.getCeilingHeight()));
   }

   private BlockWeightedRandom ceilingBrush() {
      BlockWeightedRandom ceilingBrush = new BlockWeightedRandom();
      ceilingBrush.addBlock(BlockType.FENCE_NETHER_BRICK.getBrush(), 10);
      ceilingBrush.addBlock(SingleBlockBrush.AIR, 5);
      return ceilingBrush;
   }

   @Override
   protected void generateDecorations(Coord at, List<Direction> entrances) {
      Coord coord = this.generateChestLocation(at);
      new TreasureChest(coord, this.worldEditor)
         .withChestType(this.getChestTypeOrUse(ChestType.chooseRandomAmong(this.random(), ChestType.COMMON_TREASURES)))
         .withFacing(this.getEntrance(entrances))
         .withTrap(TreasureChest.shouldBeTrapped(this.random(), this.levelSettings.getLevel()))
         .stroke(this.worldEditor, coord);
      this.generateSpawners(at);
   }

   private void generateSpawners(Coord at) {
      int dist = this.getWallDist();

      for (Direction card : Direction.cardinals()) {
         Coord spawnerCoord = at.copy().translate(card, dist).translate(card.left(), dist).up(this.random().nextInt(2));
         this.generateSpawner(spawnerCoord, new MobType[0]);
      }
   }
}
