package jaredbgreat.dldungeons.pieces.chests;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;

public class LootList extends ArrayList<LootItem> {
   ArrayList<LootItem> dummy = new ArrayList<>();

   public void add(Item item, int min, int max, int prob) {
      this.add(new LootItem(item, min, max));
   }

   public void add(Block item, int min, int max, int prob) {
      this.add(new LootItem(item, min, max));
   }

   public LootItem getLoot(Random random) {
      if (this.isEmpty()) {
         return null;
      } else {
         if (this.dummy.isEmpty() || random.nextInt(this.size()) > this.dummy.size()) {
            this.dummy.clear();
            this.dummy.addAll(this);
         }

         int which = random.nextInt(this.dummy.size());
         LootItem out = this.dummy.get(which);
         this.dummy.remove(which);
         return out;
      }
   }
}
