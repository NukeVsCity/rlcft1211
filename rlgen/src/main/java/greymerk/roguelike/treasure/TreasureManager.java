package greymerk.roguelike.treasure;

import com.github.fnar.minecraft.item.RldItemStack;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.util.IWeighted;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreasureManager {
   Set<TreasureChest> chests = new HashSet<>();
   private final Random random;

   public TreasureManager(Random random) {
      this.random = random;
   }

   public void addChest(TreasureChest toAdd) {
      this.chests.add(toAdd);
   }

   public void addItemToAll(Predicate<TreasureChest> chestPredicate, IWeighted<RldItemStack> items, int amount) {
      this.findChests(chestPredicate).forEach(chest -> IntStream.range(0, amount).mapToObj(i -> items.get(this.random)).forEach(chest::setRandomEmptySlot));
   }

   public void addItem(Predicate<TreasureChest> chestPredicate, IWeighted<RldItemStack> items, int amount) {
      List<TreasureChest> chests = this.findChests(chestPredicate);
      if (!chests.isEmpty()) {
         IntStream.range(0, amount).mapToObj(i -> items.get(this.random)).forEach(item -> this.addItemToRandomChest(chests, item));
      }
   }

   private void addItemToRandomChest(List<TreasureChest> chests, RldItemStack item) {
      chests.get(this.random.nextInt(chests.size())).setRandomEmptySlot(item);
   }

   public List<TreasureChest> findChests(Predicate<TreasureChest> predicate) {
      return this.chests.stream().filter(predicate).collect(Collectors.toList());
   }

   public static Predicate<TreasureChest> ofType(ChestType chestType) {
      return chest -> chest.getType().equals(chestType);
   }

   public static Predicate<TreasureChest> ofTypeOnLevel(ChestType chestType, int level) {
      return isOnLevel(level).and(ofType(chestType));
   }

   private static Predicate<TreasureChest> isOnLevel(int level) {
      return chest -> chest.getLevel() == level;
   }

   public static Predicate<TreasureChest> onLevelAndNotEmpty(int level) {
      return ((Predicate<TreasureChest>)TreasureChest::isNotEmpty).and(isOnLevel(level));
   }
}
