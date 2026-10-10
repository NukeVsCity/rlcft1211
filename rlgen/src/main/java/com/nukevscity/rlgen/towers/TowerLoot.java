package com.nukevscity.rlgen.towers;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

/**
 * Battle Towers TowerStageItemManager, fed with RLCraft 2.9.3's battletowers.cfg. Items from mods that are not here are skipped,
 * exactly as Battle Towers skipped entries it could not find.
 */
final class TowerLoot {
   private static final Logger LOGGER = LogUtils.getLogger();
   /** 1.12 names (and damage) whose 1.21 name differs. */
   private static final Map<String, Item> RENAMED = new HashMap<>();

   static {
      RENAMED.put("planks", Items.OAK_PLANKS);
      RENAMED.put("wool", Items.WHITE_WOOL);
      RENAMED.put("reeds", Items.SUGAR_CANE);
      RENAMED.put("brick_block", Items.BRICKS);
      RENAMED.put("sapling", Items.OAK_SAPLING);
      RENAMED.put("sign", Items.OAK_SIGN);
      RENAMED.put("fish", Items.COD);
      RENAMED.put("dye", Items.INK_SAC);
      RENAMED.put("map", Items.MAP);
   }

   private record Entry(Item item, String lootTable, int chance, int min, int max) {
   }

   private final List<Entry> entries = new ArrayList<>();

   private TowerLoot(String config) {
      for (String element : config.split(";")) {
         String[] settings = element.trim().split("-");
         if (settings[0].startsWith("ChestGenHook:")) {
            this.entries.add(new Entry(null, settings[0], 100, 0, 0));
         } else if (settings.length >= 5) {
            Item item = item(settings[0], Integer.parseInt(settings[1]));
            if (item != null) {
               this.entries.add(new Entry(item, null, Integer.parseInt(settings[2]), Integer.parseInt(settings[3]), Integer.parseInt(settings[4])));
            }
         }
      }
   }

   private static Item item(String name, int damage) {
      String path = name.startsWith("minecraft:") ? name.substring(10) : name;
      if (path.contains(":")) {
         return null; // another mod
      }
      Item renamed = RENAMED.get(path);
      if (renamed != null) {
         return renamed;
      }
      ResourceLocation id = ResourceLocation.withDefaultNamespace(path);
      return BuiltInRegistries.ITEM.containsKey(id) ? BuiltInRegistries.ITEM.get(id) : null;
   }

   private static volatile TowerLoot[] floors;
   static int itemGenerationsPerFloor = 32;

   /** AS_BattleTowersCore.floorItemManagers: Floor 1..9 and Top Floor. */
   static TowerLoot forFloor(int floor) {
      TowerLoot[] f = floors;
      if (f == null) {
         f = load();
         floors = f;
      }
      if (--floor >= f.length) {
         floor = f.length - 1;
      }
      return f[Math.max(floor, 0)];
   }

   private static TowerLoot[] load() {
      Map<String, String> values = new HashMap<>();
      try (InputStream in = TowerLoot.class.getResourceAsStream("/data/rlgen/battletowers/battletowers.cfg")) {
         BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
         String line;
         while ((line = r.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("S:") && line.contains("=")) {
               String key = line.substring(2, line.indexOf('=')).replace("\"", "");
               values.put(key, line.substring(line.indexOf('=') + 1));
            }
         }
      } catch (Exception e) {
         LOGGER.error("rlgen: could not read battletowers.cfg", e);
      }
      try {
         itemGenerationsPerFloor = Integer.parseInt(values.getOrDefault("Item Generations per Floor", "32").trim());
      } catch (NumberFormatException ignored) {
      }
      TowerLoot[] out = new TowerLoot[10];
      for (int i = 0; i < 9; i++) {
         out[i] = new TowerLoot(values.getOrDefault("Floor " + (i + 1), ""));
      }
      out[9] = new TowerLoot(values.getOrDefault("Top Floor", ""));
      return out;
   }

   /** getStageItemStacks */
   List<ItemStack> stacks(ServerLevel level, Random rand, int count) {
      List<ItemStack> result = new ArrayList<>();
      for (int index = 0; result.size() < count && index < this.entries.size(); index++) {
         Entry e = this.entries.get(index);
         if (e.item != null) {
            if (rand.nextInt(100) < e.chance) {
               result.add(new ItemStack(e.item, e.min + rand.nextInt(e.max)));
            }
         } else {
            String[] split = e.lootTable.split(":");
            List<ItemStack> generated = new ArrayList<>();
            long lootSeed = rand.nextLong();
            if (level != null) {
               LootTable table = level.getServer().reloadableRegistries()
                  .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace(split[1])));
               generated.addAll(table.getRandomItems(new LootParams.Builder(level).create(LootContextParamSets.EMPTY), lootSeed));
            }
            if (split.length > 2) {
               int number = Integer.parseInt(split[2]);
               if (number > 0) {
                  Random shuffle = new Random(lootSeed);
                  while (generated.size() > number) {
                     Collections.shuffle(generated, shuffle);
                     generated.remove(generated.size() - 1);
                  }
                  result.addAll(generated);
               }
            }
         }
      }
      return result;
   }
}
