package greymerk.roguelike.worldgen;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.Material;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.decorative.Plant;
import com.github.fnar.minecraft.block.decorative.Skull;
import com.github.fnar.minecraft.block.spawner.Spawner;
import com.github.fnar.minecraft.item.RldItemStack;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.TreasureManager;
import greymerk.roguelike.util.DyeColor;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import net.minecraft.world.item.ItemStack;

public interface WorldEditor {
   int FURNACE_FUEL_SLOT = 1;

   void generateSpawner(Spawner var1, Coord var2);

   boolean isSolidBlock(Coord var1);

   boolean isOpaqueBlock(Coord var1);

   boolean isOpaqueCubeBlock(Coord var1);

   boolean isBlockOfTypeAt(BlockType var1, Coord var2);

   boolean isMaterialAt(Material var1, Coord var2);

   Random getRandom();

   Random getRandom(Coord var1);

   boolean setBlock(Coord var1, SingleBlockBrush var2, boolean var3, boolean var4);

   boolean isAirBlock(Coord var1);

   long getSeed();

   void fillDown(Coord var1, BlockBrush var2);

   Object getTileEntity(Coord var1);

   boolean isValidGroundBlock(Coord var1);

   Map<BlockType, Integer> getStats();

   boolean canPlace(SingleBlockBrush var1, Coord var2, Direction var3);

   PositionInfo getInfo(Coord var1);

   Coord findNearestStructure(VanillaStructure var1, Coord var2);

   void setBedColorAt(Coord var1, DyeColor var2);

   void setItem(Coord var1, int var2, ItemStack var3);

   void setItem(Coord var1, int var2, RldItemStack var3);

   void setFlowerPotContent(Coord var1, Plant var2);

   void setSkull(WorldEditor var1, Coord var2, Direction var3, Skull var4);

   void setLootTable(Coord var1, String var2);

   default int getSeed(Coord coord) {
      return Objects.hash(coord.hashCode(), this.getSeed());
   }

   int getCapacity(TreasureChest var1);

   boolean isEmptySlot(TreasureChest var1, int var2);

   TreasureManager getTreasureManager();
}
