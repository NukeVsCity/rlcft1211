package com.github.alexthe666.iceandfire.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.Blocks;

/**
 * The myrmex careers of 1.12 ModVillagers (one career per caste and biome, trades by career level) and
 * EntityMyrmexBase.BasicTrade. Items of the other parts are looked up by their 1.12 names; a 1.12 metadata value (egg caste,
 * dye/fish/leaf/skull variants) is mapped to the 1.21 item, and for the myrmex eggs kept as custom data "MyrmexCaste".
 */
public final class MyrmexTrades {
   private MyrmexTrades() {
   }

   /** 1.12 PriceInfo */
   public record PriceInfo(int min, int max) {
      public int getPrice(RandomSource rand) {
         return this.min >= this.max ? this.min : this.min + rand.nextInt(this.max - this.min + 1);
      }
   }

   /** EntityMyrmexBase.BasicTrade */
   public record BasicTrade(Supplier<ItemStack> first, Supplier<ItemStack> second, PriceInfo firstPrice, PriceInfo secondPrice) {
      public void addMerchantRecipe(MerchantOffers recipeList, RandomSource random) {
         int i = this.firstPrice.getPrice(random);
         int j = this.secondPrice.getPrice(random);
         ItemStack buy = this.first.get().copyWithCount(i);
         ItemStack sell = this.second.get().copyWithCount(j);
         if (buy.isEmpty() || sell.isEmpty()) {
            return;
         }
         // 1.12 MerchantRecipe(buy, sell): 7 uses, rewards experience
         recipeList.add(new MerchantOffer(new ItemCost(buy.getItem(), i), sell, 7, 0, 0.0F));
      }
   }

   private static Supplier<ItemStack> of(Item item) {
      return () -> new ItemStack(item);
   }

   private static Supplier<ItemStack> iaf(String name) {
      return () -> new ItemStack(IafLand.item(name));
   }

   /** ItemMyrmexEgg with metadata = caste */
   public static ItemStack egg(boolean jungle, int caste) {
      ItemStack stack = new ItemStack(IafLand.item(jungle ? "myrmex_jungle_egg" : "myrmex_desert_egg"));
      if (!stack.isEmpty()) {
         CompoundTag tag = new CompoundTag();
         tag.putInt("MyrmexCaste", caste);
         CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
      }
      return stack;
   }

   /** The metadata (caste) of a myrmex egg stack. */
   public static int eggCaste(ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? 0 : data.copyTag().getInt("MyrmexCaste");
   }

   public static boolean isMyrmexEgg(ItemStack stack) {
      return !stack.isEmpty() && (IafLand.isItem(stack.getItem(), "myrmex_desert_egg") || IafLand.isItem(stack.getItem(), "myrmex_jungle_egg"));
   }

   private static PriceInfo p(int a, int b) {
      return new PriceInfo(a, b);
   }

   /** careers.get(caste)[level - 1] = trades; caste 0 worker, 1 soldier, 2 sentinel, 3 royal, 4 queen */
   private static List<List<List<BasicTrade>>> build(boolean jungle) {
      String r = jungle ? "myrmex_jungle_resin" : "myrmex_desert_resin";
      Supplier<ItemStack> resin = iaf(r);
      List<List<List<BasicTrade>>> careers = new ArrayList<>();
      // worker
      Career w = new Career();
      w.add(1, new BasicTrade(of(Items.DIRT), resin, p(32, 64), p(1, 2)));
      if (jungle) {
         w.add(1, new BasicTrade(of(Items.MELON), resin, p(10, 20), p(1, 2)));
         w.add(2, new BasicTrade(of(Blocks.JUNGLE_LEAVES.asItem()), resin, p(48, 64), p(1, 1)));
         w.add(3, new BasicTrade(of(Items.GOLD_ORE), resin, p(7, 10), p(1, 4)));
      } else {
         w.add(1, new BasicTrade(of(Items.SAND), resin, p(32, 64), p(1, 2)));
         w.add(2, new BasicTrade(of(Items.DEAD_BUSH), resin, p(5, 10), p(1, 2)));
         w.add(3, new BasicTrade(of(Items.IRON_ORE), resin, p(10, 15), p(1, 4)));
      }
      w.add(4, new BasicTrade(resin, of(Items.BONE), p(1, 5), p(5, 15)));
      w.add(4, new BasicTrade(of(Items.SUGAR), resin, p(5, 8), p(1, 2)));
      careers.add(w.levels);
      // soldier
      Career s = new Career();
      s.add(1, new BasicTrade(resin, of(Items.BONE), p(1, 5), p(1, 15)));
      s.add(1, new BasicTrade(resin, of(Items.FEATHER), p(1, 3), p(1, 3)));
      s.add(1, new BasicTrade(resin, of(Items.STRING), p(1, 3), p(1, 3)));
      s.add(2, new BasicTrade(resin, of(Items.GUNPOWDER), p(5, 7), p(1, 3)));
      s.add(2, new BasicTrade(resin, of(Items.RABBIT), p(3, 6), p(1, 1)));
      // dye 2 = cactus green, dye 3 = cocoa beans
      s.add(2, new BasicTrade(resin, of(jungle ? Items.COCOA_BEANS : Items.GREEN_DYE), p(1, 3), p(1, 3)));
      s.add(3, new BasicTrade(resin, of(Items.IRON_NUGGET), p(1, 3), p(1, 3)));
      s.add(3, new BasicTrade(resin, of(Items.CHICKEN), p(3, 6), p(1, 1)));
      s.add(4, new BasicTrade(resin, of(Items.GOLD_NUGGET), p(1, 3), p(1, 3)));
      s.add(4, new BasicTrade(resin, iaf("silver_nugget"), p(1, 3), p(1, 3)));
      s.add(5, new BasicTrade(iaf("troll_tusk"), resin, p(1, 1), p(5, 15)));
      careers.add(s.levels);
      // sentinel
      Career se = new Career();
      se.add(1, new BasicTrade(of(Items.SPIDER_EYE), resin, p(1, 1), p(1, 2)));
      se.add(1, new BasicTrade(of(Items.REDSTONE), resin, p(3, 4), p(1, 4)));
      se.add(2, new BasicTrade(resin, of(Items.EGG), p(3, 5), p(1, 1)));
      se.add(2, new BasicTrade(resin, of(Items.PORKCHOP), p(3, 7), p(1, 1)));
      se.add(2, new BasicTrade(resin, of(Items.BEEF), p(3, 7), p(1, 1)));
      se.add(2, new BasicTrade(resin, of(Items.MUTTON), p(3, 7), p(1, 1)));
      se.add(3, new BasicTrade(of(Items.POISONOUS_POTATO), resin, p(1, 1), p(10, 20)));
      // fish 3 = pufferfish
      se.add(4, new BasicTrade(of(Items.PUFFERFISH), resin, p(1, 1), p(5, 10)));
      // skull 0 = skeleton skull
      se.add(5, new BasicTrade(resin, of(Items.SKELETON_SKULL), p(20, 50), p(1, 1)));
      careers.add(se.levels);
      // royal
      Career ro = new Career();
      ro.add(1, new BasicTrade(iaf("manuscript"), resin, p(1, 1), p(4, 6)));
      ro.add(2, new BasicTrade(of(Items.GOLD_INGOT), resin, p(1, 4), p(5, 8)));
      ro.add(2, new BasicTrade(iaf("silver_ingot"), resin, p(1, 4), p(5, 8)));
      ro.add(3, new BasicTrade(resin, of(Items.RABBIT_FOOT), p(5, 10), p(1, 1)));
      ro.add(4, new BasicTrade(resin, of(Items.ENDER_PEARL), p(10, 15), p(1, 1)));
      ro.add(4, new BasicTrade(resin, iaf("wither_shard"), p(5, 8), p(1, 1)));
      ro.add(5, new BasicTrade(resin, of(Items.MAGMA_CREAM), p(6, 15), p(1, 1)));
      ro.add(5, new BasicTrade(resin, of(Items.QUARTZ), p(7, 15), p(1, 1)));
      ro.add(6, new BasicTrade(resin, of(Items.GOLDEN_CARROT), p(15, 20), p(1, 1)));
      ro.add(7, new BasicTrade(resin, of(Items.EMERALD), p(20, 30), p(1, 1)));
      careers.add(ro.levels);
      careers.add(queen(jungle, resin));
      return careers;
   }

   private static List<List<BasicTrade>> queen(boolean jungle, Supplier<ItemStack> resin) {
      int[][] prices = {{1, 10}, {10, 20}, {20, 30}, {30, 40}, {50, 64}};
      Career q = new Career();
      for (int caste = 0; caste < 5; caste++) {
         final int c = caste;
         q.add(caste + 1, new BasicTrade(resin, () -> egg(jungle, c), p(prices[caste][0], prices[caste][1]), p(1, 1)));
      }
      return q.levels;
   }

   private static final class Career {
      final List<List<BasicTrade>> levels = new ArrayList<>();

      void add(int level, BasicTrade trade) {
         while (this.levels.size() < level) {
            this.levels.add(new ArrayList<>());
         }
         this.levels.get(level - 1).add(trade);
      }
   }

   private static List<List<List<BasicTrade>>> desert;
   private static List<List<List<BasicTrade>>> jungle;

   /** VillagerCareer.getTrades(level): null past the last level. */
   public static List<BasicTrade> getTrades(int caste, boolean isJungle, int level) {
      if (desert == null) {
         desert = build(false);
         jungle = build(true);
      }
      List<List<BasicTrade>> career = (isJungle ? jungle : desert).get(caste);
      return level >= 0 && level < career.size() ? career.get(level) : null;
   }
}
