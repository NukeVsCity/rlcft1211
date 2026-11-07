package com.nukevscity.rlgen.deco;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The 1.12 vanilla trees the roofed forest grid (BiomeForest.addMushrooms) grows where Recurrent Complex allows the TREE event:
 * WorldGenCanopyTree, WorldGenBirchTree (no extra height), WorldGenTrees (oak, no vines) and WorldGenBigTree, with the Forge 1.12 patches
 * (canSustainPlant soil, onPlantGrow, isReplaceable, isAir/isLeaves). The world is 256 high.
 * <p>
 * 1.12 leaves with check_decay=false never decay until a neighbour changes; 1.21 leaves need a distance, so each tree's leaves get
 * the distance to the nearest log after the tree is placed (like 1.21 TreeFeature).
 */
public final class VanillaTrees {
   private VanillaTrees() {
   }

   private static final BlockState DARK_OAK_LOG = Blocks.DARK_OAK_LOG.defaultBlockState();
   private static final BlockState DARK_OAK_LEAVES = Blocks.DARK_OAK_LEAVES.defaultBlockState();
   private static final BlockState BIRCH_LOG = Blocks.BIRCH_LOG.defaultBlockState();
   private static final BlockState BIRCH_LEAVES = Blocks.BIRCH_LEAVES.defaultBlockState();
   private static final BlockState OAK_LOG = Blocks.OAK_LOG.defaultBlockState();
   private static final BlockState OAK_LEAVES = Blocks.OAK_LEAVES.defaultBlockState();
   private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

   /** BiomeForest.getRandomTreeFeature (ROOFED) + setDecorationDefaults + generate; generateSaplings is a no-op for all of them. */
   static void roofedForestTree(DWorld w, Random random, BlockPos pos) {
      if (random.nextInt(3) > 0) {
         new Tree(w).canopy(random, pos);
      } else if (random.nextInt(5) == 0) {
         new Tree(w).birch(random, pos);
      } else if (random.nextInt(10) == 0) {
         BIG_TREE.generateDecoration(w, random, pos);
      } else {
         new Tree(w).oak(random, pos);
      }
   }

   // ---------------- 1.12 block tests (Forge)

   /** Block.isAir: Material.AIR */
   static boolean isAir(BlockState s) {
      return s.isAir();
   }

   /** Block.isLeaves: Material.LEAVES */
   static boolean isLeaves(BlockState s) {
      return DWorld.isLeaves(s);
   }

   /** WorldGenAbstractTree.canGrowInto */
   static boolean canGrowInto(BlockState s) {
      Block b = s.getBlock();
      return b.defaultBlockState().isAir() || isLeaves(b.defaultBlockState()) || b == Blocks.GRASS_BLOCK || b == Blocks.DIRT
         || b == Blocks.COARSE_DIRT || b == Blocks.PODZOL || isOldLog(b) || s.is(BlockTags.SAPLINGS) || b == Blocks.VINE;
   }

   /** Blocks.LOG / LOG2 (all six woods, also the bark-everywhere variants) */
   private static boolean isOldLog(Block b) {
      return b == Blocks.OAK_LOG || b == Blocks.SPRUCE_LOG || b == Blocks.BIRCH_LOG || b == Blocks.JUNGLE_LOG || b == Blocks.ACACIA_LOG
         || b == Blocks.DARK_OAK_LOG || b == Blocks.OAK_WOOD || b == Blocks.SPRUCE_WOOD || b == Blocks.BIRCH_WOOD || b == Blocks.JUNGLE_WOOD
         || b == Blocks.ACACIA_WOOD || b == Blocks.DARK_OAK_WOOD;
   }

   /** WorldGenAbstractTree.isReplaceable (Forge): air, leaves, wood (BlockLog.isWood) or canGrowInto */
   static boolean isReplaceable(DWorld w, BlockPos p) {
      BlockState s = w.get(p);
      return isAir(s) || isLeaves(s) || s.is(BlockTags.LOGS) || canGrowInto(s);
   }

   /** Block.canSustainPlant(UP, oak sapling): Plains plants grow on grass, dirt (coarse, podzol) and farmland. */
   static boolean isSoil(BlockState s) {
      Block b = s.getBlock();
      return b == Blocks.GRASS_BLOCK || b == Blocks.DIRT || b == Blocks.COARSE_DIRT || b == Blocks.PODZOL || b == Blocks.FARMLAND;
   }

   /** Material.VINE: tall grass, fern, dead bush, vines, double plants */
   static boolean isVineMaterial(BlockState s) {
      Block b = s.getBlock();
      return b == Blocks.SHORT_GRASS || b == Blocks.FERN || b == Blocks.DEAD_BUSH || b == Blocks.VINE || b == Blocks.TALL_GRASS
         || b == Blocks.LARGE_FERN || b == Blocks.SUNFLOWER || b == Blocks.LILAC || b == Blocks.ROSE_BUSH || b == Blocks.PEONY;
   }

   /** One tree being placed: writes go through here so the leaves get their distances at the end. */
   static final class Tree {
      final DWorld w;
      private final List<BlockPos> leaves = new ArrayList<>();

      Tree(DWorld w) {
         this.w = w;
      }

      BlockState get(BlockPos p) {
         return this.w.get(p);
      }

      void set(BlockPos p, BlockState s) {
         this.w.set(p, s);
         if (s.getBlock() instanceof LeavesBlock) {
            this.leaves.add(p.immutable());
         }
      }

      /** Block.onPlantGrow (Forge): grass and farmland under a growing tree become dirt. */
      void onPlantGrow(BlockPos p) {
         Block b = this.get(p).getBlock();
         if (b == Blocks.GRASS_BLOCK || b == Blocks.FARMLAND) {
            this.w.set(p, Blocks.DIRT.defaultBlockState());
         }
      }

      /** 1.21 leaf distances: 1 next to a log, else 1 + the nearest leaf of this tree, 7 (decays) beyond. */
      boolean finish() {
         Map<BlockPos, Integer> dist = new HashMap<>();
         ArrayDeque<BlockPos> queue = new ArrayDeque<>();
         for (BlockPos p : this.leaves) {
            if (dist.containsKey(p) || !(this.get(p).getBlock() instanceof LeavesBlock)) {
               continue;
            }
            dist.put(p, 7);
            for (Direction d : Direction.values()) {
               if (this.get(p.relative(d)).is(BlockTags.LOGS)) {
                  dist.put(p, 1);
                  queue.add(p);
                  break;
               }
            }
         }
         while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            int n = dist.get(p) + 1;
            if (n >= 7) {
               continue;
            }
            for (Direction d : Direction.values()) {
               BlockPos q = p.relative(d);
               Integer o = dist.get(q);
               if (o != null && o > n) {
                  dist.put(q, n);
                  queue.add(q);
               }
            }
         }
         for (Map.Entry<BlockPos, Integer> e : dist.entrySet()) {
            BlockState s = this.get(e.getKey());
            if (s.getBlock() instanceof LeavesBlock && s.getValue(LeavesBlock.DISTANCE) != e.getValue()) {
               this.w.set(e.getKey(), s.setValue(LeavesBlock.DISTANCE, e.getValue()));
            }
         }
         return true;
      }

      // ---------------- WorldGenCanopyTree

      boolean canopy(Random random, BlockPos blockpos) {
         int i = random.nextInt(3) + random.nextInt(2) + 6;
         int j = blockpos.getX();
         int k = blockpos.getY();
         int l = blockpos.getZ();
         if (k < 1 || k + i + 1 >= 256) {
            return false;
         }
         BlockPos blockpos1 = blockpos.below();
         if (!(isSoil(this.get(blockpos1)) && blockpos.getY() < 256 - i - 1)) {
            return false;
         }
         if (!this.placeTreeOfHeight(blockpos, i)) {
            return false;
         }
         this.onPlantGrow(blockpos1);
         this.onPlantGrow(blockpos1.east());
         this.onPlantGrow(blockpos1.south());
         this.onPlantGrow(blockpos1.south().east());
         Direction enumfacing = HORIZONTAL[random.nextInt(HORIZONTAL.length)];
         int i1 = i - random.nextInt(4);
         int j1 = 2 - random.nextInt(3);
         int k1 = j;
         int l1 = l;
         int i2 = k + i - 1;
         for (int j2 = 0; j2 < i; j2++) {
            if (j2 >= i1 && j1 > 0) {
               k1 += enumfacing.getStepX();
               l1 += enumfacing.getStepZ();
               j1--;
            }
            int k2 = k + j2;
            BlockPos blockpos2 = new BlockPos(k1, k2, l1);
            BlockState state = this.get(blockpos2);
            if (isAir(state) || isLeaves(state)) {
               this.placeLogAt(blockpos2);
               this.placeLogAt(blockpos2.east());
               this.placeLogAt(blockpos2.south());
               this.placeLogAt(blockpos2.east().south());
            }
         }
         for (int i3 = -2; i3 <= 0; i3++) {
            for (int l3 = -2; l3 <= 0; l3++) {
               int k4 = -1;
               this.placeLeafAt(k1 + i3, i2 + k4, l1 + l3);
               this.placeLeafAt(1 + k1 - i3, i2 + k4, l1 + l3);
               this.placeLeafAt(k1 + i3, i2 + k4, 1 + l1 - l3);
               this.placeLeafAt(1 + k1 - i3, i2 + k4, 1 + l1 - l3);
               if ((i3 > -2 || l3 > -1) && (i3 != -1 || l3 != -2)) {
                  int b0 = 1;
                  this.placeLeafAt(k1 + i3, i2 + b0, l1 + l3);
                  this.placeLeafAt(1 + k1 - i3, i2 + b0, l1 + l3);
                  this.placeLeafAt(k1 + i3, i2 + b0, 1 + l1 - l3);
                  this.placeLeafAt(1 + k1 - i3, i2 + b0, 1 + l1 - l3);
               }
            }
         }
         if (random.nextBoolean()) {
            this.placeLeafAt(k1, i2 + 2, l1);
            this.placeLeafAt(k1 + 1, i2 + 2, l1);
            this.placeLeafAt(k1 + 1, i2 + 2, l1 + 1);
            this.placeLeafAt(k1, i2 + 2, l1 + 1);
         }
         for (int j3 = -3; j3 <= 4; j3++) {
            for (int i4 = -3; i4 <= 4; i4++) {
               if ((j3 != -3 || i4 != -3) && (j3 != -3 || i4 != 4) && (j3 != 4 || i4 != -3) && (j3 != 4 || i4 != 4)
                  && (Math.abs(j3) < 3 || Math.abs(i4) < 3)) {
                  this.placeLeafAt(k1 + j3, i2, l1 + i4);
               }
            }
         }
         for (int k3 = -1; k3 <= 2; k3++) {
            for (int j4 = -1; j4 <= 2; j4++) {
               if ((k3 < 0 || k3 > 1 || j4 < 0 || j4 > 1) && random.nextInt(3) <= 0) {
                  int l4 = random.nextInt(3) + 2;
                  for (int i5 = 0; i5 < l4; i5++) {
                     this.placeLogAt(new BlockPos(j + k3, i2 - i5 - 1, l + j4));
                  }
                  for (int j5 = -1; j5 <= 1; j5++) {
                     for (int l2 = -1; l2 <= 1; l2++) {
                        this.placeLeafAt(k1 + k3 + j5, i2, l1 + j4 + l2);
                     }
                  }
                  for (int k5 = -2; k5 <= 2; k5++) {
                     for (int l5 = -2; l5 <= 2; l5++) {
                        if (Math.abs(k5) != 2 || Math.abs(l5) != 2) {
                           this.placeLeafAt(k1 + k3 + k5, i2 - 1, l1 + j4 + l5);
                        }
                     }
                  }
               }
            }
         }
         return this.finish();
      }

      private boolean placeTreeOfHeight(BlockPos blockpos, int i) {
         int j = blockpos.getX();
         int k = blockpos.getY();
         int l = blockpos.getZ();
         BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
         for (int i1 = 0; i1 <= i + 1; i1++) {
            int j1 = 1;
            if (i1 == 0) {
               j1 = 0;
            }
            if (i1 >= i - 1) {
               j1 = 2;
            }
            for (int k1 = -j1; k1 <= j1; k1++) {
               for (int l1 = -j1; l1 <= j1; l1++) {
                  if (!isReplaceable(this.w, m.set(j + k1, k + i1, l + l1))) {
                     return false;
                  }
               }
            }
         }
         return true;
      }

      private void placeLogAt(BlockPos blockpos) {
         if (canGrowInto(this.get(blockpos))) {
            this.set(blockpos, DARK_OAK_LOG);
         }
      }

      private void placeLeafAt(int i, int j, int k) {
         BlockPos blockpos = new BlockPos(i, j, k);
         if (isAir(this.get(blockpos))) {
            this.set(blockpos, DARK_OAK_LEAVES);
         }
      }

      // ---------------- WorldGenBirchTree(false, false)

      boolean birch(Random random, BlockPos blockpos) {
         int i = random.nextInt(3) + 5;
         boolean flag = true;
         if (blockpos.getY() < 1 || blockpos.getY() + i + 1 > 256) {
            return false;
         }
         for (int j = blockpos.getY(); j <= blockpos.getY() + 1 + i; j++) {
            int k = 1;
            if (j == blockpos.getY()) {
               k = 0;
            }
            if (j >= blockpos.getY() + 1 + i - 2) {
               k = 2;
            }
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
            for (int l = blockpos.getX() - k; l <= blockpos.getX() + k && flag; l++) {
               for (int i1 = blockpos.getZ() - k; i1 <= blockpos.getZ() + k && flag; i1++) {
                  if (j < 0 || j >= 256) {
                     flag = false;
                  } else if (!isReplaceable(this.w, m.set(l, j, i1))) {
                     flag = false;
                  }
               }
            }
         }
         if (!flag) {
            return false;
         }
         BlockPos down = blockpos.below();
         if (!(isSoil(this.get(down)) && blockpos.getY() < 256 - i - 1)) {
            return false;
         }
         this.onPlantGrow(down);
         for (int i2 = blockpos.getY() - 3 + i; i2 <= blockpos.getY() + i; i2++) {
            int k2 = i2 - (blockpos.getY() + i);
            int l2 = 1 - k2 / 2;
            for (int i3 = blockpos.getX() - l2; i3 <= blockpos.getX() + l2; i3++) {
               int j1 = i3 - blockpos.getX();
               for (int k1 = blockpos.getZ() - l2; k1 <= blockpos.getZ() + l2; k1++) {
                  int l1 = k1 - blockpos.getZ();
                  if (Math.abs(j1) != l2 || Math.abs(l1) != l2 || random.nextInt(2) != 0 && k2 != 0) {
                     BlockPos blockpos1 = new BlockPos(i3, i2, k1);
                     // Forge: isAir || isAir, so birch leaves don't replace leaves
                     if (isAir(this.get(blockpos1))) {
                        this.set(blockpos1, BIRCH_LEAVES);
                     }
                  }
               }
            }
         }
         for (int j2 = 0; j2 < i; j2++) {
            BlockState s = this.get(blockpos.above(j2));
            if (isAir(s) || isLeaves(s)) {
               this.set(blockpos.above(j2), BIRCH_LOG);
            }
         }
         return this.finish();
      }

      // ---------------- WorldGenTrees(false): oak, minTreeHeight 4, no vines

      boolean oak(Random random, BlockPos blockpos) {
         int i = random.nextInt(3) + 4;
         boolean flag = true;
         if (blockpos.getY() < 1 || blockpos.getY() + i + 1 > 256) {
            return false;
         }
         for (int j = blockpos.getY(); j <= blockpos.getY() + 1 + i; j++) {
            int k = 1;
            if (j == blockpos.getY()) {
               k = 0;
            }
            if (j >= blockpos.getY() + 1 + i - 2) {
               k = 2;
            }
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
            for (int l = blockpos.getX() - k; l <= blockpos.getX() + k && flag; l++) {
               for (int i1 = blockpos.getZ() - k; i1 <= blockpos.getZ() + k && flag; i1++) {
                  if (j < 0 || j >= 256) {
                     flag = false;
                  } else if (!isReplaceable(this.w, m.set(l, j, i1))) {
                     flag = false;
                  }
               }
            }
         }
         if (!flag) {
            return false;
         }
         BlockPos down = blockpos.below();
         if (!(isSoil(this.get(down)) && blockpos.getY() < 256 - i - 1)) {
            return false;
         }
         this.onPlantGrow(down);
         for (int i3 = blockpos.getY() - 3 + i; i3 <= blockpos.getY() + i; i3++) {
            int i4 = i3 - (blockpos.getY() + i);
            int j1 = 1 - i4 / 2;
            for (int k1 = blockpos.getX() - j1; k1 <= blockpos.getX() + j1; k1++) {
               int l1 = k1 - blockpos.getX();
               for (int i2 = blockpos.getZ() - j1; i2 <= blockpos.getZ() + j1; i2++) {
                  int j2 = i2 - blockpos.getZ();
                  if (Math.abs(l1) != j1 || Math.abs(j2) != j1 || random.nextInt(2) != 0 && i4 != 0) {
                     BlockPos blockpos1 = new BlockPos(k1, i3, i2);
                     BlockState s = this.get(blockpos1);
                     if (isAir(s) || isLeaves(s) || isVineMaterial(s)) {
                        this.set(blockpos1, OAK_LEAVES);
                     }
                  }
               }
            }
         }
         for (int j3 = 0; j3 < i; j3++) {
            BlockState s = this.get(blockpos.above(j3));
            if (isAir(s) || isLeaves(s) || isVineMaterial(s)) {
               this.set(blockpos.above(j3), OAK_LOG);
            }
         }
         return this.finish();
      }
   }

   // ---------------- WorldGenBigTree

   /**
    * Biome.BIG_TREE_FEATURE is one static instance: heightLimit is only rolled while it is 0 and validTreeLocation can lower it, so
    * after the first big oak every later one (until a restart) uses that height or less; setDecorationDefaults sets leafDistanceLimit
    * to 5 for good. Kept, shared by all chunks like 1.12.
    */
   static final BigTree BIG_TREE = new BigTree();

   static final class BigTree {
      private Random rand;
      private Tree tree;
      private BlockPos basePos = BlockPos.ZERO;
      int heightLimit;
      int height;
      double heightAttenuation = 0.618;
      double branchSlope = 0.381;
      double scaleWidth = 1.0;
      double leafDensity = 1.0;
      int trunkSize = 1;
      int heightLimitLimit = 12;
      int leafDistanceLimit = 4;
      List<Foliage> foliageCoords;

      record Foliage(BlockPos pos, int branchBase) {
      }

      synchronized boolean generateDecoration(DWorld w, Random random, BlockPos blockpos) {
         this.leafDistanceLimit = 5;
         return this.generate(w, random, blockpos);
      }

      private static BlockPos add(BlockPos p, double x, double y, double z) {
         return BlockPos.containing(p.getX() + x, p.getY() + y, p.getZ() + z);
      }

      void generateLeafNodeList() {
         this.height = (int)((double)this.heightLimit * this.heightAttenuation);
         if (this.height >= this.heightLimit) {
            this.height = this.heightLimit - 1;
         }
         int i = (int)(1.382 + Math.pow(this.leafDensity * (double)this.heightLimit / 13.0, 2.0));
         if (i < 1) {
            i = 1;
         }
         int j = this.basePos.getY() + this.height;
         int k = this.heightLimit - this.leafDistanceLimit;
         this.foliageCoords = new ArrayList<>();
         this.foliageCoords.add(new Foliage(this.basePos.above(k), j));
         for (; k >= 0; k--) {
            float f = this.layerSize(k);
            if (!(f < 0.0F)) {
               for (int l = 0; l < i; l++) {
                  double d0 = this.scaleWidth * (double)f * ((double)this.rand.nextFloat() + 0.328);
                  double d1 = (double)(this.rand.nextFloat() * 2.0F) * Math.PI;
                  double d2 = d0 * Math.sin(d1) + 0.5;
                  double d3 = d0 * Math.cos(d1) + 0.5;
                  BlockPos blockpos = add(this.basePos, d2, (double)(k - 1), d3);
                  BlockPos blockpos1 = blockpos.above(this.leafDistanceLimit);
                  if (this.checkBlockLine(blockpos, blockpos1) == -1) {
                     int i1 = this.basePos.getX() - blockpos.getX();
                     int j1 = this.basePos.getZ() - blockpos.getZ();
                     double d4 = (double)blockpos.getY() - Math.sqrt((double)(i1 * i1 + j1 * j1)) * this.branchSlope;
                     int k1 = d4 > (double)j ? j : (int)d4;
                     BlockPos blockpos2 = new BlockPos(this.basePos.getX(), k1, this.basePos.getZ());
                     if (this.checkBlockLine(blockpos2, blockpos) == -1) {
                        this.foliageCoords.add(new Foliage(blockpos, blockpos2.getY()));
                     }
                  }
               }
            }
         }
      }

      void crosSection(BlockPos blockpos, float f, BlockState iblockstate) {
         int i = (int)((double)f + 0.618);
         for (int j = -i; j <= i; j++) {
            for (int k = -i; k <= i; k++) {
               if (Math.pow((double)Math.abs(j) + 0.5, 2.0) + Math.pow((double)Math.abs(k) + 0.5, 2.0) <= (double)(f * f)) {
                  BlockPos blockpos1 = blockpos.offset(j, 0, k);
                  BlockState s = this.tree.get(blockpos1);
                  if (isAir(s) || isLeaves(s)) {
                     this.tree.set(blockpos1, iblockstate);
                  }
               }
            }
         }
      }

      float layerSize(int i) {
         if ((float)i < (float)this.heightLimit * 0.3F) {
            return -1.0F;
         }
         float f = (float)this.heightLimit / 2.0F;
         float f1 = f - (float)i;
         float f2 = (float)Math.sqrt((double)(f * f - f1 * f1));
         if (f1 == 0.0F) {
            f2 = f;
         } else if (Math.abs(f1) >= f) {
            return 0.0F;
         }
         return f2 * 0.5F;
      }

      float leafSize(int i) {
         if (i < 0 || i >= this.leafDistanceLimit) {
            return -1.0F;
         }
         return i != 0 && i != this.leafDistanceLimit - 1 ? 3.0F : 2.0F;
      }

      void generateLeafNode(BlockPos blockpos) {
         for (int i = 0; i < this.leafDistanceLimit; i++) {
            this.crosSection(blockpos.above(i), this.leafSize(i), OAK_LEAVES);
         }
      }

      void limb(BlockPos blockpos, BlockPos blockpos1) {
         BlockPos blockpos2 = blockpos1.offset(-blockpos.getX(), -blockpos.getY(), -blockpos.getZ());
         int i = this.getGreatestDistance(blockpos2);
         float f = (float)blockpos2.getX() / (float)i;
         float f1 = (float)blockpos2.getY() / (float)i;
         float f2 = (float)blockpos2.getZ() / (float)i;
         for (int j = 0; j <= i; j++) {
            BlockPos blockpos3 = add(blockpos, (double)(0.5F + (float)j * f), (double)(0.5F + (float)j * f1), (double)(0.5F + (float)j * f2));
            this.tree.set(blockpos3, OAK_LOG.setValue(RotatedPillarBlock.AXIS, this.getLogAxis(blockpos, blockpos3)));
         }
      }

      private int getGreatestDistance(BlockPos blockpos) {
         int i = Math.abs(blockpos.getX());
         int j = Math.abs(blockpos.getY());
         int k = Math.abs(blockpos.getZ());
         if (k > i && k > j) {
            return k;
         }
         return j > i ? j : i;
      }

      private Direction.Axis getLogAxis(BlockPos blockpos, BlockPos blockpos1) {
         Direction.Axis axis = Direction.Axis.Y;
         int i = Math.abs(blockpos1.getX() - blockpos.getX());
         int j = Math.abs(blockpos1.getZ() - blockpos.getZ());
         int k = Math.max(i, j);
         if (k > 0) {
            if (i == k) {
               axis = Direction.Axis.X;
            } else if (j == k) {
               axis = Direction.Axis.Z;
            }
         }
         return axis;
      }

      void generateLeaves() {
         for (Foliage c : this.foliageCoords) {
            this.generateLeafNode(c.pos());
         }
      }

      boolean leafNodeNeedsBase(int i) {
         return (double)i >= (double)this.heightLimit * 0.2;
      }

      void generateTrunk() {
         BlockPos blockpos = this.basePos;
         BlockPos blockpos1 = this.basePos.above(this.height);
         this.limb(blockpos, blockpos1);
         if (this.trunkSize == 2) {
            this.limb(blockpos.east(), blockpos1.east());
            this.limb(blockpos.east().south(), blockpos1.east().south());
            this.limb(blockpos.south(), blockpos1.south());
         }
      }

      void generateLeafNodeBases() {
         for (Foliage c : this.foliageCoords) {
            int i = c.branchBase();
            BlockPos blockpos = new BlockPos(this.basePos.getX(), i, this.basePos.getZ());
            if (!blockpos.equals(c.pos()) && this.leafNodeNeedsBase(i - this.basePos.getY())) {
               this.limb(blockpos, c.pos());
            }
         }
      }

      int checkBlockLine(BlockPos blockpos, BlockPos blockpos1) {
         BlockPos blockpos2 = blockpos1.offset(-blockpos.getX(), -blockpos.getY(), -blockpos.getZ());
         int i = this.getGreatestDistance(blockpos2);
         float f = (float)blockpos2.getX() / (float)i;
         float f1 = (float)blockpos2.getY() / (float)i;
         float f2 = (float)blockpos2.getZ() / (float)i;
         if (i == 0) {
            return -1;
         }
         for (int j = 0; j <= i; j++) {
            BlockPos blockpos3 = add(blockpos, (double)(0.5F + (float)j * f), (double)(0.5F + (float)j * f1), (double)(0.5F + (float)j * f2));
            if (!isReplaceable(this.tree.w, blockpos3)) {
               return j;
            }
         }
         return -1;
      }

      boolean generate(DWorld w, Random random, BlockPos blockpos) {
         this.tree = new Tree(w);
         this.basePos = blockpos;
         this.rand = new Random(random.nextLong());
         if (this.heightLimit == 0) {
            this.heightLimit = 5 + this.rand.nextInt(this.heightLimitLimit);
         }
         try {
            if (!this.validTreeLocation()) {
               return false;
            }
            this.generateLeafNodeList();
            this.generateLeaves();
            this.generateTrunk();
            this.generateLeafNodeBases();
            return this.tree.finish();
         } finally {
            this.tree = null;
         }
      }

      private boolean validTreeLocation() {
         if (!isSoil(this.tree.get(this.basePos.below()))) {
            return false;
         }
         int i = this.checkBlockLine(this.basePos, this.basePos.above(this.heightLimit - 1));
         if (i == -1) {
            return true;
         } else if (i < 6) {
            return false;
         } else {
            this.heightLimit = i;
            return true;
         }
      }
   }
}
