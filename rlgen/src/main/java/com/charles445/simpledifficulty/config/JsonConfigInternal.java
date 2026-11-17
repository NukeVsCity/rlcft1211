package com.charles445.simpledifficulty.config;

import com.charles445.simpledifficulty.SimpleDifficulty;
import com.charles445.simpledifficulty.api.SDBlocks;
import com.charles445.simpledifficulty.api.SDItems;
import com.charles445.simpledifficulty.api.config.JsonConfig;
import com.charles445.simpledifficulty.api.config.JsonConfig.JsonItemIdentity;
import com.charles445.simpledifficulty.item.ItemJuice;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nukevscity.rlgen.legacy.LegacyStates;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.fml.loading.FMLPaths;

/**
 * 1.12 JsonConfigInternal: the built-in defaults of postInit, then the json files of config/simpledifficulty (written from the
 * RLCraft 2.9.3 copies in the jar when missing). The files keep their 1.12 names: "minecraft:lit_furnace" etc. are turned into 1.21
 * blocks/items through rlgen's legacy tables, names of mods that are not here match nothing (as when such a mod was absent in 1.12).
 */
public class JsonConfigInternal {
   public static float materialTemperatureFire = 5.0F;
   public static final Map<String, Map<String, String>> extraItems = new LinkedHashMap<>();
   private static final String[] FILES = {"armorTemperatures", "blockTemperatures", "consumableTemperature", "consumableThirst",
      "dimensionTemperature", "extraItems", "fluidTemperatures", "heldItemTemperatures", "materialTemperature"};

   static {
      // 1.12 preInit makeExtraItem; RLCraft's extraItems.json enables all three, dragon canteen capacity 8
      extraItems.put("frost_rod", new HashMap<>());
      extraItems.put("frost_powder", new HashMap<>());
      extraItems.put("dragon_canteen", new HashMap<>(Map.of("capacity", "8")));
   }

   public static Path jsonDirectory() {
      return FMLPaths.CONFIGDIR.get().resolve("simpledifficulty");
   }

   public static void clearContainers() {
      JsonConfig.armorTemperatures.clear();
      JsonConfig.blockTemperatures.clear();
      JsonConfig.consumableTemperature.clear();
      JsonConfig.consumableThirst.clear();
      JsonConfig.dimensionTemperature.clear();
      JsonConfig.fluidTemperatures.clear();
      JsonConfig.heldItemTemperatures.clear();
   }

   /** 1.12 postInit defaults + processAllJson. */
   public static void postInit() {
      clearContainers();
      JsonItemIdentity any = new JsonItemIdentity(-1);
      for (Item i : new Item[]{SDItems.wool_helmet.get(), SDItems.wool_chestplate.get(), SDItems.wool_leggings.get(), SDItems.wool_boots.get()}) {
         JsonConfig.registerArmorTemperature(i, 2.0F, any);
      }
      for (Item i : new Item[]{SDItems.ice_helmet.get(), SDItems.ice_chestplate.get(), SDItems.ice_leggings.get(), SDItems.ice_boots.get()}) {
         JsonConfig.registerArmorTemperature(i, -2.0F, any);
      }
      JsonConfig.registerBlockTemperature(SDBlocks.campfire.get(), 7.0F, Map.of("burning", "true"));
      JsonConfig.registerBlockTemperature(SDBlocks.campfire.get(), 0.0F, Map.of("burning", "false"));
      registerBlock("minecraft:torch", 1.0F, Map.of());
      registerBlock("minecraft:lit_furnace", 4.0F, Map.of());
      registerBlock("minecraft:lava", 12.5F, Map.of());
      registerBlock("minecraft:flowing_lava", 12.5F, Map.of());
      registerBlock("minecraft:magma", 10.0F, Map.of());
      JsonConfig.registerConsumableTemperature("food", Items.MUSHROOM_STEW, any, 1.0F, 1200);
      JsonConfig.registerConsumableTemperature("drink", SDItems.juice.get(), new JsonItemIdentity(ItemJuice.JuiceEnum.CACTUS.ordinal()), 1.0F, 1200);
      JsonConfig.registerConsumableTemperature("drink", SDItems.juice.get(), new JsonItemIdentity(ItemJuice.JuiceEnum.CHORUS_FRUIT.ordinal()), 1.0F, 2400);
      JsonConfig.registerConsumableThirst(Items.MILK_BUCKET, any, 4, 1.0F, 0.2F);
      JsonConfig.registerDimensionTemperature(0, 0.0F);
      JsonConfig.registerHeldItem(Items.MAGMA_BLOCK, 3.0F, any);
      JsonConfig.registerHeldItem(Items.TORCH, 1.0F, any);
      processAllJson();
   }

   private static JsonObject read(String name) {
      Path dir = jsonDirectory();
      Path file = dir.resolve(name + ".json");
      try {
         if (!Files.exists(file)) {
            Files.createDirectories(dir);
            try (InputStream in = JsonConfigInternal.class.getResourceAsStream("/data/simpledifficulty/rlcraft_config/" + name + ".json")) {
               if (in != null) {
                  Files.copy(in, file);
               }
            }
         }
         try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(r).getAsJsonObject();
         }
      } catch (Exception e) {
         SimpleDifficulty.logger.error("Error reading simpledifficulty json {}", file, e);
         return new JsonObject();
      }
   }

   public static void processAllJson() {
      for (Map.Entry<String, JsonElement> e : read("armorTemperatures").entrySet()) {
         for (JsonElement v : e.getValue().getAsJsonArray()) {
            JsonObject o = v.getAsJsonObject();
            for (Translated t : items(e.getKey(), identity(o))) {
               JsonConfig.registerArmorTemperature(t.item, o.get("temperature").getAsFloat(), t.identity);
            }
         }
      }
      for (Map.Entry<String, JsonElement> e : read("heldItemTemperatures").entrySet()) {
         for (JsonElement v : e.getValue().getAsJsonArray()) {
            JsonObject o = v.getAsJsonObject();
            for (Translated t : items(e.getKey(), identity(o))) {
               JsonConfig.registerHeldItem(t.item, o.get("temperature").getAsFloat(), t.identity);
            }
         }
      }
      for (Map.Entry<String, JsonElement> e : read("blockTemperatures").entrySet()) {
         for (JsonElement v : e.getValue().getAsJsonArray()) {
            JsonObject o = v.getAsJsonObject();
            Map<String, String> props = new LinkedHashMap<>();
            if (o.has("properties")) {
               o.getAsJsonObject("properties").entrySet().forEach(p -> props.put(p.getKey(), p.getValue().getAsString()));
            }
            registerBlock(e.getKey(), o.get("temperature").getAsFloat(), props);
         }
      }
      for (Map.Entry<String, JsonElement> e : read("consumableTemperature").entrySet()) {
         for (JsonElement v : e.getValue().getAsJsonArray()) {
            JsonObject o = v.getAsJsonObject();
            for (Translated t : items(e.getKey(), identity(o))) {
               JsonConfig.registerConsumableTemperature(o.get("group").getAsString(), t.item, t.identity, o.get("temperature").getAsFloat(),
                  o.get("duration").getAsInt());
            }
         }
      }
      for (Map.Entry<String, JsonElement> e : read("consumableThirst").entrySet()) {
         for (JsonElement v : e.getValue().getAsJsonArray()) {
            JsonObject o = v.getAsJsonObject();
            for (Translated t : items(e.getKey(), identity(o))) {
               JsonConfig.registerConsumableThirst(t.item, t.identity, o.get("amount").getAsInt(), o.get("saturation").getAsFloat(),
                  o.get("thirstyChance").getAsFloat());
            }
         }
      }
      for (Map.Entry<String, JsonElement> e : read("dimensionTemperature").entrySet()) {
         JsonConfig.dimensionTemperature.put(e.getKey(), new JsonConfig.JsonTemperature(e.getValue().getAsJsonObject().get("temperature").getAsFloat()));
      }
      for (Map.Entry<String, JsonElement> e : read("fluidTemperatures").entrySet()) {
         JsonConfig.registerFluidTemperature(e.getKey(), e.getValue().getAsJsonObject().get("temperature").getAsFloat());
      }
      JsonObject mat = read("materialTemperature");
      if (mat.has("fire")) {
         materialTemperatureFire = mat.get("fire").getAsFloat();
      }
      read("extraItems"); // only written out; the extra items are always registered (RLCraft enables all three)
   }

   private static JsonItemIdentity identity(JsonObject o) {
      if (!o.has("identity")) {
         return new JsonItemIdentity(-1);
      }
      JsonObject id = o.getAsJsonObject("identity");
      return new JsonItemIdentity(id.has("metadata") ? id.get("metadata").getAsInt() : -1, id.has("nbt") ? id.get("nbt").getAsString() : null);
   }

   private record Translated(Item item, JsonItemIdentity identity) {
   }

   /** A 1.12 item name + identity to the 1.21 items it covers. */
   private static List<Translated> items(String name, JsonItemIdentity identity) {
      List<Translated> out = new ArrayList<>();
      ResourceLocation id = ResourceLocation.tryParse(name);
      if (id == null) {
         return out;
      }
      String ns = id.getNamespace();
      if (!ns.equals("minecraft")) {
         if (BuiltInRegistries.ITEM.containsKey(id)) {
            out.add(new Translated(BuiltInRegistries.ITEM.get(id), identity));
         }
         return out;
      }
      // vanilla: metadata picks the 1.21 item (minecraft:fish:1 = salmon); -1 covers every 1.12 subtype
      Set<Item> items = new LinkedHashSet<>();
      if (identity.metadata == -1 || identity.metadata == 32767) {
         for (int m = 0; m < 16; m++) {
            Item it = LegacyStates.item(name, m);
            if (it != null && it != Items.AIR) {
               items.add(it);
            }
         }
      } else {
         Item it = LegacyStates.item(name, identity.metadata);
         if (it != null && it != Items.AIR) {
            items.add(it);
         }
      }
      JsonItemIdentity anyMeta = new JsonItemIdentity(-1, identity.nbt);
      for (Item it : items) {
         out.add(new Translated(it, anyMeta));
      }
      return out;
   }

   /**
    * A 1.12 block name to 1.21 blocks: the 1.21 states of every 1.12 metadata; a property that has the same value in all of them
    * (lit=true for lit_furnace) becomes part of the match when the 1.12 block had several metadata values.
    */
   private static void registerBlock(String name, float temperature, Map<String, String> props) {
      ResourceLocation id = ResourceLocation.tryParse(name);
      if (id == null) {
         return;
      }
      if (!id.getNamespace().equals("minecraft")) {
         Block b = null;
         if (BuiltInRegistries.BLOCK.containsKey(id)) {
            b = BuiltInRegistries.BLOCK.get(id);
         } else if (id.getNamespace().equals("lycanitesmobs")) {
            ResourceLocation r = ResourceLocation.fromNamespaceAndPath("rlgen", id.getPath());
            if (BuiltInRegistries.BLOCK.containsKey(r)) {
               b = BuiltInRegistries.BLOCK.get(r);
            }
         }
         if (b != null && b != Blocks.AIR) {
            JsonConfig.registerBlockTemperature(b, temperature, props);
         }
         return;
      }
      Map<Block, List<BlockState>> states = new LinkedHashMap<>();
      int metas = 0;
      for (int m = 0; m < 16; m++) {
         BlockState s = LegacyStates.block(name, m);
         if (s != null && !s.isAir()) {
            states.computeIfAbsent(s.getBlock(), k -> new ArrayList<>()).add(s);
            metas++;
         }
      }
      for (Map.Entry<Block, List<BlockState>> e : states.entrySet()) {
         Map<String, String> p = new LinkedHashMap<>(props);
         if (metas > 1) {
            BlockState first = e.getValue().get(0);
            for (Property<?> prop : first.getProperties()) {
               String v = name(first, prop);
               boolean same = e.getValue().stream().allMatch(s -> name(s, prop).equals(v));
               boolean variesInBlock = prop.getPossibleValues().size() > 1;
               if (same && variesInBlock && !p.containsKey(prop.getName()) && e.getValue().size() > 1) {
                  p.put(prop.getName(), v);
               }
            }
         }
         JsonConfig.registerBlockTemperature(e.getKey(), temperature, p);
      }
   }

   private static <T extends Comparable<T>> String name(BlockState s, Property<T> p) {
      return p.getName(s.getValue(p));
   }
}
