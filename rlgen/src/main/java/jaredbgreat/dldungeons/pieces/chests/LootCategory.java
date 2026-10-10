package jaredbgreat.dldungeons.pieces.chests;

import com.github.fnar.minecraft.item.mapper.Mc;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

public class LootCategory {
   public static final int LEVELS = 7;
   private final LootListSet lists;
   public LootList[] gear;
   public LootList[] heal;
   public LootList[] loot;

   public LootCategory(LootListSet listset) {
      this.lists = listset;
      this.gear = new LootList[]{this.lists.gear1, this.lists.gear2, this.lists.gear3, this.lists.gear4, this.lists.gear5, this.lists.gear6, this.lists.gear7};
      this.heal = new LootList[]{this.lists.heal1, this.lists.heal2, this.lists.heal3, this.lists.heal4, this.lists.heal5, this.lists.heal6, this.lists.heal7};
      this.loot = new LootList[]{this.lists.loot1, this.lists.loot2, this.lists.loot3, this.lists.loot4, this.lists.loot5, this.lists.loot6, this.lists.loot7};
   }

   public LootResult getLoot(LootType type, int level, Random random) {
      if (level <= 6) {
         level = Math.min(6, level + random.nextInt(2) - random.nextInt(2));
      }

      if (level < 0) {
         level = 0;
      }

      switch (type) {
         case GEAR: {
            if (random.nextBoolean()) {
               return this.getEnchantedGear(level, random);
            }

            int l = Math.min(6, level);
            return this.enchantedLowerLevel(this.gear[Math.min(6, l)].getLoot(random), l, random);
         }
         case HEAL: {
            int l = Math.min(6, level);
            return new LootResult(this.heal[Math.min(6, l)].getLoot(random).getStack(random), l);
         }
         case LOOT:
            if (level > 6) {
               if (level > random.nextInt(100)) {
                  return new LootResult(this.lists.special.getLoot(random).getStack(random), 7);
               }

               level = 6;
            }

            if (random.nextInt(10) == 0) {
               return this.getEnchantedBook(level, random);
            }

            return new LootResult(this.loot[level].getLoot(random).getStack(random), level);
         case RANDOM:
         default:
            switch (random.nextInt(3)) {
               case 0:
                  return this.getLoot(LootType.GEAR, level, random);
               case 1:
                  return this.getLoot(LootType.HEAL, level, random);
               case 2:
               default:
                  return this.getLoot(LootType.LOOT, level, random);
            }
      }
   }

   private LootResult getEnchantedGear(int lootLevel, Random random) {
      float portion = random.nextFloat() / 2.0F;
      int lootPart = Math.min(6, Math.max(0, (int)((float)lootLevel * (1.0F - portion) + 0.5F)));
      LootItem item = this.gear[lootPart].getLoot(random);
      int diff = lootLevel - item.level + 1;
      int enchPart = Math.min(5 + diff * (diff + 1) / 2 * 5, diff * 10);
      if (enchPart >= 1 && this.isEnchantable(item)) {
         ItemStack out = item.getStack(random);
         out = addRandomEnchantment(random, out, enchPart, random.nextBoolean());
         return new LootResult(out, Math.min(lootLevel, 6));
      } else {
         return this.enchantedLowerLevel(this.gear[Math.min(6, lootLevel)].getLoot(random), lootLevel, random);
      }
   }

   private LootResult enchantedLowerLevel(LootItem item, int level, Random random) {
      int diff = level - item.level;
      ItemStack out;
      if (this.isEnchantable(item) && diff > random.nextInt(2)) {
         int enchPart = Math.min(5 + level * (level + 1) / 2 * 5, level * 10);
         out = item.getStack(random);
         out = addRandomEnchantment(random, out, enchPart, random.nextBoolean());
      } else {
         out = item.getStack(random);
      }

      return new LootResult(out, level);
   }

   private boolean isEnchantable(LootItem in) {
      Item item = in.item;
      return (in.nbtData == null || in.nbtData.isEmpty())
         && (item instanceof SwordItem || item instanceof DiggerItem && !(item instanceof HoeItem) || item instanceof ArmorItem || item instanceof BowItem);
   }

   private LootResult getEnchantedBook(int level, Random random) {
      ItemStack out = new ItemStack(Items.BOOK, 1);
      out = addRandomEnchantment(random, out, Math.min(30, (int)((double)level * 7.5)), true);
      return new LootResult(out, Math.min(level, 6));
   }

   /** EnchantmentHelper.addRandomEnchantment; treasure allowed means every enchantment (curses too), as in 1.12. */
   private static ItemStack addRandomEnchantment(Random random, ItemStack stack, int level, boolean treasure) {
      if (stack == null || Mc.access() == null) {
         return stack;
      }
      return EnchantmentHelper.enchantItem(new LegacyRandomSource(random.nextLong()), stack, level, Mc.access(),
         treasure ? Optional.empty() : Mc.access().registryOrThrow(Registries.ENCHANTMENT).getTag(EnchantmentTags.IN_ENCHANTING_TABLE).map(h -> h));
   }

   public LootListSet getLists() {
      return this.lists;
   }
}
