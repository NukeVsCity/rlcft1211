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
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class DungeonsSpiderNest extends BaseRoom {
   int originX;
   int originY;
   int originZ;
   byte dungeonHeight;
   int dungeonLength;
   int dungeonWidth;

   public DungeonsSpiderNest(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 3;
      this.dungeonHeight = 2;
      this.dungeonLength = 3;
      this.dungeonWidth = 3;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      this.originX = at.getX();
      this.originY = at.getY();
      this.originZ = at.getZ();
      BlockWeightedRandom webs = new BlockWeightedRandom();
      webs.addBlock(BlockType.WEB.getBrush(), 3);
      webs.addBlock(SingleBlockBrush.AIR, 1);

      for (int blockX = this.originX - this.dungeonLength - 1; blockX <= this.originX + this.dungeonLength + 1; blockX++) {
         for (int blockZ = this.originZ - this.dungeonWidth - 1; blockZ <= this.originZ + this.dungeonWidth + 1; blockZ++) {
            for (int blockY = this.originY + this.dungeonHeight; blockY >= this.originY - this.dungeonHeight; blockY--) {
               int x = Math.abs(blockX - this.originX);
               int z = Math.abs(blockZ - this.originZ);
               int clearHeight = Math.max(x, z);
               if (blockY == this.originY) {
                  webs.stroke(this.worldEditor, new Coord(blockX, blockY, blockZ));
               }

               if (clearHeight < 1) {
                  clearHeight = 1;
               }

               if (Math.abs(blockY - this.originY) <= clearHeight) {
                  if (this.random().nextInt(clearHeight) == 0) {
                     webs.stroke(this.worldEditor, new Coord(blockX, blockY, blockZ));
                  } else if (this.random().nextInt(5) == 0) {
                     BlockType.GRAVEL.getBrush().stroke(this.worldEditor, new Coord(blockX, blockY, blockZ));
                  }
               }
            }
         }
      }

      Coord cursor = new Coord(this.originX, this.originY, this.originZ);
      this.generateSpawner(cursor, new MobType[]{MobType.CAVESPIDER});
      List<Coord> spaces = RectSolid.newRect(
            new Coord(this.originX - this.dungeonLength, this.originY - 1, this.originZ - this.dungeonWidth),
            new Coord(this.originX + this.dungeonLength, this.originY + 1, this.originZ + this.dungeonWidth)
         )
         .get();
      Coord.randomFrom(spaces, 1 + this.random().nextInt(3), this.random())
         .forEach(
            chestLocation -> new TreasureChest(chestLocation, this.worldEditor)
                  .withChestType(this.getChestTypeOrUse(ChestType.chooseRandomAmong(this.random(), ChestType.UNCOMMON_TREASURES)))
                  .withFacing(this.getEntrance(entrances))
                  .withTrap(false)
                  .stroke(this.worldEditor, chestLocation)
         );
      return this;
   }
}
