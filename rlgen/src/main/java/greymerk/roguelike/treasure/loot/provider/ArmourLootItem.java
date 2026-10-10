package greymerk.roguelike.treasure.loot.provider;

import com.github.fnar.minecraft.item.ArmourType;
import com.github.fnar.minecraft.item.RldItemStack;
import com.github.fnar.roguelike.loot.special.armour.SpecialArmour;
import com.github.fnar.util.Color;
import greymerk.roguelike.treasure.loot.Equipment;
import greymerk.roguelike.treasure.loot.Quality;
import java.util.Optional;
import java.util.Random;

public class ArmourLootItem extends LootItem {
   private Equipment equipment;
   private Quality quality;
   private boolean enchant;

   public ArmourLootItem(int weight, int level) {
      super(weight, level);
   }

   public ArmourLootItem(int weight, int level, Equipment equipment, Quality quality, boolean enchant) {
      super(weight, level);
      this.equipment = equipment;
      this.quality = quality;
      this.enchant = enchant;
   }

   @Override
   public RldItemStack getLootItem(Random random) {
      return SpecialtyLootItem.rollForSpecial(random)
         ? SpecialArmour.createArmour(random, this.getArmourType(random), this.getQuality(random)).asStack()
         : this.getArmourType(random)
            .asItem()
            .withQuality(this.getQuality(random))
            .withColor(Color.random(random))
            .plzEnchantAtLevel(this.enchant ? getEnchantmentLevel(random, this.level) : 0)
            .asStack();
   }

   private Quality getQuality(Random random) {
      return Optional.ofNullable(this.quality).orElseGet(() -> Equipment.rollQuality(random, this.level));
   }

   private ArmourType getArmourType(Random random) {
      return Optional.ofNullable(this.equipment).map(Equipment::asArmourType).orElseGet(() -> ArmourType.random(random));
   }
}
