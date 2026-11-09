package com.github.alexthe666.iceandfire.enums;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.item.ItemBestiary;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** 1.12 EnumBestiaryPages: the pages are stored in the bestiary's NBT int array "Pages" (custom_data). */
public enum EnumBestiaryPages {
   INTRODUCTION(2),
   FIREDRAGON(4),
   FIREDRAGONEGG(1),
   ICEDRAGON(4),
   ICEDRAGONEGG(1),
   TAMEDDRAGONS(3),
   MATERIALS(2),
   ALCHEMY(0),
   VILLAGERS(0),
   HIPPOGRYPH(1),
   GORGON(1),
   PIXIE(1),
   CYCLOPS(2),
   SIREN(2),
   HIPPOCAMPUS(2),
   DEATHWORM(3),
   COCKATRICE(2),
   STYMPHALIANBIRD(1),
   TROLL(2),
   MYRMEX(4),
   AMPHITHERE(2),
   SEASERPENT(2);

   public int pages;

   private EnumBestiaryPages(int pages) {
      this.pages = pages;
   }

   public static List<Integer> toList(int[] containedpages) {
      List<Integer> intList = new ArrayList<>();
      for (int containedpage : containedpages) {
         intList.add(containedpage);
      }
      return intList;
   }

   public static int[] fromList(List<Integer> containedpages) {
      int[] pages = new int[containedpages.size()];
      for (int i = 0; i < pages.length; i++) {
         pages[i] = containedpages.get(i);
      }
      return pages;
   }

   public static List<EnumBestiaryPages> containedPages(List<Integer> pages) {
      Iterator<Integer> itr = pages.iterator();
      List<EnumBestiaryPages> list = new ArrayList<>();
      while (itr.hasNext()) {
         list.add(values()[itr.next()]);
      }
      return list;
   }

   private static int[] pagesOf(ItemStack book) {
      CompoundTag tag = IafMeta.getTag(book);
      return tag == null ? new int[0] : tag.getIntArray("Pages");
   }

   /** 1.12 hasAllPages (quirk kept: only looks at the first page, INTRODUCTION, and returns true when it is missing). */
   public static boolean hasAllPages(ItemStack book) {
      List<EnumBestiaryPages> pages = containedPages(toList(pagesOf(book)));
      return !pages.contains(values()[0]);
   }

   public static List<Integer> enumToInt(List<EnumBestiaryPages> pages) {
      Iterator<EnumBestiaryPages> itr = pages.iterator();
      List<Integer> list = new ArrayList<>();
      while (itr.hasNext()) {
         list.add(values()[itr.next().ordinal()].ordinal());
      }
      return list;
   }

   public static EnumBestiaryPages getRand() {
      return values()[new Random().nextInt(values().length)];
   }

   public static void addRandomPage(ItemStack book) {
      if (book.getItem() instanceof ItemBestiary) {
         List<EnumBestiaryPages> list = possiblePages(book);
         if (list != null && !list.isEmpty()) {
            addPage(list.get(new Random().nextInt(list.size())), book);
         }
      }
   }

   public static List<EnumBestiaryPages> possiblePages(ItemStack book) {
      if (!(book.getItem() instanceof ItemBestiary)) {
         return null;
      }
      List<EnumBestiaryPages> containedPages = containedPages(toList(pagesOf(book)));
      List<EnumBestiaryPages> possiblePages = new ArrayList<>();
      for (EnumBestiaryPages page : values()) {
         if (!containedPages.contains(page)) {
            possiblePages.add(page);
         }
      }
      return possiblePages;
   }

   public static void addPage(EnumBestiaryPages page, ItemStack book) {
      if (book.getItem() instanceof ItemBestiary) {
         List<EnumBestiaryPages> enumlist = containedPages(toList(pagesOf(book)));
         if (!enumlist.contains(page)) {
            enumlist.add(page);
         }
         int[] arr = fromList(enumToInt(enumlist));
         IafMeta.editTag(book, tag -> tag.putIntArray("Pages", arr));
      }
   }
}
