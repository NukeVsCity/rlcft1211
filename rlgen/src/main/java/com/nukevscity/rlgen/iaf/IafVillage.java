package com.nukevscity.rlgen.iaf;

import com.nukevscity.rlgen.RLGen;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Ice and Fire 1.7.1 MapGenPixieVillage / PixieVillagePieces (a grass path square with a dark oak, paths, pixie houses) and
 * MapGenSnowVillage / SnowVillagePieces (a packed ice well, paths, ice huts, torch posts). The pieces are planned with the
 * IWorldGenerator chunk random and built inside the 64 x 64 box around the chunk centre, like their generate() did.
 * Pixies and snow villagers are not spawned.
 */
final class IafVillage {
   private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

   final List<Village> components = new ArrayList<>();
   final BoundingBox box;
   final long seed;

   private IafVillage(BoundingBox box, long seed) {
      this.box = box;
      this.seed = seed;
   }

   /** MapGenPixieVillage / MapGenSnowVillage.generate: the start in the chunk of position, built in position +- 32. */
   static IafVillage plan(IafWorld world, Random rand, Random unseeded, BlockPos position, boolean snow) {
      int chunkX = position.getX() >> 4;
      int chunkZ = position.getZ() >> 4;
      IafVillage v = new IafVillage(new BoundingBox(position.getX() - 32, 1, position.getZ() - 32, position.getX() + 32, 512, position.getZ() + 32),
         unseeded.nextLong());
      List<PieceWeight> list = new ArrayList<>();
      int size;
      if (snow) {
         size = 0; // MapGenSnowVillage never set its size
         list.add(new PieceWeight(75, randInt(rand, 5 + size, 6 + size))); // TorchNew
         list.add(new PieceWeight(65, randInt(rand, 3 + size, 5 + size))); // WoodHut
         list.removeIf(w -> w.limit == 0);
      } else {
         size = 5; // "Pixie Village Size"
         list.add(new PieceWeight(75, 10)); // PixieHouse
         list.add(new PieceWeight(25, 12)); // Road (still creates a pixie house)
      }
      Start start = new Start(rand, (chunkX << 4) + 2, (chunkZ << 4) + 2, list, size, snow);
      v.components.add(start);
      start.buildComponent(start, v.components, rand);
      while (!start.pendingRoads.isEmpty() || !start.pendingHouses.isEmpty()) {
         if (start.pendingRoads.isEmpty()) {
            start.pendingHouses.remove(rand.nextInt(start.pendingHouses.size())).buildComponent(start, v.components, rand);
         } else {
            start.pendingRoads.remove(rand.nextInt(start.pendingRoads.size())).buildComponent(start, v.components, rand);
         }
      }
      // the first addComponentParts of each piece averaged the ground inside the generation box; 1.12 terrain heights
      for (Village c : v.components) {
         if (c.groundOffset != null) {
            int sum = 0;
            int n = 0;
            BoundingBox b = c.getBoundingBox();
            for (int z = b.minZ(); z <= b.maxZ(); z++) {
               for (int x = b.minX(); x <= b.maxX(); x++) {
                  if (v.box.isInside(new BlockPos(x, 64, z))) {
                     sum += Math.max(world.height(x, z), 63);
                     n++;
                  }
               }
            }
            if (n > 0) {
               int avg = sum / n;
               c.move(c.groundShift, avg - b.maxY() + c.groundOffset, c.groundShift);
               c.built = true;
            }
         } else {
            c.built = true;
         }
      }
      return v;
   }

   /** Builds the parts of the village inside clip (one populate area). */
   void build(WorldGenLevel level, BoundingBox clip) {
      BoundingBox area = intersection(this.box, clip);
      if (area == null) {
         return;
      }
      for (Village c : this.components) {
         if (c.built && c.getBoundingBox().intersects(area)) {
            c.addComponentParts(level, new Random(this.seed ^ c.getBoundingBox().getCenter().asLong()), area);

         }
      }
   }

   private static BoundingBox intersection(BoundingBox a, BoundingBox b) {
      if (!a.intersects(b)) {
         return null;
      }
      return new BoundingBox(Math.max(a.minX(), b.minX()), Math.max(a.minY(), b.minY()), Math.max(a.minZ(), b.minZ()),
         Math.min(a.maxX(), b.maxX()), Math.min(a.maxY(), b.maxY()), Math.min(a.maxZ(), b.maxZ()));
   }

   static int randInt(Random random, int min, int max) {
      return min >= max ? min : random.nextInt(max - min + 1) + min;
   }

   static StructurePiece findIntersecting(List<? extends StructurePiece> list, BoundingBox box) {
      for (StructurePiece p : list) {
         if (p.getBoundingBox() != null && p.getBoundingBox().intersects(box)) {
            return p;
         }
      }
      return null;
   }

   static final class PieceWeight {
      final int weight;
      final int limit;
      int spawned;

      PieceWeight(int weight, int limit) {
         this.weight = weight;
         this.limit = limit;
      }

      boolean canSpawnMore() {
         return this.limit == 0 || this.spawned < this.limit;
      }
   }

   private static int updatePieceWeight(List<PieceWeight> list) {
      boolean flag = false;
      int i = 0;
      for (PieceWeight w : list) {
         if (w.limit > 0 && w.spawned < w.limit) {
            flag = true;
         }
         i += w.weight;
      }
      return flag ? i : -1;
   }

   private static Village generateComponent(Start start, List<Village> list, Random rand, int x, int y, int z, Direction facing, int type) {
      int i = updatePieceWeight(start.weights);
      if (i <= 0) {
         return null;
      }
      int j = 0;
      while (j < 5) {
         j++;
         int k = rand.nextInt(i);
         for (PieceWeight w : start.weights) {
            k -= w.weight;
            if (k < 0) {
               if (!w.canSpawnMore() || w == start.lastWeight && start.weights.size() > 1) {
                  break;
               }
               Village village;
               if (start.snow) {
                  // Torch.findPieceBox(...) is computed and thrown away
                  village = rand.nextBoolean() ? WoodHut.createPiece(start, list, rand, x, y, z, facing, type) : TorchNew.createPiece(start, list, x, y, z, facing, type);
               } else {
                  village = PixieHouse.createPiece(start, list, x, y, z, facing, type);
               }
               if (village != null) {
                  w.spawned++;
                  start.lastWeight = w;
                  if (!w.canSpawnMore()) {
                     start.weights.remove(w);
                  }
                  return village;
               }
            }
         }
      }
      return null;
   }

   static Village generateAndAddComponent(Start start, List<Village> list, Random rand, int x, int y, int z, Direction facing, int type) {
      if (type > 50) {
         return null;
      }
      if (Math.abs(x - start.getBoundingBox().minX()) <= 112 && Math.abs(z - start.getBoundingBox().minZ()) <= 112) {
         Village c = generateComponent(start, list, rand, x, y, z, facing, type + 1);
         if (c != null) {
            list.add(c);
            start.pendingHouses.add(c);
            return c;
         }
      }
      return null;
   }

   static Village generateAndAddRoadPiece(Start start, List<Village> list, Random rand, int x, int y, int z, Direction facing, int type) {
      if (type > 3 + start.terrainType) {
         return null;
      }
      if (Math.abs(x - start.getBoundingBox().minX()) <= 112 && Math.abs(z - start.getBoundingBox().minZ()) <= 112) {
         BoundingBox b = Path.findPieceBox(list, rand, x, y, z, facing, start.snow);
         if (b != null && b.minY() > 0) {
            Village c = new Path(type, b, facing, start.snow);
            list.add(c);
            start.pendingRoads.add(c);
            return c;
         }
      }
      return null;
   }

   abstract static class Village extends StructurePiece {
      /** Added to the averaged ground when the piece is moved to it (null: it isn't). */
      Integer groundOffset;
      /** The Well also moved one block along x and z. */
      int groundShift;
      boolean built;
      /** Start.isZombieInfested: no torches and no doors. */
      boolean zombie;

      Village(int type, BoundingBox box) {
         super(RLGen.RUINS_PIECE.get(), type, box);
      }

      Village(Start start, int type, BoundingBox box) {
         this(type, box);
         this.zombie = start.zombie;
      }

      void placeTorch(WorldGenLevel level, Direction facing, int x, int y, int z, BoundingBox box) {
         if (!this.zombie) {
            this.placeBlock(level, Blocks.WALL_TORCH.defaultBlockState().setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, facing), x, y, z, box);
         }
      }

      /** StructureComponent.clearCurrentPositionBlocksUpwards */
      void clearUpwards(WorldGenLevel level, int x, int y, int z, BoundingBox box) {
         BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(this.getWorldX(x, z), this.getWorldY(y), this.getWorldZ(x, z));
         if (box.isInside(p)) {
            while (!level.isEmptyBlock(p) && p.getY() < 255) {
               level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
               p.move(Direction.UP);
            }
         }
      }

      void buildComponent(Start start, List<Village> list, Random rand) {
      }

      abstract void addComponentParts(WorldGenLevel level, Random random, BoundingBox box);

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      }

      @Override
      public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen, RandomSource random, BoundingBox box, ChunkPos chunk, BlockPos pivot) {
      }

      Village getNextComponentNN(Start start, List<Village> list, Random rand, int y, int offset) {
         Direction d = this.getOrientation();
         if (d == null) {
            return null;
         }
         return switch (d) {
            case WEST, EAST -> generateAndAddComponent(start, list, rand, this.boundingBox.minX() + offset, this.boundingBox.minY() + y,
               this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth());
            default -> generateAndAddComponent(start, list, rand, this.boundingBox.minX() - 1, this.boundingBox.minY() + y,
               this.boundingBox.minZ() + offset, Direction.WEST, this.getGenDepth());
         };
      }

      Village getNextComponentPP(Start start, List<Village> list, Random rand, int y, int offset) {
         Direction d = this.getOrientation();
         if (d == null) {
            return null;
         }
         return switch (d) {
            case WEST, EAST -> generateAndAddComponent(start, list, rand, this.boundingBox.minX() + offset, this.boundingBox.minY() + y,
               this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth());
            default -> generateAndAddComponent(start, list, rand, this.boundingBox.maxX() + 1, this.boundingBox.minY() + y,
               this.boundingBox.minZ() + offset, Direction.EAST, this.getGenDepth());
         };
      }

      static boolean canVillageGoDeeper(BoundingBox b) {
         return b != null && b.minY() > 10;
      }
   }

   static final class Well extends Village {
      private final boolean snow;

      Well(Random rand, int x, int z, boolean snow) {
         super(0, snow ? new BoundingBox(x, 64, z, x + 6 - 1, 78, z + 6 - 1) : new BoundingBox(x, 64, z, x + 3, 64, z + 3));
         this.snow = snow;
         this.setOrientation(HORIZONTAL[rand.nextInt(HORIZONTAL.length)]);
         this.groundOffset = snow ? 3 : -1;
         this.groundShift = snow ? 0 : 1;
      }

      @Override
      void buildComponent(Start start, List<Village> list, Random rand) {
         BoundingBox b = this.boundingBox;
         int y = b.maxY() - 4;
         if (this.snow) {
            generateAndAddRoadPiece(start, list, rand, b.minX() - 1, y, b.minZ() + 1, Direction.WEST, this.getGenDepth());
            generateAndAddRoadPiece(start, list, rand, b.maxX() + 1, y, b.minZ() + 1, Direction.EAST, this.getGenDepth());
            generateAndAddRoadPiece(start, list, rand, b.minX() + 1, y, b.minZ() - 1, Direction.NORTH, this.getGenDepth());
            generateAndAddRoadPiece(start, list, rand, b.minX() + 1, y, b.maxZ() + 1, Direction.SOUTH, this.getGenDepth());
         } else {
            generateAndAddRoadPiece(start, list, rand, b.minX(), y, b.minZ() + 2, Direction.WEST, this.getGenDepth());
            generateAndAddRoadPiece(start, list, rand, b.maxX() + 1, y, b.minZ() + 2, Direction.EAST, this.getGenDepth());
            generateAndAddRoadPiece(start, list, rand, b.minX() + 2, y, b.minZ(), Direction.NORTH, this.getGenDepth());
            generateAndAddRoadPiece(start, list, rand, b.minX() + 2, y, b.maxZ() + 1, Direction.SOUTH, this.getGenDepth());
         }
      }

      @Override
      void addComponentParts(WorldGenLevel level, Random random, BoundingBox box) {
         if (this.snow) {
            BlockState ice = Blocks.PACKED_ICE.defaultBlockState();
            BlockState water = Blocks.WATER.defaultBlockState();
            BlockState torch = Blocks.TORCH.defaultBlockState();
            this.generateBox(level, box, 1, 0, 1, 4, 12, 4, ice, water, false);
            this.placeBlock(level, water, 2, 12, 2, box);
            this.placeBlock(level, water, 3, 12, 2, box);
            this.placeBlock(level, water, 2, 12, 3, box);
            this.placeBlock(level, water, 3, 12, 3, box);
            this.placeBlock(level, torch, 1, 12, 1, box);
            this.placeBlock(level, torch, 4, 12, 1, box);
            this.placeBlock(level, torch, 1, 12, 4, box);
            this.placeBlock(level, torch, 4, 12, 4, box);
            return;
         }
         this.generateBox(level, box, 0, 0, 0, 3, 0, 3, Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.DIRT_PATH.defaultBlockState(), false);
         BlockPos tree = new BlockPos(this.getWorldX(1, 2), this.getWorldY(1), this.getWorldZ(1, 2));
         if (box.isInside(tree)) {
            // WorldGenCanopyTree: a vanilla dark oak
            level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(TreeFeatures.DARK_OAK).ifPresent(f ->
               f.value().place(level, level.getLevel().getChunkSource().getGenerator(), RandomSource.create(random.nextLong()), tree));
         }
         this.generateBox(level, box, 0, 0, 0, 3, 0, 3, Blocks.DIRT_PATH.defaultBlockState(), Blocks.DIRT_PATH.defaultBlockState(), false);
         for (int l = 0; l < 3; l++) {
            for (int k = 0; k < 3; k++) {
               this.fillColumnDown(level, Blocks.DIRT.defaultBlockState(), l, -1, k, box);
            }
         }
      }
   }

   static final class Start extends Village {
      final Well well;
      final int terrainType;
      final List<PieceWeight> weights;
      PieceWeight lastWeight;
      final List<Village> pendingHouses = new ArrayList<>();
      final List<Village> pendingRoads = new ArrayList<>();
      final boolean snow;

      Start(Random rand, int x, int z, List<PieceWeight> weights, int size, boolean snow) {
         super(0, new BoundingBox(x, 64, z, x + 3, 64, z + 3));
         this.snow = snow;
         this.well = new Well(rand, x, z, snow);
         this.setOrientation(this.well.getOrientation());
         this.boundingBox = this.well.getBoundingBox();
         this.weights = weights;
         this.terrainType = size;
         this.groundOffset = this.well.groundOffset;
         this.groundShift = this.well.groundShift;
         this.zombie = rand.nextInt(50) == 0;
      }

      @Override
      void buildComponent(Start start, List<Village> list, Random rand) {
         this.well.buildComponent(start, list, rand);
      }

      @Override
      void addComponentParts(WorldGenLevel level, Random random, BoundingBox box) {
         this.well.addComponentParts(level, random, box);
      }

      @Override
      public void move(int x, int y, int z) {
         this.well.move(x, y, z);
         this.boundingBox = this.well.getBoundingBox();
      }
   }

   static final class Path extends Village {
      private final int length;
      private final boolean snow;

      Path(int type, BoundingBox box, Direction facing, boolean snow) {
         super(type, box);
         this.setOrientation(facing);
         this.length = Math.max(box.getXSpan(), box.getZSpan());
         this.snow = snow;
      }

      static BoundingBox findPieceBox(List<Village> list, Random rand, int x, int y, int z, Direction facing, boolean snow) {
         int step = snow ? 7 : 3;
         int width = snow ? 3 : 1;
         for (int i = step * randInt(rand, 3, 5); i >= step; i -= step) {
            BoundingBox b = BoundingBox.orientBox(x, y, z, 0, 0, 0, width, width, i, facing);
            if (findIntersecting(list, b) == null) {
               return b;
            }
         }
         return null;
      }

      @Override
      void buildComponent(Start start, List<Village> list, Random rand) {
         boolean flag = false;
         for (int i = rand.nextInt(5); i < this.length - 8; i += 2 + rand.nextInt(5)) {
            Village c = this.getNextComponentNN(start, list, rand, 0, i);
            if (c != null) {
               i += Math.max(c.getBoundingBox().getXSpan(), c.getBoundingBox().getZSpan());
               flag = true;
            }
         }
         for (int j = rand.nextInt(5); j < this.length - 8; j += 2 + rand.nextInt(5)) {
            Village c = this.getNextComponentPP(start, list, rand, 0, j);
            if (c != null) {
               j += Math.max(c.getBoundingBox().getXSpan(), c.getBoundingBox().getZSpan());
               flag = true;
            }
         }
         Direction d = this.getOrientation();
         BoundingBox b = this.boundingBox;
         if (flag && rand.nextInt(3) > 0 && d != null) {
            switch (d) {
               case SOUTH -> generateAndAddRoadPiece(start, list, rand, b.minX() - 1, b.minY(), b.maxZ() - 2, Direction.WEST, this.getGenDepth());
               case WEST -> generateAndAddRoadPiece(start, list, rand, b.minX(), b.minY(), b.minZ() - 1, Direction.NORTH, this.getGenDepth());
               case EAST -> generateAndAddRoadPiece(start, list, rand, b.maxX() - 2, b.minY(), b.minZ() - 1, Direction.NORTH, this.getGenDepth());
               default -> generateAndAddRoadPiece(start, list, rand, b.minX() - 1, b.minY(), b.minZ(), Direction.WEST, this.getGenDepth());
            }
         }
         if (flag && rand.nextInt(3) > 0 && d != null) {
            switch (d) {
               case SOUTH -> generateAndAddRoadPiece(start, list, rand, b.maxX() + 1, b.minY(), b.maxZ() - 2, Direction.EAST, this.getGenDepth());
               case WEST -> generateAndAddRoadPiece(start, list, rand, b.minX(), b.minY(), b.maxZ() + 1, Direction.SOUTH, this.getGenDepth());
               case EAST -> generateAndAddRoadPiece(start, list, rand, b.maxX() - 2, b.minY(), b.maxZ() + 1, Direction.SOUTH, this.getGenDepth());
               default -> generateAndAddRoadPiece(start, list, rand, b.maxX() + 1, b.minY(), b.minZ(), Direction.EAST, this.getGenDepth());
            }
         }
      }

      @Override
      void addComponentParts(WorldGenLevel level, Random random, BoundingBox box) {
         BlockState path = this.snow ? RLGen.FROZEN_GRASS_PATH.get().defaultBlockState() : Blocks.DIRT_PATH.defaultBlockState();
         BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
         BlockState gravel = Blocks.GRAVEL.defaultBlockState();
         BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
         for (int i = this.boundingBox.minX(); i <= this.boundingBox.maxX(); i++) {
            for (int j = this.boundingBox.minZ(); j <= this.boundingBox.maxZ(); j++) {
               BlockPos blockpos = new BlockPos(i, 64, j);
               if (box.isInside(blockpos)) {
                  blockpos = new BlockPos(i, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, i, j), j).below();
                  if (blockpos.getY() < 63) {
                     blockpos = new BlockPos(i, 62, j);
                  }
                  while (blockpos.getY() >= 62) {
                     BlockState s = level.getBlockState(blockpos);
                     if (this.snow && s.is(Blocks.GRASS_BLOCK)) {
                        if (level.isEmptyBlock(blockpos.above())) {
                           level.setBlock(blockpos, path, 2);
                           break;
                        }
                     } else if (s.is(Blocks.GRASS_BLOCK)) {
                        if (!level.isEmptyBlock(blockpos.above())) {
                           level.setBlock(blockpos.above(), Blocks.AIR.defaultBlockState(), 2);
                        }
                        if (!level.isEmptyBlock(blockpos.above(2))) {
                           level.setBlock(blockpos.above(2), Blocks.AIR.defaultBlockState(), 2);
                        }
                        level.setBlock(blockpos, path, 2);
                        break;
                     }
                     if (!s.liquid() && !s.is(Blocks.ICE)) {
                        if (!s.is(Blocks.SAND) && !s.is(Blocks.SANDSTONE) && !s.is(Blocks.RED_SANDSTONE)) {
                           blockpos = blockpos.below();
                           continue;
                        }
                        level.setBlock(blockpos, gravel, 2);
                        level.setBlock(blockpos.below(), cobble, 2);
                        break;
                     }
                     level.setBlock(blockpos, planks, 2);
                     break;
                  }
               }
            }
         }
      }
   }

   static final class PixieHouse extends Village {
      PixieHouse(int type, BoundingBox box, Direction facing) {
         super(type, box);
         this.setOrientation(facing);
         this.groundOffset = 0;
      }

      static PixieHouse createPiece(Start start, List<Village> list, int x, int y, int z, Direction facing, int type) {
         BoundingBox b = BoundingBox.orientBox(x, y, z, 0, 0, 0, 1, 1, 1, facing);
         return canVillageGoDeeper(b) && findIntersecting(list, b) == null ? new PixieHouse(type, b, facing) : null;
      }

      @Override
      void addComponentParts(WorldGenLevel level, Random random, BoundingBox box) {
         this.fillColumnDown(level, Blocks.DIRT.defaultBlockState(), 0, -1, 0, box);
         Direction facing = this.getOrientation() == null ? Direction.NORTH : this.getOrientation().getOpposite();
         BlockPos pos = new BlockPos(this.getWorldX(0, 0), this.getWorldY(0), this.getWorldZ(0, 0));
         if (box.isInside(pos)) {
            level.setBlock(pos, RLGen.PIXIE_HOUSE.get().defaultBlockState().setValue(PixieHouseBlock.FACING, facing)
               .setValue(PixieHouseBlock.TYPE, random.nextInt(5)), 2);
         }
      }
   }

   /** SnowVillagePieces.TorchNew: a spruce fence post with a black wool block and four torches. */
   static final class TorchNew extends Village {
      TorchNew(Start start, int type, BoundingBox box, Direction facing) {
         super(start, type, box);
         this.setOrientation(facing);
         this.groundOffset = 5;
      }

      static TorchNew createPiece(Start start, List<Village> list, int x, int y, int z, Direction facing, int type) {
         BoundingBox b = BoundingBox.orientBox(x, y, z, 0, 0, 0, 4, 6, 5, facing);
         return canVillageGoDeeper(b) && findIntersecting(list, b) == null ? new TorchNew(start, type, b, facing) : null;
      }

      @Override
      void addComponentParts(WorldGenLevel level, Random random, BoundingBox box) {
         BlockState fence = Blocks.SPRUCE_FENCE.defaultBlockState();
         this.placeBlock(level, fence, 1, 0, 0, box);
         this.placeBlock(level, fence, 1, 1, 0, box);
         this.placeBlock(level, fence, 1, 2, 0, box);
         this.placeBlock(level, Blocks.BLACK_WOOL.defaultBlockState(), 1, 3, 0, box);
         this.placeTorch(level, Direction.EAST, 2, 3, 0, box);
         this.placeTorch(level, Direction.NORTH, 1, 3, 1, box);
         this.placeTorch(level, Direction.WEST, 0, 3, 0, box);
         this.placeTorch(level, Direction.SOUTH, 1, 3, -1, box);
      }
   }

   /** SnowVillagePieces.WoodHut: a small packed ice and snow hut on a frozen cobblestone floor. */
   static final class WoodHut extends Village {
      private final boolean isTallHouse;
      private final int tablePosition;

      WoodHut(Start start, int type, Random rand, BoundingBox box, Direction facing) {
         super(start, type, box);
         this.setOrientation(facing);
         this.isTallHouse = rand.nextBoolean();
         this.tablePosition = rand.nextInt(3);
         this.groundOffset = 5;
      }

      static WoodHut createPiece(Start start, List<Village> list, Random rand, int x, int y, int z, Direction facing, int type) {
         BoundingBox b = BoundingBox.orientBox(x, y, z, 0, 0, 0, 4, 6, 5, facing);
         return canVillageGoDeeper(b) && findIntersecting(list, b) == null ? new WoodHut(start, type, rand, b, facing) : null;
      }

      @Override
      void addComponentParts(WorldGenLevel level, Random random, BoundingBox box) {
         BlockState floor = RLGen.FROZEN_COBBLESTONE.get().defaultBlockState();
         BlockState snow = Blocks.SNOW_BLOCK.defaultBlockState();
         BlockState stairs = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH);
         BlockState ice = Blocks.PACKED_ICE.defaultBlockState();
         BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
         BlockState air = Blocks.AIR.defaultBlockState();
         this.generateBox(level, box, 1, 1, 1, 3, 5, 4, air, air, false);
         this.generateBox(level, box, 0, 0, 0, 3, 0, 4, floor, floor, false);
         if (this.isTallHouse) {
            this.generateBox(level, box, 1, 4, 1, 2, 4, 3, ice, ice, false);
         } else {
            this.generateBox(level, box, 1, 5, 1, 2, 5, 3, ice, ice, false);
         }
         this.placeBlock(level, ice, 1, 4, 0, box);
         this.placeBlock(level, ice, 2, 4, 0, box);
         this.placeBlock(level, ice, 1, 4, 4, box);
         this.placeBlock(level, ice, 2, 4, 4, box);
         this.placeBlock(level, ice, 0, 4, 1, box);
         this.placeBlock(level, ice, 0, 4, 2, box);
         this.placeBlock(level, ice, 0, 4, 3, box);
         this.placeBlock(level, ice, 3, 4, 1, box);
         this.placeBlock(level, ice, 3, 4, 2, box);
         this.placeBlock(level, ice, 3, 4, 3, box);
         this.generateBox(level, box, 0, 1, 0, 0, 3, 0, ice, ice, false);
         this.generateBox(level, box, 3, 1, 0, 3, 3, 0, ice, ice, false);
         this.generateBox(level, box, 0, 1, 4, 0, 3, 4, ice, ice, false);
         this.generateBox(level, box, 3, 1, 4, 3, 3, 4, ice, ice, false);
         this.generateBox(level, box, 0, 1, 1, 0, 3, 3, snow, snow, false);
         this.generateBox(level, box, 3, 1, 1, 3, 3, 3, snow, snow, false);
         this.generateBox(level, box, 1, 1, 0, 2, 3, 0, snow, snow, false);
         this.generateBox(level, box, 1, 1, 4, 2, 3, 4, snow, snow, false);
         this.placeBlock(level, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState(), 0, 2, 2, box);
         this.placeBlock(level, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState(), 3, 2, 2, box);
         if (this.tablePosition > 0) {
            this.placeBlock(level, fence, this.tablePosition, 1, 3, box);
            this.placeBlock(level, Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), this.tablePosition, 2, 3, box);
         }
         this.placeBlock(level, air, 1, 1, 0, box);
         this.placeBlock(level, air, 1, 2, 0, box);
         if (!this.zombie) {
            BlockState door = Blocks.SPRUCE_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.NORTH);
            this.placeBlock(level, door, 1, 1, 0, box);
            this.placeBlock(level, door.setValue(net.minecraft.world.level.block.DoorBlock.HALF,
               net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 1, 2, 0, box);
         }
         if (this.getBlock(level, 1, 0, -1, box).isAir() && !this.getBlock(level, 1, -1, -1, box).isAir()) {
            this.placeBlock(level, stairs, 1, 0, -1, box);
            if (this.getBlock(level, 1, -1, -1, box).is(RLGen.FROZEN_GRASS_PATH.get())) {
               this.placeBlock(level, Blocks.GRASS_BLOCK.defaultBlockState(), 1, -1, -1, box);
            }
         }
         for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 4; j++) {
               this.clearUpwards(level, j, 6, i, box);
               this.fillColumnDown(level, floor, j, -1, i, box);
            }
         }
      }
   }
}
