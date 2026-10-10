package com.nukevscity.rlgen.rc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/** RC inventory generation: the loot tags in chests ("multi" fills empty slots, "single" fills its own slot). */
public final class RcLoot {
   private static final Logger LOGGER = LogUtils.getLogger();

   private RcLoot() {
   }

   record Item(JsonObject json, double weight, int min, int max) {
   }

   /** A GenericLootTable (all .rcig components with one inventoryGeneratorID), or a vanilla loot table. */
   public static final class Table {
      final List<List<Item>> components = new ArrayList<>();
      ResourceKey<LootTable> vanilla;

      static Table load(String key) {
         Table t = new Table();
         if (key.startsWith("minecraft:chests/")) {
            t.vanilla = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(key));
            return t;
         }
         JsonElement e = RcRegistry.readJson("loot/" + key.toLowerCase(java.util.Locale.ROOT) + ".json");
         if (e == null) {
            return null; // 1.12 removed the tag and generated nothing for unknown generators
         }
         for (JsonElement c : e.getAsJsonObject().getAsJsonArray("components")) {
            List<Item> items = new ArrayList<>();
            for (JsonElement ie : c.getAsJsonObject().getAsJsonArray("items")) {
               JsonObject i = ie.getAsJsonObject();
               items.add(new Item(i, RcTransformers.dbl(i, "weight", 1.0), (int)RcTransformers.dbl(i, "min", 1), (int)RcTransformers.dbl(i, "max", 1)));
            }
            t.components.add(items);
         }
         return t;
      }

      ItemStack random(Random random, WorldGenLevel level, BlockPos pos) {
         if (this.vanilla != null) {
            ServerLevel server = level.getLevel();
            LootTable table = server.getServer().reloadableRegistries().getLootTable(this.vanilla);
            LootParams params = new LootParams.Builder(server).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).create(LootContextParamSets.CHEST);
            ObjectArrayList<ItemStack> items = table.getRandomItems(params, random.nextLong());
            return items.isEmpty() ? ItemStack.EMPTY : items.get(random.nextInt(items.size()));
         }
         int max = 0;
         for (List<Item> c : this.components) {
            max += c.size();
         }
         if (max <= 0) {
            return ItemStack.EMPTY;
         }
         int i = random.nextInt(max);
         List<Item> component = null;
         for (List<Item> c : this.components) {
            i -= c.size();
            if (i < 0) {
               component = c;
               break;
            }
         }
         if (component == null || component.isEmpty()) {
            return null;
         }
         Item item = RcRegistry.selectWeighted(random, component, Item::weight);
         ItemStack stack = stack(item.json, level);
         stack.setCount(random.nextInt(Math.max(1, item.max - item.min + 1)) + item.min);
         return stack;
      }
   }

   static ItemStack stack(JsonObject json, WorldGenLevel level) {
      JsonObject o = new JsonObject();
      o.addProperty("id", json.get("id").getAsString());
      o.addProperty("count", 1);
      if (json.has("components")) {
         o.add("components", json.get("components"));
      }
      return ItemStack.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, level.registryAccess()), o)
         .resultOrPartial(err -> LOGGER.debug("rlgen: bad loot item {}: {}", json, err))
         .orElse(ItemStack.EMPTY);
   }

   /** LootGenerationHandler.generateAllTags for the tags the converter took out of the chest. */
   public static void generate(Container inv, List<RcStructureDef.LootTag> tags, Random random, WorldGenLevel level, BlockPos pos) {
      List<RcStructureDef.LootTag> sorted = new ArrayList<>(tags);
      sorted.sort(Comparator.comparingInt(RcStructureDef.LootTag::slot));
      RcRegistry reg = RcRegistry.get();
      for (RcStructureDef.LootTag tag : sorted) {
         Table table = reg.loot(tag.key());
         if (tag.slot() < inv.getContainerSize()) {
            inv.setItem(tag.slot(), ItemStack.EMPTY);
         }
         if (table == null) {
            continue;
         }
         if (tag.multi()) {
            if (tag.min() <= tag.max()) {
               int amount = random.nextInt(tag.max() - tag.min() + 1) + tag.min();
               List<Integer> empty = new ArrayList<>();
               for (int s = 0; s < inv.getContainerSize(); s++) {
                  if (inv.getItem(s).isEmpty()) {
                     empty.add(s);
                  }
               }
               for (int i = 0; i < amount; i++) {
                  int slot = empty.isEmpty() ? random.nextInt(inv.getContainerSize()) : empty.remove(random.nextInt(empty.size()));
                  ItemStack generated = table.random(random, level, pos);
                  if (generated != null) {
                     inv.setItem(slot, generated);
                  }
               }
            }
         } else {
            ItemStack generated = random.nextFloat() < tag.chance() ? table.random(random, level, pos) : null;
            if (generated != null && tag.slot() < inv.getContainerSize()) {
               inv.setItem(tag.slot(), generated);
            }
         }
      }
   }
}
