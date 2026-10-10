package com.nukevscity.rlgen.rc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.legacy.biome.Biome;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;

/** All converted RC data (data/rlgen/rc in the mod jar), loaded once. */
public final class RcRegistry {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String ROOT = "/data/rlgen/rc/";
   private static volatile RcRegistry instance;

   private final Map<String, RcStructureDef> structures = new TreeMap<>();
   private final Map<String, Optional<JsonObject>> presets = new ConcurrentHashMap<>();
   private final Map<String, Optional<Category>> categories = new ConcurrentHashMap<>();
   private final Map<String, Optional<RcLoot.Table>> loot = new ConcurrentHashMap<>();
   private final Map<Biome, Selector> selectors = new ConcurrentHashMap<>();

   public static RcRegistry get() {
      RcRegistry r = instance;
      if (r == null) {
         synchronized (RcRegistry.class) {
            r = instance;
            if (r == null) {
               r = new RcRegistry();
               r.load();
               instance = r;
            }
         }
      }
      return r;
   }

   private static InputStream open(String path) {
      return RcRegistry.class.getResourceAsStream(ROOT + path);
   }

   static JsonElement readJson(String path) {
      try (InputStream in = open(path)) {
         return in == null ? null : JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      } catch (Exception e) {
         LOGGER.warn("rlgen: can't read {}", path, e);
         return null;
      }
   }

   private void load() {
      long t0 = System.currentTimeMillis();
      JsonElement index = readJson("structures/index.json");
      if (index == null) {
         LOGGER.error("rlgen: no Recurrent Complex structures in the jar");
         return;
      }
      for (Map.Entry<String, JsonElement> e : index.getAsJsonObject().entrySet()) {
         String id = e.getValue().getAsString();
         try (InputStream in = open("structures/" + e.getKey() + ".nbt")) {
            CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
            this.structures.put(id, new RcStructureDef(id, tag, this));
         } catch (Exception ex) {
            LOGGER.warn("rlgen: can't load RC structure {}", id, ex);
         }
      }
      LOGGER.info("rlgen: loaded {} Recurrent Complex structures in {} ms", this.structures.size(), System.currentTimeMillis() - t0);
   }

   public RcStructureDef structure(String id) {
      RcStructureDef d = this.structures.get(id);
      if (d == null) {
         for (RcStructureDef s : this.structures.values()) {
            if (s.id.equalsIgnoreCase(id)) {
               return s;
            }
         }
      }
      return d;
   }

   public Iterable<RcStructureDef> structures() {
      return this.structures.values();
   }

   // ---------------- presets (PresettedObject: data of a named preset)

   private Optional<JsonObject> preset(String kind, String name) {
      return this.presets.computeIfAbsent(kind + "/" + name.toLowerCase(Locale.ROOT), k -> {
         JsonElement e = readJson("presets/" + k + ".json");
         return e != null && e.isJsonObject() ? Optional.of(e.getAsJsonObject()) : Optional.empty();
      });
   }

   JsonObject transformerPreset(String name) {
      return this.preset("transformer", name).map(o -> o.has("data") && o.get("data").isJsonObject() ? o.getAsJsonObject("data") : null).orElse(null);
   }

   JsonObject placerPreset(String name) {
      return this.preset("placer", name).map(o -> o.has("data") && o.get("data").isJsonObject() ? o.getAsJsonObject("data") : null).orElse(null);
   }

   JsonElement listPreset(String kind, String name) {
      return this.preset(kind, name).map(o -> o.has("data") && o.get("data").isJsonArray() ? o.get("data") : null).orElse(null);
   }

   JsonArray blockPreset(String name) {
      return this.preset("blocks", name).map(o -> o.has("data") && o.get("data").isJsonArray() ? o.getAsJsonArray("data") : null).orElse(null);
   }

   // ---------------- natural generation categories (.rcnc)

   public static final class Category {
      double[] defaultChances;
      final List<RcEnvChance> infos = new ArrayList<>();
      double structureMinCap = 20.0;
      float spawnDistanceMultiplier;
      float spawnDistanceMultiplierCap = 1.0F;

      record RcEnvChance(RcExpr<Biome> biome, boolean dimension, double[] chances) {
      }

      double[] spawnChance(Biome biome) {
         double[] am = this.defaultChances;
         for (RcEnvChance info : this.infos) {
            if (info.biome.test(biome) && info.dimension) {
               am = info.chances;
            }
         }
         return am;
      }

      /** SimpleCategory.structuresInBiome (distance to spawn unknown here: multiplier 1). */
      int structuresInBiome(Biome biome, double totalWeight, Random random) {
         double amount = Math.min(totalWeight / this.structureMinCap, 1.0);
         int n = 0;
         for (double chance : this.spawnChance(biome)) {
            if (random.nextDouble() < chance * amount) {
               n++;
            }
         }
         return n;
      }
   }

   private static double[] chances(JsonObject o, String single, String multi) {
      if (o.has(multi) && o.get(multi).isJsonArray()) {
         JsonArray a = o.getAsJsonArray(multi);
         double[] d = new double[a.size()];
         for (int i = 0; i < d.length; i++) {
            d[i] = a.get(i).getAsDouble();
         }
         return d;
      }
      return new double[]{RcTransformers.dbl(o, single, 0.0)};
   }

   public Category category(String name) {
      return this.categories.computeIfAbsent(name.toLowerCase(Locale.ROOT), k -> {
         JsonElement e = readJson("categories/" + k + ".json");
         if (e == null || !e.isJsonObject()) {
            return Optional.empty();
         }
         JsonObject o = e.getAsJsonObject();
         Category c = new Category();
         c.defaultChances = chances(o, "defaultSpawnChance", "defaultSpawnChances");
         if (o.has("structureMinCap")) {
            c.structureMinCap = o.get("structureMinCap").getAsDouble();
         }
         c.spawnDistanceMultiplier = (float)RcTransformers.dbl(o, "spawnDistanceMultiplier", 0.0);
         c.spawnDistanceMultiplierCap = (float)RcTransformers.dbl(o, "spawnDistanceMultiplierCap", 1.0);
         if (o.has("generationInfos")) {
            for (JsonElement ie : o.getAsJsonArray("generationInfos")) {
               JsonObject i = ie.getAsJsonObject();
               c.infos.add(new Category.RcEnvChance(RcEnv.biomeExpr(RcTransformers.str(i, "biomeMatcher", "")),
                  RcEnv.dimension(RcTransformers.str(i, "dimensionMatcher", "")), chances(i, "spawnChance", "spawnChances")));
            }
         }
         return Optional.of(c);
      }).orElse(null);
   }

   public RcLoot.Table loot(String key) {
      return this.loot.computeIfAbsent(key.toLowerCase(Locale.ROOT), k -> Optional.ofNullable(RcLoot.Table.load(key))).orElse(null);
   }

   // ---------------- selection (MixingStructureSelector over NaturalGeneration)

   public record Entry(RcStructureDef def, RcStructureDef.NaturalGen gen, double weight) {
   }

   public static final class Selector {
      /** Category name -> weighted entries. 1.12 iterated categories in identity-hash order; we use name order. */
      final Map<String, List<Entry>> byCategory = new TreeMap<>();
      final Map<String, Double> totals = new TreeMap<>();

      public boolean isEmpty() {
         return this.byCategory.isEmpty();
      }
   }

   public Selector selector(Biome biome) {
      return this.selectors.computeIfAbsent(biome, b -> {
         Selector s = new Selector();
         for (RcStructureDef def : this.structures.values()) {
            for (RcStructureDef.NaturalGen gen : def.natural) {
               double w = gen.weight(b);
               if (w > 0.0) {
                  s.byCategory.computeIfAbsent(gen.category, k -> new ArrayList<>()).add(new Entry(def, gen, w));
                  s.totals.merge(gen.category, w, Double::sum);
               }
            }
         }
         return s;
      });
   }

   /** MixingStructureSelector.generatedStructures */
   public List<Entry> generatedStructures(Selector s, Random random, Biome biome) {
      List<Entry> out = new ArrayList<>();
      for (Map.Entry<String, List<Entry>> e : s.byCategory.entrySet()) {
         Category cat = this.category(e.getKey());
         int n = cat != null ? cat.structuresInBiome(biome, s.totals.get(e.getKey()), random) : 0;
         for (int i = 0; i < n; i++) {
            out.add(selectWeighted(random, e.getValue(), Entry::weight, s.totals.get(e.getKey())));
         }
      }
      return out;
   }

   /** Structures generated by a structure list (ListGeneration.structures). */
   public List<Map.Entry<RcStructureDef, RcStructureDef.ListGen>> listStructures(String listId, net.minecraft.core.Direction front) {
      List<Map.Entry<RcStructureDef, RcStructureDef.ListGen>> out = new ArrayList<>();
      for (RcStructureDef def : this.structures.values()) {
         for (RcStructureDef.ListGen g : def.lists) {
            if (g.listId().equals(listId) && (front == null || def.rotatable || g.front() == front)) {
               out.add(Map.entry(def, g));
            }
         }
      }
      return out;
   }

   /** IvToolkit WeightedSelector.select */
   public static <X> X selectWeighted(Random random, List<X> items, ToDoubleFunction<X> weight, double total) {
      if (items.isEmpty()) {
         throw new IndexOutOfBoundsException();
      }
      double r = random.nextDouble() * total;
      int counted = 0;
      for (X x : items) {
         r -= weight.applyAsDouble(x);
         if (r <= 0.0) {
            return x;
         }
         counted++;
      }
      int pick = random.nextInt(counted);
      return items.get(Math.min(pick, items.size() - 1));
   }

   public static <X> X selectWeighted(Random random, List<X> items, ToDoubleFunction<X> weight) {
      double total = 0.0;
      for (X x : items) {
         total += weight.applyAsDouble(x);
      }
      return selectWeighted(random, items, weight, total);
   }

   static List<String> sorted(Iterable<String> s) {
      List<String> l = new ArrayList<>();
      s.forEach(l::add);
      Collections.sort(l);
      return l;
   }
}
