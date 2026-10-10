package com.nukevscity.rlgen.legacy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

/** 1.12 block name + metadata and item name + damage to 1.21 (data/rlgen/legacy, made by scripts/gen_legacy_tables.py). */
public final class LegacyStates {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static volatile Map<String, String> blocks;
   private static volatile Map<String, String> items;
   private static final Map<String, BlockState> PARSED = new ConcurrentHashMap<>();

   private LegacyStates() {
   }

   private static Map<String, String> load(String file) {
      Map<String, String> out = new HashMap<>();
      try (InputStream in = LegacyStates.class.getResourceAsStream("/data/rlgen/legacy/" + file)) {
         JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
         o.entrySet().forEach(e -> out.put(e.getKey(), e.getValue().getAsString()));
      } catch (Exception e) {
         LOGGER.error("rlgen: could not read legacy table {}", file, e);
      }
      return out;
   }

   private static String norm(String name) {
      return name.contains(":") ? name : "minecraft:" + name;
   }

   /** null when 1.12 had no such block (or it came from a mod we do not have). */
   @Nullable
   public static BlockState block(String name, int meta) {
      Map<String, String> b = blocks;
      if (b == null) {
         blocks = b = load("blocks.json");
      }
      String n = norm(name);
      String s = b.get(n + ":" + meta);
      if (s == null) {
         s = b.get(n + ":0");
      }
      if (s == null) {
         return null;
      }
      return PARSED.computeIfAbsent(s, k -> {
         try {
            return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), k, false).blockState();
         } catch (Exception e) {
            LOGGER.warn("rlgen: bad legacy state {}", k);
            return null;
         }
      });
   }

   @Nullable
   public static Item item(String name, int damage) {
      Map<String, String> i = items;
      if (i == null) {
         items = i = load("items.json");
      }
      String n = norm(name);
      String s = i.get(n + ":" + damage);
      if (s == null) {
         s = i.get(n + ":0");
      }
      if (s == null) {
         return null;
      }
      ResourceLocation id = ResourceLocation.parse(s);
      return BuiltInRegistries.ITEM.containsKey(id) ? BuiltInRegistries.ITEM.get(id) : null;
   }
}
