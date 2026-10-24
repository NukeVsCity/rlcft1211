package com.nukevscity.rlgen.quark;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.iaf.Mat112;
import com.nukevscity.rlgen.rc.BiomeDict;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Quark r1.6 underground biomes with RLCraft's settings: big ellipsoids in caves whose floors, ceilings and walls become lush grass,
 * sandstone, slime clay, elder prismarine, spider nests, overgrown, ice, firestone or glowcelium. Like Quark it runs from the DIRT
 * ore event (after the revamped stones), over the populate area, as a MultiChunkFeatureGenerator per biome.
 */
public final class UndergroundBiomes {
   private static final Direction[] HORIZONTALS = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};

   private enum Kind {
      LUSH("UndergroundBiomeLush", 20, 50, 55, 26, 14, 5, 5, 26, 14, "JUNGLE", "SWAMP"),
      SANDSTONE("UndergroundBiomeSandstone", 40, 12, 40, 32, 14, 12, 12, 32, 14, "SANDY"),
      SLIME("UndergroundBiomeSlime", 90, 10, 40, 26, 14, 12, 6, 26, 14, "SWAMP"),
      PRISMARINE("UndergroundBiomePrismarine", 100, 10, 40, 26, 14, 12, 6, 26, 14, "OCEAN"),
      SPIDER("UndergroundBiomeSpiderNest", 150, 12, 30, 10, 20, 12, 6, 10, 20, "PLAINS", "FOREST"),
      OVERGROWN("UndergroundBiomeOvergrown", 60, 50, 55, 26, 14, 5, 5, 26, 14, "FOREST", "JUNGLE"),
      ICY("UndergroundBiomeIcy", 30, 1, 40, 32, 14, 12, 12, 32, 14, "COLD"),
      LAVA("UndergroundBiomeLava", 80, 10, 40, 26, 14, 12, 6, 26, 14, "MESA", "DESERT"),
      GLOWSHROOM("UndergroundBiomeGlowshroom", 80, 10, 32, 14, 14, 6, 6, 14, 14, "MOUNTAIN", "MUSHROOM");

      final long seedXor;
      final int rarity, minY, maxY, minX, varX, minYSize, varY, minZ, varZ;
      final Set<String> types;

      Kind(String cls, int rarity, int minY, int maxY, int minX, int varX, int minYSize, int varY, int minZ, int varZ, String... types) {
         this.seedXor = (long)("class vazkii.quark.world.world.underground." + cls).hashCode();
         this.rarity = rarity;
         this.minY = minY;
         this.maxY = maxY;
         this.minX = minX;
         this.varX = varX;
         this.minYSize = minYSize;
         this.varY = varY;
         this.minZ = minZ;
         this.varZ = varZ;
         this.types = Set.of(types);
      }

      boolean mimicInside() {
         return this == ICY || this == LAVA;
      }

      double dungeonChance() {
         return this == SANDSTONE ? 0.01 : this == SPIDER ? 0.5 : -1.0;
      }
   }

   private static final class Context {
      final List<BlockPos> floorList = new LinkedList<>();
      final List<BlockPos> ceilingList = new LinkedList<>();
      final List<BlockPos> insideList = new LinkedList<>();
      final Map<BlockPos, Direction> wallMap = new HashMap<>();
   }

   private UndergroundBiomes() {
   }

   public static void generate(DWorld w, int chunkX, int chunkZ) {
      for (Kind k : Kind.values()) {
         generate(w, k, chunkX, chunkZ);
      }
   }

   /** MultiChunkFeatureGenerator.generate */
   private static void generate(DWorld w, Kind k, int chunkX, int chunkZ) {
      int radius = Math.max(k.minX + k.varX, k.minZ + k.varZ);
      int chunkRadius = (int)Math.ceil((double)radius / 16.0);
      long worldSeed = w.seed() ^ k.seedXor;
      Random worldRandom = new Random(worldSeed);
      long xSeed = worldRandom.nextLong() >> 3;
      long zSeed = worldRandom.nextLong() >> 3;
      for (int x = chunkX - chunkRadius; x <= chunkX + chunkRadius; x++) {
         for (int z = chunkZ - chunkRadius; z <= chunkZ + chunkRadius; z++) {
            Random chunkRandom = new Random(xSeed * (long)x + zSeed * (long)z ^ worldSeed);
            if (k.rarity > 0 && chunkRandom.nextInt(k.rarity) == 0) {
               BlockPos src = new BlockPos(x * 16 + chunkRandom.nextInt(16), k.minY + chunkRandom.nextInt(k.maxY - k.minY), z * 16 + chunkRandom.nextInt(16));
               if (valid(w, k, src)) {
                  int rx = k.minX + chunkRandom.nextInt(k.varX);
                  int ry = k.minYSize + chunkRandom.nextInt(k.varY);
                  int rz = k.minZ + chunkRandom.nextInt(k.varZ);
                  apply(w, k, src, chunkRandom, chunkX, chunkZ, rx, ry, rz);
               }
            }
         }
      }
   }

   private static boolean valid(DWorld w, Kind k, BlockPos src) {
      Set<String> t = BiomeDict.TYPES.get(w.biome(src).getRegistryName());
      if (t == null) {
         return false;
      }
      for (String s : k.types) {
         if (t.contains(s)) {
            return true; // sandstone: "Allow in Mesa biomes" is on
         }
      }
      return false;
   }

   private static void apply(DWorld w, Kind k, BlockPos center, Random random, int chunkX, int chunkZ, int radiusX, int radiusY, int radiusZ) {
      double rx2 = (double)(radiusX * radiusX);
      double ry2 = (double)(radiusY * radiusY);
      double rz2 = (double)(radiusZ * radiusZ);
      Random rand = new Random(w.seed() ^ center.asLong() ^ ((long)chunkX << 20) ^ chunkZ); // world.rand
      Context context = new Context();
      int minY = Math.max(1, center.getY() - radiusY);
      int maxY = Math.min(255, center.getY() + radiusY);
      BlockPos first = new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8);
      for (int ix = 0; ix < 16; ix++) {
         for (int y = minY; y < maxY; y++) {
            for (int iz = 0; iz < 16; iz++) {
               BlockPos pos = first.offset(ix, y, iz);
               int x = pos.getX() - center.getX();
               int dy = pos.getY() - center.getY();
               int z = pos.getZ() - center.getZ();
               if ((double)(x * x) / rx2 + (double)(dy * dy) / ry2 + (double)(z * z) / rz2 <= 1.0) {
                  fill(w, k, rand, pos, context);
               }
            }
         }
      }
      for (BlockPos p : context.floorList) {
         finalFloorPass(w, k, rand, p);
      }
      for (BlockPos p : context.ceilingList) {
         finalCeilingPass(w, k, rand, p);
      }
      for (BlockPos p : context.wallMap.keySet()) {
         finalWallPass(w, k, rand, p);
      }
      if (k.dungeonChance() > 0 && random.nextDouble() < k.dungeonChance()) {
         List<BlockPos> candidates = new ArrayList<>(context.wallMap.keySet());
         candidates.removeIf(p -> {
            BlockPos down = p.below();
            BlockState s = w.get(down);
            return isWall(w, down, s) || s.isAir();
         });
         if (!candidates.isEmpty()) {
            BlockPos pos = candidates.get(rand.nextInt(candidates.size()));
            Direction border = context.wallMap.get(pos);
            if (border != null) {
               spawnDungeon(w, k, rand, pos, border, chunkX, chunkZ);
            }
         }
      }
   }

   // ------------------------------------------------------------------ UndergroundBiome

   private static final java.util.function.Predicate<BlockState> STONE = s -> s.is(Blocks.STONE) || s.is(Blocks.GRANITE) || s.is(Blocks.DIORITE)
      || s.is(Blocks.ANDESITE) || s.is(RLGen.LIMESTONE.get()) || s.is(RLGen.MARBLE.get());

   private static void fill(DWorld w, Kind k, Random rand, BlockPos pos, Context context) {
      BlockState state = w.get(pos);
      if (QuarkGen.hardness(w, pos) != -1.0F && !w.canSeeSky(pos)) {
         if (isFloor(w, pos, state)) {
            context.floorList.add(pos);
            fillFloor(w, k, rand, pos);
         } else if (isCeiling(w, pos, state)) {
            context.ceilingList.add(pos);
            fillCeiling(w, k, rand, pos);
         } else if (isWall(w, pos, state)) {
            context.wallMap.put(pos, borderSide(w, pos));
            fillWall(w, k, rand, pos);
         } else if (STONE.test(state)) {
            context.insideList.add(pos);
            if (k.mimicInside()) {
               fillWall(w, k, rand, pos);
            }
         }
      }
   }

   private static boolean replaceable(DWorld w, BlockPos p) {
      BlockState s = w.get(p);
      return s.isAir() || s.canBeReplaced();
   }

   private static boolean isFloor(DWorld w, BlockPos pos, BlockState state) {
      return Mat112.opaqueCube(state) && replaceable(w, pos.above());
   }

   private static boolean isCeiling(DWorld w, BlockPos pos, BlockState state) {
      return Mat112.opaqueCube(state) && replaceable(w, pos.below());
   }

   private static boolean isWall(DWorld w, BlockPos pos, BlockState state) {
      return Mat112.opaqueCube(state) && STONE.test(state) && borderSide(w, pos) != null;
   }

   private static Direction borderSide(DWorld w, BlockPos pos) {
      BlockState state = w.get(pos);
      for (Direction facing : HORIZONTALS) {
         BlockPos off = pos.relative(facing);
         BlockState at = w.get(off);
         if (state != at && at.isAir() || at.canBeReplaced()) {
            return facing;
         }
      }
      return null;
   }

   private static void fillFloor(DWorld w, Kind k, Random rand, BlockPos pos) {
      switch (k) {
         case LUSH -> w.set(pos, Blocks.GRASS_BLOCK.defaultBlockState());
         case SANDSTONE -> {
            if (rand.nextBoolean()) {
               w.set(pos, Blocks.SAND.defaultBlockState());
               if (rand.nextDouble() < 0.03) {
                  w.set(pos.above(), Blocks.DEAD_BUSH.defaultBlockState());
               }
            } else {
               w.set(pos, Blocks.SANDSTONE.defaultBlockState());
            }
         }
         case SLIME -> {
            fillCeiling(w, k, rand, pos); // "Enable Water Floor" is off
            if (rand.nextDouble() < 0.025) {
               w.set(pos, Blocks.SLIME_BLOCK.defaultBlockState());
            }
         }
         case PRISMARINE -> {
            if (rand.nextDouble() < 0.25 && borderSide(w, pos) == null) {
               w.set(pos, Blocks.WATER.defaultBlockState());
               w.scheduleFluidTick(pos, true);
            } else {
               w.set(pos, RLGen.ELDER_PRISMARINE.get().defaultBlockState());
            }
         }
         case SPIDER -> {
            if (rand.nextDouble() < 0.3) {
               w.set(pos, RLGen.COBBED_STONE.get().defaultBlockState());
            } else {
               w.set(pos, Blocks.COBBLESTONE.defaultBlockState());
            }
            cobweb(w, rand, pos.above(), 0.04);
         }
         case OVERGROWN -> {
            if (rand.nextDouble() < 0.35) {
               w.set(pos, Blocks.COARSE_DIRT.defaultBlockState());
            } else {
               w.set(pos, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            }
         }
         case ICY -> {
            BlockState ice = Blocks.PACKED_ICE.defaultBlockState();
            w.set(pos, ice);
            if (rand.nextDouble() < 0.015) {
               int height = 3 + rand.nextInt(3);
               BlockPos p = pos;
               for (int i = 0; i < height; i++) {
                  p = p.above();
                  if (!w.isAir(p)) {
                     break;
                  }
                  w.set(p, ice);
               }
            }
         }
         case LAVA -> {
            if (borderSide(w, pos) == null && rand.nextDouble() < 0.25) {
               w.set(pos, Blocks.LAVA.defaultBlockState());
               w.scheduleFluidTick(pos, false);
            } else if (rand.nextDouble() < 0.0625) {
               w.set(pos, Blocks.OBSIDIAN.defaultBlockState());
            } else {
               w.set(pos, RLGen.FIRE_STONE.get().defaultBlockState());
            }
         }
         case GLOWSHROOM -> {
            w.set(pos, RLGen.GLOWCELIUM.get().defaultBlockState());
            if (rand.nextDouble() < 0.0625) {
               w.set(pos.above(), RLGen.GLOWSHROOM.get().defaultBlockState());
            }
         }
      }
   }

   private static void fillCeiling(DWorld w, Kind k, Random rand, BlockPos pos) {
      switch (k) {
         case SANDSTONE -> {
            if (rand.nextDouble() < 0.01) {
               w.set(pos.below(), Blocks.SANDSTONE.defaultBlockState());
            }
            w.set(pos, Blocks.SANDSTONE.defaultBlockState());
         }
         case SLIME -> {
            Block b = switch (rand.nextInt(7)) {
               case 0, 1, 2 -> Blocks.GREEN_TERRACOTTA;
               case 3, 4, 5 -> Blocks.LIME_TERRACOTTA;
               default -> Blocks.LIGHT_BLUE_TERRACOTTA;
            };
            w.set(pos, b.defaultBlockState());
         }
         case PRISMARINE -> w.set(pos, RLGen.ELDER_PRISMARINE.get().defaultBlockState());
         case SPIDER -> {
            w.set(pos, Blocks.COBBLESTONE.defaultBlockState());
            cobweb(w, rand, pos.below(), 0.1);
         }
         case OVERGROWN -> w.set(pos, Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
         case ICY -> w.set(pos, RLGen.ICY_STONE.get().defaultBlockState());
         case LAVA -> {
            if (rand.nextBoolean()) {
               w.set(pos, RLGen.FIRE_STONE.get().defaultBlockState());
            } else {
               w.set(pos, Blocks.COBBLESTONE.defaultBlockState());
            }
         }
         case GLOWSHROOM -> w.set(pos, Blocks.DIRT.defaultBlockState());
         default -> {
         }
      }
   }

   private static void fillWall(DWorld w, Kind k, Random rand, BlockPos pos) {
      switch (k) {
         case SANDSTONE -> {
            if (rand.nextDouble() < 0.15) {
               w.set(pos, Blocks.CHISELED_SANDSTONE.defaultBlockState());
            } else {
               w.set(pos, Blocks.SANDSTONE.defaultBlockState());
            }
         }
         case SLIME -> fillCeiling(w, k, rand, pos);
         case PRISMARINE -> {
            w.set(pos, RLGen.ELDER_PRISMARINE.get().defaultBlockState());
            if (rand.nextDouble() < 0.0085) {
               w.set(pos, RLGen.ELDER_SEA_LANTERN.get().defaultBlockState());
            }
         }
         case SPIDER -> {
            if (rand.nextDouble() < 0.3) {
               w.set(pos, RLGen.COBBED_STONE.get().defaultBlockState());
            } else {
               w.set(pos, Blocks.COBBLESTONE.defaultBlockState());
            }
         }
         case ICY -> w.set(pos, RLGen.ICY_STONE.get().defaultBlockState());
         case LAVA -> w.set(pos, RLGen.FIRE_STONE.get().defaultBlockState());
         case GLOWSHROOM -> w.set(pos, Blocks.DIRT.defaultBlockState());
         default -> {
         }
      }
   }

   private static void cobweb(DWorld w, Random rand, BlockPos pos, double chance) {
      if (rand.nextDouble() < chance) {
         w.set(pos, Blocks.COBWEB.defaultBlockState());
      }
   }

   private static void finalFloorPass(DWorld w, Kind k, Random rand, BlockPos pos) {
      if (k != Kind.LUSH) {
         return;
      }
      if (rand.nextDouble() < 0.05) {
         bonemealGrass(w, rand, pos);
      }
      if (rand.nextDouble() < 0.01) {
         shrub(w, rand, pos.above());
      }
   }

   /** BlockGrass.grow (ItemDye.applyBonemeal); tall grass and flowers only stay where it is light enough. */
   private static void bonemealGrass(DWorld w, Random rand, BlockPos pos) {
      BlockPos up = pos.above();
      for (int i = 0; i < 128; i++) {
         BlockPos p = up;
         int j = 0;
         while (true) {
            if (j >= i / 16) {
               if (w.isAir(p)) {
                  if (rand.nextInt(8) == 0) {
                     rand.nextInt(2); // the biome's flower
                     if (!w.isDark(p)) {
                        w.set(p, Blocks.POPPY.defaultBlockState());
                     }
                  } else if (!w.isDark(p) && w.get(p.below()).is(Blocks.GRASS_BLOCK)) {
                     w.set(p, Blocks.SHORT_GRASS.defaultBlockState());
                  }
               }
               break;
            }
            p = p.offset(rand.nextInt(3) - 1, (rand.nextInt(3) - 1) * rand.nextInt(3) / 2, rand.nextInt(3) - 1);
            if (!w.get(p.below()).is(Blocks.GRASS_BLOCK) || Mat112.opaqueCube(w.get(p))) {
               break;
            }
            j++;
         }
      }
   }

   /** WorldGenShrub(oak log, oak leaves) */
   private static void shrub(DWorld w, Random rand, BlockPos position) {
      for (BlockState s = w.get(position); (s.isAir() || s.is(net.minecraft.tags.BlockTags.LEAVES)) && position.getY() > 0; s = w.get(position)) {
         position = position.below();
      }
      BlockState soil = w.get(position);
      if (soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK)) {
         position = position.above();
         w.set(position, Blocks.OAK_LOG.defaultBlockState());
         BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
         for (int i = position.getY(); i <= position.getY() + 2; i++) {
            int j = i - position.getY();
            int kk = 2 - j;
            for (int l = position.getX() - kk; l <= position.getX() + kk; l++) {
               int i1 = l - position.getX();
               for (int j1 = position.getZ() - kk; j1 <= position.getZ() + kk; j1++) {
                  int k1 = j1 - position.getZ();
                  if (Math.abs(i1) != kk || Math.abs(k1) != kk || rand.nextInt(2) != 0) {
                     BlockPos b = new BlockPos(l, i, j1);
                     BlockState s = w.get(b);
                     if (s.isAir() || s.is(net.minecraft.tags.BlockTags.LEAVES) || s.canBeReplaced()) {
                        w.set(b, leaves);
                     }
                  }
               }
            }
         }
      }
   }

   private static void finalCeilingPass(DWorld w, Kind k, Random rand, BlockPos pos) {
      if (k == Kind.OVERGROWN && rand.nextDouble() < 0.015) {
         int count = 0;
         for (int i = 0; i < 20; i++) {
            BlockPos check = pos.offset(0, -i, 0);
            if (isFloor(w, check, w.get(check))) {
               count = i;
               break;
            }
         }
         for (int i = 0; i <= count; i++) {
            w.set(pos.offset(0, -i, 0), Blocks.OAK_LOG.defaultBlockState());
         }
      }
   }

   private static void finalWallPass(DWorld w, Kind k, Random rand, BlockPos pos) {
      if (k != Kind.LUSH) {
         return;
      }
      for (Direction facing : HORIZONTALS) {
         BlockPos off = pos.relative(facing);
         BlockPos up = off.above();
         if (isCeiling(w, up, w.get(up)) && rand.nextDouble() < 0.125) {
            boolean did = false;
            while (w.isAir(off) && off.getY() > 0) {
               w.set(off, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(facing.getOpposite()), true));
               off = off.below();
               did = true;
            }
            if (did) {
               return;
            }
         }
      }
   }

   private static void spawnDungeon(DWorld w, Kind k, Random rand, BlockPos pos, Direction side, int cx, int cz) {
      if (k == Kind.SPIDER) {
         BlockPos spawnerPos = pos.relative(side);
         EntityType<?> e = rand.nextDouble() < 0.25 ? EntityType.CAVE_SPIDER : EntityType.SPIDER;
         QuarkGen.spawner(w, spawnerPos, e);
         int range = 3;
         for (int x = -range; x < range + 1; x++) {
            for (int y = -range; y < range + 1; y++) {
               for (int z = -range; z < range + 1; z++) {
                  BlockPos p = spawnerPos.offset(x, y, z);
                  if (replaceable(w, p)) {
                     cobweb(w, rand, p, 0.5);
                  }
               }
            }
         }
      } else if (k == Kind.SANDSTONE) {
         pos = switch (side) {
            case SOUTH -> pos.offset(-3, -7, -6);
            case EAST -> pos.offset(-6, -7, 3);
            case WEST -> pos.offset(6, -7, -3);
            default -> pos.offset(3, -7, 6);
         };
         StructureTemplate t = Templates.get(w.level.getLevel().getStructureManager(), "rlgen:quark/husk_grave").orElse(null);
         if (t == null) {
            return;
         }
         StructurePlaceSettings settings = Templates.settings(QuarkGen.rotationFromFacing(side.getOpposite()), Mirror.NONE, Templates.region(w, cx, cz));
         Templates.place(w, t, pos, settings, pos.asLong());
         for (Map.Entry<BlockPos, String> e : Templates.dataBlocks(t, pos, settings).entrySet()) {
            if (e.getValue().equals("spawner")) {
               QuarkGen.spawner(w, e.getKey(), EntityType.ZOMBIE);
            } else if (e.getValue().equals("chest")) {
               QuarkGen.chest(w, e.getKey(), Direction.WEST, BuiltInLootTables.DESERT_PYRAMID, rand.nextLong());
            }
         }
      }
   }
}
