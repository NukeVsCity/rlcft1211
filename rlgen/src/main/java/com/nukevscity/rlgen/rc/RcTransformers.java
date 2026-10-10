package com.nukevscity.rlgen.rc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.legacy.biome.Biome;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;

/** The RC 1.4.8 transformers RLCraft's structures use, ported to 1.21 blocks. */
public final class RcTransformers {
   private static final Logger LOGGER = LogUtils.getLogger();

   private RcTransformers() {
   }

   public enum Phase {
      BEFORE,
      AFTER
   }

   /** A structure block: its 1.12 id/metadata (for expressions) and its 1.21 state, already rotated (null = RC special block). */
   public record Src(RcBlocks.LegacyId legacy, BlockState state) {
   }

   /** RunTransformer: the fused transformer and its prepared data, for transformers that ask "would this block be skipped?". */
   public record Run(Multi transformer, MultiData data) {
      public boolean skip(RcContext c, BlockPos worldPos, Src src, BlockPos srcPos) {
         return this.transformer.skip(this.data, c, worldPos, src, srcPos);
      }
   }

   public abstract static class T {
      Object prepare(RcContext c) {
         return null;
      }

      void configure(Object data, RcContext c, Run run) {
      }

      boolean mayGenerate(Object data, RcContext c) {
         return true;
      }

      boolean skip(Object data, RcContext c, BlockPos worldPos, Src src, BlockPos srcPos) {
         return false;
      }

      void transform(Object data, Phase phase, RcContext c, Run run) {
      }

      /** Blocks outside the structure this transformer may write (cloud expansion), in world space. */
      void addExtent(Object data, RcContext c, List<BlockPos> out) {
      }
   }

   // ------------------------------------------------------------------ parsing

   static String str(JsonObject o, String k, String def) {
      return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsString() : def;
   }

   static double dbl(JsonObject o, String k, double def) {
      return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsDouble() : def;
   }

   static boolean bool(JsonObject o, String k, boolean def) {
      return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsBoolean() : def;
   }

   /** TransformerReplace.Serializer.readLegacyMatcher */
   static String sourceExpression(JsonObject o) {
      if (o.has("source")) {
         String block = o.get("source").getAsString();
         int meta = (int)dbl(o, "sourceMetadata", -1);
         return meta >= 0 ? block + " & metadata=" + meta : block;
      }
      return str(o, "sourceExpression", "");
   }

   public static T parse(JsonObject entry, RcRegistry reg) {
      String type = str(entry, "type", "");
      JsonObject o = entry.has("transformer") ? entry.getAsJsonObject("transformer") : new JsonObject();
      return switch (type) {
         case "multi" -> Multi.parse(o, reg);
         case "natural" -> new Natural(sourceExpression(o), str(o, "destExpression", "is:replaceable | is:foliage"),
            dbl(o, "naturalExpansionDistance", 4.0), dbl(o, "naturalExpansionRandomization", 6.0));
         case "naturalAir" -> new NaturalAir(sourceExpression(o), str(o, "destExpression", ""),
            dbl(o, "naturalExpansionDistance", 4.0), dbl(o, "naturalExpansionRandomization", 10.0));
         case "negativeSpace" -> new NegativeSpace(sourceExpression(o), str(o, "destExpression", ""));
         case "replace", "replaceAll" -> Replace.parse(o, type.equals("replaceAll") && bool(o, "uniform", true), reg);
         case "propertyReplace" -> new Property(str(o, "sourceExpression", ""), str(o, "propertyName", ""), str(o, "propertyValue", ""));
         case "ensureBlocks" -> new EnsureBlocks(str(o, "sourceExpression", ""), str(o, "destExpression", ""));
         case "ruins" -> Ruins.parse(o);
         case "villagereplace" -> new VillageSpecific(sourceExpression(o));
         default -> {
            LOGGER.warn("rlgen: unknown RC transformer type {}", type);
            yield new T() {
            };
         }
      };
   }

   static List<T> parseList(JsonElement list, RcRegistry reg) {
      List<T> out = new ArrayList<>();
      if (list != null && list.isJsonArray()) {
         for (JsonElement e : list.getAsJsonArray()) {
            out.add(parse(e.getAsJsonObject(), reg));
         }
      }
      return out;
   }

   // ------------------------------------------------------------------ multi

   public static final class MultiData {
      boolean deactivated;
      final List<Object> datas = new ArrayList<>();
   }

   public static final class Multi extends T {
      final RcExpr<RcEnv> environment;
      final List<T> transformers;

      public Multi(String environment, List<T> transformers) {
         this.environment = RcEnv.envExpr(environment);
         this.transformers = transformers;
      }

      static Multi parse(JsonObject o, RcRegistry reg) {
         JsonObject data = null;
         if (o.has("dataPreset")) {
            data = reg.transformerPreset(o.get("dataPreset").getAsString());
         }
         if (data == null && o.has("data") && o.get("data").isJsonObject()) {
            data = o.getAsJsonObject("data");
         }
         if (data == null && o.has("environmentMatcher") && o.has("transformers")) {
            data = o;
         }
         if (data == null) {
            return new Multi("", new ArrayList<>());
         }
         return new Multi(str(data, "environmentMatcher", ""), parseList(data.get("transformers"), reg));
      }

      @Override
      MultiData prepare(RcContext c) {
         MultiData d = new MultiData();
         d.deactivated = !this.environment.test(c.env);
         if (!d.deactivated) {
            for (T t : this.transformers) {
               d.datas.add(t.prepare(c));
            }
         }
         return d;
      }

      @Override
      void configure(Object data, RcContext c, Run run) {
         MultiData d = (MultiData)data;
         if (!d.deactivated) {
            for (int i = 0; i < this.transformers.size(); i++) {
               this.transformers.get(i).configure(d.datas.get(i), c, run);
            }
         }
      }

      @Override
      boolean mayGenerate(Object data, RcContext c) {
         MultiData d = (MultiData)data;
         if (d.deactivated) {
            return true;
         }
         for (int i = 0; i < this.transformers.size(); i++) {
            if (!this.transformers.get(i).mayGenerate(d.datas.get(i), c)) {
               return false;
            }
         }
         return true;
      }

      @Override
      boolean skip(Object data, RcContext c, BlockPos worldPos, Src src, BlockPos srcPos) {
         MultiData d = (MultiData)data;
         if (d.deactivated) {
            return false;
         }
         for (int i = 0; i < this.transformers.size(); i++) {
            if (this.transformers.get(i).skip(d.datas.get(i), c, worldPos, src, srcPos)) {
               return true;
            }
         }
         return false;
      }

      @Override
      void transform(Object data, Phase phase, RcContext c, Run run) {
         MultiData d = (MultiData)data;
         if (!d.deactivated) {
            for (int i = 0; i < this.transformers.size(); i++) {
               this.transformers.get(i).transform(d.datas.get(i), phase, c, run);
            }
         }
      }

      @Override
      void addExtent(Object data, RcContext c, List<BlockPos> out) {
         MultiData d = (MultiData)data;
         if (!d.deactivated) {
            for (int i = 0; i < this.transformers.size(); i++) {
               this.transformers.get(i).addExtent(d.datas.get(i), c, out);
            }
         }
      }
   }

   // ------------------------------------------------------------------ clouds

   static final class CloudData {
      final Map<BlockPos, Double> cloud = new HashMap<>();
   }

   /** TransformerAbstractCloud.visitRecursively */
   static <X> boolean visitRecursively(Set<X> start, BiPredicate<Set<X>, X> changedConsumer) {
      Set<X> changedL = start;
      Set<X> changedR = new HashSet<>();
      while (!changedL.isEmpty() || !changedR.isEmpty()) {
         Set<X> current = !changedL.isEmpty() ? changedL : changedR;
         Set<X> next = current == changedL ? changedR : changedL;
         for (X x : current) {
            if (!changedConsumer.test(next, x)) {
               return false;
            }
         }
         current.clear();
      }
      return true;
   }

   abstract static class Cloud extends T {
      final RcExpr<RcBlocks.LegacyId> source;
      final RcExpr<BlockState> dest;
      final double expansionDistance;
      final double expansionRandomization;

      Cloud(String source, String dest, double expansionDistance, double expansionRandomization) {
         this.source = RcBlocks.blockExpr(source);
         this.dest = RcBlocks.worldExpr(dest);
         this.expansionDistance = expansionDistance;
         this.expansionRandomization = expansionRandomization;
      }

      abstract boolean canPenetrate(RcContext c, BlockPos worldPos, double density);

      double sideDistance(Direction side) {
         return 1.0;
      }

      abstract void transformBlock(CloudData d, RcContext c, BlockPos srcPos, BlockPos worldPos);

      @Override
      Object prepare(RcContext c) {
         return new CloudData();
      }

      @Override
      boolean skip(Object data, RcContext c, BlockPos worldPos, Src src, BlockPos srcPos) {
         return this.source.test(src.legacy());
      }

      @Override
      void configure(Object data, RcContext c, Run run) {
         CloudData d = (CloudData)data;
         RcStructureDef def = c.def;
         int[] size = def.size;
         BlurredValueField field = new BlurredValueField(size);
         int values = (int)Math.floor((float)(size[0] * size[1] * size[2]) * 0.04F + 0.5F);
         for (int i = 0; i < values; i++) {
            field.addValue(1.0F + (c.random.nextFloat() - c.random.nextFloat()) * (float)this.expansionRandomization / 100.0F, c.random);
         }
         for (int z = 0; z < size[2]; z++) {
            for (int y = 0; y < size[1]; y++) {
               for (int x = 0; x < size[0]; x++) {
                  if (this.source.test(def.legacy(x, y, z)) && this.canPenetrate(c, c.worldPos(x, y, z), 1.0)) {
                     d.cloud.put(new BlockPos(x, y, z), 1.0);
                  }
               }
            }
         }
         if (this.expansionDistance > 1.0E-6) {
            double[] falloffs = new double[6];
            List<Direction> sides = new ArrayList<>();
            for (Direction side : Direction.values()) {
               double sd = this.sideDistance(side);
               double f = sd > 1.0E-6 ? 1.0 / sd / this.expansionDistance : 0.0;
               if (f > 0.0) {
                  sides.add(side);
                  falloffs[side.ordinal()] = f;
               }
            }
            visitRecursively(new HashSet<>(d.cloud.keySet()), (changed, pos) -> {
               double density = d.cloud.get(pos);
               for (Direction side : sides) {
                  BlockPos sidePos = pos.relative(side);
                  double sideDensity = density - falloffs[side.ordinal()] * field.getValue(sidePos.getX(), sidePos.getY(), sidePos.getZ());
                  if (sideDensity > 0.0 && !(d.cloud.getOrDefault(sidePos, 0.0) >= sideDensity - 1.0E-5)
                     && this.canPenetrate(c, c.worldPos(sidePos), sideDensity)) {
                     d.cloud.put(sidePos, sideDensity);
                     changed.add(sidePos);
                  }
               }
               return true;
            });
         }
      }

      @Override
      void transform(Object data, Phase phase, RcContext c, Run run) {
         if (phase == Phase.BEFORE) {
            CloudData d = (CloudData)data;
            for (BlockPos src : d.cloud.keySet()) {
               BlockPos w = c.worldPos(src);
               if (c.includes(w)) {
                  this.transformBlock(d, c, src, w);
               }
            }
         }
      }

      @Override
      void addExtent(Object data, RcContext c, List<BlockPos> out) {
         CloudData d = (CloudData)data;
         int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
         int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
         for (BlockPos src : d.cloud.keySet()) {
            BlockPos w = c.worldPos(src);
            minX = Math.min(minX, w.getX());
            minY = Math.min(minY, w.getY());
            minZ = Math.min(minZ, w.getZ());
            maxX = Math.max(maxX, w.getX());
            maxY = Math.max(maxY, w.getY());
            maxZ = Math.max(maxZ, w.getZ());
         }
         if (minX <= maxX) {
            out.add(new BlockPos(minX - 2, minY - 2, minZ - 2));
            out.add(new BlockPos(maxX + 2, maxY + 2, maxZ + 2));
         }
      }
   }

   /** TransformerNatural: fills reccomplex:naturalFloor with the biome's ground. */
   static final class Natural extends Cloud {
      Natural(String source, String dest, double distance, double randomization) {
         super(source, dest, distance, randomization);
      }

      @Override
      double sideDistance(Direction side) {
         return side == Direction.UP ? 0.0 : (side.getAxis().isHorizontal() ? 0.5 : 1.0);
      }

      @Override
      boolean canPenetrate(RcContext c, BlockPos worldPos, double density) {
         return density >= 1.0 || this.dest.test(c.world.get(worldPos));
      }

      @Override
      void transformBlock(CloudData d, RcContext c, BlockPos srcPos, BlockPos worldPos) {
         BlockState set;
         if (worldPos.getY() < 63 - 3) {
            set = Blocks.STONE.defaultBlockState();
         } else {
            Biome biome = c.env.biome;
            set = d.cloud.containsKey(srcPos.above()) ? RcStructureDef.modern(biome.fillerBlock) : RcStructureDef.modern(biome.topBlock);
         }
         c.setBlock(worldPos, set);
      }
   }

   /** TransformerNaturalAir: carves reccomplex:negativeSpace #1 into the terrain, then drops floating foliage. */
   static final class NaturalAir extends Cloud {
      NaturalAir(String source, String dest, double distance, double randomization) {
         super(source, dest, distance, randomization);
      }

      @Override
      boolean canPenetrate(RcContext c, BlockPos worldPos, double density) {
         return this.dest.test(c.world.get(worldPos));
      }

      @Override
      void transformBlock(CloudData d, RcContext c, BlockPos srcPos, BlockPos worldPos) {
         c.setBlock(worldPos, Blocks.AIR.defaultBlockState());
      }

      @Override
      void transform(Object data, Phase phase, RcContext c, Run run) {
         super.transform(data, phase, c, run);
         if (phase != Phase.AFTER) {
            return;
         }
         CloudData d = (CloudData)data;
         Set<BlockPos> check = new LinkedHashSet<>();
         for (BlockPos pos : d.cloud.keySet()) {
            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
               if (!d.cloud.containsKey(p)) {
                  check.add(c.worldPos(p));
               }
            }
         }
         Set<BlockPos> remove = new HashSet<>();
         Set<BlockPos> start = new HashSet<>();
         for (BlockPos checking : check) {
            start.add(checking);
            boolean ok = visitRecursively(start, (changed, pos) -> {
               if (!c.world.isLive(pos)) {
                  return false;
               }
               BlockState state = c.world.get(pos);
               boolean foliage = RcBlocks.isFoliage(state);
               if (!canStay(state, c, pos)) {
                  c.setBlock(pos, Blocks.AIR.defaultBlockState());
               } else {
                  if (!foliage && !RcBlocks.isReplaceable(state)) {
                     return false;
                  }
                  if (foliage && remove.size() < 300 && remove.add(pos)) {
                     for (BlockPos n : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                        changed.add(n.immutable());
                     }
                  }
               }
               return true;
            });
            if (ok) {
               for (BlockPos p : remove) {
                  c.setBlock(p, Blocks.AIR.defaultBlockState());
               }
            }
            start.clear();
            remove.clear();
         }
      }

      static boolean canStay(BlockState state, RcContext c, BlockPos pos) {
         Block b = state.getBlock();
         if (!(b instanceof SnowLayerBlock) && !(b instanceof BushBlock)) {
            return true;
         }
         return c.level() == null || state.canSurvive(c.level(), pos);
      }
   }

   // ------------------------------------------------------------------ single block transformers

   /** TransformerNegativeSpace: keeps the world's block where reccomplex:negativeSpace sits (if dest matches). */
   static final class NegativeSpace extends T {
      final RcExpr<RcBlocks.LegacyId> source;
      final RcExpr<BlockState> dest;

      NegativeSpace(String source, String dest) {
         this.source = RcBlocks.blockExpr(source);
         this.dest = RcBlocks.worldExpr(dest);
      }

      @Override
      boolean skip(Object data, RcContext c, BlockPos worldPos, Src src, BlockPos srcPos) {
         return this.source.test(src.legacy()) && this.dest.test(c.world.get(worldPos));
      }
   }

   /** TransformerEnsureBlocks: refuses to spawn unless every matching block lands on dest (checked when suggesting). */
   static final class EnsureBlocks extends T {
      final RcExpr<RcBlocks.LegacyId> source;
      final RcExpr<BlockState> dest;

      EnsureBlocks(String source, String dest) {
         this.source = RcBlocks.blockExpr(source);
         this.dest = RcBlocks.worldExpr(dest);
      }

      @Override
      boolean mayGenerate(Object data, RcContext c) {
         if (this.dest.isEmpty()) {
            return true;
         }
         int[] size = c.def.size;
         for (int z = 0; z < size[2]; z++) {
            for (int y = 0; y < size[1]; y++) {
               for (int x = 0; x < size[0]; x++) {
                  if (this.source.test(c.def.legacy(x, y, z)) && !this.dest.test(c.world.get(c.worldPos(x, y, z)))) {
                     return false;
                  }
               }
            }
         }
         return true;
      }
   }

   abstract static class SingleBlock extends T {
      abstract boolean matches(Object data, RcContext c, Src src);

      abstract void transformBlock(Object data, RcContext c, BlockPos worldPos, Src src);

      @Override
      boolean skip(Object data, RcContext c, BlockPos worldPos, Src src, BlockPos srcPos) {
         return this.matches(data, c, src);
      }

      @Override
      void transform(Object data, Phase phase, RcContext c, Run run) {
         if (phase != Phase.BEFORE) {
            return;
         }
         int[] size = c.def.size;
         for (int x = 0; x < size[0]; x++) {
            for (int y = 0; y < size[1]; y++) {
               for (int z = 0; z < size[2]; z++) {
                  BlockPos w = c.worldPos(x, y, z);
                  if (c.includes(w)) {
                     Src src = c.def.src(x, y, z, RcTransform.ORIGINAL);
                     if (this.matches(data, c, src)) {
                        this.transformBlock(data, c, w, src);
                     }
                  }
               }
            }
         }
      }
   }

   public record Dest(BlockState state, double weight) {
   }

   /** TransformerReplace ("replace" = per block, "replaceAll" = one pick for the whole structure). */
   static final class Replace extends SingleBlock {
      final RcExpr<RcBlocks.LegacyId> source;
      final List<Dest> destination;
      final boolean uniform;

      Replace(String source, List<Dest> destination, boolean uniform) {
         this.source = RcBlocks.blockExpr(source);
         this.destination = destination;
         this.uniform = uniform;
      }

      static Replace parse(JsonObject o, boolean uniform, RcRegistry reg) {
         JsonArray dest = null;
         if (o.has("destinationPreset")) {
            dest = reg.blockPreset(o.get("destinationPreset").getAsString());
         }
         if (dest == null && o.has("destination") && o.get("destination").isJsonArray()) {
            dest = o.getAsJsonArray("destination");
         }
         List<Dest> list = new ArrayList<>();
         if (dest != null) {
            for (JsonElement e : dest) {
               JsonObject d = e.getAsJsonObject();
               list.add(new Dest(RcStructureDef.parseState(str(d, "_state", "minecraft:air")), dbl(d, "weight", 1.0)));
            }
         }
         return new Replace(sourceExpression(o), list, uniform);
      }

      Dest select(Random random) {
         if (this.destination.isEmpty()) {
            return new Dest(Blocks.AIR.defaultBlockState(), 1.0);
         }
         double total = 0.0;
         for (Dest d : this.destination) {
            total += d.weight;
         }
         return RcRegistry.selectWeighted(random, this.destination, Dest::weight, total);
      }

      @Override
      Object prepare(RcContext c) {
         return this.select(c.random);
      }

      @Override
      boolean matches(Object data, RcContext c, Src src) {
         return this.source.test(src.legacy());
      }

      @Override
      void transformBlock(Object data, RcContext c, BlockPos worldPos, Src src) {
         Dest d = this.uniform ? (Dest)data : this.select(c.random);
         RcGenerator.setBlock(c, worldPos, d.state, null, null);
      }
   }

   /** TransformerVillageSpecific: the 1.12 village block swaps (desert/savanna/taiga), only inside villages. */
   static final class VillageSpecific extends SingleBlock {
      final RcExpr<RcBlocks.LegacyId> source;

      VillageSpecific(String source) {
         this.source = RcBlocks.blockExpr(source);
      }

      @Override
      boolean matches(Object data, RcContext c, Src src) {
         Integer type = c.env.villageType;
         return type != null && src.state() != null && this.source.test(src.legacy())
            && com.nukevscity.rlgen.village.VillageBlocks.biomeSpecific(src.state(), type, null) != src.state();
      }

      @Override
      void transformBlock(Object data, RcContext c, BlockPos worldPos, Src src) {
         BlockState state = com.nukevscity.rlgen.village.VillageBlocks.biomeSpecific(src.state(), c.env.villageType, null);
         RcGenerator.setBlock(c, worldPos, c.transform.apply(state), null, null);
      }
   }

   /** TransformerProperty: "decayable" (1.12 leaves) becomes 1.21's inverted "persistent". */
   static final class Property extends SingleBlock {
      final RcExpr<RcBlocks.LegacyId> source;
      final String name;
      final String value;

      Property(String source, String name, String value) {
         this.source = RcBlocks.blockExpr(source);
         if (name.equals("decayable")) {
            this.name = "persistent";
            this.value = value.equals("true") ? "false" : "true";
         } else {
            this.name = name;
            this.value = value;
         }
      }

      BlockState with(BlockState s) {
         if (s == null) {
            return null;
         }
         for (net.minecraft.world.level.block.state.properties.Property<?> p : s.getProperties()) {
            if (p.getName().equals(this.name)) {
               return withValue(s, p, this.value);
            }
         }
         return null;
      }

      private static <V extends Comparable<V>> BlockState withValue(BlockState s, net.minecraft.world.level.block.state.properties.Property<V> p, String v) {
         return p.getValue(v).map(val -> s.setValue(p, val)).orElse(null);
      }

      @Override
      boolean matches(Object data, RcContext c, Src src) {
         return this.source.test(src.legacy()) && this.with(src.state()) != null;
      }

      @Override
      void transformBlock(Object data, RcContext c, BlockPos worldPos, Src src) {
         BlockState s = this.with(src.state());
         if (s != null) {
            c.setBlock(worldPos, s);
         }
      }
   }

   // ------------------------------------------------------------------ ruins

   static final class RuinsData {
      Double baseDecay;
      BlurredValueField surfaceField;
      BlurredValueField volumeField;
      final Set<BlockPos> fallingBlocks = new HashSet<>();

      double getDecay(BlockPos pos) {
         if (this.baseDecay == null && this.surfaceField == null && this.volumeField == null) {
            return 0.0;
         }
         double d = this.baseDecay != null ? this.baseDecay : 0.0;
         if (this.surfaceField != null) {
            int[] s = this.surfaceField.getSize();
            d += this.surfaceField.getValue(Math.min(pos.getX(), s[0]), Math.min(pos.getY(), s[1]), Math.min(pos.getZ(), s[2]));
         }
         if (this.volumeField != null) {
            d += this.volumeField.getValue(pos.getX(), pos.getY(), pos.getZ());
         }
         return d;
      }
   }

   /** TransformerRuins: decay, falling blocks, erosion, vines and cobwebs. */
   static final class Ruins extends T {
      final Direction decayDirection;
      final float minDecay, maxDecay, decayChaos, decayValueDensity;
      final boolean gravity;
      final float blockErosion, vineGrowth, cobwebGrowth;

      Ruins(Direction decayDirection, float minDecay, float maxDecay, float decayChaos, float decayValueDensity, boolean gravity, float blockErosion,
            float vineGrowth, float cobwebGrowth) {
         this.decayDirection = decayDirection;
         this.minDecay = minDecay;
         this.maxDecay = maxDecay;
         this.decayChaos = decayChaos;
         this.decayValueDensity = decayValueDensity;
         this.gravity = gravity;
         this.blockErosion = blockErosion;
         this.vineGrowth = vineGrowth;
         this.cobwebGrowth = cobwebGrowth;
      }

      static Ruins parse(JsonObject o) {
         Direction dir = Direction.byName(str(o, "decayDirection", "DOWN").toLowerCase(java.util.Locale.ROOT));
         return new Ruins(dir != null ? dir : Direction.DOWN, (float)dbl(o, "minDecay", 0.0), (float)dbl(o, "maxDecay", 0.9), (float)dbl(o, "decayChaos", 0.3),
            (float)dbl(o, "decayValueDensity", 0.04), bool(o, "gravity", true), (float)dbl(o, "blockErosion", 0.0), (float)dbl(o, "vineGrowth", 0.0),
            (float)dbl(o, "cobwebGrowth", 0.0));
      }

      /** 1.12 Material stability exponents (glass 0.1, lava 2, circuits 0.2, iron 2, wood 0.3, cloth 0.3, else 1). */
      static double stability(BlockState s) {
         if (s == null || s.isAir()) {
            return 1.0;
         }
         if (s.is(Blocks.LAVA)) {
            return 2.0;
         }
         SoundType st = s.getSoundType();
         if (st == SoundType.GLASS) {
            return 0.1;
         }
         if (st == SoundType.METAL || st == SoundType.ANVIL || st == SoundType.CHAIN) {
            return 2.0;
         }
         if (st == SoundType.WOOD || st == SoundType.WOOL || st == SoundType.LADDER) {
            return 0.3;
         }
         if (!s.blocksMotion() && !s.liquid() && !RcBlocks.isFoliage(s)) {
            return 0.2;
         }
         return 1.0;
      }

      double getDecay(RuinsData d, BlockPos src, BlockState state) {
         return Math.pow(d.getDecay(src), stability(state));
      }

      double getStability(RcStructureDef def, BlockPos src) {
         double s = this.decayDirection.getStepX() * ((double)(src.getX() + 1) / (def.size[0] + 1))
            + this.decayDirection.getStepY() * ((double)(src.getY() + 1) / (def.size[1] + 1))
            + this.decayDirection.getStepZ() * ((double)(src.getZ() + 1) / (def.size[2] + 1));
         if (s < 0.0) {
            s++;
         }
         return s;
      }

      @Override
      Object prepare(RcContext c) {
         RuinsData d = new RuinsData();
         if (this.minDecay > 0.0F || this.maxDecay > 0.0F) {
            int[] bbSize = c.transform.applySize(c.def.size);
            double chaos = (c.random.nextDouble() * 0.5 + 0.5) * this.decayChaos;
            d.baseDecay = c.random.nextDouble() * (this.maxDecay - this.minDecay) + this.minDecay;
            int[] surface = bbSize.clone();
            switch (this.decayDirection.getAxis()) {
               case X -> surface[0] = 1;
               case Y -> surface[1] = 1;
               case Z -> surface[2] = 1;
            }
            d.surfaceField = new BlurredValueField(surface);
            int surfaceValues = (int)Math.floor((float)(surface[0] * surface[1] * surface[2]) * this.decayValueDensity + 0.5);
            for (int i = 0; i < surfaceValues; i++) {
               d.surfaceField.addValue((c.random.nextDouble() - c.random.nextDouble()) * chaos * 1.5, c.random);
            }
            d.volumeField = new BlurredValueField(bbSize);
            int volumeValues = (int)Math.floor((float)(bbSize[0] * bbSize[1] * bbSize[2]) * this.decayValueDensity * 0.25 + 0.5);
            for (int i = 0; i < volumeValues; i++) {
               d.volumeField.addValue((c.random.nextDouble() - c.random.nextDouble()) * chaos * 0.5, c.random);
            }
         }
         return d;
      }

      @Override
      boolean skip(Object data, RcContext c, BlockPos worldPos, Src src, BlockPos srcPos) {
         RuinsData d = (RuinsData)data;
         if (d.fallingBlocks.contains(srcPos)) {
            return true;
         }
         double decay = this.getDecay(d, srcPos, src.state());
         if (decay < 1.0E-6) {
            return false;
         }
         return decay > 1.0 || this.getStability(c.def, srcPos) < decay;
      }

      boolean canFall(RcContext c, Run run, BlockPos worldPos, BlockPos src, Src s) {
         return !run.skip(c, worldPos, s, src) && s.state() != null && !s.state().isAir();
      }

      static boolean canLand(BlockState s) {
         return s != null && s.getPistonPushReaction() == PushReaction.NORMAL && isNormalCube(s);
      }

      static boolean isNormalCube(BlockState s) {
         return s.canOcclude() && s.isCollisionShapeFullBlock(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
      }

      @Override
      void configure(Object data, RcContext c, Run run) {
         if (!this.gravity) {
            return;
         }
         RuinsData d = (RuinsData)data;
         RcStructureDef def = c.def;
         int[] size = def.size;
         for (int z = 0; z < size[2]; z++) {
            for (int y = 0; y < size[1]; y++) {
               for (int x = 0; x < size[0]; x++) {
                  BlockPos src = new BlockPos(x, y, z);
                  Src s = def.src(x, y, z, c.transform);
                  BlockPos w = c.worldPos(src);
                  if (!run.skip(c, w, s, src) && this.canFall(c, run, w, src, s)) {
                     double stability = this.getStability(def, src);
                     double decay = this.getDecay(d, src, s.state());
                     double sq = stability * stability;
                     if (!(stability < decay) && sq * sq < decay) {
                        d.fallingBlocks.add(src);
                     }
                  }
               }
            }
         }
         Set<BlockPos> complete = new HashSet<>();
         Set<BlockPos> connected = new HashSet<>();
         boolean[] hasFloor = new boolean[1];
         for (int z = 0; z < size[2]; z++) {
            for (int y = 0; y < size[1]; y++) {
               for (int x = 0; x < size[0]; x++) {
                  BlockPos startPos = new BlockPos(x, y, z);
                  if (complete.contains(startPos)) {
                     continue;
                  }
                  hasFloor[0] = false;
                  connected.clear();
                  Set<BlockPos> start = new HashSet<>();
                  start.add(startPos);
                  visitRecursively(start, (changed, p) -> {
                     if (complete.add(p) && !d.fallingBlocks.contains(p) && def.contains(p)) {
                        Src s = def.src(p.getX(), p.getY(), p.getZ(), c.transform);
                        BlockPos w = c.worldPos(p);
                        if (this.canFall(c, run, w, p, s)) {
                           if (s.legacy().id().equals("reccomplex:generic_solid") && s.legacy().meta() == 0) {
                              hasFloor[0] = true;
                           }
                           connected.add(p);
                           for (int dx = -1; dx <= 1; dx++) {
                              for (int dy = -1; dy <= 1; dy++) {
                                 for (int dz = -1; dz <= 1; dz++) {
                                    if (dx != 0 || dy != 0 || dz != 0) {
                                       changed.add(p.offset(dx, dy, dz));
                                    }
                                 }
                              }
                           }
                        }
                     }
                     return true;
                  });
                  if (!connected.isEmpty() && connected.size() < 200 && !hasFloor[0]) {
                     d.fallingBlocks.addAll(connected);
                  }
               }
            }
         }
      }

      @Override
      void transform(Object data, Phase phase, RcContext c, Run run) {
         if (phase != Phase.AFTER) {
            return;
         }
         RuinsData d = (RuinsData)data;
         RcStructureDef def = c.def;
         for (BlockPos src : d.fallingBlocks) {
            Src s = def.src(src.getX(), src.getY(), src.getZ(), c.transform);
            if (canLand(s.state())) {
               BlockPos.MutableBlockPos dest = c.worldPos(src).mutable();
               if (!c.world.isLive(dest)) {
                  continue;
               }
               while (dest.getY() > c.level().getMinBuildHeight() && c.world.isLive(dest) && RcBlocks.isReplaceable(c.world.get(dest))) {
                  dest.move(Direction.DOWN);
               }
               dest.move(Direction.UP);
               RcGenerator.setBlock(c, dest.immutable(), s.state(), def.tileAt(src), src);
            }
         }
         if (this.blockErosion > 0.0F || this.vineGrowth > 0.0F) {
            int[] size = def.size;
            for (int z = 0; z < size[2]; z++) {
               for (int y = 0; y < size[1]; y++) {
                  for (int x = 0; x < size[0]; x++) {
                     BlockPos w = c.worldPos(x, y, z);
                     if (c.includes(w)) {
                        BlockState state = c.world.get(w);
                        Src worldSrc = new Src(RcBlocks.legacyId(state), state);
                        if (!run.skip(c, w, worldSrc, new BlockPos(x, y, z))) {
                           this.decayBlock(c, state, w);
                        }
                     }
                  }
               }
            }
         }
      }

      private static final Object[][] ERODE = {
         {Blocks.STONE_BRICKS, Blocks.STONE_BRICK_STAIRS}, {Blocks.OAK_PLANKS, Blocks.OAK_STAIRS}, {Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_STAIRS},
         {Blocks.BIRCH_PLANKS, Blocks.BIRCH_STAIRS}, {Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_STAIRS}, {Blocks.ACACIA_PLANKS, Blocks.ACACIA_STAIRS},
         {Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_STAIRS}, {Blocks.SANDSTONE, Blocks.SANDSTONE_STAIRS}, {Blocks.COBBLESTONE, Blocks.COBBLESTONE_STAIRS},
         {Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_STAIRS}, {Blocks.RED_SANDSTONE, Blocks.RED_SANDSTONE_STAIRS}, {Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_STAIRS}
      };

      void decayBlock(RcContext c, BlockState state, BlockPos pos) {
         Random random = c.random;
         BlockState newState = state;
         if (random.nextFloat() < this.blockErosion) {
            if (state.is(Blocks.STONE_BRICKS) || state.is(Blocks.CHISELED_STONE_BRICKS)) {
               newState = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            } else if (state.is(Blocks.SANDSTONE) || state.is(Blocks.CHISELED_SANDSTONE) || state.is(Blocks.CUT_SANDSTONE)) {
               newState = Blocks.SANDSTONE.defaultBlockState();
            }
         }
         for (Object[] e : ERODE) {
            if (newState.is((Block)e[0]) && random.nextFloat() < this.blockErosion * 0.125F) {
               newState = ((Block)e[1]).defaultBlockState()
                  .setValue(StairBlock.FACING, Direction.from2DDataValue(random.nextInt(4)))
                  .setValue(StairBlock.HALF, random.nextBoolean() ? Half.BOTTOM : Half.TOP);
            }
         }
         if (random.nextFloat() < this.vineGrowth) {
            if (newState.is(Blocks.STONE_BRICKS)) {
               newState = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            } else if (newState.is(Blocks.COBBLESTONE)) {
               newState = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            } else if (newState.is(Blocks.COBBLESTONE_WALL)) {
               newState = Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState();
            }
         }
         if (newState.isAir()) {
            newState = null;
            BoundingBox2 relevant = new BoundingBox2(c);
            outer:
            for (Direction direction : new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST}) {
               if (random.nextFloat() < this.vineGrowth && relevant.contains(pos.relative(direction.getOpposite())) && canVine(c, pos, direction)) {
                  BlockState down = c.world.get(pos.below());
                  down = down.is(Blocks.VINE) ? down : Blocks.VINE.defaultBlockState();
                  down = down.setValue(VineBlock.getPropertyForFace(direction.getOpposite()), true);
                  int length = 1 + random.nextInt((int)Math.floor(this.vineGrowth * 10.0F + 3.0F));
                  for (int y = 0; y < length; y++) {
                     BlockPos downPos = pos.below(y);
                     if (!c.world.isLive(downPos) || !c.world.get(downPos).isAir()) {
                        break outer;
                     }
                     c.setBlock(downPos, down);
                  }
                  break;
               }
               if (random.nextFloat() < this.cobwebGrowth && hasSolidNeighbors(c, pos, 3)) {
                  c.setBlock(pos, Blocks.COBWEB.defaultBlockState());
               }
            }
         }
         if (newState != null && newState != state) {
            c.setBlock(pos, newState);
         }
      }

      static boolean canVine(RcContext c, BlockPos pos, Direction direction) {
         BlockPos n = pos.relative(direction.getOpposite());
         return c.world.isLive(n) && c.level() != null && c.world.get(n).isFaceSturdy(c.level(), n, direction);
      }

      /** TransformerRuins.hasAirNeighbors (which really counts full blocks). */
      static boolean hasSolidNeighbors(RcContext c, BlockPos pos, int sides) {
         int num = 0;
         int neg = 0;
         for (Direction facing : Direction.values()) {
            BlockPos n = pos.relative(facing);
            if (c.world.isLive(n) && isNormalCube(c.world.get(n))) {
               num++;
            } else {
               neg++;
            }
            if (num >= sides) {
               return true;
            }
            if (neg > 6 - sides) {
               return false;
            }
         }
         return false;
      }

      /** context.boundingBox intersected with the chunk being generated. */
      record BoundingBox2(RcContext c) {
         boolean contains(BlockPos p) {
            return this.c.bb.isInside(p) && this.c.includes(p);
         }
      }
   }

   // ------------------------------------------------------------------ helpers for leaves (used by naturalAir foliage removal)

   static boolean isLeaves(BlockState s) {
      return s.getBlock() instanceof LeavesBlock;
   }
}
