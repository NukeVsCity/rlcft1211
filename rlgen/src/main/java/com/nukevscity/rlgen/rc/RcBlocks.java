package com.nukevscity.rlgen.rc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.FluidTags;

/** Block side of RC expressions: BlockExpression (structure blocks, 1.12 id + metadata) and PositionedBlockExpression (world blocks). */
public final class RcBlocks {
   private static final Map<String, Boolean> GLOBALS = Map.of("treeLeavesDecay", true); // reccomplex.cfg globalToggles
   private static Map<Block, LegacyId> legacyIds;

   private RcBlocks() {
   }

   /** A 1.12 block: registry name + metadata. */
   public record LegacyId(String id, int meta) {
   }

   static boolean global(String name) {
      return GLOBALS.getOrDefault(name, false);
   }

   public static String normalizeId(String s) {
      s = s.toLowerCase(Locale.ROOT);
      return s.indexOf(':') < 0 ? "minecraft:" + s : s;
   }

   // ---------------- BlockExpression: tests a 1.12 id + metadata

   public static RcExpr<LegacyId> blockExpr(String s) {
      return RcExpr.parse(s, RcBlocks::blockVar);
   }

   static Predicate<LegacyId> blockVar(String t) {
      String v;
      if ((v = RcExpr.strip(t, "id=")) != null) {
         String id = normalizeId(v);
         return b -> b.id.equals(id);
      }
      if ((v = RcExpr.strip(t, "domain=")) != null) {
         String ns = v.toLowerCase(Locale.ROOT);
         return b -> b.id.startsWith(ns + ":");
      }
      if ((v = RcExpr.strip(t, "metadata=")) != null || (v = RcExpr.strip(t, "#")) != null) {
         int min;
         int max;
         try {
            int dash = v.indexOf('-');
            min = Integer.parseInt(dash >= 0 ? v.substring(0, dash) : v);
            max = dash >= 0 ? Integer.parseInt(v.substring(dash + 1)) : min;
         } catch (NumberFormatException e) {
            return b -> false;
         }
         int lo = Math.min(min, max);
         int hi = Math.max(min, max);
         return b -> b.meta >= lo && b.meta <= hi;
      }
      if (t.startsWith("property[") || t.startsWith("$[")) {
         return b -> false;
      }
      String id = normalizeId(t);
      return b -> b.id.equals(id);
   }

   // ---------------- PositionedBlockExpression: tests a 1.21 world block

   public static RcExpr<BlockState> worldExpr(String s) {
      return RcExpr.parse(s, RcBlocks::worldVar);
   }

   static Predicate<BlockState> worldVar(String t) {
      String v;
      if ((v = RcExpr.strip(t, "is:")) != null) {
         return switch (v) {
            case "leaves" -> RcBlocks::isLeaves;
            case "air" -> BlockState::isAir;
            case "foliage" -> RcBlocks::isFoliage;
            case "replaceable" -> RcBlocks::isReplaceable;
            case "liquid" -> s -> s.getBlock() instanceof LiquidBlock;
            case "water" -> s -> s.getBlock() instanceof LiquidBlock && s.getFluidState().is(FluidTags.WATER);
            case "lava" -> s -> s.getBlock() instanceof LiquidBlock && s.getFluidState().is(FluidTags.LAVA);
            default -> s -> false;
         };
      }
      if ((v = RcExpr.strip(t, "sustains.")) != null) {
         return switch (v) {
            case "trees" -> s -> s.is(BlockTags.DIRT) || s.is(Blocks.FARMLAND);
            case "mushrooms" -> s -> s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.MYCELIUM);
            case "cacti" -> s -> s.is(BlockTags.SAND);
            default -> s -> false;
         };
      }
      if ((v = RcExpr.strip(t, "blocks:")) != null) {
         return switch (v) {
            case "movement" -> RcBlocks::blocksMovement;
            case "light" -> BlockState::canOcclude;
            default -> s -> false;
         };
      }
      Predicate<LegacyId> inner = blockVar((v = RcExpr.strip(t, "block.")) != null ? v : t);
      return s -> inner.test(legacyId(s));
   }

   @SuppressWarnings("deprecation")
   static boolean blocksMovement(BlockState s) {
      return s.blocksMotion();
   }

   static boolean isLeaves(BlockState s) {
      return s.is(BlockTags.LEAVES) || s.getBlock() instanceof LeavesBlock;
   }

   @SuppressWarnings("deprecation")
   static boolean isReplaceable(BlockState s) {
      return s.canBeReplaced();
   }

   /** RCBlockLogic.isFoliage: foliage, wood, leaves, or material leaves / plants / vine. */
   public static boolean isFoliage(BlockState s) {
      if (s.is(BlockTags.LOGS) || isLeaves(s) || s.getBlock() instanceof BushBlock || s.getBlock() instanceof VineBlock
         || s.is(BlockTags.REPLACEABLE_BY_TREES) || s.is(Blocks.SUGAR_CANE) || s.is(Blocks.COCOA)) {
         return true;
      }
      ResourceLocation key = BuiltInRegistries.BLOCK.getKey(s.getBlock());
      return key.getNamespace().equals("dynamictrees") || key.getPath().contains("branch") || key.getPath().endsWith("_leaves");
   }

   /** The 1.12 id + metadata a 1.21 block came from (for id= / metadata= tests on world blocks). */
   public static LegacyId legacyId(BlockState s) {
      Map<Block, LegacyId> map = legacyIds;
      if (map == null) {
         map = loadLegacyIds();
      }
      LegacyId id = map.get(s.getBlock());
      if (id != null) {
         return id;
      }
      if (s.isAir()) {
         return new LegacyId("minecraft:air", 0);
      }
      return new LegacyId(BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString(), 0);
   }

   private static synchronized Map<Block, LegacyId> loadLegacyIds() {
      if (legacyIds != null) {
         return legacyIds;
      }
      Map<Block, LegacyId> map = new HashMap<>();
      try (InputStream in = RcBlocks.class.getResourceAsStream("/data/rlgen/rc/legacy_ids.json")) {
         if (in != null) {
            JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : o.entrySet()) {
               JsonArray a = e.getValue().getAsJsonArray();
               BuiltInRegistries.BLOCK.getOptional(ResourceLocation.withDefaultNamespace(e.getKey()))
                  .ifPresent(b -> map.put(b, new LegacyId(a.get(0).getAsString(), a.get(1).getAsInt())));
            }
         }
      } catch (Exception e) {
         throw new IllegalStateException("rlgen: can't read legacy_ids.json", e);
      }
      map.put(Blocks.CAVE_AIR, new LegacyId("minecraft:air", 0));
      map.put(Blocks.VOID_AIR, new LegacyId("minecraft:air", 0));
      legacyIds = map;
      return map;
   }
}
