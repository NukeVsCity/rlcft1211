package greymerk.roguelike.treasure.loot.provider;

import com.github.fnar.minecraft.item.RldItemStack;
import com.github.fnar.roguelike.loot.special.weapons.SpecialSword;
import greymerk.roguelike.treasure.loot.Quality;
import java.util.Random;

public class ItemSmithy extends LootItem {
   public ItemSmithy(int weight, int level) {
      super(weight, level);
   }

   @Override
   public RldItemStack getLootItem(Random random) {
      return SpecialSword.newSpecialSword(random, Quality.IRON).asStack();
   }
}
