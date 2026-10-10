package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.spawner.MobType;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.BlockWeightedRandom;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class DungeonsCreeperDen extends BaseRoom {
   public static final BlockBrush TNT_META_BLOCK = BlockType.TNT.getBrush();

   public DungeonsCreeperDen(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 6;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      BlockWeightedRandom mossy = new BlockWeightedRandom();
      mossy.addBlock(this.theme().getPrimary().getWall(), 3);
      mossy.addBlock(BlockType.COBBLESTONE_MOSSY.getBrush(), 1);
      BlockWeightedRandom floor = new BlockWeightedRandom();
      floor.addBlock(this.theme().getPrimary().getFloor(), 1);
      mossy.addBlock(BlockType.COBBLESTONE_MOSSY.getBrush(), 1);
      floor.addBlock(BlockType.GRAVEL.getBrush(), 3);
      BlockWeightedRandom subfloor = new BlockWeightedRandom();
      subfloor.addBlock(floor, 3);
      subfloor.addBlock(TNT_META_BLOCK, 1);
      Coord start = at.copy().translate(-4, -4, -4);
      Coord end = at.copy().translate(4, 5, 4);
      RectHollow.newRect(start, end).fill(this.worldEditor, mossy, false, true);
      start = at.copy().translate(-3, -1, -3);
      end = at.copy().translate(3, -1, 3);
      floor.fill(this.worldEditor, RectSolid.newRect(start, end));
      start = at.copy().translate(-3, -3, -3);
      end = at.copy().translate(3, -2, 3);
      subfloor.fill(this.worldEditor, RectSolid.newRect(start, end));
      start = at.copy().translate(-3, 0, -3);
      end = at.copy().translate(3, 0, 3);
      List<Coord> chestSpaces = RectSolid.newRect(start, end).get();
      Coord.randomFrom(chestSpaces, 3, this.random())
         .forEach(
            chestSpace -> new TreasureChest(chestSpace, this.worldEditor)
                  .withChestType(this.getChestTypeOrUse(ChestType.ORE))
                  .withFacing(this.getEntrance(entrances).reverse())
                  .withTrap(true)
                  .stroke(this.worldEditor, chestSpace)
         );
      Coord cursor = at.copy();
      this.generateSpawner(cursor, new MobType[]{MobType.CREEPER});
      return this;
   }
}
