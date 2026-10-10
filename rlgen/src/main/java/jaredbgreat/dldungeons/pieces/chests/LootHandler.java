package jaredbgreat.dldungeons.pieces.chests;

import java.util.HashMap;
import java.util.Map;

public class LootHandler {
   private static LootHandler handler;
   private final Map<String, LootCategory> categories = new HashMap<>();

   private LootHandler() {
   }

   public static LootHandler getLootHandler() {
      if (handler == null) {
         handler = new LootHandler();
      }

      return handler;
   }

   public LootCategory createCategory(String name) {
      if (this.categories.containsKey(name)) {
         System.err.println("[DLDUNGEONS] Warning: Trying to create Loot Category" + name + " more than once!");
         return this.categories.get(name);
      } else {
         LootListSet listset = new LootListSet();
         LootCategory category = new LootCategory(listset);
         this.categories.put(name, category);
         return category;
      }
   }

   public LootCategory getCategory(String name) {
      return this.categories.get(name);
   }
}
