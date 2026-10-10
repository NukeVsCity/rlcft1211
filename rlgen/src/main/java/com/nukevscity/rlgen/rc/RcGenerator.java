package com.nukevscity.rlgen.rc;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.legacy.biome.Biome;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.slf4j.Logger;

/** RC's WorldGenStructures + StructureGenerator + GenericStructure.generate, split into planning (oracle) and per-chunk spawning (real world). */
public final class RcGenerator {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final long POS_SEED = 1298319823120938102L;
   public static final long PLACE_SEED = 1923419028309182309L;
   public static final long PREPARE_SEED = 2482039482903842269L;
   public static final long GENERATE_SEED = 2309842093742837432L;
   public static final long TRANSFORM_SEED = 1283901823092812394L;
   private static final int MAX_LAYERS = 30;
   private static final boolean DEBUG = Boolean.getBoolean("rlgen.rcdebug");

   private RcGenerator() {
   }

   /** Terrain and biomes of the 1.12 world (the legacy generator), for planning. */
   public interface Oracle extends RcWorldView {
      Biome biome(int x, int z);
   }

   // ------------------------------------------------------------------ instances

   /** A prepared structure (GenericStructure.InstanceData + where it goes). */
   public static final class Instance {
      public final RcStructureDef def;
      public final String genId;
      public final long seed;
      public final RcTransform transform;
      public final BlockPos lower;
      public final BoundingBox bb;
      final RcEnv env;
      final int layer;
      final Map<String, Boolean> vars = new HashMap<>();
      final RcTransformers.Multi fused;
      final RcTransformers.MultiData fusedData;
      final Map<BlockPos, Object> scripts = new LinkedHashMap<>();
      final List<Instance> children = new ArrayList<>();

      Instance(RcStructureDef def, String genId, long seed, RcTransform transform, BlockPos lower, RcEnv env, int layer) {
         this.def = def;
         this.genId = genId;
         this.seed = seed;
         this.transform = transform;
         this.lower = lower;
         int[] s = transform.applySize(def.size);
         this.bb = new BoundingBox(lower.getX(), lower.getY(), lower.getZ(), lower.getX() + s[0] - 1, lower.getY() + s[1] - 1, lower.getZ() + s[2] - 1);
         this.env = env;
         this.layer = layer;
         this.fused = new RcTransformers.Multi("", List.of(new Behavior(), def.transformer));
         this.fusedData = new RcTransformers.MultiData();
      }

      public RcEnv env() {
         return this.env;
      }

      /** Everything this structure (and its children) may write. */
      public BoundingBox extent(Oracle oracle) {
         List<BlockPos> pts = new ArrayList<>();
         pts.add(new BlockPos(this.bb.minX(), this.bb.minY(), this.bb.minZ()));
         pts.add(new BlockPos(this.bb.maxX(), this.bb.maxY(), this.bb.maxZ()));
         RcContext c = new RcContext(this.def, this.transform, this.lower, this.env, new Random(0L), oracle, this.layer, false, null);
         this.fused.addExtent(this.fusedData, c, pts);
         BoundingBox box = BoundingBox.encapsulatingPositions(pts).orElse(this.bb);
         for (Instance child : this.children) {
            box = box.encapsulate(child.extent(oracle));
         }
         return box;
      }
   }

   // ------------------------------------------------------------------ planning (WorldGenStructures.decorate)

   /** One root structure planned in a chunk. */
   public record Planned(String structure, String genId, long seed, RcTransform transform, BlockPos lower, Instance instance) {
   }

   /** The structures RC would generate when populating this chunk. */
   public static List<Planned> planChunk(long worldSeed, int cx, int cz, Oracle oracle) {
      RcRegistry reg = RcRegistry.get();
      // ChunkGeneratorOverworld.populate's random, as handed to PopulateChunkEvent.Pre
      Random rand = new Random(worldSeed);
      long k = rand.nextLong() / 2L * 2L + 1L;
      long l = rand.nextLong() / 2L * 2L + 1L;
      rand.setSeed((long)cx * k + (long)cz * l ^ worldSeed);
      Biome biomeGen = oracle.biome(cx * 16 + 8, cz * 16 + 8);
      RcRegistry.Selector selector = reg.selector(biomeGen);
      List<Planned> out = new ArrayList<>();
      if (selector.isEmpty()) {
         return out;
      }
      List<RcRegistry.Entry> generated = reg.generatedStructures(selector, rand, oracle.biome(cx * 16, cz * 16));
      for (RcRegistry.Entry e : generated) {
         long seed = rand.nextLong();
         try {
            Planned p = plan(cx, cz, e.def(), e.gen(), seed, oracle, out);
            if (p != null) {
               out.add(p);
               if (DEBUG) {
                  LOGGER.info("rlgen: RC {} at {} ({} rot {} mirror {})", p.structure(), p.lower(), e.gen().category, p.transform().rotation(),
                     p.transform().mirrorX());
               }
            } else if (DEBUG) {
               LOGGER.info("rlgen: RC {} failed in chunk {} {}", e.def().id, cx, cz);
            }
         } catch (RuntimeException ex) {
            LOGGER.error("rlgen: RC structure {} failed to plan", e.def().id, ex);
         }
      }
      return out;
   }

   static BlockPos center(BoundingBox bb) {
      return new BlockPos(bb.minX() + (bb.maxX() - bb.minX() + 1) / 2, bb.minY() + (bb.maxY() - bb.minY() + 1) / 2,
         bb.minZ() + (bb.maxZ() - bb.minZ() + 1) / 2);
   }

   static RcTransform transformFor(RcStructureDef def, long seed) {
      Random random = new Random(seed ^ TRANSFORM_SEED);
      int rotation = def.rotatable ? random.nextInt(4) : 0;
      boolean mirror = def.mirrorable && random.nextBoolean();
      return RcTransform.from(rotation, mirror);
   }

   /** WorldGenStructures.planStructureInChunk (maturity SUGGEST, from center). */
   static Planned plan(int cx, int cz, RcStructureDef def, RcStructureDef.NaturalGen gen, long seed, Oracle oracle, List<Planned> others) {
      Random posRandom = new Random(seed ^ POS_SEED);
      int x = cx * 16 + posRandom.nextInt(16) + 8;
      int z = cz * 16 + posRandom.nextInt(16) + 8;
      return planAt(def, gen, x, z, seed, oracle, others, true);
   }

   static Planned planAt(RcStructureDef def, RcStructureDef.NaturalGen gen, int x, int z, long seed, Oracle oracle, List<Planned> others,
                         boolean checkBiome) {
      RcTransform t = transformFor(def, seed);
      int[] size = t.applySize(def.size);
      BlockPos lower0 = new BlockPos(x - size[0] / 2, 0, z - size[2] / 2);
      BoundingBox surfaceBB = new BoundingBox(lower0.getX(), 0, lower0.getZ(), lower0.getX() + size[0] - 1, size[1] - 1, lower0.getZ() + size[2] - 1);
      BlockPos c = center(surfaceBB);
      Biome biome = oracle.biome(c.getX(), c.getZ());
      if (checkBiome && gen.weight(biome) <= 0.0) {
         return fail(def, "biome edge " + biome);
      }
      int y = gen.placer.place(new RcPlacer.PlaceCtx(def, t, lower0, size, new Random(seed ^ PLACE_SEED), oracle));
      if (y < 0) {
         return fail(def, "no place");
      }
      BlockPos lower = lower0.above(y);
      BoundingBox bb = new BoundingBox(lower.getX(), lower.getY(), lower.getZ(), lower.getX() + size[0] - 1, lower.getY() + size[1] - 1,
         lower.getZ() + size[2] - 1);
      if (bb.minY() < 1 || bb.maxY() > RcPlacer.HEIGHT - 1 - 1) {
         return fail(def, "out of bounds y " + bb.minY() + ".." + bb.maxY());
      }
      if (def.blocking) {
         for (Planned o : others) {
            if (o.instance.def.blocking && o.instance.bb.intersects(bb)) {
               return fail(def, "overlap"); // avoidOverlappingGeneration (only against this chunk's structures)
            }
         }
      }
      RcEnv env = new RcEnv(biome, "natural", gen.id, null);
      Instance inst = prepare(def, gen.id, seed, t, lower, env, oracle, 0, true);
      return inst == null ? fail(def, "ensureBlocks") : new Planned(def.id, gen.id, seed, t, lower, inst);
   }

   /** For /rlgen rc: the structure's first natural generation (or the surface placer) centred on {@code at}. */
   public static Instance planAt(RcStructureDef def, BlockPos at, long seed, Oracle oracle) {
      RcStructureDef.NaturalGen gen = def.natural.isEmpty() ? null : def.natural.get(0);
      if (gen == null) {
         gen = new RcStructureDef.NaturalGen();
         gen.id = "";
         gen.category = "decoration";
         gen.biomes = List.of();
         gen.dimensions = List.of();
         gen.placer = RcPlacer.surface();
      }
      Planned p = planAt(def, gen, at.getX(), at.getZ(), seed, oracle, List.of(), false);
      return p == null ? null : p.instance;
   }

   private static Planned fail(RcStructureDef def, String why) {
      if (DEBUG) {
         LOGGER.info("rlgen: RC {} not placed: {}", def.id, why);
      }
      return null;
   }

   /** Rebuilds the instance of a planned root structure (same result as planning: everything comes from the seed and the oracle). */
   public static Instance prepareRoot(String structure, String genId, long seed, RcTransform t, BlockPos lower, Oracle oracle) {
      RcStructureDef def = RcRegistry.get().structure(structure);
      if (def == null) {
         return null;
      }
      int[] size = t.applySize(def.size);
      BlockPos c = new BlockPos(lower.getX() + size[0] / 2, 0, lower.getZ() + size[2] / 2);
      RcEnv env = new RcEnv(oracle.biome(c.getX(), c.getZ()), "natural", genId, null);
      return prepare(def, genId, seed, t, lower, env, oracle, 0, true);
   }

   // ------------------------------------------------------------------ prepare (GenericStructure.prepareInstanceData)

   static Instance prepare(RcStructureDef def, String genId, long seed, RcTransform t, BlockPos lower, RcEnv env, Oracle oracle, int layer,
                           boolean suggest) {
      Instance inst = new Instance(def, genId, seed, t, lower, env, layer);
      RcContext c = new RcContext(def, t, lower, env, new Random(seed ^ PREPARE_SEED), oracle, layer, suggest, null);
      inst.vars.putAll(env.variables);
      // GenericVariableDomain.fill (its early returns kept)
      for (RcStructureDef.Variable v : def.variables) {
         if (env.variables.containsKey(v.id())) {
            inst.vars.put(v.id(), env.variables.get(v.id()));
            break;
         }
         if (inst.vars.containsKey(v.id())) {
            env.variables.put(v.id(), inst.vars.get(v.id()));
            break;
         }
         boolean value = c.random.nextFloat() < v.chance() && v.condition().test(env);
         inst.vars.put(v.id(), value);
         env.variables.put(v.id(), value);
      }
      // fused transformer: GenerationBehavior (scripts), the structure's own transformer
      Behavior behavior = (Behavior)inst.fused.transformers.get(0);
      inst.fusedData.datas.add(behavior.prepare(inst, c, oracle));
      inst.fusedData.datas.add(def.transformer.prepare(c));
      if (suggest && !inst.fused.mayGenerate(inst.fusedData, c)) {
         return null;
      }
      RcTransformers.Run run = new RcTransformers.Run(inst.fused, inst.fusedData);
      inst.fused.configure(inst.fusedData, c, run);
      return inst;
   }

   // ------------------------------------------------------------------ GenerationBehavior + world scripts

   record CommandEntry(CompoundTag tag, double weight) {
   }

   record CommandData(List<CommandEntry> entries) {
   }

   record MultiScriptData(boolean deactivated, List<Object> datas) {
   }

   record StructureScriptData(Instance child) {
   }

   static final class Behavior extends RcTransformers.T {
      Object prepare(Instance inst, RcContext c, Oracle oracle) {
         for (RcStructureDef.Tile tile : inst.def.tiles) {
            if (tile.script() != null) {
               Object d = prepareScript(tile.script(), inst, c, c.worldPos(tile.pos()), oracle);
               if (d != null) {
                  inst.scripts.put(tile.pos(), d);
               }
            }
         }
         return inst;
      }

      @Override
      boolean skip(Object data, RcContext c, BlockPos worldPos, RcTransformers.Src src, BlockPos srcPos) {
         return src.state() == null; // UnstableBlock.shouldSkipState: RC's own blocks never generate
      }

      @Override
      void transform(Object data, RcTransformers.Phase phase, RcContext c, RcTransformers.Run run) {
         if (phase != RcTransformers.Phase.AFTER) {
            return;
         }
         Instance inst = (Instance)data;
         if (c.layer >= MAX_LAYERS) {
            LOGGER.warn("rlgen: RC structure generated with over max layers");
            return;
         }
         for (Map.Entry<BlockPos, Object> e : inst.scripts.entrySet()) {
            generateScript(e.getValue(), c, c.worldPos(e.getKey()));
         }
      }

      @Override
      void addExtent(Object data, RcContext c, List<BlockPos> out) {
      }
   }

   static Object prepareScript(CompoundTag script, Instance inst, RcContext c, BlockPos pos, Oracle oracle) {
      List<Object> datas = new ArrayList<>();
      for (Tag t : script.getList("scripts", Tag.TAG_COMPOUND)) {
         datas.add(prepareOne((CompoundTag)t, inst, c, pos, oracle));
      }
      boolean deactivated = !RcEnv.envExpr(script.getString("environment")).test(c.env);
      return new MultiScriptData(deactivated, datas);
   }

   static Object prepareOne(CompoundTag s, Instance inst, RcContext c, BlockPos pos, Oracle oracle) {
      switch (s.getString("type")) {
         case "multi":
            return prepareScript(s.getCompound("inner"), inst, c, pos, oracle);
         case "command": {
            List<CommandEntry> entries = new ArrayList<>();
            for (Tag t : s.getList("commands", Tag.TAG_COMPOUND)) {
               CompoundTag ct = (CompoundTag)t;
               entries.add(new CommandEntry(ct, ct.contains("weight") ? ct.getDouble("weight") : 1.0));
            }
            return new CommandData(entries);
         }
         case "structure":
            return prepareStructureScript(s, inst, c, pos, oracle);
         default:
            return null;
      }
   }

   /** WorldScriptStructureGenerator.prepareInstanceData */
   static Object prepareStructureScript(CompoundTag s, Instance parent, RcContext c, BlockPos pos, Oracle oracle) {
      Random random = c.random;
      RcTransform transform = c.transform;
      RcRegistry reg = RcRegistry.get();
      ListTag shiftTag = s.getList("shift", Tag.TAG_INT);
      BlockPos shift = shiftTag.size() == 3 ? new BlockPos(shiftTag.getInt(0), shiftTag.getInt(1), shiftTag.getInt(2)) : BlockPos.ZERO;
      RcStructureDef def;
      String genId = null;
      RcTransform childT;
      BlockPos coord;
      if (s.getBoolean("simple")) {
         ListTag names = s.getList("structures", Tag.TAG_STRING);
         if (names.isEmpty()) {
            return null;
         }
         def = reg.structure(names.getString(random.nextInt(names.size())));
         if (def == null) {
            return null;
         }
         int rotations = def.rotatable ? (s.contains("rotation") ? transform.rotation() + s.getInt("rotation") : random.nextInt(4)) : 0;
         boolean mirror = def.mirrorable && (s.contains("mirror") ? transform.mirrorX() != s.getBoolean("mirror") : random.nextBoolean());
         childT = RcTransform.from(rotations, mirror);
         coord = transform.apply(shift, new int[]{1, 1, 1}).subtract(transform.apply(BlockPos.ZERO, def.size)).offset(pos);
      } else {
         Direction front = s.contains("front") ? Direction.byName(s.getString("front").toLowerCase(java.util.Locale.ROOT)) : null;
         List<Map.Entry<RcStructureDef, RcStructureDef.ListGen>> items = reg.listStructures(s.getString("list"), front);
         if (items.isEmpty()) {
            return null;
         }
         Map.Entry<RcStructureDef, RcStructureDef.ListGen> pick = RcRegistry.selectWeighted(random, items, e -> e.getValue().weight());
         def = pick.getKey();
         RcStructureDef.ListGen gen = pick.getValue();
         genId = gen.id();
         boolean mirror;
         int rotations;
         if (front != null) {
            Direction curFront = transform.apply(front);
            mirror = def.mirrorable && def.rotatable && random.nextBoolean();
            Integer needed = RcTransform.clockwiseRotations(gen.front(), curFront, mirror);
            rotations = needed != null ? needed : 0;
         } else {
            mirror = def.mirrorable && random.nextBoolean();
            rotations = def.rotatable ? random.nextInt(4) : 0;
         }
         childT = RcTransform.from(rotations, mirror);
         coord = transform.apply(shift.offset(gen.shift()), new int[]{1, 1, 1}).subtract(transform.apply(BlockPos.ZERO, def.size)).offset(pos);
      }
      long seed = random.nextLong();
      Instance child = prepare(def, genId, seed, childT, coord, c.env, oracle, parent.layer + 1, false);
      if (child == null) {
         return null;
      }
      parent.children.add(child);
      return new StructureScriptData(child);
   }

   static void generateScript(Object data, RcContext c, BlockPos pos) {
      if (data instanceof MultiScriptData m) {
         if (!m.deactivated) {
            for (Object d : m.datas) {
               generateScript(d, c, pos);
            }
         }
      } else if (data instanceof CommandData cd) {
         if (c.includes(pos) && !cd.entries.isEmpty()) {
            CommandEntry entry = RcRegistry.selectWeighted(c.random, cd.entries, CommandEntry::weight);
            runCommand(entry.tag, c, pos);
         }
      } else if (data instanceof StructureScriptData sd) {
         Instance child = sd.child;
         // StructureGenerator.asChild: a seed from the parent's random, a fresh environment with the same biome
         long seed = c.random.nextLong();
         spawn(child, c.world, c.genBB, seed, c.env.child());
      }
   }

   static double coord(String s, double base, boolean center) {
      if (s.startsWith("~")) {
         return s.length() == 1 ? base : base + Double.parseDouble(s.substring(1));
      }
      double v = Double.parseDouble(s);
      return center && !s.contains(".") ? v + 0.5 : v;
   }

   static void runCommand(CompoundTag cmd, RcContext c, BlockPos pos) {
      WorldGenLevel level = c.level();
      if (level == null) {
         return;
      }
      try {
         ListTag off = cmd.getList("offset", Tag.TAG_STRING);
         switch (cmd.getString("type")) {
            case "summon": {
               double x = coord(off.getString(0), pos.getX() + 0.5, true);
               double y = coord(off.getString(1), pos.getY() + 0.5, false);
               double z = coord(off.getString(2), pos.getZ() + 0.5, true);
               if (!c.includes(BlockPos.containing(x, y, z))) {
                  return;
               }
               CompoundTag tag = cmd.getCompound("nbt").copy();
               boolean plain = tag.isEmpty();
               tag.putString("id", cmd.getString("entity"));
               Entity e = EntityType.loadEntityRecursive(tag, level.getLevel(), en -> {
                  en.moveTo(x, y, z, en.getYRot(), en.getXRot());
                  return en;
               });
               if (e != null) {
                  if (plain && e instanceof Mob mob) {
                     mob.finalizeSpawn(level, level.getCurrentDifficultyAt(e.blockPosition()), MobSpawnType.COMMAND, null);
                  }
                  level.addFreshEntityWithPassengers(e);
               }
               break;
            }
            case "setblock": {
               BlockPos p = BlockPos.containing(coord(off.getString(0), pos.getX(), false), coord(off.getString(1), pos.getY(), false),
                  coord(off.getString(2), pos.getZ(), false));
               BlockState state = RcStructureDef.parseState(cmd.getString("state"));
               CompoundTag nbt = cmd.contains("nbt") ? cmd.getCompound("nbt") : null;
               setBlock(c, p, state, nbt == null ? null : new RcStructureDef.Tile(BlockPos.ZERO, nbt, List.of(), null), null);
               break;
            }
            default:
         }
      } catch (RuntimeException ex) {
         LOGGER.debug("rlgen: RC command failed: {}", cmd, ex);
      }
   }

   // ------------------------------------------------------------------ spawning (GenericStructure.generate, clipped to one chunk)

   /** Generates the part of {@code inst} inside {@code genBB}. */
   public static void spawn(Instance inst, RcWorldView world, BoundingBox genBB, long seed, RcEnv env) {
      RcStructureDef def = inst.def;
      env.variables.putAll(inst.vars);
      RcContext c = new RcContext(def, inst.transform, inst.lower, env, new Random(seed ^ GENERATE_SEED), world, inst.layer, false, genBB);
      RcTransformers.Run run = new RcTransformers.Run(inst.fused, inst.fusedData);
      inst.fused.transform(inst.fusedData, RcTransformers.Phase.BEFORE, c, run);
      int[] size = def.size;
      if (inst.bb.intersects(genBB)) {
         for (int pass = 0; pass < 2; pass++) {
            for (int x = 0; x < size[0]; x++) {
               for (int z = 0; z < size[2]; z++) {
                  BlockPos col = c.worldPos(x, 0, z);
                  if (col.getX() < genBB.minX() || col.getX() > genBB.maxX() || col.getZ() < genBB.minZ() || col.getZ() > genBB.maxZ()) {
                     continue;
                  }
                  for (int y = 0; y < size[1]; y++) {
                     RcStructureDef.Palette p = def.palette(x, y, z);
                     if (p.state() == null && pass == 1) {
                        continue;
                     }
                     BlockState state = p.state() == null ? null : inst.transform.apply(p.state());
                     if (state != null && pass != pass(state)) {
                        continue;
                     }
                     BlockPos w = col.above(y);
                     BlockPos src = new BlockPos(x, y, z);
                     if (!run.skip(c, w, new RcTransformers.Src(p.legacy(), state), src)) {
                        setBlock(c, w, state, def.tileAt(src), src);
                     }
                  }
               }
            }
         }
      }
      inst.fused.transform(inst.fusedData, RcTransformers.Phase.AFTER, c, run);
      if (c.level() != null) {
         for (RcStructureDef.EntityEntry e : def.entities) {
            spawnEntity(c, e);
         }
      }
   }

   static int pass(BlockState s) {
      return s.isAir() || s.canOcclude() && s.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) ? 0 : 1;
   }

   /** GenericStructure.setBlock: the block, its tile entity data and RC loot. */
   static void setBlock(RcContext c, BlockPos pos, BlockState state, RcStructureDef.Tile tile, BlockPos src) {
      if (state == null || !c.setBlock(pos, state)) {
         return;
      }
      WorldGenLevel level = c.level();
      if (tile == null || level == null) {
         return;
      }
      if (!level.getBlockState(pos).is(state.getBlock())) {
         return;
      }
      BlockEntity be = level.getBlockEntity(pos);
      if (be == null) {
         return;
      }
      if (tile.nbt() != null) {
         CompoundTag t = tile.nbt().copy();
         t.putInt("x", pos.getX());
         t.putInt("y", pos.getY());
         t.putInt("z", pos.getZ());
         try {
            be.loadWithComponents(t, level.registryAccess());
         } catch (RuntimeException ex) {
            LOGGER.debug("rlgen: bad block entity data at {}", pos, ex);
         }
      }
      if (!tile.loot().isEmpty() && be instanceof Container inv) {
         RcLoot.generate(inv, tile.loot(), c.random, level, pos);
      }
      be.setChanged();
   }

   static void spawnEntity(RcContext c, RcStructureDef.EntityEntry e) {
      int[] size = c.def.size;
      double[] p = c.transform.apply(e.pos(), size);
      double x = p[0] + c.lower.getX();
      double y = p[1] + c.lower.getY();
      double z = p[2] + c.lower.getZ();
      if (!c.includes(BlockPos.containing(x, y, z))) {
         return;
      }
      CompoundTag tag = e.nbt().copy();
      if (tag.contains("TileX")) {
         BlockPos tile = c.worldPos(tag.getInt("TileX"), tag.getInt("TileY"), tag.getInt("TileZ"));
         tag.putInt("TileX", tile.getX());
         tag.putInt("TileY", tile.getY());
         tag.putInt("TileZ", tile.getZ());
         if (tag.contains("Facing")) {
            tag.putByte("Facing", (byte)c.transform.apply(Direction.from3DDataValue(tag.getByte("Facing"))).get3DDataValue());
         }
         if (tag.contains("facing")) {
            tag.putByte("facing", (byte)c.transform.apply(Direction.from2DDataValue(tag.getByte("facing"))).get2DDataValue());
         }
      }
      ListTag pos = new ListTag();
      pos.add(DoubleTag.valueOf(x));
      pos.add(DoubleTag.valueOf(y));
      pos.add(DoubleTag.valueOf(z));
      tag.put("Pos", pos);
      ListTag rot = tag.getList("Rotation", Tag.TAG_FLOAT);
      float yaw = rot.size() > 0 ? rot.getFloat(0) : 0.0F;
      float pitch = rot.size() > 1 ? rot.getFloat(1) : 0.0F;
      ListTag nrot = new ListTag();
      nrot.add(FloatTag.valueOf(c.transform.applyYaw(yaw)));
      nrot.add(FloatTag.valueOf(pitch));
      tag.put("Rotation", nrot);
      try {
         Entity entity = EntityType.loadEntityRecursive(tag, c.level().getLevel(), Function.identity());
         if (entity != null) {
            c.level().addFreshEntityWithPassengers(entity);
         }
      } catch (RuntimeException ex) {
         LOGGER.debug("rlgen: can't spawn RC entity {}", tag, ex);
      }
   }

   /** WorldGenStructures.chunkSeed */
   public static long chunkSeed(long seed, ChunkPos pos) {
      return (long)pos.x * 341873128712L + (long)pos.z * 132897987541L + seed;
   }

   static ResourceLocation id(String s) {
      return ResourceLocation.parse(s);
   }

   static boolean entityExists(String id) {
      return BuiltInRegistries.ENTITY_TYPE.containsKey(id(id));
   }
}
