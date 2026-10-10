package com.nukevscity.rlgen.rc;

import com.nukevscity.rlgen.legacy.biome.Biome;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/** RC Environment (biome, generation type, variables) and the Biome/Dimension/Environment expressions. */
public final class RcEnv {
   /** DimensionDictionary types of the overworld (DimensionDictionary.registerDefaults, dimension 0). */
   private static final Set<String> OVERWORLD = Set.of("MC_DEFAULT", "REAL", "INFINITE", "NO_TOP_LIMIT", "BOTTOM_LIMIT", "PLANET_SURFACE", "EARTH");

   public final Biome biome;
   public final String genType;
   public final String genId;
   public final Integer villageType;
   public final Map<String, Boolean> variables = new HashMap<>();

   public RcEnv(Biome biome, String genType, String genId, Integer villageType) {
      this.biome = biome;
      this.genType = genType;
      this.genId = genId;
      this.villageType = villageType;
   }

   /** Environment.child(): same biome and generation, fresh variables. */
   public RcEnv child() {
      return new RcEnv(this.biome, this.genType, this.genId, this.villageType);
   }

   // ---------------- BiomeExpression

   public static RcExpr<Biome> biomeExpr(String s) {
      return RcExpr.parse(s, RcEnv::biomeVar);
   }

   static String biomeId(Biome b) {
      return RcBlocks.normalizeId(b.getRegistryName());
   }

   static boolean hasType(Biome b, String type) {
      Set<String> types = BiomeDict.TYPES.getOrDefault(b.getRegistryName(), Set.of());
      return types.contains(type) || type.equals("WATER") && (types.contains("OCEAN") || types.contains("RIVER"));
   }

   static Predicate<Biome> biomeVar(String t) {
      String v;
      if ((v = RcExpr.strip(t, "name=")) != null) {
         String name = v;
         return b -> b.getBiomeName().equals(name);
      }
      if ((v = RcExpr.strip(t, "type=")) != null || (v = RcExpr.strip(t, "$")) != null) {
         String type = v.toUpperCase(Locale.ROOT);
         return b -> hasType(b, type);
      }
      if ((v = RcExpr.strip(t, "id=")) == null) {
         v = t;
      }
      String id = RcBlocks.normalizeId(v);
      return b -> biomeId(b).equals(id);
   }

   // ---------------- DimensionExpression (always evaluated for the overworld)

   public static boolean dimension(String s) {
      return RcExpr.parse(s, RcEnv::dimVar).test(Boolean.TRUE);
   }

   static Predicate<Boolean> dimVar(String t) {
      String v;
      if ((v = RcExpr.strip(t, "type=")) != null || (v = RcExpr.strip(t, "$")) != null) {
         boolean has = OVERWORLD.contains(v.toUpperCase(Locale.ROOT));
         return d -> has;
      }
      if ((v = RcExpr.strip(t, "id=")) == null) {
         v = t;
      }
      boolean zero = v.trim().equals("0");
      return d -> zero;
   }

   // ---------------- EnvironmentExpression

   public static RcExpr<RcEnv> envExpr(String s) {
      return RcExpr.parse(s, RcEnv::envVar);
   }

   static Predicate<RcEnv> envVar(String t) {
      String v;
      if ((v = RcExpr.strip(t, "biome.")) != null) {
         Predicate<Biome> p = biomeVar(v);
         return e -> p.test(e.biome);
      }
      if ((v = RcExpr.strip(t, "dimension.")) != null || (v = RcExpr.strip(t, "dim.")) != null) {
         boolean d = dimVar(v).test(Boolean.TRUE);
         return e -> d;
      }
      if (t.startsWith("dependency.") || t.startsWith("dep.")) {
         return e -> true; // every mod RLCraft's structures depend on was installed
      }
      if ((v = RcExpr.strip(t, "villagetype=")) != null || (v = RcExpr.strip(t, "vtype.")) != null) {
         Integer type;
         try {
            type = Integer.parseInt(v);
         } catch (NumberFormatException ex) {
            type = null;
         }
         Integer ft = type;
         return e -> ft != null && ft.equals(e.villageType);
      }
      if ((v = RcExpr.strip(t, "generation.")) != null || (v = RcExpr.strip(t, "gen.")) != null) {
         String w;
         if ((w = RcExpr.strip(v, "id=")) != null) {
            String id = w;
            return e -> id.equals(e.genId);
         }
         if ((w = RcExpr.strip(v, "type=")) != null || (w = RcExpr.strip(v, "$")) != null) {
            String type = w;
            return e -> type.equals(e.genType);
         }
         return e -> false;
      }
      if ((v = RcExpr.strip(t, "variable.")) != null || (v = RcExpr.strip(t, "var.")) != null) {
         String name = v;
         return e -> e.variables.getOrDefault(name, false);
      }
      return e -> false;
   }
}
