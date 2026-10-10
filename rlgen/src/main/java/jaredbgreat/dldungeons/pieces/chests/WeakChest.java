package jaredbgreat.dldungeons.pieces.chests;

import jaredbgreat.dldungeons.builder.AbstractBlock;
import java.util.Random;
import jaredbgreat.dldungeons.compat.ChestContents;
import net.minecraft.core.BlockPos;
import jaredbgreat.dldungeons.compat.World;

public class WeakChest extends BasicChest {
   public WeakChest(int x, int y, int z, LootCategory category) {
      super(x, y, z, 0, category);
   }

   @Override
   public void place(World world, int x, int y, int z, Random random) {
      BlockPos pos = new BlockPos(x, y, z);
      ChestContents contents = new ChestContents(world, pos);
      if (world.getBlockState(pos).getBlock() != AbstractBlock.chest) {
         System.err.println("[DLDUNGEONS] ERROR! Trying to put loot into non-chest at " + x + ", " + y + ", " + z + " (basic chest).");
      } else {
         if (random.nextBoolean()) {
            if (random.nextBoolean()) {
               this.fillChest(contents, LootType.GEAR, random);
            } else {
               this.fillChest(contents, LootType.HEAL, random);
            }
         } else {
            this.fillChest(contents, LootType.GEAR, random);
            this.fillChest(contents, LootType.HEAL, random);
         }
      }
   }
}
