package com.nukevscity.rlgen.bms;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.rc.RcGenerator;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import com.yungnickyoung.minecraft.bettermineshafts.mc.StructureBoundingBox;
import com.yungnickyoung.minecraft.bettermineshafts.mc.StructureComponent;
import com.yungnickyoung.minecraft.bettermineshafts.mc.World;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.slf4j.Logger;

/**
 * A whole Better Mineshaft as one 1.21 piece. Its 1.12 components are rebuilt from the seed whenever needed; for each 1.12 populate
 * area (chunk + 8) overlapping the decorated chunk, they are placed like MapGenStructure.generateStructure did, with that chunk's
 * populate random (which Recurrent Complex had drawn from first, in PopulateChunkEvent.Pre).
 */
public final class MineshaftHolderPiece extends StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final int startX;
   private final int startZ;

   MineshaftHolderPiece(int startX, int startZ, StructureBoundingBox box) {
      super(RLGen.MINESHAFT_PIECE.get(), 0, new BoundingBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ));
      this.startX = startX;
      this.startZ = startZ;
   }

   public MineshaftHolderPiece(StructurePieceSerializationContext context, CompoundTag tag) {
      super(RLGen.MINESHAFT_PIECE.get(), tag);
      this.startX = tag.getInt("SX");
      this.startZ = tag.getInt("SZ");
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      tag.putInt("SX", this.startX);
      tag.putInt("SZ", this.startZ);
   }

   private static boolean needsShape(Block b) {
      return b instanceof FenceBlock || b instanceof WallBlock || b instanceof IronBarsBlock || b instanceof StairBlock;
   }

   @Override
   public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox box,
                           ChunkPos chunkPos, BlockPos pivot) {
      if (!(chunkGenerator instanceof LegacyChunkGenerator generator)) {
         return;
      }
      long seed = level.getSeed();
      try {
         for (int px = chunkPos.x - 1; px <= chunkPos.x; px++) {
            for (int pz = chunkPos.z - 1; pz <= chunkPos.z; pz++) {
               StructureBoundingBox area = new StructureBoundingBox(px * 16 + 8, pz * 16 + 8, px * 16 + 23, pz * 16 + 23);
               List<StructureComponent> components = MineshaftStructure.start(generator, seed, this.startX, this.startZ);
               if (components == null) {
                  return;
               }
               World world = null;
               Random rand = null;
               for (StructureComponent c : components) {
                  if (c.getBoundingBox().intersectsWith(area)) {
                     if (world == null) {
                        world = new World(level, generator, chunkPos);
                        rand = populateRandom(seed, px, pz, generator);
                     }
                     c.addComponentParts(world, rand, area);
                  }
               }
               if (world != null) {
                  List<BlockPos> placed = world.flush();
                  for (BlockPos p : placed) {
                     BlockState s = level.getBlockState(p);
                     if (needsShape(s.getBlock())) {
                        BlockState u = Block.updateFromNeighbourShapes(s, level, p);
                        if (u != s && !u.isAir()) {
                           level.setBlock(p, u, 2 | 16);
                        }
                     }
                  }
               }
            }
         }
      } catch (RuntimeException e) {
         LOGGER.error("rlgen: mineshaft {} {} failed in chunk {}", this.startX, this.startZ, chunkPos, e);
      }
   }

   /** ChunkGeneratorOverworld.populate's random as MapGenMineshaft.generateStructure got it (after RC's PopulateChunkEvent.Pre draws). */
   private static Random populateRandom(long seed, int cx, int cz, LegacyChunkGenerator generator) {
      Random rand = new Random(seed);
      long i1 = rand.nextLong() / 2L * 2L + 1L;
      long j1 = rand.nextLong() / 2L * 2L + 1L;
      rand.setSeed((long)cx * i1 + (long)cz * j1 ^ seed);
      RcGenerator.consumeSelection(rand, cx, cz, generator.legacyGenerator());
      return rand;
   }
}
