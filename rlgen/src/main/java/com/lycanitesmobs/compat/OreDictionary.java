package com.lycanitesmobs.compat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** 1.12 Forge OreDictionary, as far as Lycanites uses it (its own registerOre calls and getOres for diets/lists). */
public final class OreDictionary {
   public static final int WILDCARD_VALUE = Short.MAX_VALUE;
   private static final Map<String, List<ItemStack>> ORES = new HashMap<>();

   private OreDictionary() {
   }

   public static void registerOre(String name, ItemLike item) {
      if (item != null && item.asItem() != null) {
         registerOre(name, new ItemStack(item));
      }
   }

   public static void registerOre(String name, ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         ORES.computeIfAbsent(name, n -> new ArrayList<>()).add(stack);
      }
   }

   public static List<ItemStack> getOres(String name) {
      return ORES.getOrDefault(name, new ArrayList<>());
   }

   public static List<ItemStack> getOres(String name, boolean alwaysCreate) {
      return getOres(name);
   }

   public static boolean doesOreNameExist(String name) {
      return ORES.containsKey(name);
   }

   public static boolean has(String name, Item item) {
      for (ItemStack stack : getOres(name)) {
         if (stack.getItem() == item) {
            return true;
         }
      }
      return false;
   }
}
