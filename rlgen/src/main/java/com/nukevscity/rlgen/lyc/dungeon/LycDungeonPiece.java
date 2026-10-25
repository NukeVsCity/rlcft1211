package com.nukevscity.rlgen.lyc.dungeon;

import com.lycanitesmobs.core.dungeon.instance.DungeonInstance;
import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import com.yungnickyoung.minecraft.bettermineshafts.mc.World;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.slf4j.Logger;

/**
 * What one Lycanites dungeon puts into one 1.21 chunk: the builds of the four 1.12 populate areas (chunk + 8) that overlap it, each
 * on a buffered world so it sees its own blocks, keeping the blocks inside this chunk.
 */
public final class LycDungeonPiece extends StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final int dungeon;

   LycDungeonPiece(ChunkPos chunk, int dungeon) {
      super(RLGen.LYC_DUNGEON_PIECE.get(), 0, new BoundingBox(chunk.getMinBlockX(), -64, chunk.getMinBlockZ(), chunk.getMaxBlockX(), 319, chunk.getMaxBlockZ()));
      this.dungeon = dungeon;
   }

   public LycDungeonPiece(StructurePieceSerializationContext context, CompoundTag tag) {
      super(RLGen.LYC_DUNGEON_PIECE.get(), tag);
      this.dungeon = tag.getInt("D");
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      tag.putInt("D", this.dungeon);
   }

   @Override
   public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox box,
                           ChunkPos chunkPos, BlockPos pivot) {
      if (!(chunkGenerator instanceof LegacyChunkGenerator generator)) {
         return;
      }
      DungeonInstance instance = LycDungeons.instance(generator, this.dungeon);
      if (instance == null || instance.layout == null) {
         return;
      }
      for (int px = chunkPos.x - 1; px <= chunkPos.x; px++) {
         for (int pz = chunkPos.z - 1; pz <= chunkPos.z; pz++) {
            ChunkPos p = new ChunkPos(px, pz);
            if (!instance.layout.sectorChunkMap.containsKey(p)) {
               continue;
            }
            World world = new World(level, generator, chunkPos);
            // world.rand in 1.12
            Random random = new Random(instance.getSeed() ^ p.toLong() * 0x2545F4914F6CDD1DL);
            try {
               synchronized (instance) {
                  instance.buildChunk(world, p, random);
               }
            } catch (RuntimeException e) {
               // WorldGeneratorDungeon caught it and logged a warning; the rest of that chunk's build was skipped
               LOGGER.debug("rlgen: Lycanites dungeon build stopped in {}", p, e);
            }
            for (BlockPos pos : world.flush()) {
               BlockState s = level.getBlockState(pos);
               if (s.getBlock() instanceof StairBlock) {
                  BlockState u = Block.updateFromNeighbourShapes(s, level, pos);
                  if (u != s && !u.isAir()) {
                     level.setBlock(pos, u, 2 | 16);
                  }
               }
            }
         }
      }
   }
}
