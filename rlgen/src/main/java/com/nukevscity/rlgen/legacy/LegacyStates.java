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

   private static volatile Map<net.minecraft.world.level.block.Block, String> reverse;

   /** The 1.12 vanilla block name a 1.21 block came from (meta 0 first, never the flowing_ / lit_ twins); else its 1.21 id. */
   public static String legacyName(net.minecraft.world.level.block.Block block) {
      Map<net.minecraft.world.level.block.Block, String> r = reverse;
      if (r == null) {
         Map<String, String> b = blocks;
         if (b == null) {
            blocks = b = load("blocks.json");
         }
         Map<net.minecraft.world.level.block.Block, String> out = new HashMap<>();
         Map<net.minecraft.world.level.block.Block, Integer> rank = new HashMap<>();
         for (Map.Entry<String, String> e : b.entrySet()) {
            String key = e.getKey();
            if (!key.startsWith("minecraft:")) {
               continue;
            }
            int c = key.lastIndexOf(':');
            String name = key.substring(0, c);
            int meta = Integer.parseInt(key.substring(c + 1));
            String path = name.substring(10);
            int score = meta + (path.startsWith("flowing_") || path.startsWith("lit_") || path.startsWith("unlit_") || path.startsWith("double_")
               || path.startsWith("powered_") || path.startsWith("unpowered_") ? 100 : 0);
            String v = e.getValue();
            int br = v.indexOf('[');
            ResourceLocation id = ResourceLocation.tryParse(br < 0 ? v : v.substring(0, br));
            if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
               continue;
            }
            net.minecraft.world.level.block.Block blk = BuiltInRegistries.BLOCK.get(id);
            Integer old = rank.get(blk);
            if (old == null || score < old || score == old && name.compareTo(out.get(blk)) < 0) {
               rank.put(blk, score);
               out.put(blk, name);
            }
         }
         reverse = r = out;
      }
      String n = r.get(block);
      return n != null ? n : BuiltInRegistries.BLOCK.getKey(block).toString();
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
