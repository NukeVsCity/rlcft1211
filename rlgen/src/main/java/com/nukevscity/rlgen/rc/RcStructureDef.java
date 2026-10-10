package com.nukevscity.rlgen.rc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.biome.Biome;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

/** One converted .rcst: blocks (palette + 1.12 ids), tile entities, entities, and the parts of structure.json the generator reads. */
public final class RcStructureDef {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<String, BlockState> STATES = new ConcurrentHashMap<>();
   private static volatile BlockState[] modern;

   public record Palette(RcBlocks.LegacyId legacy, BlockState state) {
   }

   public record LootTag(String key, boolean multi, int slot, int min, int max, float chance) {
   }

   public record Tile(BlockPos pos, CompoundTag nbt, List<LootTag> loot, CompoundTag script) {
   }

   public record EntityEntry(double[] pos, CompoundTag nbt) {
   }

   public record WeightedBiome(RcExpr<Biome> expr, Double weight) {
   }

   public record WeightedDim(boolean matches, Double weight) {
   }

   public static final class NaturalGen {
      public String id;
      public String category;
      public Double weight;
      public List<WeightedBiome> biomes;
      public List<WeightedDim> dimensions;
      public RcPlacer placer;

      public double activeWeight() {
         return this.weight != null ? this.weight : 1.0;
      }

      /** NaturalGeneration.getGenerationWeight (overworld). */
      public double weight(Biome biome) {
         double b = 0.0;
         for (WeightedBiome w : this.biomes) {
            if (w.expr.test(biome)) {
               b = w.weight != null ? w.weight : 1.0;
               break;
            }
         }
         double d = 0.0;
         for (WeightedDim w : this.dimensions) {
            if (w.matches) {
               d = w.weight != null ? w.weight : 1.0;
               break;
            }
         }
         return this.activeWeight() * b * d;
      }
   }

   public record ListGen(String id, String listId, BlockPos shift, Direction front, double weight) {
   }

   public record Variable(String id, RcExpr<RcEnv> condition, float chance) {
   }

   /** VanillaGeneration: a piece of 1.12 villages. */
   public record VanillaGen(String id, Double weight, double minBaseLimit, double maxBaseLimit, double minScaledLimit, double maxScaledLimit,
                            BlockPos spawnShift, Direction front, RcExpr<Biome> biomeExpression) {
      public double activeWeight() {
         return this.weight != null ? this.weight : 1.0;
      }

      /** getVanillaWeight with RCConfig.baseVillageSpawnWeight = 10 and no spawn tweaks. */
      public int vanillaWeight() {
         return (int)Math.floor(this.activeWeight() * 1.0 * 10.0 + 0.5);
      }
   }

   public final String id;
   public final int[] size;
   public final Palette[] palette;
   public final int[] blocks;
   public final List<Tile> tiles = new ArrayList<>();
   private final Map<BlockPos, Tile> tileAt = new HashMap<>();
   public final List<EntityEntry> entities = new ArrayList<>();
   public final boolean rotatable;
   public final boolean mirrorable;
   public final boolean blocking;
   public final RcTransformers.Multi transformer;
   public final List<NaturalGen> natural = new ArrayList<>();
   public final List<ListGen> lists = new ArrayList<>();
   public final List<VanillaGen> vanilla = new ArrayList<>();
   public final List<Variable> variables = new ArrayList<>();

   RcStructureDef(String id, CompoundTag tag, RcRegistry reg) {
      this.id = id;
      this.size = tag.getIntArray("size");
      ListTag pal = tag.getList("palette", Tag.TAG_COMPOUND);
      this.palette = new Palette[pal.size()];
      for (int i = 0; i < pal.size(); i++) {
         CompoundTag p = pal.getCompound(i);
         String state = p.getString("state");
         this.palette[i] = new Palette(new RcBlocks.LegacyId(RcBlocks.normalizeId(p.getString("legacy")), p.getByte("meta") & 0xFF),
            state.isEmpty() ? null : parseState(state));
      }
      this.blocks = tag.getIntArray("blocks");
      for (Tag t : tag.getList("tiles", Tag.TAG_COMPOUND)) {
         CompoundTag c = (CompoundTag)t;
         List<LootTag> loot = new ArrayList<>();
         for (Tag l : c.getList("loot", Tag.TAG_COMPOUND)) {
            CompoundTag lc = (CompoundTag)l;
            loot.add(new LootTag(lc.getString("key"), lc.getString("kind").equals("multi"), lc.getInt("slot"), lc.getInt("min"), lc.getInt("max"),
               lc.contains("chance") ? (float)lc.getDouble("chance") : 1.0F));
         }
         Tile tile = new Tile(new BlockPos(c.getInt("x"), c.getInt("y"), c.getInt("z")), c.contains("nbt") ? c.getCompound("nbt") : null, loot,
            c.contains("script") ? c.getCompound("script") : null);
         this.tiles.add(tile);
         this.tileAt.put(tile.pos, tile);
      }
      for (Tag t : tag.getList("entities", Tag.TAG_COMPOUND)) {
         CompoundTag c = (CompoundTag)t;
         ListTag pos = c.getList("pos", Tag.TAG_DOUBLE);
         this.entities.add(new EntityEntry(new double[]{pos.getDouble(0), pos.getDouble(1), pos.getDouble(2)}, c.getCompound("nbt")));
      }

      JsonObject json = JsonParser.parseString(tag.getString("json")).getAsJsonObject();
      this.rotatable = RcTransformers.bool(json, "rotatable", false);
      this.mirrorable = RcTransformers.bool(json, "mirrorable", false);
      this.blocking = RcTransformers.bool(json, "blocking", true);
      if (json.has("transformer") && json.get("transformer").isJsonObject()) {
         JsonObject t = json.getAsJsonObject("transformer");
         this.transformer = RcTransformers.Multi.parse(t.has("transformer") ? t.getAsJsonObject("transformer") : t, reg);
      } else if (json.has("transformers")) {
         this.transformer = new RcTransformers.Multi("", RcTransformers.parseList(json.get("transformers"), reg));
      } else if (json.has("blockTransformers")) {
         this.transformer = new RcTransformers.Multi("", RcTransformers.parseList(json.get("blockTransformers"), reg));
      } else {
         this.transformer = new RcTransformers.Multi("", new ArrayList<>());
      }
      if (json.has("generationInfos")) {
         for (JsonElement e : json.getAsJsonArray("generationInfos")) {
            JsonObject g = e.getAsJsonObject();
            String type = RcTransformers.str(g, "type", "");
            JsonObject info = g.has("generationInfo") ? g.getAsJsonObject("generationInfo") : new JsonObject();
            if (type.equals("natural")) {
               this.natural.add(parseNatural(info, reg));
            } else if (type.equals("structureList")) {
               Direction front = Direction.byName(RcTransformers.str(info, "front", "NORTH").toLowerCase(Locale.ROOT));
               this.lists.add(new ListGen(RcTransformers.str(info, "id", ""), RcTransformers.str(info, "listID", ""),
                  new BlockPos((int)RcTransformers.dbl(info, "positionX", 0), (int)RcTransformers.dbl(info, "positionY", 0),
                     (int)RcTransformers.dbl(info, "positionZ", 0)),
                  front != null ? front : Direction.NORTH, RcTransformers.dbl(info, "weight", 1.0)));
            } else if (type.equals("vanilla")) {
               String f = RcTransformers.str(info, "front", null);
               this.vanilla.add(new VanillaGen(RcTransformers.str(info, "id", ""),
                  info.has("generationWeight") ? info.get("generationWeight").getAsDouble() : null,
                  RcTransformers.dbl(info, "minBaseLimit", 0.0), RcTransformers.dbl(info, "maxBaseLimit", 0.0),
                  RcTransformers.dbl(info, "minScaledLimit", 0.0), RcTransformers.dbl(info, "maxScaledLimit", 0.0),
                  new BlockPos((int)RcTransformers.dbl(info, "spawnShiftX", 0), (int)RcTransformers.dbl(info, "spawnShiftY", 0),
                     (int)RcTransformers.dbl(info, "spawnShiftZ", 0)),
                  f == null ? null : Direction.byName(f.toLowerCase(Locale.ROOT)), RcEnv.biomeExpr(RcTransformers.str(info, "biomeExpression", ""))));
            }
         }
      }
      if (json.has("variableDomain") && json.getAsJsonObject("variableDomain").has("variables")) {
         for (JsonElement e : json.getAsJsonObject("variableDomain").getAsJsonArray("variables")) {
            JsonObject v = e.getAsJsonObject();
            this.variables.add(new Variable(RcTransformers.str(v, "id", ""), RcEnv.envExpr(RcTransformers.str(v, "condition", "")),
               (float)RcTransformers.dbl(v, "chance", 0.5)));
         }
      }
   }

   private static NaturalGen parseNatural(JsonObject info, RcRegistry reg) {
      NaturalGen n = new NaturalGen();
      n.id = RcTransformers.str(info, "id", "");
      n.category = RcTransformers.str(info, "generationCategory", "decoration");
      n.weight = info.has("generationWeight") ? info.get("generationWeight").getAsDouble() : null;
      n.biomes = new ArrayList<>();
      JsonElement biomes = info.has("biomeWeightsPreset") ? reg.listPreset("biome", info.get("biomeWeightsPreset").getAsString()) : null;
      if (biomes == null) {
         biomes = info.has("generationBiomes") ? info.get("generationBiomes") : reg.listPreset("biome", "overworld");
      }
      for (JsonElement e : biomes.getAsJsonArray()) {
         JsonObject b = e.getAsJsonObject();
         String expr = RcTransformers.str(b, "biomes", null);
         if (expr == null) {
            expr = RcTransformers.str(b, "biome", "");
            if (expr.startsWith("Type:")) {
               expr = "$" + expr.substring(5).replace(",", " & $");
            }
         }
         n.biomes.add(new WeightedBiome(RcEnv.biomeExpr(expr), b.has("weight") ? b.get("weight").getAsDouble() : null));
      }
      n.dimensions = new ArrayList<>();
      JsonElement dims = info.has("dimensionWeightsPreset") ? reg.listPreset("dimension", info.get("dimensionWeightsPreset").getAsString()) : null;
      if (dims == null) {
         dims = info.has("generationDimensions") ? info.get("generationDimensions") : reg.listPreset("dimension", "overworld");
      }
      for (JsonElement e : dims.getAsJsonArray()) {
         JsonObject d = e.getAsJsonObject();
         n.dimensions.add(new WeightedDim(RcEnv.dimension(RcTransformers.str(d, "dimensions", "")), d.has("weight") ? d.get("weight").getAsDouble() : null));
      }
      n.placer = RcPlacer.parse(info, reg);
      return n;
   }

   public boolean contains(BlockPos p) {
      return p.getX() >= 0 && p.getY() >= 0 && p.getZ() >= 0 && p.getX() < this.size[0] && p.getY() < this.size[1] && p.getZ() < this.size[2];
   }

   public Palette palette(int x, int y, int z) {
      return this.palette[this.blocks[(z * this.size[1] + y) * this.size[0] + x]];
   }

   public RcBlocks.LegacyId legacy(int x, int y, int z) {
      return this.palette(x, y, z).legacy;
   }

   public RcTransformers.Src src(int x, int y, int z, RcTransform t) {
      Palette p = this.palette(x, y, z);
      return new RcTransformers.Src(p.legacy, p.state == null ? null : t.apply(p.state));
   }

   public Tile tileAt(BlockPos src) {
      return this.tileAt.get(src);
   }

   public static BlockState parseState(String s) {
      return STATES.computeIfAbsent(s, k -> {
         try {
            return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), k, false).blockState();
         } catch (Exception e) {
            LOGGER.warn("rlgen: bad block state {} in RC data", k);
            return Blocks.AIR.defaultBlockState();
         }
      });
   }

   public static BlockState modern(LBlock b) {
      BlockState[] s = modern;
      if (s == null) {
         LBlock[] all = LBlock.values();
         s = new BlockState[all.length];
         for (int i = 0; i < all.length; i++) {
            s[i] = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(all[i].modernId)).defaultBlockState();
         }
         modern = s;
      }
      return b == null ? Blocks.AIR.defaultBlockState() : s[b.ordinal()];
   }
}
