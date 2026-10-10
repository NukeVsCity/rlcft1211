package com.nukevscity.rlgen.rc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * RC SelectivePlacer + GenericPlacer: picks the Y of a structure from weighted "factors" (limit rays, block matching),
 * evaluated on the 1.12 terrain. Heights are 1.12's (world height 256, sea level 63).
 */
public final class RcPlacer {
   static final int HEIGHT = 256;
   static final int SEA_LEVEL = 63;

   final List<Factor> factors;
   final int baseline;
   final RcExpr<RcBlocks.LegacyId> sourceMatcher;

   RcPlacer(List<Factor> factors, int baseline, String sourceExpression) {
      this.factors = factors;
      this.baseline = baseline;
      this.sourceMatcher = RcBlocks.blockExpr(sourceExpression);
   }

   /** What a placer sees: the structure, its transform, bounding box at y = 0, and the place random. */
   public record PlaceCtx(RcStructureDef def, RcTransform transform, BlockPos lower, int[] bbSize, Random random, RcWorldView world) {
   }

   // ------------------------------------------------------------------ parsing

   static RcPlacer surface() {
      return new RcPlacer(parseFactors(RcRegistry.get().placerPreset("surface")), 0, "!(air | reccomplex:generic_space)");
   }

   static RcPlacer parse(JsonObject info, RcRegistry reg) {
      JsonObject placer = null;
      boolean read = false;
      if (info.has("placerPreset")) {
         placer = reg.placerPreset(info.get("placerPreset").getAsString());
         read = placer != null;
      }
      if (!read && info.has("placer") && info.get("placer").isJsonObject()) {
         placer = info.getAsJsonObject("placer");
         read = true;
      }
      if (!read && info.has("generationY")) {
         return legacy(info.getAsJsonObject("generationY"), reg);
      }
      if (!read) {
         placer = reg.placerPreset("surface");
      }
      return new RcPlacer(parseFactors(placer), (int)RcTransformers.dbl(info, "baseline", 0), RcTransformers.str(info, "sourceExpression", ""));
   }

   static List<Factor> parseFactors(JsonObject placer) {
      List<Factor> out = new ArrayList<>();
      if (placer == null || !placer.has("factors")) {
         return out;
      }
      for (JsonElement e : placer.getAsJsonArray("factors")) {
         JsonObject f = e.getAsJsonObject();
         String type = RcTransformers.str(f, "type", "");
         JsonObject o = f.has("factor") ? f.getAsJsonObject("factor") : new JsonObject();
         float priority = (float)RcTransformers.dbl(o, "priority", 1.0);
         if (type.equals("limit")) {
            List<Ray> rays = new ArrayList<>();
            JsonArray ra = o.has("rays") ? o.getAsJsonArray("rays") : new JsonArray();
            for (JsonElement re : ra) {
               JsonObject r = re.getAsJsonObject();
               rays.add(parseRay(RcTransformers.str(r, "type", ""), r.has("ray") ? r.getAsJsonObject("ray") : new JsonObject()));
            }
            out.add(new Limit(priority, rays));
         } else if (type.equals("match")) {
            out.add(new Match(priority, RcTransformers.str(o, "sourceExpression", ""), RcTransformers.str(o, "destExpression", ""),
               (float)RcTransformers.dbl(o, "requiredConformity", 0.0)));
         }
      }
      return out;
   }

   static Ray parseRay(String type, JsonObject o) {
      Float weight = o.has("weight") ? (float)o.get("weight").getAsDouble() : null;
      return switch (type) {
         case "dynpos" -> new Ray(weight, "dynpos:" + RcTransformers.str(o, "type", "bedrock"), 0, false, 0.0F, "");
         case "move" -> new Ray(weight, "move", (int)RcTransformers.dbl(o, "distance", 0), false, 0.0F, "");
         case "dynmove" -> new Ray(weight, "dynmove", 0, RcTransformers.bool(o, "up", false), 0.0F, "");
         case "average" -> new Ray(weight, "average", 0, RcTransformers.bool(o, "up", true), 0.0F, RcTransformers.str(o, "destExpression", ""));
         case "matcher" -> new Ray(weight, "matcher", 0, RcTransformers.bool(o, "up", true), (float)RcTransformers.dbl(o, "requiredRatio", 0.0),
            RcTransformers.str(o, "destExpression", ""));
         default -> new Ray(weight, "none", 0, false, 0.0F, "");
      };
   }

   /** SelectivePlacer.Serializer.readLegacyPlacer (old "generationY"). */
   static RcPlacer legacy(JsonObject g, RcRegistry reg) {
      String mode = RcTransformers.str(g, "selectionMode", "surface");
      int minY = (int)RcTransformers.dbl(g, "minY", 0);
      int maxY = (int)RcTransformers.dbl(g, "maxY", 0);
      switch (mode) {
         case "underwater":
            return new RcPlacer(parseFactors(reg.placerPreset("underwater")), -(minY + maxY) / 2, "!(air | reccomplex:generic_space)");
         case "lowestedge": {
            List<Ray> rays = List.of(new Ray(null, "dynpos:world_height", 0, false, 0.0F, ""),
               new Ray(null, "matcher", 0, false, 0.9F, "!(is:air | is:foliage | is:replaceable) & !is:liquid"),
               new Ray(null, "move", minY, false, 0.0F, ""), new Ray(1.0F, "move", maxY - minY, false, 0.0F, ""));
            return new RcPlacer(List.of(new Limit(1.0F, rays)), 0, "");
         }
         case "bedrock":
         case "sealevel":
         case "top": {
            String dyn = mode.equals("bedrock") ? "bedrock" : (mode.equals("sealevel") ? "sealevel" : "world_height");
            List<Ray> rays = List.of(new Ray(null, "dynpos:" + dyn, 0, false, 0.0F, ""), new Ray(null, "move", minY, false, 0.0F, ""),
               new Ray(1.0F, "move", maxY - minY, false, 0.0F, ""));
            return new RcPlacer(List.of(new Limit(1.0F, rays)), 0, "");
         }
         default:
            return new RcPlacer(parseFactors(reg.placerPreset("surface")), -(minY + maxY) / 2, "!(air | reccomplex:generic_space)");
      }
   }

   // ------------------------------------------------------------------ placing

   /** A weighted set of Ys (RC's LineSelection restricted to 0..255). */
   record Consideration(BitSet ys, float weight) {
   }

   /** SelectivePlacer.place: returns the Y offset, or -1. */
   public int place(PlaceCtx c) {
      List<BlockPos> surface = new ArrayList<>();
      int[] size = c.def.size;
      for (int x = 0; x < size[0]; x++) {
         for (int z = 0; z < size[2]; z++) {
            BlockPos p = new BlockPos(x, this.baseline, z);
            if (this.sourceMatcher.test(c.def.contains(p) ? c.def.legacy(x, this.baseline, z) : new RcBlocks.LegacyId("minecraft:air", 0))) {
               surface.add(p);
            }
         }
      }
      if (this.factors.isEmpty()) {
         return -1;
      }
      BitSet considerable = new BitSet(HEIGHT);
      int top = HEIGHT - c.bbSize[1];
      if (top >= 0) {
         considerable.set(0, Math.min(top, HEIGHT - 1) + 1);
      }
      List<Consideration> considerations = new ArrayList<>();
      for (Factor f : this.factors) {
         List<Consideration> cons = f.consider(c, considerable, surface);
         BitSet union = new BitSet(HEIGHT);
         for (Consideration con : cons) {
            if (con.weight <= 0.0F) {
               considerable.andNot(con.ys);
            }
         }
         for (Consideration con : cons) {
            if (con.weight > 0.0F) {
               union.or(con.ys);
               considerations.add(con);
            }
         }
         considerable.and(union);
      }
      List<int[]> ys = new ArrayList<>();
      List<Double> ws = new ArrayList<>();
      for (int y = considerable.nextSetBit(0); y >= 0; y = considerable.nextSetBit(y + 1)) {
         double w = 1.0;
         for (Consideration con : considerations) {
            if (con.ys.get(y)) {
               w *= con.weight;
            }
         }
         if (w > 0.0) {
            ys.add(new int[]{y});
            ws.add(w);
         }
      }
      if (ys.isEmpty()) {
         return -1;
      }
      // Collectors.toSet(): a HashSet<Pair<Integer, Double>>, iterated in bucket order
      int n = ys.size();
      int cap = 16;
      while (n > cap * 3 / 4) {
         cap *= 2;
      }
      Integer[] order = new Integer[n];
      int[] bucket = new int[n];
      for (int i = 0; i < n; i++) {
         order[i] = i;
         int h = Integer.hashCode(ys.get(i)[0]) ^ Double.hashCode(ws.get(i));
         bucket[i] = (h ^ (h >>> 16)) & (cap - 1);
      }
      java.util.Arrays.sort(order, (a, b) -> bucket[a] != bucket[b] ? Integer.compare(bucket[a], bucket[b]) : Integer.compare(a, b));
      double total = 0.0;
      for (Integer i : order) {
         total += ws.get(i);
      }
      double r = c.random.nextDouble() * total;
      int counted = 0;
      for (Integer i : order) {
         r -= ws.get(i);
         if (r <= 0.0) {
            return ys.get(i)[0];
         }
         counted++;
      }
      int pick = c.random.nextInt(counted);
      return ys.get(order[Math.min(pick, n - 1)])[0];
   }

   abstract static class Factor {
      final float priority;

      Factor(float priority) {
         this.priority = priority;
      }

      float weight(float w) {
         return (float)Math.pow(w, this.priority);
      }

      abstract List<Consideration> consider(PlaceCtx c, BitSet considerable, List<BlockPos> surface);
   }

   static BitSet range(int a, int b) {
      BitSet s = new BitSet(HEIGHT);
      int lo = Math.max(0, Math.min(a, b));
      int hi = Math.min(HEIGHT - 1, Math.max(a, b));
      if (lo <= hi) {
         s.set(lo, hi + 1);
      }
      return s;
   }

   static final class Limit extends Factor {
      final List<Ray> rays;

      Limit(float priority, List<Ray> rays) {
         super(priority);
         this.rays = rays;
      }

      @Override
      List<Consideration> consider(PlaceCtx c, BitSet considerable, List<BlockPos> surface) {
         List<Consideration> out = new ArrayList<>();
         int pos = HEIGHT - 1;
         for (Ray ray : this.rays) {
            int before = pos;
            Integer cast = ray.cast(c, surface, pos);
            if (cast == null) {
               break;
            }
            pos = cast;
            BitSet sel = range(pos, before);
            sel.and(considerable);
            if (ray.weight != null && !sel.isEmpty()) {
               out.add(new Consideration(sel, this.weight(ray.weight)));
            }
         }
         return out;
      }
   }

   static final class Match extends Factor {
      final RcExpr<RcBlocks.LegacyId> source;
      final RcExpr<BlockState> dest;
      final float requiredConformity;

      Match(float priority, String source, String dest, float requiredConformity) {
         super(priority);
         this.source = RcBlocks.blockExpr(source);
         this.dest = RcBlocks.worldExpr(dest);
         this.requiredConformity = requiredConformity;
      }

      float conformity(PlaceCtx c, List<BlockPos> sources, int dy) {
         int failChances = (int)((float)sources.size() * (1.0F - this.requiredConformity));
         int matched = 0;
         for (BlockPos p : sources) {
            if (this.dest.test(c.world.get(p.above(dy)))) {
               matched++;
            } else if (--failChances < 0) {
               break;
            }
         }
         return failChances >= 0 ? (float)matched / (float)sources.size() : 0.0F;
      }

      @Override
      List<Consideration> consider(PlaceCtx c, BitSet considerable, List<BlockPos> surface) {
         List<Consideration> out = new ArrayList<>();
         List<BlockPos> sources = new ArrayList<>();
         int[] size = c.def.size;
         for (int x = 0; x < size[0]; x++) {
            for (int y = 0; y < size[1]; y++) {
               for (int z = 0; z < size[2]; z++) {
                  if (this.source.test(c.def.legacy(x, y, z))) {
                     // RC applies the transform with the bounding box size here (not the structure's); kept as-is.
                     sources.add(c.transform.apply(x, y, z, c.bbSize).offset(c.lower.getX(), 0, c.lower.getZ()));
                  }
               }
            }
         }
         int start = considerable.nextSetBit(0);
         while (start >= 0) {
            int end = considerable.nextClearBit(start) - 1;
            Float cur = null;
            int lastY = end;
            for (int y = end; y >= start; y--) {
               float conf = this.conformity(c, sources, y);
               if (cur == null) {
                  cur = conf;
                  lastY = y;
               } else if (!(Math.abs(conf - cur) <= 0.01)) {
                  out.add(new Consideration(range(lastY, y + 1), this.weight(cur)));
                  cur = conf;
                  lastY = y;
               }
            }
            if (cur != null) {
               out.add(new Consideration(range(lastY, start), this.weight(cur)));
            }
            start = considerable.nextSetBit(end + 1);
         }
         return out;
      }
   }

   static final class Ray {
      final Float weight;
      final String type;
      final int distance;
      final boolean up;
      final float requiredRatio;
      final RcExpr<BlockState> dest;

      Ray(Float weight, String type, int distance, boolean up, float requiredRatio, String dest) {
         this.weight = weight;
         this.type = type;
         this.distance = distance;
         this.up = up;
         this.requiredRatio = requiredRatio;
         this.dest = RcBlocks.worldExpr(dest);
      }

      List<BlockPos> shifted(PlaceCtx c, List<BlockPos> surface) {
         List<BlockPos> out = new ArrayList<>(surface.size());
         for (BlockPos p : surface) {
            out.add(c.transform.apply(p, c.def.size).offset(c.lower));
         }
         return out;
      }

      Integer cast(PlaceCtx c, List<BlockPos> surface, int y) {
         switch (this.type) {
            case "dynpos:bedrock":
               return 0;
            case "dynpos:sealevel":
               return SEA_LEVEL;
            case "dynpos:world_height":
               return HEIGHT - 1 - c.def.size[1];
            case "move": {
               int ny = y + this.distance;
               return ny >= 0 && ny < HEIGHT ? ny : null;
            }
            case "dynmove":
               return y + c.bbSize[1] * (this.up ? 1 : -1);
            case "average": {
               List<BlockPos> shifted = this.shifted(c, surface);
               int floorBlocks = shifted.size();
               double samples = floorBlocks < 256 ? 1.0 : Math.pow(256.0F / (float)floorBlocks, 0.7F);
               List<Integer> found = new ArrayList<>();
               for (BlockPos s : shifted) {
                  if (samples >= 1.0 || c.random.nextDouble() < samples) {
                     int py = s.getY() + y;
                     while (py >= 0 && py < HEIGHT) {
                        if (this.dest.test(c.world.get(new BlockPos(s.getX(), py, s.getZ())))) {
                           found.add(py - s.getY());
                           break;
                        }
                        py += this.up ? 1 : -1;
                     }
                  }
               }
               int avg = found.isEmpty() ? -1 : robustAverage(found);
               return avg >= 0 ? avg : null;
            }
            case "matcher": {
               List<BlockPos> shifted = this.shifted(c, surface);
               while (y >= 0 && y < HEIGHT) {
                  if (this.matches(c, shifted, y)) {
                     return y;
                  }
                  y += this.up ? 1 : -1;
               }
               return null;
            }
            default:
               return null;
         }
      }

      boolean matches(PlaceCtx c, List<BlockPos> surface, int y) {
         int chances = surface.size();
         int need = (int)((float)chances * this.requiredRatio);
         chances -= need;
         for (BlockPos s : surface) {
            boolean go;
            if (this.dest.test(c.world.get(new BlockPos(s.getX(), s.getY() + y, s.getZ())))) {
               go = --need > 0;
            } else {
               go = --chances > 0;
            }
            if (!go) {
               break;
            }
         }
         return need <= 0;
      }

      static int robustAverage(List<Integer> values) {
         int average = 0;
         for (int v : values) {
            average += v;
         }
         average /= values.size();
         int averageDist = 0;
         for (int v : values) {
            averageDist += Math.abs(v - average);
         }
         averageDist /= values.size();
         int newAverage = 0;
         int ignored = 0;
         for (int v : values) {
            if (Math.abs(v - average) <= averageDist * 2) {
               newAverage += v;
            } else {
               ignored++;
            }
         }
         return newAverage / (values.size() - ignored);
      }
   }
}
