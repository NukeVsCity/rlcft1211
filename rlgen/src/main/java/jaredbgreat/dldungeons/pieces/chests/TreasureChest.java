package jaredbgreat.dldungeons.pieces.chests;

import jaredbgreat.dldungeons.builder.AbstractBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import net.minecraft.world.item.ItemStack;
import jaredbgreat.dldungeons.compat.ChestContents;
import net.minecraft.core.BlockPos;
import jaredbgreat.dldungeons.compat.World;

public class TreasureChest extends BasicChest {
   private static volatile int A2 = 3;
   private static volatile int B2 = 2;
   private static volatile int C2 = 2;
   private static volatile boolean NERF;
   private boolean withBoss;
   static ArrayList<Integer> slots = new ArrayList<>();
   int slot;

   public TreasureChest(int x, int y, int z, int level, LootCategory category) {
      super(x, y, z, level, category);
   }

   @Override
   public void place(World world, int x, int y, int z, Random random) {
      BlockPos pos = new BlockPos(x, y, z);
      Collections.shuffle(slots, random);
      this.slot = 0;
      this.level = this.level + random.nextInt(2);
      if (NERF && !this.withBoss && this.level > 6) {
         this.level = Math.max(6, this.level - 2);
      }

      if (this.level >= 7) {
         this.level = 6;
      }

      if (world.getBlockState(pos).getBlock() != AbstractBlock.chest) {
         System.err.println("[DLDUNGEONS] ERROR! Trying to put loot into non-chest at " + x + ", " + y + ", " + z + " (treasure chest).");
      } else {
         ChestContents contents = new ChestContents(world, pos);
         int num = random.nextInt(Math.max(2, A2 + this.level / B2)) + C2;

         for (int i = 0; i < num; i++) {
            ItemStack treasure = this.category.getLoot(LootType.HEAL, this.level, random).getLoot();
            contents.setInventorySlotContents(slots.get(this.slot), treasure);
            this.slot++;
         }

         num = random.nextInt(Math.max(2, A2 + this.level / B2)) + C2;

         for (int i = 0; i < num; i++) {
            ItemStack treasure = this.category.getLoot(LootType.GEAR, this.level, random).getLoot();
            contents.setInventorySlotContents(slots.get(this.slot), treasure);
            this.slot++;
         }

         num = random.nextInt(Math.max(2, A2 + this.level / B2)) + C2;

         for (int i = 0; i < num; i++) {
            LootResult lootResult = this.category.getLoot(LootType.LOOT, this.level + 1 + random.nextInt(2), random);
            ItemStack treasure = lootResult.getLoot();
            contents.setInventorySlotContents(slots.get(this.slot), treasure);
            if (NERF && lootResult.getLevel() > 6 && !this.withBoss) {
               this.level--;
            }

            this.slot++;
         }

         if (random.nextInt(7) < this.level) {
            if (this.level >= 6) {
               ItemStack treasure = this.category.getLists().special.getLoot(random).getStack(random);
               contents.setInventorySlotContents(slots.get(this.slot), treasure);
               this.slot++;
               if (random.nextBoolean()) {
                  treasure = this.category.getLists().discs.getLoot(random).getStack(random);
                  contents.setInventorySlotContents(slots.get(this.slot), treasure);
                  this.slot++;
               }
            } else {
               ItemStack treasure = this.category.getLists().discs.getLoot(random).getStack(random);
               contents.setInventorySlotContents(slots.get(this.slot), treasure);
               this.slot++;
            }
         }
      }
   }

   public TreasureChest setWithBoss(boolean bossRoom) {
      this.withBoss = bossRoom;
      return this;
   }

   private boolean validSlot(int slot) {
      return slot >= 0 && slot < 27;
   }

   public static void initSlots() {
      for (int i = 0; i < 27; i++) {
         slots.add(new Integer(i));
      }
   }

   public static void setBasicLootNumbers(int a, int b, int c, boolean nerf) {
      setBasicLootNumbers(a, b, c);
      NERF = nerf;
   }
}
