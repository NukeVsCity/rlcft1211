package jaredbgreat.dldungeons.pieces.chests;

import jaredbgreat.dldungeons.ConfigHandler;
import jaredbgreat.dldungeons.builder.AbstractBlock;
import java.util.Random;
import net.minecraft.world.item.ItemStack;
import jaredbgreat.dldungeons.compat.ChestContents;
import net.minecraft.core.BlockPos;
import jaredbgreat.dldungeons.compat.World;

public class BasicChest {
   public final int mx;
   public final int my;
   public final int mz;
   protected int level;
   private static volatile int A1 = 2;
   private static volatile int B1 = 1;
   private static volatile int C1 = 2;
   protected final LootCategory category;

   public BasicChest(int x, int y, int z, int level, LootCategory category) {
      this.mx = x;
      this.my = y;
      this.mz = z;
      this.level = level;
      this.category = category;
   }

   public void place(World world, int x, int y, int z, Random random) {
      BlockPos pos = new BlockPos(x, y, z);
      this.level = this.level + random.nextInt(2);
      this.level = Math.min(6, Math.min(this.level, 6));
      if (world.getBlockState(pos).getBlock() != AbstractBlock.chest) {
         System.err.println("[DLDUNGEONS] ERROR! Trying to put loot into non-chest at " + x + ", " + y + ", " + z + " (basic chest).");
      } else {
         ChestContents contents = new ChestContents(world, pos);
         int which = random.nextInt(3);
         switch (which) {
            case 0:
               this.fillChest(contents, LootType.HEAL, random);
               break;
            case 1:
               this.fillChest(contents, LootType.GEAR, random);
               break;
            case 2:
               this.fillChest(contents, LootType.RANDOM, random);
         }

      }
   }

   protected void fillChest(ChestContents chest, LootType kind, Random random) {
      int num = random.nextInt(Math.max(2, A1 + this.level / B1)) + C1;

      for (int i = 0; i < num; i++) {
         ItemStack treasure = this.category.getLoot(kind, this.level, random).getLoot();
         if (treasure != null) {
            chest.setInventorySlotContents(random.nextInt(27), treasure);
         }
      }

      if (!ConfigHandler.vanillaLoot) {
         ItemStack treasure = this.category.getLoot(LootType.LOOT, Math.min(6, this.level), random).getLoot();
         if (treasure != null) {
            chest.setInventorySlotContents(random.nextInt(27), treasure);
         }
      }
   }

   public static void setBasicLootNumbers(int a, int b, int c) {
      A1 = a;
      B1 = b;
      C1 = c;
   }
}
